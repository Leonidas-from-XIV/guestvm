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

/**
 * An instance of a class that implements this interface can be registered with Guest VM to intercept @see java.lang.Process calls and implement them internally.
 *
 * @author Mick Jordan
 *
 */

public interface GuestVMProcessFilter {
    /**
     * Returns the names of the processes that this filter handles.
     * @return process to filter
     */
    String[] names();

    /**
     * Execute the process that this filter handles with the given arguments.
     * The return value is either negative, indicating a failure to exec or a
     * positive value that will be passed to the @see FilterFileSystem when
     * creating the file descriptors for stdin, stdout and stderr.
     * @param prog
     * @param argBlock
     * @param argc
     * @param envBlock
     * @param envc
     * @param dir
     * @param stderrFd
     * @return
     */
    int exec(byte[] prog, byte[] argBlock, int argc, byte[] envBlock, int envc, byte[] dir);
}
