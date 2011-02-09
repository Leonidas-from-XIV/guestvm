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
 * $Id: INodeTable.java 4975 2009-02-02 08:30:52Z lsantha $
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
 
package org.jnode.fs.ext2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.BitSet;

import org.jnode.fs.FileSystemException;
import org.jnode.util.ByteBufferUtils;

/**
 * This class represents a part of the inode table (that which is contained
 * in one block group).
 * 
 * It provides methods for reading and writing the (already allocated) inodes.
 * 
 * An inode table contains just the inodes, with no extra metadata.
 * 
 * It is evidently advantageous to prefetch several blocks at a time even
 * though access is not necessarily sequential. However, we don't (appear to) 
 * know the actual number of blocks in the table, only the 
 * maximum number {@link #blockCount}. So we prefetch in groups of
 * {@value INodeTable#PREFETCH_SIZE} as blocks are requested.
 * 
 * @author Andras Nagy
 * @author Mick Jordan
 */
public class INodeTable {
    //@CONSTANT_WHEN_NOT_ZERO;
    private final int blockSize;
    int blockCount;
    Ext2FileSystem fs;
    int firstBlock; // the first block of the inode table
    private static final int PREFETCH_SIZE = 8;
    private BitSet prefetchBitSet;
    

    public INodeTable(Ext2FileSystem fs, int firstBlock) {
        this.fs = fs;
        this.firstBlock = firstBlock;
        blockSize = fs.getBlockSize();
        blockCount =
                (int) Ext2Utils.ceilDiv(
                        fs.getSuperblock().getINodesPerGroup() * INode.INODE_LENGTH, blockSize);
        prefetchBitSet = new BitSet(blockCount / PREFETCH_SIZE);
    }

    public static int getSizeInBlocks(Ext2FileSystem fs) {
        int count =
                (int) Ext2Utils.ceilDiv(
                        fs.getSuperblock().getINodesPerGroup() * INode.INODE_LENGTH, 
                        fs.getBlockSize());
        return count;
    }

    /**
     * get the <code>blockNo</code>th block from the beginning of the inode
     * table
     * 
     * @param blockNo
     * @return the contents of the block as a {@link ByteBuffer}
     * @throws FileSystemException
     * @throws IOException
     */
    private ByteBuffer getINodeTableBlock(int blockNo) throws FileSystemException, IOException {
        if (blockNo < blockCount) {
            final int b = blockNo / PREFETCH_SIZE;
            if (!prefetchBitSet.get(b)) {
                fs.readBlock(firstBlock + b * PREFETCH_SIZE, PREFETCH_SIZE - 1);
                prefetchBitSet.set(b);
            }
            // this will now hit in the cache
            return fs.readBlock(firstBlock + blockNo);
        } else {
            throw new FileSystemException("Trying to get block #" + blockNo +
                    "of an inode table that only has " + blockCount + " blocks");
        }
    }

    /**
     * Write the <code>blockNo</code>th block (from the beginning of the
     * inode table)
     * 
     * @param data
     * @param blockNo
     * @throws FileSystemException
     * @throws IOException
     */
    private void writeINodeTableBlock(ByteBuffer data, int blockNo)
        throws FileSystemException, IOException {
        if (blockNo < blockCount)
            fs.writeBlock(firstBlock + blockNo, data, false);
        else
            throw new FileSystemException("Trying to write block #" + blockNo +
                    "of an inode table that only has " + blockCount + " blocks");
    }

    /**
     * Get the indexth inode from the inode table. (index is not an inode
     * number, it is just an index in the inode table)
     * 
     * For each inode table, only one instance of INodeTable exists, so it is
     * safe to synchronize to it
     */
    public synchronized void getInodeData(int index, byte[] data) throws IOException, FileSystemException {
        int indexCopied = 0;
        while (indexCopied < INode.INODE_LENGTH) {
            int blockNo = (index * INode.INODE_LENGTH + indexCopied) / blockSize;
            int blockOffset = (index * INode.INODE_LENGTH + indexCopied) % blockSize;
            int copyLength = Math.min(blockSize - blockOffset, INode.INODE_LENGTH);
            ByteBufferUtils.buffercopy(getINodeTableBlock(blockNo), blockOffset, data, indexCopied,
                    copyLength);
            indexCopied += copyLength;
        }
    }

    /*
     * For each inode table, only one instance of INodeTable exists, so it is
     * safe to synchronize to it
     */
    public synchronized void writeInodeData(int index, byte[] data)
        throws IOException, FileSystemException {
        int indexCopied = 0;
        while (indexCopied < INode.INODE_LENGTH) {
            int blockNo = (index * INode.INODE_LENGTH + indexCopied) / blockSize;
            int blockOffset = (index * INode.INODE_LENGTH + indexCopied) % blockSize;
            int copyLength = Math.min(blockSize - blockOffset, INode.INODE_LENGTH);
            ByteBuffer originalBlock = getINodeTableBlock(blockNo);
            ByteBufferUtils.buffercopy(data, indexCopied, originalBlock, blockOffset, copyLength);
            indexCopied += copyLength;
            writeINodeTableBlock(originalBlock, blockNo);
        }
    }
}
