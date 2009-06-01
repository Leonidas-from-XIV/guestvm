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
 * $Id: Ext2Print.java 4975 2009-02-02 08:30:52Z lsantha $
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

import java.nio.ByteBuffer;

import java.util.logging.Level;
import com.sun.guestvm.logging.*;
import org.jnode.driver.block.BlockDeviceAPI;

/**
 * @author Andras Nagy
 */
public class Ext2Print {
    private static final Logger log = Logger.getLogger("EXT2");

    public static String hexFormat(int i) {
        String pad = "00000000";
        String res = Integer.toHexString(i);
        int len = Math.max(0, 8 - res.length());
        res = pad.substring(0, len) + res;
        return res;
    }

    private static int unsignedByte(byte i) {
        if (i < 0)
            return 256 + i;
        else
            return i;
    }

    public static String hexFormat(byte b) {
        int i = unsignedByte(b);
        String pad = "00";
        String res = Integer.toHexString(i);
        int len = Math.max(0, 2 - res.length());
        res = pad.substring(0, len) + res;
        return res;
    }

    public static void dumpData(BlockDeviceAPI api, int offset, int length) {
        byte[] data = new byte[length];
        try {
            api.read(offset, ByteBuffer.wrap(data));
        } catch (Exception e) {
            return;
        }
        int pageWidth = 16;
        for (int i = 0; i < length; i += pageWidth) {
            System.out.print(hexFormat(i) + ": ");
            for (int j = 0; j < pageWidth; j++)
                if (i + j < length) {
                    log.info(hexFormat(data[i + j]) + " ");
                    if ((i + j) % 4 == 3)
                        System.out.print(" - ");
                }
            System.out.println();
        }
    }
}
