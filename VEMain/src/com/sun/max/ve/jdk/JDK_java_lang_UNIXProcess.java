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
package com.sun.max.ve.jdk;

import com.sun.max.annotate.*;
import com.sun.max.ve.fs.exec.*;
import com.sun.max.ve.guk.GUKExec;
import com.sun.max.ve.logging.*;
import com.sun.max.ve.process.*;

import java.io.*;
import java.util.logging.Level;

/**
  *Substitutions for @see java.lang.UNIXProcess.
  * @author Mick Jordan
  */

@SuppressWarnings("unused")

@METHOD_SUBSTITUTIONS(className = "java.lang.UNIXProcess")
public final class JDK_java_lang_UNIXProcess {
    private static Logger _logger;
    private static ExecFileSystem _execFS;
    private static FilterFileSystem _filterFS;

    private static void init() {
        if (_logger == null) {
            _logger = Logger.getLogger("UNIXProcess");
            _execFS = ExecFileSystem.create();
            _filterFS = FilterFileSystem.create();
        }
    }

    @SUBSTITUTE
    private int waitForProcessExit(int key) {
        final VEProcessFilter filter = VEProcessFilter.getFilter(key);
        if (filter != null) {
            return filter.waitForProcessExit(key);
        } else {
            return GUKExec.waitForProcessExit(key);
        }
    }

    @SUBSTITUTE
    private int forkAndExec(byte[] prog, byte[] argBlock, int argc, byte[] envBlock, int envc, byte[] dir, boolean redirectErrorStream, FileDescriptor stdinFd, FileDescriptor stdoutFd,
                    FileDescriptor stderrFd) throws IOException {
        init();
        ExecHelperFileSystem execFS = _execFS;
        int key;

        final VEProcessFilter filter = VEProcessFilter.filter(prog);
        if (filter != null) {
            key = filter.exec(prog, argBlock, argc, envBlock, envc, dir);
            /* The filter may have elected to have the call (possibly modified) handled externally. */
            if (VEProcessFilter.getFilter(key) != null) {
                execFS = _filterFS;
            }
        } else {
            logExec(prog, argBlock, dir);
            key = GUKExec.forkAndExec(prog, argBlock, argc, dir);
        }
        if (key < 0) {
            throw new IOException("Exec failed");
        } else {
            final int[] fds = execFS.getFds(key);
            JDK_java_io_FileDescriptor.setFd(stdinFd, fds[0]);
            JDK_java_io_FileDescriptor.setFd(stdoutFd, fds[1]);
            JDK_java_io_FileDescriptor.setFd(stderrFd, fds[2]);
        }
        return key;
    }

    public static void logExec(byte[] prog, byte[] argBlock, byte[] dir) {
        if (_logger.isLoggable(Level.WARNING)) {
            final StringBuilder sb = new StringBuilder("application is trying to start a subprocess: ");
            if (dir != null) {
                sb.append("in directory: ");
                bytePrint(sb, dir, '\n');
            }
            bytePrint(sb, prog, ' ');
            bytePrintBlock(sb, argBlock, '\n');
            _logger.warning(sb.toString());
        }
    }

    public static Logger getLogger() {
        return _logger;
    }

    /**
     * Output a null-terminated byte array (@See java.lang.ProcessImpl).
     * @param data
     */
    private static void bytePrint(StringBuilder sb, byte[] data, char term) {
        for (int i = 0; i < data.length; i++) {
            final byte b = data[i];
            if (b == 0) {
                break;
            }
            sb.append((char) b);
        }
        sb.append(term);
    }

    /**
     * Output a null-separated, null-terminated byte array (@See java.lang.ProcessImpl).
     * @param data
     */
    private static void bytePrintBlock(StringBuilder sb, byte[] data, char term) {
        for (int i = 0; i < data.length; i++) {
            final byte b = data[i];
            if (b == 0) {
                sb.append(' ');
            } else {
                sb.append((char) b);
            }
        }
        sb.append(term);
    }

    @SUBSTITUTE
    private static void destroyProcess(int key) {
        init();
        final VEProcessFilter filter = VEProcessFilter.getFilter(key);
        if (filter != null) {
            filter.destroyProcess(key);
            return;
        }
        if (_logger.isLoggable(Level.WARNING)) {
            _logger.warning("application is trying to destroy process: " + key);
        }
        GUKExec.destroyProcess(key);
    }

    @SUBSTITUTE
    private static void initIDs() {

    }
}
