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

#include <lib.h>
#include <jni.h>

extern void Java_java_util_zip_Inflater_initIDs(void *env, jclass cls);
extern void Java_java_util_zip_Inflater_init(void *env, jclass cls, jboolean nowrap);
extern void Java_java_util_zip_Inflater_setDictionary(void *env, jclass cls, jlong strm,jarray b, jint off, jint len);
extern void Java_java_util_zip_Inflater_inflateBytes(void *env, jobject this, jarray b, jint off, jint len);
extern void Java_java_util_zip_Inflater_getAdler(void *env, jclass cls, jlong strm);
extern void Java_java_util_zip_Inflater_getBytesRead(void *env, jclass cls, jlong strm);
extern void Java_java_util_zip_Inflater_getBytesWritten(void *env, jclass cls, jlong strm);
extern void Java_java_util_zip_Inflater_reset(void *env, jclass cls, jlong strm);
extern void Java_java_util_zip_Inflater_end(void *env, jclass cls, jlong strm);

void *inflater_dlsym(char *symbol) {
    if (strcmp(symbol, "Java_java_util_zip_Inflater_initIDs") == 0) return Java_java_util_zip_Inflater_initIDs;
    else if (strcmp(symbol, "Java_java_util_zip_Inflater_init") == 0) return Java_java_util_zip_Inflater_init;
    else if (strcmp(symbol, "Java_java_util_zip_Inflater_setDictionary") == 0) return Java_java_util_zip_Inflater_setDictionary;
    else if (strcmp(symbol, "Java_java_util_zip_Inflater_inflateBytes") == 0) return Java_java_util_zip_Inflater_inflateBytes;
    else if (strcmp(symbol, "Java_java_util_zip_Inflater_getAdler") == 0) return Java_java_util_zip_Inflater_getAdler;
    else if (strcmp(symbol, "Java_java_util_zip_Inflater_getBytesRead") == 0) return Java_java_util_zip_Inflater_getBytesRead;
    else if (strcmp(symbol, "Java_java_util_zip_Inflater_getBytesWritten") == 0) return Java_java_util_zip_Inflater_getBytesWritten;
    else if (strcmp(symbol, "Java_java_util_zip_Inflater_reset") == 0) return Java_java_util_zip_Inflater_reset;
    else if (strcmp(symbol, "Java_java_util_zip_Inflater_end") == 0) return Java_java_util_zip_Inflater_end;
    else return 0;
}
