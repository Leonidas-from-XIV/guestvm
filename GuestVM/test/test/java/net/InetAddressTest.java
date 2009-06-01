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
package test.java.net;

import java.net.InetAddress;

public class InetAddressTest {

    public static void main(String[] args) {
        try {
            // Checkstyle: stop modified control variable check
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (arg.equals("getAddress")) {

                } else if (arg.equals("getByName")) {
                    i++;
                    String addr = args[i];
                    InetAddress a = InetAddress.getByName(addr);
                    System.out.println("hostname " + a.getHostName() + ", IP address " + a.getHostAddress());
                } else if (arg.equals("getAllByName")) {
                    i++;
                    String addr = args[i];
                    InetAddress[] a = InetAddress.getAllByName(addr);
                    for (int j = 0; j < a.length; j++) {
                        System.out.println("hostname " + a[j].getHostName() + ", IP address " + a[j].getHostAddress());
                    }

                } else if (arg.equals("isReachable")) {
                    i++;
                    String addr = args[i];
                    InetAddress a = InetAddress.getByName(addr);
                    boolean b = a.isReachable(20000);
                    System.out.println("hostname " + a.getHostName() + " is " + (b ? "reachable" : "not reachable"));
                }
            }
            // Checkstyle: resume modified control variable check
        } catch (Exception ex) {
            System.out.println(ex);
        }

    }



}
