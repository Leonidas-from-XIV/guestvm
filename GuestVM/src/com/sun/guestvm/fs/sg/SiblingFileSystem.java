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
package com.sun.guestvm.fs.sg;

import java.io.*;
import java.util.*;

import com.sun.max.unsafe.*;
import com.sun.max.memory.Memory;
import com.sun.max.util.Utf8Exception;
import com.sun.guestvm.error.GuestVMError;
import com.sun.guestvm.fs.*;
import com.sun.guestvm.jdk.JDK_java_io_UnixFileSystem;

/**
 * This class represents the file system that is based on inter-domain communication to
 * a sibling guest operating system acting as a file server.
 *
 * @author Mick Jordan
 *
 */
public final class SiblingFileSystem extends UnimplementedFileSystemImpl implements VirtualFileSystem {

    private Word _handle;
    private String _exportPath;
    private String _mountPath;
    private int _mountPathLength;

    private SiblingFileSystem(Word handle, String exportPath, String mountPath) {
        _handle = handle;
        _exportPath = exportPath;
        _mountPath = mountPath;
        _mountPathLength = _mountPath.length();
    }

    /**
     * Create the file system identified by exportPath.
     *
     * @param exportPath
     * @param mountPath
     * @return
     */
    public static SiblingFileSystem create(String exportPath, String mountPath) {
        final int numImports = SiblingFileSystemNatives.getNumImports();
        for (int i = 0; i < numImports; i++) {
            final Word handle = SiblingFileSystemNatives.getImport(i);
            try {
                final String remotePath = CString.utf8ToJava(SiblingFileSystemNatives.getPath(handle));
                if (exportPath.equals(remotePath)) {
                    return new SiblingFileSystem(handle, exportPath, mountPath);
                }
            } catch (Utf8Exception ex) {
                GuestVMError.unexpected("UTFException in SiblingFileSystem.create");
            }

        }
        return null;
    }

    /**
     * Maps the given path, that is based on the mount path to the server path.
     * @param path
     * @return
     */
    private Pointer remap(String path) {
        return CString.utf8FromJava(_exportPath + path.substring(_mountPathLength));
    }

    @Override
    public void close() {

    }

    @Override
    public String canonicalize0(String path) throws IOException {
        // TODO correct implementation
        return path;
    }

    @Override
    public boolean createDirectory(String path) {
        final Pointer p = remap(path);
        final int rc = SiblingFileSystemNatives.createDirectory(_handle, p);
        Memory.deallocate(p);
        return rc == 0;
    }

    @Override
    public boolean createFileExclusively(String path) throws IOException {
        final Pointer p = remap(path);
        final int rc = SiblingFileSystemNatives.createFileExclusively(_handle, p);
        Memory.deallocate(p);
        if (rc == 0) {
            return true;
        } else if (rc == -2) {
            return false;
        } else {
            throw new IOException("create of file " + path + " failed");
        }
    }

    @Override
    public boolean delete0(String path) {
        final Pointer p = remap(path);
        final int rc = SiblingFileSystemNatives.delete(_handle, p);
        Memory.deallocate(p);
        return rc == 0;
    }

    @Override
    public int getMode(String path) {
        final Pointer p = remap(path);
        final int mode = SiblingFileSystemNatives.getMode(_handle, p);
        Memory.deallocate(p);
        return mode;
    }

    @Override
    public long getLastModifiedTime(String path) {
        final Pointer p = remap(path);
        final long mTime = SiblingFileSystemNatives.getLastModifiedTime(_handle, p);
        Memory.deallocate(p);
        return mTime < 0 ? 0 : mTime * 1000;
    }

    @Override
    public long getLength(String path) {
        final Pointer p = remap(path);
        final long length = SiblingFileSystemNatives.getLength(_handle, p);
        Memory.deallocate(p);
        return length < 0 ? 0 : length;
    }

    @Override
    public String[] list(String path) {
        final Pointer dir = remap(path);
        final int[] nFiles = new int[1];
        final boolean[] hasMore = new boolean[1];
        final List<String> strings = new ArrayList<String>();
        int offset = 0;
        do {
            hasMore[0] = false; nFiles[0] = 0;
            final Pointer files = SiblingFileSystemNatives.list(_handle, dir, offset, nFiles, hasMore);
            if (files.isZero()) {
                return null;
            }
            for (int i = 0; i < nFiles[0]; i++) {
                final Pointer cString = files.getWord(i).asPointer();
                try {
                    final String name = CString.utf8ToJava(cString);
                    if (!JDK_java_io_UnixFileSystem.currentOrParent(name)) {
                        strings.add(name);
                    }
                    Memory.deallocate(cString);
                } catch (Utf8Exception ex) {
                    return null;
                }
            }
            offset += nFiles[0];
            Memory.deallocate(files);
        } while (hasMore[0]);
        Memory.deallocate(dir);
        return strings.toArray(new String[strings.size()]);
    }

    @Override
    public boolean rename0(String path1, String path2) {
        final Pointer p1 = remap(path1);
        final Pointer p2 = remap(path2);
        final int rc = SiblingFileSystemNatives.rename(_handle, p1, p2);
        Memory.deallocate(p1); Memory.deallocate(p2);
        return rc == 0;
    }

    // FileInputStream, FileOutputStream

    @Override
    public int read(int fd, long fileOffset) {
        return SiblingFileSystemNatives.read(_handle, fd, fileOffset);
    }

    @Override
    public int readBytes(int fd, byte[] bytes, int offset, int length, long fileOffset) {
        // This is where the native code has the edge for now. For smallish arrays it uses C stack allocation
        // for the place to read the data to.
        final Pointer nativeBytes = Memory.allocate(Size.fromInt(length));
        final int result = SiblingFileSystemNatives.readBytes(_handle, fd, nativeBytes, length, fileOffset);
        Memory.readBytes(nativeBytes, length, bytes, offset);
        Memory.deallocate(nativeBytes);
        return result;
    }

    @Override
    public int write(int fd, int b, long fileOffset) {
        return SiblingFileSystemNatives.write(_handle, fd, b, fileOffset);
    }

    @Override
    public int writeBytes(int fd, byte[] bytes, int offset, int length, long fileOffset) {
        final Pointer nativeBytes = Memory.allocate(Size.fromInt(length));
        // This is where the native code has the edge for now. For smallish arrays it uses C stack allocation
        // for the place to copy the data to.
        Memory.writeBytes(bytes, offset, length, nativeBytes);
        final int result = SiblingFileSystemNatives.writeBytes(_handle, fd, nativeBytes, length, fileOffset);
        Memory.deallocate(nativeBytes);
        return result;
    }

    @Override
    public int open(String name, int flags) {
        final Pointer p = remap(name);
        final int fd = SiblingFileSystemNatives.open(_handle, p, flags);
        Memory.deallocate(p);
        return fd;
    }

    @Override
    public int close0(int fd) {
        return SiblingFileSystemNatives.close0(_handle, fd);
    }

    @Override
    public long getLength(int fd) {
        return SiblingFileSystemNatives.getLengthFd(_handle, fd);
    }

    @Override
    public int setLength(int fd, long length) {
        return (int) SiblingFileSystemNatives.setLengthFd(_handle, fd, length);
    }

    @Override
    public int available(int fd, long fileOffset) {
        return 0;
    }

}
