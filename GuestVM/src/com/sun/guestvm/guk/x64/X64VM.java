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
package com.sun.guestvm.guk.x64;

/**
 * Interface to the virtual memory features of the x64.
 *
 * @author Mick Jordan
 *
 */

public class X64VM {

    public static final int L0_SHIFT = 0;
    public static final int L1_SHIFT = 12;
    public static final int L2_SHIFT = 21;
    public static final int L3_SHIFT = 30;
    public static final int L4_SHIFT = 39;

    public static final long L1_MASK = (1L << L2_SHIFT) - 1;
    public static final long L2_MASK = (1L << L3_SHIFT) - 1;
    public static final long L3_MASK = (1L << L4_SHIFT) - 1;

    public static final int L0_ENTRIES = 4096;
    public static final int L1_ENTRIES = 512;
    public static final int L2_ENTRIES = 512;
    public static final int L3_ENTRIES = 512;
    public static final int L4_ENTRIES = 512;

    public static final int PAGE_SHIFT = L1_SHIFT;
    public static final int PAGE_SIZE = 1 << PAGE_SHIFT;
    public static final int PAGE_OFFSET_MASK = PAGE_SIZE - 1;
    public static final int PAGE_MASK = ~PAGE_OFFSET_MASK;

    public static final int PAGE_PRESENT  = 0x001;
    public static final int PAGE_RW            = 0x002;
    public static final int PAGE_USER         = 0x004;
    public static final int PAGE_PWT          = 0x008;
    public static final int PAGE_PCD           = 0x010;
    public static final int PAGE_ACCESSED = 0x020;
    public static final int PAGE_DIRTY         = 0x040;
    public static final int PAGE_PAT            = 0x080;
    public static final int PAGE_PSE            = 0x080;
    public static final int PAGE_GLOBAL      = 0x100;

    public static final int PADDR_BITS = 52;
    public static final long PADDR_MASK = (1L << PADDR_BITS) - 1;

}
