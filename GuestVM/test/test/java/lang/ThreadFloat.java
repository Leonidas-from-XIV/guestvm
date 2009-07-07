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

import test.util.OSSpecific;

/**
 * A test to show that fp registers are preserved across thread switches.
 *
 * @author Mick Jordan
 *
 */
public class ThreadFloat extends Thread {

    private static volatile boolean _done;
    private static boolean _yield;
    /**
     * @param args
     */
    public static void main(String[] args) throws InterruptedException {
        int nThreads = 2;
        int runtime = 10;
        int timeSlice = 0;
        // Checkstyle: stop modified control variable check
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.equals("t")) {
                nThreads = Integer.parseInt(args[++i]);
            } else if (arg.equals("r")) {
                runtime = Integer.parseInt(args[++i]);
            } else if (arg.equals("y")) {
                _yield = true;
            } else if (arg.equals("ts")) {
                timeSlice = Integer.parseInt(args[++i]);
            }
        }

        final Timer timer = new Timer(true);
        timer.schedule(new MyTimerTask(), runtime *1000);
        final ThreadFloat[] threads = new ThreadFloat[nThreads];
        for (int t = 0; t < nThreads; t++) {
            threads[t] = new ThreadFloat(t);
            threads[t].setName("T" + t);
            if (timeSlice > 0) {
                OSSpecific.setTimeSlice(threads[t], timeSlice);
            }
            threads[t].start();
        }
        for (int t = 0; t < nThreads; t++) {
            threads[t].join();
            System.out.println(threads[t] + ": " + threads[t]._count);
        }
    }

    public void run() {
        while (!_done) {
            double d1 = D1 * _id;
            double d2 = D2 * _id;
            double d3 = D3 * _id;
            double d4 = D4 * _id;
            if (_yield) {
                Thread.yield();
            }
            call(d1, d2, d3, d4);
        }
    }

    ThreadFloat(int id) {
        _id = id;
    }

    private static final double D1 = 1.0;
    private static final double D2 = 2.0;
    private static final double D3 = 3.0;
    private static final double D4 = 4.0;
    private int _id;
    private long _count;

    public void call(double d1, double d2, double d3, double d4) {
        if (d1 != D1 * _id || d2 != D2 * _id || d3 != D3 * _id || d4 != D4 * _id) {
            throw new IllegalArgumentException("thread arguments mismatch");
        }
        _count++;
    }

    static class MyTimerTask extends TimerTask {
        public void run() {
            _done = true;
        }
    }

}
