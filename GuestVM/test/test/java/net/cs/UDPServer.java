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

public class UDPServer extends ServerThread {
    public UDPServer(int threadNum, SessionData sessionData, int blobSize,
            int nbuffers, boolean oneRun, boolean checkData, boolean syncCheck,
            boolean ack, boolean verbose) {
        super(threadNum, sessionData, blobSize, nbuffers, oneRun, checkData, syncCheck, ack, verbose, "UDP");
    }


    private DatagramSocket _socket;
    private DatagramPacket _packet;

    public void run() {
        super.run();
        try {
            _socket = new DatagramSocket(PORT + _threadNum);
            _packet = new DatagramPacket((byte[]) _buffer.get(0), _blobSize);
            /* TODO
            if (_socket.getReceiveBufferSize() < blobSize) {
                int bufSize = nextPowerOf2(blobSize);
                _socket.setReceiveBufferSize(bufSize);
                System.out.println("setting receive buffer to " + bufSize);
            }
            */

            for (;;) {
                try {
                    int totalOps = 0;
                    _serverOutOfBuffersCount = 0;
                    for (;;) {

                        final byte[] data = getBuffer();
                        _packet.setData(data);
                        int totalRead = 0;
                        while (totalRead < _blobSize) {
                            _socket.receive(_packet);
                            final int len = _packet.getLength();
                            if (len == 0) {
                                break;
                            }
                            totalRead += len;
                        }

                        if (totalRead == 0) {
                            break;
                        }

                        check(data, totalRead);
                        totalOps++;
                    }
                    // Done with this session
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
        _packet.setData(ACK_BYTES);
        _socket.send(_packet);
    }

    @SuppressWarnings("unused")
    private int nextPowerOf2(int size) {
        int s = 2;
        for (int i = 1; i < 32; i++) {
            if (s >= size) {
                return s;
            }
            s = s * 2;
        }
        throw new RuntimeException("nextPowerOf2!!");
    }

}
