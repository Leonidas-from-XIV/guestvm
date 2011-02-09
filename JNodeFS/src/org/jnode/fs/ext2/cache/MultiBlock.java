package org.jnode.fs.ext2.cache;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

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
