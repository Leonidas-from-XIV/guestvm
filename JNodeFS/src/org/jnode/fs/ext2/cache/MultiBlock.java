/*
 * Copyright (c) 2011, 2011, Oracle and/or its affiliates. All rights reserved.
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

import java.nio.ByteBuffer;

import org.jnode.fs.ext2.Ext2FileSystem;

/**
 * A {@link MultiBlock} supports pre-fetching of a range of blocks efficiently,
 * It maintains a single contiguous direct buffer that is used to read the blocks
 * and a set of {@link Block block} instances that subdivide the multiblock
 * and are put in the {@link BlockCache} to keep the rest of the system happy.
 * 
 * @author Mick Jordan
 *
 */

public class MultiBlock extends Block {
    private SingleBlock[] subBlocks;
    
    MultiBlock(Ext2FileSystem fs, long blockNr, ByteBuffer buffer, int blockSize) {
        super(fs, blockNr, buffer);
        
        int numBlocks = buffer.remaining() / blockSize;
        subBlocks = new SingleBlock[numBlocks];
        for (int i = 0; i < numBlocks; i++) {
            buffer.position(i * blockSize);
            ByteBuffer slice = buffer.slice();
            slice.limit(blockSize);
            subBlocks[i] = new SingleBlock(fs, blockNr + i, slice);
            subBlocks[i].parent = this;
        }
        buffer.position(0);
    }
    
    SingleBlock[] getSubBlocks() {
        return subBlocks;
    }
    
    public void flush() {
        throw new IllegalStateException("MultiBlock.flush should never be called");
    }
    
    public void setBuffer(ByteBuffer data) {
        throw new IllegalStateException("MultiBlock.setBuffer should never be called");
    }
    
    @Override
    public String toString() {
        return "MB(" + subBlocks.length + "):" + super.toString();
    }
}
