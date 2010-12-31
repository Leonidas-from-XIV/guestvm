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
package com.sun.max.ve.memory;

import com.sun.max.annotate.*;
import com.sun.max.unsafe.*;
import com.sun.max.ve.guk.GUKBitMap;

/**
 * This is an interface to the pool of virtual memory that is used for thread stacks.
 *
 * @author Mick Jordan
 *
 */
public class StackPool {

    @INLINE
    public static Address getBase() {
        return maxve_stackPoolBase();
    }

    @INLINE
    public static int getSize() {
        return maxve_stackPoolSize();
    }

    public static long getRegionSize() {
        return maxve_stackRegionSize();
    }

    public static boolean isAllocated(int slot) {
        if (_bitMap.isZero()) {
            _bitMap = maxve_stackPoolBitmap();
        }
        return GUKBitMap.isAllocated(_bitMap, slot);
    }

    public static Pointer getSpinLock() {
        return maxve_stackPoolSpinLock();
    }

    @CONSTANT_WHEN_NOT_ZERO
    private static Pointer _bitMap;

    @C_FUNCTION
    private static native Address maxve_stackPoolBase();

    @C_FUNCTION
    private static native int maxve_stackPoolSize();

    @C_FUNCTION
    private static native Pointer maxve_stackPoolBitmap();

    @C_FUNCTION
    private static native long maxve_stackRegionSize();

    @C_FUNCTION
    private static native Pointer maxve_stackPoolSpinLock();

}

