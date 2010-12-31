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
package com.sun.max.ve.monitor;

import com.sun.max.unsafe.*;
import com.sun.max.ve.guk.*;
import com.sun.max.ve.sched.*;
import com.sun.max.ve.spinlock.*;
import com.sun.max.vm.*;
import com.sun.max.vm.thread.*;
import com.sun.max.vm.monitor.modal.sync.*;
import com.sun.max.vm.reference.*;

/**
 * MaxVE implementation of a ConditionVariable, in Java.
 * The comments in Mutex.java apply equally to this class.
 *
 * @author Mick Jordan
 * @author Harald Roeck
 */
public final class JavaConditionVariable extends ConditionVariable {

    private static Scheduler _scheduler;
    private SpinLock _spinlock;
    /*
     * A list of  threads that are waiting on this condition.
     */
    private WaitList _waiters;

    static void initialize() {
        assert MaxineVM.isPrimordial();
        GUKScheduler.initialize(MaxineVM.Phase.PRIMORDIAL);
        _scheduler = SchedulerFactory.scheduler();
    }

    public JavaConditionVariable() {
        _spinlock = SpinLockFactory.create();
        _waiters = new WaitList();
    }

    public ConditionVariable init() {
        _spinlock.initialize();
        return this;
    }

    public boolean threadWait(Mutex mutex, long timeoutMilliSeconds) {
        final JavaMutex spinLockMutex = (JavaMutex) mutex;
        Pointer timer = Pointer.zero();
        final GUKVmThread current = (GUKVmThread) VmThread.current();

        if (timeoutMilliSeconds > 0) {
            timer = GUKScheduler.createTimer();
            GUKScheduler.addTimer(timer, timeoutMilliSeconds);
        }

        _spinlock.lock();
        current.setNotified(false);
        current.setSchedulable(false);
        _waiters.put(current);
        _spinlock.unlock();

        current.setConditionWait(true); // for debugging
        final int rcount = spinLockMutex.unlockAll();

        _scheduler.schedule();

        boolean result = true;
        if (timeoutMilliSeconds > 0) {
            GUKScheduler.removeTimer(timer);
            GUKScheduler.deleteTimer(timer);
        }

        _spinlock.lock();
        current.setConditionWait(false);

        if (current.isNotified()) {
            current.setNotified(false);
        } else { /* on timeout or interrupt remove thread from wait list */
            _waiters.remove(current);
        }
        if (current.isOSInterrupted()) {
            result = false;
            current.clearOSInterrupted();
        }
        _spinlock.unlock();
        /* get the lock before return, and restore recursion count */
        spinLockMutex.lock(rcount);

        return result;
    }

    public boolean threadNotify(boolean all) {
        boolean sched = false;
        GUKVmThread next;
        _spinlock.lock();
        next = _waiters.get();
        while (next != null) {
            next.setNotified(true);
            next.setSchedulable(true);
            sched = true;
            if (!all) {
                break;
            }
            next = _waiters.get();
        }
        _spinlock.unlock();
        if (sched) {
            _scheduler.schedule();
        }
        return false;
    }

    public long logId() {
        return Reference.fromJava(_spinlock).toOrigin().toLong();
    }


}
