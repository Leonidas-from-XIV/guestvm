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
package com.sun.guestvm.guk;

import com.sun.max.unsafe.*;
import com.sun.max.memory.Memory;

/**
 * An interface to creating separate processes (guests) via dom0.
 *
 * @author Mick Jordan
 *
 */

public class GUKExec {

    public static int forkAndExec(byte[] prog, byte[] argBlock, int argc, byte[] dir) {
        final Pointer nativeProg = Memory.allocate(Size.fromInt(prog.length));
        Memory.writeBytes(prog, nativeProg);
        final Pointer nativeArgs = Memory.allocate(Size.fromInt(argBlock.length));
        Memory.writeBytes(argBlock, nativeArgs);
        Pointer nativeDir = Pointer.zero();
        if (dir != null) {
            nativeDir = Memory.allocate(Size.fromInt(dir.length));
            Memory.writeBytes(dir, nativeDir);
        }
        final int rc = GUK.guk_exec_create(nativeProg, nativeArgs, argc, nativeDir);
        Memory.deallocate(nativeProg);
        Memory.deallocate(nativeArgs);
        if (dir != null) {
            Memory.deallocate(nativeDir);
        }
        return rc;
    }

    public static int waitForProcessExit(int pid) {
        return GUK.guk_exec_wait(pid);
    }

    public static int readBytes(int pid, byte[] bytes, int offset, int length, long fileOffset) {
        final Pointer nativeBytes = Memory.allocate(Size.fromInt(bytes.length));
        final int result = GUK.guk_exec_read_bytes(pid, nativeBytes, length, fileOffset);
        if (result > 0) {
            Memory.readBytes(nativeBytes, result, bytes, offset);
        }
        Memory.deallocate(nativeBytes);
        return result;
    }

    public static int close(int pid) {
        return GUK.guk_exec_close(pid);
    }

    public static int destroyProcess(int pid) {
        return GUK.guk_exec_destroy(pid);
    }
}
