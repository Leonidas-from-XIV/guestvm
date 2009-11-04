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
package com.sun.guestvm.net.tcp;
//
// TCPEndpoint.java
//
// This class implements the glue between the JDK Socket API and
// the TCP protocol state machine code.
//
// sritchie -- Nov 95
//
//
// notes
//

import java.net.*;
import java.io.*;
import java.nio.*;

import com.sun.guestvm.fs.ErrorDecoder;
import com.sun.guestvm.fs.VirtualFileSystem;
import com.sun.guestvm.net.*;
import com.sun.guestvm.util.*;


public class TCPEndpoint implements Endpoint {

    TCP tcp;
    int timeout;        // used for timing out reads and accept (in millisecs)

    public TCPEndpoint() throws IOException {
        tcp = TCP.get();

        if (tcp == null) {
            throw new SocketException("no more TCP sockets");
        }
    }

    TCPEndpoint(TCP t) {
        tcp = t;
    }

    // Bind a port number to this unused local endpoint. An endpoint
    // can't be bound twice. Passing in 0 chooses the next available port.
    // The addr argument is currently ignored, the local address is always
    // used.
    // The reuse argument is ignored. No matter what we do not allow
    // binding to the same port.
    // The bound port number is returned.
    public int bind(int addr, int port, boolean reuse) throws IOException {
        // can't bind() an endpoint twice.
        if (tcp._localPort != 0) {
            throw new BindException("port in use");
        }

        port = tcp.setLocalPort(port);

        if (port == 0) {
            throw new BindException("port in use");
        }
        return port;
    }

    public void listen(int count) throws IOException {
        boolean r = false;

        r = tcp.listen(count);
        if (r != true) {
            throw new SocketException(" can't listen()");
        }
    }


    // Wait for a new connection to arrive on this Endpoint.
    public Endpoint accept() throws IOException{

        TCP t = null;
        try {
            t = tcp.waitForConnection(timeout);
        } catch (InterruptedException ex) {
            throw new InterruptedIOException(ex.getMessage());
        }
        TCPEndpoint endp = new TCPEndpoint(t);

        return (Endpoint) endp;
    }

    public int connect(int addr, int p) throws IOException {
        try {
            int result = tcp.connect(addr, p);
            if (result == TCP.CONN_FAIL_REFUSED) {
                throw new ConnectException("Connection refused");
            }
            if (result == TCP.CONN_FAIL_TIMEOUT) {
                throw new NoRouteToHostException("Connect timed out");
            }
            p = tcp._localPort;
        } catch (InterruptedException ex) {
            throw new InterruptedIOException(ex.getMessage());
        } catch (NetworkException e) {
            throw new SocketException(e.getMessage());
        }
        return p;
    }

    public void close() throws IOException {
        try {
            if (tcp != null) {
                tcp.close();
            }
        } catch (NetworkException e) {
            throw new SocketException(e.getMessage());
        } finally {
            tcp = null;
        }
    }

    public int write(byte buf[], int off, int len) throws IOException {
        if (len <= 0) {
            return 0;
        }
        try {
            return tcp.write(buf, off, len);
        } catch (InterruptedException ex) {
            throw new InterruptedIOException(ex.getMessage());
        } catch (NetworkException e) {
            throw new SocketException(e.getMessage());
        }
    }

    public int read(byte buf[], int off, int len) throws IOException {
        try {
            return tcp.read(buf, off, len, timeout);
        } catch (InterruptedException ex) {
            throw new InterruptedIOException(ex.getMessage());
        } catch (NetworkException e) {
            throw new SocketException(e.getMessage());
        }
    }

    public int read(ByteBuffer bb)  throws IOException {
        // temporary limitation
        assert bb.hasArray();
        return read(bb.array(), bb.arrayOffset(), bb.limit() - bb.position());
    }

    public int write(ByteBuffer bb)  throws IOException {
        // temporary limitation
        assert bb.hasArray();
        final int len = bb.limit() - bb.position();
        return write(bb.array(), bb.arrayOffset(), len);
    }

    public int available() {
        int n = 0;
        n = tcp.available();
        return n;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getRemoteAddress() {
        return tcp._remoteIp;
    }

    public int getRemotePort() {
        return tcp._remotePort;
    }
    public int getLocalAddress() {
        // return tcp._localIp;
        // TODO implement setting of local address
        return 0;
    }

    public int getLocalPort() {
        return tcp._localPort;
    }

    public void configureBlocking(boolean blocking) {
        tcp.configureBlocking(blocking);
    }

    public int poll(int eventOps, long timeout) {
        final boolean input = eventOps == VirtualFileSystem.POLLIN;
        if (input) {
            // tcp.pollInput handles the listen state and the established state
            if (tcp.pollInput()) {
                return VirtualFileSystem.POLLIN;
            }
        } else {
            if (tcp.pollOutput()) {
                return VirtualFileSystem.POLLOUT;
            }
        }
        if (timeout == 0) {
            return 0;
        }
        synchronized (tcp) {
            final TimeLimitedProc timedProc = new TimeLimitedProc() {

                protected int proc(long remaining) throws InterruptedException {
                    tcp.wait(remaining);
                    if (input) {
                        if (tcp.pollInput()) {
                            return terminate(VirtualFileSystem.POLLIN);
                        }
                    } else {
                        if (tcp.pollOutput()) {
                            return terminate(VirtualFileSystem.POLLOUT);
                        }
                    }
                    return 0;
                }
            };
            return timedProc.run(timeout);
        }
    }
}

