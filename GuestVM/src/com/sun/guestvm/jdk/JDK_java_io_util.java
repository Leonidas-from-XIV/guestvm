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
package com.sun.guestvm.jdk;

import java.io.*;
import com.sun.guestvm.fs.*;
import com.sun.max.vm.object.TupleAccess;

/**
 * Support methods for FileInputStream, RandomAccessFile.
 *
 * One of the interesting questions, given that readBytes/writeBytes are private methods in FileIn/OutputStream,
 * is exactly how conservative we need to be in checking things. The Hotspot native code
 * checks everything, e.g. whether bytes == null, which seems extremely improbable.
 * For now, we follow the same script almost exactly.
 *
 * N.B. There is no explicit check in FileInputStream etc., for a closed file descriptor (fd == -1)
 * It is checked here explicitly in the call to VirtualFileSystem.getVfs, which must be the first call made.
 *
* @author Mick Jordan
 *
 */
public class JDK_java_io_util {

    /*
     * A helper class for storing the important information extracted by getFdInfo.
     * Instances of this class do not escape the methods that allocate them so
     * should be implemented on-stack  without allocation.
     */
    static class FdInfo {
        int _fd;
        VirtualFileSystem _vfs;
        long _fileOffset;

        static FdInfo getFdInfo(Object fdObj) throws IOException {
            final FdInfo result = new FdInfo();
            result._fd = TupleAccess.readInt(fdObj, JDK_java_io_fdActor.fdFieldActor().offset());
            result._vfs = VirtualFileSystemId.getVfs(result._fd);
            result._fileOffset = VirtualFileSystemOffset.get(result._fd);
            return result;
        }
    }

    static int read(Object fdObj)  throws IOException {
        final FdInfo fdInfo = FdInfo.getFdInfo(fdObj);
        final int b = fdInfo._vfs.read(VirtualFileSystemId.getFd(fdInfo._fd), fdInfo._fileOffset);
        VirtualFileSystemOffset.inc(fdInfo._fd);
        return b == 0 ? -1 : b;
    }

    static int readBytes(byte[] bytes, int offset, int length, Object fdObj) throws IOException {
        if (bytes == null) {
            throw new NullPointerException();
        }
        if ((offset < 0) || (offset > bytes.length) || (length < 0) || ((offset + length) > bytes.length) || ((offset + length) < 0)) {
            throw new IndexOutOfBoundsException();
        }
        if (length == 0) {
            return 0;
        }
        final FdInfo fdInfo = FdInfo.getFdInfo(fdObj);
        final int result = fdInfo._vfs.readBytes(VirtualFileSystemId.getFd(fdInfo._fd), bytes, offset, length, fdInfo._fileOffset);
        if (result == 0) {
            return -1;
        } else if (result < 0) {
            throw new IOException("Read error: " + ErrorDecoder.getMessage(-result));
        } else {
            VirtualFileSystemOffset.add(fdInfo._fd, result);
        }
        return result;
    }

    static void write(int b, Object fdObj) throws IOException {
        final FdInfo fdInfo = FdInfo.getFdInfo(fdObj);
        final int result = fdInfo._vfs.write(VirtualFileSystemId.getFd(fdInfo._fd), b, fdInfo._fileOffset);
        if (result > 0) {
            VirtualFileSystemOffset.inc(fdInfo._fd);
        }
    }

    static void writeBytes(byte[] bytes, int offset, int length, Object fdObj) throws IOException {
        if (bytes == null) {
            throw new NullPointerException();
        }
        if ((offset < 0) || (offset > bytes.length) || (length < 0) || ((offset + length) > bytes.length) || ((offset + length) < 0)) {
            throw new IndexOutOfBoundsException();
        }
        // This is interesting - it suggests a requirement to check the offsets even if we are not going to output anything!
        if (length == 0) {
            return;
        }
        final FdInfo fdInfo = FdInfo.getFdInfo(fdObj);
        final int result = fdInfo._vfs.writeBytes(VirtualFileSystemId.getFd(fdInfo._fd), bytes, offset, length, fdInfo._fileOffset);
        if (result < 0) {
            throw new IOException("Write error: " + ErrorDecoder.getMessage(-result));
        } else {
            VirtualFileSystemOffset.add(fdInfo._fd, result);
        }
    }

    static int open(Object fdObj, String name, int flags) throws FileNotFoundException {
        // You might think that at this point, name would be absolute (or canonical) but not so.
        // We, obviously, assume Unix conventions here.
        if (name.charAt(0) != '/') {
            // Checkstyle: stop parameter assignment check
            name = new File(name).getAbsolutePath();
            // Checkstyle: resume parameter assignment check
        }
        final VirtualFileSystem fs = FSTable.exports(name);
        if (fs == null) {
            throw new FileNotFoundException(name + " (Unmounted or uninitialized file system)");
        }
        int fd = fs.open(name, flags);
        if (fd >= 0) {
            fd = VirtualFileSystemId.getUniqueFd(fs, fd);
            TupleAccess.writeInt(fdObj, JDK_java_io_fdActor.fdFieldActor().offset(), fd);
            return fd;
        } else {
            throw new FileNotFoundException(ErrorDecoder.getFileMessage(-fd, name));
        }
    }

    static void close0(Object fdObj) throws IOException {
        final int fd = TupleAccess.readInt(fdObj, JDK_java_io_fdActor.fdFieldActor().offset());
        if (fd > 0) {
            TupleAccess.writeInt(fdObj, JDK_java_io_fdActor.fdFieldActor().offset(), -1);
            close0FD(fd);
        }
    }

    static void close0FD(int fd) throws IOException {
        final int result = VirtualFileSystemId.getVfs(fd).close0(VirtualFileSystemId.getFd(fd));
        VirtualFileSystemOffset.remove(fd);
        if (result < 0) {
            throw new IOException("Close error: " + ErrorDecoder.getMessage(-result));
        }
    }
}
