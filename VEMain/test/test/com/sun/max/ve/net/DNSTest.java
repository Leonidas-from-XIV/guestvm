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
 * Simple test of DNS lookup.
 * Usage: [r] name
 * If r absent lookup host name "name, else find hostname for ip address "name".
 *
 * @author Mick Jordan
 */

import com.sun.max.ve.net.dns.*;
import com.sun.max.ve.net.ip.*;

public class DNSTest {
    public static void main(String[] args) throws InterruptedException {
        final DNS dns = DNS.getDNS();
        if (dns == null) {
            System.out.println("no DNS: is network configured correctly?");
            System.exit(1);
        }
        String name = null;
        boolean reverse = false;
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.equals("r")) {
                reverse = true;
            } else {
                name = args[i];
            }
        }
        if (name != null) {
            if (reverse) {
                final String hostname = dns.reverseLookup(name);
                System.out.println("hostname for " + name + " is " + (hostname == null ? "not found" : hostname));
            } else {
                final IPAddress[] ipAddresses = dns.lookup(name);
                if (ipAddresses == null) {
                    System.out.println("host " + name + " not found");
                } else {
                    for (int i = 0; i < ipAddresses.length; i++) {
                        final IPAddress ipAddress = ipAddresses[i];
                        System.out.println(name + " has address " + ipAddress);
                    }
                }
            }
        }
    }
}
