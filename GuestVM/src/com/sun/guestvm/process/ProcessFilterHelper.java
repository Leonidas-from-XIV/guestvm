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
package com.sun.guestvm.process;

import java.util.*;

import com.sun.guestvm.fs.ErrorDecoder;
import com.sun.guestvm.fs.VirtualFileSystemId;

/**
 * A class to assists in the implementation of a filter for single process.
 * In particular it handles the connection to the @see FilterFileSystem by mapping
 * the file descriptor keys that encode both the exec instance and the StdIO fd.
 * Exec ids are allocated such that they are evenly divisible by 3.
 * The StdIO file descriptors are encoded as id, id+1, id+2.
 *
 * @author Mick Jordan
 *
 */
public abstract class ProcessFilterHelper implements GuestVMProcessFilter {
    private static int _nextId;
    protected String _name;
    private String[] _names;
    private static Map<Integer, ProcessFilterHelper> _fdMap = new HashMap<Integer, ProcessFilterHelper>();
    enum StdIO {
        IN, OUT, ERR;
    }

    public String[] names() {
        return _names;
    }

    protected ProcessFilterHelper(String name) {
        _name = name;
        _names = new String[] {name};
    }

    /**
     * A unique id across all filters that can also encode the three file descriptors.
     * @return
     */
    final int nextId() {
        final int result = _nextId;
        _nextId += 3;
        return result;
    }

    protected static int keyToFd(int key) {
        return key % 3;
    }

    public void registerFds(int[] fds) {
        /* The fds have the VFS id encoded but that is stripped off before being
         * passed to the concrete file systems calls, so we strip it here also. */
        for (int i = 0; i < fds.length; i++) {
            _fdMap.put(VirtualFileSystemId.getFd(fds[i]), this);
        }
    }

    /**
     * Converts a null-separated, null-terminated byte array into an array of Strings.
     * @param data
     * @return
     */
    protected String[] cmdArgs(byte[] data) {
        String[] result;
        int numArgs = 0;
        for (int i = 0; i < data.length; i++) {
            if (data[i] == 0) {
                numArgs++;
            }
        }
        result = new String[numArgs];
        if (numArgs > 0) {
            int k = 0;
            int j = 0;
            for (int i = 0; i < data.length; i++) {
                if (data[i] == 0) {
                    result[k] = new String(data, j, i - j);
                    j = i + 1;
                    k++;
                }
            }
        }
        return result;
    }

    public static int invokeClose0(int key) {
        final int rc = _fdMap.get(key).close0(keyToFd(key));
        _fdMap.remove(key);
        return rc;
    }

    public static int invokeReadBytes(int key, byte[] bytes, int offset, int length, long fileOffset) {
        final int fd = keyToFd(key);
        /* N.B. StdIO.IN is the exec filter's input, so applications write to it, and read from OUT and ERR */
        if (fd == StdIO.IN.ordinal()) {
            return -ErrorDecoder.Code.EBADF.getCode();
        }
        return _fdMap.get(key).readBytes(fd, bytes, offset, length, fileOffset);
    }

    /* This method may be overridden by the concrete filter.
     * The value of fd that is passed is the ordinal value of the StdIO enum, i.e., 0, 1, 2.
     */
    protected int readBytes(int fd, byte[] bytes, int offset, int length, long fileOffset) {
        return 0;
    }

    /* This may be overridden by the concrete filter, if there is necessary close action.
     */
    protected int close0(int key) {
        return 0;
    }


}
