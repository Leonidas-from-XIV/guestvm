/*
 * Copyright (c) 2007 Sun Microsystems, Inc.  All rights reserved.
 *
 * Sun Microsystems, Inc. has intellectual property rights relating to technology embodied in the product
 * that is described in this document. In particular, and without limitation, these intellectual property
 * rights may include one or more of the U.S. patents listed at http://www.sun.com/patents and one or
 * more additional patents or pending patent applications in the U.S. and in other countries.
 *
 * U.S. Government Rights - Commercial software. Government users are subject to the Sun
 * Microsystems, Inc. standard license agreement and applicable provisions of the FAR and its
 * supplements.
 *
 * Use is subject to license terms. Sun, Sun Microsystems, the Sun logo, Java and Solaris are trademarks or
 * registered trademarks of Sun Microsystems, Inc. in the U.S. and other countries. All SPARC trademarks
 * are used under license and are trademarks or registered trademarks of SPARC International, Inc. in the
 * U.S. and other countries.
 *
 * UNIX is a registered trademark in the U.S. and other countries, exclusively licensed through X/Open
 * Company, Ltd.
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
    boolean isReachable0(byte[] addr, int scope, int timeout, byte[] inf, int ttl, int if_scope) throws IOException {
        unimplemented("lookupAllHostAddr");
        return false;
    }

    private static void unimplemented(String method) {
        GuestVMError.unimplemented("java.net.InetAddressImpl." + method);
    }
}
