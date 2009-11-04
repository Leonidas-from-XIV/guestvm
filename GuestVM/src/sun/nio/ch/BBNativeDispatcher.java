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
package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;
import com.sun.guestvm.error.*;
import com.sun.guestvm.fs.ErrorDecoder;
import com.sun.guestvm.fs.VirtualFileSystemId;
import com.sun.guestvm.jdk.JDK_java_io_util;

/**
 * This is part of the mechanism that replaces the part of the Sun JDK that uses an "address, length"
 * pattern for the native nio interface (NativeDispatcher). This class extends NativeDispatcher
 * with methods that use ByteBuffers. These methods are used in the substituted methods of sun.nio.ch.IOUtil.
 *
 * @author Mick Jordan
 *
 */

public class BBNativeDispatcher extends ByteBufferNativeDispatcher {

    // copied from sun.nio.IOStatus (not public)
    private static final int EOF = -1;                       // end of file
    private static final int UNAVAILABLE = -2;      // Nothing available (non-blocking)
    private static final int INTERRUPTED = -3;    // System call interrupted

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

    public int write(FileDescriptor fdObj, ByteBuffer bb) throws IOException {
        final JDK_java_io_util.FdInfo fdInfo = JDK_java_io_util.FdInfo.getFdInfo(fdObj);
        final int result = convertReturnValue(fdInfo._vfs.writeBytes(VirtualFileSystemId.getFd(fdInfo._fd), bb, fdInfo._fileOffset), false);
        return result;

    }

    public int read(FileDescriptor fdObj, ByteBuffer bb) throws IOException {
        final JDK_java_io_util.FdInfo fdInfo = JDK_java_io_util.FdInfo.getFdInfo(fdObj);
        final int result = convertReturnValue(fdInfo._vfs.readBytes(VirtualFileSystemId.getFd(fdInfo._fd), bb, fdInfo._fileOffset), true);
        return result;
    }

    @Override
    void close(FileDescriptor fd) throws IOException {
        JDK_java_io_util.close0(fd);
    }

    @Override
    int read(FileDescriptor fd, long address, int len) throws IOException {
        unexpected("read");
        return 0;
    }

    @Override
    long readv(FileDescriptor fd, long address, int len) throws IOException {
        unexpected("readv");
        return 0;
    }

    @Override
    int write(FileDescriptor fd, long address, int len) throws IOException {
        unexpected("write");
        return 0;
    }

    @Override
    long writev(FileDescriptor fd, long address, int len) throws IOException {
        unexpected("writev");
        return 0;
    }

    static void unexpected(String name) {
        GuestVMError.unexpected("BBNativeDispatcher." + name + " invoked");
    }

}
