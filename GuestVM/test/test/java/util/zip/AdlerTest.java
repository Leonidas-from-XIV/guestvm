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
package test.java.util.zip;

import java.util.zip.*;

public class AdlerTest {

    /**
     * @param args
     */

    public static void main(String[] args) {
        final Adler32 a = new Adler32();
        // Checkstyle: stop modified control variable check
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.equals("u")) {
                final int uLimit = Integer.parseInt(args[++i]);
                for (int j = 1; j <= uLimit; j++) {
                    a.update(j & 0xFF);
                    System.out.println("j=" + j + ", av=" + a.getValue());
                }
            } else if (arg.equals("ub")) {
                final int size = Integer.parseInt(args[++i]);
                final byte[] b = new byte[size];
                for (int j = 0; j < size; j++) {
                    b[j] = (byte) (j & 0xFF);
                }
                a.update(b, 0, size);
                System.out.println("av=" + a.getValue());
            }
        }
        // Checkstyle: stop modified control variable check
    }

}
