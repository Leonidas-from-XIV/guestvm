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

import com.sun.guestvm.fs.*;
import com.sun.guestvm.process.*;
/**
 * Not really a file system. Similar to ExecFileSystem, supports the ability of Process filters to return values through file descriptors.
 *
 * @author Mick Jordan
 *
 */

public class FilterFileSystem extends ExecHelperFileSystem implements VirtualFileSystem {

    protected static FilterFileSystem _singleton;

    public static FilterFileSystem create() {
        if (_singleton == null) {
            _singleton = new FilterFileSystem();
        }
        return (FilterFileSystem) _singleton;
    }

    @Override
    public int available(int fd, long fileOffset) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int close0(int fd) {
        return ProcessFilterHelper.invokeClose0(fd);
    }

    @Override
    public int readBytes(int fd, byte[] bytes, int offset, int length, long fileOffset) {
        return ProcessFilterHelper.invokeReadBytes(fd, bytes, offset, length, fileOffset);
    }
}
