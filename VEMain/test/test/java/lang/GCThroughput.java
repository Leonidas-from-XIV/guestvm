/*
 * Copyright (c) 2009, 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
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
