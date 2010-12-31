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
package com.sun.max.ve.jdk;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.List;

import com.sun.max.ve.fs.VirtualFileSystemId;
import com.sun.max.ve.net.Endpoint;
import com.sun.max.ve.net.EndpointFileSystem;
import com.sun.max.ve.net.tcp.TCPEndpoint;
import com.sun.max.ve.net.udp.UDPEndpoint;

/**
 * Utility class to support network class substitutions.
 * In particular this class generates file descriptors for the network
 * classes and ensure that they are associated with the EndpointFileSystem.
 *
 * @author Mick Jordan
 *
 */


public class JavaNetUtil {

    private static List<Endpoint> _endpoints = new ArrayList<Endpoint>(16);
    private static EndpointFileSystem _endpointFileSystem;

    /**
     * Return a file descriptor id to be associated with the given endpoint.
     * @param u
     * @return
     */
    static int getFreeIndex(Endpoint u) {
        int result;
        synchronized (_endpoints) {
            final int length = _endpoints.size();
            for (int i = 0; i < length; i++) {
                if (_endpoints.get(i) == null) {
                    _endpoints.set(i, u);
                    result = i;
                    break;
                }
            }
            _endpoints.add(u);
            result = length;
        }
        return getUniqueFd(result);
    }

    private static int getUniqueFd(int fd) {
        if (_endpointFileSystem == null) {
            _endpointFileSystem = EndpointFileSystem.create();
        }
        return VirtualFileSystemId.getUniqueFd(_endpointFileSystem, fd);
    }

    static UDPEndpoint getU(int index) {
        return (UDPEndpoint) getFromVfsId(index);
    }

    static TCPEndpoint getT(int index) {
        return (TCPEndpoint) getFromVfsId(index);
    }

    static Endpoint get(FileDescriptor fdObj) {
        return getFromVfsId(JDK_java_io_FileDescriptor.getFd(fdObj));
    }

    public static Endpoint getFromVfsId(int index) {
        return _endpoints.get(VirtualFileSystemId.getFd(index));
    }

    static void set(int index, Endpoint endpoint) {
        _endpoints.set(VirtualFileSystemId.getFd(index), endpoint);
    }

    static void setNull(int index) {
        set(index, null);
    }

}
