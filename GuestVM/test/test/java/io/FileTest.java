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
package test.java.io;

import java.io.*;

public class FileTest {

    private static RandomAccessFile _raFile;
    private static int _bufSize = 128;

    public static void main(String[] args) {
        final String[] fileNames = new String[10];
        final String[] fileNames2 = new String[10];
        final String[] ops = new String[10];
        int opCount = 0;
        boolean echo = false;
        boolean append = false;

        // Checkstyle: stop modified control variable check
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.equals("f")) {
                fileNames[opCount] = args[++i];
            } else if (arg.equals("f2")) {
                fileNames2[opCount] = args[++i];
            } else if (arg.equals("op")) {
                ops[opCount++] = args[++i];
                fileNames[opCount] = fileNames[opCount - 1];
            } else if (arg.equals("echo")) {
                echo = true;
            } else if (arg.equals("bs")) {
                _bufSize = Integer.parseInt(args[++i]);
            } else if (arg.equals("a")) {
                append = true;
            }
        }
        // Checkstyle: resume modified control variable check

        if (opCount == 0) {
            System.out.println("no operations given");
            return;
        }
        for (int j = 0; j < opCount; j++) {
            try {
                final String fileName = fileNames[j];
                final File file = new File(fileName);
                final String op = ops[j];
                if (echo) {
                    System.out.println("command: " + op + " " + fileName);
                }
                if (op.equals("canRead")) {
                    System.out.println("canRead " + fileName + " returned "
                            + file.canRead());
                } else if (op.equals("canWrite")) {
                    System.out.println("canWrite " + fileName + " returned "
                            + file.canWrite());
                } else if (op.equals("canExecute")) {
                    System.out.println("canExecute " + fileName + " returned "
                            + file.canExecute());
                } else if (op.equals("exists")) {
                    System.out.println("exists " + fileName + " returned "
                            + file.exists());
                } else if (op.equals("isFile")) {
                    System.out.println("isFile " + fileName + " returned "
                            + file.isFile());
                } else if (op.equals("isDirectory")) {
                    System.out.println("isDirectory " + fileName + " returned "
                            + file.isDirectory());
                } else if (op.equals("getLength")) {
                    System.out.println("length of " + fileName + " is "
                            + file.length());
                } else if (op.equals("setLength")) {
                    final RandomAccessFile ra = new RandomAccessFile(fileName, "rw");
                    try {
                        ra.setLength(Long.parseLong(fileNames2[j]));
                        System.out.println("setLength of " + fileName + " ok ");
                        ra.close();
                    } catch (IOException ex) {
                        System.out.println("setLength of " + fileName + " failed: " + ex.toString());
                    }
                } else if (op.equals("setReadOnly")) {
                    System.out.println("setReadOnly " + fileName + " returned "
                            + file.setReadOnly());
                } else if (op.equals("setWritable")) {
                    System.out.println("setWritable " + fileName + " returned "
                            + file.setWritable(true));
                } else if (op.equals("setExecutable")) {
                    System.out.println("setExecutable " + fileName
                            + " returned " + file.setExecutable(true));
                } else if (op.equals("unsetExecutable")) {
                    System.out.println("unsetExecutable " + fileName
                            + " returned " + file.setExecutable(false));
                } else if (op.equals("list")) {
                    final File[] files = file.listFiles();
                    if (files == null) {
                        System.out.println("list returned null");
                    } else {
                        listFiles(file.getAbsolutePath(), files);
                    }
                } else if (op.equals("delete")) {
                    final boolean rc = file.delete();
                    System.out.println("file delete of " + fileName
                            + checkRc(rc));
                } else if (op.equals("lastModified")) {
                    System.out.println("mtime of " + fileName + " is "
                            + file.lastModified());
                } else if (op.equals("isDirectory")) {
                    System.out.println("isDirectory of " + fileName + " is "
                            + file.isDirectory());
                } else if (op.equals("mkdir")) {
                    final boolean rc = file.mkdir();
                    System.out.println("mkdir of " + fileName + checkRc(rc));
                } else if (op.equals("rename")) {
                    final boolean rc = file.renameTo(new File(fileNames2[j]));
                    System.out.println("rename of " + fileName + " to "
                            + fileNames2[j] + checkRc(rc));
                } else if (op.equals("createNewFile")) {
                    try {
                        final boolean rc = file.createNewFile();
                        System.out.println("createNewFile of " + fileName
                                + checkRc(rc));
                    } catch (IOException ex) {
                        System.out.println(ex);
                    }
                } else if (op.equals("readFile")) {
                    readFile(fileName, true);
                } else if (op.equals("copyFile")) {
                    copyFile(fileName, fileNames2[j]);
                } else if (op.equals("compareFile")) {
                    compareFile(fileName, fileNames2[j]);
                } else if (op.equals("readFileSingle")) {
                    readFile(fileName, false);
                } else if (op.equals("writeFile")) {
                    writeFile(fileName, true, append);
                } else if (op.equals("writeFileSingle")) {
                    writeFile(fileName, false, append);
                } else if (op.equals("openRA")) {
                    _raFile = openRA(fileName, fileNames2[j]);
                } else if (op.equals("readRAFile")) {
                    readRAFile(true);
                } else if (op.equals("readRAFileSingle")) {
                    readRAFile(false);
                } else if (op.equals("getRAPtr")) {
                    System.out.println("getFilePointer returned " + _raFile.getFilePointer());
                } else if (op.equals("seekRA")) {
                    final long offset = Long.parseLong(fileNames2[j]);
                    _raFile.seek(offset);
                } else if (op.equals("getAbsolutePath")) {
                    System.out.println("getAbsolutePath of " + fileName + " returned " + file.getAbsolutePath());
                } else if (op.equals("getCanonicalPath")) {
                    System.out.println("getCanonicalPath of " + fileName + " returned " + file.getCanonicalPath());
                } else if (op.equals("createTempFile")) {
                    System.out.println("createTempFile of " + fileName + " returned " + File.createTempFile(fileName, null, null));
                } else {
                    System.out.println("unknown command: " + op);
                }
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }
    }

    private static String checkRc(boolean rc) {
        return rc ? " ok" : " not ok";
    }

    private static void listFiles(String fileName, File[] files) {
        System.out.println("Contents of " + fileName);
        for (File f : files) {
            System.out.println("  " + rwx(f) + f.length() + "  " + f.lastModified() + "  " + f.getName());
        }
    }

    private static String rwx(File file) {
        String result = file.isDirectory() ? "d" : "-";
        result += file.canRead() ? "r" : "-";
        result += file.canWrite() ? "w" : "-";
        result += file.canExecute() ? "x" : "-";
        return result + "   ";
    }

    private static void readFile(String fileName, boolean array) {
        System.out.println("readFile  " + fileName + " " + (array ? "multiple" : "single"));
        FileInputStream fs = null;
        try {
            fs = new FileInputStream(fileName);
            if (array) {
                int n;
                final byte[] buf = new byte[_bufSize];
                while ((n = fs.read(buf)) != -1) {
                    System.out.write(buf, 0, n);
                }
            } else {
                int b;
                while ((b = fs.read()) != -1) {
                    System.out.write(b);
                }
            }
        } catch (IOException ex) {
            System.out.println(ex);
        } finally {
            if (fs != null) {
                try {
                    fs.close();
                } catch (Exception ex) {
                }
            }
        }
    }

    private static void copyFile(String fileName, String toFile) {
        System.out.println("copyFile  " + fileName + " " + toFile);
        FileInputStream fsIn = null;
        FileOutputStream fsOut = null;
        try {
            fsIn = new FileInputStream(fileName);
            fsOut = new FileOutputStream(toFile);
            int n;
            final byte[] buf = new byte[_bufSize];
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

    private static void compareFile(String fileName1, String fileName2) {
        System.out.println("compareFile  " + fileName1 + " " + fileName2);
        FileInputStream fsIn1 = null;
        FileInputStream fsIn2 = null;
        final long l1 = new File(fileName1).length();
        final long l2 = new File(fileName2).length();
        if (l1 != l2) {
            System.out.println("files are differerent lengths " + l1 + ", " + l2);
        }
        try {
            fsIn1 = new FileInputStream(fileName1);
            fsIn2 = new FileInputStream(fileName2);
            int n1;
            int c1 = 0;
            int c2 = 0;
            final byte[] buf1 = new byte[_bufSize];
            final byte[] buf2 = new byte[_bufSize];
            while ((n1 = fsIn1.read(buf1)) != -1) {
                final int n2 = fsIn2.read(buf2);
                if (n1 != n2) {
                    throw new IOException("file read length mismatch n1 " + n1 + " n2 " + n2 + " after " + c1 + " bytes");
                }
                c1 += n1;
                c2 += n2;
                for (int i = 0; i < n1; i++) {
                    if (buf1[i] != buf2[i]) {
                        throw new IOException("bytes differ at offset " + (n1 + i) + " b1 " + Integer.toHexString(buf1[i] & 0xFF) + " b2 " + Integer.toHexString(buf2[i] & 0xFF));
                    }
                }
            }
            System.out.println("files are equal");
        } catch (IOException ex) {
            System.out.println(ex);
        } finally {
            if (fsIn1 != null) {
                try {
                    fsIn1.close();
                } catch (Exception ex) {
                }
            }
            if (fsIn2 != null) {
                try {
                    fsIn2.close();
                } catch (Exception ex) {
                }
            }
        }
    }

    private static void writeFile(String fileName, boolean array, boolean append) {
        System.out.println("writeFile  " + fileName + " "
                + (array ? "multiple" : "single"));
        final String data = "The Quick Brown Fox Jumps Over The Lazy Dog\n";
        final byte[] byteData = data.getBytes();
        final int dataLength = data.length();
        FileOutputStream fs = null;
        try {
            fs = new FileOutputStream(fileName, append);
            for (int i = 0; i < 100; i++) {
                if (array) {
                    fs.write(byteData);
                } else {
                    for (int j = 0; j < dataLength; j++) {
                        fs.write(data.charAt(j));
                    }
                }
            }
        } catch (IOException ex) {
            System.out.println(ex);
        } finally {
            if (fs != null) {
                try {
                    fs.close();
                } catch (Exception ex) {
                }
            }
        }

    }

    private static RandomAccessFile openRA(String path, String mode) throws FileNotFoundException {
        return new RandomAccessFile(path, mode);
    }

    private static void readRAFile(boolean array) {
        System.out.println("readRAFile  "  + (array ? "multiple" : "single"));
        try {
            if (array) {
                int n;
                final byte[] buf = new byte[_bufSize];
                while ((n = _raFile.read(buf)) != -1) {
                    System.out.write(buf, 0, n);
                }
            } else {
                int b;
                while ((b = _raFile.read()) != -1) {
                    System.out.write(b);
                }
            }
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

}
