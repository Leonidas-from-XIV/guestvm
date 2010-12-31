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
import com.sun.max.ve.guk.GUKPagePool;
import com.sun.max.vm.code.CodeManager;
import com.sun.max.vm.heap.Heap;
import com.sun.max.vm.tele.*;
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
        return maxve_heapPoolBase();
    }

    public static int getSize() {
        return maxve_heapPoolSize();
    }

    public static long getRegionSize() {
        return maxve_heapPoolRegionSize();
    }

    public static boolean isAllocated(int slot) {
        if (_bitMap.isZero()) {
            _bitMap = maxve_heapPoolBitmap();
        }
        return GUKBitMap.isAllocated(_bitMap, slot);
    }

    private static class MyHeapSizeInfo extends Heap.HeapSizeInfo {
        private Size initialSize;
        private Size maxSize;
        private boolean set;
        
        @Override
        protected Size getInitialSize() {
            if (!set) {
                setHeapSizeInfo();
            }
            return initialSize;
        }
        
        @Override
        protected Size getMaxSize() {
            if (!set) {
                setHeapSizeInfo();
            }
            return maxSize;
        }
        
        private void setHeapSizeInfo() {
            // Unless overridden on the command line, we set the heap sizes
            // based on the current and maximum memory allocated by the hypervisor,
            // what we have used to date and the code region size (which is managed by the heap)
            final long extra = GUKPagePool.getMaximumReservation() - GUKPagePool.getCurrentReservation();
            long initialHeapSize = toUnit(GUKPagePool.getFreeBulkPages() * 4096);
            initialHeapSize -= toUnit(CodeManager.runtimeCodeRegionSize.getValue().toLong());
            
            if (Inspectable.isVmInspected()) {
                /* some slop for inspectable heap info, should be provided by Inspector not guessed at */
                initialHeapSize -= toUnit(initialHeapSize / 100);
            }
            final long maxHeapSize = toUnit(initialHeapSize + extra * 4096);
            
            initialSize = Heap.initialHeapSizeOption.isPresent() ? super.getInitialSize() : Size.fromLong(initialHeapSize);
            maxSize = Heap.maxHeapSizeOption.isPresent() ? super.getMaxSize() : Size.fromLong(maxHeapSize);
            set = true;
        }
    }

    private static Heap.HeapSizeInfo heapSizeInfo = new MyHeapSizeInfo();
    
    public static Heap.HeapSizeInfo getHeapSizeInfo() {
        return heapSizeInfo;
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
    private static native Address maxve_heapPoolBase();

    @C_FUNCTION
    private static native int maxve_heapPoolSize();

    @C_FUNCTION
    private static native Pointer maxve_heapPoolBitmap();

    @C_FUNCTION
    private static native long maxve_heapPoolRegionSize();

}
