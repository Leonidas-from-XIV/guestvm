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

import com.sun.max.annotate.*;
import com.sun.max.program.ProgramError;
import com.sun.max.ve.net.*;
import com.sun.max.ve.net.device.*;
import com.sun.max.ve.net.ip.*;
import com.sun.max.ve.unsafe.UnsafeCast;
import com.sun.max.vm.actor.holder.ClassActor;
import com.sun.max.vm.heap.Heap;

/**
 * Substitutions for @see java.net.NetworkInterface.
 * @author Mick Jordan
 *
 */

@METHOD_SUBSTITUTIONS(NetworkInterface.class)
public class JDK_java_net_NetworkInterface {

    @ALIAS(declaringClass = NetworkInterface.class, name="<init>")
    private native void init(String name, int index, InetAddress[] addrs);
    
    static NetworkInterface createNetworkInterface(String name, int index, InetAddress[] addrs) {
        // Use the ALIAS mechanism to avoid reflection
        final NetworkInterface networkInterface = UnsafeCast.asNetworkInterface(Heap.createTuple(ClassActor.fromJava(NetworkInterface.class).dynamicHub()));
        JDK_java_net_NetworkInterface thisNetworkInterface = asJDK_java_net_NetworkInterface(networkInterface);
        thisNetworkInterface.init(name, index, addrs);
        return networkInterface;
    }

    static NetDevice findDevice(String name) {
        final NetDevice[] netDevices = Init.getNetDevices();
        for (int i = 0; i < netDevices.length; i++) {
            if (netDevices[i].getNICName().equals(name)) {
                return netDevices[i];
            }
        }
        return null;
    }

    @SUBSTITUTE
    private static NetworkInterface[] getAll() throws SocketException {
        final NetDevice[] netDevices = Init.getNetDevices();
        final NetworkInterface[] result = new NetworkInterface[netDevices.length];
        for (int i = 0; i < netDevices.length; i++) {
            final InetAddress[] inetAddresses = new InetAddress[1];
            inetAddresses[0] = JDK_java_net_Inet4AddressImpl .createInet4Address(Init.hostName(), Init.getLocalAddress(netDevices[i]).addressAsInt());
            final NetworkInterface networkInterface = createNetworkInterface(netDevices[i].getNICName(), i, inetAddresses);
            result[i] = networkInterface;
        }
        return result;
    }

    @SUBSTITUTE
    private static NetworkInterface getByName0(String name) throws SocketException {
        final NetDevice netDevice = findDevice(name);
        if (netDevice != null) {
            final InetAddress[] inetAddresses = new InetAddress[1];
            inetAddresses[0] = JDK_java_net_Inet4AddressImpl.createInet4Address(Init.hostName(), Init.getLocalAddress(netDevice).addressAsInt());
            return createNetworkInterface(name, 0, inetAddresses);
        }
        return null;
    }

    @SUBSTITUTE
    private static NetworkInterface getByInetAddress0(InetAddress inetAddress) throws SocketException {
        final NetDevice[] netDevices = Init.getNetDevices();
        final IPAddress addrToCheck = new IPAddress(inetAddress.getAddress());
        for (int i = 0; i < netDevices.length; i++) {
            final NetDevice netDevice = netDevices[i];
            final IPAddress ipAddress = Init.getLocalAddress(netDevice);
            if (addrToCheck.addressAsInt() == ipAddress.addressAsInt()) {
                return createNetworkInterface(netDevices[i].getNICName(), 0, new InetAddress[] {inetAddress});
            }
        }
        return null;
    }

    @SUBSTITUTE
    private static NetworkInterface getByIndex(int index) {
        //final NetDevice[] netDevices = Init.getNetDevices();
        return null;
    }

    @SUBSTITUTE
    private static long getSubnet0(String name, int ind) throws SocketException {
        ProgramError.unexpected("getSubnet0 not implemented");
        return 0;
    }

    @SUBSTITUTE
    private static Inet4Address getBroadcast0(String name, int ind) throws SocketException {
        ProgramError.unexpected("getBroadcast0 not implemented");
        return null;
    }

    @SUBSTITUTE
    private static boolean isUp0(String name, int ind) throws SocketException {
        final NetDevice netDevice = findDevice(name);
        return netDevice != null;
    }

    @SUBSTITUTE
    private static boolean isLoopback0(String name, int ind) throws SocketException {
        final NetDevice netDevice = findDevice(name);
        return netDevice == Init.getLoopbackDevice();
    }

    @SUBSTITUTE
    private static boolean supportsMulticast0(String name, int ind) throws SocketException {
        ProgramError.unexpected("supportsMulticast0 not implemented");
        return false;
    }

    @SUBSTITUTE
    private static boolean isP2P0(String name, int ind) throws SocketException {
        ProgramError.unexpected("isP2P0 not implemented");
        return false;
    }

    @SUBSTITUTE
    private static byte[] getMacAddr0(byte[] inAddr, String name, int ind) throws SocketException {
        final NetDevice netDevice = findDevice(name);
        final byte[] macBytes = netDevice.getMACAddress();
        final byte[] result = new byte[macBytes.length];
        System.arraycopy(macBytes, 0, result, 0, macBytes.length);
        return result;
    }

    @SUBSTITUTE
    private static int getMTU0(String name, int ind) throws SocketException {
        ProgramError.unexpected("getMTU0 not implemented");
        return 0;
    }

    @SUBSTITUTE
    private static void init() {

    }
}
