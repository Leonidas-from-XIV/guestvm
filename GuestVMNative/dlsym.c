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
 * Maxine assumes that native symbols are resolved by the dlsym function, which is normally
 * part of the dynamic library mechanism on traditional operating systems. dlsym is typically
 * called once per native symbol resolution and subsequently cached in the VM, so performance
 * of the lookup is not an issue.
 *
 * GuestVM has a very simple dlsym implementation that simply assumes that the native
 * symbol is defined somewhere in the image. So for a "char *symbol" argument it
 * simply returns symbol. This requires a definition for the symbol at compile time and
 * an actual implementation at run time.
 *
 * Rather than have one large lookup table, the implementation of dlsym is distributed
 * among the several files. In particular GuestVM native code that defines such functions
 * always includes a subsystem dlsym function named "subsystem_dlsym".
 *
 * Author: Mick Jordan, Sun Microsystems Inc.
 *
 */

#include <os.h>
#include <hypervisor.h>
#include <types.h>
#include <spinlock.h>
#include <sched.h>
#include <lib.h>
#include <mm.h>
#include <time.h>
#include <jni.h>
#include <xmalloc.h>
#include <console.h>

extern void * maxine_dlsym(const char *symbol);
extern void * fs_dlsym(const char *symbol);
extern void * net_dlsym(const char *symbol);
extern void * guk_dlsym(const char *symbol);
extern void * blk_dlsym(const char *symbol);
extern void * thread_stack_pool_dlsym(const char *symbol);
extern void * heap_pool_dlsym(const char *symbol);
extern void * code_pool_dlsym(const char *symbol);
extern void * inflater_dlsym(const char *symbol);
extern void * StrictMath_dlsym(const char *symbol);


void *dlsym(void *a1, const char *symbol) {
  void *result;
  if ((result = maxine_dlsym(symbol)) ||
      (result = fs_dlsym(symbol)) ||
      (result = net_dlsym(symbol)) ||
      (result = guk_dlsym(symbol)) ||
      (result = blk_dlsym(symbol)) ||
      (result = thread_stack_pool_dlsym(symbol)) ||
      (result = heap_pool_dlsym(symbol)) ||
      (result = code_pool_dlsym(symbol)) ||
      (result = StrictMath_dlsym(symbol))) {
    return result;
  } else if (strcmp(symbol, "JNI_OnLoad") == 0) {
	  // special case, allows library loading to appear to work but avoids invoking JNI_OnLoad
	  return 0;
  } else {
    guk_printk("Guest VM: symbol %s not found, exiting\n", symbol);
    crash_exit();
    return 0;
  }
}

struct dlhandle {
  char *path;
};

void *dlopen(char *path, int flags) {
  struct dlhandle * handle = xmalloc(struct dlhandle);
  //printk("dlopen: %s\n", path);
  handle->path = path;
  return handle;
}

char *dlerror(void) {
  guk_printk("guestvm: dlerror not implemented\n");
  return NULL;
}

int dlclose(void) {
	guk_printk("guestvm: dlclose not implemented\n");
  return 0;
}

