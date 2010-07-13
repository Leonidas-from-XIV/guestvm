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
package test.com.sun.guestvm.thread;

import java.util.Random;
import com.sun.guestvm.test.*;

/**
 * A program to test safepoints; most of the key code is in {@link VmThreadTestHelper}.
 * Creates n threads that spin until told not to, then repeatedly stops a random subset of them.
 * Args:
 * t n     create n spinners (default 1)
 * d      mark spinners daemons (default false)
 * n n    do n iterations
 * s n    sleep for n milliseconds between iterations
 *
 * @author Mick Jordan
 *
 */

public class SafepointTest {
    private static volatile boolean[] running;

    public static void main(String[] args) {
        int runs = 10;
        int threads = 1;
        final Random rand = new Random(47679);
        boolean daemon = false;
        int sleep = 0;
        Spinner[] spinners;

        // Checkstyle: stop control variable check
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.equals("t")) {
                threads = Integer.parseInt(args[++i]);
            } else if (arg.equals("n")) {
                runs = Integer.parseInt(args[++i]);
            } else if (arg.equals("d")) {
                daemon = true;
            } else if (arg.equals("s")) {
                sleep = Integer.parseInt(args[++i]);
            }
        }
        running = new boolean[threads];
        spinners = new Spinner[threads];
        for (int t = 0; t < threads; t++) {
            spinners[t] = new Spinner(t);
            spinners[t].setDaemon(daemon);
            spinners[t].start();
            while (!running[t]) {
                Thread.yield();
            }
        }

        for (int i = 0; i < runs; i++) {
            // safepoint a random number of spinners
            final int n = rand.nextInt(threads) + 1;
            final Thread[] safepointees = new Thread[n];
            System.out.print("Stopping spinners:");
            for (int t = 0; t < n; t++) {
                int sx = -1;
                boolean clash;
                do {
                    clash = false;
                    sx = rand.nextInt(threads);
                    // check new
                    for (int j = 0; j < t; j++) {
                        if (safepointees[j] == spinners[sx]) {
                            clash = true;
                            break;
                        }
                    }
                } while (clash);
                safepointees[t] = spinners[sx];
                System.out.print(" " + sx);
            }
            System.out.println();
            VmThreadTestHelper.runSafepointProcedure(safepointees);
            if (sleep > 0) {
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException ex) {
                }
            }
        }
        if (!daemon) {
            for (int t = 0; t < threads; t++) {
                running[t] = false;
            }
        }
    }

    private static class Spinner extends Thread {
        private int me;
        Spinner(int i) {
            me = i;
            setName("Spinner-" + i);
        }
        public void run() {
            running[me] = true;
            int count = 0;
            while (running[me]) {
                count++;
            }
        }
    }



}
