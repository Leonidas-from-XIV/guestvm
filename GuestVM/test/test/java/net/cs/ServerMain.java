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
package test.java.net.cs;

// Simple test of Server - simply consumes and discards data

import java.lang.reflect.Constructor;

public class ServerMain {

    public static void main(String[] args) {

        SessionData sessionData;
        int blobSize = 1000;
        int nbuffers = 100;
        boolean oneRun = false;
        boolean checkData = false;
        boolean syncCheck = true;
        boolean ack = true;
        boolean verbose = false;
        String sdImpl = "test.java.net.cs.Default";
        String protocol = "UDP";

        String serializedDataFile = null;

        int nthreads = 1;
        // Checkstyle: stop modified control variable check
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.charAt(0) == '-') {
                arg = arg.substring(1);
            }
            if (arg.equals("bs")) {
                i++;
                blobSize = Integer.parseInt(args[i]);
            } else if (arg.equals("sf")) {
                i++;
                serializedDataFile = args[i];
            } else if (arg.equals("onerun")) {
                oneRun = true;
            } else if (arg.equals("check")) {
                checkData = true;
            } else if (arg.equals("sync")) {
                syncCheck = false;
            } else if (arg.equals("noack")) {
                ack = false;
            } else if (arg.equals("th")) {
                i++;
                nthreads = checkForInt(args, i);
            } else if (arg.equals("buffers")) {
                i++;
                nbuffers = checkForInt(args, i);
            } else if (arg.equals("verbose")) {
                verbose = true;
            } else if (arg.equals("sdimpl")) {
                i++;
                sdImpl = args[i];
            } else if (arg.equals("type")) {
                protocol = args[++i];
            }
        }
        // Checkstyle: resume modified control variable check

        try {
            if (serializedDataFile != null) {
                sessionData = new FileSessionData(serializedDataFile);
            } else {
                sessionData = (SessionData) Class.forName(
                        sdImpl + "SessionData").newInstance();
                sessionData.setDataSize(blobSize);
            }

            final ServerThread[] serverThreads = new ServerThread[nthreads];
            final Consumer[] consumerThreads = new Consumer[nthreads];
            for (int i = 0; i < nthreads; i++) {
                final Class<?> serverClass = Class.forName("test.java.net.cs." + protocol + "Server");
                final Constructor<?> serverCons = serverClass.getConstructor(int.class, SessionData.class, int.class, int.class,
                        boolean.class, boolean.class, boolean.class, boolean.class, boolean.class);
                serverThreads[i] = (ServerThread) serverCons.newInstance(i, sessionData, blobSize,
                        nbuffers, oneRun, checkData, syncCheck, ack, verbose);
                // create a thread to consume data from server
                consumerThreads[i] = new Consumer(serverThreads[i], blobSize);

            }
            for (int i = 0; i < nthreads; i++) {
                serverThreads[i].start();
                consumerThreads[i].start();
            }
            for (int i = 0; i < nthreads; i++) {
                serverThreads[i].join();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int checkForInt(String[] args, int i) {
        if ((i < args.length)
                && (('0' <= args[i].charAt(0)) && args[i].charAt(0) <= '9')) {
            return Integer.parseInt(args[i]);
        } else {
            System.out.println("usage: integer value expected");
            System.exit(1);
            return -1;
        }
    }

    static class Consumer extends Thread {
        private ServerThread _server;
        private byte[] _data;

        public Consumer(ServerThread server, int blobSize) {
            _data = new byte[blobSize];
            _server = server;
            setDaemon(true);
        }

        public void run() {
            for (;;) {
                _server.getData(_data);
            }
        }
    }
}

