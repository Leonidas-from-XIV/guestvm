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
import static java.net.SocketOptions.*;

import com.sun.max.annotate.*;
import com.sun.max.collect.*;
import com.sun.max.vm.actor.holder.ClassActor;
import com.sun.max.vm.actor.member.*;
import com.sun.max.vm.object.*;
import com.sun.max.vm.classfile.constant.SymbolTable;
import com.sun.guestvm.error.*;
import com.sun.guestvm.net.ip.IPAddress;
import com.sun.guestvm.net.tcp.TCPEndpoint;
import com.sun.guestvm.logging.*;

/**
 * This class implements the native methods in @see java.net.PlainSocketImpl in terms
 * of com.sun.guestvm.net classes. PlainSocketImpl assumes the use of file descriptors
 * with a socket represented with an int as per standard Unix. We have to emulate that even though
 * we represent sockets using TCPEndpoint objects.
 *
 * N.B. The native methods are in the PlainSocketImpl class but the actual class at runtime is the subclass SocksSocketImpl
 *
 * @author Mick Jordan
 *
 */

@SuppressWarnings("unused")

@METHOD_SUBSTITUTIONS(hiddenClass = "java.net.PlainSocketImpl")
final class JDK_java_net_PlainSocketImpl {

    private static Logger _logger;

    private static FileDescriptor checkOpen(Object self) throws SocketException {
        final Object fdObj = TupleAccess.readObject(self, _fileDescriptorFieldActor.offset());
        if (fdObj == null) {
            throw new SocketException("socket closed");
        }
        return (FileDescriptor) fdObj;
    }

    private static TCPEndpoint getEndpoint(Object self) throws SocketException {
        final FileDescriptor fdObj = checkOpen(self);
        return (TCPEndpoint) JDK_java_net_util.get(fdObj);
    }

    /**
     * Return the TCPEndpoint associated with file descriptor argument.
     * This is for the benefit of JDK_java_net_SocketInput/OutputStream
     * @param fdObj
     */
    static TCPEndpoint getEndpoint(FileDescriptor fdObj) throws SocketException {
        final int fd = TupleAccess.readInt(fdObj, _fdFieldActor.offset());
        if (fd < 0) {
            throw new SocketException("socket closed");
        }
        return  JDK_java_net_util.getT(fd);

    }

    @SUBSTITUTE
    private  void socketCreate(boolean isServer) throws IOException {
        final Object fdObj = checkOpen(this);
        final int fd = JDK_java_net_util.getFreeIndex(new TCPEndpoint());
        TupleAccess.writeInt(fdObj, _fdFieldActor.offset(), fd);
    }

    @SUBSTITUTE
    private  void socketConnect(InetAddress address, int port, int timeout) throws IOException {
        final TCPEndpoint endpoint = getEndpoint(this);
        // CheckStyle: stop parameter assignment check
        port = endpoint.connect(IPAddress.byteToInt(address.getAddress()), port);
        // CheckStyle: resume parameter assignment check
        TupleAccess.writeInt(this, _localportFieldActor.offset(), port);
        // Odd that this does not happen in the caller
        TupleAccess.writeObject(this, _addressFieldActor.offset(), address);
    }

    @SUBSTITUTE
    private  void socketBind(InetAddress address, int port) throws IOException {
        final TCPEndpoint endpoint = getEndpoint(this);
        // CheckStyle: stop parameter assignment check
        port  = endpoint.bind(IPAddress.byteToInt(address.getAddress()), port, false);
        // CheckStyle: resume parameter assignment check
        TupleAccess.writeInt(this, _localportFieldActor.offset(), port);
        // Odd that this does not happen in the caller
        TupleAccess.writeObject(this, _addressFieldActor.offset(), address);
    }

    @SUBSTITUTE
    private  void socketListen(int count) throws IOException {
        final TCPEndpoint endpoint = getEndpoint(this);
        endpoint.listen(count);
    }

    @SUBSTITUTE
    private  void socketAccept(SocketImpl si) throws IOException {
        final TCPEndpoint endpoint = getEndpoint(this);
        final int timeout = TupleAccess.readInt(this, _timeoutFieldActor.offset());
        if (timeout != 0) {
            endpoint.setTimeout(timeout);
        }
        final int fd = JDK_java_net_util.getFreeIndex((TCPEndpoint) endpoint.accept());
        // set fd field in FileDescriptor in si
        TupleAccess.writeInt(TupleAccess.readObject(si, _fileDescriptorFieldActor.offset()), _fdFieldActor.offset(), fd);
        // copy address and localport fields from this to si
        TupleAccess.writeObject(si, _addressFieldActor.offset(), TupleAccess.readObject(this, _addressFieldActor.offset()));
        TupleAccess.writeInt(si, _localportFieldActor.offset(), TupleAccess.readInt(this, _localportFieldActor.offset()));
    }

    @SUBSTITUTE
    private  int socketAvailable() throws IOException {
        final TCPEndpoint endpoint = getEndpoint(this);
        return endpoint.available();
    }

    @SUBSTITUTE
    private  void socketClose0(boolean useDeferredClose) throws IOException {
        // TODO: figure out what should really be done about useDeferredClose
        final Object fdObj = checkOpen(this);
        final  int fd = TupleAccess.readInt(fdObj, _fdFieldActor.offset());
        if (fd != -1) {
            JDK_java_net_util.getT(fd).close();
            TupleAccess.writeInt(fdObj, _fdFieldActor.offset(), -1);
        }
    }
    @SUBSTITUTE
    private  void socketShutdown(int howto) throws IOException {
        final TCPEndpoint endpoint = getEndpoint(this);
        endpoint.close();
    }

    @SUBSTITUTE
    private  void socketSetOption(int cmd, boolean on, Object value) throws IOException {
        final TCPEndpoint endpoint = getEndpoint(this);
        switch (cmd) {
            case TCP_NODELAY:
                _logger.warning("TCP_NODELAY not implemented");
                break;

            case SO_TIMEOUT:
                // Hotspot native says this is a no-op for Solaris/Linux
                // TCPEndpoint has a timeout option which is used by SocketInputStream.socketRead0.
                // However our caller, PlainSocketImpl, also caches the timeout value and
                // passes this explicitly to socketRead0, so nothing needs to be done here.
                break;

            default:
                _logger.warning("setSocketOption " + Integer.toHexString(cmd) + " not implemented");
        }
    }

    @SUBSTITUTE
    private  int socketGetOption(int opt, Object iaContainerObj) throws SocketException {
        GuestVMError.unimplemented("PlainSocketImpl.socketGetOption");
        return 0;
    }

    @SUBSTITUTE
    private  int socketGetOption1(int opt, Object iaContainerObj, FileDescriptor fd) throws SocketException {
        GuestVMError.unimplemented("PlainSocketImpl.socketGetOption1");
        return 0;
    }

    @SUBSTITUTE
    private  void socketSendUrgentData(int data) throws IOException {
        GuestVMError.unimplemented("socketSendUrgentData");
    }

    @SUBSTITUTE
    private static void initProto() {
        try {
            final Class<?> klass = Class.forName("java.net.PlainSocketImpl");
            final ClassActor classActor = ClassActor.fromJava(klass);
            _fileDescriptorFieldActor = JDK_java_io_FileDescriptor.fileDescriptorFieldActor(klass);
            _fdFieldActor = JDK_java_io_FileDescriptor.fdFieldActor();
            _timeoutFieldActor = (FieldActor) classActor.findFieldActor(SymbolTable.makeSymbol("timeout"));
            _localportFieldActor = (FieldActor) classActor.findFieldActor(SymbolTable.makeSymbol("localport"));
            _addressFieldActor = (FieldActor) classActor.findFieldActor(SymbolTable.makeSymbol("address"));
            _logger = Logger.getLogger("JDK_java_net_PlainSocketImpl");
        } catch (ClassNotFoundException ex) {
            GuestVMError.unexpected("JDK_java_net_PlainSocketImpl: failed to load substitutee class");
        }
    }

    @CONSTANT_WHEN_NOT_ZERO
    private static FieldActor _fileDescriptorFieldActor;
    private static FieldActor _fdFieldActor;
    private static FieldActor _timeoutFieldActor;
    private static FieldActor _localportFieldActor;
    private static FieldActor _addressFieldActor;

}
