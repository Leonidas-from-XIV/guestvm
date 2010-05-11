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
package com.sun.max.elf.xen;

import com.sun.max.elf.xen.NotesSection.DescriptorType;


/**
 * @author Puneeet Lakhina
 *
 */
public class HeaderNoteDescriptor extends NotesSectionDescriptor {

    static enum DomainType {PARAVIRTUALIZED,FULL_VIRTUALIZED};
    private static final long PVDOMAIN_MAGIC_NUMBER = 0xF00FEBEDL;
    private static final long FVDOMAIN_MAGIC_NUMBER = 0xF00FEBEEL;
    /*
     * The domain type depends on the magic number.
     *
     */
    private long _magicnumber;

    /**
     * @return the _magicnumber
     */
    public long get_magicnumber() {
        return _magicnumber;
    }

    /**
     * @param magicnumber the _magicnumber to set
     */
    public void set_magicnumber(long magicnumber) {
        if(magicnumber != PVDOMAIN_MAGIC_NUMBER && magicnumber != FVDOMAIN_MAGIC_NUMBER) {
            throw new IllegalArgumentException("Improper magic number");
        }
        _magicnumber = magicnumber;
        if(magicnumber == PVDOMAIN_MAGIC_NUMBER) {
            this._domainType = DomainType.PARAVIRTUALIZED;
        }else {
            this._domainType = DomainType.FULL_VIRTUALIZED;
        }
    }

    /**
     * @return the _domainType
     */
    public DomainType get_domainType() {
        return _domainType;
    }

    /**
     * @param domainType the _domainType to set
     */
    public void set_domainType(DomainType domainType) {
        _domainType = domainType;
    }

    /**
     * @return the _vcpus
     */
    public long get_vcpus() {
        return _vcpus;
    }

    /**
     * @param vcpus the _vcpus to set
     */
    public void set_vcpus(long vcpus) {
        _vcpus = vcpus;
    }

    /**
     * @return the _noOfPages
     */
    public long get_noOfPages() {
        return _noOfPages;
    }

    /**
     * @param noOfPages the _noOfPages to set
     */
    public void set_noOfPages(long noOfPages) {
        _noOfPages = noOfPages;
    }

    /**
     * @return the _pageSize
     */
    public long get_pageSize() {
        return _pageSize;
    }

    /**
     * @param pageSize the _pageSize to set
     */
    public void set_pageSize(long pageSize) {
        _pageSize = pageSize;
    }
    private DomainType _domainType;
    private long _vcpus;
    private long _noOfPages;
    private long _pageSize;
    public HeaderNoteDescriptor() {
        super(DescriptorType.HEADER);
    }
}
