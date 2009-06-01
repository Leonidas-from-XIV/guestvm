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

/**
 * Substitutions for the native methods in java.util.ZipFile.
 * Unfortunately, ZipFile traffics in long values that are, in traditional VMs, addresses of structures allocated on the C heap.
 * We can't change this without changing the implementation of ZipFile itself. So we have to  convert the real objects
 * that we use into small integers to comply with this interface.
 *
 * @author Mick Jordan
 */

import java.util.zip.*;

import com.sun.guestvm.zip.*;
import com.sun.max.annotate.*;

@SuppressWarnings("unused")

@METHOD_SUBSTITUTIONS(ZipFile.class)
public class JDK_java_util_zip_ZipFile {

    @SUBSTITUTE
    private static long open(String name, int mode, long lastModified) throws ZipException {
        return ZZipFile.create(name, mode, lastModified).getId();
    }

    @SUBSTITUTE
    private static int getTotal(long jzfile) {
        return ZZipFile.get(jzfile).getTotal();
    }

    @SUBSTITUTE
    private static long getEntry(long jzfile, String name, boolean addSlash) {
        return ZZipFile.get(jzfile).getEntry(name, addSlash);
    }

    @SUBSTITUTE
    private static void freeEntry(long jzfile, long jzentry) {
        // nothing to do as we don't allocate jzentry
    }

    @SUBSTITUTE
    private static int getMethod(long jzentry) {
        return ZZipFile.getMethod(jzentry);
    }

    @SUBSTITUTE
    private static long getNextEntry(long jzfile, int i) {
        return ZZipFile.get(jzfile).getNextEntry(i);
    }

    @SUBSTITUTE
    private static void close(long jzfile) {
        ZZipFile.get(jzfile).close(jzfile);
    }

    @SUBSTITUTE
    private static int read(long jzfile, long jzentry, long pos, byte[] b, int off, int len)  throws ZipException {
        return ZZipFile.read(jzfile, jzentry, pos, b, off, len);
    }

    @SUBSTITUTE
    private static long getCSize(long jzentry) {
        return ZZipFile.getCSize(jzentry);
    }

    @SUBSTITUTE
    private static long getSize(long jzentry) {
        return ZZipFile.getSize(jzentry);
    }

    @SUBSTITUTE
    private static String getZipMessage(long jzfile) {
        return null;
    }

    @SUBSTITUTE
    private static void initIDs() {

    }

}
