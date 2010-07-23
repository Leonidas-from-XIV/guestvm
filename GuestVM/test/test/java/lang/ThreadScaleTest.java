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
 * How many threads can we run?
 * Args:
 * t n     number of threads to (try to) run (default {@value ThreadScaleTest#MAX_THREADS}
 * s n    how long (secs) should each thread sleep between wakeups (default {@value ThreadScaleTest#DEFAULT_SLEEPTIME}
 * r n     how long (decs) should each thread run (default {@value ThreadScaleTest#DEFAULT_RUNTIME}
 *
 * @author Mick Jordan
 *
 */
public final class ThreadScaleTest extends Thread {

    private static final int MAX_THREADS = 65536;
    private static final int DEFAULT_SLEEPTIME = 10;
    private static final int DEFAULT_RUNTIME = 60;
    private static int _sleepTime = DEFAULT_SLEEPTIME;
    private static int _runTime = DEFAULT_RUNTIME;
    private static boolean _verbose;

    /**
     * @param args
     */
    public static void main(String[] args) {
        int n = 0;
        int nmax = MAX_THREADS;

        // Checkstyle: stop modified control variable check
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.equals("t")) {
                nmax = Integer.parseInt(args[++i]);
            } else if (arg.equals("s")) {
                _sleepTime = Integer.parseInt(args[++i]);
            } else if (arg.equals("r")) {
                _runTime = Integer.parseInt(args[++i]);
            } else if (arg.equals("v")) {
                _verbose = true;
            }  else if (arg.equals("gt")) {
                OSSpecific.setTraceState(Integer.parseInt(args[++i]), true);
            }
        }
        // Checkstyle: stop modified control variable check
        _sleepTime *= 1000;
        _runTime *= 1000;
       final List<Thread> threads = new ArrayList<Thread>();

        try {
            while (n < nmax) {
                ThreadScaleTest tst = new ThreadScaleTest(n);
                if (_verbose) {
                    System.out.println("created thread " + n);
                }
                threads.add(tst);
                tst.start();
                if (_verbose) {
                    System.out.println("started thread " + n);
                }
               n++;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("created " + n + " threads");
        try {
            for (Thread t : threads) {
                t.join();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private int _id;
    private ThreadScaleTest(int t) {
        _id = t;
    }

    public void run() {
        long now = System.currentTimeMillis();
        final long end = now + _runTime;
        while (now < end) {
            try {
                if (_verbose) {
                    System.out.println("thread " + _id + " running");
                }
                Thread.sleep(_sleepTime);
                now = System.currentTimeMillis();
            } catch (InterruptedException ex) {

            }
        }
        if (_verbose) {
            System.out.println("thread " + _id + " exiting");
        }

    }

}
