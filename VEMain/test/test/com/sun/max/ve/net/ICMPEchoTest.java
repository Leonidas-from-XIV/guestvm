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

/** Long name for "ping".
 * Usage: [reverse] [repeat n] [onethread] host
 * If reverse is set host is expected to be an IP address else a host name
 * If repeat is not given a one-shot ping is made to host for a matching reply.
 * If repeat is given and onethread is not, a sequence of n pings are made
 * in the same thread matching any reply. If onethread is given, each ping
 * is done in a separate thread and expects  a matching reply.
 *
 * @author Mick Jordan
 */
import com.sun.max.ve.net.dns.*;
import com.sun.max.ve.net.icmp.ICMP;
import com.sun.max.ve.net.ip.*;

public class ICMPEchoTest implements Runnable {

    private static int _repeat = -1;
    /**
     * @param args
     */
    public static void main(String[] args) {
        final DNS dns = DNS.getDNS();
        String name = null;
        boolean reverse = false;
        boolean oneThreadPerPing = false;
        // Checkstyle: stop modified control variable check
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.charAt(0) == '-') {
                arg = arg.substring(1);
            }
            if (arg.equals("reverse")) {
                reverse = true;
            } else if (arg.equals("repeat")) {
                _repeat = Integer.parseInt(args[++i]);
            } else if (arg.equals("onethread")) {
                oneThreadPerPing = true;
            } else {
                name = args[i];
            }
        }
        // Checkstyle: resume modified control variable check
        if (name != null) {
            IPAddress ipAddress;
            if (!reverse) {
                final IPAddress[] ipAddresses = dns.lookup(name);
                if (ipAddresses == null) {
                    System.out.println("host " + name + " not found");
                    return;
                }
                ipAddress = ipAddresses[0];
            } else {
                ipAddress = IPAddress.parse(name);
                name = dns.reverseLookup(ipAddress);
                if (name == null) {
                    System.out.println("no name found for " + ipAddress.toString());
                    name = "no name";
                }
            }
            if (_repeat > 0) {
                final int id = ICMP.nextId();
                int seq = 0;
                if (!oneThreadPerPing) {
                    final Thread thread = new Thread(new ICMPEchoTest(name, ipAddress, id, seq, false));
                    thread.setName("ICMPEchoTest");
                    thread.start();
                    try {
                        thread.join();
                    } catch (InterruptedException ex) {
                    }
                } else {
                    while (_repeat > 0) {
                        new Thread(new ICMPEchoTest(name, ipAddress, id, seq++, true)).start();
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                        }
                        _repeat--;
                    }
                }
            } else {
                final int response = ICMP.doSeqMatchingICMPEchoReq(ipAddress, ICMP.nextId(), 0);
                if (response >= 0) {
                    System.out.println(name + " is alive");
                } else {
                    System.out.println("no response from " + name);
                }
            }
        }
    }

    private String _name;
    private IPAddress _ipAddress;
    private int _id;
    private int _seq;
    private boolean _oneShot;

    public ICMPEchoTest(String name, IPAddress ipAddress, int id, int seq, boolean oneShot) {
        _name = name;
        _ipAddress = ipAddress;
        _id = id;
        _seq = seq;
        _oneShot = oneShot;
    }

    public void run() {
        while (_repeat > 0) {
            final long startTime = System.nanoTime();
            int response;
            if (_oneShot) {
                response = ICMP.doSeqMatchingICMPEchoReq(_ipAddress, _id, _seq);
            } else {
                response = ICMP.doICMPEchoReq(_ipAddress, _id, _seq);
            }
            if (response >= 0) {
                final long duration = System.nanoTime() - startTime;
                System.out.println("response from " + _name + ": icmp_seq=" + response + ". time=" + duration + " ns");
            }
            if (_oneShot) {
                return;
            }
            _seq++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
            _repeat--;
        }
    }
}
