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
package com.sun.max.tele.debug.guestvm.dbchannel.dataio;

import java.io.*;
import com.sun.max.program.*;
import com.sun.max.tele.debug.guestvm.dbchannel.*;

/**
 * An implementation of {@link SimpleProtocol} that communicates using {@link DataInputStream} and {@link DataOutputStream) and is
 * essentially a custom remote method invocation system.
 *
 * Byte arrays are supported and, obviously, require special treatment. Arrays are tagged as {@link ArrayMode.#IN in}, {@link ArrayMode#OUT out}
 * or {@link ArrayMode#INOUT inout}. That is, when an array is passed as parameter, the ordinal value of the {@link ArrayMode} is written first,
 * followed by the length of the array, followed by the bytes of the array, except in the case of {@link ArrayMode#OUT}, where no bytes are sent.
 * The contents of the array are "returned" as an auxiliary result if the mode is {@link ArrayMode#OUT} or {@link ArrayMode#INOUT}; the bytes
 * precede the method result value.
 *
 * For this to work, the {@link ArrayMode} of a parameter must be registered with {@link RIProtocolAdaptor} by the target end of the communication..
 *
* @author Mick Jordan
 *
 */

public class DataIOProtocol implements SimpleProtocol {
    public enum ArrayMode {
        IN,
        OUT,
        INOUT
    };

    protected DataInputStream in;
    protected DataOutputStream out;

    private final Timings timings = new Timings("DataIOProtocol.readBytes");

    protected DataIOProtocol() {
    }

    protected void setStreams(InputStream in, OutputStream out) {
        this.in = new DataInputStream(in);
        this.out = new DataOutputStream(out);
    }

    @Override
    public boolean activateWatchpoint(long start, long size, boolean after, boolean read, boolean write, boolean exec) {
        unimplemented("activateWatchpoint");
        return false;
    }

    @Override
    public boolean attach(int domId, int threadLocalsAreaSize, long extra1) {
        try {
            out.writeUTF("attach");
            out.writeInt(domId);
            out.writeInt(threadLocalsAreaSize);
            out.writeLong(extra1);
            out.flush();
            boolean result = in.readBoolean();
            return result;
        } catch (Exception ex) {
            Trace.line(1, ex);
            return false;
        }
    }

    @Override
    public boolean detach() {
        try {
            out.writeUTF("detach");
            out.flush();
            return in.readBoolean();
        } catch (Exception ex) {
            Trace.line(1, ex);
            return false;
        }
    }

    @Override
    public long getBootHeapStart() {
        try {
            out.writeUTF("getBootHeapStart");
            out.flush();
            return in.readLong();
        } catch (IOException ex) {
            ProgramError.unexpected(ex);
            return 0;
        }
    }

    @Override
    public int maxByteBufferSize() {
        try {
            out.writeUTF("maxByteBufferSize");
            out.flush();
            return in.readInt();
        } catch (IOException ex) {
            ProgramError.unexpected(ex);
            return 0;
        }
    }

    @Override
    public int readBytes(long src, byte[] dst, int dstOffset, int length) {
        try {
            timings.start();
            out.writeUTF("readBytes");
            out.writeLong(src);
            outArray(ArrayMode.OUT, dst);
            out.writeInt(dstOffset);
            out.writeInt(length);
            out.flush();
            inArray(dst, dstOffset, length);
            final int result = in.readInt();
            timings.add();
            return result;
        } catch (IOException ex) {
            ProgramError.unexpected(ex);
            return 0;
        }
    }

    @Override
    public boolean readRegisters(int threadId, byte[] integerRegisters, int integerRegistersSize, byte[] floatingPointRegisters, int floatingPointRegistersSize, byte[] stateRegisters,
                    int stateRegistersSize) {
        try {
            out.writeUTF("readRegisters");
            out.writeInt(threadId);
            outArray(ArrayMode.OUT, integerRegisters);
            out.writeInt(integerRegistersSize);
            outArray(ArrayMode.OUT, floatingPointRegisters);
            out.writeInt(floatingPointRegistersSize);
            outArray(ArrayMode.OUT, stateRegisters);
            out.writeInt(stateRegistersSize);
            out.flush();
            inArray(integerRegisters, 0, integerRegistersSize);
            inArray(floatingPointRegisters, 0, floatingPointRegistersSize);
            inArray(stateRegisters, 0, stateRegistersSize);
            return in.readBoolean();
        } catch (IOException ex) {
            ProgramError.unexpected(ex);
            return false;
        }
    }

    @Override
    public int readWatchpointAccessCode() {
        try {
            out.writeUTF("readWatchpointAccessCode");
            out.flush();
            return in.readInt();
        } catch (IOException ex) {
            ProgramError.unexpected(ex);
            return 0;
        }
    }

    @Override
    public long readWatchpointAddress() {
        try {
            out.writeUTF("readWatchpointAddress");
            out.flush();
            return in.readLong();
        } catch (IOException ex) {
            ProgramError.unexpected(ex);
            return 0;
        }
    }

    @Override
    public boolean deactivateWatchpoint(long start, long size) {
        try {
            out.writeUTF("deactivateWatchpoint");
            out.writeLong(start);
            out.writeLong(size);
            out.flush();
            return in.readBoolean();
        } catch (IOException ex) {
            ProgramError.unexpected(ex);
            return false;
        }
    }

    @Override
    public int resume() {
        try {
            out.writeUTF("resume");
            out.flush();
            return in.readInt();
        } catch (IOException ex) {
            ProgramError.unexpected(ex);
            return 0;
        }
    }

    @Override
    public int setInstructionPointer(int threadId, long ip) {
        try {
            out.writeUTF("setInstructionPointer");
            out.writeInt(threadId);
            out.writeLong(ip);
            out.flush();
            return in.readInt();
        } catch (IOException ex) {
            ProgramError.unexpected(ex);
            return 0;
        }
    }

    @Override
    public int setTransportDebugLevel(int level) {
        try {
            out.writeUTF("setTransportDebugLevel");
            out.flush();
            return in.readInt();
        } catch (IOException ex) {
            ProgramError.unexpected(ex);
            return 0;
        }
    }

    @Override
    public boolean singleStep(int threadId) {
        try {
            out.writeUTF("singleStep");
            out.writeInt(threadId);
            out.flush();
            return in.readBoolean();
        } catch (IOException ex) {
            ProgramError.unexpected(ex);
            return false;
        }
    }

    @Override
    public boolean suspend(int threadId) {
        try {
            out.writeUTF("suspend");
            out.writeInt(threadId);
            out.flush();
            return in.readBoolean();
        } catch (IOException ex) {
            ProgramError.unexpected(ex);
            return false;
        }
    }

    @Override
    public boolean suspendAll() {
        try {
            out.writeUTF("suspendAll");
            out.flush();
            return in.readBoolean();
        } catch (IOException ex) {
            ProgramError.unexpected(ex);
            return false;
        }
    }

    @Override
    public int writeBytes(long dst, byte[] src, int srcOffset, int length) {
        try {
            out.writeUTF("writeBytes");
            out.writeLong(dst);
            outArray(ArrayMode.IN, src);
            out.writeInt(srcOffset);
            out.writeInt(length);
            out.flush();
            return in.readInt();
        } catch (IOException ex) {
            ProgramError.unexpected(ex);
            return 0;
        }
    }

    @Override
    public int gatherThreads(long threadLocalsList, long primordialThreadLocals) {
        try {
            out.writeUTF("gatherThreads");
            out.writeLong(threadLocalsList);
            out.writeLong(primordialThreadLocals);
            out.flush();
            return in.readInt();
        } catch (IOException ex) {
            ProgramError.unexpected(ex);
            return 0;
        }
    }

    @Override
    public int readThreads(int size, byte[] gatherThreadData) {
        try {
            out.writeUTF("readThreads");
            out.writeInt(size);
            outArray(ArrayMode.OUT, gatherThreadData);
            out.flush();
            inArray(gatherThreadData, 0, size);
            return in.readInt();
        } catch (IOException ex) {
            ProgramError.unexpected(ex);
            return 0;
        }
    }

    private void outArray(ArrayMode mode, byte[] b) throws IOException {
        out.writeInt(mode.ordinal());
        // write b.length first so callee can allocate
        out.writeInt(b.length);
        if (mode != ArrayMode.OUT) {
            out.write(b);
        }
    }

    private void inArray(byte[] dst, int dstOffset, int length) throws IOException {
        byte[] result = new byte[dst.length];
        in.readFully(result);
        System.arraycopy(result, dstOffset, dst, dstOffset, length);
    }


    private void unimplemented(String name) {
        ProgramError.unexpected(getClass().getName() + "." + name + " unimplemented");
    }

}