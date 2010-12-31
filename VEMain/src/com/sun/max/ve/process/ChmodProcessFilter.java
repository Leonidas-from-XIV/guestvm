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
package com.sun.max.ve.process;

import com.sun.max.ve.fs.*;

/**
 * A filter for for chmod for internal file systems.
 *
 * @author Mick Jordan
 *
 */

public class ChmodProcessFilter extends VEProcessFilter {

    public ChmodProcessFilter() {
        super("chmod");
    }

    @Override
    public int exec(byte[] prog, byte[] argBlock, int argc, byte[] envBlock, int envc, byte[] dir) {
        assert argc == 2;
        final String[] args = cmdArgs(argBlock);
        /* this is a very partial implementation, just support "chmod octal-mode file" */
        final int mode = Integer.parseInt(args[0], 8);
        final String path = args[1];
        final VirtualFileSystem vfs = FSTable.exports(path);
        if (vfs == null) {
            return -1;
        } else {
            vfs.setMode(path, mode);
        }
        return nextId();
    }

}
