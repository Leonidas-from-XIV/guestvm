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
 */
package test.jnode.fs.ext2;

import java.io.*;
import java.nio.*;
import java.util.Iterator;

import org.jnode.driver.*;
import org.jnode.driver.block.*;
import org.jnode.fs.*;
import org.jnode.fs.ext2.*;

import com.sun.guestvm.tools.ext2.*;

public class Ext2Test {

    /**
     * @param args
     */
    public static void main(String[] args) {
        final String[] ops = new String[10];
        final String[] values = new String[10];
        int opCount = 0;
        values[0] = "0";
        // Checkstyle: stop modified control variable check
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.equals("op")) {
                ops[opCount++] = args[++i];
                values[opCount] = values[opCount - 1];
            } else if (arg.equals("v")) {
                values[opCount] = args[++i];
            }
        }
        // Checkstyle: resume modified control variable check

        Device device = null;
        org.jnode.fs.FileSystem<?> fs = null;
        FSDirectory root = null;
        try {
            final Ext2FileSystemType fsType = new Ext2FileSystemType();
            for (int j = 0; j < opCount; j++) {
                final String op = ops[j];
                final String value = values[j];
                if (op.equals("device")) {
                    device = new Device("fileDevice");
                    device.registerAPI(FSBlockDeviceAPI.class, new JNodeFSBlockDeviceAPIFileImpl(value));
                } else if (op.equals("format")) {
                    final org.jnode.fs.ext2.Ext2FileSystem ext2fs = fsType.create(device, false);
                    ext2fs.create(BlockSize._4Kb);
                    fs = ext2fs;
                } else if (op.equals("mount")) {
                    fs = fsType.create(device, false);
                } else if (op.equals("totalspace")) {
                    System.out.println("total space is " + fs.getTotalSpace());
                } else if (op.equals("freespace")) {
                    System.out.println("free space is " + fs.getFreeSpace());
                } else if (op.equals("root")) {
                    final FSEntry fsEntry = fs.getRootEntry();
                    System.out.println("root is " + fsEntry.getName());
                    root = fsEntry.getDirectory();
                } else if (op.equals("ls")) {
                    final Iterator<? extends FSEntry> iter = root.iterator();
                    while (iter.hasNext()) {
                        final FSEntry fsEntry = iter.next();
                        System.out.println("  " + fsEntry.getName());
                    }
                } else if (op.equals("read")) {
                    readFile(fs.getRootEntry().getDirectory(), value);
                }
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }  finally {
            if (fs != null) {
                try {
                    fs.close();
                } catch (IOException ex) {
                    System.out.println(ex);
                }
            }
        }

    }

    private static void readFile(FSDirectory root, String path) throws IOException {
        final String[] parts = path.split("/");
        final String name = parts[parts.length - 1];
        final FSDirectory dir = root;
        final FSEntry fsEntry = dir.getEntry(name);
        if (dir == null) {
            throw new FileNotFoundException(name);
        }
        if (fsEntry.isDirectory()) {
            throw new IOException(name + " is a directory");
        }
        final FSFile fsFile = fsEntry.getFile();
        final long fileOffset = 0;
        final ByteBuffer ext2Buf = ByteBuffer.allocateDirect(4096);
        final byte[] buffer = new byte[4096];
        long length = fsFile.getLength();
        while (length > 0) {
            int n = buffer.length;
            if (length < n) {
                n = (int) length;
                ext2Buf.limit(n);
            }
            fsFile.read(fileOffset, ext2Buf);
            length -= n;
            ext2Buf.position(0);
            ext2Buf.get(buffer, 0, n);
            System.out.write(buffer, 0, n);
        }

    }
}
