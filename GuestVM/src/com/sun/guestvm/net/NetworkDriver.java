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
package com.sun.guestvm.net;

/**
 * NetworkDriver defines an interface for network device drivers
 * to implement.  This is so that we can have more than one implementation
 * of a network driver.
 *
 */
public interface NetworkDriver {

    /**
     * General initialization routine.  Must be called before any
     * packet activity.
     */
    void initNetworkDriver();

    /**
     * Get a device-dependent packet suitable for output.
     *
     * @param hlen Usually the size of the link header returned by headerHint()
     * @param dlen The maximum size of the IP pkt incl IP header
     * @return A packet to use or null if none available
     */
    Packet getTransmitPacket(int hlen, int dlen);

    /**
     * Sends an IP packet out the network.
     *
     * @param pkt Everything in the packet except any link headers
     * @param dst_ip The destination IP address for the packet
     */
    void output(Packet pkt, int dstIp);

    /**
     * Returns default router  - called by ProtocolStack
     * Some links, like PPP know the default router. Others,
     * like Ethernet don't, and should return 0.
     *
     * @param dst The destination IP address
     */
    int    getDefaultRouter(int dst);

    /**
     * Return boot configuration - called by ProtocolStack
     * Some links, like PPP provide boot configuration.
     * Others, like Ethernet don't and should return null
     *
     * @return a BootConfiguration object
     */
    // BootConfiguration getConfig();

    /**
     * print a summary of interface status.
     *
     * @param out A PrintStream to use
     */
    // void report(java.io.PrintStream out);

    /**
     * @return the MTU for this network interface
     */
    int getMtu();

    /**
     * @return the packet header offset for this interface
     */
    int headerHint();

    /*
     * Checks whether the given IP address is available for use.
     * This is useful for protecting against duplicate IP addresses
     * on a network during auto-configuration.
     *
     * @param ip_addr The IP address to test
     * @return true if ip_addr already exists, false otherwise.
     */
    boolean checkForIP(int ipAddr);

    /**
     * Enable multicast reception. The network driver need not use-
     * count, as IP will only call this once for any given address
     * Some links may not support multicasting and should ignore this call
     *
     * @param group the class-D IP address of the group to join
     */
    void joinGroup(int ipAddr);

    /**
     * Disable multicast reception.
     * IP will not call this for a particular address if joinGroup
     * hasn't already been called.
     * Some links may not support multicasting and should ignore this call
     *
     * @param group the class-D IP address of the group to leave
     */
    void leaveGroup(int ipAddr);
}
