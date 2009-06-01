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

/**
 * Stress test for monitor contention.
 *
 * @author Mick Jordan
 *
 */

public class ThreadContend {
    private static final Object _lock = new Object();
    private static volatile boolean _done;

    public static void main(String[] args) throws InterruptedException {
        int threadCount = 1;
        int runTime = 5;
        // Checkstyle: stop modified control variable check
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.equals("t")) {
                threadCount = Integer.parseInt(args[++i]);
            } else if (arg.equals("r")) {
                runTime = Integer.parseInt(args[++i]);
            }
        }
        // Checkstyle: resume modified control variable check
        final Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Contender();
        }
        final Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            public void run() {
                _done = true;
            }
        }, runTime * 1000);

        System.out.println("test starting");
        for (Thread t : threads) {
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }
        System.out.println("test complete");
    }

    static class Contender extends Thread {
        public void run() {
            while (!_done) {
                synchronized (_lock) {

                }
            }
        }
    }
}
