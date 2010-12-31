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
package com.sun.max.ve.jdk;

import java.io.*;

import static com.sun.max.ve.fs.VirtualFileSystem.*;

import com.sun.max.annotate.*;
import com.sun.max.ve.fs.FSTable;
import com.sun.max.ve.fs.VirtualFileSystem;

/** Substitutions for @see java.io.UnixFileSystem.
 * @author Mick Jordan
 */

@SuppressWarnings("unused")

@METHOD_SUBSTITUTIONS(className = "java.io.UnixFileSystem")
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
        final VirtualFileSystem fs = FSTable.exports(path);
        if (fs == null) {
            return path;
        }
        return collapse(fs.canonicalize0(path));
    }

    /**
     * Remove . and .. components where possible
     * @param path
     * @return collapsed path
     */
    private static String collapse(String path) {
        assert path.charAt(0) == '/';
        final String[] parts = path.split("/");
        int removed = 0;
        for (int i = 1; i < parts.length; i++) {
            final String part = parts[i];
            final int partLen = part.length();
            if (partLen == 1 && part.charAt(0) == '.') {
                parts[i] = null;
                removed++;
            } else if (partLen == 2 && part.charAt(0) == '.' && part.charAt(1) == '.') {
                int j;
                for (j = i - 1; j >= 1; j--) {
                    if (parts[j] != null) {
                        break;
                    }
                }
                if (j <= 0) {
                    continue;
                }
                parts[j] = null;
                parts[i] = null;
                removed += 2;
            }
        }
        if (removed > 0) {
            final StringBuilder sb = new StringBuilder();
            sb.append('/');
            boolean needSep = false;
            for (String part : parts) {
                if (part != null && part.length() > 0) {
                    if (needSep) {
                        sb.append('/');
                    }
                    sb.append(part);
                    needSep = true;
                }
            }
            return sb.toString();
        }
        return path;
    }

    @SUBSTITUTE
    private int getBooleanAttributes0(File f) {
        final String absPath = f.getAbsolutePath();
        final VirtualFileSystem fs = FSTable.exports(absPath);
        if (fs == null) {
            return 0;
        }
        final int mode = fs.getMode(absPath);
        if (mode < 0) {
            return 0;
        }
        final int fmt = mode & S_IFMT;
        return BA_EXISTS | (fmt == S_IFREG ? BA_REGULAR : 0) | (fmt == S_IFDIR ? BA_DIRECTORY : 0);
    }

    @SUBSTITUTE
    private long getLastModifiedTime(File f) {
        final String absPath = f.getAbsolutePath();
        final VirtualFileSystem fs = FSTable.exports(absPath);
        if (fs == null) {
            return 0;
        }
        return fs.getLastModifiedTime(absPath);
    }

    @SUBSTITUTE
    private boolean checkAccess(File f, int access) {
        final String absPath = f.getAbsolutePath();
        final VirtualFileSystem fs = FSTable.exports(absPath);
        if (fs == null) {
            return false;
        }
        return fs.checkAccess(absPath, access);
    }

    @SUBSTITUTE
    private long getLength(File f) {
        final String absPath = f.getAbsolutePath();
        final VirtualFileSystem fs = FSTable.exports(absPath);
        if (fs == null) {
            return 0;
        }
        return fs.getLength(absPath);
    }

    @SUBSTITUTE
    private boolean setPermission(File f, int access, boolean enable, boolean owneronly) {
        final String absPath = f.getAbsolutePath();
        final VirtualFileSystem fs = FSTable.exports(absPath);
        if (fs == null) {
            return false;
        }
        int amode = 0;
        switch (access) {
            case ACCESS_READ:
                amode = owneronly ? S_IRUSR : S_IRUSR | S_IRGRP | S_IROTH;
                break;
            case ACCESS_WRITE:
                amode = owneronly ? S_IWUSR : S_IWUSR | S_IWGRP | S_IWOTH;
                break;
            case ACCESS_EXECUTE:
                amode = owneronly ? S_IXUSR : S_IXUSR | S_IXGRP | S_IXOTH;
                break;
            default:
                assert false;
        }
        int mode = fs.getMode(absPath);
        if (mode >= 0) {
            if (enable) {
                mode |= amode;
            } else {
                mode &= ~amode;
            }
            return fs.setMode(absPath, mode) >= 0;
        } else {
            return false;
        }
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
        final String absPath = f.getAbsolutePath();
        final VirtualFileSystem fs = FSTable.exports(absPath);
        if (fs == null) {
            return false;
        }
        return fs.delete0(absPath);
    }

    @SUBSTITUTE
    private String[] list(File f) {
        final String absPath = f.getAbsolutePath();
        final VirtualFileSystem fs = FSTable.exports(absPath);
        if (fs == null) {
            return null;
        }
        return fs.list(absPath);
    }

    @SUBSTITUTE
    private boolean createDirectory(File f) {
        final String absPath = f.getAbsolutePath();
        final VirtualFileSystem fs = FSTable.exports(absPath);
        if (fs == null) {
            return false;
        }
        return fs.createDirectory(absPath);
    }

    @SUBSTITUTE
    private boolean rename0(File f1, File f2) {
        final String absPath1 = f1.getAbsolutePath();
        final VirtualFileSystem fs1 = FSTable.exports(absPath1);
        if (fs1 == null) {
            return false;
        }
        final String absPath2 = f2.getAbsolutePath();
        final VirtualFileSystem fs2 = FSTable.exports(absPath2);
        if (fs1 != fs2) {
            return false;
        }
        return fs1.rename0(absPath1, absPath2);
    }

    @SUBSTITUTE
    private boolean setLastModifiedTime(File f, long time) {
        final String absPath = f.getAbsolutePath();
        final VirtualFileSystem fs = FSTable.exports(absPath);
        if (fs == null) {
            return false;
        }
        return fs.setLastModifiedTime(absPath, time);
    }

    @SUBSTITUTE
    private boolean setReadOnly(File f) {
        final String absPath = f.getAbsolutePath();
        final VirtualFileSystem fs = FSTable.exports(absPath);
        if (fs == null) {
            return false;
        }
        final int mode = fs.getMode(absPath);
        return fs.setMode(absPath, mode & ~(S_IWUSR | S_IWGRP | S_IWOTH)) >= 0;
    }

    @SUBSTITUTE
    private long getSpace(File f, int t) {
        final String absPath = f.getAbsolutePath();
        final VirtualFileSystem fs = FSTable.exports(absPath);
        if (fs == null) {
            return 0;
        }
        return fs.getSpace(absPath, t);
    }

    @SUBSTITUTE
    private static void initIDs() {
    }

    private static void init() {
    }

}
