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
package com.sun.guestvm.fs.ext2;

import java.io.File;
import java.io.IOException;
import java.util.*;
import com.sun.guestvm.logging.*;
import com.sun.guestvm.jdk.*;

import org.jnode.driver.*;
import org.jnode.driver.block.*;
import org.jnode.fs.*;
import org.jnode.fs.ext2.*;

import com.sun.guestvm.error.GuestVMError;
import com.sun.guestvm.fs.*;

/**
 * This is the ext2 file from JNode system that GuestVM uses for the virtual disk device.
 * Note that file locking is unnecessary since the filesystem is not shared with any other
 * "processes".
 *
 * @author Mick Jordan
 *
 */

public final class Ext2FileSystem extends DefaultFileSystemImpl implements VirtualFileSystem {

    private FSEntry _rootEntry;
    private FSDirectory _root;
    private String _mountPath;
    private int _mountPathPrefixIndex;

    private List<FileData> _openFiles = new ArrayList<FileData>();
    private static final int BUFFER_SIZE = 4096;
    private static Logger _logger;

    private static class FileData {
        FSFile _fsFile;
        java.nio.ByteBuffer _byteBuffer;
        boolean _isWrite;

        FileData(FSFile fsFile, java.nio.ByteBuffer byteBuffer, boolean isWrite) {
            _fsFile = fsFile;
            _byteBuffer = byteBuffer;
            _isWrite = isWrite;
        }
    }

    @Override
    public void close() {
        try {
            _root.getFileSystem().close();
        } catch (IOException ex) {
            _logger.warning(ex.toString());
        }
    }

    @Override
    public String canonicalize0(String path) throws IOException {
        // TODO correct implementation
        return path;
    }

    private Ext2FileSystem(Device device, String mountPath, boolean readOnly) throws FileSystemException, IOException {
        if (_logger == null) {
            _logger = Logger.getLogger(getClass().getName());
        }
        final Ext2FileSystemType fsType = new Ext2FileSystemType();
        _rootEntry = fsType.create(device, readOnly).getRootEntry();
        _root = _rootEntry.getDirectory();
        _mountPath = mountPath;
        _mountPathPrefixIndex = _mountPath.split(File.separator).length;
    }

    /**
     * Access Ext2 file system on block device.
     * devPath syntax: /blk/N
     * @param devPath block device path
     * @return
     */
    public static Ext2FileSystem create(String devPath, String mountPath, boolean readOnly) {
        final int index = devPath.lastIndexOf('/');
        if (index > 0) {
            try {
                final int n = Integer.parseInt(devPath.substring(index + 1));
                final FSBlockDeviceAPI blkDevice = JNodeFSBlockDeviceAPIBlkImpl.create(n);
                if (blkDevice == null) {
                    return null;
                }
                final Device device = new Device("fsdev:" + devPath + ":" + mountPath);
                device.registerAPI(FSBlockDeviceAPI.class, blkDevice);
                return new Ext2FileSystem(device, mountPath, readOnly);
            } catch (NumberFormatException ex) {
                return null;
            } catch (FileSystemException ex) {
                return null;
            } catch (IOException ex) {
                return null;
            }
        }
        return null;
    }

    private int addFd(FSFile fsFile, boolean isWrite) {
        final int size = _openFiles.size();
        // We do not allocate direct byte buffers because the Ext2FileSystem copies data from byte buffers
        // representing disk blocks into the byte buffer we provide. I.e., this buffer is not directly involved in
        // low level I/O operations.
        final FileData fileData = new FileData(fsFile, java.nio.ByteBuffer.allocate(BUFFER_SIZE), isWrite);
        for (int i = 0; i < size; i++) {
            if (_openFiles.get(i) == null) {
                _openFiles.set(i, fileData);
                return i;
            }
        }
        _openFiles.add(fileData);
        return size;
    }


    @Override
    public boolean checkAccess(String path, int access) {
        try {
            final FSEntry fsEntry = matchPath(path);
            if (fsEntry == null) {
                return false;
            }
            final FSAccessRights fsRights = fsEntry.getAccessRights();
            switch (access) {
                case ACCESS_READ:
                    return fsRights.canRead();
                case ACCESS_WRITE:
                    return fsRights.canWrite();
                case ACCESS_EXECUTE:
                    return fsRights.canExecute();
            }
        } catch (IOException ex) {
            _logger.warning(ex.toString());
            return false;
        }
        return false;
    }

    @Override
    public int close0(int fd) {
        final FileData fileData = _openFiles.get(fd);
        try {
            if (fileData._isWrite) {
                fileData._fsFile.flush();
            }
        } catch (IOException ex) {
            _logger.warning(ex.toString());
        }
        fileData._byteBuffer = null;
        _openFiles.set(fd, null);
        return 0;
    }

    @Override
    public boolean createDirectory(String path) {
        return create(path, false);
    }

    @Override
    public boolean createFileExclusively(String path) throws IOException {
        return create(path, true);
    }

    private synchronized boolean create(String path, boolean isFile) {
        try {
            final Match m = match(path);
            if (m == null || m.matchTail() != null) {
                return false;
            }
            if (isFile) {
                m._d.addFile(m._tail);
            } else {
                m._d.addDirectory(m._tail);
            }
            return true;
        } catch (IOException ex) {
            _logger.warning(ex.toString());
            return false;
        }
    }

    @Override
    public boolean delete0(String path) {
        try {
            final Match m = match(path);
            if (m != null) {
                final FSEntry dd = m.matchTail();
                if (dd != null) {
                    // TODO permissions
                    m._d.remove(m._tail);
                    return true;
                }
            }
        } catch (IOException ex) {
            _logger.warning(ex.toString());
            return false;
        }
        return false;
    }

    @Override
    public long getLastModifiedTime(String path) {
        long result = 0;
        try {
            final  FSEntry fsEntry = matchPath(path);
            if (fsEntry != null) {
                result = fsEntry.getLastModified();
            }
        } catch (IOException ex) {
            _logger.warning(ex.toString());
        }
        return result;
    }

    @Override
    public long getLength(String path) {
        long result = 0;
        try {
            final FSEntry fsEntry = matchPath(path);
            if (fsEntry != null && fsEntry.isFile()) {
                result = fsEntry.getFile().getLength();
            }
        } catch (IOException ex) {
            _logger.warning(ex.toString());
        }
        return result;
    }

    @Override
    public long getLength(int fd) {
        final FileData fileData = _openFiles.get(fd);
        return fileData._fsFile.getLength();
    }

    @Override
    public int getMode(String path) {
        int result = -1;
        try {
            final FSEntry fsEntry = matchPath(path);
            if (fsEntry != null) {
                result = fsEntry.isFile() ? S_IFREG : S_IFDIR;
                final FSAccessRights r = fsEntry.getAccessRights();
                if (r.canRead()) {
                    result |= S_IREAD;
                }
                if (r.canWrite()) {
                    result |= S_IWRITE;
                }
                if (r.canExecute()) {
                    result |= S_IEXEC;
                }
            }
        } catch (IOException ex) {
            _logger.warning(ex.toString());
        }
        return result;
    }

    @Override
    public long getSpace(String path, int t) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String[] list(String path) {
        try {
            final FSEntry fsEntry = matchPath(path);
            if (fsEntry != null && fsEntry.isDirectory()) {
                final FSDirectory fsDir = fsEntry.getDirectory();
                final Iterator< ? extends FSEntry> iter = fsDir.iterator();
                final List<String> strings = new ArrayList<String>();
                while (iter.hasNext()) {
                    final FSEntry fsChildEntry = iter.next();
                    final String name = fsChildEntry.getName();
                    if (!JDK_java_io_UnixFileSystem.currentOrParent(name)) {
                        strings.add(name);
                    }
                }
                return strings.toArray(new String[strings.size()]);
            }
        } catch (IOException ex) {
            _logger.warning(ex.toString());
        }
        return null;
    }

    @Override
    public int open(String name, int flags) {
        try {
            final Match m = match(name);
            if (m == null) {
                return -ErrorDecoder.Code.EISDIR.getCode();
            }
            FSEntry fsEntry = m.matchTail();
            FSFile fsFile = null;
            boolean isWrite = false;
            if (flags == VirtualFileSystem.O_RDONLY) {
                // reading
                if (fsEntry == null) {
                    return -ErrorDecoder.Code.ENOENT.getCode();
                }
                if (fsEntry.isDirectory()) {
                    return -ErrorDecoder.Code.EISDIR.getCode();
                }
                fsFile = fsEntry.getFile();
            } else {
                // writing, may have to create
                isWrite = true;
                if (fsEntry == null) {
                    fsEntry = m._d.addFile(m._tail);
                    fsFile = fsEntry.getFile();
                } else {
                    // exists, check is a file
                    if (fsEntry.isDirectory()) {
                        return -ErrorDecoder.Code.EISDIR.getCode();
                    }
                    fsFile = fsEntry.getFile();
                    if ((flags & O_TRUNC) != 0) {
                        fsFile.setLength(0);
                    }
                }
            }
            return addFd(fsFile, isWrite);
        } catch (IOException ex) {
            _logger.warning(ex.toString());
            return -ErrorDecoder.Code.EIO.getCode();
        }
    }

    @Override
    public int read(int fd, long fileOffset) {
        try {
            final FileData fileData = _openFiles.get(fd);
            final java.nio.ByteBuffer byteBuffer = fileData._byteBuffer;
            final long fsLength = fileData._fsFile.getLength();
            if (fileOffset >= fsLength) {
                return -1;
            }
            byteBuffer.position(0);
            byteBuffer.limit(1);
            fileData._fsFile.read(fileOffset, byteBuffer);
            byteBuffer.position(0);
            return byteBuffer.get();
        } catch (IOException ex) {
            _logger.warning(ex.toString());
            return -1;
        }
    }

    @Override
    public int readBytes(int fd, byte[] bytes, int offset, int length, long fileOffset) {
        // CheckStyle: stop parameter assignment check
        try {
            final FileData fileData = _openFiles.get(fd);
            final java.nio.ByteBuffer byteBuffer = fileData._byteBuffer;
            final long fsLength = fileData._fsFile.getLength();
            if (fileOffset >= fsLength) {
                return 0;
            }
            if (length > (fsLength - fileOffset)) {
                length = (int) (fsLength - fileOffset);
            }
            int left = length;
            while (left > 0) {
                byteBuffer.position(0);
                final int toDo = left > BUFFER_SIZE ? BUFFER_SIZE : left;
                byteBuffer.limit(toDo);
                fileData._fsFile.read(fileOffset, byteBuffer);
                byteBuffer.position(0);
                byteBuffer.get(bytes, offset, toDo);
                left -= toDo;
                offset += toDo;
                fileOffset += toDo;
            }
            return length;
        } catch (IOException ex) {
            _logger.warning(ex.toString());
            return -ErrorDecoder.Code.EIO.getCode();
        }
        // CheckStyle: resume parameter assignment check
    }

    @Override
    public boolean rename0(String path1, String path2) {
        try {
            final Match m1 = match(path1);
            final Match m2 = match(path2);
            /* At this point we should have matched up to the last component of both paths */
            if (m1 != null && m2 != null) {
                final FSEntry d1 = m1.matchTail();
                if (d1 == null) {
                    /* path1 does not exist */
                    return false;
                }
                final FSEntry d2 = m2.matchTail();
                if (d1 == d2) {
                    /* rename to self */
                    return true;
                }
                if (d2 != null) {
                    /* path2 already exists */
                    return false;
                }
                if (m1._d == m2._d) {
                    // rename within same directory, this we can do easily
                    d1.setName(m2._tail);
                } else {
                    final FileSystem<FSEntry> fs = (FileSystem<FSEntry>) d1.getFileSystem();
                    fs.rename(d1, m2._e, m2._tail);
                }
                return true;
            }
        } catch (IOException ex) {
            _logger.warning(ex.toString());
        }
        return false;
    }

    @Override
    public boolean setLastModifiedTime(String path, long time) {
        try {
            final FSEntry fsEntry = matchPath(path);
            fsEntry.setLastModified(time);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    @Override
    public int setLength(int fd, long length) {
        try {
            final FileData fileData = _openFiles.get(fd);
            fileData._fsFile.setLength(length);
            return 0;
        } catch (IOException ex) {
            _logger.warning(ex.toString());
            return -1;
        }
    }

    @Override
    public int setMode(String path, int mode) {
        final int result = -1;
        try {
            final FSEntry fsEntry = matchPath(path);
            if (fsEntry != null) {
                final FSAccessRights r = fsEntry.getAccessRights();
                /**
                 * The Ext2 API matches the java.io.File API which means that
                 * arbitrary mode patters cannot be handled. The code below
                 * assumes that mode was generated by UnixFileSystem.setPermission,
                 * i.e., it recovers the enable and owneronly arguments.
                 */
                if ((mode & (S_IRUSR | S_IRGRP | S_IROTH)) != 0) {
                    r.setReadable(true, (mode & (S_IRGRP | S_IROTH)) == 0);
                } else {
                    r.setReadable(false, (mode & (S_IRGRP | S_IROTH)) == 0);
                }
                if ((mode & (S_IWUSR | S_IWGRP | S_IWOTH)) != 0) {
                    r.setWritable(true, (mode & (S_IWGRP | S_IWOTH)) == 0);
                } else {
                    r.setWritable(false, (mode & (S_IWGRP | S_IWOTH)) == 0);
                }
                if ((mode & (S_IXUSR | S_IXGRP | S_IXOTH)) != 0) {
                    r.setExecutable(true, (mode & (S_IXGRP | S_IXOTH)) == 0);
                } else {
                    r.setExecutable(false, (mode & (S_IXGRP | S_IXOTH)) == 0);
                }
            }
        } catch (IOException ex) {
            _logger.warning(ex.toString());
        }
        return result;
    }

    @Override
    public int write(int fd, int b, long fileOffset) {
        try {
            final FileData fileData = _openFiles.get(fd);
            final java.nio.ByteBuffer byteBuffer = fileData._byteBuffer;
            byteBuffer.position(0);
            byteBuffer.limit(1);
            byteBuffer.put((byte) b);
            byteBuffer.position(0);
            fileData._fsFile.write(fileOffset, byteBuffer);
            return 0;
        } catch (IOException ex) {
            _logger.warning(ex.toString());
            return -ErrorDecoder.Code.EIO.getCode();
        }
    }

    @Override
    public int writeBytes(int fd, byte[] bytes, int offset, int length, long fileOffset) {
        // CheckStyle: stop parameter assignment check
        try {
            final FileData fileData = _openFiles.get(fd);
            final java.nio.ByteBuffer byteBuffer = fileData._byteBuffer;
            int left = length;
            while (left > 0) {
                byteBuffer.position(0);
                final int toDo = left > BUFFER_SIZE ? BUFFER_SIZE : left;
                byteBuffer.limit(toDo);
                byteBuffer.put(bytes, offset, toDo);
                byteBuffer.position(0);
                fileData._fsFile.write(fileOffset, byteBuffer);
                left -= toDo;
                offset += toDo;
                fileOffset += toDo;
            }
            return length;
        } catch (IOException ex) {
            _logger.warning(ex.toString());
            return -ErrorDecoder.Code.EIO.getCode();
        }
        // CheckStyle: stop parameter assignment check
    }

    /**
     * Represents the result of matching a pathname against the directory hierarchy,
     * The FSDirectory _d corresponds to the directory containing the final component
     * of the pathname, which is stored in _tail.
     */
    static class Match {
        FSEntry _e;
        FSDirectory _d;
        String _tail;
        Match(FSDirectory d, String tail) {
            _d = d;
            _tail = tail;
        }

        Match(FSEntry e, FSDirectory d, String tail) {
            this(d, tail);
            _e = e;
        }

        FSEntry matchTail() throws IOException {
            return _d.getEntry(_tail);
        }
    }

    /**
     * Matches the sequence of names in parts against the directory hierarchy,
     * up to the last but one component of the path (which is stored in the Match object
     * for subsequent checking (e.g. Match.matchTail).
     * @param name path to match
     * @return Match object or null if no match
     */
    private Match match(String name) throws IOException {
        final String[] parts = name.split(File.separator);
        if (parts.length <= _mountPathPrefixIndex) {
            return new Match(_root, ".");
        }
        FSDirectory d = _root;
        FSEntry fsEntry = _rootEntry;
        for (int i = _mountPathPrefixIndex; i < parts.length - 1; i++) {
            fsEntry = d.getEntry(parts[i]);
            if (fsEntry == null || fsEntry.isFile()) {
                return null;
            }
            d = fsEntry.getDirectory();
        }
        return new Match(fsEntry, d, parts[parts.length - 1]);
    }

    /**
     * Convenience function that first matches the path and then tries to match the final component.
     * @param name
     * @return FSEntry corresponding to last component of path or null if no match
     * @throws IOException
     */
    private FSEntry matchPath(String name) throws IOException {
        final Match m = match(name);
        if (m != null) {
            return m.matchTail();
        }
        return null;
    }

    @Override
    public int available(int fd, long fileOffset) {
        // TODO implement
        return 0;
    }

    @Override
    public long skip(int fd, long n, long fileOffset) {
        GuestVMError.unimplemented("Ext2FileSystem.skip");
        return 0;
    }

    @Override
    public long uniqueId(int fd) {
        final Ext2File ext2File = (Ext2File) _openFiles.get(fd)._fsFile;
        return ext2File.getINode().getINodeNr();
    }

    @Override
    public int force0(int fd, boolean metaData) throws IOException {
        final Ext2File ext2File = (Ext2File) _openFiles.get(fd)._fsFile;
        ext2File.flush();
        return 0;
    }
}
