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

struct thread* guestvmXen_get_current(void) {
  struct thread* thread = current;
  return thread;
}

typedef struct {
  void * ss_sp;
  unsigned long ss_size;
} stackinfo_t;

void guestvmXen_get_stack_info(stackinfo_t *info) {
  struct thread* thread = current;
  info->ss_sp = thread->stack + thread->stack_size;
  info->ss_size = thread->stack_size;
}

int guestvmXen_thread_join(struct thread *joinee) {
  guk_join_thread(joinee);
  return 0;
}

void guestvmXen_yield(void) {
  guk_schedule();
}

void guestvmXen_interrupt(struct thread *thread) {
  guk_interrupt(thread);
}

void guestvmXen_set_priority(struct thread *thread, int priority) {
  // ukernel does not support priorities, handled by Java scheduler
}

/*
 * Returns -1 if sleep was interrupted
 */
int guestvmXen_sleep(long nanosecs) {
  return nanosleep(nanosecs);
}

struct thread* guestvmXen_create_thread_with_stack(char *name,
                                              void (*function)(void *),
                                              void *stack,
                unsigned long stacksize,
                int priority,
                void *data) {
  // priority is not supported by the ukernel scheduler, handled by the Java scheduler
    return guk_create_thread_with_stack(name, function, 0, stack, stacksize, data);
}

typedef unsigned int guestvmXen_ThreadKey;

void* guestvmXen_thread_getSpecific(guestvmXen_ThreadKey key) {
    struct thread* thread = current;
    return thread->specific;
}

void guestvmXen_thread_setSpecific(guestvmXen_ThreadKey key, void *value) {
    struct thread* thread = current;
    thread->specific = value;
}

extern void set_specifics_destructor(void (*destructor)(void *));
int guestvmXen_thread_initializeSpecificsKey(guestvmXen_ThreadKey *key, void (*destructor)(void *)) {
  set_specifics_destructor(destructor);
  return 0;
}

extern unsigned long nativeThreadCreate(int id, int stackSize, int priority);
extern unsigned char nativeJoin(void *a1);
extern int Java_com_sun_max_vm_thread_VmThread_nativeSleep(void *env, void *c, long numberOfMilliSeconds);
extern void Java_com_sun_max_vm_thread_VmThread_nativeSetPriority(void *env,  void *c, void *nativeThread, int priority);
extern void Java_com_sun_max_vm_thread_VmThread_nativeYield(void *env,  void *c);
extern void Java_com_sun_max_vm_thread_VmThread_nativeInterrupt(void *env,  void *c, void *thread);
extern int nonJniNativeSleep(long numberOfMilliSeconds);

void *maxine_threads_dlsym(const char * symbol) {
    if (strcmp(symbol, "nativeThreadCreate") == 0) return nativeThreadCreate;
    else if (strcmp(symbol, "nativeJoin") == 0) return nativeJoin;
    else if (strcmp(symbol, "Java_com_sun_max_vm_thread_VmThread_nativeSetPriority") == 0)
      return Java_com_sun_max_vm_thread_VmThread_nativeSetPriority;
    else if (strcmp(symbol, "Java_com_sun_max_vm_thread_VmThread_nativeYield") == 0)
      return Java_com_sun_max_vm_thread_VmThread_nativeYield;
    else if (strcmp(symbol, "Java_com_sun_max_vm_thread_VmThread_nativeSleep") == 0)
      return Java_com_sun_max_vm_thread_VmThread_nativeSleep;
    else if (strcmp(symbol, "Java_com_sun_max_vm_thread_VmThread_nativeInterrupt") == 0)
      return Java_com_sun_max_vm_thread_VmThread_nativeInterrupt;
    else if (strcmp(symbol, "nonJniNativeSleep") == 0) return nonJniNativeSleep;
    else return 0;
}
