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

import com.sun.guestvm.fs.console.ConsoleFileSystem;
import com.sun.guestvm.fs.ext2.Ext2FileSystem;
import com.sun.guestvm.fs.image.ImageFileSystem;
import com.sun.guestvm.fs.nfs.NfsFileSystem;
import com.sun.guestvm.fs.sg.SiblingFileSystem;
import com.sun.guestvm.fs.tmp.TmpFileSystem;

/**
 *
 * @author Mick Jordan
 *
 */

public class FSTable {
    private enum InitState {NONE, IN_BASIC, BASIC, IN_EXT2, EXT2, IN_NFS, NFS, COMPLETE};
    private static Map<String, VirtualFileSystem> _fileSystems = new HashMap<String, VirtualFileSystem>();
    private static InitState _initState = InitState.NONE;
    private static final String FS_LIST_PROPERTY = "guestvm.fs.list";
    private static final String[] DEFAULT_FS_LIST = {"ext2"};
    private static String[] _fsList = DEFAULT_FS_LIST;

    private static boolean initFS(String xfs) {
        for (String fs : _fsList) {
            if (fs.equals(xfs)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("serial")
    static class CheckStateException extends RuntimeException {
        CheckStateException(String msg) {
            super(msg);
        }
    }

    private static void checkState(InitState initState) {
        if (_initState == initState) {
            throw new CheckStateException("recursion initializing file systems - state " + initState);
        }
        _initState = initState;
    }

    private static void initializeBasic() {
        if (_initState == InitState.NONE) {

            checkState(InitState.IN_BASIC);
            // register shutdown hook to close file systems
            Runtime.getRuntime().addShutdownHook(new Thread(new CloseHook(), "FS_ShutdownHook"));

            final String fsList = System.getProperty(FS_LIST_PROPERTY);
            if (fsList != null) {
                _fsList = fsList.split(",");
            }

            // This call guarantees that file descriptors 0,1,2 map to the ConsoleFileSystem
            VirtualFileSystemId.getUniqueFd(new ConsoleFileSystem(), 0);

            final ImageFileSystem imageFileSystem = ImageFileSystem.create();
            _fileSystems.put(imageFileSystem.getPath(), imageFileSystem);

            final  TmpFileSystem tmpFS = TmpFileSystem.create();
            checkedPut(tmpFS.getPath(), tmpFS);

            if (initFS("sg")) {
                final  SiblingFileSystem[] siblingFileSystems = SiblingFileSystem.create();
                for (SiblingFileSystem siblingFileSystem : siblingFileSystems) {
                    checkedPut(siblingFileSystem.getPath(), siblingFileSystem);
                }
            }

            _initState = InitState.BASIC;
        }
    }

    private static void initExt2() {
        if (_initState.ordinal() < InitState.EXT2.ordinal()) {
            if (initFS("ext2")) {
                checkState(InitState.IN_EXT2);
                final  Ext2FileSystem ext2FS = Ext2FileSystem.create();
                checkedPut(ext2FS.getPath(), ext2FS);
            }
            _initState = InitState.EXT2;
        }
    }

    private static void initNfs() {
        if (_initState.ordinal() < InitState.NFS.ordinal()) {
            if (initFS("nfs")) {
                checkState(InitState.IN_NFS);
                final NfsFileSystem[] nfsFileSystems = NfsFileSystem.create();
                for (NfsFileSystem nfsFileSystem : nfsFileSystems) {
                    checkedPut(nfsFileSystem.getPath(), nfsFileSystem);
                }
            }
            _initState = InitState.NFS;
        }
    }

    private static VirtualFileSystem initialize(String path) {
        if (_initState != InitState.COMPLETE) {
            // To avoid unpleasant recursion that might occur, e.g. in Ext2FileSystem,
            // we initialize the file systems incrementally in order of complexity, and check for a match
            // at each phase.
            initializeBasic();
            VirtualFileSystem fs = match(path);
            if (fs != null) {
                return fs;
            }
            try {
                initExt2();
            } catch (CheckStateException ex) {
                // there is recursion in Ext2FileSystem regarding timezone config files
                // we just return in this case
                return null;
            }
            fs = match(path);
            if (fs != null) {
                return fs;
            }
            initNfs();
            _initState = InitState.COMPLETE;
        }
        return match(path);
    }

    private static void checkedPut(String path, VirtualFileSystem fs) {
        if (fs != null) {
            _fileSystems.put(path, fs);
        }
    }

    public static void close() {
        for (VirtualFileSystem fs :  _fileSystems.values()) {
            fs.close();
        }
    }

    /**
     * Return the file system that exports file or null if none do.
     * @param file
     * @return
     */
    public static VirtualFileSystem exports(String path) {
        if (_initState != InitState.COMPLETE) {
            return initialize(path);
        }
        return match(path);
    }

    private static VirtualFileSystem match(String path) {
        for (Map.Entry<String, VirtualFileSystem> me : _fileSystems.entrySet()) {
            if (path.startsWith(me.getKey())) {
                return me.getValue();
            }
        }
        return null;
    }

    public static VirtualFileSystem exports(File file) {
        // TODO The matter of canonicalization, e.g. file.getCanonicalPath();
        return exports(file.getAbsolutePath());
    }

    static class CloseHook implements Runnable {
        public void run() {
            close();
        }
    }

}
