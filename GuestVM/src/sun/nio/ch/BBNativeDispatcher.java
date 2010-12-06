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
import com.sun.guestvm.jdk.JavaIOUtil;
import com.sun.guestvm.jdk.JavaIOUtil.FdInfo;
import com.sun.max.vm.actor.holder.ClassActor;
import com.sun.max.vm.actor.member.FieldActor;

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

    public static void resetNativeDispatchers() {
        final BBNativeDispatcher bbnd = new BBNativeDispatcher();
        resetNativeDispatcher("sun.nio.ch.DatagramChannelImpl", bbnd);
        resetNativeDispatcher("sun.nio.ch.ServerSocketChannelImpl", bbnd);
        resetNativeDispatcher("sun.nio.ch.SocketChannelImpl", bbnd);
        resetNativeDispatcher("sun.nio.ch.SinkChannelImpl", bbnd);
        resetNativeDispatcher("sun.nio.ch.SourceChannelImpl", bbnd);
        resetNativeDispatcher("sun.nio.ch.FileChannelImpl", bbnd);
    }

    private static void resetNativeDispatcher(String name, BBNativeDispatcher nd) {
        try {
            final FieldActor rfa = ClassActor.fromJava(Class.forName(name)).findLocalStaticFieldActor("nd");
            assert rfa != null;
            rfa.setObject(null, nd);
        } catch (ClassNotFoundException ex) {
            GuestVMError.unexpected("problem with Class.forName: " + name);
        }
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

    public int write(FileDescriptor fdObj, ByteBuffer bb) throws IOException {
        final JavaIOUtil.FdInfo fdInfo = JavaIOUtil.FdInfo.getFdInfo(fdObj);
        final int result = convertReturnValue(fdInfo._vfs.writeBytes(VirtualFileSystemId.getFd(fdInfo._fd), bb, fdInfo._fileOffset), false);
        return result;
    }

    public int write(FileDescriptor fdObj, ByteBuffer[] bbs) throws IOException {
        final JavaIOUtil.FdInfo fdInfo = JavaIOUtil.FdInfo.getFdInfo(fdObj);
        final int fd = VirtualFileSystemId.getFd(fdInfo._fd);
        return write(fdObj, fdInfo, fd, fdInfo._fileOffset, bbs);
    }


    @Override
    public int write(FileDescriptor fdObj, long fileOffset, ByteBuffer... bbs) throws IOException {
        final JavaIOUtil.FdInfo fdInfo = JavaIOUtil.FdInfo.getFdInfo(fdObj);
        final int fd = VirtualFileSystemId.getFd(fdInfo._fd);
        return write(fdObj,fdInfo,fd,fileOffset,bbs);
    }

    private int write(FileDescriptor fdObj, FdInfo fdInfo, int fd, long fileOffset, ByteBuffer... bbs)throws IOException {
        int bytesWritten = 0;
        for (int i = 0; i < bbs.length; i++) {
            final int result = convertReturnValue(fdInfo._vfs.writeBytes(fd, bbs[i], fileOffset), false);
            if (result < 0) {
                return result;
            }
            bytesWritten += result;
        }
        return bytesWritten;
    }
    public int read(FileDescriptor fdObj, ByteBuffer bb) throws IOException {
        final JavaIOUtil.FdInfo fdInfo = JavaIOUtil.FdInfo.getFdInfo(fdObj);
        final int result = convertReturnValue(fdInfo._vfs.readBytes(VirtualFileSystemId.getFd(fdInfo._fd), bb, fdInfo._fileOffset), true);
        return result;
    }

    @Override
    public int read(FileDescriptor fdObj, long fileOffset, ByteBuffer... bb) throws IOException {
        final JavaIOUtil.FdInfo fdInfo = JavaIOUtil.FdInfo.getFdInfo(fdObj);
        final int fd = VirtualFileSystemId.getFd(fdInfo._fd);
        return read(fdObj, fdInfo, fd, fileOffset, bb);
    }

    private int read(FileDescriptor fdObj, FdInfo fdInfo, int fd, long fileOffset, ByteBuffer... bbs) throws IOException {
        int bytesRead = 0;
        for (int i = 0; i < bbs.length; i++) {
            final int result = convertReturnValue(fdInfo._vfs.readBytes(fd, bbs[i], fileOffset), true);
            if (result < 0) {
                return result;
            }
            bytesRead += result;
        }
        return bytesRead;
    }

    public int read(FileDescriptor fdObj, ByteBuffer[] bbs) throws IOException {
        final JavaIOUtil.FdInfo fdInfo = JavaIOUtil.FdInfo.getFdInfo(fdObj);
        final int fd = VirtualFileSystemId.getFd(fdInfo._fd);
        return read(fdObj, fdInfo, fd, fdInfo._fileOffset, bbs);
    }

    @Override
    void close(FileDescriptor fd) throws IOException {
        JavaIOUtil.close0(fd);
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
