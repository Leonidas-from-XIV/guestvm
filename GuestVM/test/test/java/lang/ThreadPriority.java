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

import test.util.*;

/**
 * This is a test originally written to test scheduling threads with different priorities, although
 * it can also be used to test the effectiveness of scheduling the same priority threads.
 * Each thread runs for the designated test time incrementing its private counter.
 * Therefore, if threads are of equal priority, and assuming fair scheduling, each thread's
 * final count should be approximately equal. Furthermore, the total count should scale
 * linearly with the number of VCPUS.
 *
 * Args:
 * t n     run with n threads
 * p t n  set priority of thread t to n (default NORM_PRIORITY)
 * ts t n  set scheduling timeslice for thread t to n (if possible)
 * h       run an additional thread with max priority
 * s n    display OS-specific thread scheduler stats every n seconds (if available)
 *
 * @author Mick Jordan
 *
 */

public class ThreadPriority {

    private static int _runTime = 5;
    private static Counter[] _counters;
    private static Thread[] _threads;
    private static volatile boolean _done;
    private static int _threadStatsPeriod;

    public static void main(String[] args) throws InterruptedException {
        int threadCount = 1;
        int[] priorities = new int[1];
        int[] timeslices = new int[1];
        priorities[0] = Thread.NORM_PRIORITY;
        timeslices[0] = 0;
        boolean highPriority = false;
        // Checkstyle: stop modified control variable check
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.equals("t")) {
                threadCount = Integer.parseInt(args[++i]);
                priorities = new int[threadCount];
                timeslices = new int[threadCount];
                for (int t = 0; t < threadCount; t++) {
                    priorities[t] = Thread.NORM_PRIORITY;
                }
                for (int t = 0; t < threadCount; t++) {
                    timeslices[t] = 0;
                }
            } else if (arg.equals("r")) {
                _runTime = Integer.parseInt(args[++i]);
            } else if (arg.equals("s")) {
                _threadStatsPeriod = Integer.parseInt(args[++i]);
            } else if (arg.equals("p")) {
                final int t = Integer.parseInt(args[++i]);
                final int p = Integer.parseInt(args[++i]);
                priorities[t] = p;
            } else if (arg.equals("ts")) {
                final int t = Integer.parseInt(args[++i]);
                final int rs = Integer.parseInt(args[++i]);
                timeslices[t] = rs;
            } else if (arg.equals("h")) {
                highPriority = true;
            }
        }
        // Checkstyle: resume modified control variable check

        if (_threadStatsPeriod > 0) {
            OSSpecific.periodicThreadStats(_threadStatsPeriod * 1000);
        }

        Thread hp = null;
        if (highPriority) {
            hp = new Thread(new HighPriority());
            hp.setPriority(Thread.MAX_PRIORITY);
            hp.start();
        }
        _counters = new Counter[threadCount];
        _threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            _counters[i] = new Counter(priorities[i], timeslices[i]);
            _threads[i] = new Thread(_counters[i]);
            _threads[i].setName("Counter[" + i + ":" + priorities[i] + "]");
            _threads[i].setPriority(priorities[i]);
        }
        for (Thread t : _threads) {
            t.start();
        }
        for (Thread t : _threads) {
            t.join();
        }
        if (highPriority) {
            hp.join();
        }
        displayCounts();
    }

    static void displayCounts() {
        for (int i = 0; i < _threads.length; i++) {
            System.out.println("  " + _threads[i].getName() + ": " + _counters[i].getCount() + ", time checks: " + _counters[i].getTimeCheckCount());
        }
    }

    static class Counter implements Runnable {
        private long _count;
        private int _timeslice;
        private int _priority;
        private long _timeCheckCount;

        Counter(int priority, int timeslice) {
            _timeslice = timeslice;
            _priority = priority;
        }

        public void run() {
            final int priority = Thread.currentThread().getPriority();
            final long endTime = System.currentTimeMillis() + _runTime * 1000;
            if (priority != _priority) {
                throw new RuntimeException("priority mismatch " + _priority + ", " + priority);
            }
            if (_timeslice > 0) {
                OSSpecific.setTimeSlice(Thread.currentThread(), _timeslice);
            }
            while (!_done) {
                _count++;
                if (_count % 10000000 == 0) {
                    _timeCheckCount++;
                    if (System.currentTimeMillis() > endTime) {
                        _done = true;
                    }
                }
            }
        }

        public synchronized long getCount() {
            return _count;
        }

        public synchronized long getTimeCheckCount() {
            return _timeCheckCount;
        }

    }

    static class HighPriority implements Runnable {
        public void run() {
            while (!_done) {
                try {
                    System.out.println("HighPriority running");
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {

                }
            }
        }
    }
}
