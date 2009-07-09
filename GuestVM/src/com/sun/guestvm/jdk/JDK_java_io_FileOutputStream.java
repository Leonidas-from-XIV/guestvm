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

import static com.sun.guestvm.fs.VirtualFileSystem.*;
import com.sun.guestvm.fs.*;

import com.sun.max.annotate.*;
import com.sun.max.vm.actor.member.*;
import com.sun.max.vm.object.*;

/**
 * This is a GuestVM specific substitution for the native methods in FileOutputStream
 * that attempts to do more at the Java level than is done in the standard Hotspot JDK.
 *
 * @author Mick Jordan
 */

@SuppressWarnings("unused")

@METHOD_SUBSTITUTIONS(FileOutputStream.class)
final class JDK_java_io_FileOutputStream {
    private JDK_java_io_FileOutputStream() {
    }

    @SUBSTITUTE
    private void open(String name) throws FileNotFoundException {
        JDK_java_io_util.open(TupleAccess.readObject(this, fileDescriptorFieldActor().offset()), name, O_WRONLY | O_CREAT | O_TRUNC);
    }

    @SUBSTITUTE
    private void openAppend(String name) throws FileNotFoundException {
        final int fd = JDK_java_io_util.open(TupleAccess.readObject(this, fileDescriptorFieldActor().offset()), name, O_WRONLY | O_CREAT | O_APPEND);
        final  VirtualFileSystem vfs = VirtualFileSystemId.getVfsUnchecked(fd);
        VirtualFileSystemOffset.set(fd, vfs.getLength(VirtualFileSystemId.getFd(fd)));
    }

    @SUBSTITUTE
    private void close0() throws IOException {
        JDK_java_io_util.close0(TupleAccess.readObject(this, fileDescriptorFieldActor().offset()));
    }

    @SUBSTITUTE
    private void write(int b) throws IOException {
        JDK_java_io_util.write(b, TupleAccess.readObject(this, fileDescriptorFieldActor().offset()));
    }

    @SUBSTITUTE
    private void writeBytes(byte[] bytes, int offset, int length) throws IOException {
        JDK_java_io_util.writeBytes(bytes, offset, length, TupleAccess.readObject(this, fileDescriptorFieldActor().offset()));
    }

    @SUBSTITUTE
    private static void initIDs() {
    }

    @CONSTANT_WHEN_NOT_ZERO
    private static FieldActor _fileDescriptorFieldActor;

    @INLINE
    private FieldActor fileDescriptorFieldActor() {
        if (_fileDescriptorFieldActor == null) {
            _fileDescriptorFieldActor = JDK_java_io_fdActor.fileDescriptorFieldActor(getClass());
        }
        return _fileDescriptorFieldActor;
    }

}
