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
package com.sun.guestvm.jdk;

import com.sun.max.annotate.*;

/**
 * This is a pass through to the GUN Classpath implementation of StrictMath.
 *
 * @author Mick Jordan
 *
 */

@METHOD_SUBSTITUTIONS(StrictMath.class)
final class JDK_java_lang_StrictMath {
    @SUBSTITUTE
    private static double sin(double a) {
        return gnu.java.lang.StrictMath.sin(a);
    }

    @SUBSTITUTE
    private static double cos(double a) {
        return gnu.java.lang.StrictMath.cos(a);
    }

    @SUBSTITUTE
    private static double tan(double a) {
        return gnu.java.lang.StrictMath.tan(a);
    }

    @SUBSTITUTE
    private static double asin(double a) {
        return gnu.java.lang.StrictMath.asin(a);
    }

    @SUBSTITUTE
    public static double acos(double a) {
        return gnu.java.lang.StrictMath.acos(a);
    }

    @SUBSTITUTE
    private static double atan(double a) {
        return gnu.java.lang.StrictMath.atan(a);
    }

    @SUBSTITUTE
    private static double exp(double a) {
        return gnu.java.lang.StrictMath.exp(a);
    }

    @SUBSTITUTE
    private static double log(double a) {
        return gnu.java.lang.StrictMath.log(a);
    }

    @SUBSTITUTE
    private static double log10(double a) {
        throw new InternalError("log10 not implemented");
        //return gnu.java.lang.StrictMath.log10(a);
    }

    @SUBSTITUTE
    private static double sqrt(double a) {
        return gnu.java.lang.StrictMath.sqrt(a);
    }

    @SUBSTITUTE
    private static double cbrt(double a) {
        return gnu.java.lang.StrictMath.cbrt(a);
    }

    @SUBSTITUTE
    private static double IEEEremainder(double f1, double f2) {
        return gnu.java.lang.StrictMath.IEEEremainder(f1, f2);
    }

    @SUBSTITUTE
    private static double ceil(double a) {
        return gnu.java.lang.StrictMath.ceil(a);
    }

    @SUBSTITUTE
    private static double floor(double a) {
        return gnu.java.lang.StrictMath.floor(a);
    }

    @SUBSTITUTE
    private static double atan2(double y, double x) {
        return gnu.java.lang.StrictMath.atan2(y, x);
    }

    @SUBSTITUTE
    private static double pow(double a, double b) {
        return gnu.java.lang.StrictMath.pow(a, b);
    }

    @SUBSTITUTE
    private static double sinh(double x) {
        return gnu.java.lang.StrictMath.sinh(x);
    }

    @SUBSTITUTE
    private static double cosh(double x) {
        return gnu.java.lang.StrictMath.cosh(x);
    }

    @SUBSTITUTE
    private static double tanh(double x) {
        return gnu.java.lang.StrictMath.tanh(x);
    }

    @SUBSTITUTE
    private static double hypot(double x, double y) {
        throw new InternalError("hypot not implemented");
        //return gnu.java.lang.StrictMath.hypot(x, y);
    }

    @SUBSTITUTE
    private static double expm1(double x) {
        return gnu.java.lang.StrictMath.expm1(x);
    }

    @SUBSTITUTE
    private static double log1p(double x) {
        throw new InternalError("log1p not implemented");
        //return gnu.java.lang.StrictMath.log1p(x);
    }

}
