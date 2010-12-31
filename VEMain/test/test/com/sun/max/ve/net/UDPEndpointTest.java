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
 * Test for reading/writing to UDPEndPoints
 * Usage port p [timeout t] [write] [noread] [dest host] [readthread] [writedelay w] [runtime r]
 * By default reads from port p on host (default local host) in main thread.
 * noread suppresses reading and write writes to port with optional delay w (default 1s).
 * readthread puts reader in new thread, allowing concurrent reading/writing.
 * runtime sets the time of the test (default 30s).
 * timeout sets read timeout to t (default 10s).
 *
 * @author Mick Jordan
 */
import java.io.*;
import com.sun.max.ve.net.*;
import com.sun.max.ve.net.dns.DNS;
import com.sun.max.ve.net.ip.IPAddress;
import com.sun.max.ve.net.udp.*;

public class UDPEndpointTest implements Runnable {

    private static int _port = 0;
    private static int _timeout = 10000;
    private static int _destAddr;
    private static int _writeDelay = 1000;
    private static long _runtime = 30000;

    public static void main(String[] args) throws Exception {
        boolean reading = true;
        boolean writing = false;
        boolean readThread = false;
        _destAddr = Init.getLocalAddress().addressAsInt();
        // Checkstyle: stop modified control variable check
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.equals("port")) {
                _port = Integer.parseInt(args[++i]);
            } else if (arg.equals("timeout")) {
                _timeout = Integer.parseInt(args[++i]);
            } else if (arg.equals("write")) {
                writing = true;
            } else if (arg.equals("noread")) {
                reading = false;
            } else if (arg.equals("dest")) {
                final String host = args[++i];
                final IPAddress[] ipAddresses = DNS.getDNS().lookup(host);
                if (ipAddresses == null) {
                    throw new Exception("host " + host + " not found");
                } else {
                    _destAddr = ipAddresses[0].addressAsInt();
                }
            } else if (arg.equals("readthread")) {
                readThread = true;
            } else if (arg.equals("writedelay")) {
                _writeDelay = Integer.parseInt(args[++i]);
            } else if (arg.equals("runtime")) {
                _runtime = Integer.parseInt(args[++i]);
            }
        }
        // Checkstyle: resume modified control variable check

        if (reading) {
            if (readThread) {
                final Thread th = new Thread(new UDPEndpointTest());
                th.start();
            } else {
                read();
            }
        }
        if (writing) {
            write();
        }
    }

    public void run() {
        read();
    }

    private static void read() {
        final UDPEndpoint ep = new UDPEndpoint();
        final byte[] buf = new byte[4096];
        final long endtime = System.currentTimeMillis() + _runtime;
        try {
            ep.bind(0, _port, false);
            ep.setTimeout(_timeout);
            while (System.currentTimeMillis() < endtime) {
                try {
                    final int n = ep.read(buf, 0, buf.length);
                    System.out.println("read " + n + " bytes");
                } catch (IOException ex) {
                    System.out.println(ex);
                    break;
                }
            }
        } catch (IOException ex) {
            System.out.println(ex);
        }

    }

    private static void write() {
        final UDPEndpoint ep = new UDPEndpoint();
        final byte[] buf = new byte[1024];
        final long endtime = System.currentTimeMillis() + _runtime;
        try {
            ep.connect(_destAddr, _port);
            while (System.currentTimeMillis() < endtime) {
                try {
                    ep.write(buf, 0, buf.length);
                    System.out.println("wrote " + buf.length + " bytes");
                    if (_writeDelay != 0) {
                        try {
                            Thread.sleep(_writeDelay);
                        } catch (InterruptedException ex) {

                        }
                    }
                } catch (IOException ex) {
                    System.out.println(ex);
                    break;
                }
            }
        } catch (IOException ex) {
            System.out.println(ex);
        }

    }


}
