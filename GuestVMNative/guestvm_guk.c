/*
 * Copyright (c)  2009 Sun Microsystems, Inc., 4150 Network Circle, Santa
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
/*
 * Shim to Guest VM microkernel (GUK).
 *
 * Author: Mick Jordan, Sun Microsystems Inc.
 */

#include <os.h>
#include <hypervisor.h>
#include <types.h>
#include <spinlock.h>
#include <lib.h>
#include <mm.h>
#include <time.h>
#include <jni.h>
#include <completion.h>
#include <xmalloc.h>
#include <sched.h>
#include <appsched.h>
#include <mtarget.h>
#include <trace.h>
#include <netfront.h>

/* This is where we hook into GUK's interface for getting to the Inspector after a crash */

typedef void (*GUKIsCrashingMethod)(void);
GUKIsCrashingMethod is_crashing_method;
void guk_is_crashing(void) {
	(*is_crashing_method)();
}

void guk_register_is_crashing_method(void *addr) {
	is_crashing_method = addr;
}

JNIEXPORT void JNICALL
Java_com_sun_guestvm_guk_GUK_guk_1schedule(JNIEnv *env, jclass c) {
    guk_schedule();
}

JNIEXPORT void JNICALL
Java_com_sun_guestvm_guk_GUK_guk_1preempt_1schedule(JNIEnv *env, jclass c) {
    guk_preempt_schedule();
}

JNIEXPORT void JNICALL
Java_com_sun_guestvm_guk_GUK_guk_1wait_1completion(JNIEnv *env, jclass c, struct completion *comp)
{
    guk_wait_for_completion(comp);
}

JNIEXPORT jlong JNICALL
Java_com_sun_guestvm_guk_GUK_guk_1watch_1memory_1target(JNIEnv *env, jclass c) {
	return guk_watch_memory_target();
}

JNIEXPORT jint JNICALL
Java_java_lang_Runtime_availableProcessors(JNIEnv *env, jclass c) {
	return guk_sched_num_cpus();
}

extern int guk_exec_create(char *prog, char *arg_block, int argc, char *dir);
JNIEXPORT jint JNICALL
Java_com_sun_guestvm_guk_GUK_guk_1exec_1create(JNIEnv *env, jclass c, char *prog, char *argBlock, int argc, char *dir) {
  return guk_exec_create(prog, argBlock, argc, dir);
}

extern int guk_exec_wait(int pid);
JNIEXPORT jint JNICALL
Java_com_sun_guestvm_guk_GUK_guk_1exec_1wait(JNIEnv *env, jclass c, int pid) {
	return guk_exec_wait(pid);
}

extern void guk_exec_destroy(int pid);
JNIEXPORT void JNICALL
Java_com_sun_guestvm_guk_GUK_guk_1exec_1destroy(JNIEnv *env, jclass c, int pid) {
    guk_exec_destroy(pid);
}

extern int guk_exec_close(int pid);
JNIEXPORT int JNICALL
Java_com_sun_guestvm_guk_GUK_guk_1exec_1close(JNIEnv *env, jclass c, int pid) {
    return guk_exec_close(pid);
}

extern int guk_exec_read_bytes(int pid, char *buf, int length, long fileOffset);
JNIEXPORT jint JNICALL
Java_com_sun_guestvm_guk_GUK_guk_1exec_1read_1bytes(JNIEnv *env, jclass c, jint pid, char *buf, int length, jlong fileOffset) {
	return guk_exec_read_bytes(pid, buf, length, fileOffset);
}

extern int guk_exec_write_bytes(int pid, char *buf, int length, long fileOffset);
JNIEXPORT jint JNICALL
Java_com_sun_guestvm_guk_GUK_guk_1exec_1write_1bytes(JNIEnv *env, jclass c, jint pid, char *buf, int length, jlong fileOffset) {
	return guk_exec_write_bytes(pid, buf, length, fileOffset);
}

long guk_nano_time(void) {
	return NOW();
}

/* Owing the the ttprintk macro magic we have to expand these manually. */
void guk_ttprintk0(char * fmt) {
	guk_ttprintk("%s\n", fmt);
}

void guk_ttprintk1(char * fmt, int arg) {
	guk_ttprintk("%s %d\n", fmt, arg);
}

void guk_ttprintk2(char * fmt, int arg1, int arg2) {
	guk_ttprintk("%s %d %d\n", fmt, arg1, arg2);
}

void guk_ttprintk3(char * fmt, int arg1, int arg2, int arg3) {
	guk_ttprintk("%s %d %d %d\n", fmt, arg1, arg2, arg3);
}

void guk_ttprintk4(char * fmt, int arg1, int arg2, int arg3, int arg4) {
	guk_ttprintk("%s %d %d %d %d\n", fmt, arg1, arg2, arg3, arg4);
}

void guk_ttprintk5(char * fmt, int arg1, int arg2, int arg3, int arg4, int arg5) {
	guk_ttprintk("%s %d %d %d %d %d\n", fmt, arg1, arg2, arg3, arg4, arg5);
}

extern int guk_domain_id(void);

void *guk_dlsym(const char * symbol) {
  if (strcmp(symbol, "Java_com_sun_guestvm_guk_GUK_guk_1schedule") == 0)
    return Java_com_sun_guestvm_guk_GUK_guk_1schedule;
  else if (strcmp(symbol, "Java_com_sun_guestvm_guk_GUK_guk_1preempt_1schedule") == 0)
    return Java_com_sun_guestvm_guk_GUK_guk_1preempt_1schedule;
  else if (strcmp(symbol, "Java_com_sun_guestvm_guk_GUK_guk_1wait_1completion") == 0)
    return Java_com_sun_guestvm_guk_GUK_guk_1wait_1completion;
  else if (strcmp(symbol, "Java_com_sun_guestvm_guk_GUK_guk_1watch_1memory_1target") == 0)
    return Java_com_sun_guestvm_guk_GUK_guk_1watch_1memory_1target;
  else if (strcmp(symbol, "Java_com_sun_guestvm_guk_GUK_guk_1exec_1create") == 0)
    return Java_com_sun_guestvm_guk_GUK_guk_1exec_1create;
  else if (strcmp(symbol, "Java_com_sun_guestvm_guk_GUK_guk_1exec_1close") == 0)
    return Java_com_sun_guestvm_guk_GUK_guk_1exec_1close;
  else if (strcmp(symbol, "Java_com_sun_guestvm_guk_GUK_guk_1exec_1wait") == 0)
    return Java_com_sun_guestvm_guk_GUK_guk_1exec_1wait;
  else if (strcmp(symbol, "Java_com_sun_guestvm_guk_GUK_guk_1exec_1destroy") == 0)
    return Java_com_sun_guestvm_guk_GUK_guk_1exec_1destroy;
  else if (strcmp(symbol, "Java_com_sun_guestvm_guk_GUK_guk_1exec_1read_1bytes") == 0)
     return Java_com_sun_guestvm_guk_GUK_guk_1exec_1read_1bytes;
  else if (strcmp(symbol, "Java_com_sun_guestvm_guk_GUK_guk_1exec_1write_1bytes") == 0)
     return Java_com_sun_guestvm_guk_GUK_guk_1exec_1write_1bytes;

  else if (strcmp(symbol, "guk_block") == 0) return guk_block;
  else if (strcmp(symbol, "guk_current") == 0) return guk_current;
  else if (strcmp(symbol, "guk_wake") == 0) return guk_wake;
  else if (strcmp(symbol, "guk_attach_to_appsched") == 0) return guk_attach_to_appsched;
  else if (strcmp(symbol, "guk_detach_from_appsched") == 0) return guk_detach_from_appsched;
  else if (strcmp(symbol, "guk_create_spin_lock") == 0) return guk_create_spin_lock;
  else if (strcmp(symbol, "guk_delete_spin_lock") == 0) return guk_delete_spin_lock;
  else if (strcmp(symbol, "guk_create_completion") == 0) return guk_create_completion;
  else if (strcmp(symbol, "guk_delete_completion") == 0) return guk_delete_completion;
  else if (strcmp(symbol, "guk_create_timer") == 0) return guk_create_timer;
  else if (strcmp(symbol, "guk_remove_timer") == 0) return guk_remove_timer;
  else if (strcmp(symbol, "guk_add_timer") == 0) return guk_add_timer;
  else if (strcmp(symbol, "guk_delete_timer") == 0) return guk_delete_timer;
  else if (strcmp(symbol, "guk_complete") == 0) return guk_complete;
  else if (strcmp(symbol, "guk_spin_lock") == 0) return guk_spin_lock;
  else if (strcmp(symbol, "guk_spin_unlock") == 0) return guk_spin_unlock;
  else if (strcmp(symbol, "guk_print_runqueue") == 0) return guk_print_runqueue;
  else if (strcmp(symbol, "guk_spin_lock_irqsave") == 0) return guk_spin_lock_irqsave;
  else if (strcmp(symbol, "guk_spin_unlock_irqrestore") == 0) return guk_spin_unlock_irqrestore;
  else if (strcmp(symbol, "guk_local_irqsave") == 0) return guk_local_irqsave;
  else if (strcmp(symbol, "guk_local_irqrestore") == 0) return guk_local_irqrestore;
  else if (strcmp(symbol, "guk_register_upcalls") == 0) return guk_register_upcalls;
  else if (strcmp(symbol, "guk_sched_num_cpus") == 0) return guk_sched_num_cpus;
  else if (strcmp(symbol, "guk_smp_cpu_state") == 0) return guk_smp_cpu_state;
  else if (strcmp(symbol, "guk_kick_cpu") == 0) return guk_kick_cpu;
  else if (strcmp(symbol, "guk_crash_exit_msg") == 0) return guk_crash_exit_msg;
  else if (strcmp(symbol, "guk_set_timeslice") == 0) return guk_set_timeslice;
  else if (strcmp(symbol, "guk_mfn_to_pfn") == 0) return guk_mfn_to_pfn;
  else if (strcmp(symbol, "guk_pfn_to_mfn") == 0) return guk_pfn_to_mfn;
  else if (strcmp(symbol, "guk_pagetable_base") == 0) return guk_pagetable_base;
  else if (strcmp(symbol, "guk_allocate_pages") == 0) return guk_allocate_pages;
  else if (strcmp(symbol, "guk_increase_page_pool") == 0) return guk_increase_page_pool;
  else if (strcmp(symbol, "guk_decrease_page_pool") == 0) return guk_decrease_page_pool;
  else if (strcmp(symbol, "guk_decreaseable_page_pool") == 0) return guk_decreaseable_page_pool;
  else if (strcmp(symbol, "guk_current_reservation") == 0) return guk_current_reservation;
  else if (strcmp(symbol, "guk_maximum_reservation") == 0) return guk_maximum_reservation;
  else if (strcmp(symbol, "guk_maximum_ram_page") == 0) return guk_maximum_ram_page;
  else if (strcmp(symbol, "guk_page_pool_start") == 0) return guk_page_pool_start;
  else if (strcmp(symbol, "guk_page_pool_end") == 0) return guk_page_pool_end;
  else if (strcmp(symbol, "guk_page_pool_bitmap") == 0) return guk_page_pool_bitmap;
  else if (strcmp(symbol, "guk_machine_page_pool_bitmap") == 0) return guk_machine_page_pool_bitmap;
  else if (strcmp(symbol, "guk_total_free_pages") == 0) return guk_total_free_pages;
  else if (strcmp(symbol, "guk_bulk_free_pages") == 0) return guk_bulk_free_pages;
  else if (strcmp(symbol, "guk_dump_page_pool_state") == 0) return guk_dump_page_pool_state;
  else if (strcmp(symbol, "guk_allocate_2mb_machine_pages") == 0) return guk_allocate_2mb_machine_pages;
  else if (strcmp(symbol, "guk_netfront_xmit")  == 0) return guk_netfront_xmit;
  else if (strcmp(symbol, "guk_register_is_crashing_method")  == 0) return guk_register_is_crashing_method;
  else if (strcmp(symbol, "guk_domain_id")  == 0) return guk_domain_id;
  else if (strcmp(symbol, "guk_get_cpu_running_time")  == 0) return guk_get_cpu_running_time;
  else if (strcmp(symbol, "guk_nano_time")  == 0) return guk_nano_time;

  else if  (strcmp(symbol, "guk_ttprintk0") == 0) return guk_ttprintk0;
  else if  (strcmp(symbol, "guk_ttprintk1") == 0) return guk_ttprintk1;
  else if  (strcmp(symbol, "guk_ttprintk2") == 0) return guk_ttprintk2;
  else if  (strcmp(symbol, "guk_ttprintk3") == 0) return guk_ttprintk3;
  else if  (strcmp(symbol, "guk_ttprintk4") == 0) return guk_ttprintk4;
  else if  (strcmp(symbol, "guk_ttprintk5") == 0) return guk_ttprintk5;
  else if  (strcmp(symbol, "guk_set_trace_state") == 0) return guk_set_trace_state;
  else if  (strcmp(symbol, "guk_get_trace_state") == 0) return guk_get_trace_state;

  else if  (strcmp(symbol, "Java_java_lang_Runtime_availableProcessors") == 0) return Java_java_lang_Runtime_availableProcessors;
  else return 0;
}
