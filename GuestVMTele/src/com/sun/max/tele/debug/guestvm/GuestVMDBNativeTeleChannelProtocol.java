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

import java.nio.ByteBuffer;

import com.sun.max.program.*;
import com.sun.max.tele.channel.TeleChannelProtocol;
import com.sun.max.tele.debug.ProcessState;

/**
 * An implementation of {@link TeleChannelProtocol} that links directly to native code
 * that communicates directly through JNI to  the Xen ring mechanism to the target Guest VM domain.
 * This requires that the Inspector or Inspector Agent run with root privileges in dom0.
 *
 * @author Mick Jordan
 *
 */

public class GuestVMDBNativeTeleChannelProtocol extends GuestVMNativeTeleChannelProtocolAdaptor {

	public GuestVMDBNativeTeleChannelProtocol() {
		
	}
	
    @Override
    public boolean attach(int id) {
        Trace.line(1, "attaching to domain " + id);
        natives.teleInitialize(threadLocalsAreaSize);
        return nativeAttach(id);
    }

    @Override
    public boolean detach() {
        Trace.line(1, "detaching from domain");
        return nativeDetach();
    }

    @Override
    public boolean activateWatchpoint(long start, long size, boolean after, boolean read, boolean write, boolean exec) {
        return nativeActivateWatchpoint(start, size, after, read, write, exec);
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
        return nativeGetBootHeapStart();
    }

    @Override
    public int maxByteBufferSize() {
        return nativeMaxByteBufferSize();
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

    /*
     * nativeResume returns true if the target domain has terminated, false if it stopped.
     * Since the resume always succeeds we always return true.
     * There is no per-thread resumption.
     */
    
    @Override
    public boolean resume(long threadId) {
        terminated = nativeResume();
        return true;
    }

    @Override
    public boolean resumeAll() {
        terminated = nativeResume();
        return true;
    }

    @Override
    public boolean setInstructionPointer(long threadId, long ip) {
        return nativeSetInstructionPointer((int) threadId, ip) ==.0;
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
    public int writeBytes(long dst, byte[] src, int srcOffset, int length) {
        return nativeWriteBytes(dst, src, false, 0, length);
    }

    @Override
    public int writeBytes(long dst, ByteBuffer src, int srcOffset, int length) {
        return nativeWriteBytes(dst, src, src.isDirect(), srcOffset, length);
    }

    private static native boolean nativeAttach(int domId);
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
