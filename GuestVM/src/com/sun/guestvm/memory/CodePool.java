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
package com.sun.guestvm.memory;

import com.sun.guestvm.guk.GUKBitMap;
import com.sun.max.annotate.*;
import com.sun.max.unsafe.*;

/**
 * This is an interface to the pool of virtual memory that is used for compiled code..
 *
 * @author Mick Jordan
 *
 */

public class CodePool {
    // Code starts at the 3TB address range
    public static final long CODE_REGION_BASE = 3L * 1024 * 1024 * 1024 * 1024;

    public static Address getBase() {
        return guestvmXen_codePoolBase();
    }

    public static int getSize() {
        return guestvmXen_codePoolSize();
    }

    public static long getRegionSize() {
        return guestvmXen_codePoolRegionSize();
    }

    public static boolean isAllocated(int slot) {
        if (_bitMap.isZero()) {
            _bitMap = guestvmXen_codePoolBitmap();
        }
        return GUKBitMap.isAllocated(_bitMap, slot);
    }

    @CONSTANT_WHEN_NOT_ZERO
    private static Pointer _bitMap;

    @C_FUNCTION
    private static native Address guestvmXen_codePoolBase();

    @C_FUNCTION
    private static native int guestvmXen_codePoolSize();

    @C_FUNCTION
    private static native Pointer guestvmXen_codePoolBitmap();

    @C_FUNCTION
    private static native long guestvmXen_codePoolRegionSize();

}
