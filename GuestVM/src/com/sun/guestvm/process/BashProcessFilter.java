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

import com.sun.guestvm.fs.*;
/**
 * A filter for bash executions. (Mostly for Hadoop).
 *
 * @author Mick Jordan
 *
 */
public class BashProcessFilter extends GuestVMProcessFilter {

    public BashProcessFilter() {
        super("bash");
    }

    @Override
    public int exec(byte[] prog, byte[] argBlock, int argc, byte[] envBlock, int envc, byte[] dir) {
        final String[] args = cmdArgs(argBlock);
        int result = -1;
        assert args[0].equals("-c");
        if (args[1].equals("groups")) {
            result = nextId();
            final String groups = System.getProperty("guestvm.groups");
            if (groups != null) {
                setData(result, StdIO.OUT, groups.getBytes());
            }
        } else if (args[1].startsWith("exec")) {
            final String[] execArgs = args[1].split(" ");
            if (stripQuotes(execArgs[1]).equals("df")) {
                final String arg2 = stripQuotes(execArgs[2]);
                String path;
                boolean onek = true;
                if (arg2.equals("-k")) {
                    path = stripQuotes(execArgs[3]);
                } else {
                    path = arg2;
                }
                final VirtualFileSystem vfs = FSTable.exports(path);
                if (vfs != null) {
                    long used = vfs.getSpace(path, VirtualFileSystem.SPACE_USED);
                    long total = vfs.getSpace(path, VirtualFileSystem.SPACE_TOTAL);
                    long avail = vfs.getSpace(path, VirtualFileSystem.SPACE_USABLE);
                    if (onek) {
                        used = used / 1024;
                        total = total / 1024;
                        avail = avail / 1024;
                    }
                    final StringBuilder data = new StringBuilder("Filesystem           1K-blocks      Used Available Use% Mounted on\n");
                    data.append(path); data.append(' ');
                    data.append(total); data.append(' ');
                    data.append(used); data.append(' ');
                    data.append(avail); data.append(' ');
                    data.append(used / total * 100); data.append(' ');
                    data.append(FSTable.getInfo(vfs).mountPath()); data.append('\n');
                    result = nextId();
                    setData(result, StdIO.OUT, data.toString().getBytes());
                }

            }
        }
        return result;
    }

    private static String stripQuotes(String s) {
        final int length = s.length();
        if (s.charAt(0) == '\'' && s.charAt(length - 1) == '\'') {
            return s.substring(1, length - 1);
        }
        return s;
    }

}
