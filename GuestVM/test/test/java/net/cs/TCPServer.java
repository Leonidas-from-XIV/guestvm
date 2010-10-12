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

import java.net.*;
import java.io.*;

public class TCPServer extends ServerThread {

    public TCPServer(int threadNum, SessionData sessionData, int blobSize, int nbuffers, boolean oneRun, boolean checkData, boolean syncCheck, boolean ack, boolean verbose) {
        super(threadNum, sessionData, blobSize, nbuffers, oneRun, checkData, syncCheck, ack, verbose, "TCP");
    }

    private OutputStream _out;

    public void run() {
        super.run();
        try {
            final ServerSocket server = new ServerSocket(PORT + _threadNum);
            for (;;) {
                try {
                    final Socket sock = server.accept();
                    verbose("connection accepted on " + sock.getLocalPort() + " from " + sock.getInetAddress());
                    final InputStream in = sock.getInputStream();
                    _out = sock.getOutputStream();
                    int totalOps = 0;
                    _serverOutOfBuffersCount = 0;
                    for (;;) {
                        final byte[] data = getBuffer();

                        final int ch1 = in.read();
                        final int ch2 = in.read();
                        final int ch3 = in.read();
                        final int ch4 = in.read();
                        if ((ch1 | ch2 | ch3 | ch4) < 0) {
                            break;
                        }

                        final int bytesToRead = (ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0);

                        in.read(); // id
                        int totalRead = 0;
                        int len;
                        try {
                            // CheckStyle: stop inner assignment check
                            while ((totalRead < bytesToRead) && ((len = in.read(data, totalRead, bytesToRead - totalRead)) > 0)) {
                                totalRead += len;
                            }
                            // CheckStyle: resume inner assignment check
                        } catch (InterruptedIOException e) {
                            e.printStackTrace();
                            sock.close();
                            return;
                        }

                        check(data, totalRead);

                        if (totalRead == 0) {
                            break;
                        }
                        totalOps++;
                    }
                    in.close();
                    _out.close();
                    verbose("OPS: " + totalOps + ", out of buffers " + _serverOutOfBuffersCount + " times");
                    if (_oneRun) {
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void doAck() throws IOException {
        _out.write(ACK_BYTES, 0, ACK_BYTES.length);
    }
}
