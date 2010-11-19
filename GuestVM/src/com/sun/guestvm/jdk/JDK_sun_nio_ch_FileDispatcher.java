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
import java.nio.*;
import sun.nio.ch.*;

import com.sun.max.annotate.*;
import com.sun.max.vm.runtime.*;
import com.sun.guestvm.error.*;
import com.sun.guestvm.fs.*;

/**
 * Substitutions for  @see sun.nio.ch.FileDispatcher.
 * N.B. None of these methods should ever be called, except closeIntFD, as we
 * install a GuestVM specific dispatcher that works with ByteBuffers.
 *
 * @author Mick Jordan
 *
 */

@SuppressWarnings("unused")

@METHOD_SUBSTITUTIONS(className = "sun.nio.ch.FileDispatcher")
final class JDK_sun_nio_ch_FileDispatcher {

    @SUBSTITUTE
    private static int read0(FileDescriptor fdObj, long address, int length) throws IOException {
        GuestVMError.unimplemented("sun.nio.ch.FileDispatcher.read0");
        return 0;
    }

    @SUBSTITUTE
    private static int pread0(FileDescriptor fd, long address, int len, long position) throws IOException {
        GuestVMError.unimplemented("sun.nio.ch.FileDispatcher.pread0");
        return 0;
    }

    @SUBSTITUTE
    private static long readv0(FileDescriptor fd, long address, int len) throws IOException {
        GuestVMError.unimplemented("sun.nio.ch.FileDispatcher.readv0");
        return 0;
    }

    @SUBSTITUTE
    private static int write0(FileDescriptor fdObj, long address, int length) throws IOException {
        GuestVMError.unimplemented("sun.nio.ch.FileDispatcher.write0");
        return 0;
    }

    @SUBSTITUTE
    private static int pwrite0(FileDescriptor fd, long address, int len, long position) throws IOException {
        GuestVMError.unimplemented("sun.nio.ch.FileDispatcher.pwrite0");
        return 0;
    }

    @SUBSTITUTE
    private static long writev0(FileDescriptor fd, long address, int len) throws IOException {
        GuestVMError.unimplemented("sun.nio.ch.FileDispatcher.writev0");
        return 0;
    }

    @SUBSTITUTE
    private static void close0(FileDescriptor fd) throws IOException {
        JavaIOUtil.close0(fd);
    }

    @SUBSTITUTE
    private static void preClose0(FileDescriptor fd) throws IOException {
        // TODO the HotSpot native code does the "dup" thing, what is our equivalent?
    }

    @SUBSTITUTE
    private static void closeIntFD(int fd) throws IOException {
        JavaIOUtil.close0FD(fd);
    }

    @SUBSTITUTE
    private static void init() {
    }

}
