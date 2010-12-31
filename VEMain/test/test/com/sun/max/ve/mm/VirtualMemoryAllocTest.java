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
package test.com.sun.max.ve.mm;

/**
 * This is a specific test for the page allocation algorithm in the kernel.
 * Assumes we are running with a 16MB semispace heap and 64MB of domain memory.
 * The large allocation should fail, but the small should succeed.
 *
 * @author Mick Jordan
 *
 */

import com.sun.max.memory.*;
import com.sun.max.unsafe.*;

public class VirtualMemoryAllocTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        final Pointer p = VirtualMemory.allocate(Size.fromLong(32 * 1024 * 1024), VirtualMemory.Type.HEAP);
        System.out.println("Large allocation " + check(p));
        final Pointer q = VirtualMemory.allocate(Size.fromInt(1024 * 1024), VirtualMemory.Type.STACK);
        System.out.println("Small allocation " + check(q));
    }

    private static String check(Pointer p) {
        if (p.isZero()) {
            return "failed";
        } else {
            return "ok";
        }
    }

}
