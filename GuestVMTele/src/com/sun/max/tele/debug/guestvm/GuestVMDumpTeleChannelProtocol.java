/*
 * Copyright (c) 2007 Sun Microsystems, Inc.  All rights reserved.
 *
 * Sun Microsystems, Inc. has intellectual property rights relating to technology embodied in the product
 * that is described in this document. In particular, and without limitation, these intellectual property
 * rights may include one or more of the U.S. patents listed at http://www.sun.com/patents and one or
 * more additional patents or pending patent applications in the U.S. and in other countries.
 *
 * U.S. Government Rights - Commercial software. Government users are subject to the Sun
 * Microsystems, Inc. standard license agreement and applicable provisions of the FAR and its
 * supplements.
 *
 * Use is subject to license terms. Sun, Sun Microsystems, the Sun logo, Java and Solaris are trademarks or
 * registered trademarks of Sun Microsystems, Inc. in the U.S. and other countries. All SPARC trademarks
 * are used under license and are trademarks or registered trademarks of SPARC International, Inc. in the
 * U.S. and other countries.
 *
 * UNIX is a registered trademark in the U.S. and other countries, exclusively licensed through X/Open
 * Company, Ltd.
 */
package com.sun.max.tele.debug.guestvm;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.sun.guestvm.guk.x64.X64VM;
import com.sun.max.program.ProgramError;
import com.sun.max.program.Trace;
import com.sun.max.tele.TeleVM;
import com.sun.max.tele.channel.TeleChannelProtocol;
import com.sun.max.tele.channel.iostream.TeleChannelDataIOProtocolAdaptor;
import com.sun.max.tele.debug.ProcessState;
import com.sun.max.tele.debug.guestvm.xen.PageTableAccess;
import com.sun.max.tele.debug.guestvm.xen.dump.CoreDumpPageTableAccess;
import com.sun.max.tele.debug.guestvm.xen.dump.GuestContext;
import com.sun.max.tele.debug.guestvm.xen.dump.XenCoreDumpELFReader;
import com.sun.max.unsafe.Address;
import com.sun.max.vm.runtime.FatalError;
/**
 * @author Puneeet Lakhina
 * @author Mick Jordan
 *
 */
public class GuestVMDumpTeleChannelProtocol extends TeleChannelDataIOProtocolAdaptor implements GuestVMTeleChannelProtocol {

    private ImageFileHandler imageFileHandler;
    private XenCoreDumpELFReader xenReader = null;
    private PageTableAccess pageTableAccess;
    private int threadLocalsAreaSize;
    /**
     * Creates an instance of {@link TeleChannelProtocol} that can read from Xen core dumps.
     *
     * @param dumpImageFileStr designates the dump file and image file separated by a comma (",")
     */
    private GUKThreadListAccess tla;

    public GuestVMDumpTeleChannelProtocol(TeleVM teleVM, File imageFile, File dumpFile) {
        try {
            this.imageFileHandler = ImageFileHandler.open(imageFile);
            xenReader = new XenCoreDumpELFReader(new RandomAccessFile(dumpFile, "r"));
            pageTableAccess = new CoreDumpPageTableAccess(xenReader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean activateWatchpoint(long start, long size, boolean after, boolean read, boolean write, boolean exec) {
        inappropriate("activateWatchpoint");
        return false;
    }

    @Override
    public boolean initialize(int threadLocalsAreaSize, boolean bigEndian) {
    	this.threadLocalsAreaSize = threadLocalsAreaSize;
        return true;
    }

    @Override
    public void setNativeAddresses(long threadListAddress, long bootHeapStartAddress, long resumeAddress) {
    	
    }

    @Override
    public boolean attach(int id) {
		tla = new GUKThreadListAccess(this, threadLocalsAreaSize, imageFileHandler.getThreadListSymbolAddress());
        return true;
    }

    @Override
    public boolean detach() {
        // nothing to do
        return false;
    }

    @Override
    public boolean deactivateWatchpoint(long start, long size) {
        inappropriate("deactivateWatchpoint");
        return false;
    }

    @Override
    public boolean gatherThreads(Object teleDomainObject, Object threadSequence, long threadLocalsList, long primordialThreadLocals) {
        // we use the GUKThreadListAccess class
        return tla.gatherThreads(teleDomainObject, threadSequence, threadLocalsList, primordialThreadLocals);
     }

    @Override
    public long getBootHeapStart() {
        long address = imageFileHandler.getBootHeapStartSymbolAddress();
        try {
            //This essentially assumes 64 bitness of the address and the target.
            return xenReader.getPagesSection().getX64WordAtOffset(address);
        } catch (Exception e) {
        	e.printStackTrace();
            ProgramError.unexpected("Couldnt get Boot Heap start from the dump File");
        }
        return 0;
    }

    @Override
    public int maxByteBufferSize() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int readBytes(long src, byte[] dst, int dstOffset, int length) {
        //Resolve the address
    	try {
    		Address l1pte = pageTableAccess.getAddressForPte(pageTableAccess.getPteForAddress(Address.fromLong(src)));
    		long physicalAddr = l1pte.toLong() + (src & (X64VM.L0_ENTRIES-1));
			return xenReader.getPagesSection().readBytes(physicalAddr, dst, dstOffset, length);
		} catch (Exception e) {
			e.printStackTrace();
		}
        return 0;
    }

	@Override
	public boolean readRegisters(long threadId, byte[] integerRegisters,
			int integerRegistersSize, byte[] floatingPointRegisters,
			int floatingPointRegistersSize, byte[] stateRegisters,
			int stateRegistersSize) {
		GUKThreadListAccess.GUKThreadInfo threadInfo =(GUKThreadListAccess.GUKThreadInfo ) tla.getThreadInfo((int) threadId);
		if (threadInfo.regsAvail) {
			System.arraycopy(threadInfo.integerRegisters, 0, integerRegisters, 0, integerRegisters.length);
			System.arraycopy(threadInfo.floatingPointRegisters, 0, floatingPointRegisters, 0, floatingPointRegisters.length);
			System.arraycopy(threadInfo.stateRegisters, 0, stateRegisters, 0, stateRegisters.length);
		} else {
			// we are filling in the cache in threadInfo
			assert threadInfo.integerRegisters == integerRegisters;
			try {
				GuestContext context = xenReader.getGuestContext(tla
						.getCpu((int) threadId));
				context.getCpuUserRegs().canonicalizeTeleIntegerRegisters(
						integerRegisters);
				context.getCpuUserRegs().canonicalizeTeleStateRegisters(
						stateRegisters);
				System.arraycopy(context.getfpuRegisters(), 0,
						floatingPointRegisters, 0, floatingPointRegistersSize);
				threadInfo.regsAvail = true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

    @Override
    public int readWatchpointAccessCode() {
        inappropriate("readWatchpointAccessCode");
        return 0;
    }

    @Override
    public long readWatchpointAddress() {
        inappropriate("readWatchpointAddress");
        return 0;
    }

    @Override
    public boolean resume(long threadId) {
        inappropriate("resume");
        return false;
    }
    
    @Override
    public boolean resumeAll() {
        inappropriate("resumeAll");
        return false;
    }

    @Override
    public boolean kill() {
        inappropriate("kill");
        return false;
    }

    @Override
    public int waitUntilStoppedAsInt() {
    	inappropriate("waitUntilStoppedAsInt");
    	return 0;
    }

    @Override
    public ProcessState waitUntilStopped() {
    	inappropriate("waitUntilStopped");
    	return ProcessState.UNKNOWN;
    }

    @Override
    public boolean setInstructionPointer(long threadId, long ip) {
        inappropriate("setInstructionPointer");
        return false;
    }

    @Override
    public int setTransportDebugLevel(int level) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean singleStep(long threadId) {
        inappropriate("singleStep");
        return false;
    }

    @Override
    public boolean suspend(long threadId) {
        inappropriate("suspend");
        return false;
    }

    @Override
    public boolean suspendAll() {
        inappropriate("suspendAll");
        return false;
    }
    
    @Override
    public int writeBytes(long dst, byte[] src, int srcOffset, int length) {
        Trace.line(2, "WARNING: Inspector trying to write to " + Long.toHexString(dst));
        return length;
    }

    static void inappropriate(String name) {
        FatalError.unexpected("DumpProtocol: inappropriate method: " + name + " invoked");
    }
}
