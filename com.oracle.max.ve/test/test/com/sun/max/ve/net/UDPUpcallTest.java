/*
 * Copyright (c) 2009, 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package test.com.sun.max.ve.net;

/**
 * Simple test for UDP upcalls.
 * Usage: port port
 */
import com.sun.max.ve.net.Packet;
import com.sun.max.ve.net.udp.*;

public class UDPUpcallTest implements UDPUpcall {

    /**
     * @param args
     */
    public static void main(String[] args) {
        int port = 0;
        // Checkstyle: stop modified control variable check
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.equals("port")) {
                port = Integer.parseInt(args[++i]);
            }
        }
        // Checkstyle: resume modified control variable check
        final UDPUpcallTest obj = new UDPUpcallTest();
        final int rport = UDP.register(obj, port, false);
        System.out.println("UDP.register returned " + rport);
        if (rport == 0) {
            return;
        } else {
            synchronized (obj) {
                try {
                    obj.wait();  // forever
                } catch (InterruptedException ex) {
                }
            }
        }

    }

    public void input(Packet pkt) {
        System.out.println("packet received");
    }

}
