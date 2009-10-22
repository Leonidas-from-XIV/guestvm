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
import com.sun.max.vm.runtime.*;
import com.sun.guestvm.error.*;
import com.sun.guestvm.fs.*;

/**
 * Substitutions for  @see sun.nio.ch.FileDispatcher.
 * @author Mick Jordan
 *
 */

@SuppressWarnings("unused")

@METHOD_SUBSTITUTIONS(hiddenClass = "sun.nio.ch.FileDispatcher")
final class JDK_sun_nio_ch_FileDispatcher {

    // copied from sun.nio.IOStatus (not public)
    private static final int EOF = -1;                       // end of file
    private static final int UNAVAILABLE = -2;      // Nothing available (non-blocking)
    private static final int INTERRUPTED = -3;    // System call interrupted

    @SUBSTITUTE
    private static int read0(FileDescriptor fdObj, long address, int length) throws IOException {
        final JDK_java_io_util.FdInfo fdInfo = JDK_java_io_util.FdInfo.getFdInfo(fdObj);
        final int result = convertReturnValue(fdInfo._vfs.readBytes(VirtualFileSystemId.getFd(fdInfo._fd), address, 0, length, fdInfo._fileOffset), true);
        return result;
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

    private static int convertReturnValue(int n, boolean reading) throws IOException {
        if (n > 0) {
            return n;
        } else if (n < 0) {
            if (-n == ErrorDecoder.Code.EINTR.getCode()) {
                return INTERRUPTED;
            } else if (-n == ErrorDecoder.Code.EAGAIN.getCode()) {
                return UNAVAILABLE;
            }
            throw new IOException("Read error: " + ErrorDecoder.getMessage(-n));
        } else {
            if (reading) {
                return EOF;
            } else {
                return 0;
            }
        }
    }

    @SUBSTITUTE
    private static int write0(FileDescriptor fdObj, long address, int length) throws IOException {
        final JDK_java_io_util.FdInfo fdInfo = JDK_java_io_util.FdInfo.getFdInfo(fdObj);
        final int result = convertReturnValue(fdInfo._vfs.writeBytes(VirtualFileSystemId.getFd(fdInfo._fd), address, 0, length, fdInfo._fileOffset), false);
        return result;
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
        JDK_java_io_util.close0(fd);
    }

    @SUBSTITUTE
    private static void preClose0(FileDescriptor fd) throws IOException {
        // TODO the HotSpot native code does the "dup" thing, what is our equivalent?
    }

    @SUBSTITUTE
    private static void closeIntFD(int fd) throws IOException {
        JDK_java_io_util.close0FD(fd);
    }

    @SUBSTITUTE
    private static void init() {
        // TODO the HotSpot native code creates a pair of file descriptors with socketpair,
        // saves one of them for preClose0 and closes the other.
    }

}
