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

public class ThreadYieldTest extends Thread {

    private static boolean _done;
    private static int _runTime = 60;
    private long _count;


    public static void main(String[] args) throws InterruptedException {

        int nThreads = 2;
        final Timer timer = new Timer(true);
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.equals("t")) {
                nThreads = Integer.parseInt(args[++i]);
            } else if (arg.equals("r")) {
                _runTime = Integer.parseInt(args[++i]) * 1000;
            }
        }
        timer.schedule(new MyTimerTask(), _runTime * 1000);
        final ThreadYieldTest[] threads = new ThreadYieldTest[nThreads];
        for (int t = 0; t < nThreads; t++) {
            threads[t] = new ThreadYieldTest();
            threads[t].setName("T" + t);
        }
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        for (ThreadYieldTest thread : threads) {
            System.out.println(thread.getName() + ": " + thread._count);
        }
    }

    public void run() {
        while (!_done) {
            _count++;
            Thread.yield();
        }
    }

    static class MyTimerTask extends TimerTask {
        public void run() {
            _done = true;
        }
    }
}
