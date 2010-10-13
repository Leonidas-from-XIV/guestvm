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
import com.sun.max.vm.object.TupleAccess;
import com.sun.guestvm.error.*;
import com.sun.guestvm.fs.ErrorDecoder;
import com.sun.guestvm.net.Endpoint;

/**
 * Substitutions for native methods in sun.nio.ch.ServerSocketChannelImpl.
 *
 * @author Mick Jordan
 *
 */

@SuppressWarnings("unused")

@METHOD_SUBSTITUTIONS(className = "sun.nio.ch.ServerSocketChannelImpl")
final class JDK_sun_nio_ch_ServerSocketChannelImpl {
    @SUBSTITUTE
    private static void listen(FileDescriptor fdObj, int backlog) throws IOException {
        final Endpoint endpoint = JDK_java_net_util.get(fdObj);
        endpoint.listen(backlog);
    }

    @SUBSTITUTE
    private int accept0(FileDescriptor fdObj, FileDescriptor newfdObj, InetSocketAddress[] isaa) throws IOException {
        // this is the listen endpoint
        final Endpoint endpoint = JDK_java_net_util.get(fdObj);
        // this is the accepted endpoint
        final Endpoint acceptEndpoint = endpoint.accept();
        if (acceptEndpoint == null) {
            return -ErrorDecoder.Code.EAGAIN.getCode();
        }
        int newfd = JDK_java_net_util.getFreeIndex(acceptEndpoint);
        TupleAccess.writeInt(newfdObj, JDK_java_io_FileDescriptor.fdFieldActor().offset(), newfd);
        isaa[0] = new InetSocketAddress(JDK_java_net_Inet4AddressImpl.createInet4Address(null, acceptEndpoint.getRemoteAddress()), acceptEndpoint.getRemotePort());
        return 1;
    }

    @SUBSTITUTE
    private static void initIDs() {

    }
}
