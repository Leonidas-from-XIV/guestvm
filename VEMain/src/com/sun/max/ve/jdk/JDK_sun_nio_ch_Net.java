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
package com.sun.max.ve.jdk;

import java.io.*;
import java.net.*;
import com.sun.max.annotate.*;
import com.sun.max.ve.error.*;
import com.sun.max.ve.logging.*;
import com.sun.max.ve.net.*;
import com.sun.max.ve.net.ip.IPAddress;
import com.sun.max.ve.net.tcp.*;
import com.sun.max.ve.net.udp.*;

/**
 * Substitutions for native methods in sun.nio.ch.Net.
 *
 * @author Mick Jordan
 *
 */

@SuppressWarnings("unused")

@METHOD_SUBSTITUTIONS(className = "sun.nio.ch.Net")
final  class JDK_sun_nio_ch_Net {

    private static final int SOCK_STREAM = 2;
    private static final int SOCK_DGRAM = 1;

    @SUBSTITUTE
    private static int socket0(boolean stream, boolean reuse) throws IOException {
        int result = -1;
        // TODO reuse
        if (stream) {
            result = JavaNetUtil.getFreeIndex(new TCPEndpoint());
        } else {
            result = JavaNetUtil.getFreeIndex(new UDPEndpoint());
        }
        return result;
    }

    @SUBSTITUTE
    private static void bind(FileDescriptor fd, InetAddress addr, int port) throws IOException {
        final Endpoint endpoint = JavaNetUtil.get(fd);
        endpoint.bind(IPAddress.byteToInt(addr.getAddress()), port, false);
    }

    @SUBSTITUTE
    private static int connect(FileDescriptor fd, InetAddress remote, int remotePort, int trafficClass) throws IOException {
        final Endpoint endpoint = JavaNetUtil.get(fd);
        endpoint.connect(IPAddress.byteToInt(remote.getAddress()), remotePort);
        return 1;
    }

    @SUBSTITUTE
    private static int localPort(FileDescriptor fd) {
        final Endpoint endpoint = JavaNetUtil.get(fd);
        return endpoint.getLocalPort();
    }

    @SUBSTITUTE
    private static InetAddress localInetAddress(FileDescriptor fd) {
        final Endpoint endpoint = JavaNetUtil.get(fd);
        final int ra = endpoint.getLocalAddress();
        assert ra == 0;
        return JDK_java_net_Inet4AddressImpl.createInet4Address("0.0.0.0", ra);
    }

    @SUBSTITUTE
    private static int getIntOption0(FileDescriptor fd, int opt) {
        VEError.unimplemented("sun.nio.net.getIntOption0");
        return 0;
    }

    @SUBSTITUTE
    private static void setIntOption0(FileDescriptor fd, int opt, int arg) throws IOException {
        Logger.getLogger("sun.nio.ch.Net").warning("option: " + opt + " not implemented");
    }

    @SUBSTITUTE
    private static void initIDs() {
    }


}
