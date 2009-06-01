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
package com.sun.guestvm.jdk;

import java.io.*;
import com.sun.guestvm.fs.FSTable;
import com.sun.guestvm.fs.VirtualFileSystem;

import static com.sun.guestvm.fs.VirtualFileSystem.*;
import com.sun.max.annotate.*;

/** Substitutions for @see java.io.UnixFileSystem.
 * @author Mick Jordan
 */

@SuppressWarnings("unused")

@METHOD_SUBSTITUTIONS(hiddenClass = "java.io.UnixFileSystem")
public final class JDK_java_io_UnixFileSystem {

    private JDK_java_io_UnixFileSystem() {
    }

    public static boolean currentOrParent(String name) {
        if (name.charAt(0) == '.') {
            final int length = name.length();
            if (length == 1) {
                return true;
            } else if (length == 2) {
                return name.charAt(1) == '.';
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @SUBSTITUTE
    private String canonicalize0(String path) throws IOException {
        return path; // TODO: make the original method work
    }

    @SUBSTITUTE
    private int getBooleanAttributes0(File f) {
        final VirtualFileSystem fs = FSTable.exports(f);
        if (fs == null) {
            return 0;
        }
        final int mode = fs.getMode(f.getPath());
        if (mode < 0) {
            return 0;
        }
        final int fmt = mode & S_IFMT;
        return BA_EXISTS | (fmt == S_IFREG ? BA_REGULAR : 0) | (fmt == S_IFDIR ? BA_DIRECTORY : 0);
    }

    @SUBSTITUTE
    private long getLastModifiedTime(File f) {
        final VirtualFileSystem fs = FSTable.exports(f);
        if (fs == null) {
            return 0;
        }
        return fs.getLastModifiedTime(f.getPath());
    }

    @SUBSTITUTE
    private boolean checkAccess(File f, int access) {
        final VirtualFileSystem fs = FSTable.exports(f);
        if (fs == null) {
            return false;
        }
        return fs.checkAccess(f.getPath(), access);
    }

    @SUBSTITUTE
    private long getLength(File f) {
        final VirtualFileSystem fs = FSTable.exports(f);
        if (fs == null) {
            return 0;
        }
        return fs.getLength(f.getPath());
    }

    @SUBSTITUTE
    private boolean setPermission(File f, int access, boolean enable, boolean owneronly) {
        final VirtualFileSystem fs = FSTable.exports(f);
        if (fs == null) {
            return false;
        }
        return fs.setPermission(f.getPath(), access, enable, owneronly);
    }

    @SUBSTITUTE
    private boolean createFileExclusively(String path) throws IOException {
        final VirtualFileSystem fs = FSTable.exports(path);
        if (fs == null) {
            return false;
        }
        return fs.createFileExclusively(path);
    }

    @SUBSTITUTE
    private boolean delete0(File f) {
        final VirtualFileSystem fs = FSTable.exports(f);
        if (fs == null) {
            return false;
        }
        return fs.delete0(f.getPath());
    }

    @SUBSTITUTE
    private String[] list(File f) {
        final VirtualFileSystem fs = FSTable.exports(f);
        if (fs == null) {
            return null;
        }
        return fs.list(f.getPath());
    }

    @SUBSTITUTE
    private boolean createDirectory(File f) {
        final VirtualFileSystem fs = FSTable.exports(f);
        if (fs == null) {
            return false;
        }
        return fs.createDirectory(f.getPath());
    }

    @SUBSTITUTE
    private boolean rename0(File f1, File f2) {
        final VirtualFileSystem fs = FSTable.exports(f1);
        if (fs == null) {
            return false;
        }
        return fs.rename0(f1.getPath(), f2.getPath());
    }

    @SUBSTITUTE
    private boolean setLastModifiedTime(File f, long time) {
        final VirtualFileSystem fs = FSTable.exports(f);
        if (fs == null) {
            return false;
        }
        return fs.setLastModifiedTime(f.getPath(), time);
    }

    @SUBSTITUTE
    private boolean setReadOnly(File f) {
        final VirtualFileSystem fs = FSTable.exports(f.getPath());
        if (fs == null) {
            return false;
        }
        return fs.setReadOnly(f.getPath());
    }

    @SUBSTITUTE
    private long getSpace(File f, int t) {
        final VirtualFileSystem fs = FSTable.exports(f);
        if (fs == null) {
            return 0;
        }
        return fs.getSpace(f.getPath(), t);
    }

    @SUBSTITUTE
    private static void initIDs() {
    }

    private static void init() {
    }

}
