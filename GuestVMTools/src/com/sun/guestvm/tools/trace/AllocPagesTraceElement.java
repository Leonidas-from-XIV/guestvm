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
package com.sun.guestvm.tools.trace;


public class AllocPagesTraceElement extends AllocTraceElement {
    // Must match com.sun.max.memory.VirtualMemory.Type
    public enum MemType {
        HEAP,               // for the garbage collected heap
        STACK,             // for thread stacks
        CODE,               // for compiled code
        DATA,               // for miscellaneous data
        PAGE_FRAME  // page table frame
    }

    private static int _pageSize = 4096;
    private int _pages;
    private MemType _memType;
    private int _firstFreePage;
    private int _hwmAllocPage;

    public void setPages(int order) {
        _pages = order;
    }

    public int getPages() {
        return _pages;
    }

    @Override
    public int getAdjSize() {
        return getPages() * _pageSize;
    }

    public static int getPageSize() {
        return _pageSize;
    }

    public void setType(int memTypeOrdinal) {
        switch (memTypeOrdinal) {
            case 0:
                _memType = MemType.HEAP;
                break;
            case 1:
                _memType = MemType.STACK;
                break;
            case 2:
                _memType = MemType.CODE;
                break;
            case 3:
                _memType = MemType.DATA;
                break;
            case 4:
                _memType = MemType.PAGE_FRAME;
                break;
            default:
                throw new IllegalArgumentException("invalid MemType ordinal " + memTypeOrdinal);
        }
    }

    public MemType getType() {
        return _memType;
    }

    public void setFirstFreePage(int firstFreePage) {
        _firstFreePage = firstFreePage;
    }

    public int getFirstFreePage() {
        return _firstFreePage;
    }

    public void setHwmAllocPage(int hwmAllocPage) {
        _hwmAllocPage = hwmAllocPage;
    }

    public int getHwmAllocPage() {
        return _hwmAllocPage;
    }

    public String toString() {
        return super.toString() + " " + _pages + " " + _memType.ordinal();
    }

}
