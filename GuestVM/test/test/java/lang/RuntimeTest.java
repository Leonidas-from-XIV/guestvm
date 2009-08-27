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
package test.java.lang;

import java.io.*;
import java.util.*;

public class RuntimeTest {

    private static boolean _reflectImmediate = true;
    private static List<String> _stdOutLines = new ArrayList<String>();
    private static List<String> _stdErrLines = new ArrayList<String>();
    private static StringBuffer _stdOutBuffer = new StringBuffer();
    private static StringBuffer _stdErrBuffer = new StringBuffer();
    /**
     * @param args
     */
    public static void main(String[] args) {
        final Runtime runtime = Runtime.getRuntime();
        File wdir = null;
        boolean lines = true;
        int execCount = 1;
        // Checkstyle: stop modified control variable check
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("-")) {
                arg = arg.substring(1);
            }
            if (arg.equals("ap")) {
                System.out.println("availableProcessors=" + runtime.availableProcessors());
            } else if (arg.equals("quiet")) {
                _reflectImmediate = false;
            } else if (arg.equals("chars")) {
                lines = false;
            } else if (arg.equals("wdir")) {
                wdir = new File(args[++i]);
            } else if (arg.equals("ec")) {
                execCount = Integer.parseInt(args[++i]);
            } else if (arg.equals("exec")) {
                List<String> execArgsList = new ArrayList<String>();
                i++;
                while (i < args.length) {
                    final String execArg = args[i++];
                    if (arg.startsWith("-")) {
                        break;
                    }
                    execArgsList.add(execArg);
                }
                final String[] execArgs = new String[execArgsList.size()];
                execArgsList.toArray(execArgs);
                for (int e = 0; e < execCount; e++) {
                    Process p = null;
                    BufferedReader stdOut = null;
                    BufferedReader stdErr = null;
                    try {
                        p = runtime.exec(execArgs, null, wdir);
                        stdOut = new BufferedReader(new InputStreamReader(p.getInputStream()));
                        readFully(stdOut, true, lines);
                        stdErr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                        readFully(stdOut, true, lines);
                        System.out.println("waitFor returned " + p.waitFor());
                        if (!_reflectImmediate) {
                            delayedOutput(true, lines);
                            delayedOutput(false, lines);
                        }
                    } catch (IOException ex) {
                        System.err.println(ex);
                    } catch (InterruptedException ex) {
                        System.err.println(ex);
                    } finally {
                        if (p != null) {
                            try {
                                stdOut.close();
                                stdErr.close();
                                p.destroy();
                            } catch (IOException ex) {

                            }
                        }
                    }
                }
            }
        }
        // Checkstyle: resume modified control variable check

    }

    private static void readFully(BufferedReader in, boolean isStdOut, boolean lines) throws IOException {
        if (_reflectImmediate) {
            System.out.println(isStdOut ? "stdout: " : "stderr: ");
        }
        if (lines) {
            readFullyLines(in, isStdOut);
        } else {
            readFullyChars(in, isStdOut);
        }
    }

    private static void readFullyLines(BufferedReader in, boolean isStdOut) throws IOException {
        while (true) {
            final String line = in.readLine();
            if (line == null) {
                break;
            }
            if (_reflectImmediate) {
                System.out.println(line);
            } else {
                if (isStdOut) {
                    _stdOutLines.add(line);
                } else {
                    _stdErrLines.add(line);
                }
            }
        }
    }

    private static void readFullyChars(BufferedReader in, boolean isStdOut) throws IOException {
        char[] buf = new char[512];
        int nRead;
        while ((nRead = in.read(buf, 0, buf.length)) > 0) {
            if (isStdOut) {
                _stdOutBuffer.append(buf, 0, nRead);
            } else {
                _stdErrBuffer.append(buf, 0, nRead);
            }
            if (_reflectImmediate) {
                for (int i = 0; i < nRead; i++) {
                    System.out.print(buf[i]);
                }
            }
        }
    }

    private static void delayedOutput(boolean isStdOut, boolean lines) {
        System.out.println(isStdOut ? "stdout: " : "stderr: ");
        if (lines) {
            delayedOutputLines(isStdOut);
        } else {
            delayedOutputChars(isStdOut);
        }
    }

    private static void delayedOutputLines(boolean isStdOut) {
        List<String> lines = isStdOut ? _stdOutLines : _stdErrLines;
        for (String line : lines) {
            System.out.println(line);
        }
    }

    private static void delayedOutputChars(boolean isStdOut) {
        StringBuffer buf = isStdOut ? _stdOutBuffer : _stdErrBuffer;
            System.out.println(buf);
    }
}
