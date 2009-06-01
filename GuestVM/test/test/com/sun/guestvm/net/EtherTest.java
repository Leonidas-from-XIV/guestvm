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
 * A simple test of the functionality of the Ethernet protocol handler.
 * Set -Dguestvm.net.protocol.ether.Ether.debug and/or -Dguestvm.net.protocol.ether.Ether.dump
 * to see more output.
 *
 * @author Mick Jordan
 */

import com.sun.guestvm.net.device.*;
import com.sun.guestvm.net.guk.GUKNetDevice;

public class EtherTest {
    private static final int RUNTIME = 10; // seconds
    public static void main(String[] args) throws InterruptedException {
        int runTime = RUNTIME;
        // Checkstyle: stop modified control variable check
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("t")) {
                runTime = Integer.parseInt(args[++i]);
            }
        }
        // Checkstyle: resume modified control variable check
        final NetDevice device = GUKNetDevice.create();
        device.registerHandler(new com.sun.guestvm.net.protocol.ether.Ether(device));
        long now = System.currentTimeMillis();
        final long end = now + runTime * 1000;
        while (now < end) {
            Thread.sleep(1000);
            System.out.println("drop count " + device.dropCount());
            now = System.currentTimeMillis();
        }
    }

}
