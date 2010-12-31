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

import static com.sun.max.ve.jdk.AliasCast.*;

import java.net.*;
import java.io.*;

import com.sun.max.annotate.*;
import com.sun.max.ve.net.*;
import com.sun.max.ve.net.dns.*;
import com.sun.max.ve.net.icmp.ICMP;
import com.sun.max.ve.net.ip.IPAddress;
import com.sun.max.ve.unsafe.*;
import com.sun.max.vm.actor.holder.ClassActor;
import com.sun.max.vm.heap.Heap;

/**
 * MaxVE specific substitutions for @see java.net.Inet4AddressImpl.
 *
 * @author Mick Jordan
*/

@METHOD_SUBSTITUTIONS(className = "java.net.Inet4AddressImpl")
final class JDK_java_net_Inet4AddressImpl {

    @ALIAS(declaringClass = java.net.Inet4Address.class, name="<init>")
    private native void init(String hostname, int ipAddr);
    
    static Inet4Address createInet4Address(String hostname, int ipAddr) {
        // Use the ALIAS mechanism to avoid reflection
        final Inet4Address inet4Address = UnsafeCast.asInet4Address(Heap.createTuple(ClassActor.fromJava(Inet4Address.class).dynamicHub()));
        JDK_java_net_Inet4AddressImpl thisInet4Address = asJDK_java_net_Inet4AddressImpl(inet4Address);
        thisInet4Address.init(hostname, ipAddr);
        return inet4Address;
    }

    @SUBSTITUTE
    InetAddress[] lookupAllHostAddr(String hostname) throws UnknownHostException {
        final DNS dns = getDNS();
        final IPAddress[] ipAddresses = dns.lookup(hostname);
        if (ipAddresses == null) {
            throw new UnknownHostException();
        }
        final InetAddress[] result = new InetAddress[ipAddresses.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = createInet4Address(hostname, ipAddresses[i].addressAsInt());
        }
        return result;
    }

    @SUBSTITUTE
    String getLocalHostName() throws UnknownHostException {
        return getHostByIPAddress(Init.getLocalAddress());
    }

    @SUBSTITUTE
    String getHostByAddr(byte[] addr) throws UnknownHostException {
        return getHostByIPAddress(new IPAddress(addr));
    }

    @SUBSTITUTE
    boolean isReachable0(byte[] addr, int timeout, byte[] ifaddr, int ttl) throws IOException {
        if (ttl == 0) {
            // CheckStyle: stop parameter assignment check
            ttl = ICMP.defaultTimeout();
            // Checkstyle: resume final variable check
        }
        return ICMP.doSeqMatchingICMPEchoReq(new IPAddress(addr), timeout, ttl, ICMP.nextId(), 0) == 0;
    }

    @INLINE
    private String getHostByIPAddress(IPAddress ipAddress) throws UnknownHostException {
        final String result = getDNS().reverseLookup(ipAddress);
        if (result == null) {
            throw new UnknownHostException("host " + ipAddress + " not found");
        }
        return result;
    }

    private static DNS getDNS()  throws UnknownHostException {
        final DNS dns = DNS.getDNS();
        if (dns == null) {
            throw new UnknownHostException("network is unavailable");
        }
        return dns;
    }
}
