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
import static com.sun.guestvm.fs.VirtualFileSystem.*;

import com.sun.max.annotate.*;
import com.sun.max.vm.actor.member.*;
import com.sun.guestvm.fs.ErrorDecoder;

/**
 * This is a GuestVM specific substitution for the native methods in FileInputStream
 * that attempts to do more at the Java level than is done in the standard Hotspot JDK.
 *
 * @author Mick Jordan
 */

@SuppressWarnings("unused")

@METHOD_SUBSTITUTIONS(FileInputStream.class)
final class JDK_java_io_FileInputStream {

    private JDK_java_io_FileInputStream() {
    }

    @SUBSTITUTE
    private void open(String name) throws FileNotFoundException {
        JDK_java_io_util.open(fileDescriptorFieldActor().readObject(this), name, O_RDONLY);
    }

    @SUBSTITUTE
    int read() throws IOException {
        return JDK_java_io_util.read(fileDescriptorFieldActor().readObject(this));
    }

    @SUBSTITUTE
    int readBytes(byte[] bytes, int offset, int length) throws IOException {
        return JDK_java_io_util.readBytes(bytes, offset, length, fileDescriptorFieldActor().readObject(this));
    }

    @SUBSTITUTE
    private long skip(long n) throws IOException {
        if (n < 0) {
            throw new IOException("skip with negative argument: " + n);
        }
        final int fd = JDK_java_io_fdActor.fdFieldActor().readInt(fileDescriptorFieldActor().readObject(this));
        final long result = VirtualFileSystemId.getVfs(fd).skip(VirtualFileSystemId.getFd(fd), n, VirtualFileSystemOffset.get(fd));
        if (result < 0) {
            throw new IOException("error in skip: " + ErrorDecoder.getMessage((int) -result));
        } else {
            VirtualFileSystemOffset.add(fd, result);
            return result;
        }
    }

    @SUBSTITUTE
    private int available() throws IOException {
        final int fd = JDK_java_io_fdActor.fdFieldActor().readInt(fileDescriptorFieldActor().readObject(this));
        final int result = VirtualFileSystemId.getVfs(fd).available(VirtualFileSystemId.getFd(fd), VirtualFileSystemOffset.get(fd));
        if (result < 0) {
            throw new IOException("error in available: " + ErrorDecoder.getMessage(-result));
        } else {
            return result;
        }
    }

    @SUBSTITUTE
    void close0() throws IOException {
        JDK_java_io_util.close0(fileDescriptorFieldActor().readObject(this));
    }

    @SUBSTITUTE
    private static void initIDs() {

    }

    @CONSTANT_WHEN_NOT_ZERO
    private static ReferenceFieldActor _fileDescriptorFieldActor;

    @INLINE
    private ReferenceFieldActor fileDescriptorFieldActor() {
        if (_fileDescriptorFieldActor == null) {
            _fileDescriptorFieldActor = JDK_java_io_fdActor.fileDescriptorFieldActor(getClass());
        }
        return _fileDescriptorFieldActor;
    }

}
