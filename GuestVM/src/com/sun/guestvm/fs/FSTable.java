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
package com.sun.guestvm.fs;

import java.util.*;

import com.sun.guestvm.logging.*;
import com.sun.guestvm.fs.console.ConsoleFileSystem;
import com.sun.guestvm.fs.ext2.Ext2FileSystem;
import com.sun.guestvm.fs.image.ImageFileSystem;
import com.sun.guestvm.fs.nfs.NfsFileSystem;
import com.sun.guestvm.fs.sg.SiblingFileSystem;
import com.sun.guestvm.fs.tmp.TmpFileSystem;

/**
 * Stores information on (mounted) file systems.
 * The guestvm.fs.table property holds a list of specifications modelled on /etc/fstab
 * N.B. we do not unify the file system tree up to the root, aka /; each file system
 * must use a unique mount path that is not a prefix of another and path searches
 * always start from one of these mount paths. I.e,, you cannot do the equivalent of "ls /".
 * This could be fixed but it is not clear that it is necessary (since we are not building
 * an operating system).
 *
 * @author Mick Jordan
 *
 */

public class FSTable {
    private static Map<Info, VirtualFileSystem> _fsTable = new HashMap<Info, VirtualFileSystem>();
    private static final String FS_TABLE_PROPERTY = "guestvm.fs.table";
    private static final String FS_INFO_SEPARATOR = ",";
    private static final String FS_TABLE_SEPARATOR = ";";
    private static final String DEFAULT_FS_TABLE_PROPERTY = "ext2" + FS_INFO_SEPARATOR + "/blk/0" + FS_INFO_SEPARATOR + "/guestvm/ext2";
    private static boolean _initFSTable;
    private static Logger _logger;

    public static class Info {
        private String _type;
        private String _devPath;
        private String _mountPath;
        private VirtualFileSystem _fs;

        public static enum Parts {
            TYPE, DEVPATH, MOUNTPATH, OPTIONS, DUMP, ORDER;
        }

        private static final int PARTS_LENGTH = Parts.values().length;

        Info(String type, String devPath, String mountPath) {
            _type = type;
            _devPath = devPath;
            _mountPath = mountPath;
        }

        @Override
        public boolean equals(Object other) {
            return _mountPath.equals((Info) other);
        }

        @Override
        public int hashCode() {
            return _mountPath.hashCode();
        }
    }

    private static void logBadEntry(String msg) {
        _logger.warning(msg + ", ignoring");
    }

    private static void initFSTable() {
        if (!_initFSTable) {
            _logger = Logger.getLogger(FSTable.class.getName());
            // register shutdown hook to close file systems
            Runtime.getRuntime().addShutdownHook(new Thread(new CloseHook(), "FS_ShutdownHook"));
            // This call guarantees that file descriptors 0,1,2 map to the ConsoleFileSystem
            VirtualFileSystemId.getUniqueFd(new ConsoleFileSystem(), 0);

            final Info imageInfo = new Info("img", ImageFileSystem.getPath(), ImageFileSystem.getPath());
            initFS(imageInfo);
            final Info tmpInfo = new Info("tmp", TmpFileSystem.getPath(), TmpFileSystem.getPath());
            initFS(tmpInfo);

            String fsTableProperty = System.getProperty(FS_TABLE_PROPERTY);
            if (fsTableProperty == null) {
                fsTableProperty = DEFAULT_FS_TABLE_PROPERTY;
            }
            final String[] entries = fsTableProperty.split(FS_TABLE_SEPARATOR);
            for (String entry : entries) {
                final String[] info = entry.split(FS_INFO_SEPARATOR, Info.PARTS_LENGTH);
                if (info.length < 2) {
                    logBadEntry("fs.table entry " + entry + " is malformed");
                    continue;
                }
                final String type = info[0];
                final String devPath = info[1];
                final String mountPath = (info.length <= 2 || info[2].length() == 0) ? devPath : info[2];
                if (!mountPath.startsWith("/")) {
                    logBadEntry("mountpath " + mountPath + " is not absolute");
                    continue;
                }
                // check unique mountpaths
                for (Info fsInfo : _fsTable.keySet()) {
                    if (fsInfo._mountPath.startsWith(mountPath) || mountPath.startsWith(fsInfo._mountPath)) {
                        logBadEntry("mountpath " + mountPath + " is not unique");
                        continue;
                    }
                }
                _fsTable.put(new Info(type, devPath, mountPath), null);
            }
            _initFSTable = true;
        }
    }

    /**
     * Create the fileystem instance.
     *
     * @param info fstable info
     * @return VirtualFileSystem instance
     */
    private static VirtualFileSystem initFS(Info fsInfo) {
        VirtualFileSystem  result = null;
        if (fsInfo._type.equals("ext2")) {
            result =  Ext2FileSystem.create(fsInfo._devPath, fsInfo._mountPath);
        } else if (fsInfo._type.equals("nfs")) {
            result =  NfsFileSystem.create(fsInfo._devPath, fsInfo._mountPath);
        } else if (fsInfo._type.equals("sg")) {
            result = SiblingFileSystem.create(fsInfo._devPath, fsInfo._mountPath);
        } else if (fsInfo._type.equals("img")) {
            result = ImageFileSystem.create();
        } else if (fsInfo._type.equals("tmp")) {
            result = TmpFileSystem.create();
        }
        return checkedPut(fsInfo, result);
    }


    private static VirtualFileSystem checkedPut(Info fsInfo, VirtualFileSystem fs) {
        if (fs != null) {
            fsInfo._fs = fs;
            _fsTable.put(fsInfo, fs);
        }
        return fs;
    }

    public static void close() {
        for (VirtualFileSystem fs :  _fsTable.values()) {
            if (fs != null) {
                fs.close();
            }
        }
    }

    /**
     * Return the file system that exports file or null if none do.
     * @param file
     * @return
     */
    public static VirtualFileSystem exports(String path) {
        if (!_initFSTable) {
            initFSTable();
        }
        for (Info fsInfo : _fsTable.keySet()) {
            if (path.startsWith(fsInfo._mountPath)) {
                if (fsInfo._fs == null) {
                    initFS(fsInfo);
                }
                return fsInfo._fs;
            }
        }
        return null;
    }

    static class CloseHook implements Runnable {
        public void run() {
            close();
        }
    }

}
