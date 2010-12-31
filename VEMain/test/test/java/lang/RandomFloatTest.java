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
import java.io.*;

//import com.sun.max.ve.guk.GUKTrace;

public class RandomFloatTest {

    private static Random _random;
    private static final long DEFAULT_SEED = 467373;
    private static final int DEFAULT_COUNT = 100000;
    private static long[] _doubleBits;
    private static long[] _time;
    private static int _doubleBitsIndex;
    private static boolean _verbose;
    private static boolean _veryverbose;
    private static int _count;
    private static double[] _someDoubleValues = {
        0.15668326373182195,
        0.9342348148907909,
        0.19488241784922322,
        0.3001841713022635,
        0.9849768013837051,
        0.7022213700303095,
        0.068353575553881,
        0.23062432787202736,
        0.24091426752079903,
        0.12654135033250058
    };
    private static int _someDoublesIndex;
    private static boolean _someDoubles;

    private enum STATE {
        RAN, MUL, REM, SUB;
    }
    /**
     * @param args
     */
    public static void main(String[] args) {
        long seed = DEFAULT_SEED;
        int count = DEFAULT_COUNT;
        boolean doubles = true;
        boolean trace = false;
        String randomFile = null;
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.equals("t")) {
                seed = System.currentTimeMillis();
                System.out.println("seed is " + seed);
            } else if (arg.equals("s")) {
                seed = Long.parseLong(args[++i]);
            } else if (arg.equals("c")) {
                count = Integer.parseInt(args[++i]);
            } else if (arg.equals("f")) {
                doubles = false;
            } else if (arg.equals("v")) {
                _verbose = true;
            } else if (arg.equals("vv")) {
                _veryverbose = true;
                _verbose = true;
            } else if (arg.equals("tt")) {
                trace = true;
            } else if (arg.equals("r")) {
                randomFile = args[++i];
            } else if (arg.equals("d")) {
                _someDoubles = true;
            }
        }
        _random = new Random();
        _random.setSeed(seed);
        _doubleBits = new long[4 * count];
        _time = new long[4 * count];
        if (trace) {
//            GUKTrace.setTraceState(GUKTrace.Name.SCHED, true);
        }
        try {
            if (randomFile != null) {
                createRandomFile(randomFile, count);
                return;
            }
            if (doubles) {
                doubles(count);
            } else {
                floats(count);
            }
            System.out.println("Completed " + _count + " iterations");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void createRandomFile(String name, int count) throws IOException {
        PrintStream ps = null;
        try {
            ps = new PrintStream(new FileOutputStream(name));
            for (int i = 0; i < count; i++) {
                ps.println(_random.nextDouble());
            }
        } finally {
            ps.close();
        }
    }

    private static void reportException(NumberFormatException ex) {
        if (_verbose)  {
            ex.printStackTrace();
        } else {
            System.out.println(ex.getMessage());
        }
        final int errIndex = _doubleBitsIndex - 1;
        final int opIndex = errIndex % 4;
        final int startIndex = (errIndex / 4) * 4 - 4;
        for (int i = startIndex; i < _doubleBitsIndex; i++) {
            printIndex(i);
        }
        while (_doubleBitsIndex % 4 != 0) {
            _doubleBitsIndex++;
        }
    }

    private static void printIndex(int i) {
        System.out.println("doubleBits[" + indexAndOp(i) + "] @" + _time[i] + " " + Long.toHexString(_doubleBits[i]) + " " + Double.longBitsToDouble(_doubleBits[i]));
    }

    private static String indexAndOp(int i) {
        int r = i % 4;
        StringBuilder sb = new StringBuilder();
        sb.append(i / 4);
        sb.append(": ");
        sb.append(STATE.values()[r]);
        return sb.toString();
    }

    private static void floats(int count) {

        for (int i = 0; i < count; i++) {
            try {
                if (_verbose && i % 10000 == 0) {
                    System.out.println("Iteration " + i + " @" + System.nanoTime());
                }
             create_random_float_val_return(1.00f, 100.00f, 01f);
             _count++;
            } catch (NumberFormatException ex) {
                reportException(ex);
            }
        }
    }

    private static void doubles(long count) {
        for (int i = 0; i < count; i++) {
            try {
                if (_verbose && i % 10000 == 0) {
                    System.out.println("Iteration " + i + " @" + System.nanoTime());
                }
                create_random_double_val_return(1.00, 100.00, 01);
                _count++;
            } catch (NumberFormatException ex) {
                reportException(ex);
            }
        }
    }

   private static float create_random_float_val_return(float val_lo, float val_hi, float precision) {
        float f, result;
        f = (float) checkVal(_random.nextFloat());
        f = (float) checkVal(f * (val_hi - val_lo) + val_lo);
        result = f - (float) checkVal(Math.IEEEremainder(f, precision));
        return (float) checkVal(result);
    }


    private static double nextDouble() {
        if (_someDoubles) {
            double result = _someDoubleValues[_someDoublesIndex++];
            if (_someDoublesIndex >= _someDoubleValues.length) {
                _someDoublesIndex = 0;
            }
            return result;
        } else {
            return _random.nextDouble();
        }
    }

    private static double create_random_double_val_return(double val_lo, double val_hi, double precision) {
        double f, result;
        f = checkVal(nextDouble());;
        f = checkVal(f * (val_hi - val_lo) + val_lo);
        result = f - checkVal(Math.IEEEremainder(f, precision));
        return checkVal(result);
    }


    private static double checkVal(double d) {
        _doubleBits[_doubleBitsIndex++] = Double.doubleToRawLongBits(d);
        _time[_doubleBitsIndex - 1] = System.nanoTime();
        if (Double.isNaN(d) || Double.isInfinite(d)) {
            crash();
            throw new NumberFormatException("count:op " + indexAndOp(_doubleBitsIndex - 1));
        }
        if (_veryverbose) {
            printIndex(_doubleBitsIndex - 1);
        }
        return d;
    }

    private static void crash() {

    }
}
