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
/*
 * Native symbols for Maxine threads
 *
 * Author: Mick Jordan, Sun Microsystems Inc.
 *            Harald Roeck, Sun Microsystems, Inc., summer intern 2008.
 */

#include <os.h>
#include <hypervisor.h>
#include <types.h>
#include <sched.h>
#include <arch_sched.h>
#include <trace.h>
#include <lib.h>
#include <appsched.h>
#include <mm.h>
#include <trace.h>

struct thread* maxve_get_current(void) {
  struct thread* thread = current;
  return thread;
}

// this should be included from maxine/maxve.h but host headers cause problems
typedef	struct {
	unsigned long ss_base;
	size_t	ss_size;
} maxve_stackinfo_t;

void maxve_get_stack_info(maxve_stackinfo_t *info) {
  struct thread* thread = current;
  // Maxine requires that we allocate a guard page for the red-zone below the actual stack.
  info->ss_base = (unsigned long) thread->stack + PAGE_SIZE;
  info->ss_size = thread->stack_size - PAGE_SIZE;
}

int maxve_thread_join(struct thread *joinee) {
  guk_join_thread(joinee);
  return 0;
}

void maxve_yield(void) {
  guk_schedule();
}

void maxve_interrupt(struct thread *thread) {
  guk_interrupt(thread);
}

void maxve_set_priority(struct thread *thread, int priority) {
  // ukernel does not support priorities, handled by Java scheduler
}

/*
 * Returns -1 if sleep was interrupted
 */
int maxve_sleep(long nanosecs) {
  return nanosleep(nanosecs);
}

extern unsigned long allocate_thread_stack(size_t size);

struct thread* maxve_create_thread(
                void (*function)(void *),
                unsigned long stacksize,
                int priority,
                void *runArg) {
    // N.B. priority is not supported by the ukernel scheduler, only by the Java scheduler;
	// stacksize is guaranteed by caller  to be a multiple of page size
	unsigned long stackbase = allocate_thread_stack(stacksize);
	if (stackbase == 0) {
		return 0;
	}
    return guk_create_thread_with_stack("java_thread", function, 0, (void*) stackbase, stacksize, runArg);
}

typedef unsigned int maxve_ThreadKey;

void* maxve_thread_getSpecific(maxve_ThreadKey key) {
    struct thread* thread = current;
    return thread->specific;
}

void maxve_thread_setSpecific(maxve_ThreadKey key, void *value) {
    struct thread* thread = current;
    thread->specific = value;
}

extern void set_specifics_destructor(void (*destructor)(void *));
int maxve_thread_initializeSpecificsKey(maxve_ThreadKey *key, void (*destructor)(void *)) {
  set_specifics_destructor(destructor);
  return 0;
}

extern unsigned long nativeThreadCreate(int id, int stackSize, int priority);
extern unsigned char nonJniNativeJoin(void *a1);
extern int Java_com_sun_max_vm_thread_VmThread_nativeSleep(void *env, void *c, long numberOfMilliSeconds);
extern int Java_com_sun_max_vm_thread_VmThread_nativeJoin(void *env, void *c, void *nativeThread);
extern void Java_com_sun_max_vm_thread_VmThread_nativeSetPriority(void *env,  void *c, void *nativeThread, int priority);
extern void Java_com_sun_max_vm_thread_VmThread_nativeYield(void *env,  void *c);
extern void Java_com_sun_max_vm_thread_VmThread_nativeInterrupt(void *env,  void *c, void *thread);
extern int nonJniNativeSleep(long numberOfMilliSeconds);
extern void nativeSetGlobalThreadLock(void  *mutex);

void *maxine_threads_dlsym(const char * symbol) {
    if (strcmp(symbol, "nativeThreadCreate") == 0) return nativeThreadCreate;
    else if (strcmp(symbol, "Java_com_sun_max_vm_thread_VmThread_nativeSetPriority") == 0)
      return Java_com_sun_max_vm_thread_VmThread_nativeSetPriority;
    else if (strcmp(symbol, "Java_com_sun_max_vm_thread_VmThread_nativeYield") == 0)
      return Java_com_sun_max_vm_thread_VmThread_nativeYield;
    else if (strcmp(symbol, "Java_com_sun_max_vm_thread_VmThread_nativeSleep") == 0)
      return Java_com_sun_max_vm_thread_VmThread_nativeSleep;
    else if (strcmp(symbol, "Java_com_sun_max_vm_thread_VmThread_nativeJoin") == 0)
      return Java_com_sun_max_vm_thread_VmThread_nativeJoin;
    else if (strcmp(symbol, "Java_com_sun_max_vm_thread_VmThread_nativeInterrupt") == 0)
      return Java_com_sun_max_vm_thread_VmThread_nativeInterrupt;
    else if (strcmp(symbol, "nonJniNativeSleep") == 0) return nonJniNativeSleep;
    else if (strcmp(symbol, "nonJniNativeJoin") == 0) return nonJniNativeJoin;
    else if (strcmp(symbol, "nativeSetGlobalThreadLock") == 0) return nativeSetGlobalThreadLock;
    else return 0;
}
