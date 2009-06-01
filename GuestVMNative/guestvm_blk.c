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
 * Shim to the blk device.
 * Author: Mick Jordan, Sun Microsystems Inc.
 */
#include <os.h>
#include <hypervisor.h>
#include <types.h>
#include <lib.h>
#include <mm.h>
#include <jni.h>
#include <blk_front.h>

JNIEXPORT int JNICALL
Java_com_sun_guestvm_blk_guk_GUKBlkDevice_nativeGetDevices(JNIEnv *env, jclass c) {
    return blk_get_devices();
}

JNIEXPORT int JNICALL
Java_com_sun_guestvm_blk_guk_GUKBlkDevice_nativeGetSectors(JNIEnv *env, jclass c, jint device) {
    return blk_get_sectors(device);
}

JNIEXPORT long JNICALL
Java_com_sun_guestvm_blk_guk_GUKBlkDevice_nativeWrite(JNIEnv *env, jclass c, jint device, jlong address, void *buf, jint length) {
    return blk_write(device, address, buf, length);
}

JNIEXPORT long JNICALL
Java_com_sun_guestvm_blk_guk_GUKBlkDevice_nativeRead(JNIEnv *env, jclass c, jint device, jlong address, void *buf, jint length) {
    return blk_read(device, address, buf, length);
}

void *blk_dlsym(const char *symbol) {
    if (strcmp(symbol, "Java_com_sun_guestvm_blk_guk_GUKBlkDevice_nativeGetDevices") == 0)
      return Java_com_sun_guestvm_blk_guk_GUKBlkDevice_nativeGetDevices;
    else if (strcmp(symbol, "Java_com_sun_guestvm_blk_guk_GUKBlkDevice_nativeGetSectors") == 0)
      return Java_com_sun_guestvm_blk_guk_GUKBlkDevice_nativeGetSectors;
    else if (strcmp(symbol, "Java_com_sun_guestvm_blk_guk_GUKBlkDevice_nativeWrite") == 0)
      return Java_com_sun_guestvm_blk_guk_GUKBlkDevice_nativeWrite;
    else if (strcmp(symbol, "Java_com_sun_guestvm_blk_guk_GUKBlkDevice_nativeRead") == 0)
      return Java_com_sun_guestvm_blk_guk_GUKBlkDevice_nativeRead;
    else return 0;
}
