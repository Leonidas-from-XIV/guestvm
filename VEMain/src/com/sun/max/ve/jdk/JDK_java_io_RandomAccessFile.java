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

import static com.sun.max.ve.fs.VirtualFileSystem.*;
import static com.sun.max.ve.jdk.AliasCast.*;

import java.io.*;

import com.sun.max.annotate.*;
import com.sun.max.ve.error.*;
import com.sun.max.ve.fs.*;


/**
 * Substitutions for @see java.io.RandomAccessFile.
 * @author Mick Jordan
 */

@METHOD_SUBSTITUTIONS(RandomAccessFile.class)

public class JDK_java_io_RandomAccessFile {

    // Copied from RandomAccessFile.java (O -> RA because RDONLY differs from standard Unix value (in VirtualFileSystem)
    private static final int RA_RDONLY = 1;
    private static final int RA_RDWR =   2;
    private static final int RA_SYNC =   4;
    private static final int RA_DSYNC =  8;
    
    @ALIAS(declaringClass = RandomAccessFile.class)
    FileDescriptor fd;

    @INLINE
    private static FileDescriptor getFileDescriptor(Object obj) {
        JDK_java_io_RandomAccessFile thisRandomAccessFile = asJDK_java_io_RandomAccessFile(obj);
        return thisRandomAccessFile.fd;
    }

    @SuppressWarnings("unused")
    @SUBSTITUTE
    private void open(String name, int mode) throws FileNotFoundException {
        int uMode = 0;
        if ((mode & RA_RDONLY) != 0) {
            uMode = O_RDONLY;
        } else if ((mode & RA_RDWR) != 0) {
            uMode = O_RDWR | O_CREAT;
            if ((mode & RA_SYNC) != 0) {
                uMode = O_RDWR | O_CREAT;
                //VEError.unimplemented("RandomAccessFile SYNC mode");
            } else if ((mode & RA_DSYNC) != 0) {
                uMode = O_RDWR | O_CREAT;
                //VEError.unimplemented("RandomAccessFile DSYNC mode");
            }
        } else {
            VEError.unexpected("RandomAccessFile.open mode: " + mode);
        }
        JavaIOUtil.open(getFileDescriptor(this), name, uMode);

    }

    @SuppressWarnings("unused")
    @SUBSTITUTE
    private int read() throws IOException {
        return JavaIOUtil.read(getFileDescriptor(this));
    }

    @SuppressWarnings("unused")
    @SUBSTITUTE
    private int readBytes(byte[] b, int offset, int length) throws IOException {
        return JavaIOUtil.readBytes(b, offset, length, getFileDescriptor(this));
    }

    @SuppressWarnings("unused")
    @SUBSTITUTE
    private void write(int b) throws IOException {
        JavaIOUtil.write(b, getFileDescriptor(this));
    }

    @SUBSTITUTE
    private void writeBytes(byte[] bytes, int offset, int length) throws IOException {
        JavaIOUtil.writeBytes(bytes, offset, length, getFileDescriptor(this));
    }

    @SuppressWarnings("unused")
    @SUBSTITUTE
    private long getFilePointer() throws IOException {
        final int fd = JDK_java_io_FileDescriptor.getFd(getFileDescriptor(this));
        final long fileOffset = VirtualFileSystemOffset.get(fd);
        return fileOffset;
    }

    @SuppressWarnings("unused")
    @SUBSTITUTE
    private void seek(long pos) throws IOException {
        final int fd = JDK_java_io_FileDescriptor.getFd(getFileDescriptor(this));
        VirtualFileSystemOffset.set(fd, pos);
    }

    @SuppressWarnings("unused")
    @SUBSTITUTE
    private long length() throws IOException {
        final int fd = JDK_java_io_FileDescriptor.getFd(getFileDescriptor(this));
        return VirtualFileSystemId.getVfs(fd).getLength(VirtualFileSystemId.getFd(fd));
    }

    @SuppressWarnings("unused")
    @SUBSTITUTE
    private void setLength(long newLength) throws IOException {
        final int fd = JDK_java_io_FileDescriptor.getFd(getFileDescriptor(this));
        VirtualFileSystemId.getVfs(fd).setLength(VirtualFileSystemId.getFd(fd), newLength);
    }

    @SuppressWarnings("unused")
    @SUBSTITUTE
    private void close0() throws IOException {
        JavaIOUtil.close0(getFileDescriptor(this));
    }

    @SuppressWarnings("unused")
    @SUBSTITUTE
    private static void initIDs() {

    }

}
