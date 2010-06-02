/*
 * Copyright (c) 2007 Sun Microsystems, Inc.  All rights reserved.
 *
 * Sun Microsystems, Inc. has intellectual property rights relating to technology embodied in the product
 * that is described in this document. In particular, and without limitation, these intellectual property
 * rights may include one or more of the U.S. patents listed at http://www.sun.com/patents and one or
 * more additional patents or pending patent applications in the U.S. and in other countries.
 *
 * U.S. Government Rights - Commercial software. Government users are subject to the Sun
 * Microsystems, Inc. standard license agreement and applicable provisions of the FAR and its
 * supplements.
 *
 * Use is subject to license terms. Sun, Sun Microsystems, the Sun logo, Java and Solaris are trademarks or
 * registered trademarks of Sun Microsystems, Inc. in the U.S. and other countries. All SPARC trademarks
 * are used under license and are trademarks or registered trademarks of SPARC International, Inc. in the
 * U.S. and other countries.
 *
 * UNIX is a registered trademark in the U.S. and other countries, exclusively licensed through X/Open
 * Company, Ltd.
 */
package com.sun.max.tele.debug.guestvm.dbchannel.dump;

import java.io.*;

import com.sun.max.elf.xen.*;
import com.sun.max.tele.page.*;
import com.sun.max.unsafe.*;


/**
 * @author Puneeet Lakhina
 *
 */
public class CoreDumpPageTableAccess extends AbstractX64PageTableAccess {

    private XenCoreDumpELFReader dumpReader;
    private static final Long PAGE_TABLE_BASE_ADDRESS_MASK=4503599627370495L;
    public CoreDumpPageTableAccess(XenCoreDumpELFReader dumpReader) {
        this.dumpReader = dumpReader;
    }


    /* (non-Javadoc)
     * @see com.sun.max.tele.page.PageTableAccess#getMfnForPfn(long)
     */
    @Override
    public long getMfnForPfn(long pfn) throws IOException {
        // Pfn starts at 0
        if (pfn < getNoOfPages()) {
            return dumpReader.getPagesSection().getPageInfoForPfn(pfn).getGmfn();
        } else {
            throw new IndexOutOfBoundsException("page frame index " + pfn + " is out of range");
        }
    }

    /* (non-Javadoc)
     * @see com.sun.max.tele.page.PageTableAccess#getNoOfPages()
     */
    @Override
    public long getNoOfPages() throws IOException {
        return dumpReader.getNoOfPages();
    }

    /* (non-Javadoc)
     * @see com.sun.max.tele.page.PageTableAccess#getPTEntryAtIndex(com.sun.max.unsafe.Address, int)
     */
    @Override
    public long getPTEntryAtIndex(Address table, int index)throws IOException {
        return dumpReader.getPagesSection().getX64WordAtOffset(table.toLong() + (index * 8));
    }

    /* (non-Javadoc)
     * @see com.sun.max.tele.page.PageTableAccess#getPageTableBase()
     */
    @Override
    public Address getPageTableBase()throws IOException {
        return getAddressForPfn(dumpReader.getPagesSection().getPageInfoForMfn(dumpReader.getGuestContext(0).getCtrlreg()[3] >> 12).getPfn());
    }


    /* (non-Javadoc)
     * @see com.sun.max.tele.page.PageTableAccess#getPfnForMfn(long)
     */
    @Override
    public long getPfnForMfn(long mfn)throws IOException {
        return dumpReader.getPagesSection().getPageInfoForMfn(mfn).getPfn();
    }

}