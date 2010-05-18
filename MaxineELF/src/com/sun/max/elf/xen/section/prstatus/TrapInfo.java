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
package com.sun.max.elf.xen.section.prstatus;


/**
 * @author Puneeet Lakhina
 *
 */
public class TrapInfo {

    private short _vector;
    private short _flags;
    private int _codeSelector;
    private long _codeOffset;
    /**
     * @return the _vector
     */
    public short get_vector() {
        return _vector;
    }

    /**
     * @param vector the _vector to set
     */
    public void set_vector(short vector) {
        _vector = vector;
    }

    /**
     * @return the _flags
     */
    public short get_flags() {
        return _flags;
    }

    /**
     * @param flags the _flags to set
     */
    public void set_flags(short flags) {
        _flags = flags;
    }

    /**
     * @return the _codeSelector
     */
    public int get_codeSelector() {
        return _codeSelector;
    }

    /**
     * @param codeSelector the _codeSelector to set
     */
    public void set_codeSelector(int codeSelector) {
        _codeSelector = codeSelector;
    }

    /**
     * @return the _codeOffset
     */
    public long get_codeOffset() {
        return _codeOffset;
    }

    /**
     * @param codeOffset the _codeOffset to set
     */
    public void set_codeOffset(long codeOffset) {
        _codeOffset = codeOffset;
    }


}
