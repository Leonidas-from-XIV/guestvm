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
package test.java.nio;

import java.nio.*;
import java.nio.channels.*;
import java.nio.channels.spi.*;


public class NIOTest {
    public static void main(String[] args) {
        final String[] ops = new String[10];
        final String[] values = new String[10];
        int opCount = 0;
        values[0] = "0";
        // Checkstyle: stop modified control variable check
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.equals("op")) {
                ops[opCount++] = args[++i];
                values[opCount] = values[opCount-1];
            } else if (arg.equals("v")) {
                values[opCount] = args[++i];
            }
        }
        // Checkstyle: resume modified control variable check

        for (int j = 0; j < opCount; j++) {
            final String op = ops[j];
            final String value = values[j];
            if (op.equals("allocate")) {
                final ByteBuffer b = ByteBuffer.allocate(Integer.parseInt(value));
                System.out.println(b);
            } else if (op.equals("allocateDirect")) {
                final ByteBuffer b = ByteBuffer.allocateDirect(Integer.parseInt(value));
                System.out.println(b);
            } else if (op.equals("provider")) {
                final SelectorProvider provider = SelectorProvider.provider();
                System.out.println("provder=" + provider);
            }
        }
    }
}
