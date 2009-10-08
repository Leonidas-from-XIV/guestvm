package test.java.io;

import java.io.*;
import java.util.*;

/**
 * A stress test for file I/O.
 * First we create a tree of files of known sizes and content.
 * Then we spawn a given number of threads to read files at random,
 * checking the content. Optionally, we can also create writer threads that
 * create additional files.
 *
 * @author Mick Jordan
 *
 */
public class FileStressTest {

    private static final int MAXFILESIZE = 16384;
    private static Map<String, byte[]> _fileTable = new HashMap<String, byte[]>();
    private static String[] _fileList;
    private static Random _rand;
    private static int _createSeed = 467349;
    private static int _readSeed = 756433;
    private static boolean _verbose;
    private static String _rootName = "/scratch";
    private static int _maxFiles = 10;
    private static int _maxDirs = 3;
    private static int _depth = 5;
    private static int _numReads = 1000;
    private static int _numWrites = 50;
    /**
     * @param args
     */
    public static void main(String[] args)  throws Exception {
        int numReaders = 1;
        int numWriters = 0;
        // Checkstyle: stop modified control variable check
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.equals("r")) {
                numReaders = Integer.parseInt(args[++i]);
            } else if (arg.equals("w")) {
                numWriters = Integer.parseInt(args[++i]);
            } else if (arg.equals("d")) {
                _depth = Integer.parseInt(args[++i]);
            } else if (arg.equals("mf")) {
                _maxFiles = Integer.parseInt(args[++i]);
            } else if (arg.equals("md")) {
                _maxDirs = Integer.parseInt(args[++i]);
            } else if (arg.equals("root")) {
                _rootName = args[++i];
            } else if (arg.equals("v")) {
                _verbose = true;
            }
        }
        _rand = new Random(_createSeed);
        final File root = new File(_rootName + "/d0");
        root.mkdir();
        createTree(root, _depth, _maxDirs, _maxFiles);
        _fileList = new String[_fileTable.size()];
        _fileTable.keySet().toArray(_fileList);
        if (_verbose) {
            System.out.println("created " + _fileTable.size() + " files");
        }

        final Thread[] readers = new Thread[numReaders];
        final Thread[] writers = new Thread[numWriters];
        for (int r = 0; r < numReaders; r++) {
            readers[r] = new Reader(r);
            readers[r].setName("Reader-" + r);
            readers[r].start();
        }
        for (int w = 0; w < numWriters; w++) {
            writers[w] = new Writer();
            writers[w].setName("Writer-" + w);
            writers[w].start();
        }
        for (int r = 0; r < numReaders; r++) {
            readers[r].join();
        }
        for (int w = 0; w < numWriters; w++) {
            writers[w].join();
        }
    }

    private static void createTree(File parent, int depth, int maxDirs, int maxFiles) throws IOException {
        int filesToDo = _rand.nextInt(maxFiles + 1);
        int dirsToDo = (depth > 0) ? _rand.nextInt(maxDirs + 1) : 0;
        if (dirsToDo == 0) {
            dirsToDo = 1;
        }
        final File[] dirs = new File[dirsToDo];
        if (_verbose) {
            System.out.println("createTree " + parent.getAbsolutePath() + ", subdirs " + dirsToDo + ", files " + filesToDo);
        }
        while (dirsToDo > 0 || filesToDo > 0) {
            final boolean isFile = _rand.nextBoolean();
            if (isFile && filesToDo > 0) {
                createFile(parent, filesToDo);
                filesToDo--;
            } else if (dirsToDo > 0) {
                dirs[dirsToDo - 1] = new File(parent,  "d" + dirsToDo);
                dirs[dirsToDo - 1].mkdir();
                dirsToDo--;
            }
        }
        if (depth > 0) {
            final int nDepth = depth - 1;
            for (int d = 0; d < dirs.length; d++) {
                createTree(dirs[d], nDepth, maxDirs, maxFiles);
            }
        }
    }

    private static void createFile(File parent, int key) throws IOException {
        final File file = new File(parent, "f" + key);
        final FileOutputStream wr = new FileOutputStream(file);
        final int size = _rand.nextInt(MAXFILESIZE);
        final byte[] data = new byte[size];
        _rand.nextBytes(data);
        if (_verbose) {
            System.out.println("createFile " + file.getAbsolutePath());
        }
        try {
            wr.write(data);
            _fileTable.put(file.getAbsolutePath(), data);
        } finally {
            wr.close();
        }
    }

    static class Reader extends Thread {
        private int _id;
        Reader(int id) {
            _id = id;
        }
        public void run() {
            int numReads = _numReads;
            Random rand = new Random(_readSeed + _id *17);
            while (numReads > 0) {
                final int index = rand.nextInt(_fileList.length);
                FileInputStream in = null;
                final String name = _fileList[index];
                if (_verbose) {
                    System.out.println(Thread.currentThread().getName() + ": reading " + name);
                }
                try {
                    in = new FileInputStream(name);
                    final byte[] writtenData = _fileTable.get(name);
                    final byte[] readData = new byte[writtenData.length];
                    final int n = in.read(readData);
                    if (n != writtenData.length) {
                        throw new IOException("length mismatch on file " + name + ", size " + writtenData.length + ", read " + n);
                    }
                    for (int i = 0; i < n; i++) {
                        if (readData[i] != writtenData[i]) {
                            throw new IOException("read mismatch on file " + name + ", wrote " + writtenData[i] + ", read " + readData[i]);
                        }
                    }
                } catch (Exception ex) {
                    System.out.println(Thread.currentThread().getName() + ": " + ex);
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException ex) {
                        }
                    }
                }
                numReads--;
            }
        }
    }

    static class Writer extends Thread {
        public void run() {
            int d = 0;
            int numWrites = _numWrites;
            while (numWrites > 0) {
                File root = new File(_rootName + "/w" + d);
                try {
                    if (_verbose) {
                        System.out.println(Thread.currentThread().getName() + " creating tree at " + root.getAbsolutePath());
                    }
                    root.mkdir();
                    createTree(root, _depth, _maxDirs, _maxFiles);
                } catch (IOException ex) {
                    System.out.println(Thread.currentThread().getName() + ": " + ex);
                }
            }
            numWrites--;
        }
    }

}
