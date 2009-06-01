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
 * These symbols are defined in MaxineNative, and it is those functions that actually
 * invoke the functions in guestvm_monitor.c.
 *
 * Author: Mick Jordan, Sun Microsystems Inc.
 *
 * TODO: access the headers from MaxineNative instead of using externs
 *
 */

#include <lib.h>

extern int nativeMutexSize(void);
extern int nativeMutexInitialize(void *p);
extern int nativeConditionSize(void);
extern int nativeConditionInitialize(void *p);
extern int nativeMutexUnlock(void *p);
extern int nativeConditionNotify(void *p, int a);
extern int Java_com_sun_max_vm_monitor_modal_sync_nat_NativeMutex_nativeMutexLock(void *env, void *c, void* mutex);
extern int Java_com_sun_max_vm_monitor_modal_sync_nat_NativeConditionVariable_nativeConditionWait(void *env, void *c, void *mutex, void *condition, long timeoutMilliSeconds);

void *maxine_monitor_dlsym(char *symbol) {
    if (strcmp(symbol, "nativeMutexSize") == 0) return nativeMutexSize;
    else if (strcmp(symbol, "nativeMutexInitialize") == 0) return nativeMutexInitialize;
    else if (strcmp(symbol, "nativeConditionSize") == 0) return nativeConditionSize;
    else if (strcmp(symbol, "nativeConditionInitialize") == 0) return nativeConditionInitialize;
    else if (strcmp(symbol, "nativeMutexUnlock") == 0) return nativeMutexUnlock;
    else if (strcmp(symbol, "nativeConditionNotify") == 0) return nativeConditionNotify;
    else if (strcmp(symbol, "Java_com_sun_max_vm_monitor_modal_sync_nat_NativeMutex_nativeMutexLock") == 0)
      return Java_com_sun_max_vm_monitor_modal_sync_nat_NativeMutex_nativeMutexLock;
    else if (strcmp(symbol, "Java_com_sun_max_vm_monitor_modal_sync_nat_NativeConditionVariable_nativeConditionWait") == 0)
      return  Java_com_sun_max_vm_monitor_modal_sync_nat_NativeConditionVariable_nativeConditionWait;
    else return 0;
}
