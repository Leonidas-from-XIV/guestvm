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
package com.sun.max.tele.debug.guestvm.dbchannel.xg;

import java.nio.*;

import com.sun.max.elf.*;
import com.sun.max.program.*;
import com.sun.max.tele.debug.guestvm.dbchannel.*;

/**
 *
 * An implementation of {@link Protocol} that links directly to native code
 * that communicates directly through JNI to  the "xg" debug agent that accesses the target Guest VM domain.
 * This requires that the Inspector or Inspector Agent run with root privileges in dom0.
 *
 * N.B. The xg interface is very simple and, unlike the db-front/db-back custom interface that is accessed by
 * {@link DBProtocol}, has no notion of threads or other Maxine VM abstractions. These have to be
 * discovered by analyzing the memory using knowledge, primarily symbolic references from the VM image file,
 * about how the lowest level VM layer is implemented.
 *
 * @author Mick Jordan
 *
 */

public class XGProtocol implements Protocol {

    /**
     * If we are running in agent, this field is {@code null}.
     */
    private ImageFileHandler imageFileHandler;

    public XGProtocol(ImageFileHandler imageFileHandler) {
        this.imageFileHandler = imageFileHandler;
    }

    @Override
    public boolean activateWatchpoint(long start, long size, boolean after, boolean read, boolean write, boolean exec) {
        return nativeActivateWatchpoint(start, size, after, read, write, exec);
    }

    private int maxVCPU;

    @Override
    public boolean attach(int domId, int threadLocalsAreaSize, long extra1) {
        Trace.line(1, "attaching to domain " + domId);
        final int attachResult = nativeAttach(domId, imageFileHandler == null ? extra1 : imageFileHandler.getThreadListSymbolAddress());
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
        assert imageFileHandler != null;
        final long addr = imageFileHandler.getBootHeapStartSymbolAddress();
        return getBootHeapStart(this, addr);
    }

    public long getBootHeapStart(SimpleProtocol p, long addr) {
        final ByteBuffer bb = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
        final int n = p.readBytes(addr, bb.array(), 0, 8);
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
    public int readBytes(long src, Object dst, boolean isDirectByteBuffer, int dstOffset, int length) {
        return nativeReadBytes(src, dst, isDirectByteBuffer, dstOffset, length);
    }

    @Override
    public boolean readRegisters(int threadId, byte[] integerRegisters, int integerRegistersSize, byte[] floatingPointRegisters, int floatingPointRegistersSize, byte[] stateRegisters,
                    int stateRegistersSize) {
        return nativeReadRegisters(threadId, integerRegisters, integerRegistersSize, floatingPointRegisters, floatingPointRegistersSize, stateRegisters, stateRegistersSize);
    }

    @Override
    public int readWatchpointAccessCode() {
        return nativeReadWatchpointAccessCode();
    }

    @Override
    public long readWatchpointAddress() {
        return nativeReadWatchpointAddress();
    }

    @Override
    public int resume() {
        return nativeResume();
    }

    @Override
    public int setInstructionPointer(int threadId, long ip) {
        return nativeSetInstructionPointer(threadId, ip);
    }

    @Override
    public int setTransportDebugLevel(int level) {
        return nativeSetTransportDebugLevel(level);
    }

    @Override
    public boolean singleStep(int threadId) {
        return nativeSingleStep(threadId);
    }

    @Override
    public boolean suspend(int threadId) {
        return nativeSuspend(threadId);
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
    public int writeBytes(long dst, Object src, boolean isDirectByteBuffer, int srcOffset, int length) {
        return nativeWriteBytes(dst, src, isDirectByteBuffer, srcOffset, length);
    }

    private static native int nativeAttach(int domId, long threadListAddress);
    private static native boolean nativeDetach();
    private static native long nativeGetBootHeapStart();
    private static native int nativeSetTransportDebugLevel(int level);
    private static native int nativeReadBytes(long src, Object dst, boolean isDirectByteBuffer, int dstOffset, int length);
    private static native int nativeWriteBytes(long dst, Object src, boolean isDirectByteBuffer, int srcOffset, int length);
    private static native int nativeMaxByteBufferSize();
    private static native boolean nativeGatherThreads(Object teleDomain, Object threadSequence, long threadLocalsList, long primordialThreadLocals);
    private static native int nativeResume();
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

    @Override
    public int gatherThreads(long threadLocalsList, long primordialThreadLocals) {
        ProgramError.unexpected("SimpleProtocol.gatherThreads(int, int) should not be called in this configuration");
        return 0;
    }

    @Override
    public int readThreads(int size, byte[] gatherThreadsData) {
        ProgramError.unexpected("SimpleProtocol.readThreads should not be called in this configuration");
        return 0;
    }


}