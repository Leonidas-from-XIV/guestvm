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
package test.com.sun.max.ve.spinlock;

import com.sun.max.ve.spinlock.*;

/**
 * A test for whether spin locks prevent pre-emptive scheduling.
 * The Looper thread is started and waits for the Spinner, then loops asserting that the spinner is not running.
 * The Spinner grabs the spin lock and loops for the given running time and then releases the lock.
 * If the Spinner is pre-empted, the Looper will run and output a message to that effect.
 *
 * N.B. You must run this test with VCPUS == 1!
 *
 * The P variety of spinlocks, e.g., com.sun.max.ve.spinlock.tas.p.PTTASSpinLock
 * should produce the message All others should not.
 *
 * @author Mick Jordan
 *
 */
public class SpinLockPETest {
    private static int _runTime = 10000;
    private static final String SPINLOCK_PKG = "com.sun.max.ve.spinlock.";
    private static volatile boolean _spinnerRunning;
    private static SpinLock _spinLock;


    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        String klass = "ukernel.UKernelSpinLock";
        // Checkstyle: stop modified control variable check
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.equals("r")) {
                _runTime = Integer.parseInt(args[++i]) * 1000;
            } else if (arg.equals("c")) {
                klass = args[++i];
            }
        }
        // Checkstyle: resume modified control variable check

        final SpinLockFactory factory = (SpinLockFactory) Class.forName(SPINLOCK_PKG + klass + "Factory").newInstance();
        SpinLockFactory.setInstance(factory);
        _spinLock = SpinLockFactory.createAndInit();

        final Thread looper =  new Looper();
        looper.setDaemon(true);
        looper.start();
        final Spinner spinner = new Spinner();
        spinner.start();
        spinner.join();
    }

    static class Looper extends Thread {

        public void run() {
            // wait for spinner to start;
            while (!_spinnerRunning) {
                continue;
            }
            // the only way we could get here is if the spinner is pre-empted or done.
            while (true) {
                if (_spinnerRunning) {
                    System.out.println("Looper running while Spinner holds lock");
                    break;
                }
            }
        }
    }

    static class Spinner extends Thread {
        public void run() {
            final long start = System.currentTimeMillis();
            long now = start;
            _spinLock.lock();
            _spinnerRunning = true;
            while (now < start + _runTime) {
                now = System.currentTimeMillis();
            }
            _spinnerRunning = false;
            _spinLock.unlock();
        }

    }

}
