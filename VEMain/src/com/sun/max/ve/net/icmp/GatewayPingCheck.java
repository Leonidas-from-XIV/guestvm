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
package com.sun.max.ve.net.icmp;

import java.util.*;

import com.sun.max.ve.net.*;
import com.sun.max.ve.net.debug.*;
import com.sun.max.ve.net.ip.*;


/**
* GatewayPingCheck is used to build a list of ICMP echo requests of gateways
* for which we are waiting for replies. This is used for dead gateway detection
* as per the Host Requirements RFC.
*/

public class GatewayPingCheck implements ICMPHandler {

    private static long PING_TIME = 60000L;    // time in milliseconds we wait for an ICMP_ECHOREQ to return.
    private static Map<Integer, GatewayPingCheck> _gatewayPingMap = Collections.synchronizedMap(new HashMap<Integer, GatewayPingCheck>());
    private int _router;                                         // the router being pinged
    private int _ident;                                           // the ident in the ICMP echo message
    private boolean _dead;                                 // true iff no reply before timeout
    private long _time;                                         // time ping sent
    private PingTimerTask _timerTask;
    private static Timer _timer;                          // handles all timeout tasks

    private GatewayPingCheck(int router) {
         if (_timer == null) {
            // not initialized yet
            _timer = new Timer("GatewayPingCheck", true);
        }

        _router = router;
        _dead = false;

        ICMP.registerHandler(router, this);
        sendAndScheduleTimer();
    }

    public static GatewayPingCheck create(int router) {
        return new GatewayPingCheck(router);
    }

    private class PingTimerTask extends TimerTask {
        public void run() {
            Route.markRouterDead(_router);

             _dead = true;
             // send out another ping.
             sendAndScheduleTimer();
        }
    }

    private void sendAndScheduleTimer() {
        _ident = ICMP.nextId();
        _time = System.currentTimeMillis();
        _gatewayPingMap.put(_router, this);
        // Start timer for PING_TIME milliseconds. If we don't get a reply in
        // that time we mark the gateway dead.
        _timerTask = new PingTimerTask();
        _timer.schedule(_timerTask, PING_TIME);
        ICMP.rawSendICMPEchoReq(_router, ICMP.defaultTimeout(), ICMP.defaultTTL(), _ident, 0);
    }

    public void handle(Packet pkt, int src_ip, int id, int seq) {
        GatewayPingCheck g = _gatewayPingMap.get(src_ip);
        if (g != null) {
            // Do we care that the id matches? I say not.
            // If the router was marked dead, then see if we can mark
            // it alive again.
            if (g._dead == true) {
                Route.markRouterAlive(g._router);
            }

            // Stop this ping's timer.
            _timerTask.cancel();

            // Remove from the map
            _gatewayPingMap.remove(src_ip);
        }
    }

    public static boolean isInList(int rtr) {
        return _gatewayPingMap.containsKey(rtr);
    }

    public static void dump() {
        Debug.println("Gateway Ping Requests");
        synchronized (_gatewayPingMap) {
            if (_gatewayPingMap.isEmpty())
                Debug.println(" < No Entries >");
            else {
                Iterator<?> iter = _gatewayPingMap.values().iterator();
                while (iter.hasNext()) {
                    GatewayPingCheck g = (GatewayPingCheck) iter.next();
                    Debug.println("       " + IPAddress.toString(g._router)
                            + ", id " + g._ident + ", time " + g._time
                            + ", dead " + g._dead);
                }
            }
        }
    }
}


