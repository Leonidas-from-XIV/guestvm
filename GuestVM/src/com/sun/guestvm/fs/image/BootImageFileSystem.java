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
package com.sun.guestvm.fs.image;

import java.io.*;
import java.util.*;

import com.sun.max.annotate.HOSTED_ONLY;
import com.sun.max.io.Files;
import com.sun.max.program.ProgramError;
import com.sun.max.program.Trace;
import com.sun.max.vm.MaxineVM;
import com.sun.guestvm.error.GuestVMError;
import com.sun.guestvm.fs.*;

/**
 * A (read-only)file system that is pre-loaded into a guestvm image.
 *
 * @author Mick Jordan
 *
 */
public class BootImageFileSystem extends UnimplementedFileSystemImpl implements VirtualFileSystem {

    public static final String BOOTIMAGE_FILESYSTEM_PROPERTY = "guestvm.bootimage.fs.contents";
    private static BootImageFileSystem _singleton = new BootImageFileSystem();
    private static final int S_IFREG = 0x8000;
    private static final int S_IFDIR = 0x4000;

    private static byte[][] _openFiles = new byte[64][];
    
    private static String imageFSPrefix = "";
    private static Map<String, byte[]> _fileSystem = new HashMap<String, byte[]>();

    static {
        final String prop = System.getProperty(BOOTIMAGE_FILESYSTEM_PROPERTY);
        if (prop != null) {
            BufferedReader bs = null;
            try {
                bs = new BufferedReader(new FileReader(prop));
                while (true) {
                    final String line = bs.readLine();
                    if (line == null) {
                        break;
                    }
                    if (line.length() == 0 || line.charAt(0) == '#') {
                        continue;
                    }
                    String argument = null;
                    String command;
                    final int ix = line.indexOf(' ');
                    if (ix > 0) {
                        command = line.substring(0, ix);
                        argument = line.substring(ix + 1).trim();
                    } else {
                        command = line;
                    }
                    if (command.equals("path")) {
                        doImageFile(argument);
                    } else if (command.equals("prefix")) {
                        doImagefsPrefix(argument);
                    } else {
                        GuestVMError.unexpected("BootImageFileSystem: unknown command: " + command + " in file " + prop);
                    }
                }
            } catch (IOException ex) {

            } finally {
                if (bs != null) {
                    try {
                        bs.close();
                    } catch (IOException ex) {
                    }
                }
            }
        }
    }
       
    @HOSTED_ONLY
    public static void putImageFile(File file, File asFile) {
        try {
            String path = asFile.getPath();
            if (asFile.isAbsolute() || imageFSPrefix.equals("")) {
                path = imageFSPrefix + path;
            } else {
                path = imageFSPrefix + File.separator + path;
            }
            _fileSystem.put(path, Files.toBytes(file));
            Trace.line(1, "added file " + file.getPath() + " to boot image file system as path " + path);
        } catch (IOException ioException) {
            ProgramError.unexpected("Error reading from " + file + ": " + ioException);
        }
    }

    @HOSTED_ONLY
    protected static void putDirectory(File dir, File asFile) {
        final String[] files = dir.list();
        for (String path : files) {
            final File child = new File(dir, path);
            final File asChild = new File(asFile, path);
            if (child.isDirectory()) {
                putDirectory(child, asChild);
            } else {
                putImageFile(child, asChild);
            }
        }
    }
    
    @HOSTED_ONLY
    protected static void doImageFile(String argument) {
        String pathName = argument;
        String asName = argument;
        final int ix = argument.lastIndexOf(' ');
        if (ix > 0) {
            pathName = argument.substring(0, ix);
            asName = argument.substring(ix + 1);
        }
        final File file = new File(pathName);
        final File asFile = new File(asName);
        if (file.exists()) {
            if (file.isDirectory()) {
                putDirectory(file, asFile);
            } else {
                putImageFile(file, asFile);
            }
        } else {
            ProgramError.unexpected("failed to find file: " + argument);
        }
    }

    @HOSTED_ONLY
    protected static void doImagefsPrefix(String argument) {
        final int last = argument.length() - 1;
        if (argument.charAt(last) == File.separatorChar) {
            imageFSPrefix = argument.substring(0, last);
        } else {
            imageFSPrefix = argument;
        }
        Trace.line(1, "setting boot image file system prefix to " + imageFSPrefix);
    }

    public static String getPath() {
        return imageFSPrefix;
    }

    @Override
    public void close() {

    }

    private static synchronized int getFd(byte[] data) {
        for (int i = 0; i < _openFiles.length; i++) {
            if (_openFiles[i] == null) {
                _openFiles[i] = data;
                return i;
            }
        }
        return -1;
    }

    public static BootImageFileSystem create() {
        return _singleton;
    }

    @Override
    public String canonicalize0(String path) throws IOException {
        // TODO correct implementation
        return path;
    }

    @Override
    public boolean checkAccess(String path, int access) {
        if (!(_fileSystem.containsKey(path) || isDir(path))) {
            return false;
        }
        switch (access) {
            case ACCESS_READ:
                return true;
            case ACCESS_WRITE:
                return false;
            case ACCESS_EXECUTE:
                return false;
        }
        return false;
    }

    @Override
    public boolean createDirectory(String path) {
        return false;
    }

    @Override
    public boolean createFileExclusively(String path) throws IOException {
        if (MaxineVM.isHosted()) {
            if (_fileSystem.containsKey(path)) {
                return false;
            }
            _fileSystem.put(path, null);
        }

        return false;
    }

    @Override
    public boolean delete0(String path) {
        return false;
    }

    @Override
    public long getLastModifiedTime(String path) {
        // Not recorded
        return 0;
    }

    @Override
    public long getLength(String path) {
        final byte[] data = _fileSystem.get(path);
        return data == null ? 0 : data.length;
    }

    private boolean isDir(String path) {
        final int dirLength = path.length();
        final Set<String> keys = _fileSystem.keySet();
        for (String entry : keys) {
            if (entry.startsWith(path)) {
                if (entry.charAt(dirLength) == File.separatorChar) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int getMode(String path) {
        if (_fileSystem.containsKey(path)) {
            return S_IFREG | S_IREAD;
        }
        // maybe a directory?
        if (isDir(path)) {
            return S_IFDIR | S_IREAD | S_IEXEC;
        }
        return -1;
    }

    @Override
    public long getSpace(String path, int t) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String[] list(String path) {
        final String dir = path;
        final List<String> results = new ArrayList<String>();
        final int dirLength = dir.length();
        final Set<String> keys = _fileSystem.keySet();
        for (String keyPath : keys) {
            if (keyPath.startsWith(dir)) {
                if (keyPath.charAt(dirLength) == File.separatorChar) {
                    // matches dir exactly
                    final int ix = keyPath.indexOf(File.separatorChar, dirLength + 1);
                    if (ix < 0) {
                        // leaf file
                        results.add(keyPath.substring(dirLength + 1));
                    } else {
                        // subdirectory
                        final String subDir = keyPath.substring(dirLength + 1, ix);
                        boolean isNew = true;
                        for (String s : results) {
                            if (s.equals(subDir)) {
                                isNew = false;
                                break;
                            }
                        }
                        if (isNew) {
                            results.add(subDir);
                        }
                    }
                }
            }
        }
        return results.toArray(new String[results.size()]);
    }

    @Override
    public boolean rename0(String path1, String path2) {
        return false;
    }

    @Override
    public boolean setLastModifiedTime(String path, long time) {
        return false;
    }

    @Override
    public int setMode(String path, int mode) {
        return -ErrorDecoder.Code.EROFS.getCode();
    }

    // FileInputStream, FileOutputStream

    @Override
    public int read(int fd, long fileOffset) {
        final byte[] data = _openFiles[fd];
        if ((int) fileOffset >= data.length) {
            return -1;
        }
        return data[(int) fileOffset] & 0xFF;
    }

    @Override
    public int readBytes(int fd, byte[] bytes, int offset, int length, long fileOffset) {
        final byte[] data = _openFiles[fd];
        if ((int) fileOffset >= data.length) {
            return -1;
        }
        if (length > data.length - (int) fileOffset) {
            // CheckStyle: stop parameter assignment check
            length = data.length - (int) fileOffset;
            // CheckStyle: resume parameter assignment check
        }
        System.arraycopy(data, (int) fileOffset, bytes, offset, length);
        return length;

    }

    @Override
    public int write(int fd, int b, long fileOffset) {
        return -ErrorDecoder.Code.EROFS.getCode();

    }

    @Override
    public int writeBytes(int fd, byte[] bytes, int offset, int length, long fileOffset) {
        return -ErrorDecoder.Code.EROFS.getCode();
    }

    @Override
    public int open(String name, int flags) {
        if (flags == VirtualFileSystem.O_RDONLY) {
            final byte[] data = _fileSystem.get(name);
            if (data == null) {
                return -ErrorDecoder.Code.ENOENT.getCode();
            }
            return getFd(data);
        } else {
            return -ErrorDecoder.Code.EROFS.getCode();
        }
    }

    @Override
    public int close0(int fd) {
        _openFiles[fd] = null;
        return 0;
    }

    @Override
    public long getLength(int fd) {
        return _openFiles[fd].length;
    }

    @Override
    public int setLength(int fd, long length) {
        return -ErrorDecoder.Code.EROFS.getCode();
    }

    @Override
    public int available(int fd, long fileOffset) {
        final byte[] data = _openFiles[fd];
        if ((int) fileOffset >= data.length) {
            return 0;
        }
        return data.length - (int) fileOffset;
    }

    @Override
    public long skip(int fd, long n, long fileOffset) {
        // spec allows skip to go past end of file
        return n;
    }

}
