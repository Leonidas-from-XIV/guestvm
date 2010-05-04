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

import test.util.OSSpecific;

public class ThreadDeathTest implements Runnable {

    /**
     * @param args
     */
    public static void main(String[] args) throws InterruptedException {
        int numThreads = 10;
        int lifeTime = 5;
        long sleep = 0;
        int interval = 1;
        boolean trace = false;
        // Checkstyle: stop modified control variable check
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("n")) {
                numThreads = Integer.parseInt(args[++i]);
            } else if (arg.equals("t")) {
                lifeTime = Integer.parseInt(args[++i]);
            } else if (arg.equals("s")) {
                sleep = Long.parseLong(args[++i]);
            } else if (arg.equals("r")) {
                trace = true;
            } else if (arg.equals("i")) {
                interval = Integer.parseInt(args[++i]);
            }
        }
        // Checkstyle: resume modified control variable check

        if (sleep > 0) {
            System.out.println("Sleeping for " + sleep + " seconds");
            Thread.sleep(sleep * 1000);
        }

        if (trace) {
            OSSpecific.setTraceState(0, true);
        }

        System.out.println("Starting");
        final Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new ThreadDeathTest(i, lifeTime));
            threads[i].setName("AppThread-" + i);
            threads[i].start();
            Thread.sleep(interval * 1000);
        }
        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }
        if (sleep > 0) {
            System.out.println("Sleeping for " + sleep + " seconds");
            Thread.sleep(sleep * 1000);
        }
        System.out.println("Exiting");
    }

    private int _id;
    private int _lifeTime;
    ThreadDeathTest(int i, int lifeTime) {
        _id = i;
        _lifeTime = lifeTime;
    }

    public void run() {
        try {
            System.out.println("Thread " + _id + " running");
            Thread.sleep(_lifeTime * 1000);
            System.out.println("Thread " + _id + " terminating");
        } catch (InterruptedException ex) {
        }
    }

}
