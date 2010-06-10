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
package test.util;

import java.net.*;
import java.io.*;

public class TCPListener extends Thread {

    private static int _port = 10000;

    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.equals("port")) {
                _port = Integer.parseInt(args[++i]);
            }
        }
        new TCPListener().start();
    }

    public void run() {
        ServerSocket server = null;
        Socket sock = null;
        InputStream in = null;
        try {
            server = new ServerSocket(_port);
            for (;;) {
                try {
                    sock = server.accept();
                    System.out.println("connection accepted on " + sock.getLocalPort() + " from " + sock.getInetAddress());
                    in = sock.getInputStream();
                    for (;;) {
                        final int data = in.read();
                        if (data < 0) {
                            break;
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    if (in != null) {
                        in.close();
                    }
                    if (sock != null) {
                        sock.close();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}
