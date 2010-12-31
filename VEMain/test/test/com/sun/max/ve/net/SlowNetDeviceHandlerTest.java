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
package test.com.sun.max.ve.net;

/**
 * A test to demonstrate the impact of slow response network packet handlers.
 * Usage: [d runtime] [s sleeptime] [r] [c] [t]
 * The test, which runs for "runtime" seconds (default 30), registers a network packet handler which, by default, sleeps for
 * "sleeptime" milliseconds (default 5000) before returning.  This scenario will typically result in dropped packets by
 * the network driver as not enough handlers will be available for all incoming packets.
 * If r is set, the sleep is for a random time up to sleeptime.
 * If c is set a compute-bound thread is run in parallel, which can be used to see the handler thread schedule latency.
 * If t is set, scheduler tracing is turned on (also requires -XX:GUKTrace to be set on guest launch).
 *
 * The main thread reports the dropped packet count every second.
 *
 * @author Mick Jordan
 */

import java.util.*;

import com.sun.max.ve.guk.GUKTrace;
import com.sun.max.ve.net.*;
import com.sun.max.ve.net.device.*;
import com.sun.max.ve.net.guk.*;

public class SlowNetDeviceHandlerTest implements NetDevice.Handler, Runnable {
    static int _sleepTime = 5000;
    static long _startTime;
    static Object _lock = new Object();
    static boolean _waiting = true;
    static boolean _randomSleep = false;
    static Random _random = new Random();
    static long _endTime;
    static boolean _done;
    private static final long SEC_TO_NANO = 1000 * 1000 * 1000;

    public static void main(String[] args) throws InterruptedException {
        boolean compute = false;
        boolean trace = false;
        long duration  = 30;
        // Checkstyle: stop modified control variable check
       for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.equals("s")) {
                _sleepTime = Integer.parseInt(args[++i]);
            } else if (arg.equals("r")) {
                _randomSleep = true;
            } else if (arg.equals("c")) {
                compute = true;
            } else if (arg.equals("t")) {
                trace = true;
            } else if (arg.equals("d")) {
                duration = Integer.parseInt(args[++i]);
            }
        }
       // Checkstyle: resume modified control variable check

        if (trace) {
            GUKTrace.setTraceState(GUKTrace.Name.SCHED, true);
        }
        _startTime = System.nanoTime();
        _endTime = _startTime + duration * SEC_TO_NANO;
        System.out.println("starting: handler sleep time: " + _sleepTime);
        final NetDevice device = GUKNetDevice.create();
        final SlowNetDeviceHandlerTest self = new SlowNetDeviceHandlerTest();
        device.registerHandler(self);

        if (compute) {
            final Thread spinner = new Thread(new SlowNetDeviceHandlerTest());
            spinner.setName("Spinner");
            spinner.setDaemon(true);
            spinner.start();
        }

        while (!_done) {
            Thread.sleep(1000);
            tprintln("drop counter: " + device.dropCount());
        }
    }

    static long relTime() {
        final long now = System.nanoTime();
        if (now >= _endTime) {
            _done = true;
        }
        return now - _startTime;
    }

    public void handle(Packet packet) {
        final int sleepTime = _randomSleep ? _random.nextInt(_sleepTime) : _sleepTime;
        tprintln("handler entry: ttp: " + (System.nanoTime() - packet.getTimeStamp()) + " : len: " + packet.length() + " sleep: " + sleepTime);
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException ex) {
            tprintln("handler interrupted");
        }
        tprintln("handler exit");
    }

    public void run() {
        long x = 0;
        while (true && !_done) {
            x++;
        }
    }

    static void tprintln(String msg) {
        System.out.println(Thread.currentThread().getName() + ": @" + relTime() + ": " + msg);
    }

}
