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
package com.sun.guestvm.guk;

import com.sun.guestvm.memory.HeapPool;
import com.sun.guestvm.guk.x64.*;
import com.sun.max.annotate.*;
import com.sun.max.memory.VirtualMemory;
import com.sun.max.unsafe.*;
import com.sun.max.vm.Log;
import com.sun.max.vm.VMConfiguration;
import com.sun.max.lang.Unsigned;

/**
 * An interface to the physical page allocation subsystem in the microkernel.
 * This is the actual physical memory that we have to play with.
 * For convenience this memory is also mapped 1-1 to the equivalent
 * virtual memory addresses, but may be mapped elsewhere (e.g. for thread stacks).
 * This means that you can call VM.getAddressForPfn(pfn) for any value
 * in the range [getStart() .. getEnd()] and the result will be something that you safely
 * read/write with normal Address operations.
 *
 * The class includes support for responding to hypervisor requests to balloon memory up and down,
 * which interfaces with the heap scheme.
 *
 * @author Mick Jordan
 *
 */

public final class GUKPagePool  implements Runnable {

    public static final int PAGE_SIZE = X64VM.PAGE_SIZE;

    @INLINE
    public static Pointer allocatePages(int n, VirtualMemory.Type type) {
        return GUK.guk_allocate_pages(n, type.ordinal());
    }

   /**
     * Returns the current memory reservation for the domain.
     * @return number of pages allocated to domain
     */
    public static long getCurrentReservation() {
        return GUK.guk_current_reservation();
    }

    /**
     * Returns the maximum memory reservation for the domain.
     * @return
     */
    public static long getMaximumReservation() {
        return GUK.guk_maximum_reservation();
    }

    /**
     * Returns the maximum ram page  on the machine.
     * @return
     */
    public static long getMaximumRamPage() {
        return GUK.guk_maximum_ram_page();
    }
    /**
     * Returns the page number of the beginning of the page pool.
     * @return
     */
    public static long getStart() {
        return GUK.guk_page_pool_start();
    }

    /**
     * Returns the page number of the end of the page pool.
     * @return
     */

    public static long getEnd() {
        return GUK.guk_page_pool_end();
    }

    /**
     * The total number of free pages in the page pool.
     * @return
     */
    public static long getFreePages() {
        return GUK.guk_total_free_pages();
    }

    /**
     * The total number of pages in the free pool available for bulk allocation.
     * Bulk is not precisely defined but heap and code allocations are always bulk.
     * @return
     */
    public static long getFreeBulkPages() {
        return GUK.guk_bulk_free_pages();
    }

    /**
     * Returns true iff the given page is allocated.
     * @param pfn
     * @return
     */
    public static boolean isAllocated(long pfn) {
        if (_bitMap.isZero()) {
            _bitMap = GUK.guk_page_pool_bitmap();
        }
        return GUKBitMap.isAllocated(_bitMap, pfn);
    }

    public static void logState() {
        GUK.guk_dump_page_pool_state();
    }

    /**
     * Increase the page pool for the domain (if possible).
     * @param pages
     * @return the actual number allocated
     */
    public static long increasePagePool(long pages) {
        final long result =  GUK.guk_increase_page_pool(pages);
        GUKPageTables.updateNumPages();
        return result;
    }

    /**
     * Returns the number of pages that could be returned to the hypervisor.
     * @return
     */
    public static long decreaseablePagePool() {
        return GUK.guk_decreaseable_page_pool();
    }

    /**
     * Decrease the page pool for the domain (if possible).
     * @param pages
     * @return the actual number freed
     */
    public static long decreasePagePool(long pages) {
        final long result =  GUK.guk_decrease_page_pool(pages);
        GUKPageTables.updateNumPages();
        return result;
    }

    /**
     * Create a thread to watch for target memory changes from Xen.
     * @param currentTarget current target domain memory size in bytes
     */
    public static void createTargetMemoryThread(long currentTarget) {
        _log = System.getProperty(LOG_PROPERTY) != null;
        final Thread t = new Thread(new GUKPagePool(currentTarget));
        t.setName("Balloon");
        t.setDaemon(true);
        t.start();
    }

    private static final String LOG_PROPERTY = "guestvm.memset.log";
    private static boolean _log;
    private long _current; // current domain memory reservation size in pages

    private GUKPagePool(long target) {
        _current = toPages(HeapPool.toUnit(target));
    }

    public void run() {
        while (true) {
            // Xen gives us the target in units of 1K
            final long target = toPages(HeapPool.toUnit(GUK.guk_watch_memory_target() * 1024));
            if (target != _current) {
                if (_log) {
                    Log.print("PhysicalPagePool.watchTarget, current: "); logMB(_current, false); Log.print(", target: ");  logMB(target, true);
                    logState();
                }
                long change = target - _current;
                if (target > _current) {
                    change = GUKPagePool.increasePagePool(change);
                    VMConfiguration.vmConfig().heapScheme().increaseMemory(Size.fromLong(toBytes(change)));
                } else {
                    change = _current - target;
                    VMConfiguration.vmConfig().heapScheme().decreaseMemory(Size.fromLong(toBytes(change)));
                    long possibleDecrease = GUKPagePool.decreaseablePagePool();
                    if (possibleDecrease > 0) {
                        // keep decrease in heap units
                        possibleDecrease = toPages(HeapPool.toUnit(toBytes(possibleDecrease)));
                        GUKPagePool.decreasePagePool(possibleDecrease <= change ? possibleDecrease : change);
                    }
                    change = -possibleDecrease;
                }
                _current += change;
                if (_log) {
                    Log.print("PhysicalPagePool..watchTarget, change: "); logMB(change, false); Log.print(", current: "); logMB(_current, true);
                    GUKPagePool.logState();
                }
            }
        }
    }

    private static void logMB(long n, boolean nl) {
        Log.print(toMB(n));
        Log.print("MB");
        if (nl) {
            Log.println();
        }
    }

    /**
     * Converts bytes to pages.
     * @param n
     * @return
     */
    @INLINE
    private static long toPages(long n) {
        return Unsigned.ldiv(n, 4096);
    }

    /**
     * Converts pages to bytes.
     * @param n
     * @return
     */
    @INLINE
    private static long toBytes(long n) {
        return n * 4096;
    }

    /**
     * Converts pages to MB units.
     * @param n
     * @return
     */
    @INLINE
    private static long toMB(long n) {
        return Unsigned.ldiv(toBytes(n), 1024 * 1024);
    }

    @CONSTANT_WHEN_NOT_ZERO
    private static Pointer _bitMap;

}
