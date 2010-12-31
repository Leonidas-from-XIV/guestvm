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
package com.sun.max.ve.net.protocol.ether;

import java.util.*;

import com.sun.max.ve.net.*;
import com.sun.max.ve.net.debug.*;
import com.sun.max.ve.net.device.*;


public class Ether implements NetDevice.Handler {
    private static boolean _debug;
    private static boolean _dump;

    public static final int ETHERNET_MAXIMUM_FRAME_SIZE = 1514;

    private NetDevice _dev;

    private byte[] _ownHardwareAddress;

    public static final int DST_OFFSET = 0;
    public static final int SRC_OFFSET = 6;
    public static final int TYPE_OFFSET = 12;
    public static final int ADDR_SIZE = 6;

    public static final byte[] ETHER_BCAST = {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff };

    public static final int PROTO_IP   = 0x0800;
    public static final int PROTO_ARP  = 0x0806;
    public static final int PROTO_RARP = 0x8035;


    private NetDevice.Handler _myIPConsumer;
    private NetDevice.Handler _myARPConsumer;
    private NetDevice.Handler  _myRARPConsumer;
    private boolean _avoidSplitting = true;
    private Map<String, Integer> _dispatch = new HashMap<String, Integer>(3);

    private static void dprint(String m) {
        if (_debug) {
            Debug.println(m);
        }
    }

    public Ether(NetDevice dev) {
        _debug = System.getProperty("max.ve.net.protocol.ether.debug") != null;
        _dump = System.getProperty("max.ve.net.protocol.ether.dump") != null;
        this._dev = dev;
        _ownHardwareAddress = dev.getMACAddress();
        _dispatch.put("IP", 0x0800);
        _dispatch.put("ARP", 0x0806);
        _dispatch.put("RARP", 0x8035);
        dev.registerHandler(this);
    }

    public NetDevice getNetDevice() {
        return _dev;
    }

    public int getMTU() {
        return _dev.getMTU();
    }

    public byte[] getMacAddress() {
        return _ownHardwareAddress;
    }

    /**
     * Install a  handler that receives packets from the network
     * This method is called by higher layers.
     */
    public void registerHandler(NetDevice.Handler handler, String name) {
        if (name.equals("IP")) {
            _myIPConsumer = handler;
        } else if (name.equals("ARP")) {
            _myARPConsumer = handler;
        } else if (name.equals("RARP")) {
            _myRARPConsumer = handler;
        } else {
            throw new Error("Unknown protocol " + name);
        }
    }

    public NetDevice.Handler getReceiverHandler() {
        return this;
    }

    public void handle(Packet pkt) {
        if (_avoidSplitting) {
            /**/
            if (_dump) {
                dump(pkt);
            }
            /**/
            final int id = pkt.getShort(TYPE_OFFSET);
            switch (id) {
                case PROTO_IP:
                    if (_debug) {
                        dprint("Ether: received IP packet");
                    }
                    if (_myIPConsumer == null) {
                        dprint("NO IP consumer");
                    } else {
                        _myIPConsumer.handle(pkt);
                    }
                    break;
                case PROTO_ARP:
                    if (_debug) {
                        dprint("Ether: received ARP packet");
                    }
                    if (_myARPConsumer == null) {
                        dprint("NO ARP consumer");
                    } else {
                        _myARPConsumer.handle(pkt);
                    }
                    break;

                case PROTO_RARP:
                    if (_debug) {
                        dprint("Ether: received RARP packet");
                    }
                    if (_myRARPConsumer == null) {
                        dprint("NO RARP consumer");
                    } else {
                        _myRARPConsumer.handle(pkt);
                    }
                    break;
                default:
                    if (_debug) {
                        dprint("EtherQueueConsumerThread: 802.3 Encapsulation - ignored");
                    }
            }
        } else {
            // _avoidSplitting =false ???
        }
    }



    public byte[] getBroadcastAddr() {
        return ETHER_BCAST;
    }

    /*
    public Memory transmitARPBroadcast(Memory userbuf) {
        Memory buf = userbuf.joinPrevious();
        //Debug.out.println("jx.net.Ether: ARP broadcast");
        return transmitSpecial(_ownHardwareAddress, ETHER_BCAST, _dispatch.findID("ARP"), buf);
    }
    */

    public void transmitARPBroadcast(Packet pkt) {
        transmitBroadcast(pkt, _dispatch.get("ARP"));
    }

    public void transmitBroadcast(Packet pkt, int type) {
        transmitSpecial(_ownHardwareAddress, ETHER_BCAST, type, pkt);
    }

    public void transmit(byte[] dest, int type, Packet pkt) {
        transmitSpecial(_ownHardwareAddress, dest, type, pkt);
    }

    public void transmit(long dest, int type, Packet pkt) {
        transmitSpecial(_ownHardwareAddress, dest, type, pkt);
    }

    public void transmitSpecial(byte[] src, byte[] dest, int type, Packet pkt) {
        setInfo(src, dest, type, pkt);
        _dev.transmit(pkt);
    }

    public void transmitSpecial(byte[] src, long dest, int type, Packet pkt) {
        pkt.setHeaderOffset(0);
        pkt.putEthAddr(dest, DST_OFFSET);
        pkt.putEthAddr(src, SRC_OFFSET);
        pkt.putShort(type, TYPE_OFFSET);
        _dev.transmit(pkt);
    }

    public void transmitSpecial1(byte[] src, byte[] dest, int type, Packet pkt, int size) {
        setInfo(src, dest, type, pkt);
        _dev.transmit1(pkt, 0, size);
    }

    private void setInfo(byte[] src, byte[] dest, int type, Packet pkt) {
        pkt.setHeaderOffset(0);
        pkt.putEthAddr(dest, DST_OFFSET);
        pkt.putEthAddr(src, SRC_OFFSET);
        pkt.putShort(type, TYPE_OFFSET);
    }

    public static String addressToString(byte[] address) {
        final StringBuilder b = new StringBuilder();
        for (int i = 0; i < address.length; i++) {
            if (i != 0) {
                b.append(':');
            }
            b.append(byteToHex(address[i]));
        }
        return b.toString();
    }

    public static String addressToString(long address) {
        final StringBuilder b = new StringBuilder();
        int shift = 40;
        for (int i = 0; i < ADDR_SIZE; i++) {
            if (i != 0) {
                b.append(':');
            }
            b.append(byteToHex((byte) ((address >> shift) & 0xFF)));
            shift -= 8;
        }
        return b.toString();
    }

    private static char toHex(int i) {
        if (i <= 9) {
            return (char) ('0' + i);
        }
        switch (i) {
            case 10:
                return 'A';
            case 11:
                return 'B';
            case 12:
                return 'C';
            case 13:
                return 'D';
            case 14:
                return 'E';
            case 15:
                return 'F';
        }
        return 0;
    }

    private static String byteToHex(byte n) {
        int i = n;
        if (i < 0) {
            i += 256;
        }
        final int i1 = i & 0xf;
        final int i2 = (i & 0xf0) >> 4;
        final StringBuilder b = new StringBuilder(2);
        return b.append(toHex(i2)).append(toHex(i1)).toString();
    }

    private void dump(Packet pkt) {
        Debug.println("Ether-Packet:");
        Debug.println("  Source: " + addressToString(pkt.getEthAddr(6)));
        Debug.println("  Dest: " + addressToString(pkt.getEthAddr(0)));
        final int type = pkt.getShort(TYPE_OFFSET);
        String length = "";
        String typeString;
        switch (type) {
            case 0x800:
                typeString = "IP";
                break;
            case 0x806:
                typeString = "ARP";
                break;
            case 0x8035:
                typeString = "RARP";
                break;
            default:
                typeString = "802.3";
                length = " Length: " + Integer.toString(type);
        }
        Debug.println("  Type: " + typeString + length);

    }
}

