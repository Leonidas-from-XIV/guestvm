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
 * $Id: Ext2FileSystemType.java 4975 2009-02-02 08:30:52Z lsantha $
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

import org.jnode.driver.Device;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.fs.BlockDeviceFileSystemType;
import org.jnode.fs.FileSystemException;
import org.jnode.partitions.PartitionTableEntry;
//import org.jnode.partitions.ibm.IBMPartitionTableEntry;
//import org.jnode.partitions.ibm.IBMPartitionTypes;

/**
 * @author Andras Nagy
 */
public class Ext2FileSystemType implements BlockDeviceFileSystemType<Ext2FileSystem> {
    public static final Class<Ext2FileSystemType> ID = Ext2FileSystemType.class;

    /**
     * @see org.jnode.fs.FileSystemType#create(Device, boolean)
     */
    public Ext2FileSystem create(Device device, boolean readOnly) throws FileSystemException {
        Ext2FileSystem fs = new Ext2FileSystem(device, readOnly, this);
	try {
            fs.read();
	} catch (FileSystemException ex) {
	    if (!(ex.getMessage().startsWith("Not ext2 superblock"))) {
		throw ex;
	    }
        }
	return fs;
    }

    /**
     * @see org.jnode.fs.FileSystemType#getName()
     */
    public String getName() {
        return "EXT2";
    }

    /**
     * @see org.jnode.fs.FileSystemType#supports(PartitionTableEntry, byte[], FSBlockDeviceAPI)
     */
    public boolean supports(PartitionTableEntry pte, byte[] firstSector, FSBlockDeviceAPI devApi) {
        /*
        if (pte != null) {
            if (pte instanceof IBMPartitionTableEntry) {
                if (((IBMPartitionTableEntry) pte).getSystemIndicator() != IBMPartitionTypes.PARTTYPE_LINUXNATIVE) {
                    return false;
                }
            }
        }
        */

        //need to check the magic
        ByteBuffer magic = ByteBuffer.allocate(2);
        try {
            devApi.read(1024 + 56, magic);
        } catch (IOException e) {
            return false;
        }
        return (Ext2Utils.get16(magic.array(), 0) == 0xEF53);
    }
}
