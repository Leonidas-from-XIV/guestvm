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

/**
 * A class to assists in the implementation of a filter for single process.
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
    protected int nextId() {
        final int result = _nextId;
        _nextId += 3;
        return result;
    }

    protected int close0(int key) {
        return 0;
    }

    protected static int keyToFd(int key) {
        return key % 3;
    }

    protected static int keyToExec(int key) {
        return (key / 3) * 3;
    }

    public static int invokeClose0(int key) {
        return _fdMap.get(key).close0(key);
    }

    protected abstract  int readBytes(int key, byte[] bytes, int offset, int length, long fileOffset);

    public static int invokeReadBytes(int key, byte[] bytes, int offset, int length, long fileOffset) {
        if (keyToFd(key) == StdIO.IN.ordinal()) {
            return -ErrorDecoder.Code.EBADF.getCode();
        }
        return _fdMap.get(key).readBytes(key, bytes, offset, length, fileOffset);
    }

}
