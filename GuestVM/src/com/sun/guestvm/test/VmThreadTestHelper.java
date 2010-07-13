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
package com.sun.guestvm.test;

import com.sun.max.unsafe.*;
import com.sun.max.vm.runtime.*;
import com.sun.max.vm.reference.Reference;
import com.sun.max.vm.thread.*;
import com.sun.guestvm.guk.*;
import com.sun.guestvm.sched.*;

/**
 * Some support classes for tests that involve {@link VmThread}.
 * These must be compiled into the image.
 *
 * @author Mick Jordan
 *
 */
public class VmThreadTestHelper {
    public static VmThread current() {
        return VmThread.current();
    }

    public static long currentAsAddress() {
        return Reference.fromJava(VmThread.current()).toOrigin().asAddress().toLong();
    }

    public static int idLocal() {
        return VmThreadLocal.ID.getConstantWord().asAddress().toInt();
    }

    public static int idCurrent() {
        return VmThread.current().id();
    }

    public static long nativeCurrent() {
        return VmThread.current().nativeThread().asAddress().toLong();
    }

    public static long nativeUKernel() {
        return GUKScheduler.currentThread().toLong();
    }

    public static int nativeId(Thread t) {
        final GUKVmThread gvm = (GUKVmThread) VmThread.fromJava(t);
        return gvm == null ? -1 : gvm.safeNativeId();
    }

    public interface Procedure {
        void run(long trapState);
    }

    /**
     * Run the {@link #testSafepointProcedure} on a set of threads.
     * @param threads
     */
    public static void runSafepointProcedure(Thread[] threads) {
        TestSafepointProcedure testSafepointProcedure = new TestSafepointProcedure(threads);
        TestPointerProcedure testPointerProcedure = new TestPointerProcedure(testSafepointProcedure);
        synchronized (VmThreadMap.ACTIVE) {
            VmThreadMap.ACTIVE.forAllThreadLocals(new MatchPredicate(threads), testPointerProcedure);
        }
        testSafepointProcedure.waitForSafePoint();
    }

    private static class MatchPredicate implements Pointer.Predicate {
        Thread[] threads;

        MatchPredicate() {
        }

        MatchPredicate(Thread[] threads) {
            this.threads = threads;
        }
        public boolean evaluate(Pointer p) {
            for (int i = 0; i < threads.length; i++) {
                if (p == VmThread.fromJava(threads[i]).vmThreadLocals()) {
                    return true;
                }
            }
            return false;
        }
    }

    private static class TestPointerProcedure implements Pointer.Procedure {
        TestSafepointProcedure tsp;
        TestPointerProcedure( TestSafepointProcedure tsp){
            this.tsp = tsp;
        }
        public void run(Pointer threadLocals) {
            Safepoint.runProcedure(threadLocals, tsp);
        }

    }

    private static class TestSafepointProcedure implements Safepoint.Procedure {
        private Barrier safePoint;
        private Barrier resume;
        private Thread[] threads;

        TestSafepointProcedure(Thread[] threads) {
            this.threads = threads;
            safePoint = new Barrier(threads.length + 1);
        }

        void waitForSafePoint() {
            while (safePoint.getThreadCount() < threads.length) {
                Thread.yield();
            }
            for (int t = 0; t < threads.length; t++) {
                System.out.println("Thread " + threads[t].getName() + " stopped");
            }
            // release everyone
            safePoint.waitForRelease();
        }

        public void run(Pointer trapState) {
            // wait until all threads reach the safepoint
            safePoint.waitForRelease();
        }
    }

    /**
     * All threads wait at barrier until last thread arrives.
     */
    static class Barrier {
        private int threads;
        private int threadCount = 0;

        public Barrier(int threads) {
            this.threads = threads;
        }

        public synchronized void reset() {
            threadCount = 0;
        }

        /**
         * Get number of threads that have reached the barrier.
         * @return number of threads that have reached the barrier
         */
        public synchronized int getThreadCount() {
            return threadCount;
        }

        public synchronized void waitForRelease() {
            try {
                threadCount++;
                if (threadCount == threads) {
                    notifyAll();
                } else {
                    while (threadCount < threads) {
                        wait();
                    }
                }
            } catch (InterruptedException ex) {
            }
        }
    }

    private static MatchPredicate matchPredicate = new MatchPredicate();
    // force classes to be compiled into image

    static TestPointerProcedure __x = new TestPointerProcedure(new TestSafepointProcedure(new Thread[]{}));
    static Barrier __y =  new Barrier(0);

}
