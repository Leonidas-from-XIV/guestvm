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
 * $Id: Ext2File.java 4975 2009-02-02 08:30:52Z lsantha $
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

import java.util.logging.Level;

import com.sun.max.ve.logging.Logger;

import org.jnode.fs.FileSystemException;
import org.jnode.fs.ReadOnlyFileSystemException;
import org.jnode.fs.spi.AbstractFSFile;
import org.jnode.util.ByteBufferUtils;

/**
 * @author Andras Nagy
 */
public class Ext2File extends AbstractFSFile {

    INode iNode;

    private static final Logger log = Logger.getLogger(Ext2File.class.getName());

    public Ext2File(INode iNode) {
        super(iNode.getExt2FileSystem());
        this.iNode = iNode;
        //log.setLevel(Level.FINEST);
    }

    public INode getINode() {
        return iNode;
    }

    /**
     * @see org.jnode.fs.FSFile#getLength()
     */
    public long getLength() {
        //log.log(Level.FINEST, "getLength(): "+iNode.getSize());
        return iNode.getSize();
    }

    private long getLengthInBlocks() {
        return iNode.getSizeInBlocks();
    }

    /**
     * @see org.jnode.fs.FSFile#setLength(long)
     */
    public void setLength(long length) throws IOException {
        if (!canWrite())
            throw new ReadOnlyFileSystemException("FileSystem or File is readonly");

        long blockSize = iNode.getExt2FileSystem().getBlockSize();


        iNode = iNode.syncAndLock();
        synchronized (iNode) {
            final long fileLength = getLength();
            try {
                 //if length<fileLength, then the file is truncated
                if (length < fileLength) {
                    long blockNr = length / blockSize;
                    long blockOffset = length % blockSize;
                    long nextBlock;
                    if (blockOffset == 0)
                        nextBlock = blockNr;
                    else
                        nextBlock = blockNr + 1;

                    for (long i = iNode.getAllocatedBlockCount() - 1; i >= nextBlock; i--) {
                        if (log.isLoggable(Level.FINEST)) {
                            doLog(Level.FINEST, "setLength(): freeing up block " + i
                                    + " of inode");
                        }
                        iNode.freeDataBlock(i);
                    }
                    iNode.setSize(length);

                    iNode.setMtime(System.currentTimeMillis() / 1000);

                    return;
                }

                //if length>fileLength, then new blocks are allocated for the
                // file
                //The content of the new blocks is undefined (see the
                // setLength(long i)
                //method of java.io.RandomAccessFile
                if (length > fileLength) {
                    long len = length - fileLength;
                    long blocksAllocated = getLengthInBlocks();
                    long bytesAllocated = fileLength;
                    long bytesCovered = 0;
                    while (bytesCovered < len) {
                        long blockIndex = (bytesAllocated + bytesCovered) / blockSize;
                        long blockOffset = (bytesAllocated + bytesCovered) % blockSize;
                        long newSection = Math.min(len - bytesCovered, blockSize - blockOffset);

                        //allocate a new block if needed
                        if (blockIndex >= blocksAllocated) {
                            iNode.allocateDataBlock(blockIndex);
                            blocksAllocated++;
                        }

                        bytesCovered += newSection;
                    }
                    iNode.setSize(length);

                    iNode.setMtime(System.currentTimeMillis() / 1000);

                    return;
                }
            } catch (Throwable ex) {
                final IOException ioe = new IOException();
                ioe.initCause(ex);
                throw ioe;
            } finally {
                //setLength done, unlock the inode from the cache
                iNode.decLocked();
            }
        }
    }

     public void read(long fileOffset, ByteBuffer destBuf) throws IOException {
        final int toRead = destBuf.remaining();

        iNode = iNode.syncAndLock();
        synchronized (iNode) {
            try {
                long fileLength = getLength();
                if (toRead + fileOffset > fileLength)
                    throw new IOException("Can't read past the file!");
                long blockSize = iNode.getExt2FileSystem().getBlockSize();
                long lastBlockNr = (fileLength - 1) / blockSize;
                long bytesRead = 0;
                while (bytesRead < toRead) {
                    long blockNr = (fileOffset + bytesRead) / blockSize;
                    long blockOffset = (fileOffset + bytesRead) % blockSize;
                    long copyLength = Math.min(toRead - bytesRead, blockSize - blockOffset);

                    if (log.isLoggable(Level.FINEST)) {
                        doLog(Level.FINEST, "blockNr: "+blockNr+", blockOffset: "+blockOffset+
                    		      ", copyLength: "+copyLength+", bytesRead: "+bytesRead);
                    }

                    ByteBuffer blockBuffer = iNode.getDataBlock(blockNr, lastBlockNr - blockNr);
                    ByteBufferUtils.buffercopy(blockBuffer, (int) blockOffset, destBuf, (int) bytesRead, (int) copyLength, false);

                    bytesRead += copyLength;
                }
            } catch (Throwable ex) {
                final IOException ioe = new IOException();
                ioe.initCause(ex);
                throw ioe;
            } finally {
                //read done, unlock the inode from the cache
                iNode.decLocked();
            }
        }
    }

    /**
     * Write into the file. fileOffset is between 0 and getLength() (see the
     * methods write(byte[], int, int), setPosition(long), setLength(long) in
     * org.jnode.fs.service.def.FileHandleImpl)
     *
     * @see org.jnode.fs.FSFile#write(long, byte[], int, int)
     */
    //public void write(long fileOffset, byte[] src, int off, int len)
    public void write(long fileOffset, ByteBuffer srcBuf) throws IOException {
        final int len = srcBuf.remaining();
        final int off = 0;
        
        if (fileSystem.isReadOnly()) {
            throw new ReadOnlyFileSystemException("write in readonly filesystem");
        }

        iNode = iNode.syncAndLock();
        synchronized (iNode) {
            try {
                if (fileOffset > getLength())
                    throw new IOException("Can't write beyond the end of the file! (fileOffset: " + fileOffset +
                            ", getLength()" + getLength());
                if (off + len > srcBuf.remaining())
                    throw new IOException("src is shorter than what you want to write");

                if (log.isLoggable(Level.FINEST)) {
                    doLog(Level.FINEST, "write(fileOffset=" + fileOffset + ", src, off, len="
                            + len + ")");
                }

                final long blockSize = iNode.getExt2FileSystem().getBlockSize();
                long blocksAllocated = iNode.getAllocatedBlockCount();
                long bytesWritten = 0;
                while (bytesWritten < len) {
                    long blockIndex = (fileOffset + bytesWritten) / blockSize;
                    long blockOffset = (fileOffset + bytesWritten) % blockSize;
                    long copyLength = Math.min(len - bytesWritten, blockSize - blockOffset);

                    //If only a part of the block is written, then read the
                    //block and update its contents with the data in src. If the
                    //whole block is overwritten, then skip reading it.
                    ByteBuffer destBuf;
                    if (!((blockOffset == 0) && (copyLength == blockSize)) && (blockIndex < blocksAllocated))
                        destBuf = iNode.getDataBlock(blockIndex);
                    else
                        destBuf = ByteBuffer.allocate((int) blockSize);

                    ByteBufferUtils.buffercopy(srcBuf, (int) (off + bytesWritten), destBuf, (int) blockOffset, (int) copyLength, true);

                    //allocate a new block if needed
                    if (blockIndex >= blocksAllocated) {
                        try {
                            iNode.allocateDataBlock(blockIndex);
                        } catch (FileSystemException ex) {
                            final IOException ioe = new IOException("Internal filesystem exception");
                            ioe.initCause(ex);
                            throw ioe;
                        }
                        blocksAllocated++;
                    }

                    //write the block
                    iNode.writeDataBlock(blockIndex, destBuf);

                    bytesWritten += copyLength;
                }
                // if we have extended the file then increase the size
                if (fileOffset + len > getLength()) {
                    iNode.setSize(fileOffset + len);
                }

                iNode.setMtime(System.currentTimeMillis() / 1000);
            } catch (Throwable ex) {
                final IOException ioe = new IOException();
                ioe.initCause(ex);
                throw ioe;
            } finally {
                //write done, unlock the inode from the cache
                iNode.decLocked();
            }
        }
    }

    /**
     * Flush any cached data to the disk.
     *
     * @throws IOException
     */
    public void flush() throws IOException {
        if (log.isLoggable(Level.FINEST)) {
            doLog(Level.FINEST, "Ext2File.flush()");
        }
        iNode.update();
        //update the group descriptors and superblock: needed if blocks have
        //been allocated or deallocated
        iNode.getExt2FileSystem().updateFS();
    }

    private void doLog(Level level, String msg) {
        log.log(level, fileSystem.getDevice().getId() + " " + msg);
    }
}
