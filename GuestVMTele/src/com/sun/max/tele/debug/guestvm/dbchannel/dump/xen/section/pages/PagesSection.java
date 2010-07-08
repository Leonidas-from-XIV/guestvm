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
package com.sun.max.tele.debug.guestvm.dbchannel.dump.xen.section.pages;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import com.sun.max.elf.ELFDataInputStream;
import com.sun.max.elf.ELFHeader;
import com.sun.max.elf.ELFSectionHeaderTable;


/**
 * @author Puneeet Lakhina
 *
 */
public class PagesSection {

    private RandomAccessFile raf;
    private ELFSectionHeaderTable.Entry pageSectionHeader;
    private ELFSectionHeaderTable.Entry p2mSectionHeader;
    private ELFHeader elfHeader;
    private long noOfPages;
    private Map<Long, PageInfo> mfnPageInfoMap = new HashMap<Long, PageInfo>();
    private long pageSize;

    public PagesSection(RandomAccessFile raf,ELFSectionHeaderTable.Entry pageSectionHeader,ELFSectionHeaderTable.Entry p2mSectionHeader, ELFHeader elfHeader,long noOfPages,long pageSize) {
        this.raf = raf;
        this.pageSectionHeader = pageSectionHeader;
        this.p2mSectionHeader = p2mSectionHeader;
        this.elfHeader=elfHeader;
        this.noOfPages = noOfPages;
        this.pageSize = pageSize;
    }

    public long getX64WordAtOffset(long sectionLocalOffset)throws IOException {
        return getDataInputStream(sectionLocalOffset).read_Elf64_XWord();
    }
    private ELFDataInputStream getDataInputStream(long sectionLocalOffset) throws IOException {
        raf.seek(pageSectionHeader.getOffset()+sectionLocalOffset);
        return new ELFDataInputStream(elfHeader, raf);
    }

    public int readBytes(long address,byte[] dst, int dstOffset,int length)throws IOException {
    	if(address > pageSectionHeader.getSize()) {
            throw new IllegalArgumentException("Improper address:" + address + " Size is:" + pageSectionHeader.getSize());
        }
        raf.seek(pageSectionHeader.getOffset() + address);
        return raf.read(dst, dstOffset, length);
    }
    /**
     * Get the page info corresponding to this pseudo physical pfn.
     * @param pfn
     * @return
     */
    public PageInfo getPageInfoForPfn(long pfn)throws IOException {
        raf.seek(p2mSectionHeader.getOffset()+pfn * 16);
        PageInfo pageInfo = new PageInfo();
        ELFDataInputStream dataInputStream = new ELFDataInputStream(elfHeader,raf);
        pageInfo.setPfn(dataInputStream.read_Elf64_Addr());
        pageInfo.setGmfn(dataInputStream.read_Elf64_Addr());
        if(!pageInfo.isValid()) {
            return null;
        }
        if(pageInfo.getPfn() != pfn) {
            throw new RuntimeException("Improper read.The pfn at the offset doesnt match.");
        }
        return pageInfo;
    }

    /**
     * Get the page info corresponding to this pseudo physical pfn.
     * @param pfn
     * @return
     */
    public PageInfo getPageInfoForMfn(long mfn)throws IOException {
        if(mfnPageInfoMap.get(mfn) != null ) {
            return mfnPageInfoMap.get(mfn);
        }
        raf.seek(p2mSectionHeader.getOffset() + mfnPageInfoMap.size() * 16);
        ELFDataInputStream dataInputStream = new ELFDataInputStream(elfHeader,raf);
        for(int i=mfnPageInfoMap.size();i<noOfPages;i++) {
            PageInfo pageInfo = new PageInfo();
            pageInfo.setPfn(dataInputStream.read_Elf64_XWord());
            pageInfo.setGmfn(dataInputStream.read_Elf64_XWord());
            if(!pageInfo.isValid()) {
                continue;
            }
            mfnPageInfoMap.put(pageInfo.getGmfn(), pageInfo);
            if(pageInfo.getGmfn() == mfn) {
                return pageInfo;
            }
        }
        throw new RuntimeException("Mfn "+Long.toHexString(mfn) + " not found");
    }
}
