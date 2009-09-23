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
package com.sun.guestvm.tools.ext2;

import java.io.*;
import java.nio.*;
import java.util.Iterator;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jnode.driver.*;
import org.jnode.driver.block.*;
import org.jnode.fs.*;
import org.jnode.fs.ext2.*;

/**
 * Tools for actions, e.g., format, copyin, copyout, mkdir, mkfile, ls, rm, on an ext2 file system stored in a disk image file.
 *
 * Usage:
 * format -disk imagefile
 * copy[in] -disk imagefile -from file -ext2path tofile
 * copyout -disk imagefile -ext2path fromfile -to file
 * ls -disk imagefile -from file -ext2path dir
 * mkdir -disk imagefile -ext2path dir
 * mkfile -disk imagefile -ext2path file
 * rm disk imagefile -ext2path file
 *
 * The keyword arguments can be in any order and, if the command is preceded by -c,
 * it can also.
 *
 * @author Mick Jordan
 *
 */
public class Ext2FileTool {

    static boolean _verbose = false;
    static boolean _veryVerbose = false;
    static boolean _recurse = false;
    static boolean _hidden = false;
    static boolean _details = false;
    static SimpleDateFormat _dateFormat = new SimpleDateFormat();
    static String[] _commands = {"format", "copy", "copyin", "copyout", "ls", "mkdir", "mkfile", "rm"};

    /**
     * @param args
     */
    public static void main(String[] args) {
        String fromFile = null;
        String diskFile = null;
        String ext2Path = null;
        String command = null;

        // Checkstyle: stop modified control variable check
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (i == 0 && !arg.startsWith("-")) {
                command = args[i];
            } else if (arg.equals("-c")) {
                command = args[++i];
            } else if (arg.equals("-from") || arg.equals("-to")) {
                fromFile = args[++i];
            } else if (arg.equals("-disk")) {
                diskFile = args[++i];
            } else if (arg.equals("-ext2path")) {
                ext2Path = args[++i];
            } else if (arg.equals("-v")) {
                _verbose = true;
            } else if (arg.equals("-vv")) {
                _veryVerbose = true;
            } else if (arg.equals("-r")) {
                _recurse = true;
            } else if (arg.equals("-a")) {
                _hidden = true;
            } else if (arg.equals("-l")) {
                _details = true;
            } else {
                System.out.println("unknown argument " + arg);
                usage();
                return;
            }
        }
        // Checkstyle: resume modified control variable check

        if (!checkCommand(command)) {
            return;
        }

        if (diskFile == null)  {
            usage();
            return;
        }

        if (!(new File(diskFile).exists())) {
            System.out.println("disk file " + diskFile + " does not exist");
            return;
        }

        org.jnode.fs.FileSystem<?> fs = null;
        try {
            final Device device = new Device("fileDevice");
            device.registerAPI(FSBlockDeviceAPI.class, new JNodeFSBlockDeviceAPIFileImpl(diskFile));
            final Ext2FileSystemType fsType = new Ext2FileSystemType();

            if (command.equals("format")) {
                final org.jnode.fs.ext2.Ext2FileSystem ext2fs = fsType.create(device, false);
                ext2fs.create(BlockSize._4Kb);
                return;
            }

            if (ext2Path == null) {
                usage();
                return;
            }

            fs = fsType.create(device, false);
            final FSDirectory root = fs.getRootEntry().getDirectory();
            final Match m = match(ext2Path, root);
            if (m == null) {
                throw new IOException("path " + ext2Path + " not found");
            }
            if (command.equals("copyin") || command.equals("copy")) {
                if (_recurse) {
                    copyTree(m, fromFile, ext2Path);
                } else {
                    copyIn(m, fromFile, ext2Path);
                }
            } else if (command.equals("copyout")) {
                copyOut(m, fromFile, ext2Path);
            } else if (command.equals("mkdir")) {
                mkdir(m, ext2Path);
            } else if (command.equals("mkfile")) {
                mkfile(m, ext2Path);
            } else if (command.equals("ls")) {
                ls(m, ext2Path);
            } else if (command.equals("rm")) {
                remove(m, ext2Path);
            }
        } catch (Exception ex) {
            System.out.println(ex);
            ex.printStackTrace();
        } finally {
            if (fs != null) {
                try {
                    fs.close();
                } catch (IOException ex) {
                    System.out.println(ex);
                    ex.printStackTrace();
                }
            }
        }
    }

    private static void usage() {
        System.out.println("usage: ");
        System.out.println("  [-c] copy[in] -disk diskfile -from file -ext2path path");
        System.out.println("  [-c] copy[out -disk diskfile -ext2path path -to file");
        System.out.println("  [-c] mkdir -disk diskfile -ext2path dir");
        System.out.println("  [-c] mkfile -disk diskfile -ext2path file");
        System.out.println("  [-c] ls -disk diskfile -ext2path file [-l] [-a] [-r]");
        System.out.println("  [-c] rm -disk diskfile -ext2path file");
    }

    private static boolean checkCommand(String command) {
        for (String c : _commands) {
            if (c.equals(command)) {
                return true;
            }
        }
        System.out.println("unknown command: " + command);
        usage();
        return false;
    }

    static class Match {
        FSDirectory _d;
        String _tail;
        Match(FSDirectory d, String tail) {
            _d = d;
            _tail = tail;
        }

        FSEntry matchTail() throws IOException {
            return _d.getEntry(_tail);
        }
    }

    /**
     * Matches the sequence of names in parts against the directory hierarchy.
     * @param parts
     * @param complete
     * @return Match object or null if no match
     */
    private static Match match(String name, FSDirectory root) throws IOException {
        final String[] parts = name.split(File.separator);
        if (parts.length == 0) {
            return new Match(root, ".");
        }
        FSDirectory d = root;
        for (int i = 1; i < parts.length - 1; i++) {
            final FSEntry fsEntry = d.getEntry(parts[i]);
            if (fsEntry == null || fsEntry.isFile()) {
                return null;
            }
            d = fsEntry.getDirectory();
        }
        return new Match(d, parts[parts.length - 1]);
    }

    private static void mkdir(Match m, String ext2Path) throws IOException {
        if (_verbose) {
            System.out.println("creating directory " + ext2Path);
        }
        FSEntry fsEntry = m._d.getEntry(m._tail);
        if (fsEntry == null) {
            fsEntry = m._d.addDirectory(m._tail);
        } else {
            throw new IOException(ext2Path + " already exists");
        }
    }

    private static void mkfile(Match m, String ext2Path) throws IOException {
        if (_verbose) {
            System.out.println("creating file " + ext2Path);
        }
        FSEntry fsEntry = m._d.getEntry(m._tail);
        if (fsEntry == null) {
            fsEntry = m._d.addFile(m._tail);
        } else {
            throw new IOException(ext2Path + " already exists");
        }
    }

    private static void copyIn(Match m, String fromFile, String ext2Path) throws IOException {
        FSEntry fsEntry = m.matchTail();
        if (fsEntry != null) {
            if (fsEntry.isDirectory()) {
                final FSDirectory fsEntryDir = fsEntry.getDirectory();
                final String[] parts = fromFile.split(File.separator);
                FSEntry subFsEntry = fsEntryDir.getEntry(parts[parts.length - 1]);
                if (subFsEntry == null) {
                    subFsEntry = fsEntryDir.addFile(parts[parts.length - 1]);
                }
                fsEntry = subFsEntry;
            }
        } else {
            fsEntry = m._d.addFile(m._tail);
        }
        copyInFile(fromFile, fsEntry.getFile());
    }

    private static void copyTree(Match m, String fromFilename, String ext2Path) throws IOException {
        final File fromFile = new File(fromFilename);
        if (fromFile.isFile()) {
            copyIn(m, fromFilename, ext2Path);
            return;
        }
        if (!fromFile.exists()) {
            throw new IOException(fromFilename + " does not exist");
        }
        FSEntry fsEntry = m.matchTail();
        if (fsEntry == null) {
            throw new IOException(ext2Path + " does not exist");
        } else  if (fsEntry.isFile()) {
            throw new IOException(ext2Path + " is a file, not a directory");
        }
        final FSDirectory fsDir = fsEntry.getDirectory();
        fsEntry = fsDir.addDirectory(fromFile.getName());
        copyDir(fromFile, fsEntry.getDirectory());
    }

    private static void copyDir(File dir, FSDirectory ext2Dir) throws IOException {
        if (_verbose) {
            System.out.println("copying directory " + dir.getAbsolutePath());
        }
        final File[] files = dir.listFiles();
        for (File file : files) {
            final String name = file.getName();
            if (file.isFile()) {
                final FSEntry fsEntry = ext2Dir.addFile(name);
                copyInFile(file.getAbsolutePath(), fsEntry.getFile());
            } else {
                final FSEntry fsEntry = ext2Dir.addDirectory(name);
                copyDir(file, fsEntry.getDirectory());
            }
        }
    }

    private static void copyInFile(String fileName, FSFile ext2File) throws IOException {
        if (_verbose) {
            System.out.println("copying file " + fileName);
        }
        BufferedInputStream is = null;
        try {
            final byte[] buffer = new byte[4096];
            final java.nio.ByteBuffer ext2Buffer = ByteBuffer.allocateDirect(4096);
            is = new BufferedInputStream(new FileInputStream(fileName));
            int n;
            long fileOffset = 0;
            while ((n = is.read(buffer)) > 0) {
                ext2Buffer.put(buffer, 0, n);
                ext2Buffer.position(0);
                ext2Buffer.limit(n);
                ext2File.write(fileOffset, ext2Buffer);
                fileOffset += n;
                ext2Buffer.position(0);
                ext2Buffer.limit(4096);
            }
            ext2File.flush();
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    private static void copyOut(Match m, String toFileName, String ext2Path) throws IOException {
        final FSEntry fsEntry = m.matchTail();
        if (fsEntry != null) {
            if (fsEntry.isDirectory()) {
                throw new IOException("cannot copy a directory");
            } else {
                final File toFile = new File(toFileName);
                String copyFileName = toFileName;
                if (toFile.isDirectory()) {
                    copyFileName = toFileName + File.separator + m._tail;
                }
                copyOutFile(copyFileName, fsEntry.getFile());
            }
        } else {
            throw new IOException(ext2Path + " not found");
        }
    }

    private static void copyOutFile(String fileName, FSFile fsFile) throws IOException {
        BufferedOutputStream os = null;
        try {
            final byte[] buffer = new byte[4096];
            final java.nio.ByteBuffer ext2Buffer = ByteBuffer.allocateDirect(4096);
            os = new BufferedOutputStream(new FileOutputStream(fileName));
            long fileOffset = 0;
            long length = fsFile.getLength();
            while (length > 0) {
                int n = buffer.length;
                if (length < n) {
                    n = (int) length;
                }
                ext2Buffer.limit(n);
                fsFile.read(fileOffset, ext2Buffer);
                length -= n;
                fileOffset += n;
                ext2Buffer.position(0);
                ext2Buffer.get(buffer, 0, n);
                os.write(buffer, 0, n);
                ext2Buffer.position(0);
            }
        } finally {
            if (os != null) {
                os.close();
            }
        }
    }

    private static void remove(Match m, String ext2Path) throws IOException {
        final FSEntry tailEntry = m.matchTail();
        if (tailEntry == null) {
            throw new IOException(ext2Path + " not found");
        } else if (!tailEntry.isDirectory()) {
            final FSEntry fsEntry = m._d.getEntry(m._tail);
            assert fsEntry != null;
            m._d.remove(m._tail);
        }

    }

    private static void ls(Match m, String ext2Path) throws IOException {
        final FSEntry tailEntry = m.matchTail();
        if (tailEntry == null) {
            throw new IOException(ext2Path + " not found");
        } else if (tailEntry.isDirectory()) {
            ls(tailEntry, ext2Path);
        } else {
            throw new IOException(ext2Path + " is not a directory");
        }
    }

    private static void ls(FSEntry fsDirEntry, String prefix) throws IOException {
        final FSDirectory fsDir = fsDirEntry.getDirectory();
        if (_recurse) {
            System.out.println(prefix + ":");
        }
        Iterator<? extends FSEntry> iter = fsDir.iterator();
        while (iter.hasNext()) {
            final FSEntry fsEntry = iter.next();
            final String name = fsEntry.getName();
            if (_hidden || !name.startsWith(".")) {
                if (_details) {
                    System.out.print(rwx(fsEntry));
                    if (fsEntry.isFile()) {
                        final Ext2File fsFile = (Ext2File) fsEntry.getFile();
                        System.out.print("  " + fsFile.getLength());
                        System.out.print("  " + fsFile.getINode().getINodeNr());
                    }
                    System.out.print("  " + _dateFormat.format(new Date(fsEntry.getLastModified())) + "  ");
                }
                System.out.println(name);
            }
        }
        if (_recurse) {
            iter = fsDir.iterator();
            while (iter.hasNext()) {
                final FSEntry fsEntry = iter.next();
                if (fsEntry.isDirectory()) {
                    final String name = fsEntry.getName();
                    if (name.equals(".") || name.equals("..")) {
                        continue;
                    }
                    if (_hidden || !name.startsWith(".")) {
                        System.out.println();
                        ls(fsEntry, prefix + (prefix.equals(File.separator) ? "" : File.separator) + name);
                    }
                }
            }
        }
    }

    private static String rwx(FSEntry fsEntry) throws IOException {
        final FSAccessRights fsa = fsEntry.getAccessRights();
        String result = fsEntry.isDirectory() ? "d" : "-";
        result += fsa.canRead() ? "r" : "-";
        result += fsa.canWrite() ? "w" : "-";
        result += fsa.canExecute() ? "x" : "-";
        return result + "   ";
    }
}
