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
package test.com.sun.guestvm.net;

/**
 * A test to demonstrate the impact of unresponsive network packet handlers.
 * Usage: [s s] [h h] [w w]
 * The test registers a network packet handler which, by default, sleeps for
 * s seconds (default 5) before returning. The main thread reports the dropped
 * packet count every second.
 * If h is set to 2 the handler blocks until the a waiting thread notifies it after w seconds (default 5).
 * Both of these scenarios will typically result in dropped packets.
 *
 * @author Mick Jordan
 */

import com.sun.guestvm.net.*;
import com.sun.guestvm.net.device.*;
import com.sun.guestvm.net.guk.*;

public class SlowNetDeviceHandlerTest implements NetDevice.Handler, Runnable {
    static int _sleepTime = 5000;
    static int _waitTime = 5000;
    static int _handler = 1;
    static long _now;
    static Object _lock = new Object();
    static boolean _waiting = true;

    public static void main(String[] args) throws InterruptedException {
        // Checkstyle: stop modified control variable check
       for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.equals("s")) {
                _sleepTime = Integer.parseInt(args[++i]);
            } else if (arg.equals("h")) {
                _handler = Integer.parseInt(args[++i]);
            } else if (arg.equals("w")) {
                _waitTime = Integer.parseInt(args[++i]);
            }
        }
       // Checkstyle: resume modified control variable check

        _now = System.currentTimeMillis();
        System.out.println("starting: handler sleep time: " + _sleepTime);
        final NetDevice device = GUKNetDevice.create();
        final SlowNetDeviceHandlerTest self = new SlowNetDeviceHandlerTest();
        device.registerHandler(self);
        if (_handler == 2) {
            final Thread notifier = new Thread(self);
            notifier.setDaemon(true);
            notifier.start();
        }

        while (true) {
            Thread.sleep(1000);
            System.out.println("drop counter @ " + relTime() + " : " + device.dropCount());
        }
    }

    static long relTime() {
        return System.currentTimeMillis() - _now;
    }

    public void handle(Packet packet) {
        System.out.println("handler entry @ " + relTime() + " : " + packet.length() + " bytes received");
        if (_handler == 1) {
            handlerBody1();
        } else if (_handler == 2) {
            handlerBody2();
        }
        System.out.println("handler exit @ " + relTime());
    }

    void handlerBody1() {
        try {
            Thread.sleep(_sleepTime);
        } catch (InterruptedException ex) {
            System.out.println("handler interrupted");
        }
    }

    void handlerBody2() {
        synchronized (_lock) {
            _waiting = true;
            while (_waiting) {
                try {
                    _lock.wait();
                    System.out.println("handler wait ended @ " + relTime());
                } catch (InterruptedException ex) {
                    System.out.println("handler interrupted");
                }
            }
        }
    }

    public void run() {
        for (;;) {
            try {
                Thread.sleep(_waitTime);
                synchronized (_lock) {
                    _waiting = false;
                    _lock.notify();
                }
            } catch (InterruptedException ex) {
            }
        }

    }

}
