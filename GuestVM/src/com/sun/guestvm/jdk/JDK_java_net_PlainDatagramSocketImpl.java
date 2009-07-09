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
package com.sun.guestvm.jdk;

import java.io.*;
import java.net.*;
import com.sun.max.annotate.*;
import com.sun.max.collect.*;
import com.sun.max.program.ProgramError;
import com.sun.max.vm.actor.holder.ClassActor;
import com.sun.max.vm.actor.member.*;
import com.sun.max.vm.object.*;
import com.sun.max.vm.classfile.constant.SymbolTable;
import com.sun.guestvm.net.ip.IPAddress;
import com.sun.guestvm.net.udp.*;

/**
 * This class implements the native methods in @see java.net.PlainDatagramSocketImpl in terms
 * of com.sun.guestvm.net classes. PlainDatagramSocketImpl assumes the use of file descriptors
 * with a socket represented with an int as per standard Unix. We have to emulate that even though
 * we represent sockets using UDPEndpoint objects.
 *
 * @author Mick Jordan
 *
 */

@SuppressWarnings("unused")

@METHOD_SUBSTITUTIONS(hiddenClass = "java.net.PlainDatagramSocketImpl")
public class JDK_java_net_PlainDatagramSocketImpl {

    private static VariableSequence<UDPEndpoint> _endpoints = new ArrayListSequence<UDPEndpoint>(16);

    private static int getFreeIndex(UDPEndpoint u) {
        synchronized (_endpoints) {
            final int length = _endpoints.length();
            for (int i = 0; i < length; i++) {
                if (_endpoints.get(i) == null) {
                    _endpoints.set(i, u);
                    return i;
                }
            }
            _endpoints.append(u);
            return length;
        }
    }

    private static Object checkOpen(Object self) throws SocketException {
        final Object fdObj = TupleAccess.readObject(self, _fileDescriptorFieldActor.offset());
        if (fdObj == null) {
            throw new SocketException("socket closed");
        }
        return fdObj;
    }

    @SUBSTITUTE
    private synchronized void bind0(int lport, InetAddress laddr) throws SocketException {
        final Object fdObj = checkOpen(this);
        final UDPEndpoint endpoint = _endpoints.get(TupleAccess.readInt(fdObj, _fdFieldActor.offset()));
        // TODO fix bind's first argument
        endpoint.bind(0, lport, false);
    }

    @SUBSTITUTE
    private void send(DatagramPacket p) throws IOException {
        final Object fdObj = checkOpen(this);
        final UDPEndpoint endpoint = _endpoints.get(TupleAccess.readInt(fdObj, _fdFieldActor.offset()));
        final byte[] buf = p.getData();
        final int off = p.getOffset();
        final int len = p.getLength();
        int port;
        int addr;
        if (TupleAccess.readBoolean(this, _connectedFieldActor.offset())) {
            port = endpoint.getRemotePort();
            addr = endpoint.getRemoteAddress();
        } else {
            port = p.getPort();
            addr = IPAddress.byteToInt(p.getAddress().getAddress());
        }
        endpoint.write(addr, port, buf, off, len, 0);
    }

    @SUBSTITUTE
    private synchronized int peek(InetAddress i) throws IOException {
        ProgramError.unexpected("peek not implemented");
        return 0;
    }

    @SUBSTITUTE
    private synchronized int peekData(DatagramPacket p) throws IOException {
        ProgramError.unexpected("peekData not implemented");
        return 0;
    }

    @SUBSTITUTE
    private synchronized void receive0(DatagramPacket p) throws IOException {
        final Object fdObj = checkOpen(this);
        final UDPEndpoint endpoint = _endpoints.get(TupleAccess.readInt(fdObj, _fdFieldActor.offset()));
        final byte[] buf = p.getData();
        final int off = p.getOffset();
        final int len = p.getLength();
        final int timeout = TupleAccess.readInt(this, _timeoutFieldActor.offset());
        if (timeout > 0) {
            endpoint.setTimeout(timeout);
        }
        final UDPEndpoint.Source source = new UDPEndpoint.Source();
        final int n = endpoint.read(buf, off, len, source);
        InetAddress inetAddress = p.getAddress();
        if (inetAddress != null) {
            if (source.addr != IPAddress.byteToInt(inetAddress.getAddress())) {
                inetAddress = null;
            }
        }
        if (inetAddress == null) {
            p.setAddress(JDK_java_net_Inet4AddressImpl.createInet4Address(null, source.addr));
        }
        p.setPort(source.port);
        p.setLength(n);
    }

    @SUBSTITUTE
    private void setTimeToLive(int ttl) throws IOException {
        ProgramError.unexpected("setTimeToLive not implemented");
    }

    @SUBSTITUTE
    private int getTimeToLive() throws IOException {
        ProgramError.unexpected("getTimeToLive not implemented");
        return 0;
    }

    @SUBSTITUTE
    private void setTTL(byte ttl) throws IOException {
        ProgramError.unexpected("setTTL not implemented");
    }

    @SUBSTITUTE
    private byte getTTL() throws IOException {
        ProgramError.unexpected("getTTL not implemented");
        return 0;
    }

    @SUBSTITUTE
    private void join(InetAddress inetaddr, NetworkInterface netIf) throws IOException {
        ProgramError.unexpected("join not implemented");
    }

    @SUBSTITUTE
    private void leave(InetAddress inetaddr, NetworkInterface netIf) throws IOException {
        ProgramError.unexpected("leave not implemented");
    }

    @SUBSTITUTE
    private void datagramSocketCreate() throws SocketException {
        final Object fdObj = checkOpen(this);
        final int fd = getFreeIndex(new UDPEndpoint());
        TupleAccess.writeInt(fdObj, _fdFieldActor.offset(), fd);
    }

    @SUBSTITUTE
    private void datagramSocketClose() {
        final Object fdObj = TupleAccess.readObject(this, _fileDescriptorFieldActor.offset());
        if (fdObj != null) {
            final int fd = TupleAccess.readInt(fdObj, _fdFieldActor.offset());
            if (fd != -1) {
                _endpoints.set(fd, null);
                TupleAccess.writeInt(fdObj, _fdFieldActor.offset(),  -1);
            }
        }
    }

    @SUBSTITUTE
    private void socketSetOption(int opt, Object val) throws SocketException {
        ProgramError.unexpected("socketSetOption not implemented");
    }

    @SUBSTITUTE
    private Object socketGetOption(int opt) throws SocketException {
        ProgramError.unexpected("socketGetOption not implemented");
        return null;
    }

    @SUBSTITUTE
    private void connect0(InetAddress address, int port) throws SocketException {
        final Object fdObj = checkOpen(this);
        final UDPEndpoint endpoint = _endpoints.get(TupleAccess.readInt(fdObj, _fdFieldActor.offset()));
        endpoint.connect(IPAddress.byteToInt(address.getAddress()), port);
        TupleAccess.writeBoolean(this, _connectedFieldActor.offset(), true);
    }

    @SUBSTITUTE
    private void disconnect0(int family) {
        ProgramError.unexpected("disconnect0 not implemented");
    }

    @SUBSTITUTE
    private static void init() {
        try {
            final Class<?> klass = Class.forName("java.net.PlainDatagramSocketImpl");
            final ClassActor classActor = ClassActor.fromJava(klass);
            _fileDescriptorFieldActor = JDK_java_io_FileDescriptor.fileDescriptorFieldActor(klass);
            _fdFieldActor = JDK_java_io_FileDescriptor.fdFieldActor();
            _connectedFieldActor = (FieldActor) classActor.findFieldActor(SymbolTable.makeSymbol("connected"));
            _timeoutFieldActor = (FieldActor) classActor.findFieldActor(SymbolTable.makeSymbol("timeout"));
        } catch (ClassNotFoundException ex) {
            ProgramError.unexpected("JDK_java_net_PlainDatagramSocketImpl: failed to load substitutee class");
        }
    }

    @CONSTANT_WHEN_NOT_ZERO
    private static FieldActor _fileDescriptorFieldActor;
    private static FieldActor _fdFieldActor;
    private static FieldActor _connectedFieldActor;
    private static FieldActor _timeoutFieldActor;

}
