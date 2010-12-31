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
 * A simple test for handler registration with the network device driver.
 * Usage: [s n]
 * Reports packet delivery and number of bytes received.
 * The main thread sleeps for n seconds (default 5), reporting
 * the number of dropped packets.
 *
 * @author Mick Jordan
 */
import com.sun.max.ve.net.*;
import com.sun.max.ve.net.device.*;
import com.sun.max.ve.net.guk.*;

public class NetDeviceHandlerTest  {
    private static int _sleepTime = 5000;
    public static void main(String[] args) throws InterruptedException {
        // Checkstyle: stop modified control variable check
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.equals("s")) {
                _sleepTime = Integer.parseInt(args[++i]);
            }
        }
        // Checkstyle: resume modified control variable check
        new NetDeviceHandlerTest().run();
    }

    public void run() throws InterruptedException {
        final NetDevice nd = GUKNetDevice.create();
        nd.registerHandler(new Handler());
        while (true) {
            Thread.sleep(_sleepTime);
            System.out.println("drop count " + nd.dropCount());
        }
    }

    static class Handler implements NetDevice.Handler {
        public void handle(Packet packet) {
            System.out.println("" + packet.length() + " bytes received");
        }
    }
}
