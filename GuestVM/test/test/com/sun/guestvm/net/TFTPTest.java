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
 * Test for TFTP.
 * Usage: s server f file
 * Prints the content of the file to the standard output.
 * server can be a hostname or an IP address
 *
 * @author Mick Jordan
 */
import java.io.*;

import com.sun.guestvm.net.*;
import com.sun.guestvm.net.dns.DNS;
import com.sun.guestvm.net.ip.IPAddress;
import com.sun.guestvm.net.tftp.*;

public class TFTPTest {
    public static void main(String[] args) {
        String serverName = null;
        String fileName = null;
        // Checkstyle: stop modified control variable check
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.equals("s")) {
                i++;
                serverName = args[i];
            } else if (arg.equals("f")) {
                i++;
                fileName = args[i];
            }
        }
        // Checkstyle: resume modified control variable check
        if (serverName == null || fileName == null) {
            System.out.println("usage: s server f filename");
        } else {
            final DNS dns = Init.getDNS();
            IPAddress ipAddress = null;
            try {
                ipAddress = IPAddress.parse(serverName);
            } catch (NumberFormatException ex) {
                ipAddress = dns.lookupOne(serverName);
            }
            if (ipAddress == null) {
                System.out.println("server " + serverName + " not found");
            } else {
                final TFTP.Client tftp = new TFTP.Client(ipAddress);
                final byte[] buffer = new byte[4096];
                try {
                    final int bytesRead = tftp.readFile(fileName, buffer);
                    System.out.println("read " + bytesRead + " bytes");
                    for (int i = 0; i < bytesRead; i++) {
                        System.out.print(buffer[i]);
                    }
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }

            }
        }
    }
}
