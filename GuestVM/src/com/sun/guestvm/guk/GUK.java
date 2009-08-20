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

import com.sun.max.annotate.*;
import com.sun.max.unsafe.*;
import com.sun.max.vm.*;
import com.sun.max.vm.reference.*;
import com.sun.max.vm.runtime.*;

/**
 * Interface to Guest VM microkernel (GUK).
 *
 * This is the single point of contact with the Guest VM microkernel.
 * All calls to GUK must pass through this class. Note that most of the methods
 * in this class are not public as other classes in this package,
 * e.g. @see GUKScheduler, provide a Java-centric interface
 * to the microkernel, using Java naming conventions and
 * possibly modified names (in case the GUK name is non-intuitive).
 * Those methods call the corresponding native methods in this class whose
 * name matches the GUK C symbol.
 * A few methods are not C_FUNCTIONs as they may block, i.e they
 * are standard JNI methods.
 *
 * @author Mick Jordan
 *
 */

public final class GUK {

    static {
        new CriticalNativeMethod(GUK.class, "guk_schedule");
        new CriticalNativeMethod(GUK.class, "guk_preempt_schedule");
        new CriticalNativeMethod(GUK.class, "guk_wait_completion");
        new CriticalNativeMethod(GUK.class, "guk_watch_memory_target");
        new CriticalNativeMethod(GUK.class, "guk_current");
        new CriticalNativeMethod(GUK.class, "guk_block");
        new CriticalNativeMethod(GUK.class, "guk_wake");
        new CriticalNativeMethod(GUK.class, "guk_attach_to_appsched");
        new CriticalNativeMethod(GUK.class, "guk_detach_from_appsched");
        new CriticalNativeMethod(GUK.class, "guk_create_spin_lock");
        new CriticalNativeMethod(GUK.class, "guk_delete_spin_lock");
        new CriticalNativeMethod(GUK.class, "guk_create_timer");
        new CriticalNativeMethod(GUK.class, "guk_delete_timer");
        new CriticalNativeMethod(GUK.class, "guk_add_timer");
        new CriticalNativeMethod(GUK.class, "guk_remove_timer");
        new CriticalNativeMethod(GUK.class, "guk_spin_lock");
        new CriticalNativeMethod(GUK.class, "guk_spin_unlock");
        new CriticalNativeMethod(GUK.class, "guk_spin_lock_irqsave");
        new CriticalNativeMethod(GUK.class, "guk_spin_unlock_irqrestore");
        new CriticalNativeMethod(GUK.class, "guk_local_irqsave");
        new CriticalNativeMethod(GUK.class, "guk_local_irqrestore");
        new CriticalNativeMethod(GUK.class, "guk_create_completion");
        new CriticalNativeMethod(GUK.class, "guk_delete_completion");
        new CriticalNativeMethod(GUK.class, "guk_complete");
        new CriticalNativeMethod(GUK.class, "guk_print_runqueue");
        new CriticalNativeMethod(GUK.class, "guk_register_upcalls");
        new CriticalNativeMethod(GUK.class, "guk_sched_num_cpus");
        new CriticalNativeMethod(GUK.class, "guk_smp_cpu_state");
        new CriticalNativeMethod(GUK.class, "guk_kick_cpu");
        new CriticalNativeMethod(GUK.class, "guk_crash_exit_msg");
        new CriticalNativeMethod(GUK.class, "guk_set_timeslice");
        new CriticalNativeMethod(GUK.class, "guk_allocate_pages");
        new CriticalNativeMethod(GUK.class, "guk_page_pool_start");
        new CriticalNativeMethod(GUK.class, "guk_page_pool_end");
        new CriticalNativeMethod(GUK.class, "guk_page_pool_bitmap");
        new CriticalNativeMethod(GUK.class, "guk_machine_page_pool_bitmap");
        new CriticalNativeMethod(GUK.class, "guk_total_free_pages");
        new CriticalNativeMethod(GUK.class, "guk_bulk_free_pages");
        new CriticalNativeMethod(GUK.class, "guk_dump_page_pool_state");
        new CriticalNativeMethod(GUK.class, "guk_increase_page_pool");
        new CriticalNativeMethod(GUK.class, "guk_decrease_page_pool");
        new CriticalNativeMethod(GUK.class, "guk_decreaseable_page_pool");
        new CriticalNativeMethod(GUK.class, "guk_current_reservation");
        new CriticalNativeMethod(GUK.class, "guk_maximum_reservation");
        new CriticalNativeMethod(GUK.class, "guk_maximum_ram_page");
        new CriticalNativeMethod(GUK.class, "guk_mfn_to_pfn");
        new CriticalNativeMethod(GUK.class, "guk_pfn_to_mfn");
        new CriticalNativeMethod(GUK.class, "guk_pagetable_base");
        new CriticalNativeMethod(GUK.class, "guk_allocate_2mb_machine_pages");
        new CriticalNativeMethod(GUK.class, "guk_netfront_xmit");
        new CriticalNativeMethod(GUK.class, "guk_ttprintk0");
        new CriticalNativeMethod(GUK.class, "guk_ttprintk1");
        new CriticalNativeMethod(GUK.class, "guk_ttprintk2");
        new CriticalNativeMethod(GUK.class, "guk_ttprintk3");
        new CriticalNativeMethod(GUK.class, "guk_ttprintk4");
        new CriticalNativeMethod(GUK.class, "guk_ttprintk5");
        new CriticalNativeMethod(GUK.class, "guk_set_trace_state");
        new CriticalNativeMethod(GUK.class, "guk_get_trace_state");
        new CriticalNativeMethod(GUK.class, "guk_exec_create");
        new CriticalNativeMethod(GUK.class, "guk_exec_wait");
        new CriticalNativeMethod(GUK.class, "guk_exec_close");
        new CriticalNativeMethod(GUK.class, "guk_exec_read_bytes");
        new CriticalNativeMethod(GUK.class, "guk_exec_destroy");
    }

    private GUK() {
    }

    /**
     * The offset of the byte array data from the byte array object's origin.
     */

    private static final Offset _dataOffset = VMConfiguration.target().layoutScheme().byteArrayLayout.getElementOffsetFromOrigin(0);

    @INLINE
    public static void crash(byte[] msg) {
        guk_crash_exit_msg(Reference.fromJava(msg).toOrigin().plus(_dataOffset));
    }

    /*
     * The actual native methods exported by GUK
     *
     */

    // Functions that may block
    static native void guk_schedule();
    static native void guk_preempt_schedule();
    static native void guk_wait_completion(Pointer comp);
    static native long guk_watch_memory_target();
    static native int guk_exec_create(Pointer prog, Pointer args, int argc, Pointer dir);
    static native int guk_exec_wait(int pid);
    static native int guk_exec_close(int pid);
    static native int guk_exec_read_bytes(int pid, Pointer bytes, int length, long fileOffset);
    static native int guk_exec_destroy(int pid);

    // C_FUNCTIONs

    @C_FUNCTION
    static native Pointer guk_current();
    @C_FUNCTION
    static native void guk_block(Word thread);
    @C_FUNCTION //(isInterruptHandler = true)
    static native void guk_wake(Word thread);
    @C_FUNCTION
    static native void guk_attach_to_appsched(Word thread, int id);
    @C_FUNCTION
    static native void guk_detach_from_appsched(Word thread);
    @C_FUNCTION
    static native Pointer guk_create_spin_lock();
    @C_FUNCTION
    static native void guk_delete_spin_lock(Pointer lock);
    @C_FUNCTION
    static native Pointer guk_create_timer();
    @C_FUNCTION
    static native void guk_delete_timer(Pointer lock);
    @C_FUNCTION
    static native void guk_add_timer(Pointer timer, long timeout);
    @C_FUNCTION
    static native int guk_remove_timer(Pointer timer);
    @C_FUNCTION //(isInterruptHandler = true)
    static native void guk_spin_lock(Pointer lock);
    @C_FUNCTION //(isInterruptHandler = true)
    static native void guk_spin_unlock(Pointer lock);
    @C_FUNCTION
    static native long guk_spin_lock_irqsave(Pointer lock);
    @C_FUNCTION
    static native void guk_spin_unlock_irqrestore(Pointer lock, long flags);
    @C_FUNCTION
    static native long guk_local_irqsave();
    @C_FUNCTION
    static native void guk_local_irqrestore(long flags);
    @C_FUNCTION
    static native Pointer guk_create_completion();
    @C_FUNCTION
    static native void guk_delete_completion(Pointer completion);
    @C_FUNCTION //(isInterruptHandler = true)
    static native void guk_complete(Pointer comp);
    @C_FUNCTION
    static native void guk_print_runqueue();
    @C_FUNCTION
    static native int guk_register_upcalls(Address scheduleCall, Address descheduleCall, Address wakeCall,
                    Address blockCall, Address attachCall, Address detachCall, Address pickCpuCall, Address runnableUpcall);
    @C_FUNCTION
    static native int guk_sched_num_cpus();
    @C_FUNCTION
    static native int guk_smp_cpu_state(int cpu);
    @C_FUNCTION
    static native int guk_kick_cpu(int cpu);
    @C_FUNCTION
    static native void guk_crash_exit_msg(Pointer msg);
    @C_FUNCTION
    static native int guk_set_timeslice(Word thread, int t);
    @C_FUNCTION
    static native Pointer guk_allocate_pages(int n, int type);
    @C_FUNCTION
    static native long guk_page_pool_start();
    @C_FUNCTION
    static native long guk_page_pool_end();
    @C_FUNCTION
    static native Pointer guk_page_pool_bitmap();
    @C_FUNCTION
    static native Pointer guk_machine_page_pool_bitmap();
    @C_FUNCTION
    static native long guk_total_free_pages();
    @C_FUNCTION
    static native long guk_bulk_free_pages();
    @C_FUNCTION
    static native long guk_dump_page_pool_state();
    @C_FUNCTION
    static native long guk_increase_page_pool(long pages);
    @C_FUNCTION
    static native long guk_decrease_page_pool(long pages);
    @C_FUNCTION
    static native long guk_decreaseable_page_pool();
    @C_FUNCTION
    static native long guk_current_reservation();
    @C_FUNCTION
    static native long guk_maximum_reservation();
    @C_FUNCTION
    static native long guk_maximum_ram_page();
    @C_FUNCTION
    static native long guk_mfn_to_pfn(long mfn);
    @C_FUNCTION
    static native long guk_pfn_to_mfn(long pfn);
    @C_FUNCTION
    static native long guk_pagetable_base();
    @C_FUNCTION
    static native long guk_allocate_2mb_machine_pages(int n, int type);

    @C_FUNCTION
    public static native void guk_netfront_xmit(Address buffer, int len);

    @C_FUNCTION
    static native void guk_ttprintk0(Pointer msg);
    @C_FUNCTION
    static native void guk_ttprintk1(Pointer msg, long arg);
    @C_FUNCTION
    static native void guk_ttprintk2(Pointer msg, long arg1, long arg2);
    @C_FUNCTION
    static native void guk_ttprintk3(Pointer msg, long arg1, long arg2, long arg3);
    @C_FUNCTION
    static native void guk_ttprintk4(Pointer msg, long arg1, long arg2, long arg3, long arg4);
    @C_FUNCTION
    static native void guk_ttprintk5(Pointer msg, long arg1, long arg2, long arg3, long arg4, long arg5);
    @C_FUNCTION
    static native int guk_set_trace_state(int var, int value);
    @C_FUNCTION
    static native int guk_get_trace_state(int var);
}
