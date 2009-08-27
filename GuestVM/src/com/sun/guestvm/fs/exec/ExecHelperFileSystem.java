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

import com.sun.guestvm.fs.DefaultFileSystemImpl;
import com.sun.guestvm.fs.VirtualFileSystem;
import com.sun.guestvm.fs.VirtualFileSystemId;
import com.sun.max.program.ProgramError;

/**
 * All the unimplemented methods for ExecFileSystem and FilterFileSystem.
 *
 * @author Mick Jordan
 *
 */

public abstract class ExecHelperFileSystem extends DefaultFileSystemImpl implements VirtualFileSystem {

    /**
     * This method is called to generate the stdin, stdout and stderr file descriptors, respectively.
    *
     * @param key that identifies the exec call
     * @return an array of length three containing the file descriptors
     */
    public int[] getFds(int key) {
        final int[] fds = getSpecificFds(key);
        for (int i = 0; i < fds.length; i++) {
            fds[i] = VirtualFileSystemId.getUniqueFd(this, fds[i]);
        }
        return fds;
    }

    /**
     * Default implementation encodes the fd in the key.
     *
     * @param key
     * @return
     */
    protected int[] getSpecificFds(int key) {
        return new int[] {key, key + 1, key + 2};
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
