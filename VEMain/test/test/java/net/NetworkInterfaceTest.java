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

import java.util.*;
import java.net.*;

public class NetworkInterfaceTest {

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
       // Checkstyle: stop modified control variable check
       for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("getNetworkInterfaces")) {
                getNetworkInterfaces();
            } else if (arg.equals("getByInetAddress")) {
                getByInetAddress(args[++i]);
            } else if (arg.equals("isUp")) {
                isUp(args[++i]);
            } else if (arg.equals("getByName")) {
                getByName(args[++i]);
            } else if (arg.equals("getHardwareAddress")) {
                getHardwareAddress(args[++i]);
            }
        }
       // Checkstyle: resume modified control variable check
    }

    private static void getNetworkInterfaces() throws Exception {
        final Enumeration<NetworkInterface> intfsEnum = NetworkInterface.getNetworkInterfaces();
        System.out.println("Network interfaces:");
        while (intfsEnum.hasMoreElements()) {
            final NetworkInterface intf = intfsEnum.nextElement();
            System.out.println("  " + intf.getName() + ", " + intf.getDisplayName());
            final Enumeration<InetAddress> addrEnum = intf.getInetAddresses();
            System.out.println("  InetAddresses:");
            while (addrEnum.hasMoreElements()) {
                final InetAddress inetAddress = addrEnum.nextElement();
                System.out.println("    " + inetAddress.getHostAddress() + ", " + inetAddress.getHostName());
            }
        }
    }

    private static void getByInetAddress(String addr) throws Exception {
        final InetAddress inetAddress = InetAddress.getByName(addr);
        System.out.println("getByInetAddress(" + addr + ") = " + NetworkInterface.getByInetAddress(inetAddress));
    }

    private static void getByName(String arg) throws Exception {
        System.out.println("getByName(" + arg + ") = " + NetworkInterface.getByName(arg));
    }

    private static void isUp(String arg) throws Exception {
        final NetworkInterface networkInterface = NetworkInterface.getByName(arg);
        System.out.println("isUp(" + arg + ") = " + networkInterface.isUp());
    }

    private static void getHardwareAddress(String arg) throws Exception {
        System.out.print("getHardwareAddress(" + arg + ") = ");
        final byte[] b = NetworkInterface.getByName(arg).getHardwareAddress();
        for (int i = 0; i < b.length; i++) {
            if (i != 0) {
                System.out.print(":");
            }
            System.out.print(Integer.toHexString(b[i] & 0xFF));
        }
        System.out.println("");
    }

}
