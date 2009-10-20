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

import com.sun.guestvm.error.GuestVMError;
import com.sun.guestvm.guk.GUKExec;
import com.sun.guestvm.jdk.*;

/**
 * This is a filter for handling Java processes. It is a little unusual as it still ends up doing a remote exec,
 * but it needs to alter the arguments to reflect the GuestVM environment.
 *
 * @author Mick Jordan
 *
 */

public class JavaProcessFilter extends GuestVMProcessFilter {

    private static String _guestvmDir;
    private static final byte[] _userDir = "-Duser.dir=".getBytes();
    private static final byte[] _java = "/bin/java".getBytes();
    private String[] _names = new String[2];

    public JavaProcessFilter() {
        _guestvmDir = System.getProperty("guestvm.dir");
        if (_guestvmDir == null) {
            GuestVMError.unexpected("guestvm.dir property is not set");
        }
        _names[0] = "java";
        _names[1] = System.getProperty("java.home") + _java;
    }

    @Override
    public String[] names() {
        return _names;
    }

    @Override
    public int exec(byte[] prog, byte[] argBlock, int argc, byte[] envBlock, int envc, byte[] dir) {
        final String[] args = cmdArgs(argBlock);
        /* Ideally applications would be configurable for running a Guest (i.e. Maxine) VM.
         * If not we have to remove, e.g., Hotspot-specific arguments.
         * We also have to careful about the working directory. We want to run the Guest VM "java" command
         * from the same directory that this Guest VM instance was launched, but the app has likely
         * set wdir to some directory in the Guest VM file system environment. So we have to set
         * the user.dir property to that value.
         */
        int newLength = args.length;
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.equals("-XX:+UnlockDiagnosticVMOptions") ||
                            arg.equals("-XX:NewRatio=2") ||
                            arg.equals("-XX:+LogVMOutput") ||
                            arg.equals("-client")) {
                args[i] = null;
                newLength--;
            }
        }
        byte[] userDir = null;
        if (dir != null) {
            userDir = concatBytes(_userDir, dir);
        }
        final byte[] newArgBlock = toBytes(args, newLength, userDir);
        final byte[] newProg = concatBytes(_guestvmDir.getBytes(), _java);
        JDK_java_lang_UNIXProcess.logExec(newProg, newArgBlock, _guestvmDir.getBytes());
        return GUKExec.forkAndExec(newProg, newArgBlock, argc, _guestvmDir.getBytes());
    }

    private static byte[] concatBytes(byte[] a, byte[] b) {
        final byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;

    }

    private static byte[] toBytes(String[] cmdArray, int newLength, byte[] userDir) {
        final byte[][] args = new byte[newLength][];
        int size = args.length; // For added NUL bytes
        int j = 0;
        for (int i = 0; i < cmdArray.length; i++) {
            if (cmdArray[i] != null) {
                args[j] = cmdArray[i].getBytes();
                size += args[j].length;
                j++;
            }
        }
        final byte[] argBlock = new byte[size + (userDir != null ? userDir.length + 1 : 0)];
        int i = 0;
        for (byte[] arg : args) {
            System.arraycopy(arg, 0, argBlock, i, arg.length);
            i += arg.length + 1;
            // No need to write NUL bytes explicitly
        }
        if (userDir != null) {
            System.arraycopy(userDir, 0, argBlock, i, userDir.length);
        }
        return argBlock;
    }

}
