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

import java.io.*;

public class MathTest {

    private static PrintStream out;

    /**
     * @param args
     */
    public static void main(String[] args) {
        out = System.out;
        final String result = checkMathFcts();
        if (result != null) {
            out.println(result);
        } else {
            out.println("OK");
        }
    }

    static boolean checkClose(String exprStr, double v, double r) {

        double m, av = v, ar = r;

        if (av < 0.0)
            av = -av;

        if (ar < 0.0)
            ar = -ar;

        if (av > ar)

            m = av;

        else

            m = ar;

        if (m == 0.0)
            m = 1.0;

        if ((v - r) / m > 0.0001) {

            out.println(exprStr + " evaluated to: " + v + ", expected: " + r);

            return false;

        }

        return true;

    }

    static String checkMathFcts() {

        out.print("checkMathFcts: ");

        boolean ok = true;

        if (!checkClose("log(0.7)", Math.log(0.7), -0.356675))
            ok = false;

        if (!checkClose("sin(0.7)", Math.sin(0.7), 0.644218))
            ok = false;

        if (!checkClose("cos(0.7)", Math.cos(0.7), 0.764842))
            ok = false;

        if (!checkClose("tan(0.7)", Math.tan(0.7), 0.842288))
            ok = false;

        if (!checkClose("asin(0.7)", Math.asin(0.7), 0.775397))
            ok = false;

        if (!checkClose("acos(0.7)", Math.acos(0.7), 0.795399))
            ok = false;

        if (!checkClose("atan(0.7)", Math.atan(0.7), 0.610726))
            ok = false;

        if (!ok)
            return "Some math function failed";

        return null;

    }

}
