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


public class InterruptTest  {

    /**
     * @param args
     */
    public static void main(String[] args) {
        boolean waitTest = false;
        boolean sleepTest = false;
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.equals("s")) {
                sleepTest = true;
            } else if (arg.equals("w")) {
                waitTest = true;
            }
        }
        if (waitTest) {
            final Thread waitInterruptee = new WaitInterruptee();
            waitInterruptee.setName("interruptee");
            waitInterruptee.start();
            waitInterruptee.interrupt();
            try {
                waitInterruptee.join();
            } catch (InterruptedException ex) {
                System.out.println("[" + Thread.currentThread().getName() + "] caught InterruptedException on join, status " + Thread.currentThread().isInterrupted());
            }
        }

        if (sleepTest) {
            final Thread sleepInterruptee = new SleepInterruptee();
            sleepInterruptee.setName("interruptee");
            sleepInterruptee.start();
            sleepInterruptee.interrupt();
            try {
                sleepInterruptee.join();
            } catch (InterruptedException ex) {
                System.out.println("[" + Thread.currentThread().getName() + "] caught InterruptedException on join, status " + Thread.currentThread().isInterrupted());
            }
        }
    }

    static class WaitInterruptee extends Thread {
        public void run() {
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                    System.out.println("[" + Thread.currentThread().getName() + "] caught InterruptedException on wait, status " + Thread.currentThread().isInterrupted());
                }
            }
        }
    }

    static class SleepInterruptee extends Thread {
        public void run() {
            synchronized (this) {
                try {
                    sleep(1000);
                } catch (InterruptedException ex) {
                    System.out.println("[" + Thread.currentThread().getName() + "] caught InterruptedException on sleep, status " + Thread.currentThread().isInterrupted());
                }
            }
        }
    }

}
