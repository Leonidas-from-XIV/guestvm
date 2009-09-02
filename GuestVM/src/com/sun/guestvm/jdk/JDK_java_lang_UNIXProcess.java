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

import com.sun.guestvm.fs.exec.*;
import com.sun.guestvm.guk.GUKExec;
import com.sun.guestvm.process.*;
import com.sun.max.annotate.*;
import com.sun.max.vm.object.TupleAccess;
import com.sun.guestvm.logging.*;

import java.io.*;
import java.util.logging.Level;

/**
  *Substitutions for @see java.lang.UNIXProcess.
  * @author Mick Jordan
  */

@SuppressWarnings("unused")

@METHOD_SUBSTITUTIONS(hiddenClass = "java.lang.UNIXProcess")
final class JDK_java_lang_UNIXProcess {
    private static Logger _logger;
    private static ExecFileSystem _execFS;
    private static FilterFileSystem _filterFS;
    private static final int FILTER_BIT = 0x40000000;

    private static void init() {
        if (_logger == null) {
            _logger = Logger.getLogger(Process.class.getName());
            _execFS = ExecFileSystem.create();
            _filterFS = FilterFileSystem.create();
        }
    }

    private static boolean isFilter(int key) {
        return (key & FILTER_BIT) != 0;
    }

    @SUBSTITUTE
    private int waitForProcessExit(int key) {
        if (isFilter(key)) {
            return 0;
        } else {
            return GUKExec.waitForProcessExit(key);
        }
    }

    @SUBSTITUTE
    private int forkAndExec(byte[] prog, byte[] argBlock, int argc, byte[] envBlock, int envc, byte[] dir, boolean redirectErrorStream, FileDescriptor stdinFd, FileDescriptor stdoutFd,
                    FileDescriptor stderrFd) throws IOException {
        int key;
        int rkey;
        ExecHelperFileSystem execFS;
        init();
        final GuestVMProcessFilter filter = GuestVMProcess.filter(prog);
        if (filter != null) {
            key = filter.exec(prog, argBlock, argc, envBlock, envc, dir);
            rkey = key | FILTER_BIT;
            execFS = _filterFS;
        } else {
            if (_logger.isLoggable(Level.WARNING)) {
                System.out.print("WARNING: application is trying to start a subprocess: ");
                if (dir != null) {
                    System.out.print("in directory: ");
                    bytePrint(dir, '\n');
                }
                bytePrint(prog, ' ');
                bytePrintBlock(argBlock, '\n');
            }
            key = GUKExec.forkAndExec(prog, argBlock, argc, dir);
            rkey = key;
            execFS = _execFS;
        }
        if (key < 0) {
            throw new IOException("Exec failed");
        } else {
            final int[] fds = execFS.getFds(key);
            TupleAccess.writeInt(stdinFd, JDK_java_io_fdActor.fdFieldActor().offset(), fds[0]);
            TupleAccess.writeInt(stdoutFd, JDK_java_io_fdActor.fdFieldActor().offset(), fds[1]);
            TupleAccess.writeInt(stderrFd, JDK_java_io_fdActor.fdFieldActor().offset(), fds[2]);
            if (isFilter(rkey)) {
                filter.registerFds(fds);
            }
        }
        /* The value we return encodes whether or not the exec is a filter, which is (only) used by waitForProcess/destroyProcess */
        return rkey;
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
        init();
        if (_logger.isLoggable(Level.WARNING)) {
            System.out.println("WARNING: application is trying to destroy process: " + pid);
        }
        GUKExec.destroyProcess(pid);
    }

    @SUBSTITUTE
    private static void initIDs() {

    }
}
