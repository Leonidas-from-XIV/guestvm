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

import static com.sun.max.ve.jdk.AliasCast.*;

import java.io.*;
import sun.nio.ch.FileKey;
import com.sun.max.annotate.*;
import com.sun.max.ve.fs.*;

/**
 * MaxVE implementation of sun.nio.ch.FileKey
 *
 * A FileKey is just a unique identifier for a file, whereas a pathname could be aliased.
 * Unix Hotspot uses the st_dev and st_ino fields from the stat system call.
 * MaxVE has no direct equivalent but the VirtualFileSystemId encoded in the
 * fd field of a {@link FileDescriptor} is equivalent to st_dev and the st_ino case is handled by a
 * method in {@link VirtualFileSystem}.
 *
 * @author Mick Jordan
 */


@METHOD_SUBSTITUTIONS(FileKey.class)
public class JDK_sun_nio_ch_FileKey {
    
    @SuppressWarnings("unused")
    @ALIAS(declaringClass = FileKey.class)
    private long st_dev;
    
    @SuppressWarnings("unused")
    @ALIAS(declaringClass = FileKey.class)
    private long st_ino;
    
    @SuppressWarnings("unused")
    @SUBSTITUTE
    private void init(FileDescriptor fdObj) throws IOException {
        final int fd = JDK_java_io_FileDescriptor.getFd(fdObj);
        JDK_sun_nio_ch_FileKey thisJDK_sun_nio_ch_FileKey = asJDK_sun_nio_ch_FileKey(this);
        thisJDK_sun_nio_ch_FileKey.st_dev = VirtualFileSystemId.getVfsId(fd);
        thisJDK_sun_nio_ch_FileKey.st_ino = VirtualFileSystemId.getVfs(fd).uniqueId(VirtualFileSystemId.getFd(fd));
    }

    @SuppressWarnings("unused")
    @SUBSTITUTE
    private static void initIDs() {

    }

}
