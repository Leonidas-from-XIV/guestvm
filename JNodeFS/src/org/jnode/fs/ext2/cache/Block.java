/*
 * Copyright (c) 2009, 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
/*
 * $Id: Block.java 4975 2009-02-02 08:30:52Z lsantha $
 *
 * Copyright (C) 2003-2009 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jnode.fs.ext2.cache;

import java.io.IOException;

import java.util.logging.Level;

import com.sun.max.ve.logging.Logger;

import org.jnode.fs.ext2.Ext2FileSystem;

/**
 * @author Andras Nagy
 */
public class Block {
    private final Logger log = Logger.getLogger(getClass().getName());

    protected byte[] data;
    boolean dirty = false;
    protected Ext2FileSystem fs;
    protected long blockNr;

    public Block(Ext2FileSystem fs, long blockNr, byte[] data) {
        this.data = data;
        this.fs = fs;
        this.blockNr = blockNr;
        // log.setLevel(Level.FINEST);
    }

    /**
     * Returns the data.
     *
     * @return byte[]
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Sets the data.
     *
     * @param data The data to set
     */
    public void setData(byte[] data) {
        this.data = data;
        dirty = true;
    }

    /**
     * flush is called when the block is being removed from the cache
     */
    public void flush() throws IOException {
        if (dirty) {
            fs.writeBlock(blockNr, data, true);
            if (log.isLoggable(Level.FINEST)) {
                log.log(Level.FINEST, "BLOCK FLUSHED FROM CACHE");
            }
        }
    }

    /**
     * Get the dirty flag.
     *
     * @return the dirty flag
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Set the dirty flag.
     * @param b
     */
    public void setDirty(boolean b) {
        dirty = b;
    }

}
