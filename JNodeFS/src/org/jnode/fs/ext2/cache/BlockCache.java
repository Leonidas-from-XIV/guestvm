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
 * $Id: BlockCache.java 4975 2009-02-02 08:30:52Z lsantha $
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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import java.util.logging.Level;

import org.jnode.fs.ext2.Ext2FileSystem;

import com.sun.max.ve.logging.*;

/**
 * The file system block cache.
 * The maximum size can be controlled by the system property {@value CACHE_SIZE_PROPERTY}.
 * A {@link SingleBlock} stores its data using a {@link DirectByteBuffer}, a pool of which is maintained here.
 * 
 * Not explicitly synchronized, so all callers must synchronize on the instance before calling.
 * 
 * @author Andras Nagy
 * @author Mick Jordan
 */
public final class BlockCache {
    private static final Logger log = Logger.getLogger(BlockCache.class.getName());
    private static final String CACHE_SIZE_PROPERTY = "max.ve.fs.ext2.cachesize";
    private static final int DEFAULT_CACHE_SIZE = 16 * 1024 * 1024;
    
    private int maxCacheSize;
    private int maxCacheSizeInBlocks;
    private static ByteBufferFactory byteBufferFactory;
    private LinkedHashMap<Long, Block> cache;
    private int blockSize;
    private BufferManager bufferManager;

    public BlockCache(int blockSize) {
        this.blockSize = blockSize;
        maxCacheSize = getCacheSize();
        maxCacheSizeInBlocks = maxCacheSize / blockSize;
        cache = new CacheMap();
        byteBufferFactory = ByteBufferFactory.create();
        if (byteBufferFactory ==  null) {
            log.log(Level.SEVERE, "failed to create ByteBufferFactory");
        }
        bufferManager = new CVMBufferManager();        
    }
    
    private int getCacheSize() {
        String cacheSizeProperty = System.getProperty(CACHE_SIZE_PROPERTY);
        int result = DEFAULT_CACHE_SIZE;
        if (cacheSizeProperty != null) {
            int scale =1;
            final int len = cacheSizeProperty.length();
            switch (cacheSizeProperty.charAt(len - 1)) {
                case 'm': case 'M': scale = 1024 * 1024; break;
                case 'k': case 'K': scale = 1024; break;
                default:
            }
            if (scale > 1) {
                cacheSizeProperty = cacheSizeProperty.substring(0, len - 1);
            }
            result = Integer.parseInt(cacheSizeProperty) * scale;
            if (result % blockSize != 0) {
                result = (result + blockSize) / blockSize;
            }
        }
        return result;
    }

    public Block put(Block block) {
        Block result = block;
        if (block instanceof MultiBlock) {
            SingleBlock[] subBlocks = ((MultiBlock) block).getSubBlocks();
            for (SingleBlock subBlock : subBlocks) {
                cache.put(subBlock.blockNr, subBlock);
            }
            result = subBlocks[0];
        } else {
          cache.put(block.blockNr, block);
        }
        return result;
    }
    
    public Collection<Block> values() {
        return cache.values();
    }
    
    public Block get(long blockNr) {
        return cache.get(blockNr);
    }
    
    public boolean contains(long blockNr) {
        return cache.containsKey(blockNr);
    }
    
    /**
     * Get a {@link Block}  for I/O and subsequent placement in the cache.
     * If <code>preFetchCount</code> is non-zero then attempt to get a buffer large enough
     * for <code>preFetchCount + 1</code> blocks.
     * @param fs
     * @param blockNr
     * @param preFetchCount
     * @return
     */
    public Block getBlock(Ext2FileSystem fs, long blockNr, int preFetchCount) {
        ByteBuffer buffer = bufferManager.allocateBuffer(1 + preFetchCount);
        final int nBlocks = buffer.remaining() / blockSize;
        return nBlocks == 1 ? new SingleBlock(fs, blockNr, buffer) : new MultiBlock(fs, blockNr, buffer, blockSize);
    }
    
    /**
     * Release a {@link Block} that was allocated by {@#getBlock} but not placed in the cache.
     * @param block
     */
    public void releaseBlock(Block block) {
        bufferManager.recycleBuffer((block.getBuffer()));
    }
    

    /**
     * Map from fs block numbers to {@link Block} instances, utilising the LRU
     * feature of {@link LinkedHashMap} to recycle blocks when the cache fills up.
     */
    class CacheMap extends LinkedHashMap<Long, Block> {
        
        CacheMap() {
            super(maxCacheSizeInBlocks, 0.75f, true);
        }
        
        @Override
        protected synchronized boolean removeEldestEntry(Map.Entry<Long, Block> eldest) {
            if (cache.size() >= maxCacheSizeInBlocks) {
                try {
                    Block block = eldest.getValue();
                    block.flush();
                    
                    if (log.isLoggable(Level.FINEST)) {
                        log.log(Level.FINEST, "BlockCache.removeEldestEntry: " + block);
                    }
                    
                    // recycle the byte buffer                    
                    if (block.parent != null) {
                        for (SingleBlock singleBlock : ((MultiBlock) block.parent).getSubBlocks()) {
                            this.remove(singleBlock.blockNr);
                        }
                        bufferManager.recycleBuffer(block.parent.getBuffer());
                        return false;
                    } else {
                        bufferManager.recycleBuffer(block.getBuffer());
                        return true;
                    }
                } catch (IOException e) {
                    log.log(Level.SEVERE, "Exception when flushing a block from the cache", e);
                }
                return true;
            } else {
                return false;
            }
        }
    }
    
    private abstract class BufferManager {
        abstract ByteBuffer allocateBuffer(int nBlocks);
        abstract void recycleBuffer(ByteBuffer buffer);  
    }
    
    /**
     * Contiguous Virtual Memory implementation.
     * Allocate a contiguous master {@link ByteBuffer} up front and
     * slices it up as necessary. Uses a {@link BitSet} to record
     * free/used single block buffers. Since can't get from a slice
     * back to the bitmap slot, have to also keep byte buffer instances
     * in an array for recycling.
     */
    private class CVMBufferManager extends BufferManager {
        private BitSet bufferBitMap;
        private ByteBuffer masterBuffer;
        private ByteBuffer[] buffers;
        private int bitMapSize;

        public CVMBufferManager() {
            masterBuffer = byteBufferFactory.allocate(maxCacheSize);
            bitMapSize = maxCacheSize / blockSize;
            buffers = new ByteBuffer[bitMapSize];
            bufferBitMap = new BitSet(bitMapSize);
            bufferBitMap.set(0, bitMapSize);
        }
        
        ByteBuffer allocateBuffer(int nBlocks) {
            int b = findBlocks(nBlocks);
            if (b < 0) {
                b = findBlocks(1);
                nBlocks = 1;
            }
            assert b >= 0;
            masterBuffer.position(b * blockSize);
            ByteBuffer result = masterBuffer.slice();
            result.limit(nBlocks * blockSize);
            buffers[b] = result;
            masterBuffer.position(0);
            return result;
        }
        
        void recycleBuffer(ByteBuffer buffer) {
            for (int i = 0; i < bitMapSize; i++) {
                if (buffers[i] == buffer) {
                    int nBlocks = buffer.remaining() / blockSize;
                    bufferBitMap.set(i, i + nBlocks);
                    buffers[i] = null;
                    return;
                }
            }
            assert false;
        }
        
        /**
         * Tries to find n contiguous blocks.
         * 
         * @param n
         * @return
         */
        private int findBlocks(int n) {
            // first fit
            int i = 0;
            while (i < bitMapSize) {
                int j = bufferBitMap.nextSetBit(i);
                if (j < 0) {
                    break;
                }
                i = bufferBitMap.nextClearBit(j);
                if ((i - j) >= n) {
                    bufferBitMap.clear(j, j + n);
                    return j;
                }
            }
            return -1;
        }
    }
}
