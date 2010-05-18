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
package com.sun.max.elf.xen;

import java.io.IOException;
import java.io.RandomAccessFile;

import com.sun.max.elf.ELFHeader;
import com.sun.max.elf.ELFLoader;
import com.sun.max.elf.ELFSectionHeaderTable;
import com.sun.max.elf.xen.section.notes.NotesSection;
import com.sun.max.elf.xen.section.prstatus.GuestContext;

/**
 * @author Puneeet Lakhina
 *
 */
public class XenCoreDumpELFReader {

    private static final String NOTES_SECTION_NAME = ".note.Xen";
    private static final String CONTEXT_SECTION_NAME = ".xen_prstatus";
    private static final String SHARED_INFO_SECTION_NAME = ".xen_shared_info";
    private static final String P2M_SECTION_NAME = ".xen.p2m";
    private static final String PFN_SECTION_NAME = ".xen.pfn";
    private static final String XEN_PAGES_SECTION_NAME = ".xen_pages";

    private RandomAccessFile _fis;
    private ELFHeader _header;
    private ELFSectionHeaderTable _sectionHeaderTable;
    private ELFSectionHeaderTable.Entry _notesSectionHeader;
    private ELFSectionHeaderTable.Entry _contextSectionHeader;

    public XenCoreDumpELFReader(RandomAccessFile raf, ELFHeader header) throws IOException {
        this._fis = raf;
        this._header = header;
        this._sectionHeaderTable = ELFLoader.readSHT(raf, header);
        for (ELFSectionHeaderTable.Entry entry : _sectionHeaderTable.entries) {
            String sectionName = entry.getName();
            System.out.println(sectionName);
            if (NOTES_SECTION_NAME.equalsIgnoreCase(sectionName)) {
                _notesSectionHeader = entry;
            }
            if (CONTEXT_SECTION_NAME.equalsIgnoreCase(sectionName)) {
                _contextSectionHeader = entry;
            }
        }

    }

    public NotesSection readNotesSection() throws IOException,ImproperDumpFileException {
        NotesSection notesSection = new NotesSection(_fis, _header, _notesSectionHeader);
        notesSection.read();
        return notesSection;
    }

    public GuestContext readGuestContext(int cpuid) throws IOException,ImproperDumpFileException {
        GuestContext context = new GuestContext(_fis, _header, _contextSectionHeader,cpuid);
        context.read();
        return context;
    }

    public GuestContext readGuestContext() throws IOException,ImproperDumpFileException {
        return readGuestContext(0);
    }
}
