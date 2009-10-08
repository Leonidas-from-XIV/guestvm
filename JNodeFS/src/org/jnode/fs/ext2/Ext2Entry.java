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
 * $Id: Ext2Entry.java 4975 2009-02-02 08:30:52Z lsantha $
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

import java.util.logging.Level;
import com.sun.guestvm.logging.Logger;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.spi.*;

/**
 * @author Andras Nagy
 *
 * In case of a directory, the data will be parsed to get the file-list by
 * Ext2Directory. In case of a regular file, no more processing is needed.
 *
 * TODO: besides getFile() and getDirectory(), we will need getBlockDevice()
 * getCharacterDevice(), etc.
 */
public class Ext2Entry extends AbstractFSEntry {

    private final Logger log = Logger.getLogger(getClass().getName());
    private INode iNode = null;
    private int type;

    public Ext2Entry(INode iNode, String name, int type, Ext2FileSystem fs, AbstractFSDirectory parent) {
        super(fs, parent, name, getFSEntryType(name, iNode));
        this.iNode = iNode;
        this.type = type;

	//log.setLevel(Level.INFO);

	if (log.isLoggable(Level.FINEST)) {
	    log.log(Level.FINEST, "Ext2Entry(iNode, name): name="+name+
		    (isDirectory()?" is a directory ":"")+
		    (isFile()?" is a file ":""));
	}
    }

    public long getLastChanged() throws IOException {
        return iNode.getCtime();
    }

    public long getLastModified() throws IOException {
        return iNode.getMtime();
    }

    public long getLastAccessed() throws IOException {
        return iNode.getAtime();
    }

    public void setLastChanged(long lastChanged) throws IOException {
        iNode.setCtime(lastChanged);
    }

    public void setLastModified(long lastModified) throws IOException {
        iNode.setMtime(lastModified);
    }

    public void setLastAccessed(long lastAccessed) throws IOException {
        iNode.setAtime(lastAccessed);
    }

    /**
     * Returns the type.
     *
     * @return int type. Valid types are Ext2Constants.EXT2_FT_*
     */
    public int getType() {
        return type;
    }

    INode getINode() {
        return iNode;
    }

    private static int getFSEntryType(String name, INode iNode) {
        int mode = iNode.getMode() & Ext2Constants.EXT2_S_IFMT;

        if ("/".equals(name))
            return AbstractFSEntry.ROOT_ENTRY;
        else if (mode == Ext2Constants.EXT2_S_IFDIR)
            return AbstractFSEntry.DIR_ENTRY;
        else if (mode == Ext2Constants.EXT2_S_IFREG || mode == Ext2Constants.EXT2_FT_SYMLINK)
            return AbstractFSEntry.FILE_ENTRY;
        else
            return AbstractFSEntry.OTHER_ENTRY;
    }
}
