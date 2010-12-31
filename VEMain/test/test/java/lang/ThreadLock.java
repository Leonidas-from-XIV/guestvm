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

/**
 * A class that can be used to understand, using the Inspector, exactly what happens
 * when multiple threads contend for a lock.
 *
 * @author Mick Jordan
 *
 */

public class ThreadLock {

    private static int _sleeptime = 0;
    public static void main(String[] args) {
        // Checkstyle: stop modified control variable check
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("s")) {
                _sleeptime = Integer.parseInt(args[++i]);
            }
        }
        // Checkstyle: resume modified control variable check
        final Thread t1 = new Thread(new Locker(1));
        final Thread t2 = new Thread(new Locker(2));
        final Thread t3 = new Thread(new Locker(3));
        t1.start();
        t2.start();
        t3.start();
    }

    static class Locker  implements Runnable {
        private static Object _lock = new Object();
        private int _id;
        Locker(int i) {
            _id = i;
        }
        public void run() {
            System.out.println("Locker " + _id + " going for lock");
            synchronized (_lock) {
                System.out.println("Locker " + _id + " got lock");
                try {
                    Thread.sleep(_sleeptime * 1000);
                } catch (InterruptedException ex) {

                }
            }
        }

    }

}
