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
package test.com.sun.guestvm.spinlock;

import com.sun.guestvm.spinlock.*;

/**
 * Simple test to measure spinlock performance.
 * A set of threads contend for a shared spinlock.
 * The work is represented by incrementing a per-thread counter
 * a variable number of times while the lock is held.
 *
 * Args: [t nt] [r nr] [f nf] [k klass]
 *
 * t: number of threads
 * r: runtime in seconds
 * f: number of times counter is incremented while lock is held
 * k: spin lock class to instantiate (omitting com.sun.guestvm.spinlock prefix)
 *
 * @author Mick Jordan
 *
 */
public class SpinLockTest implements Runnable {

    private static int _runTime = 10000;
    private static final String SPINLOCK_PKG = "com.sun.guestvm.spinlock.";
    private static boolean _done;
    private static SpinLock _spinLock;

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        int nThreads = 2;
        int factor = 1;
        String klass = "ukernel.UKernelSpinLock";
        // Checkstyle: stop modified control variable check
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.equals("t")) {
                nThreads = Integer.parseInt(args[++i]);
            } else if (arg.equals("r")) {
                _runTime = Integer.parseInt(args[++i]) * 1000;
            } else if (arg.equals("c")) {
                klass = args[++i];
            } else if (arg.equals("f")) {
                factor = Integer.parseInt(args[++i]);
            }
        }
        // Checkstyle: resume modified control variable check

        final SpinLockFactory factory = (SpinLockFactory) Class.forName(SPINLOCK_PKG + klass + "Factory").newInstance();
        SpinLockFactory.setInstance(factory);
        _spinLock = SpinLockFactory.createAndInit();

        final Spinner[] threads = new Spinner[nThreads];
        for (int t = 0; t < nThreads; t++) {
            threads[t] = new Spinner(factor);
        }

        new Thread(new SpinLockTest()).start();

        for (int t = 0; t < nThreads; t++) {
            threads[t].start();
        }

        for (int t = 0; t < nThreads; t++) {
            threads[t].join();
        }
        long totalCount = 0;

        for (int t = 0; t < nThreads; t++) {
            System.out.println("thread " + t + ": " + threads[t].counter());
            totalCount += threads[t].counter();
        }
        System.out.println("total count: " + totalCount);

        if (_spinLock instanceof CountingSpinLock) {
            final CountingSpinLock cSpinLock = (CountingSpinLock) _spinLock;
            System.out.println("max spin count: " + cSpinLock.getMaxSpinCount());
        }

        _spinLock.cleanup();
    }

    public void run() {
        try {
            Thread.sleep(_runTime);
        } catch (InterruptedException ex) {

        }
        _done = true;
    }

    static class Spinner extends Thread {
        private long _counter;
        private int _factor;

        Spinner(int factor) {
            _factor = factor;
        }

        public void run() {
            while (!_done) {
                _spinLock.lock();
                int f = _factor;
                while (f > 0) {
                    _counter++;
                    f--;
                }
                _spinLock.unlock();
            }
        }

        public long counter() {
            return _counter;
        }

    }

}
