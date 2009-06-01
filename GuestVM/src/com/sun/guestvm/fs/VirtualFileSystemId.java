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
import com.sun.max.annotate.*;

/**
 * Provides a way to map from VirtualFileSystem instances to small integers. This is because
 * the upper layers, e.g. FileInputStream traffic in file descriptors, aka small integers, and there
 * needs to be a way to get from file descriptors to the corresponding VirtualFileSystem instance.
 * This avoids having to ensure that file descriptors returned by VirtualFileSystem instances are
 * globally unique, since this can be achieved by merging a file descriptor and a VirtualFileSystem
 * instance id.
 *
 * The file descriptors 0, 1 and 2 are predefined globally; therefore the first entry in the table
 * must be the file system that supports these standard descriptors.
 *
 * @author Mick Jordan
 *
 */

public final class VirtualFileSystemId {

    private static VirtualFileSystem[] _fsTable = new VirtualFileSystem[16];
    private static int _nextFreeIndex = 0;

    private static int getVfsId(VirtualFileSystem fs) {
        for (int i = 0; i < _nextFreeIndex; i++) {
            if (_fsTable[i] == fs) {
                return i;
            }
        }
        _fsTable[_nextFreeIndex++] = fs;
        return _nextFreeIndex - 1;
    }

    public static int getUniqueFd(VirtualFileSystem fs, int fd) {
        return (getVfsId(fs) << 16) | fd;
    }

    public static VirtualFileSystem getVfs(int uniqueFd)  throws IOException {
        if (uniqueFd < 0) {
            throw new IOException(ErrorDecoder.Code.EBADF.getMessage());
        }
        return _fsTable[uniqueFd >> 16];
    }

    @INLINE
    public static VirtualFileSystem getVfsUnchecked(int uniqueFd) {
        return _fsTable[uniqueFd >> 16];
    }

    @INLINE
    public static int getFd(int uniqueFd) {
        return uniqueFd & 0xFFFF;
    }

    @INLINE
    public static int getVfsId(int uniqueFd) {
        return uniqueFd >> 16;
    }

    private VirtualFileSystemId() {
    }


}
