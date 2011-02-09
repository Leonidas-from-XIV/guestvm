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
package org.jnode.fs.ext2.cache;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jnode.fs.ext2.Ext2FileSystem;

/**
 * The essential state representing a file system block.
 * 
 * @author Mick Jordan
 *
 */

public abstract class Block {

    protected ByteBuffer buffer;
    protected boolean dirty = false;
    protected Ext2FileSystem fs;
    protected long blockNr;
    protected Block parent;
    
    protected Block(Ext2FileSystem fs, long blockNr, ByteBuffer data) {
        this.buffer = data;
        this.fs = fs;
        this.blockNr = blockNr;                
    }
    
    /**
     * Returns the data as a {@link ByteBuffer}.
     *
     * @return byte[]
     */
    public ByteBuffer getBuffer() {
        return buffer;
    }

    public abstract void setBuffer(ByteBuffer b);
    public abstract void flush() throws IOException;
    
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

    /**
     * If this block is a sub-block of a larger block, return the parent value.
     * @return
     */
    public Block getParent() {
        return parent;
    }
    
    @Override
    public String toString() {
        return Long.toString(blockNr) + ":" + (dirty ? "D" : "C");
    }
}
