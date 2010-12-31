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

import com.sun.max.annotate.INLINE;
import com.sun.max.ve.spinlock.*;
import com.sun.max.ve.spinlock.guk.DIGUKSpinLockFactory;

/**
 * This class provides the locking on the Scheduler run queues.
 * Synchronization on run queues must be explicit using spinlocks and not
 * Java monitors/mutex that could block.
 *
 * It uses a ukernel spinlock implementation that disables/re-enables interrupts
 * on lock/unlock to prevent an event causing recursive entry into the scheduler.
 *
 * @author Mick Jordan
 *
 */

public class SpinLockedRunQueue {
    private SpinLock _lock;

    protected SpinLockedRunQueue() {
        _lock = DIGUKSpinLockFactory.create();
    }

    /**
     * Runtime Initialization for the queue implementation.
     */
    public void runtimeInitialize() {
        _lock.initialize();
    }

    /**
     * Lock and disable interrupts (events).
     * @return the flags needed to unlock
     */
    @INLINE(override = true)
    public void lock() {
        _lock.lock();
    }

    /**
     * Unlock and enable interrupts (events).
     * @param flags from the matching lock call
     */
    @INLINE(override = true)
    public void unlock() {
        _lock.unlock();
    }

}
