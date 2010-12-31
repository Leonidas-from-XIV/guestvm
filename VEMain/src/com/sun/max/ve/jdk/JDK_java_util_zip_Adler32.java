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
/* From adler32.c -- compute the Adler-32 checksum of a data stream
 * Copyright (C) 1995-1998 Mark Adler
 * For conditions of distribution and use, see copyright notice in zlib.h
 */

package com.sun.max.ve.jdk;

import java.util.zip.*;
import com.sun.max.annotate.*;

/**
 * Substitutions for  @see java.util.zip.Adler.
 * @author Mick Jordan
 */

@SuppressWarnings("unused")

@METHOD_SUBSTITUTIONS(Adler32.class)
final class JDK_java_util_zip_Adler32 {

    private static final long BASE = 65521;
    private static final int NMAX = 5552;
    /* NMAX is the largest n such that 255n(n+1)/2 + (n+1)(BASE-1) <= 2^32-1 */

    @SUBSTITUTE
    private static int update(int adler, int b) {
        long s1 = adler & 0xFFFF;
        long s2 = (adler >> 16) & 0xFFFF;
        s1 += b;
        s2 += s1;
        s1 %= BASE;
        s2 %= BASE;
        return (int) ((s2 << 16) | s1);
    }

    @SUBSTITUTE
    private static int updateBytes(int adler, byte[] b, int off, int len) {
        if (b == null) {
            return adler;
        }
        long s1 = adler & 0xFFFF;
        long s2 = (adler >> 16) & 0xFFFF;

        int xoff = off;
        while (len > 0) {
            final int k = len < NMAX ? len : NMAX;
            for (int i = 0; i < k; i++) {
                s1 += (int) (b[xoff + i] & 0xFF);
                s2 += s1;
            }
            // CheckStyle: stop parameter assignment check
            len -= k;
            // CheckStyle: resume parameter assignment check
            xoff += k;
            s1 %= BASE;
            s2 %= BASE;
        }
        return (int) ((s2 << 16) | s1);
    }
}
