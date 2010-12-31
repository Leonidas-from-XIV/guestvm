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
package com.sun.max.ve.util;

import com.sun.max.ve.fs.ErrorDecoder;
import com.sun.max.vm.Log;

/**
 * A class that handles timeouts while running a given procedure.
 *
 * @author Mick Jordan
 *
 */

public abstract class TimeLimitedProc {
    private boolean _return;

    /**
     * This is the method that does the work.
     * It must set _return = true to terminate the run method.
     * @param remaining the amount of time left (or 0 if infinite)
     * @return result of the method
     * @throws InterruptedException
     */
    protected abstract int proc(long remaining) throws InterruptedException;

    /**
     * Run proc until timeout expires.
     * @param timeout if < 0 implies infinite, otherwise given value
     * @return
     */
    public int run(long timeout) {
        final long start = System.currentTimeMillis();
        long remaining = timeout < 0 ? 0 : timeout;
        _return = false;
        while (true) {
            try {
                final int result = proc(remaining);
                if (_return) {
                    log("run " + this + " returning " + result);
                    return result;
                }
                // timeout expired?
                if (timeout > 0) {
                    final long now = System.currentTimeMillis();
                    if (now - start >= timeout) {
                        log("run " + this + " timed out");
                        return 0;
                    }
                    remaining -= now - start;
                }
            } catch (InterruptedException ex) {
                log("run " + this + " interrupted");
                return -ErrorDecoder.Code.EINTR.getCode();
            }
        }
    }

    /**
     * The way to terminate the run method.
     * @param result
     * @return
     */
    public int terminate(int result) {
        _return = true;
        return result;
    }

    static boolean _init;
    static boolean _log;
    static void log(String s) {
        if (!_init) {
            _log = System.getProperty("max.ve.util.tlp.debug") != null;
            _init = true;
        }
        if (_log) {
            Log.print(Thread.currentThread()); Log.print(' '); Log.println(s);
        }
    }
}

