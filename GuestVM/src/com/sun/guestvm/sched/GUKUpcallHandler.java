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

import com.sun.max.annotate.*;
import com.sun.max.unsafe.*;
import com.sun.max.vm.compiler.*;
import com.sun.max.vm.runtime.*;
import com.sun.max.vm.*;
import com.sun.guestvm.guk.*;

/**
 * This abstract class handles the connection with the microkernel upcalls.
 * It invokes the actual Scheduler method in response to the upcall.
 * Safepoints are disabled while in the scheduler.
 *
 * @author Mick Jordan
 *
 */
public abstract class GUKUpcallHandler extends Scheduler {
    public enum CpuState{
        DOWN,
        UP,
        SUSPENDING,
        RESUMING,
        SLEEPING
    }

    protected static final int MAX_CPU = 32;
    protected static int _numCpus;  // actual number of CPUs
    protected static boolean _upcallsActive = false; // true once the upcalls have been registered with the uKernel

    private static Scheduler _sched;
    private static final CriticalMethod scheduleUpcall = new CriticalMethod(GUKUpcallHandler.class, "schedule_upcall", null, CallEntryPoint.C_ENTRY_POINT);
    private static final CriticalMethod descheduleUpcall = new CriticalMethod(GUKUpcallHandler.class, "deschedule_upcall", null, CallEntryPoint.C_ENTRY_POINT);
    private static final CriticalMethod wakeUpcall = new CriticalMethod(GUKUpcallHandler.class, "wake_upcall", null, CallEntryPoint.C_ENTRY_POINT);
    private static final CriticalMethod blockUpcall = new CriticalMethod(GUKUpcallHandler.class, "block_upcall", null, CallEntryPoint.C_ENTRY_POINT);
    private static final CriticalMethod attachUpcall = new CriticalMethod(GUKUpcallHandler.class, "attach_upcall", null, CallEntryPoint.C_ENTRY_POINT);
    private static final CriticalMethod detachUpcall = new CriticalMethod(GUKUpcallHandler.class, "detach_upcall", null, CallEntryPoint.C_ENTRY_POINT);
    private static final CriticalMethod pickCpuUpcall = new CriticalMethod(GUKUpcallHandler.class, "pick_cpu_upcall", null, CallEntryPoint.C_ENTRY_POINT);
    private static final CriticalMethod runnableUpcall = new CriticalMethod(GUKUpcallHandler.class, "runnable_upcall", null, CallEntryPoint.C_ENTRY_POINT);

    public void initialize(MaxineVM.Phase phase) {
        registerWithMicrokernel();
    }

    public void schedule() {
        GUKScheduler.schedule();
    }

    private void registerWithMicrokernel() {
        _upcallsActive = GUKScheduler.registerUpcalls(
                        scheduleUpcall.address(), descheduleUpcall.address(),
                        wakeUpcall.address(), blockUpcall.address(),
                        attachUpcall.address(), detachUpcall.address(),
                        pickCpuUpcall.address(), runnableUpcall.address()) == 0;
    }

    @HOSTED_ONLY
    protected GUKUpcallHandler() {
        _sched = this;
    }

    public boolean active() {
        return _upcallsActive;
    }

    /**
     * Unless already disabled, disable safepoints.
     * @return true iff this method disabled safepoints
     */
    @INLINE
    private static boolean disableSafepoints() {
        final boolean safepointsDisabled = Safepoint.isDisabled();
        if (!safepointsDisabled) {
            Safepoint.disable();
        }
        return !safepointsDisabled;
    }

    @INLINE
    private static void enableSafepoints(boolean safepointsDisabled) {
        if (safepointsDisabled) {
            Safepoint.enable();
        }
    }

    /*
     * Normally we would use try/finally for the safepoint disable/enable but if the
     * scheduler throws an exception we are dead anyway.
     */

    @SuppressWarnings({ "unused"})
    @C_FUNCTION
    private static Word schedule_upcall(int cpu) {
        final boolean safepointsDisabled = disableSafepoints();
        final Word result = _sched.scheduleUpcall(cpu);
        enableSafepoints(safepointsDisabled);
        return result;
    }

    @SuppressWarnings({ "unused"})
    @C_FUNCTION
    private static void deschedule_upcall(int cpu) {
        final boolean safepointsDisabled = disableSafepoints();
        _sched.descheduleUpcall(cpu);
        enableSafepoints(safepointsDisabled);
    }

    @SuppressWarnings({ "unused"})
    @C_FUNCTION
    private static void wake_upcall(int id, int cpu) {
        final boolean safepointsDisabled = disableSafepoints();
        _sched.wakeUpcall(id, cpu);
        enableSafepoints(safepointsDisabled);
    }

    @SuppressWarnings({ "unused"})
    @C_FUNCTION
    private static void block_upcall(int id, int cpu) {
        final boolean safepointsDisabled = disableSafepoints();
        _sched.blockUpcall(id, cpu);
        enableSafepoints(safepointsDisabled);
    }

    @SuppressWarnings({ "unused"})
    @C_FUNCTION
    private static void attach_upcall(int id, int tcpu, int xcpu) {
        final boolean safepointsDisabled = disableSafepoints();
        _sched.attachUpcall(id, tcpu, xcpu);
        enableSafepoints(safepointsDisabled);
    }

    @SuppressWarnings({ "unused"})
    @C_FUNCTION
    private static void detach_upcall(int id, int cpu) {
        final boolean safepointsDisabled = disableSafepoints();
        _sched.detachUpcall(id, cpu);
        enableSafepoints(safepointsDisabled);
    }

    @SuppressWarnings({ "unused"})
    @C_FUNCTION
    private static int pick_cpu_upcall() {
        final boolean safepointsDisabled = disableSafepoints();
        final int result = _sched.pickCpuUpcall();
        enableSafepoints(safepointsDisabled);
        return result;
    }

    @SuppressWarnings({ "unused"})
    @C_FUNCTION
    private static int runnable_upcall(int cpu) {
        final boolean safepointsDisabled = disableSafepoints();
        final int result = _sched.runnableUpcall(cpu);
        enableSafepoints(safepointsDisabled);
        return result;
    }

}
