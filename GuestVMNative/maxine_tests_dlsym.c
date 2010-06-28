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
  * Native symbols for Maxine tests.
  * These are only needed for test programs.
  */

#include <lib.h>
#include <jni.h>

extern jlong JNICALL
Java_test_bench_threads_JNI_1invocations_nativework(JNIEnv *env, jclass cls, jlong workload);
extern jint JNICALL
Java_jtt_jni_JNI_1OverflowArguments_read1(JNIEnv *env, jclass cls, jlong zfile,
														jlong zentry, jlong pos, jbyteArray bytes, jint off, jint len);
extern jint JNICALL
Java_jtt_jni_JNI_1OverflowArguments_read2(JNIEnv *env, jclass cls, jlong zfile,
														jlong zentry, jlong pos, jbyteArray bytes, jint off, jint len);

void *maxine_tests_dlsym(const char *symbol) {
    if (strcmp(symbol, "Java_test_bench_threads_JNI_1invocations_nativework") == 0) return Java_test_bench_threads_JNI_1invocations_nativework;
    else if (strcmp(symbol, "Java_jtt_jni_JNI_1OverflowArguments_read1") == 0) return Java_jtt_jni_JNI_1OverflowArguments_read1;
    else if (strcmp(symbol, "Java_jtt_jni_JNI_1OverflowArguments_read2") == 0) return Java_jtt_jni_JNI_1OverflowArguments_read2;
   else return 0;
}
