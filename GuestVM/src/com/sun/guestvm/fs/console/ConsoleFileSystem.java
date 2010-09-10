/*
 * Copyright (c) 2009 Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, California 95054, U.S.A. All rights reserved.
 *
 * U.S. Government Rights - Commercial software. Government users are
 * subject to the Sun Microsystems, Inc. standard license agreement and
 * applicable provisions of the FAR and its supplements.
 *
 * Use is subject to license terms.
 *
 * This distribution may include materials developed by third parties.
 *
 * Parts of the product may be derived from Berkeley BSD systems,
 * licensed from the University of California. UNIX is a registered
 * trademark in the U.S.  and in other countries, exclusively licensed
 * through X/Open Company, Ltd.
 *
 * Sun, Sun Microsystems, the Sun logo and Java are trademarks or
 * registered trademarks of Sun Microsystems, Inc. in the U.S. and other
 * countries.
 *
 * This product is covered and controlled by U.S. Export Control laws and
 * may be subject to the export or import laws in other
 * countries. Nuclear, missile, chemical biological weapons or nuclear
 * maritime end uses or end users, whether direct or indirect, are
 * strictly prohibited. Export or reexport to countries subject to
 * U.S. embargo or to entities identified on U.S. export exclusion lists,
 * including, but not limited to, the denied persons and specially
 * designated nationals lists is strictly prohibited.
 *
 */
package com.sun.guestvm.fs.console;

import com.sun.max.unsafe.*;
import com.sun.max.vm.*;
import com.sun.max.vm.reference.*;
import com.sun.max.memory.Memory;
import com.sun.guestvm.fs.*;

/**
 * This is not really a file system, it just supports the standard file descriptors
 * that want to read/write from the console.
 *
 * @author Mick Jordan
 *
 */
public class ConsoleFileSystem extends UnimplementedFileSystemImpl implements VirtualFileSystem {

    @Override
    public void close() {

    }

    @Override
    public int write(int fd, int b, long fileOffset) {
        return nativeWrite(b);
    }

    /*
     * Hotspot native code avoids allocation for smallish buffers by
     * copying to an on-stack array. Since we can't do that (yet)
     * in Java, we rely on boot heap objects not being GC'ed
     * and synchronize console output
     */

    private static final byte[] writeBuffer = new byte[1024];
    private static final byte[] readBuffer = new byte[1024];
    /**
     * The offset of the byte array data from the byte array object's origin.
     */
    private static final Offset _dataOffset = VMConfiguration.vmConfig().layoutScheme().byteArrayLayout.getElementOffsetFromOrigin(0);

    /**
     *
     */
    @Override
    public synchronized int writeBytes(int fd, byte[] bytes, int offset, int length, long fileOffset) {
        final Pointer nativeBytes = Reference.fromJava(writeBuffer).toOrigin().plus(_dataOffset);
        int result = 0;
        int left = length;
        int newOffset = offset;
        while (left > 0) {
            final int toWrite = left > writeBuffer.length ? writeBuffer.length : left;
            Memory.writeBytes(bytes, newOffset, toWrite, nativeBytes);
            result += nativeWriteBytes(nativeBytes, toWrite);
            left -= toWrite;
            newOffset += toWrite;
        }
        return result;
    }

    @Override
    public int read(int fd, long fileOffset) {
        return nativeRead();
    }

    @Override
    public synchronized int readBytes(int fd, byte[] bytes, int offset, int length, long fileOffset) {
        final Pointer nativeBytes = Reference.fromJava(readBuffer).toOrigin().plus(_dataOffset);
        assert length >= readBuffer.length;
        final int n = nativeReadBytes(nativeBytes, readBuffer.length);
        Memory.readBytes(nativeBytes, n, bytes, offset);
        return n;
    }

    @Override
    public int available(int fd, long fileOffset) {
        return 0;
    }

    @Override
    public long skip(int fd, long n, long fileOffset) {
        return 0;
    }

    @Override
    public long uniqueId(int fd) {
        return fd;
    }

    private static native int nativeWriteBytes(Pointer p, int length);
    private static native int nativeWrite(int b);
    private static native int nativeReadBytes(Pointer p, int length);
    private static native int nativeRead();

}
