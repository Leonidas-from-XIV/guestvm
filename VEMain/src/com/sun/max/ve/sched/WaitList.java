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
package com.sun.max.ve.sched;

/**
 * provides a FIFO list of SchedThreads; no locking in here, users of this class have to do the locking.
 *
 * @author Harald Roeck
 *
 */
public class WaitList {

    private GUKVmThread _first;
    private GUKVmThread _last;

    /**
     * Insert a thread into the list.
     *
     * @param thread the thread to insert
     */
    public void put(GUKVmThread thread) {
        if (_first == null) { /* list is empty */
            // CheckStyle: stop inner assignment check
            _first = _last = thread;
            // CheckStyle: resume inner assignment check
        } else {
            _last.setNextWaiting(thread);
            _last = thread;
        }

    }

    /**
     * Removes and returns the first thread in the list.
     *
     * @return the first thread in the list or null if the list is empty
     */
    public GUKVmThread get() {
        GUKVmThread retval;
        retval = _first;
        if (retval != null) {
            _first = retval.getNextWaiting();
            if (_first == null) {
                _last = null;
            }
            retval.setNextWaiting(null);
        }
        return retval;
    }

    /**
     * Removes a thread from the list.
     *
     * @param thread the thread to remove from the list
     */
    public void remove(GUKVmThread thread) {
        if (_first == null || thread == null) {
            return;
        } else if (thread == _first) {
            get();
        } else {
            GUKVmThread iterator = _first;
            GUKVmThread prev = null;
            while (iterator != null) {
                if (iterator == thread) {
                    break;
                }
                prev = iterator;
                iterator = iterator.getNextWaiting();
            }

            if (iterator == thread) { /* found the thread */
                prev.setNextWaiting(thread.getNextWaiting());
                if (thread == _last) {
                    _last = prev;
                }
            }
        }
    }
}
