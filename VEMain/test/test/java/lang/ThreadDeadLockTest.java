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

import java.lang.management.*;


public class ThreadDeadLockTest {

    private static Lock lock1 = new Lock(1);
    private static Lock lock2 = new Lock(2);
    private static boolean verbose;

    private static class Lock {
        int id;
        Lock(int id) {
            this.id = id;
        }
        public String toString() {
            return "lock-" + id;
        }
    }

    public static void main(String[] args) throws Exception {
        boolean deadLock = false;
        boolean dump = false;
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.equals("dl")) {
                deadLock = true;
            } else if (arg.equals("dump")) {
                dump = true;
            } else if (arg.equals("v")) {
                verbose = true;
            }
        }
        final DThread thread1 = new DThread(lock1, lock2);
        final DThread thread2 = new DThread(thread1, lock2, lock1);
        thread1.setOtherThread(thread2);
        thread1.start();
        thread2.start();
        System.out.println("waiting for threads to deadlock");
        Thread.sleep(1000);
        final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        if (dump) {
            final long[] ids = new long[2];
            ids[0] = thread1.getId();
            ids[1] = thread2.getId();
            final ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(ids, true, false);
            for (int i = 0; i < ids.length; i++) {
                System.out.println("Stack for thread " + threadInfos[i].getThreadName());
                final StackTraceElement[] stackTrace = threadInfos[i].getStackTrace();
                for (int j = 0; j < stackTrace.length; j++) {
                    System.out.println("\tat " + stackTrace[j]);
                }
                final MonitorInfo[] monitorInfos = threadInfos[i].getLockedMonitors();
                System.out.println("Thread id: " + ids[i] + " locked monitors");
                for (MonitorInfo monitorInfo : monitorInfos) {
                    System.out.println("  hc: " + monitorInfo.getIdentityHashCode() + " class: " + monitorInfo.getClassName() + " depth: " + monitorInfo.getLockedStackDepth());
                }
            }
        }
        if (deadLock) {
            final long[] ids = threadMXBean.findMonitorDeadlockedThreads();
            if (ids == null) {
                System.out.println("findMonitorDeadlockedThreads returned null");
            } else {
                for (int i = 0; i < ids.length; i++) {
                    System.out.println("Thread id: " + ids[i] + " is monitor deadlocked");
                }
            }
        }
    }

    static class DThread extends Thread {
        private static int id = 1;
        protected boolean locked;
        protected DThread otherThread;
        private Lock lock1;
        private Lock lock2;

        DThread(Lock lock1, Lock lock2) {
            this(null,lock1, lock2);
        }

        DThread(DThread otherThread, Lock lock1, Lock lock2) {
            this.otherThread = otherThread;
            this.lock1 = lock1;
            this.lock2 = lock2;
            setDaemon(true);
            setName("Locker-" + id++);
        }

        void setOtherThread(DThread otherThread) {
            this.otherThread = otherThread;
        }

        public void run() {
            assert otherThread != null;
            synchronized (lock1) {
                if (verbose) {
                    log(" acquired " + lock1);
                }
                synchronized (this) {
                    locked = true;
                    notify();
                }
                // wait for thread2 to acquire lock2
                synchronized (otherThread) {
                    while (!otherThread.locked) {
                        try {
                            otherThread.wait(1000);
                        } catch (InterruptedException ex) {

                        }
                    }
                }
                // now deadlock
                if (verbose) {
                    log(" other thread acquired " + lock1 + ", trying for " + lock2);
                }
                synchronized (lock2) {
                    if (verbose) {
                        log(" acquired " + lock2);
                    }
                }
            }
        }

        private void log(String m) {
            System.out.println(System.nanoTime() + ": " + getName() + m);
        }
    }


 }
