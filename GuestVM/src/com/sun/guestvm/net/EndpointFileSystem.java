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

import com.sun.guestvm.fs.*;
import com.sun.guestvm.jdk.JDK_java_net_util;

/**
 * Not really a file system, but ensures that network endpoints are mapped to file descriptors that
 * follow the conventions in VirtualFileSystemId. This became a requirement when implementing
 * parts of the java.nio package, which is heavily dependent on file descriptors and invokes
 * generic calls that require the underlying "file system" to be determinable.
 *
 * @author Mick Jordan
 *
 */

public class EndpointFileSystem extends UnimplementedFileSystemImpl implements VirtualFileSystem {
    private static EndpointFileSystem _singleton;

    public static EndpointFileSystem create() {
        if (_singleton == null) {
            _singleton = new EndpointFileSystem();
        }
        return _singleton;
    }

    @Override
    public void configureBlocking(int fd, boolean blocking) {
        final Endpoint endpoint = JDK_java_net_util.getFromVfsId(fd);
        endpoint.configureBlocking(blocking);
    }

    @Override
    public int poll0(int fd, int eventOps, long timeout) {
        final Endpoint endpoint = JDK_java_net_util.getFromVfsId(fd);
        return endpoint.poll(eventOps, timeout);
    }
}
