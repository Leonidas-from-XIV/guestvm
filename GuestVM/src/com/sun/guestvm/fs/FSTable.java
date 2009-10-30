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

import java.io.File;
import java.util.*;

import com.sun.guestvm.error.GuestVMError;
import com.sun.guestvm.fs.console.ConsoleFileSystem;
import com.sun.guestvm.fs.ext2.Ext2FileSystem;
import com.sun.guestvm.fs.heap.HeapFileSystem;
import com.sun.guestvm.fs.image.ImageFileSystem;
import com.sun.guestvm.fs.nfs.NfsFileSystem;
import com.sun.guestvm.fs.sg.SiblingFileSystem;

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
    private static final String TMPDIR_PROPERTY = "guestvm.tmpdir";
    private static final String DEFAULT_TMPDIR = "/tmp";
    private static final String FS_INFO_SEPARATOR = ":";
    private static final String FS_TABLE_SEPARATOR = ";";
    private static final String READ_ONLY = "ro";
    public static final String AUTO = "auto";
    private static final String DEFAULT_FS_TABLE_PROPERTY = "ext2" + FS_INFO_SEPARATOR + "/blk/0" + FS_INFO_SEPARATOR + "/guestvm/java" + FS_INFO_SEPARATOR + READ_ONLY;
    public static final String TMP_FS_INFO = "heap" + FS_INFO_SEPARATOR + "/heap/0" + FS_INFO_SEPARATOR;
    public static final String IMG_FS_INFO = "img" + FS_INFO_SEPARATOR + "/img/0" + FS_INFO_SEPARATOR;
    private static final String FS_OPTIONS_SEPARATOR = ",";
    private static final int TYPE_INDEX = 0;
    private static final int DEV_INDEX = 1;
    private static final int MOUNT_INDEX = 2;
    private static final int OPTIONS_INDEX = 3;

    private static boolean _initFSTable;
    private static RootFileSystem _rootFS;

    public static class Info {
        private String _type;
        private String _devPath;
        private String _mountPath;
        private String[] _options;
        private VirtualFileSystem _fs;

        public static enum Parts {
            TYPE, DEVPATH, MOUNTPATH, OPTIONS, DUMP, ORDER;
        }

        private static final int PARTS_LENGTH = Parts.values().length;

        Info(String type, String devPath, String mountPath, String[] options) {
            _type = type;
            _devPath = devPath;
            _mountPath = mountPath;
            _options = options == null ? new String[0] : options;
        }

        boolean readOnly() {
            for (String option : _options) {
                if (option.equals("ro")) {
                    return true;
                }
            }
            return false;
        }

        boolean autoMount() {
            for (String option : _options) {
                if (option.equals("auto")) {
                    return true;
                }
            }
            return false;

        }

        public String mountPath() {
            return _mountPath;
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
        GuestVMError.unexpected(msg);
    }

    private static void initFSTable() {
        if (!_initFSTable) {
            // register shutdown hook to close file systems
            Runtime.getRuntime().addShutdownHook(new Thread(new CloseHook(), "FS_ShutdownHook"));
            // This call guarantees that file descriptors 0,1,2 map to the ConsoleFileSystem
            VirtualFileSystemId.getUniqueFd(new ConsoleFileSystem(), 0);

            _rootFS = RootFileSystem.create();

            String fsTableProperty = System.getProperty(FS_TABLE_PROPERTY);
            if (fsTableProperty == null) {
                fsTableProperty = DEFAULT_FS_TABLE_PROPERTY;
            }

            /*  prepend the image and default heap fs */
            fsTableProperty = IMG_FS_INFO + ImageFileSystem.getPath() + FS_INFO_SEPARATOR + AUTO + FS_TABLE_SEPARATOR + fsTableProperty;
            String tmpDir = System.getProperty(TMPDIR_PROPERTY);
            if (tmpDir == null) {
                tmpDir = DEFAULT_TMPDIR;
            }
            fsTableProperty = TMP_FS_INFO + tmpDir + FS_INFO_SEPARATOR + AUTO + FS_TABLE_SEPARATOR + fsTableProperty;

            final String[] entries = fsTableProperty.split(FS_TABLE_SEPARATOR);
            for (String entry : entries) {
                final String[] info = fixupNfs(entry.split(FS_INFO_SEPARATOR, Info.PARTS_LENGTH));
                if (info.length < MOUNT_INDEX || info.length > OPTIONS_INDEX + 1) {
                    logBadEntry("fs.table entry " + entry + " is malformed");
                    continue;
                }
                final String type = info[TYPE_INDEX];
                final String devPath = info[DEV_INDEX];
                final String mountPath = (info.length <= MOUNT_INDEX || info[MOUNT_INDEX].length() == 0) ? devPath : info[MOUNT_INDEX];
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
                String[] options = null;
                if (info.length > OPTIONS_INDEX) {
                    options = info[OPTIONS_INDEX].split(FS_OPTIONS_SEPARATOR);

                }
                final Info fsInfo = new Info(type, devPath, mountPath, options);
                VirtualFileSystem vfs = null;
                if (fsInfo.autoMount()) {
                    vfs = initFS(fsInfo);
                } else {
                    // record the info so that a future path lookup will match,
                    // the fs will be mounted at that point.
                    _fsTable.put(fsInfo, vfs);
                }
            }
            _initFSTable = true;
        }
    }

    /*
     * Nfs is irregular in that a ":" is used internally in the device path, so we recover from that here.
     */
    private static String[] fixupNfs(String[] parts) {
        String[] result = parts;
        if (parts[TYPE_INDEX].equals("nfs")) {
            result = new String[parts.length - 1];
            result[TYPE_INDEX] = parts[TYPE_INDEX];
            result[DEV_INDEX] = parts[DEV_INDEX] + ":" + parts[DEV_INDEX + 1];
            if (parts.length > MOUNT_INDEX + 1) {
                result[MOUNT_INDEX] = parts[MOUNT_INDEX + 1];
                if (parts.length > OPTIONS_INDEX + 1) {
                    result[OPTIONS_INDEX] = parts[OPTIONS_INDEX + 1];
                }
            }
        }
        return result;
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
            result =  Ext2FileSystem.create(fsInfo._devPath, fsInfo._mountPath, fsInfo.readOnly());
        } else if (fsInfo._type.equals("nfs")) {
            result =  NfsFileSystem.create(fsInfo._devPath, fsInfo._mountPath);
        } else if (fsInfo._type.equals("sg")) {
            result = SiblingFileSystem.create(fsInfo._devPath, fsInfo._mountPath);
        } else if (fsInfo._type.equals("img")) {
            result = ImageFileSystem.create();
        } else if (fsInfo._type.equals("heap")) {
            result = HeapFileSystem.create(fsInfo._devPath, fsInfo._mountPath);
        }
        return checkedPut(fsInfo, result);
    }


    private static VirtualFileSystem checkedPut(Info fsInfo, VirtualFileSystem fs) {
        if (fs != null) {
            fsInfo._fs = fs;
            _fsTable.put(fsInfo, fs);
            RootFileSystem.mount(fsInfo);
        }
        return fs;
    }

    public static Info getInfo(VirtualFileSystem vfs) {
        for (Map.Entry<Info, VirtualFileSystem> entry : _fsTable.entrySet()) {
            if (entry.getValue() == vfs) {
                return entry.getKey();
            }
        }
        return null;
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
        assert path != null;
        for (Info fsInfo : _fsTable.keySet()) {
            final int mountLength = fsInfo._mountPath.length();
            if (path.startsWith(fsInfo._mountPath) && (path.length() == mountLength || path.charAt(mountLength) == File.separatorChar)) {
                if (fsInfo._fs == null) {
                    initFS(fsInfo);
                }
                return fsInfo._fs;
            }
        }
        /* We may have been given a path that is above the mount points */
        if (_rootFS.getMode(path) > 0) {
            return _rootFS;
        }
        return null;
    }

    static class CloseHook implements Runnable {
        public void run() {
            close();
        }
    }


}
