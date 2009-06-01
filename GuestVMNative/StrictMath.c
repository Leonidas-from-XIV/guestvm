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
#include <os.h>
#include <hypervisor.h>
#include <types.h>
#include <lib.h>
#include <jni.h>
//#include "fdlibm.h"

extern jdouble Java_java_lang_StrictMath_cos(JNIEnv *env, jclass unused, jdouble d);
extern jdouble Java_java_lang_StrictMath_sin(JNIEnv *env, jclass unused, jdouble d);
extern jdouble Java_java_lang_StrictMath_tan(JNIEnv *env, jclass unused, jdouble d);
extern jdouble Java_java_lang_StrictMath_asin(JNIEnv *env, jclass unused, jdouble d);
extern jdouble Java_java_lang_StrictMath_acos(JNIEnv *env, jclass unused, jdouble d);
extern jdouble Java_java_lang_StrictMath_atan(JNIEnv *env, jclass unused, jdouble d);
extern jdouble Java_java_lang_StrictMath_exp(JNIEnv *env, jclass unused, jdouble d);
extern jdouble Java_java_lang_StrictMath_log(JNIEnv *env, jclass unused, jdouble d);
extern jdouble Java_java_lang_StrictMath_log10(JNIEnv *env, jclass unused, jdouble d);
extern jdouble Java_java_lang_StrictMath_sqrt(JNIEnv *env, jclass unused, jdouble d);
extern jdouble Java_java_lang_StrictMath_cbrt(JNIEnv *env, jclass unused, jdouble d);
extern jdouble Java_java_lang_StrictMath_ceil(JNIEnv *env, jclass unused, jdouble d);
extern jdouble Java_java_lang_StrictMath_floor(JNIEnv *env, jclass unused, jdouble d);
extern jdouble Java_java_lang_StrictMath_atan2(JNIEnv *env, jclass unused, jdouble d1, jdouble d2);
extern jdouble Java_java_lang_StrictMath_pow(JNIEnv *env, jclass unused, jdouble d1, jdouble d2);
extern jdouble Java_java_lang_StrictMath_IEEEremainder(JNIEnv *env, jclass unused, jdouble dividend, jdouble divisor);
extern jdouble Java_java_lang_StrictMath_cosh(JNIEnv *env, jclass unused, jdouble d);
extern jdouble Java_java_lang_StrictMath_sinh(JNIEnv *env, jclass unused, jdouble d);
extern jdouble Java_java_lang_StrictMath_tanh(JNIEnv *env, jclass unused, jdouble d);
extern jdouble Java_java_lang_StrictMath_hypot(JNIEnv *env, jclass unused, jdouble x, jdouble y);
extern jdouble Java_java_lang_StrictMath_log1p(JNIEnv *env, jclass unused, jdouble d);
extern jdouble Java_java_lang_StrictMath_expm1(JNIEnv *env, jclass unused, jdouble d);

void * StrictMath_dlsym(const char *symbol) {
    if (strcmp(symbol, "Java_java_lang_StrictMath_cos") == 0) return Java_java_lang_StrictMath_cos;
    else if (strcmp(symbol, "Java_java_lang_StrictMath_sin") == 0) return Java_java_lang_StrictMath_sin;
    else if (strcmp(symbol, "Java_java_lang_StrictMath_tan") == 0) return Java_java_lang_StrictMath_tan;
    else if (strcmp(symbol, "Java_java_lang_StrictMath_asin") == 0) return Java_java_lang_StrictMath_asin;
    else if (strcmp(symbol, "Java_java_lang_StrictMath_acos") == 0) return Java_java_lang_StrictMath_acos;
    else if (strcmp(symbol, "Java_java_lang_StrictMath_atan") == 0) return Java_java_lang_StrictMath_atan;
    else if (strcmp(symbol, "Java_java_lang_StrictMath_exp") == 0) return Java_java_lang_StrictMath_exp;
    else if (strcmp(symbol, "Java_java_lang_StrictMath_log") == 0) return Java_java_lang_StrictMath_log;
    else if (strcmp(symbol, "Java_java_lang_StrictMath_log10") == 0) return Java_java_lang_StrictMath_log10;
    else if (strcmp(symbol, "Java_java_lang_StrictMath_sqrt") == 0) return Java_java_lang_StrictMath_sqrt;
    else if (strcmp(symbol, "Java_java_lang_StrictMath_cbrt") == 0) return Java_java_lang_StrictMath_cbrt;
    else if (strcmp(symbol, "Java_java_lang_StrictMath_ceil") == 0) return Java_java_lang_StrictMath_ceil;
    else if (strcmp(symbol, "Java_java_lang_StrictMath_floor") == 0) return Java_java_lang_StrictMath_floor;
    else if (strcmp(symbol, "Java_java_lang_StrictMath_atan2") == 0) return Java_java_lang_StrictMath_atan2;
    else if (strcmp(symbol, "Java_java_lang_StrictMath_pow") == 0) return Java_java_lang_StrictMath_pow;
    else if (strcmp(symbol, "Java_java_lang_StrictMath_IEEEremainder") == 0) return Java_java_lang_StrictMath_IEEEremainder;
    else if (strcmp(symbol, "Java_java_lang_StrictMath_cosh") == 0) return Java_java_lang_StrictMath_cosh;
    else if (strcmp(symbol, "Java_java_lang_StrictMath_sinh") == 0) return Java_java_lang_StrictMath_sinh;
    else if (strcmp(symbol, "Java_java_lang_StrictMath_tanh") == 0) return Java_java_lang_StrictMath_tanh;
    else if (strcmp(symbol, "Java_java_lang_StrictMath_hypot") == 0) return Java_java_lang_StrictMath_hypot;
    else if (strcmp(symbol, "Java_java_lang_StrictMath_log1p") == 0) return Java_java_lang_StrictMath_log1p;
    else if (strcmp(symbol, "Java_java_lang_StrictMath_expm1") == 0) return Java_java_lang_StrictMath_expm1;
    else return 0;
}

JNIEXPORT jdouble JNICALL
Java_java_lang_StrictMath_cos(JNIEnv *env, jclass unused, jdouble d)
{
    guk_printk("WARNING: StrictMath.cos not implemented\n");
    return 0.0;
}

JNIEXPORT jdouble JNICALL
Java_java_lang_StrictMath_sin(JNIEnv *env, jclass unused, jdouble d)
{
    guk_printk("WARNING: StrictMath.sin not implemented\n");
    return 0.0;
}

JNIEXPORT jdouble JNICALL
Java_java_lang_StrictMath_tan(JNIEnv *env, jclass unused, jdouble d)
{
    guk_printk("WARNING: StrictMath.tan not implemented\n");
    return 0.0;
}

JNIEXPORT jdouble JNICALL
Java_java_lang_StrictMath_asin(JNIEnv *env, jclass unused, jdouble d)
{
    guk_printk("WARNING: StrictMath.asin not implemented\n");
    return 0.0;
}

JNIEXPORT jdouble JNICALL
Java_java_lang_StrictMath_acos(JNIEnv *env, jclass unused, jdouble d)
{
    guk_printk("WARNING: StrictMath.acos not implemented\n");
    return 0.0;
}

JNIEXPORT jdouble JNICALL
Java_java_lang_StrictMath_atan(JNIEnv *env, jclass unused, jdouble d)
{
    guk_printk("WARNING: StrictMath.atan not implemented\n");
    return 0.0;
}

JNIEXPORT jdouble JNICALL
Java_java_lang_StrictMath_exp(JNIEnv *env, jclass unused, jdouble d)
{
    guk_printk("WARNING: StrictMath.exp not implemented\n");
    return 0.0;
}

JNIEXPORT jdouble JNICALL
Java_java_lang_StrictMath_log(JNIEnv *env, jclass unused, jdouble d)
{
    guk_printk("WARNING: StrictMath.log not implemented\n");
    return 0.0;
}

JNIEXPORT jdouble JNICALL
Java_java_lang_StrictMath_log10(JNIEnv *env, jclass unused, jdouble d)
{
    guk_printk("WARNING: StrictMath.log10 not implemented\n");
    return 0.0;
}

JNIEXPORT jdouble JNICALL
Java_java_lang_StrictMath_sqrt(JNIEnv *env, jclass unused, jdouble d)
{
    guk_printk("WARNING: StrictMath.sqrt not implemented\n");
    return 0.0;
}

JNIEXPORT jdouble JNICALL
Java_java_lang_StrictMath_cbrt(JNIEnv *env, jclass unused, jdouble d)
{
    guk_printk("WARNING: StrictMath.cbrt not implemented\n");
    return 0.0;
}

JNIEXPORT jdouble JNICALL
Java_java_lang_StrictMath_ceil(JNIEnv *env, jclass unused, jdouble d)
{
    guk_printk("WARNING: StrictMath.ceil not implemented\n");
    return 0.0;
}

JNIEXPORT jdouble JNICALL
Java_java_lang_StrictMath_floor(JNIEnv *env, jclass unused, jdouble d)
{
    guk_printk("WARNING: StrictMath.floor not implemented\n");
    return 0.0;
}

JNIEXPORT jdouble JNICALL
Java_java_lang_StrictMath_atan2(JNIEnv *env, jclass unused, jdouble d1, jdouble d2)
{
    guk_printk("WARNING: StrictMath.atan2 not implemented\n");
    return 0.0;
}

JNIEXPORT jdouble JNICALL
Java_java_lang_StrictMath_pow(JNIEnv *env, jclass unused, jdouble d1, jdouble d2)
{
    guk_printk("WARNING: StrictMath.pow not implemented\n");
    return 0.0;
}

JNIEXPORT jdouble JNICALL
Java_java_lang_StrictMath_IEEEremainder(JNIEnv *env, jclass unused,
                                  jdouble dividend,
                                  jdouble divisor)
{
    guk_printk("WARNING: StrictMath.IEEEremainder not implemented\n");
    return 0.0;
}

JNIEXPORT jdouble JNICALL
Java_java_lang_StrictMath_cosh(JNIEnv *env, jclass unused, jdouble d)
{
    guk_printk("WARNING: StrictMath.cosh not implemented\n");
    return 0.0;
}

JNIEXPORT jdouble JNICALL
Java_java_lang_StrictMath_sinh(JNIEnv *env, jclass unused, jdouble d)
{
    guk_printk("WARNING: StrictMath.sinh not implemented\n");
    return 0.0;
}

JNIEXPORT jdouble JNICALL
Java_java_lang_StrictMath_tanh(JNIEnv *env, jclass unused, jdouble d)
{
    guk_printk("WARNING: StrictMath.tanh not implemented\n");
    return 0.0;
}

JNIEXPORT jdouble JNICALL
Java_java_lang_StrictMath_hypot(JNIEnv *env, jclass unused, jdouble x, jdouble y)
{
    guk_printk("WARNING: StrictMath.hypot not implemented\n");
    return 0.0;
}



JNIEXPORT jdouble JNICALL
Java_java_lang_StrictMath_log1p(JNIEnv *env, jclass unused, jdouble d)
{
    guk_printk("WARNING: StrictMath.log1p not implemented\n");
    return 0.0;
}

JNIEXPORT jdouble JNICALL
Java_java_lang_StrictMath_expm1(JNIEnv *env, jclass unused, jdouble d)
{
    guk_printk("WARNING: StrictMath.expm1 not implemented\n");
    return 0.0;
}
