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
package com.sun.guestvm.fs.nfs;

import java.io.*;
import java.util.*;

import com.sun.guestvm.logging.*;
import com.sun.guestvm.fs.*;
import com.sun.max.annotate.*;
import com.sun.max.vm.runtime.*;
import com.sun.max.vm.run.guestvm.*;

import com.sun.nfs.*;

/**
 * NFS client for Guest VM.
 *
 * @author Mick Jordan
 *
 */

public final class NfsFileSystem implements VirtualFileSystem {

    private static Logger _logger;
    private Nfs _nfs;
    private String _mountPath;
    private int _mountPathPrefixIndex;
    private List<Nfs> _openFiles = new ArrayList<Nfs>();
    private static final int CREATE_FILE_MODE = S_IRUSR | S_IWUSR | S_IRGRP | S_IWGRP | S_IROTH | S_IWOTH;
    private static final int CREATE_DIR_MODE = CREATE_FILE_MODE | S_IXUSR | S_IXGRP | S_IXOTH;

    @INLINE
    private static void initLogger() {
        if (_logger == null) {
            _logger = Logger.getLogger(NfsFileSystem.class.getName());
        }
    }

    private NfsFileSystem(String server, String serverPath, String mountPath) throws IOException {
        // The Nfs classes are not in the image and we cannot resolve references to them
        // since image classes resolve through the boot image class loader.
        // So sun.boot.class.path must be set to include the classes.
        _nfs = NfsConnect.connectToNfs(server, NfsConnect.NFS_PORT, serverPath);
        _mountPath = mountPath;
        _mountPathPrefixIndex = _mountPath.split(File.separator).length;
    }

    public static NfsFileSystem create(String nfsPath, String mountPath) {
        // Initializing NFS requires that the launcher (and associated classloaders) are initialized.
        // If an NFS path is on the classpath we will get here due to a canonicalize call from the
        // launcher initialization that will cause recursion if we try to initialize. It's ok to return
        // null here in that case as that will cause canonicalize to return the path it was given
        // and actual initialization will be delayed until some real use of NFS.
        if (GuestVMRunScheme.launcherInit()) {
            initLogger();
            try {
                final int ix = nfsPath.indexOf(':');
                if (ix < 0) {
                    _logger.warning("NFS mount is not of form server:path");
                } else {
                    final String server = nfsPath.substring(0, ix);
                    final String serverPath = nfsPath.substring(ix + 1);
                    return new NfsFileSystem(server, serverPath, mountPath);
                }
            } catch (IOException ex) {
                _logger.warning("NFS mount " + nfsPath + " failed");
            }
        }
        return null;
    }

    private int addFd(Nfs fe) {
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

    @Override
    public void close() {

    }

    @Override
    public String canonicalize0(String path) throws IOException {
        // TODO correct implementation
        return path;
    }

    @Override
    public boolean checkAccess(String path, int access) {
        try {
            final Nfs fsEntry = matchPath(path);
            if (fsEntry == null) {
                return false;
            }
            switch (access) {
                case ACCESS_READ:
                    return fsEntry.canRead();
                case ACCESS_WRITE:
                    return fsEntry.canWrite();
                case ACCESS_EXECUTE:
                    return fsEntry.canExecute();
            }
        } catch (IOException ex) {
            _logger.warning(ex.toString());
            return false;
        }
        return false;
    }

    @Override
    public int close0(int fd) {
        final Nfs fsEntry = _openFiles.get(fd);
        try {
            fsEntry.close();
        } catch (IOException ex) {
            _logger.warning(ex.toString());
            return -ErrorDecoder.Code.EIO.getCode();
        }
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

    private  boolean create(String path, boolean isFile) {
        try {
            final Match m = match(path);
            if (m == null || m.matchTail() != null) {
                return false;
            }
            if (isFile) {
                m._d.create(m._tail, CREATE_FILE_MODE);
            } else {
                m._d.mkdir(m._tail, CREATE_DIR_MODE);
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
            final Nfs fsEntry = m.matchTail();
            if (m == null || fsEntry == null) {
                return false;
            }
            if (fsEntry.isFile()) {
                m._d.remove(m._tail);
            } else {
                return false;
            }
            return true;
        } catch (IOException ex) {
            _logger.warning(ex.toString());
            return false;
        }
    }

    @Override
    public long getLastModifiedTime(String path) {
        long result = 0;
        try {
            final  Nfs fsEntry = matchPath(path);
            if (fsEntry != null) {
                result = fsEntry.mtime();
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
            final Nfs fsEntry = matchPath(path);
            if (fsEntry != null && fsEntry.isFile()) {
                result = fsEntry.length();
            }
        } catch (IOException ex) {
            _logger.warning(ex.toString());
        }
        return result;
    }

    @Override
    public long getLength(int fd) {
        final Nfs fsEntry = _openFiles.get(fd);
        try {
            return fsEntry.length();
        } catch (IOException ex) {
            _logger.warning(ex.toString());
        }
        return 0;
    }

    @Override
    public int getMode(String path) {
        int result = -1;
        try {
            final Nfs fsEntry = matchPath(path);
            if (fsEntry != null) {
                result = fsEntry.isFile() ? S_IFREG : S_IFDIR;
                if (fsEntry.canRead()) {
                    result |= S_IREAD;
                }
                if (fsEntry.canWrite()) {
                    result |= S_IWRITE;
                }
                if (fsEntry.canExecute()) {
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
            final Nfs nfsEntry = matchPath(path);
            if (nfsEntry != null && nfsEntry.isDirectory()) {
                return nfsEntry.readdir();
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
            Nfs fsEntry = m.matchTail();
            if (flags == VirtualFileSystem.O_RDONLY) {
                // reading
                if (fsEntry == null) {
                    return -ErrorDecoder.Code.ENOENT.getCode();
                }
                if (fsEntry.isDirectory()) {
                    return -ErrorDecoder.Code.EISDIR.getCode();
                }
            } else {
                // writing, may have to create
                if (fsEntry == null) {
                    fsEntry = m._d.create(m._tail, CREATE_FILE_MODE);
                } else {
                    // exists, check is a file
                    if (fsEntry.isDirectory()) {
                        return -ErrorDecoder.Code.EISDIR.getCode();
                    }
                    if ((flags & O_TRUNC) != 0) {
                        // Nothing special to do
                    }
                }
            }
            return addFd(fsEntry);
        } catch (IOException ex) {
            _logger.warning(ex.toString());
            return -ErrorDecoder.Code.EIO.getCode();
        }
    }

    @Override
    public int read(int fd, long fileOffset) {
        // TODO once per fd buffer
        final byte[] buf = new byte[1];
        final int result = readBytes(fd, buf, 0, 1, fileOffset);
        if (result == 0) {
            return 0;
        } else {
            return buf[0];
        }
    }

    @Override
    public int readBytes(int fd, byte[] bytes, int offset, int length, long fileOffset) {
        final Nfs fsEntry = _openFiles.get(fd);
        try {
            final int result =  fsEntry.read(bytes, offset, length, fileOffset);
            // Nfs.read returns -1 on EOF, we return 0
            if (result == -1) {
                return 0;
            }
            return result;
        } catch (IOException ex) {
            _logger.warning(ex.toString());
            return -ErrorDecoder.Code.EIO.getCode();
        }
    }

    @Override
    public boolean rename0(String path1, String path2) {
        try {
            final Match m1 = match(path1);
            final Match m2 = match(path2);
            if (m1 == null || m2 == null) {
                return false;
            }
            if (m1.matchTail() == null || m2.matchTail() != null) {
                return false;
            }
            m1._d.rename(m2._d, m1._tail, m2._tail);
            return true;
        } catch (IOException ex) {
            _logger.warning(ex.toString());
            return false;
        }
    }

    @Override
    public boolean setLastModifiedTime(String path, long time) {
        try {
            final Nfs fsEntry = matchPath(path);
            if (fsEntry != null) {
                fsEntry.mtime(time);
                return true;
            }
        } catch (IOException ex) {
            _logger.warning(ex.toString());
        }
        return false;
    }

    @Override
    public int setLength(int fd, long length) {
        final Nfs fsEntry = _openFiles.get(fd);
        try {
            fsEntry.length(length);
            return 0;
        } catch (IOException ex) {
            _logger.warning(ex.toString());
            return -1;
        }
    }

    @Override
    public int setMode(String path, int mode) {
        try {
            final Nfs fsEntry = matchPath(path);
            if (fsEntry != null) {
                fsEntry.mode(mode);
                return 0;
            } else {
                return -ErrorDecoder.Code.ENOENT.getCode();
            }
        } catch (IOException ex) {
            _logger.warning(ex.toString());
        }
        return -ErrorDecoder.Code.EIO.getCode();
    }

    @Override
    public int write(int fd, int b, long fileOffset) {
        // TODO once per fd buffer
        final byte[] buf = new byte[1];
        buf[0] = (byte) b;
        return writeBytes(fd, buf, 0, 1, fileOffset);

    }

    @Override
    public int writeBytes(int fd, byte[] bytes, int offset, int length, long fileOffset) {
        final Nfs fsEntry = _openFiles.get(fd);
        try {
            fsEntry.write(bytes, offset, length, fileOffset);
            return length;
        } catch (IOException ex) {
            _logger.warning(ex.toString());
            return -ErrorDecoder.Code.EIO.getCode();
        }
    }

    @Override
    public int available(int fd, long fileOffset) {
        return 0;
    }

    @Override
    public long skip(int fd, long n, long fileOffset) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long uniqueId(int fd) {
     // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int lock0(int fd, boolean blocking, long pos, long size, boolean shared) throws IOException {
        FatalError.crash(getClass().getName() + "lock0");
        return 0;
    }

    @Override
    public void release0(int fd, long pos, long size) throws IOException {
        FatalError.crash(getClass().getName() + "release0");
    }

    @Override
    public int force0(int fd, boolean metaData) throws IOException {
        FatalError.crash(getClass().getName() + "force0");
        return 0;
    }

    /**
     * Represents the result of matching a pathname against the directory hierarchy,
     * The Nfs directory _d corresponds to the directory containing the final component
     * of the pathname, which is stored in _tail.
     */
    static class Match {
        Nfs _d;
        String _tail;
        Match(Nfs d, String tail) {
            _d = d;
            _tail = tail;
        }

        Nfs matchTail() {
            try {
                return _d.lookup(_tail);
            } catch (IOException ex) {
                return null;
            }
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
        if (parts.length == _mountPathPrefixIndex) {
            return new Match(_nfs, ".");
        }
        Nfs d = _nfs;
        for (int i = _mountPathPrefixIndex; i < parts.length - 1; i++) {
            final Nfs nfsEntry = d.lookup(parts[i]);
            if (nfsEntry == null || nfsEntry.isFile()) {
                return null;
            }
            d = nfsEntry;
        }
        return new Match(d, parts[parts.length - 1]);
    }

    /**
     * Convenience function that first matches the path and then tries to match the final component.
     * @param name
     * @return Nfs object corresponding to last component of path or null if no match
     * @throws IOException
     */
    private Nfs matchPath(String name) throws IOException {
        final Match m = match(name);
        if (m != null) {
            return m.matchTail();
        }
        return null;
    }

}
