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
package com.sun.guestvm.fs.ext2;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.partitions.PartitionTableEntry;

import com.sun.guestvm.blk.guk.*;


public final  class JNodeFSBlockDeviceAPIBlkImpl implements FSBlockDeviceAPI {

    private GUKBlkDevice _blkDevice;
    private long _length;

    private JNodeFSBlockDeviceAPIBlkImpl(GUKBlkDevice blkDevice) {
        _blkDevice = blkDevice;
        _length = _blkDevice.getSectors() * _blkDevice.getSectorSize();
    }

    public static JNodeFSBlockDeviceAPIBlkImpl create(int id) {
        final GUKBlkDevice blkDevice = GUKBlkDevice.create(id);
        if (blkDevice != null) {
            return new JNodeFSBlockDeviceAPIBlkImpl(blkDevice);
        }
        return null;
    }

    @Override
    public PartitionTableEntry getPartitionTableEntry() {
        return null;
    }

    @Override
    public int getSectorSize() throws IOException {
        check();
        return _blkDevice.getSectorSize();
    }

    @Override
    public void flush() throws IOException {
        // nothing to do
    }

    @Override
    public long getLength() throws IOException {
        check();
        return _length;
    }

    @Override
    public void read(long devOffset, ByteBuffer dest) throws IOException {
        check();
        final boolean ha = dest.hasArray();
        assert ha;
        final int ao = dest.arrayOffset();
        final byte[] b = dest.array();
        _blkDevice.read(devOffset, b, ao, b.length);
    }

    @Override
    public void write(long devOffset, ByteBuffer src) throws IOException {
        check();
        final boolean ha = src.hasArray();
        assert ha;
        final int ao = src.arrayOffset();
        final byte[] b = src.array();
        _blkDevice.write(devOffset, b, ao, b.length);
    }

    private void check() throws IOException {
        if (_blkDevice == null) {
            throw new IOException("device not available");
        }
    }

}
