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
package com.sun.guestvm.jdk;

import com.sun.max.annotate.*;
import java.io.*;

/**
  *Substitutions for @see java.lang.UNIXProcess.
  * @author Mick Jordan
  */

@SuppressWarnings("unused")

@METHOD_SUBSTITUTIONS(hiddenClass = "java.lang.UNIXProcess")
final class JDK_java_lang_UNIXProcess {

    @SUBSTITUTE
    private int waitForProcessExit(int pid) {
        // Nothing to wait for
        return 0;
    }

    @SUBSTITUTE
    private int forkAndExec(byte[] prog, byte[] argBlock, int argc, byte[] envBlock, int envc, byte[] dir, boolean redirectErrorStream, FileDescriptor stdinFd, FileDescriptor stdoutFd,
                    FileDescriptor stderrFd) throws IOException {
        System.out.print("WARNING: application is trying to start a subprocess: ");
        if (dir != null) {
            System.out.print("in directory: ");
            bytePrint(dir, '\n');
        }
        bytePrint(prog, ' ');
        bytePrintBlock(argBlock, '\n');
        return 0;
    }

    /**
     * Output a null-terminated byte array (@See java.lang.ProcessImpl).
     * @param data
     */
    private static void bytePrint(byte[] data, char term) {
        for (int i = 0; i < data.length - 1; i++) {
            final byte b = data[i];
            if (b == 0) {
                break;
            }
            System.out.write(b);
        }
        System.out.write(term);
    }

    /**
     * Output a null-separated, null-terminated byte array (@See java.lang.ProcessImpl).
     * @param data
     */
    private static void bytePrintBlock(byte[] data, char term) {
        for (int i = 0; i < data.length - 1; i++) {
            final byte b = data[i];
            if (b == 0) {
                System.out.write(' ');
            } else {
                System.out.write(b);
            }
        }
        System.out.write(term);
    }

    @SUBSTITUTE
    private static void destroyProcess(int pid) {
        System.out.println("WARNING: application is trying to destroy process: " + pid);
    }

    @SUBSTITUTE
    private static void initIDs() {

    }
}
