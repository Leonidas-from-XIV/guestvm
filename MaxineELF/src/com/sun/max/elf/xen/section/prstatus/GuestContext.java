/*
 * Copyright (c) 2009 Sun Microsystems, Inc., 4150 Network Circle, Santa Clara, California 95054, U.S.A. All rights
 * reserved.
 *
 * U.S. Government Rights - Commercial software. Government users are subject to the Sun Microsystems, Inc. standard
 * license agreement and applicable provisions of the FAR and its supplements.
 *
 * Use is subject to license terms.
 *
 * This distribution may include materials developed by third parties.
 *
 * Parts of the product may be derived from Berkeley BSD systems, licensed from the University of California. UNIX is a
 * registered trademark in the U.S. and in other countries, exclusively licensed through X/Open Company, Ltd.
 *
 * Sun, Sun Microsystems, the Sun logo and Java are trademarks or registered trademarks of Sun Microsystems, Inc. in the
 * U.S. and other countries.
 *
 * This product is covered and controlled by U.S. Export Control laws and may be subject to the export or import laws in
 * other countries. Nuclear, missile, chemical biological weapons or nuclear maritime end uses or end users, whether
 * direct or indirect, are strictly prohibited. Export or reexport to countries subject to U.S. embargo or to entities
 * identified on U.S. export exclusion lists, including, but not limited to, the denied persons and specially designated
 * nationals lists is strictly prohibited.
 */
package com.sun.max.elf.xen.section.prstatus;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import com.sun.max.elf.ELFHeader;
import com.sun.max.elf.ELFSectionHeaderTable;
import com.sun.max.elf.xen.ImproperDumpFileException;

/**
 * The CPU context dumped by xen
 *
 * @author Puneeet Lakhina
 *
 */
public class GuestContext {

    /**
     * This only includes XMM0 - XMM15
     */
    private byte[] _fpuRegisters = new byte[128];
    private long _flags;
    private byte[] _intRegisters;
    private TrapInfo[] _trapInfo = new TrapInfo[256];
    private long _linearAddressBase, _linearAddressEntries;
    private long[] _gdtFrames = new long[16];
    private long _gdtEntries;
    private long _kernelSS;
    private long _kernelSP;
    private long[] ctrlreg = new long[8];
    private long[] debugreg = new long[8];
    private long _eventCallBackEip;
    private long _failsafeCallbackEip;
    private long _syscallCallbackEip;
    private long _vmAssist;
    private long _fsBase;
    private long _gsBaseKernel;
    private long _gsBaseUser;
    private RandomAccessFile _dumpraf;
    private ELFHeader _header;
    private ELFSectionHeaderTable.Entry _sectionHeader;
    private ByteBuffer _sectionDataBuffer;
    private int _cpuid;

    public GuestContext(RandomAccessFile dumpraf, ELFHeader header, ELFSectionHeaderTable.Entry sectionHeader,int cpuid) {
        this._dumpraf = dumpraf;
        this._header = header;
        this._sectionHeader = sectionHeader;
        this._cpuid = cpuid;
    }

    public void read() throws IOException, ImproperDumpFileException {
        _dumpraf.seek(_sectionHeader.getOffset());
        byte[] sectionData = new byte[_sectionHeader.getSize()];
        _dumpraf.read(sectionData);
        _sectionDataBuffer = ByteBuffer.wrap(sectionData);
        readfpu();
    }

    private void readfpu() {
        _sectionDataBuffer.position(0 + 21 * 8);
        // Skip registers we dont want
        for (int i = 0; i < 15; i++) {
            // for each register read 8 bytes
            for (int j = 0; j < 8; j++) {
                _fpuRegisters[i*8+j] = _sectionDataBuffer.get();
            }
            _sectionDataBuffer.position(_sectionDataBuffer.position()+8);
        }
    }
}
