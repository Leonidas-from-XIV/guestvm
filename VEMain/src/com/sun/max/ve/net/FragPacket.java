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
package com.sun.max.ve.net;

import com.sun.max.program.*;

/**
 * A FragPacket is used to represent fragmented IP packets that share data with the original
 * unfragmented packet, which avoids copying but, of course, has its perils. When a FragPacket is first
 * created it is essentially a clone of the original packet, save that the underlying data buffer is not copied
 * but shared. The other state, e.g., header offset, data length, is copied and therefore capable of separate
 * evolution. All methods that access this state, e.g., @see getHeaderOffset simply invoke the superclass
 * definition to access this separate state, forwarding the arguments. All methods that access the underlying
 * buffer, e.g., @see putInt, also invoke the superclass method, but adjust the offset with the value that
 * was set when the FragPacket was created. N.B. This depends on there being no nested calls in Packet that might
 * callback into here and add the offset twice.
 *
 * N.B. FragPackets are only used for output.
 *
 * @author Mick Jordan
 *
 */
public class FragPacket extends Packet {
    // private Packet _unFrag;
    private int _offset;

    /**
     * Create a fragmented packet from the given underlying packet.
     * The offset is where the FragPacket is to start in the underlying packet.
     * @param unfrag
     * @param offset
     */
    public FragPacket(Packet unfrag, int offset) {
        super(unfrag.getHeaderOffset(), unfrag.dataLength(), unfrag.getBuf());
        // _unFrag = unfrag;
        _offset = offset;
    }

    @Override
    public boolean isFragment() {
        return true;
    }

    @Override
    public Packet copy() {
        error("copy");
        return null;
    }

    @Override
    public int cksum(int offset, int len) {
        return super.cksum(offset + _offset, len);
    }

    @Override
    public byte getByteIgnoringHeaderOffset(int off) {
        return super.getByteIgnoringHeaderOffset(off + _offset);
    }

    @Override
    public byte[] getEthAddr(int offset) {
        error("getEthAddr");
        return null;
    }

    @Override
    public long getEthAddrAsLong(int off) {
        error("getEthAddrAsLong");
        return 0;
    }

    @Override
    public int getInt(int off) {
        return super.getInt(off + _offset);
    }

    @Override
    public int getShort(int off) {
        return super.getShort(off + _offset);
    }

    @Override
    public int getByte(int off) {
        return super.getByte(off + _offset);
    }

    @Override
    public void getBytes(int srcOffset, byte[] dst, int dstOffset, int len) {
        error("getBytes");
    }

    @Override
    public void putEthAddr(byte[] addr, int offset) {
        super.putEthAddr(addr, offset + _offset);
    }

    @Override
    public void putEthAddr(long d, int off) {
        super.putEthAddr(d, off + _offset);
    }

    @Override
    public void putInt(int d, int off) {
        super.putInt(d, _offset + off);
    }

    @Override
    public void putShort(int d, int off) {
        super.putShort(d, off + _offset);
    }

    @Override
    public void putByte(int d, int off) {
        super.putByte(d, off + _offset);
    }

    @Override
    public void putBytes(byte[] src, int srcOffset, int dstOffset, int len) {
        super.putBytes(src, srcOffset, dstOffset + _offset, len);
    }

    @Override
    public void putBytes(Packet pkt, int srcOffset, int dstOffset, int len) {
        error("putBytes");
    }

    private static void error(String m) {
        ProgramError.unexpected("FragPacket." + m + " not implemented");
    }

}
