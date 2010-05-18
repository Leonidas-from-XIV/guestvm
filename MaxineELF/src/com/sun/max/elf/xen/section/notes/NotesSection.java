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
package com.sun.max.elf.xen.section.notes;

import java.io.IOException;
import java.io.RandomAccessFile;

import com.sun.max.elf.ELFDataInputStream;
import com.sun.max.elf.ELFHeader;
import com.sun.max.elf.ELFSectionHeaderTable;
import com.sun.max.elf.xen.ImproperDumpFileException;

/**
 * Represents the notes section in the xen core dump elf file
 *
 * @author Puneeet Lakhina
 *
 */
public class NotesSection {

    private ELFDataInputStream _elfdis;
    private NoneNoteDescriptor _noneNoteDescriptor;
    private HeaderNoteDescriptor _headerNoteDescriptor;
    private XenVersionDescriptor _xenVersionDescriptor;
    private FormatVersionDescriptor _formatVersionDescriptor;
    private ELFHeader _elfHeader;
    private ELFSectionHeaderTable.Entry _sectionHeader;
    private RandomAccessFile _dumpraf;

    static enum DescriptorType {
        NONE, HEADER, XEN_VERSION, FORMAT_VERSION;

        public static DescriptorType fromIntType(int type) {
            switch (type) {
                case 0x2000000:
                    return NONE;
                case 0x2000001:
                    return HEADER;
                case 0x2000002:
                    return XEN_VERSION;
                case 0x2000003:
                    return FORMAT_VERSION;
                default:
                    throw new IllegalArgumentException("Improper type value");
            }
        }
    };

    public NotesSection(RandomAccessFile raf, ELFHeader elfheader, ELFSectionHeaderTable.Entry sectionHeader) {
        this._dumpraf = raf;
        this._elfHeader = elfheader;
        this._sectionHeader = sectionHeader;
    }

    public void read() throws IOException, ImproperDumpFileException {
        _dumpraf.seek(_sectionHeader.getOffset());
        this._elfdis = new ELFDataInputStream(_elfHeader, _dumpraf);
        // readNone
        // readHeader
        // readVersion
        /*
         * the layout of the notes sections is Name Size (4 bytes) Descriptor Size (4 bytes) Type (4 bytes) - usually
         * interpreted as Int. Name Descriptor
         */
        /* the Name is always Xen and in case of notes section in thus coredump thus is length = 4 (including nullbyte) */
        int readLength = 0;
        while (readLength < _sectionHeader.getSize()) {
            int nameLength = _elfdis.read_Elf64_Word();
            if (nameLength != 4) {
                throw new ImproperDumpFileException("Length of name in notes section must be 4");
            }
            int descriptorlength = _elfdis.read_Elf64_Word();
            DescriptorType type = DescriptorType.fromIntType(_elfdis.read_Elf64_Word());
            String name = readString(nameLength);
            if (!name.equals("Xen")) {
                throw new ImproperDumpFileException("Name of each descriptor in the notes section should be xen");
            }
            readLength += (12 + nameLength);
            switch (type) {
                case NONE:
                    if (descriptorlength != 0) {
                        throw new ImproperDumpFileException("None descriptor should be 0 length");
                    }
                    this._noneNoteDescriptor = new NoneNoteDescriptor();
                    readLength += descriptorlength;
                    break;
                case HEADER:
                    readHeaderDescriptor(descriptorlength);
                    readLength += descriptorlength;
                    break;
                case XEN_VERSION:
                    readXenVersionDescriptor(descriptorlength);
                    readLength += descriptorlength;
                    break;

                case FORMAT_VERSION:
                    readFormatVersionDescriptor(descriptorlength);
                    readLength += descriptorlength;
                    break;
            }
        }
    }

    private void readHeaderDescriptor(int length) throws IOException, ImproperDumpFileException {
        if (length != 32) {
            throw new ImproperDumpFileException("Length of the header section should be 32 bytes");
        }
        this._headerNoteDescriptor = new HeaderNoteDescriptor();
        this._headerNoteDescriptor.set_magicnumber(_elfdis.read_Elf64_XWord());
        this._headerNoteDescriptor.set_vcpus(_elfdis.read_Elf64_XWord());
        this._headerNoteDescriptor.set_noOfPages(_elfdis.read_Elf64_XWord());
        this._headerNoteDescriptor.set_pageSize(_elfdis.read_Elf64_XWord());
    }

    private void readXenVersionDescriptor(int length) throws IOException, ImproperDumpFileException {
        this._xenVersionDescriptor = new XenVersionDescriptor();
        // 1272 =
        // sizeof(majorversion)+sizeof(minorversion)+sizeof(extraversion)+sizeof(compileinfo)+sizeof(capabilitiesinfo)+sizeof(changesetinfo)+sizeof(pagesize)
        // the platform param length is platform dependent thus we deduce it based on the total size
        int platformParamLength = length - 1272;
        if (platformParamLength != 4 && platformParamLength != 8) {
            throw new ImproperDumpFileException("Improper xen version descriptor");
        }
        this._xenVersionDescriptor.set_majorVersion(_elfdis.read_Elf64_XWord());
        this._xenVersionDescriptor.set_minorVersion(_elfdis.read_Elf64_XWord());
        this._xenVersionDescriptor.set_extraVersion(readString(XenVersionDescriptor.EXTRA_VERSION_LENGTH));
        this._xenVersionDescriptor.set_compileInfo(readString(XenVersionDescriptor.CompileInfo.COMPILE_INFO_COMPILER_LENGTH),
                        readString(XenVersionDescriptor.CompileInfo.COMPILE_INFO_COMPILE_BY_LENGTH), readString(XenVersionDescriptor.CompileInfo.COMPILE_INFO_COMPILER_DOMAIN_LENGTH),
                        readString(XenVersionDescriptor.CompileInfo.COMPILE_INFO_COMPILE_DATE_LENGTH));
        this._xenVersionDescriptor.set_capabilities(readString(XenVersionDescriptor.CAPABILITIES_LENGTH));
        this._xenVersionDescriptor.set_changeSet(readString(XenVersionDescriptor.CHANGESET_LENGTH));
        if(platformParamLength == 4) {
            this._xenVersionDescriptor.set_platformParamters(_elfdis.read_Elf64_Word());
        }else {
            this._xenVersionDescriptor.set_platformParamters(_elfdis.read_Elf64_XWord());
        }
        this._xenVersionDescriptor.set_pageSize(_elfdis.read_Elf64_XWord());
    }

    private void readFormatVersionDescriptor(int length) throws IOException, ImproperDumpFileException {
        if (length != 8) {
            throw new ImproperDumpFileException("the format version notes descriptor should be 8 bytes");
        }

        this._formatVersionDescriptor = new FormatVersionDescriptor();
        this._formatVersionDescriptor.set_formatVersion(this._elfdis.read_Elf64_XWord());
    }

    /**
     * Read a string from the file with length length. The returned string is of size length - 1 as java strings arent
     * null terminated
     *
     * @param length
     * @return
     */
    private String readString(int length) throws IOException {
        byte[] arr = new byte[length - 1];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = _elfdis.read_Elf64_byte();
        }
        _elfdis.read_Elf64_byte();
        return new String(arr);

    }

    public String toString() {
        return "Header:"+_headerNoteDescriptor != null ? _headerNoteDescriptor.toString():null;
    }
}
