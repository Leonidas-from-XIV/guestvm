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
 * Native symbols for Maxine startup/shutdown and other miscellaneous functions.
 */

#include <os.h>
#include <hypervisor.h>
#include <types.h>
#include <lib.h>
#include <mm.h>
#include <lib.h>

extern void *native_executablePath(void);
extern void native_exit(void);
extern void native_trap_exit(int code, void *address);
extern void *native_environment(void);
//extern void nativeInitializeJniInterface(void *jnienv);
extern void *native_properties(void);
/*
 * These functions are referenced from MaxineNative/image.c
 */
void exit(int n) {
  ok_exit();
}

int getpagesize(void) {
   return PAGE_SIZE;
}

void *maxine_misc_dlsym(const char *symbol) {
/*    if (strcmp(symbol, "nativeInitializeJniInterface")  == 0) return nativeInitializeJniInterface;
    else */if (strcmp(symbol, "native_exit") == 0) return native_exit;
    else if (strcmp(symbol, "native_trap_exit") == 0) return native_trap_exit;
    else if (strcmp(symbol, "native_executablePath") == 0) return native_executablePath;
    else if (strcmp(symbol, "native_environment") == 0) return native_environment;
    else if (strcmp(symbol, "native_properties") == 0) return native_properties;
    else if (strcmp(symbol, "exit") == 0) return exit;
    else return 0;
}
