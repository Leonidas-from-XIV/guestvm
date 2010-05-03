/*******************************************************************************
 * openthinclient.org ThinClient suite
 * 
 * Copyright (C) 2004, 2007 levigo holding GmbH. All Rights Reserved.
 * 
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 *******************************************************************************/
/*
 * Automatically generated by jrpcgen 1.0.5 on 04.05.05 18:15 jrpcgen is part of
 * the "Remote Tea" ONC/RPC package for Java See
 * http://remotetea.sourceforge.net for details
 */
package org.openthinclient.mountd.tea;

import java.io.IOException;
import java.net.InetAddress;

import org.acplt.oncrpc.OncRpcClient;
import org.acplt.oncrpc.OncRpcClientStub;
import org.acplt.oncrpc.OncRpcException;
import org.acplt.oncrpc.XdrVoid;

/**
 * The class <code>mountClient</code> implements the client stub proxy for the
 * MOUNTPROG remote program. It provides method stubs which, when called, in
 * turn call the appropriate remote method (procedure).
 */
public class mountClient extends OncRpcClientStub {

    /**
     * Constructs a <code>mountClient</code> client stub proxy object from which
     * the MOUNTPROG remote program can be accessed.
     * 
     * @param host
     *            Internet address of host where to contact the remote program.
     * @param protocol
     *            {@link org.acplt.oncrpc.OncRpcProtocols Protocol} to be used
     *            for ONC/RPC calls.
     * @throws OncRpcException
     *             if an ONC/RPC error occurs.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public mountClient(InetAddress host, int protocol) throws OncRpcException,
                                                      IOException {
        super(host, mount.MOUNTPROG, 1, 0, protocol);
    }

    /**
     * Constructs a <code>mountClient</code> client stub proxy object from which
     * the MOUNTPROG remote program can be accessed.
     * 
     * @param host
     *            Internet address of host where to contact the remote program.
     * @param port
     *            Port number at host where the remote program can be reached.
     * @param protocol
     *            {@link org.acplt.oncrpc.OncRpcProtocols Protocol} to be used
     *            for ONC/RPC calls.
     * @throws OncRpcException
     *             if an ONC/RPC error occurs.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public mountClient(InetAddress host, int port, int protocol)
                                                                throws OncRpcException,
                                                                IOException {
        super(host, mount.MOUNTPROG, 1, port, protocol);
    }

    /**
     * Constructs a <code>mountClient</code> client stub proxy object from which
     * the MOUNTPROG remote program can be accessed.
     * 
     * @param host
     *            Internet address of host where to contact the remote program.
     * @param program
     *            Remote program number.
     * @param version
     *            Remote program version number.
     * @param protocol
     *            {@link org.acplt.oncrpc.OncRpcProtocols Protocol} to be used
     *            for ONC/RPC calls.
     * @throws OncRpcException
     *             if an ONC/RPC error occurs.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public mountClient(InetAddress host, int program, int version, int protocol)
                                                                                throws OncRpcException,
                                                                                IOException {
        super(host, program, version, 0, protocol);
    }

    /**
     * Constructs a <code>mountClient</code> client stub proxy object from which
     * the MOUNTPROG remote program can be accessed.
     * 
     * @param host
     *            Internet address of host where to contact the remote program.
     * @param program
     *            Remote program number.
     * @param version
     *            Remote program version number.
     * @param port
     *            Port number at host where the remote program can be reached.
     * @param protocol
     *            {@link org.acplt.oncrpc.OncRpcProtocols Protocol} to be used
     *            for ONC/RPC calls.
     * @throws OncRpcException
     *             if an ONC/RPC error occurs.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public mountClient(InetAddress host, int program, int version, int port,
                       int protocol) throws OncRpcException, IOException {
        super(host, program, version, port, protocol);
    }

    /**
     * Constructs a <code>mountClient</code> client stub proxy object from which
     * the MOUNTPROG remote program can be accessed.
     * 
     * @param client
     *            ONC/RPC client connection object implementing a particular
     *            protocol.
     * @throws OncRpcException
     *             if an ONC/RPC error occurs.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public mountClient(OncRpcClient client) throws OncRpcException, IOException {
        super(client);
    }

    /**
     * Call remote procedure MOUNTPROC_DUMP_1.
     * 
     * @return Result from remote procedure call (of type mountlist).
     * @throws OncRpcException
     *             if an ONC/RPC error occurs.
     */
    public mountlist MOUNTPROC_DUMP_1() throws OncRpcException {
        XdrVoid args$ = XdrVoid.XDR_VOID;
        mountlist result$ = new mountlist();
        client.call(mount.MOUNTPROC_DUMP_1, mount.MOUNTVERS, args$, result$);
        return result$;
    }

    /**
     * Call remote procedure MOUNTPROC_EXPORT_1.
     * 
     * @return Result from remote procedure call (of type exports).
     * @throws OncRpcException
     *             if an ONC/RPC error occurs.
     */
    public exports MOUNTPROC_EXPORT_1() throws OncRpcException {
        XdrVoid args$ = XdrVoid.XDR_VOID;
        exports result$ = new exports();
        client.call(mount.MOUNTPROC_EXPORT_1, mount.MOUNTVERS, args$, result$);
        return result$;
    }

    /**
     * Call remote procedure MOUNTPROC_EXPORTALL_1.
     * 
     * @return Result from remote procedure call (of type exports).
     * @throws OncRpcException
     *             if an ONC/RPC error occurs.
     */
    public exports MOUNTPROC_EXPORTALL_1() throws OncRpcException {
        XdrVoid args$ = XdrVoid.XDR_VOID;
        exports result$ = new exports();
        client.call(mount.MOUNTPROC_EXPORTALL_1, mount.MOUNTVERS, args$,
                    result$);
        return result$;
    }

    /**
     * Call remote procedure MOUNTPROC_MNT_1.
     * 
     * @param arg1
     *            parameter (of type dirpath) to the remote procedure call.
     * @return Result from remote procedure call (of type fhstatus).
     * @throws OncRpcException
     *             if an ONC/RPC error occurs.
     */
    public fhstatus MOUNTPROC_MNT_1(dirpath arg1) throws OncRpcException {
        fhstatus result$ = new fhstatus();
        client.call(mount.MOUNTPROC_MNT_1, mount.MOUNTVERS, arg1, result$);
        return result$;
    }

    /**
     * Call remote procedure MOUNTPROC_NULL_1.
     * 
     * @throws OncRpcException
     *             if an ONC/RPC error occurs.
     */
    public void MOUNTPROC_NULL_1() throws OncRpcException {
        XdrVoid args$ = XdrVoid.XDR_VOID;
        XdrVoid result$ = XdrVoid.XDR_VOID;
        client.call(mount.MOUNTPROC_NULL_1, mount.MOUNTVERS, args$, result$);
    }

    /**
     * Call remote procedure MOUNTPROC_UMNT_1.
     * 
     * @param arg1
     *            parameter (of type dirpath) to the remote procedure call.
     * @throws OncRpcException
     *             if an ONC/RPC error occurs.
     */
    public void MOUNTPROC_UMNT_1(dirpath arg1) throws OncRpcException {
        XdrVoid result$ = XdrVoid.XDR_VOID;
        client.call(mount.MOUNTPROC_UMNT_1, mount.MOUNTVERS, arg1, result$);
    }

    /**
     * Call remote procedure MOUNTPROC_UMNTALL_1.
     * 
     * @throws OncRpcException
     *             if an ONC/RPC error occurs.
     */
    public void MOUNTPROC_UMNTALL_1() throws OncRpcException {
        XdrVoid args$ = XdrVoid.XDR_VOID;
        XdrVoid result$ = XdrVoid.XDR_VOID;
        client.call(mount.MOUNTPROC_UMNTALL_1, mount.MOUNTVERS, args$, result$);
    }

}
// End of mountClient.java
