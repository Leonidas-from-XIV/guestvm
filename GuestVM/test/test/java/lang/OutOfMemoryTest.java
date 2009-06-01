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
package test.java.lang;

import java.util.*;
import com.sun.max.lang.StaticLoophole;

public class OutOfMemoryTest {

    private static final long MB = 1024 * 1024;
    private static boolean _verbose = false;

    /**
     * @param args
     */
    public static void main(String[] args) {

        long maxMem = 8;
        boolean free = false;
        int repeat = 1;
        // Checkstyle: stop modified control variable check
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.equals("m")) {
                maxMem = Long.parseLong(args[++i]);
            } else if (arg.equals("v")) {
                _verbose = true;
            } else if (arg.equals("f")) {
                free = true;
            } else if (arg.equals("r")) {
                repeat = Integer.parseInt(args[++i]);
            }
        }
        // Checkstyle: resume modified control variable check
        maxMem = maxMem * MB;

        final Object[] lists = new Object[repeat];
        while (repeat > 0) {
            lists[repeat - 1] = new ArrayList<Object[]>();
            List<Object[]> leak = StaticLoophole.cast(lists[repeat - 1]);

            try {
                long count = 0;
                while (count < maxMem) {
                    leak.add(allocate());
                    count += 1024 * 8;
                    if (count % MB == 0) {
                        allocated(count);
                    }
                }
            } catch (OutOfMemoryError ex) {
                System.out.println("Out of Memory");
                if (free) {
                    lists[repeat - 1] = null;
                    leak = null;
                }
            }
            repeat--;
        }

    }

    private static Object[] allocate() {
        return new Object[1024];
    }

    private static void allocated(long count) {
        if (_verbose) {
            System.out.println("allocated " + count);
        }
    }

}
