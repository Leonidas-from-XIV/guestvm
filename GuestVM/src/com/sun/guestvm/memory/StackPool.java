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
import com.sun.guestvm.guk.GUKPageTables;
import com.sun.guestvm.guk.x64.*;
import com.sun.guestvm.spinlock.guk.*;
import com.sun.max.annotate.*;
import com.sun.max.unsafe.*;

import static com.sun.guestvm.guk.GUKPageTables.*;

/**
 * This is an interface to the pool of virtual memory that is used for thread stacks.
 *
 * @author Mick Jordan
 *
 */
public class StackPool {

    @INLINE
    public static Address getBase() {
        return guestvmXen_stackPoolBase();
    }

    @INLINE
    public static int getSize() {
        return guestvmXen_stackPoolSize();
    }

    public static long getRegionSize() {
        return guestvmXen_stackRegionSize();
    }

    public static boolean isAllocated(int slot) {
        if (_bitMap.isZero()) {
            _bitMap = guestvmXen_stackPoolBitmap();
        }
        return GUKBitMap.isAllocated(_bitMap, slot);
    }

    /**
     * Validates the page frame mappings of all the thread stacks in the pool.
     */
    public static void validate() {
        final Pointer lock = guestvmXen_stackPoolSpinLock();
        GUKSpinLock.spinLock(lock);
        for (int i = 0; i < getSize(); i++) {
            if (isAllocated(i)) {
                GUKSpinLock.spinUnlock(lock);
                validateStack(i);
                GUKSpinLock.spinLock(lock);
            }
        }
        GUKSpinLock.spinUnlock(lock);
    }

    /**
     * A Java thread stack should have the following format:
     *
     * Red zone page: unmapped
     * Yellow zone page: mapped but not present
     * Zero or more unmapped pages
     * Blue zone page: mapped but not present
     * 7 or more active pages: mapped and present
     *
     * The VM thinks the stack base is at the start of the Yellow zone page.
     * The information on where the blue zone page is currently located is stored
     * in the NativeThreadLocalsStruct, {@link NativeThreadLocal}, but that
     * is not accessible from the stack pool.
     */
    private static void validateStack(int i) {
        final long regionSize = getRegionSize();
        final long stackBase = getBase().toLong() + i * regionSize;
        final long stackEnd = stackBase + regionSize;
        boolean invalid = false;
        long p = stackBase;
        System.out.println("Validating stack page frames for slot " + i);
        check(p, pageStatus(p), PageStatus.UNMAPPED);
        p += X64VM.PAGE_SIZE;
        check(p, pageStatus(p), PageStatus.MAPPED_NOT_PRESENT);
        p += X64VM.PAGE_SIZE;
        while (p < stackEnd) {
            PageStatus ps = pageStatus(p);
            while (p < stackEnd && ps == PageStatus.UNMAPPED) {
                p += X64VM.PAGE_SIZE;
                ps = pageStatus(p);
            }
            if (p >= stackEnd) {
                // never found any mapped pages
                System.out.println("no active pages found");
                invalid = true;
                break;
            }
            check(p, ps, PageStatus.MAPPED_NOT_PRESENT);
            p += X64VM.PAGE_SIZE;
            ps = pageStatus(p);
            while (p < stackEnd && ps == PageStatus.MAPPED_PRESENT) {
                p += X64VM.PAGE_SIZE;
                ps = pageStatus(p);
            }
            if (p < stackEnd) {
                System.out.println("unexpected page state " + ps + ", at " + Long.toHexString(p));
                invalid = true;
                break;
            }
        }
    }


    private enum PageStatus {
        MAPPED_PRESENT,
        MAPPED_NOT_PRESENT,
        UNMAPPED
    }

    private static PageStatus pageStatus(long p) {
        try {
            GUKPageTables.getPteForAddress(Address.fromLong(p));
            return PageStatus.MAPPED_PRESENT;
        } catch (PteNotPresentException ex) {
            return ex._pte == 0 ? PageStatus.UNMAPPED : PageStatus.MAPPED_NOT_PRESENT;
        }
    }

    private static void check(long p, PageStatus real, PageStatus desired) {
        if (real != desired) {
            System.out.println("page at address " + Long.toHexString(p) + " is " + real + ", should be " + desired);
        }
    }

    @CONSTANT_WHEN_NOT_ZERO
    private static Pointer _bitMap;

    @C_FUNCTION
    private static native Address guestvmXen_stackPoolBase();

    @C_FUNCTION
    private static native int guestvmXen_stackPoolSize();

    @C_FUNCTION
    private static native Pointer guestvmXen_stackPoolBitmap();

    @C_FUNCTION
    private static native long guestvmXen_stackRegionSize();

    @C_FUNCTION
    private static native Pointer guestvmXen_stackPoolSpinLock();

}

