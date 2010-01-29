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
package com.sun.guestvm.test;

import java.io.*;


public class PreRunFileCopy {

    private static boolean _verbose;

    public PreRunFileCopy() {

    }

    public static void premain(String agentArgs) throws IOException {
        final String[] args = agentArgs.split(",");
        if (args.length < 2) {
            usage();
        }
        final String from = args[0];
        final String to = args[1];
        _verbose = args.length > 2 && args[2].equals("verbose");
        copyFiles(new File(from), new File(to));
    }

    private static void usage() throws IOException {
        throw new IOException("usage: from,to[,verbose]");
    }

    private static void copyFiles(File dir1, File dir2) throws IOException {
        final File[] files = dir1.listFiles();
        if (files == null) {
            throw new IOException(dir1 + "  not found");
        }
        for (File f : files) {
            final File dir2File = new File(dir2, f.getName());
            if (f.isDirectory()) {
                if (!dir2File.exists()) {
                    if (!dir2File.mkdir()) {
                        throw new IOException("cannot create directory " + dir2File.getAbsolutePath());
                    }
                } else {
                    if (dir2File.isFile()) {
                        throw new IOException(dir2File.getAbsolutePath() + " exists as a file");
                    }
                }
                copyFiles(f, dir2File);
            } else {
                copyFile(f.getAbsolutePath(), dir2File.getAbsolutePath());
            }
        }
    }

    private static void copyFile(String fileName, String toFile) {
        if (_verbose) {
            System.out.println("copyFile  " + fileName + " " + toFile);
        }
        FileInputStream fsIn = null;
        FileOutputStream fsOut = null;
        try {
            fsIn = new FileInputStream(fileName);
            fsOut = new FileOutputStream(toFile);
            int n;
            final byte[] buf = new byte[512];
            while ((n = fsIn.read(buf)) != -1) {
                fsOut.write(buf, 0, n);
            }
        } catch (IOException ex) {
            System.out.println(ex);
        } finally {
            if (fsIn != null) {
                try {
                    fsIn.close();
                } catch (Exception ex) {
                }
            }
            if (fsOut != null) {
                try {
                    fsOut.close();
                } catch (Exception ex) {
                }
            }
        }
    }
}
