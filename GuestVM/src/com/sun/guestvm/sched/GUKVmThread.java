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

import com.sun.max.vm.thread.*;
import com.sun.max.annotate.*;
import com.sun.max.unsafe.*;
import com.sun.guestvm.guk.*;

/**
 * A subclass of VmThread that is tied to the Guest VM microkernel (GUK) native thread.
 *
 * @author Harald Roeck
 * @author Mick Jordan
 *
 */
public class GUKVmThread extends VmThread {

    private int _cpu; // cpu the thread is running or scheduled to run on
    private boolean _running; // true if this thread is running or scheduled to run next
    private boolean _notified; //  true if condition notification received
    private GUKVmThread _nextWaiting; /* for waiting list in mutex and condition variables */


    /*
     * The following values have to match the ukernel flag definitions in include/sched.h.
     */
    private static final int RUNNABLE_FLAG = 0x00000001;
    private static final int RUNNING_FLAG = 0x00000002;
    private static final int RESCHED_FLAG = 0x00000004;
    private static final int DYING_FLAG = 0x00000008;
    private static final int INTERRUPTED_FLAG = 0x00000080;
    private static final int AUX1_FLAG = 0x00000400;
    private static final int AUX2_FLAG = 0x00000800;
    private static final int SLEEP_FLAG = 0x00001000;
    private static final int APPSCHED_FLAG = 0x00002000;

    /*
     * Byte (address) offsets into the native thread struct. Must match include/sched.h.
     */
    private static final int PREEMPT_COUNT_OFFSET = 0;
    private static final int FLAGS_OFFSET = 4;
    private static final int ID_OFFSET = 16;
    // private static final int JAVA_ID_OFFSET = 18;
    private static final int CPU_OFFSET = 72;
    private static final int FLAGS_OFFSET_ASINT = FLAGS_OFFSET / 4;
    private static final int CPU_OFFSET_ASINT = CPU_OFFSET / 4;
    private static final int ID_OFFSET_AS_SHORT = ID_OFFSET / 2;
    // private static final int JAVA_ID_OFFSET_AS_SHORT = JAVA_ID_OFFSET / 2;

    public GUKVmThread() {
        super();
        _running = false;
        _cpu = -1;
        _nextWaiting = null;
        _notified = false;
    }

    @Override
    protected void initializationComplete() {
        GUKScheduler.attachThread(nativeThread, id());
    }

    @Override
    protected void terminationComplete() {
        GUKScheduler.detachThread(nativeThread);
    }

    @INLINE
    public final int nativeId() {
        return nativeThread.asPointer().getShort(ID_OFFSET_AS_SHORT);
    }

    public int compareTo(GUKVmThread sthread) {
        return this.javaThread().getPriority() - sthread.javaThread().getPriority();
    }

    @INLINE
    private void setFlags(int flags) {
        nativeThread.asPointer().setInt(FLAGS_OFFSET_ASINT, flags);
    }

    @INLINE
    private int getFlags() {
        return nativeThread.asPointer().getInt(FLAGS_OFFSET_ASINT);
    }

    @INLINE
    private void clearFlag(int flag) {
        int flags = getFlags();
        flags &= ~flag;
        setFlags(flags);
    }

    @INLINE
    private void setFlag(int flag) {
        int flags = getFlags();
        flags |= flag;
        setFlags(flags);
    }

    @INLINE
    private boolean isFlag(int flag) {
        return (getFlags() & flag) != 0;
    }

    @INLINE
    public final boolean isRunnable() {
        return isFlag(RUNNABLE_FLAG);
    }

    @INLINE
    public final void setRunnable(boolean runnable) {
        if (runnable) {
            setFlag(RUNNABLE_FLAG);
        } else {
            clearFlag(RUNNABLE_FLAG);
        }
    }

    @INLINE
    public final boolean isRunning() {
        /*
         * a thread is considered running if it is currently active as indicated by the flags or the scheduler decided
         * to run it next
         */
        return _running || isFlag(RUNNING_FLAG);
    }

    @INLINE
    public final void setRunning(boolean running) {
        _running = running;
    }

    @INLINE
    public final boolean isDying() {
        return isFlag(DYING_FLAG);
    }

    @INLINE
    public final boolean isSleeping() {
        return isFlag(SLEEP_FLAG);
    }

    @INLINE
    public final boolean isOSInterrupted() {
        return isFlag(INTERRUPTED_FLAG);
    }

    @INLINE
    public final void clearOSInterrupted() {
        clearFlag(INTERRUPTED_FLAG);
    }

    @INLINE
    public final void setConditionWait(boolean bool) {
        if (bool) {
            setFlag(AUX2_FLAG);
        } else {
            clearFlag(AUX2_FLAG);
        }
    }

    @INLINE
    public final void setMutexWait(boolean bool) {
        if (bool) {
            setFlag(AUX1_FLAG);
        } else {
            clearFlag(AUX1_FLAG);
        }
    }

    @INLINE
    public final boolean isJava() {
        return isFlag(APPSCHED_FLAG);
    }

    /**
     * Mark the thread as blocked or ready to run.
     */
    public void setSchedulable(boolean ready) {
        final Scheduler sched = SchedulerFactory.scheduler();
        if (ready) {
            if (sched.active()) {
                setRunnable(true);
                sched.wake(this);
            } else {
                wakeup();
            }
        } else {
            if (sched.active()) {
                setRunnable(false);
                sched.block(this);
            } else {
                block();
            }
        }
    }

    /** Set the cpu for the thread.
    * @param cpu the cpu to set
     */
    public void setCpu(int cpu) {
        this._cpu = cpu;
        nativeThread.asPointer().setInt(CPU_OFFSET_ASINT, cpu);
    }

    /**
     * @return the cpu
     */
    public int getCpu() {
        return _cpu;
    }

    /**
     * @return the _notified
     */
    public boolean isNotified() {
        return _notified;
    }

    /**
     * @param notified
     *                the _notified to set
     */
    public void setNotified(boolean notified) {
        this._notified = notified;
    }

    /**
     * @return the _nextWaiting
     */
    public GUKVmThread getNextWaiting() {
        return _nextWaiting;
    }

    /**
     * @param waiting
     *                the _nextWaiting to set
     */
    public void setNextWaiting(GUKVmThread waiting) {
        _nextWaiting = waiting;
    }

    @INLINE
    private void block() {
        GUKScheduler.block(nativeThread);
    }

    @INLINE
    private void wakeup() {
        GUKScheduler.wake(nativeThread);
    }

    @INLINE
    private static Pointer nativeThreadPointer() {
        return VmThread.current().nativeThread().asPointer();
    }

    /**
     * Disable pre-emption for the current thread.
     */
    @INLINE
    public static final void disablePreemption() {
        final Pointer ntp = nativeThreadPointer();
        ntp.setInt(1 + ntp.getInt(PREEMPT_COUNT_OFFSET));
    }

    @INLINE
    public static void enablePreemption() {
        final Pointer ntp = nativeThreadPointer();
        ntp.setInt(ntp.getInt() - 1);
        if ((ntp.getInt(FLAGS_OFFSET_ASINT) & RESCHED_FLAG) != 0) {
            GUKScheduler.preemptSchedule();
        }
    }

    @INLINE
    public static GUKVmThread getFromNative(Word nativeThread) {
        final int id = get_java_id(nativeThread);
        final GUKVmThread retval = (GUKVmThread) VmThreadMap.ACTIVE.getVmThreadForID(id);
        return retval;
    }

    @C_FUNCTION
    private static native int get_java_id(Word nativeThread);


}
