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
package com.sun.max.tele.debug.maxve;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.sun.max.program.ProgramError;
import com.sun.max.program.Trace;
import com.sun.max.tele.channel.TeleChannelProtocol;
import com.sun.max.tele.debug.ProcessState;

/**
 *
 * An implementation of {@link TeleChannelProtocol} that links directly to native code
 * that communicates directly through JNI to  the "xg" debug agent that accesses the target Guest VM domain.
 * This requires that the Inspector or Inspector Agent run with root privileges in dom0.
 *
 * N.B. The xg interface is very simple and, unlike the db-front/db-back custom interface that is accessed by
 * {@link MaxVEDBNativeTeleChannelProtocol}, has no notion of threads or other Maxine VM abstractions. These have to be
 * discovered by analyzing the memory using knowledge, primarily symbolic references from the VM image file,
 * about how the lowest level VM layer is implemented.
 *
 * @author Mick Jordan
 *
 */

public class MaxVEXGNativeTeleChannelProtocol  extends MaxVENativeTeleChannelProtocolAdaptor {

    /**
     * If we are running in agent, this field is {@code null}. Either way the relevant values are passed in the
     * {@link #setNativeAddresses(long, long, long)} method.
     */
    private ImageFileHandler imageFileHandler;
    
    private long threadListAddress;
    private long bootHeapStartAddress;
    private long resumeAddress;
    private boolean started;
	
	public MaxVEXGNativeTeleChannelProtocol() {
	}
	

    public MaxVEXGNativeTeleChannelProtocol(ImageFileHandler imageFileHandler) {
        this.imageFileHandler = imageFileHandler;
    }

    @Override
    public boolean activateWatchpoint(long start, long size, boolean after, boolean read, boolean write, boolean exec) {
        return nativeActivateWatchpoint(start, size, after, read, write, exec);
    }

    private int maxVCPU;

    @Override
    public boolean initialize(int threadLocalsAreaSize, boolean bigEndian) {
    	this.threadLocalsAreaSize = threadLocalsAreaSize;
        return true;
    }
    
    @Override
    public void setNativeAddresses(long threadListAddress, long bootHeapStartAddress, long resumeAddress) {
    	this.threadListAddress = threadListAddress;
    	this.bootHeapStartAddress = bootHeapStartAddress;
    	this.resumeAddress = resumeAddress;
    }

    @Override
    public boolean attach(int id) {
        Trace.line(1, "attaching to domain " + id);
        natives.teleInitialize(threadLocalsAreaSize);
        nativeInit();
        final int attachResult = nativeAttach(id, threadListAddress);
        if (attachResult < 0) {
            return false;
        } else {
            maxVCPU = attachResult;
            return true;
        }
    }

    @Override
    public boolean detach() {
        Trace.line(1, "detaching from domain");
        return nativeDetach();
    }

    @Override
    public boolean deactivateWatchpoint(long start, long size) {
        return nativeDeactivateWatchpoint(start, size);
    }

    @Override
    public boolean gatherThreads(Object teleDomain, Object threadSequence, long threadLocalsList, long primordialThreadLocals) {
        return nativeGatherThreads(teleDomain, threadSequence, threadLocalsList, primordialThreadLocals);
    }

    @Override
    public long getBootHeapStart() {
        final ByteBuffer bb = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
        final int n = readBytes(bootHeapStartAddress, bb.array(), 0, 8);
        if (n != 8) {
            ProgramError.unexpected("getBootHeapStart read failed");
        }
        return bb.getLong();
    }

    @Override
    public int maxByteBufferSize() {
        return 4096;
    }

    @Override
    public int readBytes(long src, byte[] dst, int dstOffset, int length) {
        return nativeReadBytes(src, dst, false, 0, length);
    }

    @Override
    public int readBytes(long src, ByteBuffer dst, int dstOffset, int length) {
        return nativeReadBytes(src, dst, dst.isDirect(), dstOffset, length);
    }

    @Override
    public boolean readRegisters(long threadId, byte[] integerRegisters, int integerRegistersSize, byte[] floatingPointRegisters, int floatingPointRegistersSize, byte[] stateRegisters,
                    int stateRegistersSize) {
        return nativeReadRegisters((int) threadId, integerRegisters, integerRegistersSize, floatingPointRegisters, floatingPointRegistersSize, stateRegisters, stateRegistersSize);
    }

    @Override
    public int readWatchpointAccessCode() {
        return nativeReadWatchpointAccessCode();
    }

    @Override
    public long readWatchpointAddress() {
        return nativeReadWatchpointAddress();
    }

    private boolean terminated;

    @Override
    public boolean resume(long threadId) {
    	resumeAll();
        return true;
    }

    @Override
    public boolean resumeAll() {
        if (!started) {
            // release domain
            byte[] one = new byte[] {1};
            writeBytes(resumeAddress, one, 0, 1);
        }
        terminated = nativeResume();
        started = true;
        return true;
    }

    /*
     * Note that resume does not return until the domain is stopped or terminated.
     */
    @Override
    public ProcessState waitUntilStopped() {
    	return terminated ? ProcessState.TERMINATED : ProcessState.STOPPED;
    }

    @Override
    public int waitUntilStoppedAsInt() {
    	return terminated ? ProcessState.TERMINATED.ordinal() : ProcessState.STOPPED.ordinal();
    }
    
    @Override
    public boolean setInstructionPointer(long threadId, long ip) {
        return nativeSetInstructionPointer((int) threadId, ip) == 0;
    }

    @Override
    public int setTransportDebugLevel(int level) {
        return nativeSetTransportDebugLevel(level);
    }

    @Override
    public boolean singleStep(long threadId) {
        return nativeSingleStep((int) threadId);
    }

    @Override
    public boolean suspend(long threadId) {
        return nativeSuspend((int) threadId);
    }

    @Override
    public boolean suspendAll() {
        return nativeSuspendAll();
    }

    @Override
    public int writeBytes(long dst, byte[] src, int srcOffset, int length) {
        return nativeWriteBytes(dst, src, false, 0, length);
    }

    @Override
    public int writeBytes(long dst, ByteBuffer src, int srcOffset, int length) {
        return nativeWriteBytes(dst, src, src.isDirect(), srcOffset, length);
    }

    private static native void nativeInit();
    private static native int nativeAttach(int domId, long threadListAddress);
    private static native boolean nativeDetach();
    private static native long nativeGetBootHeapStart();
    private static native int nativeSetTransportDebugLevel(int level);
    private static native int nativeReadBytes(long src, Object dst, boolean isDirectByteBuffer, int dstOffset, int length);
    private static native int nativeWriteBytes(long dst, Object src, boolean isDirectByteBuffer, int srcOffset, int length);
    private static native int nativeMaxByteBufferSize();
    private static native boolean nativeGatherThreads(Object teleDomain, Object threadSequence, long threadLocalsList, long primordialThreadLocals);
    private static native boolean nativeResume();
    private static native int nativeSetInstructionPointer(int threadId, long ip);
    private static native boolean nativeSingleStep(int threadId);
    private static native boolean nativeSuspendAll();
    private static native boolean nativeSuspend(int threadId);
    private static native boolean nativeActivateWatchpoint(long start, long size, boolean after, boolean read, boolean write, boolean exec);
    private static native boolean nativeDeactivateWatchpoint(long start, long size);
    private static native long nativeReadWatchpointAddress();
    private static native int nativeReadWatchpointAccessCode();

    private static native boolean nativeReadRegisters(int threadId,
                    byte[] integerRegisters, int integerRegistersSize,
                    byte[] floatingPointRegisters, int floatingPointRegistersSize,
                    byte[] stateRegisters, int stateRegistersSize);

}
