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
package com.sun.max.ve.jdk;

import java.io.*;

import com.sun.max.ve.fs.*;

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
public class JavaIOUtil {

    /*
     * A helper class for storing the important information extracted by getFdInfo.
     * Instances of this class do not escape the methods that allocate them so
     * should be implemented on-stack without allocation.
     */
    public static class FdInfo {
        public int _fd;
        public VirtualFileSystem _vfs;
        public long _fileOffset;
        
        private FdInfo(int fd) throws IOException {
            this._fd = fd;
            this._vfs = VirtualFileSystemId.getVfs(fd);
            this._fileOffset = VirtualFileSystemOffset.get(fd);
        }

        public static FdInfo getFdInfo(FileDescriptor fdObj) throws IOException {
            return new FdInfo(JDK_java_io_FileDescriptor.getFd(fdObj));
        }
        
        public static FdInfo getFdInfo(int fd)  throws IOException {
            return new FdInfo(fd);
        }
        
    }

    static int read(FileDescriptor fdObj)  throws IOException {
        final FdInfo fdInfo = FdInfo.getFdInfo(fdObj);
        final int result = fdInfo._vfs.read(VirtualFileSystemId.getFd(fdInfo._fd), fdInfo._fileOffset);
        if (result >= 0) {
            VirtualFileSystemOffset.inc(fdInfo._fd);
            return result;
        }
        if (result != -1) {
            throw new IOException("Read error: " + ErrorDecoder.getMessage(-result));
        }
        return result;
    }

    static int readBytes(byte[] bytes, int offset, int length, FileDescriptor fdObj) throws IOException {
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
        if (result > 0) {
            VirtualFileSystemOffset.add(fdInfo._fd, result);
            return result;
        } else if (result != -1) {
            throw new IOException("Read error: " + ErrorDecoder.getMessage(-result));
        }
        return result;
    }

    static void write(int b, FileDescriptor fdObj) throws IOException {
        final FdInfo fdInfo = FdInfo.getFdInfo(fdObj);
        final int result = fdInfo._vfs.write(VirtualFileSystemId.getFd(fdInfo._fd), b, fdInfo._fileOffset);
        if (result > 0) {
            VirtualFileSystemOffset.inc(fdInfo._fd);
        }
    }

    static void writeBytes(byte[] bytes, int offset, int length, FileDescriptor fdObj) throws IOException {
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

    static int open(FileDescriptor fdObj, String name, int flags) throws FileNotFoundException {
        // You might think that at this point, name would be absolute (or canonical) but not so.
        // We, obviously, assume Unix conventions here.
        if (name.length() > 0 && name.charAt(0) != '/') {
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
            JDK_java_io_FileDescriptor.setFd(fdObj, fd);
            return fd;
        } else {
            throw new FileNotFoundException(ErrorDecoder.getFileMessage(-fd, name));
        }
    }

    public static void close0(FileDescriptor fdObj) throws IOException {
        final int fd = JDK_java_io_FileDescriptor.getFd(fdObj);
        if (fd > 0) {
            JDK_java_io_FileDescriptor.setFd(fdObj, -1);
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
