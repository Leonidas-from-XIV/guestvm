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
import java.nio.ByteBuffer;
import java.util.List;

import com.sun.max.tele.MaxWatchpoint.WatchpointSettings;
import com.sun.max.tele.TeleVM;
import com.sun.max.tele.debug.ProcessState;
import com.sun.max.tele.debug.TeleNativeThread;
import com.sun.max.tele.debug.TeleWatchpoint;
import com.sun.max.tele.memory.TeleFixedMemoryRegion;
import com.sun.max.unsafe.Address;
import com.sun.max.unsafe.Pointer;
import com.sun.max.vm.runtime.FatalError;

/**
 * This class encapsulates all interaction with the Xen db communication channel and ensures
 * that access is single-threaded.
 *
 * @author Mick Jordan
 *
 */
public final class GuestVMXenDBChannel {
    private static GuestVMTeleDomain teleDomain;
    private static GuestVMTeleChannelProtocol channelProtocol;
    private static int maxByteBufferSize;
    
    public static synchronized void attach(GuestVMTeleDomain teleDomain, int domId) {
        GuestVMXenDBChannel.teleDomain = teleDomain;
        channelProtocol = (GuestVMTeleChannelProtocol) TeleVM.teleChannelProtocol();
        channelProtocol.initialize(teleDomain.vm().bootImage().header.tlaSize, false);
        // To avoid having to replicate the DB/XG sub-variant on this side of the channel protocol,
        // we always call setNativeAddresses, even though the DB/Dump modes don't need it.
        final File maxvm = new File(teleDomain.vm().vmDirectory(), "maxvm");
        try {
            final ImageFileHandler fh  = ImageFileHandler.open(maxvm);
            channelProtocol.setNativeAddresses(fh.getThreadListSymbolAddress(), fh.getBootHeapStartSymbolAddress(), fh.getSymbolAddress("xg_resume_flag"));
        } catch (Exception ex) {
        	FatalError.unexpected("failed to open maxvm image file", ex);
        }
        channelProtocol.attach(domId);
        maxByteBufferSize = channelProtocol.maxByteBufferSize();
    }

    public static synchronized Pointer getBootHeapStart() {
        return Pointer.fromLong(channelProtocol.getBootHeapStart());
    }

    public static synchronized void setTransportDebugLevel(int level) {
        channelProtocol.setTransportDebugLevel(level);
    }

    private static int readBytes0(long src, ByteBuffer dst, int dstOffset, int length) {
        assert dst.limit() - dstOffset >= length;
        if (dst.isDirect()) {
            return channelProtocol.readBytes(src, dst, dstOffset, length);
        }
        assert dst.array() != null;
        return channelProtocol.readBytes(src, dst.array(), dst.arrayOffset() + dstOffset, length);
    }

    public static synchronized int readBytes(Address src, ByteBuffer dst, int dstOffset, int length) {
        int lengthLeft = length;
        int localOffset = dstOffset;
        long localAddress = src.toLong();
        while (lengthLeft > 0) {
            final int toDo = lengthLeft > maxByteBufferSize ? maxByteBufferSize : lengthLeft;
            final int r = readBytes0(localAddress, dst, localOffset, toDo);
            if (r != toDo) {
                return -1;
            }
            lengthLeft -= toDo;
            localOffset += toDo;
            localAddress += toDo;
        }
        return length;
    }

    private static int writeBytes0(long dst, ByteBuffer src, int srcOffset, int length) {
        assert src.limit() - srcOffset >= length;
        if (src.isDirect()) {
            return channelProtocol.writeBytes(dst, src, srcOffset, length);
        }
        assert src.array() != null;
        return channelProtocol.writeBytes(dst, src.array(), src.arrayOffset() + srcOffset, length);

    }

    public static synchronized int writeBytes(ByteBuffer buffer, int offset, int length, Address address) {
        int lengthLeft = length;
        int localOffset = offset;
        long localAddress = address.toLong();
        while (lengthLeft > 0) {
            final int toDo = lengthLeft > maxByteBufferSize ? maxByteBufferSize : lengthLeft;
            final int r = writeBytes0(localAddress, buffer, localOffset, toDo);
            if (r != toDo) {
                return -1;
            }
            lengthLeft -= toDo;
            localOffset += toDo;
            localAddress += toDo;
        }
        return length;
    }

    public static synchronized void gatherThreads(List<TeleNativeThread> threads, long threadLocalsList, long primordialThreadLocals) {
        channelProtocol.gatherThreads(teleDomain, threads, threadLocalsList, primordialThreadLocals);
    }

    public static synchronized boolean resume(int domainId) {
        return channelProtocol.resume(0);
    }
    
    public static synchronized ProcessState waitUntilStopped() {
    	return channelProtocol.waitUntilStopped();
    }

    public static synchronized boolean setInstructionPointer(int threadId, long ip) {
        return channelProtocol.setInstructionPointer(threadId, ip);
    }

    public static synchronized boolean readRegisters(int threadId, byte[] integerRegisters, int integerRegistersSize, byte[] floatingPointRegisters, int floatingPointRegistersSize,
                    byte[] stateRegisters, int stateRegistersSize) {
        return channelProtocol.readRegisters(threadId, integerRegisters, integerRegistersSize, floatingPointRegisters, floatingPointRegistersSize, stateRegisters, stateRegistersSize);
    }

    public static synchronized boolean singleStep(int threadId) {
        return channelProtocol.singleStep(threadId);
    }

    /**
     * This is not synchronized because it is used to interrupt a resume that already holds the lock.
     *
     * @return
     */
    public static boolean suspendAll() {
        return channelProtocol.suspendAll();
    }

    public static synchronized boolean suspend(int threadId) {
        return channelProtocol.suspend(threadId);
    }

    public static synchronized boolean activateWatchpoint(int domainId, TeleWatchpoint teleWatchpoint) {
        final WatchpointSettings settings = teleWatchpoint.getSettings();
        return channelProtocol.activateWatchpoint(teleWatchpoint.memoryRegion().start().toLong(), teleWatchpoint.memoryRegion().size().toLong(), true, settings.trapOnRead, settings.trapOnWrite, settings.trapOnExec);
    }

    public static synchronized boolean deactivateWatchpoint(int domainId, TeleFixedMemoryRegion memoryRegion) {
        return channelProtocol.deactivateWatchpoint(memoryRegion.start().toLong(), memoryRegion.size().toLong());
    }

    public static synchronized long readWatchpointAddress(int domainId) {
        return channelProtocol.readWatchpointAddress();
    }

    public static synchronized int readWatchpointAccessCode(int domainId) {
        return channelProtocol.readWatchpointAccessCode();
    }

}
