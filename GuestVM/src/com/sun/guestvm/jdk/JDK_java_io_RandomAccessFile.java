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
import com.sun.guestvm.error.*;
import static com.sun.guestvm.fs.VirtualFileSystem.*;
import com.sun.max.vm.actor.member.*;
import com.sun.max.vm.object.*;

import com.sun.guestvm.fs.*;

/**
 * Substitutions for @see java.io.RandomAccessFile.
 * @author Mick Jordan
 */

@METHOD_SUBSTITUTIONS(RandomAccessFile.class)

@SuppressWarnings("unused")

public class JDK_java_io_RandomAccessFile {

    // Copied from RandomAccessFile.java (O -> RA because RDONLY differs from standard Unix value (in VirtualFileSystem)
    private static final int RA_RDONLY = 1;
    private static final int RA_RDWR =   2;
    private static final int RA_SYNC =   4;
    private static final int RA_DSYNC =  8;
    @SUBSTITUTE
    private void open(String name, int mode) throws FileNotFoundException {
        int uMode = 0;
        if ((mode & RA_RDONLY) != 0) {
            uMode = O_RDONLY;
        } else if ((mode & RA_RDWR) != 0) {
            uMode = O_RDWR | O_CREAT;
            if ((mode & RA_SYNC) != 0) {
                uMode = O_RDWR | O_CREAT;
                //GuestVMError.unimplemented("RandomAccessFile SYNC mode");
            } else if ((mode & RA_DSYNC) != 0) {
                uMode = O_RDWR | O_CREAT;
                //GuestVMError.unimplemented("RandomAccessFile DSYNC mode");
            }
        } else {
            GuestVMError.unexpected("RandomAccessFile.open mode: " + mode);
        }
        JDK_java_io_util.open(TupleAccess.readObject(this, fileDescriptorFieldActor().offset()), name, uMode);

    }

    @SUBSTITUTE
    private int read() throws IOException {
        return JDK_java_io_util.read(TupleAccess.readObject(this, fileDescriptorFieldActor().offset()));
    }

    @SUBSTITUTE
    private int readBytes(byte[] b, int offset, int length) throws IOException {
        return JDK_java_io_util.readBytes(b, offset, length, TupleAccess.readObject(this, fileDescriptorFieldActor().offset()));
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
    private long getFilePointer() throws IOException {
        final int fd = TupleAccess.readInt(TupleAccess.readObject(this, fileDescriptorFieldActor().offset()), JDK_java_io_fdActor.fdFieldActor().offset());
        final long fileOffset = VirtualFileSystemOffset.get(fd);
        return fileOffset;
    }

    @SUBSTITUTE
    private void seek(long pos) throws IOException {
        final int fd = TupleAccess.readInt(TupleAccess.readObject(this, fileDescriptorFieldActor().offset()), JDK_java_io_fdActor.fdFieldActor().offset());
        VirtualFileSystemOffset.set(fd, pos);
    }

    @SUBSTITUTE
    private long length() throws IOException {
        final int fd = TupleAccess.readInt(TupleAccess.readObject(this, fileDescriptorFieldActor().offset()), JDK_java_io_fdActor.fdFieldActor().offset());
        return VirtualFileSystemId.getVfs(fd).getLength(VirtualFileSystemId.getFd(fd));
    }

    @SUBSTITUTE
    private void setLength(long newLength) throws IOException {
        final int fd = TupleAccess.readInt(TupleAccess.readObject(this, fileDescriptorFieldActor().offset()), JDK_java_io_fdActor.fdFieldActor().offset());
        VirtualFileSystemId.getVfs(fd).setLength(VirtualFileSystemId.getFd(fd), newLength);
    }

    @SUBSTITUTE
    private void close0() throws IOException {
        JDK_java_io_util.close0(TupleAccess.readObject(this, fileDescriptorFieldActor().offset()));
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
