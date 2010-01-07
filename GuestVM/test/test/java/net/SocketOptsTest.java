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
package test.java.net;

import java.net.*;

public class SocketOptsTest {

    private static Socket _socket = null;
    private static ServerSocket _serverSocket = null;
    private static DatagramSocket _datagramSocket = null;
    /**
     * @param args
     */
    public static void main(String[] args) {
        final String[] ops = new String[10];
        final String[] opArgs1 = new String[10];
        final String[] opArgs2 = new String[10];
        int opCount = 0;
        // Checkstyle: stop modified control variable check
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.equals("a") || arg.equals("a1")) {
                opArgs1[opCount] = args[++i];
            } else if (arg.equals("a2")) {
                opArgs2[opCount] = args[++i];
            } else if (arg.equals("op")) {
                ops[opCount++] = args[++i];
                opArgs1[opCount] = opArgs1[opCount - 1];
                opArgs2[opCount] = opArgs2[opCount - 1];
                            }
        }
        // Checkstyle: resume modified control variable check

        if (opCount == 0) {
            System.out.println("no operations given");
            return;
        }
        for (int j = 0; j < opCount; j++) {
            final String opArg1 = opArgs1[j];
            final String opArg2 = opArgs2[j];
            final String op = ops[j];

            try {
                if (op.equals("css")) {
                    _serverSocket = new ServerSocket(Integer.parseInt(opArg1));
                } else if (op.equals("cds")) {
                    _datagramSocket = new DatagramSocket(Integer.parseInt(opArg1));
                } else if (op.equals("cs")) {
                    _socket = new Socket();
                } else if (op.equals("getReceiveBufferSize")) {
                    System.out.println("getReceiveBufferSize=" + getReceiveBufferSize(opArg1));
                } else if (op.equals("setReceiveBufferSize")) {
                    setReceiveBufferSize(opArg1, Integer.parseInt(opArg2));
                    System.out.println("setReceiveBufferSize:" + opArg1 + " ok");
                } else if (op.equals("getSendBufferSize")) {
                    System.out.println("getSendBufferSize=" + getSendBufferSize(opArg1));
                } else if (op.equals("setSendBufferSize")) {
                    setSendBufferSize(opArg1, Integer.parseInt(opArg2));
                    System.out.println("setSendBufferSize:" + opArg1 + " ok");
                }
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }

    }

    private static int getReceiveBufferSize(String type) throws SocketException {
        if (type.equals("s")) {
            return _socket.getReceiveBufferSize();
        } else if (type.equals("ss")) {
            return _serverSocket.getReceiveBufferSize();
        } else if (type.equals("ds")) {
            return _datagramSocket.getReceiveBufferSize();
        } else {
            throw new SocketException("unknown socket type: " + type);
        }
    }

    private static void setReceiveBufferSize(String type, int size) throws SocketException {
        if (type.equals("s")) {
            _socket.setReceiveBufferSize(size);
        } else if (type.equals("ss")) {
            _serverSocket.setReceiveBufferSize(size);
        } else if (type.equals("ds")) {
            _datagramSocket.setReceiveBufferSize(size);
        } else {
            throw new SocketException("unknown socket type: " + type);
        }
    }

    private static int getSendBufferSize(String type) throws SocketException {
        if (type.equals("s")) {
            return _socket.getSendBufferSize();
        } else if (type.equals("ss")) {
            throw new SocketException("getSendBufferSize on ss unavailable operation");
        } else if (type.equals("ds")) {
            return _datagramSocket.getSendBufferSize();
        } else {
            throw new SocketException("unknown socket type: " + type);
        }
    }

    private static void setSendBufferSize(String type, int size) throws SocketException {
        if (type.equals("s")) {
            _socket.setSendBufferSize(size);
        } else if (type.equals("ss")) {
            throw new SocketException("setSendBufferSize on ss unavailable operation");
        } else if (type.equals("ds")) {
            _datagramSocket.setSendBufferSize(size);
        } else {
            throw new SocketException("unknown socket type: " + type);
        }
    }
}


