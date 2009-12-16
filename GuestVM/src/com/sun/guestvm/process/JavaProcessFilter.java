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

import java.io.File;
import java.util.*;
import java.util.logging.Level;

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

    private static byte[] _guestvmDirBytes;
    private static final byte[] _userDirBytes = "-Duser.dir=".getBytes();
    private static final String _java = "/bin/java";
    private static final byte[] _javaBytes = _java.getBytes();
    private String[] _names = new String[2];

    public JavaProcessFilter() {
        final String guestvmDir = System.getProperty("guestvm.dir");
        if (guestvmDir == null) {
            GuestVMError.unexpected("guestvm.dir property is not set");
        }
        _guestvmDirBytes = guestvmDir.getBytes();
        _names[0] = "java";
        _names[1] = System.getProperty("java.home") + _java;
    }

    @Override
    public String[] names() {
        return _names;
    }

    @Override
    public int exec(byte[] prog, byte[] argBlock, int argc, byte[] envBlock, int envc, byte[] dir) {
        final String[] args = cmdArgs(argBlock, dir == null ? null : concatBytes(_userDirBytes, dir));
        /* Ideally applications would be configurable for running a Guest (i.e. Maxine) VM.
         * If not we have to remove, e.g., Hotspot-specific arguments.
         * We also have to careful about the working directory. We want to run the Guest VM "java" command
         * from the same directory that this Guest VM instance was launched, but the app has likely
         * set wdir to some directory in the Guest VM file system environment. So we have to set
         * the user.dir property to that value.
         *
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

        //args = checkCompressArgs(args);

        final byte[] newArgBlock = toBytes(args, newLength);
        final byte[] newProg = concatBytes(_guestvmDirBytes, _javaBytes);
        JDK_java_lang_UNIXProcess.logExec(newProg, newArgBlock, _guestvmDirBytes);
        return GUKExec.forkAndExec(newProg, newArgBlock, argc, _guestvmDirBytes);
    }

    private static byte[] concatBytes(byte[] a, byte[] b) {
        final byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;

    }

    private static byte[] toBytes(String[] cmdArray, int newLength) {
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
        final byte[] argBlock = new byte[size];
        int i = 0;
        for (byte[] arg : args) {
            System.arraycopy(arg, 0, argBlock, i, arg.length);
            i += arg.length + 1;
            // No need to write NUL bytes explicitly
        }
        return argBlock;
    }

    /* Code to compress pathnames using shell like variables.
     * It works, but didn't typically achieve enough compression
     * and has been superceded by the ramdisk command line mechanism.
     *
    private static final int ARGSLENGTH_THRESHOLD = 896;  // leave some space for stuff added by java script
    private static String[] checkCompressArgs(String[] args) {
        final int newArgsLength = argsLength(args);
        if (newArgsLength > ARGSLENGTH_THRESHOLD) {
            final String[] cargs = compressArgs(args, newArgsLength - ARGSLENGTH_THRESHOLD);
            if (cargs == null) {
                JDK_java_lang_UNIXProcess.getLogger().log(Level.WARNING, "exec args may be too long, can't compress");
            } else {
                return cargs;
            }
        }
        return args;
    }

    private static String[] compressArgs(String[] args, int reduction) {
        // We are not trying to be general here, just deal with the expected situation,
        // which is many long filenames with repeated prefixes.
        final List<Locator> candidates = new ArrayList<Locator>();
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg != null) {
                locatePathnames(candidates, args, i);
            }
        }
        for (Locator s : candidates) {
            if (s._parts.length > 2) {
                int upb = s._parts.length - 2;
                while (upb > 0) {
                    int pl = 0;
                    for (int i = 0; i <= upb; i++) {
                        pl += s._parts[i].length() + 1;
                    }
                    if (pl > 5) {
                        for (Locator c : candidates) {
                            if (s != c) {
                                if (upb < c._parts.length && match(s._parts, c._parts, upb)) {
                                    s._matches[upb]++;
                                }
                            }
                        }
                        final int thisReduction = (pl - 5) * s._matches[upb];
                        // check if this will do
                        if (thisReduction >= reduction) {
                            final String[] newArgs = new String[args.length + 1];
                            newArgs[0] = "-XX:GVMArgVar:v0=" + concatParts(s._parts, 0, upb, false);
                            int nx = 1;
                            for (int i = 0; i < args.length; i++) {
                                final String arg = args[i];
                                String newArg = arg;
                                if (arg != null) {
                                    int cx = findFirstLocatorForArgIndex(candidates, i);
                                    if (cx >= 0) {
                                        newArg = ""; // reset
                                        int ax = 0;
                                        // This arg may contain several pathnames, zero or more that match our variable choice
                                        while (true) {
                                            final Locator c = candidates.get(cx);
                                            // append text up to start of pathname
                                            newArg += args[i].substring(ax, c._sx);
                                            if (upb < c._parts.length && match(s._parts, c._parts, upb)) {
                                                // append variable occurrence plus parts of pathname not matched
                                                newArg += "${v0}" + concatParts(c._parts, upb + 1, c._parts.length - 1, true);
                                            } else {
                                                // this pathname did not match our variable, so append it unmodified
                                                newArg += concatParts(c._parts, 0, c._parts.length - 1, false);
                                            }
                                            ax = c._ex;
                                            cx++;
                                            if (cx == candidates.size() ||  candidates.get(cx)._argIndex != i) {
                                                // append text after final pathname
                                                newArg += arg.substring(c._ex);
                                                break;
                                            }
                                        }

                                    }
                                }
                                newArgs[nx++] = newArg;
                            }
                            return newArgs;
                        }
                    }
                    upb--;
                }
            }
        }
        return null;
    }

    private static int argsLength(String[] args) {
        int result = 0;
        for (String arg : args) {
            if (args != null) {
                result += arg.length();
            }
        }
        return result;
    }

    private static int findFirstLocatorForArgIndex(List<Locator> list, int argIndex) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i)._argIndex == argIndex) {
                return i;
            }
        }
        return -1;
    }

    private static void locatePathnames(List<Locator> candidates, String[] args, int argIndex) {
        final String arg = args[argIndex];
        final int length = arg.length();
        int sx;
        int ex;
        int i = 0;
        while (i < length) {
            if (arg.charAt(i) == File.separatorChar) {
                int j = i;
                sx = i;
                ex = 0;
                while (j < length) {
                    final char ch = arg.charAt(j);
                    if (ch == ':' || ch == ';') {
                        ex = j;
                        break;
                    }
                    j++;
                }
                if (ex == 0) {
                    ex = length;
                }
                candidates.add(new Locator(argIndex, arg, sx, ex));
                i = j;
            }
            i++;
        }
    }

    private static boolean match(String[] a, String[] b, int upb) {
        for (int x = 0; x <= upb; x++) {
            if (!a[x].equals(b[x])) {
                return false;
            }
        }
        return true;
    }

    private static String concatParts(String[] parts, int lwb, int upb, boolean leadingSep) {
        if (lwb > upb) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        if (leadingSep) {
            sb.append(File.separatorChar);
        }
        for (int i = lwb; i <= upb; i++) {
            sb.append(parts[i]);
            if (i != upb) {
                sb.append(File.separatorChar);
            }
        }
        return sb.toString();
    }

    static class Locator {
        int _argIndex;
        int _sx;
        int _ex;
        String[] _parts;
        int[] _matches;
        Locator(int argIndex, String arg, int sx, int ex) {
            _argIndex = argIndex;
            _sx = sx;
            _ex = ex;
            _parts = arg.substring(sx, ex).split(File.separator);
            _matches = new int[_parts.length];
            for (int i = 0; i < _matches.length; i++) {
                _matches[i] = 0;
            }
        }
    }
*/
}
