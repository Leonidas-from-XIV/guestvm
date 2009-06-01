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
 * Miscellaneous numeric function symbol definitions from Maxine.
 *
 * Author: Mick Jordan, Sun Microsystems Inc.
 */

#include <types.h>
#include <lib.h>
#include <jni.h>

double fmod(double a, double b) {
	printk("WARNING: fmod not implemented\n");
	return 0;
}

// Defined in substrate/snippet.c
extern jint nativeLongCompare(jlong greater, long less);
extern jlong nativeLongSignedShiftedRight(jlong number, int shift);
extern jlong nativeLongMultiply(jlong factor1, jlong factor2);
extern jlong nativeLongDivided(jlong dividend, jlong divisor);
extern jlong nativeLongRemainder(jlong dividend, jlong divisor);
extern jfloat nativeFloatRemainder(jfloat dividend, jfloat divisor);
extern jdouble nativeDoubleRemainder(JNIEnv *env, jclass c, jdouble dividend, jdouble divisor);

JNIEXPORT jfloat JNICALL
 Java_java_lang_Float_intBitsToFloat(JNIEnv *env, jclass unused, jint v) {
    union {
	int i;
	float f;
    } u;
    u.i = (long)v;
    return (jfloat)u.f;
}

JNIEXPORT jint JNICALL
Java_java_lang_Float_floatToRawIntBits(JNIEnv *env, jclass unused, jfloat v)
{
    union {
	int i;
	float f;
    } u;
    u.f = (float)v;
    return (jint)u.i;
}

/* Useful on machines where jlong and jdouble have different endianness. */
#define jlong_to_jdouble_bits(a)
#define jdouble_to_jlong_bits(a)

JNIEXPORT jdouble JNICALL
Java_java_lang_Double_longBitsToDouble(JNIEnv *env, jclass unused, jlong v)
{
    union {
	jlong l;
	double d;
    } u;
    jlong_to_jdouble_bits(&v);
    u.l = v;
    return (jdouble)u.d;
}

/*
 * Find the bit pattern corresponding to a given double float, NOT collapsing NaNs
 */
JNIEXPORT jlong JNICALL
Java_java_lang_Double_doubleToRawLongBits(JNIEnv *env, jclass unused, jdouble v)
{
    union {
	jlong l;
	double d;
    } u;
    jdouble_to_jlong_bits(&v);
    u.d = (double)v;
    return u.l;
}


void *maxine_numerics_dlsym(const char *symbol) {
  if (strcmp(symbol, "nativeLongCompare") == 0) return nativeLongCompare;
  else if (strcmp(symbol, "nativeLongSignedShiftedRight") == 0) return nativeLongSignedShiftedRight;
  else if (strcmp(symbol, "nativeLongMultiply") == 0) return nativeLongMultiply;
  else if (strcmp(symbol, "nativeLongDivided") == 0) return nativeLongDivided;
  else if (strcmp(symbol, "nativeLongRemainder") == 0) return nativeLongRemainder;
  else if (strcmp(symbol, "nativeFloatRemainder") == 0) return nativeFloatRemainder;
  else if (strcmp(symbol, "nativeDoubleRemainder") == 0) return nativeDoubleRemainder;
  else if (strcmp(symbol, "Java_java_lang_Float_intBitsToFloat") == 0) return Java_java_lang_Float_intBitsToFloat;
  else if (strcmp(symbol, "Java_java_lang_Float_floatToRawIntBits") == 0) return Java_java_lang_Float_floatToRawIntBits;
  else if (strcmp(symbol, "Java_java_lang_Double_longBitsToDouble") == 0) return Java_java_lang_Double_longBitsToDouble;
  else if (strcmp(symbol, "Java_java_lang_Double_doubleToRawLongBits") == 0) return Java_java_lang_Double_doubleToRawLongBits;
  else return 0;
}

