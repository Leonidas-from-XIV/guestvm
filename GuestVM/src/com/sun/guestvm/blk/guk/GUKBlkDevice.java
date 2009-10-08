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
package com.sun.guestvm.blk.guk;

import com.sun.max.memory.Memory;
import com.sun.max.memory.VirtualMemory;
import com.sun.max.unsafe.*;
import com.sun.guestvm.blk.device.BlkDevice;
import com.sun.guestvm.guk.*;

/**
 * Guest VM microkernel implementation of BlkDevice.
 *
 * @author Mick Jordan
 *
 */

public final class GUKBlkDevice implements BlkDevice {

    private static boolean _init;
    private static boolean _available;
    private static int _devices;
    private Pointer _writeBuffer;
    private Pointer _readBuffer;
    private int _id;
    private static int _pageSize = 4096;

    private GUKBlkDevice(int id) {
        _id = id;
        _writeBuffer = GUKPagePool.allocatePages(1, VirtualMemory.Type.DATA);
        _readBuffer = GUKPagePool.allocatePages(1, VirtualMemory.Type.DATA);
    }

    public static GUKBlkDevice create(int id) {
        if (!_init) {
            _devices = nativeGetDevices();
            _available = _devices > 0;
            _init = true;
        }
        if (_available) {
            if (id < _devices) {
                return new GUKBlkDevice(id);
            }
        }
        return null;
    }

    /**
     * Return the number of devices available.
     * Devices number from zero.
     * @return the number of devices
     */
    public static int getDevices() {
        return nativeGetDevices();
    }

    public int getSectors() {
        return nativeGetSectors(_id);
    }

    public int getSectorSize() {
        return 512;
    }

// CheckStyle: stop parameter assignment check

/*
 * If we changed the interface to use ByteBuffers AND the caller used "direct"
 * (i.e. native) buffers, we could avoid both synchronization and copying, as
 * GUK can handle concurrent writes and can handle more the one page of data.
 *
 * @see com.sun.guestvm.blk.device.BlkDevice#write(int, long, byte[], int, int)
 */
    public synchronized long write(long address, byte[] data, int offset, int length) {
        if (_available) {
            int left = length;
            while (left > 0) {
                final int toDo = left > _pageSize ? _pageSize : left;
                Memory.writeBytes(data, offset, toDo, _writeBuffer);
                nativeWrite(_id, address, _writeBuffer, toDo);
                left -= toDo;
                offset += toDo;
            }
            return length;
        }
        return -1;
    }

    public synchronized long read(long address, byte[] data, int offset, int length) {
        if (_available) {
            int left = length;
            while (left > 0) {
                final int toDo = left > _pageSize ? _pageSize : left;
                nativeRead(_id, address, _readBuffer, toDo);
                Memory.readBytes(_readBuffer, toDo, data, offset);
                left -= toDo;
                offset += toDo;
            }
            return length;
        }
        return -1;
    }

    private static native int nativeGetDevices();

    private static native int nativeGetSectors(int device);

    private static native long nativeWrite(int device, long address, Pointer data, int length);

    private static native long nativeRead(int device, long address, Pointer data, int length);
}
