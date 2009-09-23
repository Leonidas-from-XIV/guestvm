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
package com.sun.guestvm.fs;

import java.io.*;

/**
 * Of course we have to have a virtual file system interface.
 * These are almost exactly the native methods substituted by UnixFileSystem,
 * FileInputStream/FileOutputStream, RandonAccessFile and the nio classes.
 *
 * @author Mick Jordan
 *
 */
public interface VirtualFileSystem {

    // Unix open flags
    int O_RDONLY = 0;
    int O_WRONLY = 1;
    int O_RDWR = 2;
    int O_APPEND = 0x8;
    int O_CREAT = 0x100;
    int O_TRUNC = 0x200;

    // Unix file modes
    int S_IFMT = 0xF000;
    int S_IAMB = 0x1FF;
    int S_IFREG = 0x8000;
    int S_IFDIR = 0x4000;
    int S_IREAD = 0x100;
    int S_IWRITE = 0x80;
    int S_IEXEC = 0x40;

    int S_IRUSR = 0x100;
    int S_IWUSR = 0x80;
    int S_IXUSR = 0x40;
    int S_IRGRP = 0x20;
    int S_IWGRP = 0x10;
    int S_IXGRP = 0x8;
    int S_IROTH = 0x4;
    int S_IWOTH = 0x2;
    int S_IXOTH = 0x1;

    // copied from java.io.FileSystem.java
    int BA_EXISTS    = 0x01;
    int BA_REGULAR   = 0x02;
    int BA_DIRECTORY = 0x04;

    int ACCESS_READ    = 0x04;
    int ACCESS_WRITE   = 0x02;
    int ACCESS_EXECUTE = 0x01;

    int SPACE_TOTAL  = 0;
    int SPACE_FREE   = 1;
    int SPACE_USABLE = 2;
    int SPACE_USED = 3; // added, notionally SPACE_TOTAL-SPACE_FREE, but what if SPACE_TOTAL is fuzzy, e.g. /tmp?

    /**
     * Shutdown the file system.
     */
    void close();

    /*
     *  UnixFileSystem methods
     *  All file system paths passed to the following methods should be absolute.
     */

    String canonicalize0(String path) throws IOException;

    // This replaces getBooleanAttributes0, it returns the Unix mode
    int getMode(String path);

    // This replaces setPermission, it sets the Unix mode
    int setMode(String path, int mode);

    long getLastModifiedTime(String path);

    boolean checkAccess(String path, int access);

    long getLength(String path);

    boolean createFileExclusively(String path) throws IOException;

    boolean delete0(String path);

    String[] list(String path);

    boolean createDirectory(String path);

    boolean rename0(String path1, String path2);

    boolean setLastModifiedTime(String path, long time);

    long getSpace(String path, int t);

    /*
     * FileInputStream, FileOutputStream methods
     *
     *  These functions enode the traditional C errno value in their results.
     *  A negative return is to be interpreted as an error and equals to -errno.
     *  A zero return from read/readBytes means EOF.
     *
     */

    int available(int fd, long fileOffset);

    long skip(int fd, long n, long fileOffset);

    int read(int fd, long fileOffset);

    int readBytes(int fd, byte[] bytes, int offset, int length, long fileOffset);

    int write(int fd, int b, long fileOffset);

    int writeBytes(int fd, byte[] bytes, int offset, int length, long fileOffset);

    int open(String name, int flags);

    int close0(int fd);

    /*
     * RandomAccessFile methods
     */

    long getLength(int fd);

    int setLength(int fd, long length);

    /*
     * nio.* method support
     */

    /**
     * an "inode" in Unix terms, aka a unique value that identifies the file open on fd.
     */
    long uniqueId(int fd);

    int lock0(int fd, boolean blocking, long pos, long size, boolean shared) throws IOException;

    void release0(int fd, long pos, long size) throws IOException;

    int force0(int fd, boolean metaData) throws IOException;
}
