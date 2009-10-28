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
import com.sun.guestvm.error.*;
import com.sun.guestvm.fs.*;
import static com.sun.guestvm.jdk.JDK_java_io_util.*;
import com.sun.max.annotate.*;
import com.sun.max.vm.actor.member.*;
import com.sun.max.vm.object.*;
import com.sun.max.vm.runtime.*;

/**
 * Substitutions for  @see sun.nio.ch.IOUtil.
 * @author Mick Jordan
 *
 */

@SuppressWarnings("unused")

@METHOD_SUBSTITUTIONS(hiddenClass = "sun.nio.ch.IOUtil")
public class JDK_sun_nio_ch_IOUtil {

    private static PipeFileSystem _pipeFS = new PipeFileSystem();

    @SUBSTITUTE
    private static void initIDs() {

    }

    @SUBSTITUTE
    static boolean randomBytes(byte[] someBytes) {
        GuestVMError.unimplemented("sun.nio.ch.IOUtil.randomBytes");
        return false;
    }

    @SUBSTITUTE
    static void initPipe(int[] fda, boolean blocking) {
        _pipeFS.createPipe(fda, blocking);
    }

    @SUBSTITUTE
    static boolean drain(int fd) throws IOException {
        GuestVMError.unimplemented("sun.nio.ch.IOUtil.drain");
        return false;
    }

    @SUBSTITUTE
    static void configureBlocking(FileDescriptor fd, boolean blocking) throws IOException {
        final FdInfo fdInfo = FdInfo.getFdInfo(fd);
        fdInfo._vfs.configureBlocking(VirtualFileSystemId.getFd(fdInfo._fd), blocking);
    }

    @SUBSTITUTE
    static int fdVal(FileDescriptor fdObj) {
        return TupleAccess.readInt(fdObj, JDK_java_io_fdActor.fdFieldActor().offset());
    }

    @SUBSTITUTE
    static void setfdVal(FileDescriptor fdObj, int value) {
        TupleAccess.writeInt(fdObj, JDK_java_io_fdActor.fdFieldActor().offset(), value);
    }

}
