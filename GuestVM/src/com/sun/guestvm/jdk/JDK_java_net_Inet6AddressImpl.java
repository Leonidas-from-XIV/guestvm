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
package com.sun.guestvm.jdk;

import java.io.*;
import java.net.*;
import com.sun.max.annotate.*;
import com.sun.guestvm.error.*;

/**
 * GuestVM specific substitutions for @see java.net.Inet6AddressImpl.
 * SInce we do not implement ipv6 this class exists just to catch the case
 * where someone tries to set ipv6 as the default.
 *
 * @author Mick Jordan
 *
 */

@METHOD_SUBSTITUTIONS(hiddenClass = "java.net.Inet6AddressImpl")
public class JDK_java_net_Inet6AddressImpl {

    @SUBSTITUTE
    InetAddress[] lookupAllHostAddr(String hostname) throws UnknownHostException {
        unimplemented("lookupAllHostAddr");
        return null;
    }

    @SUBSTITUTE
    String getLocalHostName() throws UnknownHostException {
        unimplemented("lookupAllHostAddr");
        return null;
    }

    @SUBSTITUTE
    String getHostByAddr(byte[] addr) throws UnknownHostException {
        unimplemented("lookupAllHostAddr");
        return null;
    }

    @SUBSTITUTE
    boolean isReachable0(byte[] addr, int scope, int timeout, byte[] inf, int ttl, int ifScope) throws IOException {
        unimplemented("lookupAllHostAddr");
        return false;
    }

    private static void unimplemented(String method) {
        GuestVMError.unimplemented("java.net.InetAddressImpl." + method);
    }
}
