/*
 * Copyright (c) 2007 Sun Microsystems, Inc.  All rights reserved.
 *
 * Sun Microsystems, Inc. has intellectual property rights relating to technology embodied in the product
 * that is described in this document. In particular, and without limitation, these intellectual property
 * rights may include one or more of the U.S. patents listed at http://www.sun.com/patents and one or
 * more additional patents or pending patent applications in the U.S. and in other countries.
 *
 * U.S. Government Rights - Commercial software. Government users are subject to the Sun
 * Microsystems, Inc. standard license agreement and applicable provisions of the FAR and its
 * supplements.
 *
 * Use is subject to license terms. Sun, Sun Microsystems, the Sun logo, Java and Solaris are trademarks or
 * registered trademarks of Sun Microsystems, Inc. in the U.S. and other countries. All SPARC trademarks
 * are used under license and are trademarks or registered trademarks of SPARC International, Inc. in the
 * U.S. and other countries.
 *
 * UNIX is a registered trademark in the U.S. and other countries, exclusively licensed through X/Open
 * Company, Ltd.
 */
package com.sun.max.tele.debug.guestvm.dbchannel.agent;

import java.io.*;
import java.net.*;
import com.sun.max.program.*;
import com.sun.max.tele.debug.guestvm.dbchannel.*;
import com.sun.max.tele.debug.guestvm.dbchannel.RIProtocolAdaptor.*;
import com.sun.max.tele.debug.guestvm.dbchannel.tcp.*;

/**
 * An agent that handles the dom0 side of the Maxine Inspector debug communication channel.
 *
 * @author Mick Jordan
 *
 */
public class ProtocolAgent {

    private static int port = TCPProtocol.DEFAULT_PORT;
    private static final String DBRING = "agent.AgentDB";
    private static final String XG = "agent.AgentXG";
    private static String impl = DBRING;
    private static int dbtLevel = 0;
    private static boolean oneShot = true;
    /**
     * @param args
     */
    public static void main(String[] args) {
        int traceLevel = 0;
        // Checkstyle: stop modified control variable check
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.equals("-port")) {
                port = Integer.parseInt(args[++i]);
            } else if (arg.equals("-impl")) {
                impl = args[++i];
            } else if (arg.equals("-trace")) {
                traceLevel = Integer.parseInt(args[++i]);
            } else if (arg.equals("-dbtlevel")) {
                dbtLevel = Integer.parseInt(args[++i]);
            } else  if (arg.equals("-xg")) {
                impl = XG;
            }
        }
        // Checkstyle: resume modified control variable check
        if (traceLevel > 0) {
            Trace.on(traceLevel);
        }
        System.loadLibrary("tele");
        if (impl.equals(DBRING)) {
            System.loadLibrary("guk_db");
        }
        listen();
    }

    public static void listen() {
        try {
            final ServerSocket server = new ServerSocket(port);
            for (;;) {
                try {
                    Trace.line(1, "waiting for connection");
                    final Socket sock = server.accept();
                    Trace.line(1, "connection accepted on " + sock.getLocalPort() + " from " + sock.getInetAddress());
                    final Handler handler = new Handler(sock);
                    handler.start();
                    // no concurrent connections, underlying native support cannot handle that at the moment
                    handler.join();
                    if (oneShot) {
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

    static class Handler extends Thread {
        private Socket socket;
        private DataInputStream in;
        private DataOutputStream out;
        private RIProtocolAdaptor protocol;

        Handler(Socket socket) throws Exception {
            this.socket = socket;
            try {
                in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            } catch (IOException ex) {
                close();
                throw ex;
            }
            final String protocolClassName = "com.sun.max.tele.debug.guestvm.dbchannel." + impl + "Protocol";
            protocol = (RIProtocolAdaptor) Class.forName(protocolClassName).newInstance();
            if (dbtLevel > 0) {
                ((Protocol) protocol).setTransportDebugLevel(dbtLevel);
            }
        }

        @Override
        public void run() {
            boolean terminated = false;
            while (!terminated) {
                try {
                    final String command = in.readUTF();
                    MethodInfo m = protocol.methodMap.get(command);
                    if (m == null) {
                        Trace.line(1, "command " + command + " not available");
                    } else {
                        final Object[] args = protocol.readArgs(in, m);
                        final Object result = m.method.invoke(protocol, args);
                        protocol.writeResult(out, m, result, args);
                        if (command.equals("resume") && ((Integer) result == 1)) {
                            terminated = true;
                        }
                    }
                } catch (Exception ex) {
                    System.err.println(ex);
                    ex.printStackTrace();
                    System.err.println("terminating connection");
                    terminated = true;
                }
            }
            close();
        }

        private void close() {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception ex) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (Exception ex) {
                }
            }
        }
    }

}
