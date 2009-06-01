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
package com.sun.guestvm.monitor;


import com.sun.max.vm.*;
import com.sun.max.vm.thread.*;
import com.sun.max.vm.monitor.modal.sync.*;
import com.sun.max.vm.reference.*;

import com.sun.guestvm.guk.*;
import com.sun.guestvm.sched.*;
import com.sun.guestvm.spinlock.*;

/**
 * GuestVM implementation of a Mutex, in Java, using spin locks.
 * This class is optionally used in GuestVM over Maxine's SpinLockMutex
 * implementation by setting the max.mutex.factory.class=SpinLockMutexFactory
 *
 * This code will inter-operate with the Java version of the thread scheduler because it
 * changes thread state by calling the SchedThread methods (which simply forward to the
 * uKernel scheduler if Java scheduling is not active).
 *
 * @author Mick Jordan
 * @author Harald Roeck
 */
public final class JavaMutex extends Mutex {

    private int _rcount;
    private GUKVmThread _holder;
    private SpinLock _spinlock;
    private static Scheduler _scheduler;
    /*
     * A list of threads that are waiting on this monitor.
     */
    private WaitList _waiters;

    static void initialize() {
        assert MaxineVM.hostOrTarget().phase() == MaxineVM.Phase.PRIMORDIAL;
        GUKScheduler.initialize(MaxineVM.Phase.PRIMORDIAL);
        _scheduler = SchedulerFactory.scheduler();
    }

    public JavaMutex() {
        _spinlock = SpinLockFactory.create();
        _rcount = 0;
        _waiters = new WaitList();
    }

    public Mutex init() {
        _spinlock.initialize();
        return this;
    }

    public void cleanup() {
        _spinlock.cleanup();
    }

    public boolean lock() {
        final GUKVmThread current = (GUKVmThread) VmThread.current();

        if (_holder == current) {
            ++_rcount;
        } else {
            _spinlock.lock();
            current.setMutexWait(true); // for debugging
            while (_holder != null) {
                _waiters.put(current);
                current.setSchedulable(false);
                _spinlock.unlock();
                _scheduler.schedule();
                _spinlock.lock();
            }
            _holder = current;
            _rcount = 1;
            current.setMutexWait(false);
            _spinlock.unlock();
        }
        return true;
    }

    public boolean unlock() {
        final GUKVmThread current = (GUKVmThread) VmThread.current();
        assert current == _holder;

        if (--_rcount == 0) {
            _spinlock.lock();
            _holder = null;
            final GUKVmThread next = _waiters.get();
            _spinlock.unlock();
            if (next != null) {
                next.setSchedulable(true);
                _scheduler.schedule();
            }
        }
        return true;
    }

    boolean lock(int rcount) {
        final boolean retval = lock();
        _rcount = rcount;
        return retval;
    }

    int unlockAll() {
        final int rcount = _rcount;
        _rcount = 1;
        unlock();
        return rcount;
    }

    public long logId() {
        return Reference.fromJava(_spinlock).toOrigin().toLong();
    }

}
