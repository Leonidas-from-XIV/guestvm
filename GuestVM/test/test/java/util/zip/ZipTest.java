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
package test.java.util.zip;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.util.jar.*;

import test.util.OSSpecific;

public class ZipTest {

    private static Map<String, ZipEntry> _zipMap = new HashMap<String, ZipEntry>();
    private static boolean _quiet = false;
    private static int _randomSeed = 467377;
    private static boolean _traceMM;

    /**
     * @param args
     */
    public static void main(String[] args) {
        final String[] fileNames = new String[10];
        final String[] fileNames2 = new String[10];
        final String[] ops = new String[10];
        int opCount = 0;
        // Checkstyle: stop modified control variable check
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.equals("f")) {
                fileNames[opCount] = args[++i];
            } else  if (arg.equals("f2")) {
                fileNames2[opCount] = args[++i];
            } else if (arg.equals("op")) {
                ops[opCount++] = args[++i];
                fileNames[opCount] = fileNames[opCount - 1];
                fileNames2[opCount] = fileNames2[opCount - 1];
            } else if (arg.equals("-tmm")) {
                _traceMM = true;
            }
        }
        // Checkstyle: resume modified control variable check

        ZipFile zipFile = null;
        JarFile jarFile = null;
        ZipInputStream zipStream = null;
        for (int j = 0; j < opCount; j++) {
            try {
                final String fileName = fileNames[j];
                final String op = ops[j];
                if (op.equals("open")) {
                    zipFile = new ZipFile(fileName);
                } else if (op.equals("openStream")) {
                    zipStream = openZipStream(fileName);
                } else if (op.equals("openJar")) {
                    jarFile = new JarFile(fileName);
                    zipFile = jarFile;
                } else if (op.equals("close")) {
                    zipFile.close();
                } else if (op.equals("getEntry")) {
                    doGetEntry(checkOpen(zipFile), fileNames[j]);
                } else if (op.equals("entries")) {
                    doEntries(checkOpen(zipFile), false);
                } else if (op.equals("entriesDetails")) {
                    doEntries(checkOpen(zipFile), true);
                } else if (op.equals("entriesStream")) {
                    doEntriesStream(checkOpen(zipStream), false);
                } else if (op.equals("entriesStreamDetails")) {
                    doEntriesStream(checkOpen(zipStream), true);
                } else if (op.equals("readEntry")) {
                    doReadEntry(checkOpen(zipFile), fileNames[j], null, fileNames2[j] != null && fileNames2[j].equals("print"));
                } else if (op.equals("copyEntry")) {
                    doReadEntry(checkOpen(zipFile), fileNames[j], fileNames2[j], false);
                } else if (op.equals("metaNames")) {
                    checkOpen(zipFile);
                    doMetaNames(jarFile);
                } else if (op.equals("randomSeed")) {
                    _randomSeed = Integer.parseInt(fileNames2[j]);
                } else if (op.equals("randomRead")) {
                    doRandomRead(checkOpen(zipFile), Integer.parseInt(fileNames2[j]));
                } else if (op.equals("q")) {
                    _quiet = true;
                } else if (op.equals("v")) {
                    _quiet = false;
                }
            } catch  (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

    private static ZipInputStream openZipStream(String fileName) throws Exception {
        return new ZipInputStream(new BufferedInputStream(new FileInputStream(fileName)));
    }

    private static ZipFile checkOpen(ZipFile zipFile) throws Exception {
        if (zipFile == null) {
            throw new Exception("zip file not opened");
        }
        return zipFile;
    }

    private static ZipInputStream checkOpen(ZipInputStream zipStream) throws Exception {
        if (zipStream == null) {
            throw new Exception("zip file not opened");
        }
        return zipStream;
    }

    private static void doGetEntry(ZipFile zipFile, String entryName) {
        final ZipEntry zipEntry = zipFile.getEntry(entryName);
        if (zipEntry == null) {
            System.out.println("entry " + entryName + "  not found");
        } else {
            displayEntry(zipEntry);
            System.out.println("");
        }
    }

    private static void displayEntry(ZipEntry zipEntry) {
        System.out.print(" size: " + zipEntry.getSize() +
                        " csize: " + zipEntry.getCompressedSize() +
                        " crc: " + Long.toHexString(zipEntry.getCrc()) +
                        " method: " + zipEntry.getMethod() +
                        " time: " + zipEntry.getTime() +
                        " comment: " + zipEntry.getComment() +
                        " extra: " + zipEntry.getExtra());
    }

    private static void doEntries(ZipFile zipFile, boolean verbose) {
        final Enumeration<? extends ZipEntry> iter = zipFile.entries();
        while (iter.hasMoreElements()) {
            final ZipEntry zipEntry = iter.nextElement();
            handleEntry(zipEntry, verbose);
        }
    }

    private static void handleEntry(ZipEntry zipEntry, boolean verbose) {
        if (!_quiet) {
            System.out.print(zipEntry.getName());
            if (verbose) {
                displayEntry(zipEntry);
            }
        }
        _zipMap.put(zipEntry.getName(), zipEntry);
        if (!_quiet) {
            System.out.println("");
        }

    }

    private static void doEntriesStream(ZipInputStream zipStream, boolean verbose) throws IOException {
        ZipEntry zipEntry;
        while((zipEntry = zipStream.getNextEntry()) != null) {
            handleEntry(zipEntry, verbose);
        }
    }

    private static void doReadEntry(ZipFile zipFile, String entryName, String outFile, boolean print) {
        System.out.println("readEntry " + entryName + " outFile " + outFile + " print " + print);
        final ZipEntry zipEntry = zipFile.getEntry(entryName);
        final long size = zipEntry.getSize();
        if (zipEntry == null) {
            System.out.println("entry " + entryName + " not found");
            return;
        }
        InputStream is = null;
        OutputStream os = null;
        try {
            if (outFile != null) {
                os = new FileOutputStream(outFile);
            }
            is = zipFile.getInputStream(zipEntry);
            int n = 0;
            int pc = 0;
            long rsize = 0;
            final byte[] buf = new byte[128];
            while ((n = is.read(buf)) > 0) {
                rsize += n;
                if (outFile == null) {
                    for (int i = 0; i < n; i++) {
                        if (print) {
                            final int d1 = (buf[i] >> 4) & 0xF;
                            final int d2 = buf[i] & 0xF;
                            System.out.print(Integer.toHexString(d1));
                            System.out.print(Integer.toHexString(d2));
                            pc++;
                            if (pc % 32  == 0) {
                                System.out.println();
                            } else {
                                System.out.print(" ");
                            }
                        } else {
                            System.out.write(buf[i]);
                        }
                    }
                } else {
                    os.write(buf, 0, n);
                }
            }
            if (rsize != size) {
                System.out.println("size mismatch: " + size + ", read " + rsize);
            }
        } catch (IOException ex) {
            System.out.println(ex);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ex) {
                }
            }
        }
    }

    private static void doMetaNames(JarFile jarFile) throws IOException {
        final Manifest manifest = jarFile.getManifest();
        for (Map.Entry<String, Attributes> entry : manifest.getEntries().entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    private static void doRandomRead(ZipFile zipFile, int count)  throws Exception {
        if (_zipMap.size() == 0) {
            throw new Exception("entries not read");
        }
        final int size = _zipMap.size();
        final ZipEntry[] entries = new ZipEntry[size];
        _zipMap.values().toArray(entries);
        final Random random = new Random(_randomSeed);
        final byte[] buf = new byte[128];
        int ccount = count;
        while (ccount-- > 0) {
            final int entryIndex = random.nextInt(size);
            final ZipEntry zipEntry = entries[entryIndex];
            final long entrySize = zipEntry.getSize();
            if (!_quiet) {
                System.out.println("reading entry " + zipEntry.getName());
            }
            InputStream is = null;
            try {
                is = zipFile.getInputStream(zipEntry);
                long n = 0;
                long totalRead = 0;
                while ((n = is.read(buf)) > 0) {
                    totalRead += n;
                }
                if (totalRead != entrySize) {
                    throw new Exception("size mismatch, read " + totalRead + " size " + size);
                }
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ex) {
                    }
                }
            }
        }
        System.out.println("randomRead read " + count + " entries");
    }
}
