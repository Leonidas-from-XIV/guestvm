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

import static com.sun.guestvm.jdk.AliasCast.*;
import static com.sun.guestvm.fs.VirtualFileSystem.*;

import java.io.*;

import com.sun.max.annotate.*;

import com.sun.guestvm.fs.*;

/**
 * This is a GuestVM specific substitution for the native methods in FileInputStream.
 *
 * @author Mick Jordan
 */

@METHOD_SUBSTITUTIONS(FileInputStream.class)
final class JDK_java_io_FileInputStream {
    
    @ALIAS(declaringClass = FileInputStream.class)
    FileDescriptor fd;

    private JDK_java_io_FileInputStream() {
    }
    
    @INLINE
    private static FileDescriptor getFileDescriptor(Object obj) {
        JDK_java_io_FileInputStream thisFileInputStream = asJDK_java_io_FileInputStream(obj);
        return thisFileInputStream.fd;
    }

    @SuppressWarnings("unused")
    @SUBSTITUTE
    private void open(String name) throws FileNotFoundException {
        JavaIOUtil.open(getFileDescriptor(this), name, O_RDONLY);
    }

    @SUBSTITUTE
    int read() throws IOException {
        return JavaIOUtil.read(getFileDescriptor(this));
    }

    @SUBSTITUTE
    int readBytes(byte[] bytes, int offset, int length) throws IOException {
        return JavaIOUtil.readBytes(bytes, offset, length, getFileDescriptor(this));
    }

    @SuppressWarnings("unused")
    @SUBSTITUTE
    private long skip(long n) throws IOException {
        if (n < 0) {
            throw new IOException("skip with negative argument: " + n);
        }
        final int fd = JDK_java_io_FileDescriptor.getFd(getFileDescriptor(this));
        final long result = VirtualFileSystemId.getVfs(fd).skip(VirtualFileSystemId.getFd(fd), n, VirtualFileSystemOffset.get(fd));
        if (result < 0) {
            throw new IOException("error in skip: " + ErrorDecoder.getMessage((int) -result));
        } else {
            VirtualFileSystemOffset.add(fd, result);
            return result;
        }
    }

    @SuppressWarnings("unused")
    @SUBSTITUTE
    private int available() throws IOException {
        final int fd = JDK_java_io_FileDescriptor.getFd(getFileDescriptor(this));
        final int result = VirtualFileSystemId.getVfs(fd).available(VirtualFileSystemId.getFd(fd), VirtualFileSystemOffset.get(fd));
        if (result < 0) {
            throw new IOException("error in available: " + ErrorDecoder.getMessage(-result));
        } else {
            return result;
        }
    }

    @SUBSTITUTE
    void close0() throws IOException {
        JavaIOUtil.close0(getFileDescriptor(this));
    }

    @SuppressWarnings("unused")
    @SUBSTITUTE
    private static void initIDs() {

    }


}
