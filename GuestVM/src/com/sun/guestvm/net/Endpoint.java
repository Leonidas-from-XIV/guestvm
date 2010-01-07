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
package com.sun.guestvm.net;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Endpoint.java
 *
 * This interface describes the methods common to TCP and UDP protocol
 * socket endpoints.  It is implemented by the protocol-specific
 * layers to provide the implementation (TcpEndpoint and UdpEndpoint).
 *
 */
public interface Endpoint {

    int connect(int addr, int port) throws IOException;

    int bind(int addr, int port, boolean reuse) throws IOException;

    void listen(int count) throws IOException;

    Endpoint accept() throws IOException;

    int SHUT_RD = 0;
    int SHUT_WR = 1;
    int SHUT_RDWR = 2;

    void close(int how) throws IOException;

    int getRemoteAddress();

    int getRemotePort();

    int getLocalAddress();

    int getLocalPort();

    int getRecvBufferSize();

    int getSendBufferSize();

    void setRecvBufferSize(int size);

    void setSendBufferSize(int size);

    int write(byte[] b, int off, int len) throws IOException;

    int read(byte[] b, int off, int len) throws IOException;

    int write(ByteBuffer bb) throws IOException;

    int read(ByteBuffer bb) throws IOException;

    int available() throws IOException;

    void setTimeout(int timeout);

    void configureBlocking(boolean blocking);

    int poll(int eventOps, long timeout);
}
