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

public class TCPClient extends ClientThread {

    public TCPClient(String host, int threadNum, SessionData sessionData,
            long timeToRun, boolean ack, long delay, boolean verbose) {
        super(host, threadNum, sessionData, timeToRun, ack, delay, verbose, "TCP");
    }

    private OutputStream _out;
    private InputStream _in;
    int _id;

    public void run() {
        super.run();
        try {
            final Socket sock = new Socket(_host, ServerThread.PORT + _threadNum);

            if (_verbose) {
                verbose("connected to server");
            }

            _out = sock.getOutputStream();
            _in = sock.getInputStream();

            _id = 0;
            writeLoop();
            _out.close();
            _in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void doSend(byte[] data) throws IOException {
        verbose("writing " + data.length + " bytes");
        final int v = data.length;
        _out.write((v >>> 24) & 0xFF);
        _out.write((v >>> 16) & 0xFF);
        _out.write((v >>> 8) & 0xFF);
        _out.write((v >>> 0) & 0xFF);

        _out.write(_id & 0xFF);
        _out.write(data, 0, v);

        _id++;
        int len;
        if (_ack) {
            int totalRead = 0;
            // CheckStyle: stop inner assignment check
            while ((totalRead != ServerThread.ACK_BYTES.length)
                    && ((len = _in.read(_ackBytes, totalRead,
                            ServerThread.ACK_BYTES.length - totalRead)) > 0)) {
                totalRead += len;
            }
            // CheckStyle: resume inner assignment check
            verbose("data ack ok");
        }

    }
}
