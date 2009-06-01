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
import com.sun.guestvm.guk.GUKPagePool;
import com.sun.max.annotate.*;
import com.sun.max.unsafe.*;
import com.sun.max.vm.heap.Heap;

/**
 * An interface to heap virtual memory pool and control of heap size.
 *
 * All heaps sizes and adjustments are multiples of 4MB.
 *
 * @author Mick Jordan
 *
 */

public final class HeapPool {
    public static Address getBase() {
        return guestvmXen_heapPoolBase();
    }

    public static int getSize() {
        return guestvmXen_heapPoolSize();
    }

    public static long getRegionSize() {
        return guestvmXen_heapPoolRegionSize();
    }

    public static boolean isAllocated(int slot) {
        if (_bitMap.isZero()) {
            _bitMap = guestvmXen_heapPoolBitmap();
        }
        return GUKBitMap.isAllocated(_bitMap, slot);
    }

    public static void setInitialHeapSize() {
        // Unless overridden on the command line, we set the heap sizes
        // based on the current and maximum memory allocated by the hypervisor.
        final long extra = GUKPagePool.getMaximumReservation() - GUKPagePool.getCurrentReservation();
        final long initialHeapSize = toUnit((GUKPagePool.getFreeBulkPages()) * 4096);
        final long maxHeapSize = toUnit(initialHeapSize + extra * 4096);
        if (!Heap.initialSizeOptionIsPresent()) {
            Heap.setInitialSize(Size.fromLong(initialHeapSize));
        }
        if (!Heap.maxSizeOptionIsPresent()) {
            Heap.setMaxSize(Size.fromLong(maxHeapSize));
        }
    }

    private static final long FOUR_MB_MASK = ~((4 * 1024 * 1024) - 1);

    /**
     * Round a size in bytes to multiple of a heap unit.
     * @param n
     * @return
     */
    @INLINE
    public static long toUnit(long n) {
        return n & FOUR_MB_MASK;
    }


    @CONSTANT_WHEN_NOT_ZERO
    private static Pointer _bitMap;

    @C_FUNCTION
    private static native Address guestvmXen_heapPoolBase();

    @C_FUNCTION
    private static native int guestvmXen_heapPoolSize();

    @C_FUNCTION
    private static native Pointer guestvmXen_heapPoolBitmap();

    @C_FUNCTION
    private static native long guestvmXen_heapPoolRegionSize();

}
