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


public class MathTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        final long[] op1 = new long[10];
        final long[] op2 = new long[10];
        final String[] ops = new String[10];
        int opCount = 0;

        // Checkstyle: stop modified control variable check
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.equals("op1")) {
                op1[opCount] = Long.parseLong(args[++i]);
            } else if (arg.equals("op2")) {
                op2[opCount] = Long.parseLong(args[++i]);
            } else if (arg.equals("op")) {
                ops[opCount++] = args[++i];
                op1[opCount] = op1[opCount - 1];
                op2[opCount] = op2[opCount - 1];
            }
        }
        // Checkstyle: resume modified control variable check

        for (int j = 0; j < opCount; j++) {
            final String op = ops[j];
            if (op.equals("pow")) {
                System.out.println("pow(" + op1[j] + ", " + op2[j] + ") = " + (long) Math.pow((double) op1[j], (double) op2[j]));
            } else if  (op.equals("lpow")) {
                System.out.println("lpow(" + op1[j] + ", " + op2[j] + ") = " + pow(op1[j], op2[j]));
            }
        }
    }

    private static long pow(long a, long b) {
        if (b == 0) {
            return 1;
        } else if (b == 1) {
            return a;
        } else {
            long result = a;
            for (long i = 1; i < b; i++) {
                result *= a;
            }
            return result;
        }
    }


}
