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
 * A test of the ARP protocol handler.
 * Usage: ip address [reap r] [tick n] [t]
 *
 * reap explicitly sets the cache reap interval
 * tick explicitly sets the ARP cache entry timeout
 * t runs the check for addr as a separate thread
 *
 * @author Mick Jordan
 */

import java.util.*;
import com.sun.max.ve.net.*;
import com.sun.max.ve.net.arp.*;
import com.sun.max.ve.net.device.*;
import com.sun.max.ve.net.guk.GUKNetDevice;
import com.sun.max.ve.net.ip.*;
import com.sun.max.ve.net.protocol.ether.*;

public class ARPTest  implements Runnable {
    private static ARP _arp;

    private IPAddress _ipAddress;
    ARPTest(IPAddress ipAddress) {
        _ipAddress = ipAddress;
    }

    public void run() {
        _arp.checkForIP(_ipAddress.addressAsInt(), 4);
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws InterruptedException {
        final List<String> ipAddressStrings = new ArrayList<String>();
        int reapInterval = 0;
        int ticks = 0;
        boolean threads = false;
        // Checkstyle: stop modified control variable check
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.equals("ip")) {
                ipAddressStrings.add(args[++i]);
            } else if (arg.equals("reap")) {
                reapInterval = Integer.parseInt(args[++i]);
            } else if (arg.equals("tick")) {
                ticks = Integer.parseInt(args[++i]);
            } else if (arg.equals("t")) {
                threads = true;
            }
        }
        // Checkstyle: resume modified control variable check
        final NetDevice device = GUKNetDevice.create();
        final Ether ether = new Ether(device);
        Init.checkConfig();
        if (reapInterval > 0) {
            ARP.setCacheReapInterval(reapInterval);
        }
        if (ticks > 0) {
            ARP.setCacheEntryTimeout(ticks);
        }

        _arp = ARP.getARP(ether);
        IP.init(Init.getLocalAddress().addressAsInt(), Init.getNetMask().addressAsInt());
        ether.registerHandler(_arp, "ARP");
        for (String ipAddressString : ipAddressStrings) {
            if (threads) {
                new Thread(new ARPTest(IPAddress.parse(ipAddressString))).start();
            } else {
                new ARPTest(IPAddress.parse(ipAddressString)).run();
            }
        }
        while (true) {
            Thread.sleep(10000);
            System.out.println("ARP Cache");
            final ARP.CacheEntry[] entries = _arp.getArpCache();
            for (ARP.CacheEntry entry : entries) {
                System.out.println("  " + entry.getIPAddress() + " : " + Ether.addressToString(entry.getEthAddress()));
            }
        }
    }
}

