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
package com.sun.max.ve.fs.exec;

import com.sun.max.ve.fs.VirtualFileSystem;
import com.sun.max.ve.guk.GUKExec;

/**
 * This is not really a file system but it supports the ability to communicate
 * with the fork/exec backend using file descriptors.
 *
 * @author Mick Jordan
 *
 */

public class ExecFileSystem extends ExecHelperFileSystem implements VirtualFileSystem {

    protected static ExecFileSystem _singleton;

    public static ExecFileSystem create() {
        if (_singleton == null) {
            _singleton = new ExecFileSystem();
        }
        return (ExecFileSystem) _singleton;
    }

    @Override
    public int available(int fd, long fileOffset) {
        // TODO implement
        return 0;
    }


    @Override
    public int close0(int fd) {
        return GUKExec.close(fd);
    }

    @Override
    public int readBytes(int fd, byte[] bytes, int offset, int length, long fileOffset) {
        return GUKExec.readBytes(fd, bytes, offset, length, fileOffset);
    }

    @Override
    public int writeBytes(int fd, byte[] bytes, int offset, int length, long fileOffset) {
        return GUKExec.writeBytes(fd, bytes, offset, length, fileOffset);
    }
}
