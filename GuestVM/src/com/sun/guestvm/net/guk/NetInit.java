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
package com.sun.guestvm.net.guk;

import com.sun.guestvm.net.*;
import com.sun.guestvm.net.debug.*;
import com.sun.guestvm.net.device.*;

public final class NetInit  extends Init implements Runnable {
    private static final String DROP_PROPERTY = "guestvm.net.dropreport";
    private static NetDevice _netDevice;
    private static int _dropReportInterval;
    private NetInit() {
        super(new NetDevice[] {create()});
        final String dropReport = System.getProperty(DROP_PROPERTY);
        if (dropReport != null) {
            _dropReportInterval = Integer.parseInt(dropReport);
            final Thread t = new Thread(this);
            t.setDaemon(true);
            t.start();
        }

    }

    public static NetInit init() {
        return new NetInit();
    }

    public void run() {
        while (true) {
            Debug.println("NetDevice: drop count " + _netDevice.dropCount());
            try {
                Thread.sleep(_dropReportInterval);
            } catch (InterruptedException ex) {
            }
        }
    }

    private static NetDevice create() {
        _netDevice = GUKNetDevice.create();
        return _netDevice;
    }
}
