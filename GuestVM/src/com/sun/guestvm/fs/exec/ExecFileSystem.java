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
package com.sun.guestvm.fs.exec;

import java.io.IOException;
import java.util.*;

import com.sun.max.program.*;

import com.sun.guestvm.fs.DefaultFileSystemImpl;
import com.sun.guestvm.fs.VirtualFileSystem;
import com.sun.guestvm.fs.VirtualFileSystemId;
import com.sun.guestvm.guk.GUKExec;

/**
 * This is not really a file system but it supports the ability to communicate
 * with the fork/exec backend using file descriptors.
 *
 * @author Mick Jordan
 *
 */

public class ExecFileSystem extends DefaultFileSystemImpl implements VirtualFileSystem {

    private static ExecFileSystem _singleton;

    public static ExecFileSystem create() {
        if (_singleton == null) {
            _singleton = new ExecFileSystem();
        }
        return _singleton;
    }

    public static int getFd(int fd) {
        return VirtualFileSystemId.getUniqueFd(_singleton, fd);
    }

    @Override
    public int available(int fd, long fileOffset) {
        // TODO implement
        return 0;
    }

    @Override
    public String canonicalize0(String path) throws IOException {
        ProgramError.unexpected("canonicalize0 not implemented");
        return null;
    }

    @Override
    public boolean checkAccess(String path, int access) {
        ProgramError.unexpected("checkAccess not implemented");
        return false;
    }

    @Override
    public void close() {
    }

    @Override
    public int close0(int fd) {
        return GUKExec.close(fd);
    }

    @Override
    public boolean createDirectory(String path) {
        ProgramError.unexpected("createDirectory not implemented");
        return false;
    }

    @Override
    public boolean createFileExclusively(String path) throws IOException {
        ProgramError.unexpected("createFileExclusively not implemented");
        return false;
    }

    @Override
    public boolean delete0(String path) {
        ProgramError.unexpected("delete0 not implemented");
        return false;
    }

    @Override
    public long getLastModifiedTime(String path) {
        ProgramError.unexpected("getLastModifiedTime not implemented");
        return 0;
    }

    @Override
    public long getLength(String path) {
        ProgramError.unexpected("getLength not implemented");
        return 0;
    }

    @Override
    public long getLength(int fd) {
        ProgramError.unexpected("getLength not implemented");
        return 0;
    }

    @Override
    public int getMode(String path) {
        ProgramError.unexpected("getMode not implemented");
        return 0;
    }

    @Override
    public long getSpace(String path, int t) {
        ProgramError.unexpected("getSpace not implemented");
        return 0;
    }

    @Override
    public String[] list(String path) {
        ProgramError.unexpected("list not implemented");
        return null;
    }

    @Override
    public int open(String name, int flags) {
        ProgramError.unexpected("open not implemented");
        return 0;
    }

    @Override
    public int read(int fd, long fileOffset) {
        ProgramError.unexpected("read not implemented");
        return 0;
    }

    @Override
    public int readBytes(int fd, byte[] bytes, int offset, int length, long fileOffset) {
        return GUKExec.readBytes(fd, bytes, offset, length, fileOffset);
    }

    @Override
    public boolean rename0(String path1, String path2) {
        ProgramError.unexpected("rename0 not implemented");
        return false;
    }

    @Override
    public boolean setLastModifiedTime(String path, long time) {
        ProgramError.unexpected("setLastModifiedTime not implemented");
        return false;
    }

    @Override
    public int setLength(int fd, long length) {
        ProgramError.unexpected("setLength not implemented");
        return 0;
    }

    @Override
    public boolean setPermission(String path, int access, boolean enable, boolean owneronly) {
        ProgramError.unexpected("setPermission not implemented");
        return false;
    }

    @Override
    public boolean setReadOnly(String path) {
        ProgramError.unexpected("setReadOnly not implemented");
        return false;
    }

    @Override
    public long skip(int fd, long n, long fileOffset) {
        ProgramError.unexpected("skip not implemented");
        return 0;
    }

    @Override
    public long uniqueId(int fd) {
        ProgramError.unexpected("uniqueId not implemented");
        return 0;
    }

    @Override
    public int write(int fd, int b, long fileOffset) {
        ProgramError.unexpected("write not implemented");
        return 0;
    }

    @Override
    public int writeBytes(int fd, byte[] bytes, int offset, int length, long fileOffset) {
        ProgramError.unexpected("writeBytes not implemented");
        return 0;
    }

}
