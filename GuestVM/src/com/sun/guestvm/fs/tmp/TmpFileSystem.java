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
package com.sun.guestvm.fs.tmp;

import java.io.*;
import java.util.*;

import com.sun.guestvm.fs.ErrorDecoder;
import com.sun.guestvm.fs.*;
import com.sun.max.program.ProgramError;

/**
 * A heap-based file system for /tmp.
 * Single global lock protects everything accessed through public methods.
 */

public final class TmpFileSystem extends DefaultFileSystemImpl implements VirtualFileSystem {

    private static TmpFileSystem _singleton;
    private SubDirEntry _root = new SubDirEntry();
    private List<FileEntry> _openFiles = new ArrayList<FileEntry>();
    private static final int READ_WRITE = S_IREAD | S_IWRITE;

    abstract static  class DirEntry {
        int _mode = READ_WRITE;
        long _modified;

        DirEntry() {
            _modified = System.currentTimeMillis();
        }

        boolean isFile() {
            return false;
        }

        boolean isDir() {
            return false;
        }
    }

    static class FileEntry extends DirEntry {
        static long _nextId = 0;
        static final int FILE_BLOCK_SIZE = 1024;
        static final int DIV_FILE_BLOCK_SIZE = 10;
        static final int MOD_FILE_BLOCK_SIZE = FILE_BLOCK_SIZE - 1;
        final List<byte[]> _blocks = new ArrayList<byte[]>();
        long _size;
        long _maxSize;
        int _nextIndex;
        long _id;

        FileEntry() {
            _mode |= S_IFREG;
            _id = _nextId++;
        }

        boolean isFile() {
            return true;
        }

        void addCapacity(long fileOffset) {
            _blocks.add(_nextIndex++, new byte[FILE_BLOCK_SIZE]);
            _maxSize += FILE_BLOCK_SIZE;
        }

        void write(int b, long fileOffset) {
            final int index = (int) fileOffset >> DIV_FILE_BLOCK_SIZE;
            final int offset = (int) fileOffset & MOD_FILE_BLOCK_SIZE;
            // Checkstyle: stop indentation check
            _blocks.get(index)[offset] = (byte) b;
            // Checkstyle: resume indentation check
            _size++;
        }

        int read(long fileOffset) {
            if (fileOffset >= _size) {
                return -1;
            } else {
                final int index = (int) fileOffset >> DIV_FILE_BLOCK_SIZE;
                final int offset = (int) fileOffset & MOD_FILE_BLOCK_SIZE;
                return (int) _blocks.get(index)[offset] & 0xFF;
            }
        }
    }

    static class SubDirEntry extends DirEntry {
        Map<String, DirEntry> _contents = new HashMap<String, DirEntry>();

        SubDirEntry() {
            _mode |= S_IFDIR + S_IEXEC;
        }

        boolean isDir() {
            return true;
        }

        void put(String name, DirEntry entry) {
            _contents.put(name, entry);
        }

        DirEntry get(String name) {
            return _contents.get(name);
        }
    }

    private TmpFileSystem() {
        _root.put("tmp", new SubDirEntry());
    }

    public static TmpFileSystem create() {
        if (_singleton != null) {
            return _singleton;
        }
        _singleton = new TmpFileSystem();
        return _singleton;
    }

    @Override
    public String getPath() {
        return "/tmp";
    }

    @Override
    public void close() {

    }

    @Override
    public synchronized String canonicalize0(String path) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public synchronized boolean checkAccess(String path, int access) {
        final DirEntry d = matchPath(path);
        if (d == null) {
            return false;
        }
        switch (access) {
            case ACCESS_READ:
                return (d._mode & S_IREAD) != 0;
            case ACCESS_WRITE:
                return (d._mode & S_IWRITE) != 0;
            case ACCESS_EXECUTE:
                return (d._mode & S_IEXEC) != 0;
        }
        return false;
    }

    @Override
    public synchronized int close0(int fd) {
        _openFiles.set(fd, null);
        return 0;
    }

    @Override
    public synchronized boolean createDirectory(String path) {
        return create(path, false);
    }

    @Override
    public synchronized boolean createFileExclusively(String path) throws IOException {
        return create(path, true);
    }

    private synchronized boolean create(String path, boolean isFile) {
        assert path.startsWith(getPath());
        final Match m = match(path, false);
        if (m == null || m.matchTail() != null) {
            return false;
        }
        m._d.put(m._tail, isFile ? new FileEntry() : new SubDirEntry());
        return true;

    }

    @Override
    public synchronized boolean delete0(String path) {
        final Match m = match(path, false);
        if (m != null) {
            final DirEntry dd = m.matchTail();
            if (dd != null) {
                if (dd.isFile()) {
                    // TODO permissions
                    m._d._contents.remove(m._tail);
                    return true;
                } else {
                    // check empty
                    final SubDirEntry sdd = (SubDirEntry) dd;
                    if (sdd._contents.isEmpty()) {
                        m._d._contents.remove(m._tail);
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public synchronized long getLastModifiedTime(String path) {
        long result = 0;
        final DirEntry d = matchPath(path);
        if (d != null) {
            result = d._modified;
        }
        return result;
    }

    @Override
    public synchronized long getLength(String path) {
        long result = 0;
        final DirEntry d = matchPath(path);
        if (d != null && d.isFile()) {
            result = ((FileEntry) d)._size;
        }
        return result;
    }

    @Override
    public synchronized int getMode(String path) {
        final DirEntry d = matchPath(path);
        if (d != null) {
            return d._mode;
        }
        return -1;
    }

    @Override
    public synchronized long getSpace(String path, int t) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public synchronized String[] list(String path) {
        final Match m = match(path, true);
        if (m == null) {
            return null;
        } else {
            final Set <String> set = m._d._contents.keySet();
            final String[] result = new String[set.size()];
            set.toArray(result);
            return result;
        }
    }

    @Override
    public synchronized int open(String name, int flags) {
        final Match m = match(name, false);
        if (m == null) {
            return -ErrorDecoder.Code.ENOENT.getCode();
        }
        if (flags == VirtualFileSystem.O_RDONLY) {
            // reading
            final DirEntry fe = m.matchTail();
            if (fe == null) {
                return -ErrorDecoder.Code.ENOENT.getCode();
            } else if (!fe.isFile()) {
                return  -ErrorDecoder.Code.EISDIR.getCode();
            }
            return addFd((FileEntry) fe);
        } else {
            // writing
            DirEntry fe = m.matchTail();
            if (fe == null) {
                fe = new FileEntry();
                m._d.put(m._tail,  fe);
            } else {
                // exists, check is a file
                if (fe.isDir()) {
                    return  -ErrorDecoder.Code.EISDIR.getCode();
                }
                final FileEntry ffe = (FileEntry) fe;
                // do we need to truncate?
                if ((flags & O_TRUNC) != 0) {
                    ffe._modified = System.currentTimeMillis();
                    ffe._size = 0;
                }
            }
            return addFd((FileEntry) fe);
        }
    }

    @Override
    public synchronized int read(int fd, long fileOffset) {
        final FileEntry fe = _openFiles.get(fd);
        return fe.read(fileOffset);
    }

    @Override
    public synchronized int readBytes(int fd, byte[] bytes, int offset, int length,
            long fileOffset) {
        final FileEntry fe = _openFiles.get(fd);
        if ((int) fileOffset >= fe._size) {
            return 0;
        }
        if (length > (int) (fe._size - fileOffset)) {
            // CheckStyle: stop parameter assignment check
            length = (int) (fe._size - fileOffset);
            // CheckStyle: resume parameter assignment check
        }
        for (int i = 0; i < length; i++) {
            bytes[i + offset] = (byte) fe.read(i + fileOffset);
        }
        return length;
    }

    @Override
    public synchronized boolean rename0(String path1, String path2) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public synchronized boolean setLastModifiedTime(String path, long time) {
        final DirEntry d = matchPath(path);
        if (d != null) {
            d._modified = time;
            return true;
        }
        return false;
    }

    @Override
    public synchronized boolean setPermission(String path, int access,
            boolean enable, boolean owneronly) {
        final DirEntry d = matchPath(path);
        if (d != null) {
            int mode = 0;
            switch (access) {
                case ACCESS_READ:
                    mode = S_IREAD;
                    break;
                case ACCESS_WRITE:
                    mode = S_IWRITE;
                    break;
                case ACCESS_EXECUTE:
                    mode = S_IEXEC;
                    break;
            }
            d._mode = enable ? d._mode | mode : d._mode & ~mode;
            return true;
        }
        return false;
    }

    @Override
    public synchronized boolean setReadOnly(String path) {
        final DirEntry d = matchPath(path);
        if (d != null) {
            d._mode &= ~S_IWRITE;
            return true;
        }
        return false;
    }

    @Override
    public synchronized int write(int fd, int b, long fileOffset) {
        final FileEntry fe = _openFiles.get(fd);
        while (fileOffset >= fe._maxSize) {
            fe.addCapacity(fileOffset);
        }
        fe.write(b, fileOffset);
        return 0;
    }

    @Override
    public synchronized int writeBytes(int fd, byte[] bytes, int offset, int length,
            long fileOffset) {
        for (int i = 0; i < length; i++) {
            write(fd, bytes[offset + i], fileOffset + i);
        }
        return 0;
    }

    @Override
    public long getLength(int fd) {
        final FileEntry fe = _openFiles.get(fd);
        return fe._size;
    }

    @Override
    public void setLength(int fd, long length) {
        ProgramError.unexpected("setLength not implemented");
    }

    @Override
    public int available(int fd, long fileOffset) {
        final FileEntry fe = _openFiles.get(fd);
        if ((int) fileOffset >= fe._size) {
            return 0;
        }
        final long avail = fe._size - fileOffset;
        if (avail > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) avail;
    }

    @Override
    public long skip(int fd, long n, long fileOffset) {
        // spec allows skip to go past end of file
        return n;
    }

    @Override
    public long uniqueId(int fd) {
        return _openFiles.get(fd)._id;
    }

    /*
     * private support methods
     */

    static class Match {
        SubDirEntry _d;
        String _tail;
        Match(SubDirEntry d, String tail) {
            _d = d;
            _tail = tail;
        }

        DirEntry matchTail() {
            return _d.get(_tail);
        }
    }

    private int addFd(FileEntry fe) {
        final int size = _openFiles.size();
        for (int i = 0; i < size; i++) {
            if (_openFiles.get(i) == null) {
                _openFiles.set(i, fe);
                return i;
            }
        }
        _openFiles.add(fe);
        return size;
    }

    /**
     * Matches the sequence of names in parts against the directory hierarchy.
     * If complete is true, expects the last component to represent a directory
     * otherwise expects next to last to be a directory. Returns the
     * @param parts
     * @param complete
     * @return matching SubDirEntry or null if no match
     */
    private Match match(String name, boolean complete) {
        final String[] parts = name.split(File.separator);
        SubDirEntry d = _root;
        final int length = complete ? parts.length : parts.length - 1;
        for (int i = 1; i < length; i++) {
            final DirEntry dd = d.get(parts[i]);
            if (dd == null || dd.isFile() || (dd._mode & S_IEXEC) == 0) {
                return null;
            }
            d = (SubDirEntry) dd;
        }
        return new Match(d, parts[parts.length - 1]);
    }

    private DirEntry matchPath(String name) {
        final Match m = match(name, false);
        if (m != null) {
            return m.matchTail();
        }
        return null;
    }

}
