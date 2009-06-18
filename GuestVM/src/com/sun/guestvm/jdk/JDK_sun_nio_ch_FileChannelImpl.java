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
import com.sun.max.annotate.*;
import com.sun.max.program.*;
import com.sun.max.vm.runtime.*;
import com.sun.guestvm.fs.*;

/**
 * Substitutions for  @see sun.nio.ch.FileChannelImpl.
 * @author Mick Jordan
 *
 */

@SuppressWarnings("unused")

@METHOD_SUBSTITUTIONS(sun.nio.ch.FileChannelImpl.class)
public class JDK_sun_nio_ch_FileChannelImpl {

    @SUBSTITUTE
    private int lock0(FileDescriptor fdObj, boolean blocking, long pos, long size, boolean shared) throws IOException {
        final int fd = JDK_java_io_fdActor.fdFieldActor().readInt(fdObj);
        return VirtualFileSystemId.getVfs(fd).lock0(fd, blocking, pos, size, shared);
    }

    @SUBSTITUTE
    private void release0(FileDescriptor fdObj, long pos, long size) throws IOException {
        final int fd = JDK_java_io_fdActor.fdFieldActor().readInt(fdObj);
        final  VirtualFileSystem vfs = VirtualFileSystemId.getVfs(fd);
        vfs.release0(VirtualFileSystemId.getFd(fd), pos, size);
    }

    @SUBSTITUTE
    private long map0(int prot, long position, long length) throws IOException {
        FatalError.crash("sun.nio.FileChannelImpl.map0");
        return -1;
    }

    @SUBSTITUTE
    private static int unmap0(long address, long length) {
        FatalError.crash("sun.nio.FileChannelImpl.unmap0");
        return -1;
    }

    @SUBSTITUTE
    private int force0(FileDescriptor fdObj, boolean metaData) throws IOException {
        final int fd = JDK_java_io_fdActor.fdFieldActor().readInt(fdObj);
        final  VirtualFileSystem vfs = VirtualFileSystemId.getVfs(fd);
        return vfs.force0(VirtualFileSystemId.getFd(fd), metaData);

    }

    @SUBSTITUTE
    private int truncate0(FileDescriptor fd, long size) {
        FatalError.crash("sun.nio.FileChannelImpl.truncate0");
        return -1;
    }

    @SUBSTITUTE
    private long transferTo0(int src, long position, long count, int dst) {
        FatalError.crash("sun.nio.FileChannelImpl.transferTo0");
        return -1;
    }

    @SUBSTITUTE
    private long position0(FileDescriptor fd, long offset) {
        FatalError.crash("sun.nio.FileChannelImpl.position0");
        return -1;
    }

    @SUBSTITUTE
    private long size0(FileDescriptor fd) {
        FatalError.crash("sun.nio.FileChannelImpl.size0");
        return -1;
    }

    @SUBSTITUTE
    private static long initIDs() {
        return 4096; // pagesize - should be acquired from GUK
    }

}
