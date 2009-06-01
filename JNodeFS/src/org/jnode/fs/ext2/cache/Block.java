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
 * Modified from JNode original by Mick Jordan, May 2009.
 *
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

import com.sun.guestvm.logging.Logger;
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
