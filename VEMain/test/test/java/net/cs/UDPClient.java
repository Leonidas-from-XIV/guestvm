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

public class UDPClient extends ClientThread {

    public UDPClient(String host, int threadNum, SessionData sessionData,
            long timeToRun, boolean ack, long delay, boolean verbose) {
        super(host, threadNum, sessionData, timeToRun, ack, delay, verbose, "UDP");
    }

    private DatagramSocket _socket;
    private DatagramPacket _packet;

    public void run() {
        super.run();
        try {
            _socket = new DatagramSocket();
            _packet = new DatagramPacket(_ackBytes,
                    _ackBytes.length, InetAddress.getByName(_host), ServerThread.PORT
                            + _threadNum);
            writeLoop();
            // sign off with a zero length packet
            _ack = false;
            doSend(new byte[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void doSend(byte[] data) throws IOException {
        verbose("writing " + data.length + " bytes");
        _packet.setData(data);
        _socket.send(_packet);
        if (_ack) {
            int totalRead = 0;
            while (totalRead != ServerThread.ACK_BYTES.length) {
                _packet.setData(_ackBytes);
                _socket.receive(_packet);
                totalRead += _packet.getLength();
            }
            verbose("data ack ok");
        }

    }

}

