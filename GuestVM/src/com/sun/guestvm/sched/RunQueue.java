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
package com.sun.guestvm.sched;

/**
 * Basic run queue support for scheduler (but implementation independent for testing).
 * Used by the Java scheduler to organize the runnable threads.
 * The choice of data structure is left to the concrete subclass
 * Access to the queue must be protected by the spin lock associated with the queue.
 * Methods with name lockedXXX acquire and release the lock internally, otherwise
 * it is the callers responsibility to ensure the lock is held (useful for multiple operations).
 *
 * Run queues are typically allocated during image build in the Maxine boot heap,
 * but may require additional initialization at run-time (e.g. native spin lock creation).
 *
 * Therefore, separate initialization methods are provided for use at image build time
 * and runtime.
 *
 * @author Mick Jordan
 * @author Harald Roeck
 *
 * @param <T> Queue entry type
 */

public abstract class RunQueue<T> extends SpinLockedRunQueue implements Iterable<T> {
    /**
     * Image buildtime initialization for the queue implementation.
     */
    public abstract void buildtimeInitialize();

    /*
     * The caller must hold the lock before invoking the following methods.
     */

    /**
     * Insert a thread at the end of the run queue.
     *
     * @param thread thread to insert
     */
    public abstract void insert(T thread);

    /**
     * Remove a thread from the run queue.
     *
     * @param thread thread to remove
     */
    public abstract void remove(T thread);

    /**
     * Return the head of this queue.
     *
     * @return the first thread in the queue
     */
    public abstract T head();

    /**
     * Return the second element of the queue.
     *
     * @return second element of the queue or null if none
     */
    public abstract T next();

    /**
     * Move head to end of queue.
     */
    public abstract void moveHeadToEnd();

    /**
     * Size (length) of the queue.
     */
    public abstract int size();

    /** Is the queue empty?
     * @return true if queue is empty
     */
    public abstract boolean empty();

    /*
     * These variants internally lock/unlock the queue. Therefore, the lock must not already be held.
     */

    /**
     * Insert a thread at the end of the run queue.
     *
     * @param thread
     *                thread to insert
     */
    public void lockedInsert(T thread) {
        try {
            lock();
            insert(thread);
        } finally {
            unlock();
        }
    }

    /**
     * Remove a thread from the run queue. until it invokes the scheduler.
     *
     * @param thread
     *                thread to remove
     */
    public void lockedRemove(T thread) {
        try {
            lock();
            remove(thread);
        } finally {
            unlock();
        }
    }

    /**
     * Return the head of this queue, i.e the thread currently running or next to run (if a ukernel thread is currently
     * executing).
     *
     * @return the first thread in the queue
     */
    public T lockedHead() {
        try {
            lock();
            return head();
        } finally {
            unlock();
        }
    }

    /**
     * Return the second element of the queue, i.e. the thread to run next.
     *
     * @return second element of the queue or null if none
     */
    public T lockedNext() {
        try {
            lock();
            return next();
        } finally {
            unlock();
        }

    }

    /**
     * Move head to end of queue.
     */
    public void lockedMoveHeadToEnd() {
        try {
            lock();
            moveHeadToEnd();
        } finally {
            unlock();
        }
    }

    /**
     * Size (length) of the queue.
     */
    public int lockedSize() {
        try {
            lock();
            return size();
        } finally {
            unlock();
        }
    }

    /**
     * Is the queue empty?
     *
     * @return true if queue is empty
     */
    public boolean lockedEmpty() {
        try {
            lock();
            return empty();
        } finally {
            unlock();
        }
    }


}
