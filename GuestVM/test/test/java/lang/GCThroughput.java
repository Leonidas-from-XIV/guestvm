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


public class GCThroughput implements Runnable {

    private static int _runTime = 30;
    private static int _reportFrequency = 5;
    private static volatile boolean _done = false;
    private static volatile long _tpt;
   /**
     * @param args
     */
    public static void main(String[] args) {

        // Checkstyle: stop modified control variable check
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.equals("t")) {
                _runTime = Integer.parseInt(args[++i]);
            }
        }
        // Checkstyle: resume modified control variable check
        new Thread(new GCThroughput()).start();
        while (!_done) {
            @SuppressWarnings("unused")
            final Object[] o = new Object[1024];
            _tpt++;
        }
        System.out.println("Average TPS: " + _tpt / _runTime);
    }

    public void run() {
        long runTime = 0;
        long tpt = 0;
        while (runTime < _runTime) {
            try {
                Thread.sleep(_reportFrequency * 1000);
                runTime += _reportFrequency;
                System.out.println("Interval TPS: " + (_tpt - tpt) / _reportFrequency);
                tpt = _tpt;
            } catch (InterruptedException ex) {
                runTime = _runTime;
            }
        }
        _done = true;
    }

}
