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
import sun.nio.ch.FileKey;
import com.sun.max.annotate.*;
import com.sun.max.vm.actor.holder.*;
import com.sun.max.vm.actor.member.*;
import com.sun.max.vm.object.*;
import com.sun.max.vm.classfile.constant.*;
import com.sun.guestvm.fs.*;

/**
 * GuestVM implementation of sun.nio.ch.FileKey
 *
 * A FileKey is just a unique identifier for a file, whereas a pathname could be aliased.
 * Unix Hotspot uses the st_dev and st_ino fields from the stat system call.
 * GuestVM has no direct equivalent but the VirtualFileSystemId encoded in the
 * fd field of a @see FileDescriptor is equivalent to st_dev and the st_ino case is handled by a
 * method in @see VirtualFileSystem.
 *
 * @author Mick Jordan
 */

/**
 * GuestVM implementation of FileKey
 *
 * A FileKey is a unique id for an open file. On Hotspot/Unix it is represented by
 * the st_dev and st_ino fields returned by the stat system call.
 *
 * On GuestVM we use the VirtualFileSystemId for st_dev and VirtualFileSystem.uniqueId for st_ino.
 *
 * @author Mick Jordan
 *
 */

@SuppressWarnings("unused")

@METHOD_SUBSTITUTIONS(FileKey.class)
public class JDK_sun_nio_ch_FileKey {
    @SUBSTITUTE
    private static void initIDs() {

    }

    @SUBSTITUTE
    private void init(FileDescriptor fdObj) throws IOException {
        final int fd = TupleAccess.readInt(fdObj, JDK_java_io_fdActor.fdFieldActor().offset());
        TupleAccess.writeLong(this, devFieldActor().offset(), VirtualFileSystemId.getVfsId(fd));
        final VirtualFileSystem vfs = VirtualFileSystemId.getVfs(fd);
        TupleAccess.writeLong(this, inoFieldActor().offset(), vfs.uniqueId(VirtualFileSystemId.getFd(fd)));
    }

    @CONSTANT_WHEN_NOT_ZERO
    private static FieldActor _devFieldActor;

    @INLINE
    static FieldActor devFieldActor() {
        if (_devFieldActor == null) {
            _devFieldActor = (FieldActor) ClassActor.fromJava(FileKey.class).findFieldActor(SymbolTable.makeSymbol("st_dev"));
        }
        return _devFieldActor;
    }

    @CONSTANT_WHEN_NOT_ZERO
    private static FieldActor _inoFieldActor;

    @INLINE
    static FieldActor inoFieldActor() {
        if (_inoFieldActor == null) {
            _inoFieldActor = (FieldActor) ClassActor.fromJava(FileKey.class).findFieldActor(SymbolTable.makeSymbol("st_ino"));
        }
        return _inoFieldActor;
    }
}
