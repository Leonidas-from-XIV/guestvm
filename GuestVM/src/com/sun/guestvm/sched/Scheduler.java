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

import com.sun.max.unsafe.*;
import com.sun.max.vm.MaxineVM;

/**
 * This abstract class defines the interface to the Guest VM Java thread scheduler.
 *
 * @author Mick Jordan
 *
 */

public abstract class Scheduler {
    /**
     * Perform any runtime initialization.
     * Guaranteed to be called exactly once before any Java threads are attached.
     * @param the phase the VM in at the time of the call.
     */
    public abstract void initialize(MaxineVM.Phase phase);

    /**
     * Checks whether the Java scheduler  is active, i.e., scheduling the Java threads.
     * @return true if this scheduler is active, false if microkernel scheduler is active
     */
    public abstract boolean active();

    /**
     * Provides an opportunity to analyse the set of threads created by the VM itself.
     * Called just before the VM launches the main thread on the main class.
     */
    public abstract void starting();

    /*
     * In all of the following upcalls, the cpu value denotes the cpu on which the upcall is executing.
     * N.B. In calls that include a thread argument, e.g., block, the thread's assigned cpu may
     * differ from the cpu argument if the thread invoking the action is running on a different cpu!
     */

    /**
     * Block the given thread.
     * This method is called from the ukernel scheduler when some thread
     * (possibly itself) invokes the ukernel block function for the given thread.
     * The thread has been marked non-runnable at the ukernel level.
     *
     * @param id the thread being blocked
     * @param xcpu the cpu on which the call is executing
     */
    protected abstract void blockUpcall(int id, int xcpu);

    /**
     * Wake the given thread.
     * This method is called from the ukernel scheduler when some thread
     * invokes the ukernel wake function for the given thread.
     * The thread has been marked runnable at the ukernel level.
     * @param id the thread to be woken
     * @param xcpu the cpu on which the call is executing
     */
    protected abstract void wakeUpcall(int id, int xcpu);

    /**
     * Pick a cpu on which to run a newly created thread.
     *
     * @return cpu to be used
     */
    protected abstract int pickCpuUpcall();

    /**
     * Any runnable threads for given cpu?
     * @param cpu
     * @return 1 if runnable threads, 0 otherwise
    */
    protected abstract int runnableUpcall(int cpu);

    /**
     * Attach a Java thread to this scheduler.
     * This method is called via the ukernel scheduler from GVmThread.initializationComplete.
     * The call is made by the thread itself and it has just removed itself from the ukernel
     * scheduler run queue and marked itself not-runnable.
     *
     * @param id the VM thread id
     * @paaram cpu assigned cpu of thread (from pick_cpu)
     * @param xcpu the cpu on which the call is executing
     */
    protected abstract void attachUpcall(int id, int cpu, int xcpu);

    /**
     * Detach a (terminating) Java thread from this scheduler.
     * This is called from the ukernel scheduler just after the thread
     * returns from the run method. On return from this upcall the
     * thread loses its Java tag and will be scheduled by the ukernel
     * scheduler, for the brief period until it terminates and is reaped.
     * @param id the VM thread id
     * @param xcpu the cpu on which the call is executing
     */
    protected abstract void detachUpcall(int id, int xcpu);

    /**
     * Find a java thread to run on given cpu The method is invoked by the ukernel scheduler
     * when it has no ukernel threads to run (which is most of the time).
     *
     * @param cpu the cpu on which the call is executing
     * @return Address of native thread to run or zero if none (idle thread will run)
     */
    protected abstract Word scheduleUpcall(int cpu);

    /**
     * This method is called from the ukernel scheduler when a Java thread
     * is being de-scheduled in favor of a ukernel thread - an infrequent event.
     * @param cpu the cpu on which the call is executing
     */
    protected abstract void descheduleUpcall(int cpu);

    /*
     * Direct calls to the scheduler from Java code.
     */

    /**
     * Invoke the scheduler.
     */
    public abstract void schedule();

    /**
     * Wake thread @see wakeUpcall.
     * @param thread thread to wake
     */
    public abstract void wake(GUKVmThread thread);

    /**
     * Block thread @see blockUpcall.
     * @param thread thread to block.
     */
    public abstract void block(GUKVmThread thread);
}
