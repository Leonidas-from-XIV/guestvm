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

import com.sun.guestvm.fs.*;

import com.sun.max.annotate.*;

/**
 * This is a GuestVM specific substitution for the native methods in FileOutputStream.
 *
 * @author Mick Jordan
 */

@METHOD_SUBSTITUTIONS(FileOutputStream.class)
final class JDK_java_io_FileOutputStream {
    
    @ALIAS(declaringClass = FileOutputStream.class)
    FileDescriptor fd;

    private JDK_java_io_FileOutputStream() {
    }

    @INLINE
    private static FileDescriptor getFileDescriptor(Object obj) {
        JDK_java_io_FileOutputStream thisFileOutputStream = asJDK_java_io_FileOutputStream(obj);
        return thisFileOutputStream.fd;
    }

    @SuppressWarnings("unused")
    @SUBSTITUTE
    private void open(String name) throws FileNotFoundException {
        JavaIOUtil.open(getFileDescriptor(this), name, O_WRONLY | O_CREAT | O_TRUNC);
    }

    @SuppressWarnings("unused")
    @SUBSTITUTE
    private void openAppend(String name) throws FileNotFoundException {
        final int fd = JavaIOUtil.open(getFileDescriptor(this), name, O_WRONLY | O_CREAT | O_APPEND);
        final  VirtualFileSystem vfs = VirtualFileSystemId.getVfsUnchecked(fd);
        VirtualFileSystemOffset.set(fd, vfs.getLength(VirtualFileSystemId.getFd(fd)));
    }

    @SuppressWarnings("unused")
    @SUBSTITUTE
    private void close0() throws IOException {
        JavaIOUtil.close0(getFileDescriptor(this));
    }

    @SuppressWarnings("unused")
    @SUBSTITUTE
    private void write(int b) throws IOException {
        JavaIOUtil.write(b, getFileDescriptor(this));
    }

    @SuppressWarnings("unused")
    @SUBSTITUTE
    private void writeBytes(byte[] bytes, int offset, int length) throws IOException {
        JavaIOUtil.writeBytes(bytes, offset, length, getFileDescriptor(this));
    }

    @SuppressWarnings("unused")
    @SUBSTITUTE
    private static void initIDs() {
    }

}
