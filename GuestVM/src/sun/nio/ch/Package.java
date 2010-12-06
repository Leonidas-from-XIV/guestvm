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
package sun.nio.ch;

import com.sun.max.config.BootImagePackage;
import com.sun.max.vm.hosted.*;

/**
 * NOTE: The handling of {@code sun.nio.ch} is convoluted but necessary owing to its curious structure and our desired
 * to change its native API.
 * 
 * There are two issues with {@code sun.nio.ch}. The first is that we need to change the {@link NativeDispatcher}
 * interface to use {@link ByteBuffer}. This requires the use of SUBSTITUTE but it also requires that the substitution
 * class be "in" sun.nio.ch because {@link NativeDispatcher} is not public.  The second is that package contains
 * implementations for multiple operating systems, that use native code, that obviously cannot all load on any
 * given system. To handle 1 we make {@code sun.nio.ch} an {@link ExtPackage}, even though it is part of the JDK
 * and to handle 2, we explicitly load just the classes we need.
 * 
 * @author Mick Jordan
 */

public class Package extends BootImagePackage {
    private static final String[] classes = {
        "sun.nio.ch.IOUtil", "sun.nio.ch.Util", "sun.nio.ch.FileKey", "sun.nio.ch.IOStatus", "sun.nio.ch.BBNativeDispatcher", 
        "sun.nio.ch.DatagramChannelImpl", "sun.nio.ch.ServerSocketChannelImpl", "sun.nio.ch.SocketChannelImpl",
        "sun.nio.ch.SinkChannelImpl", "sun.nio.ch.SourceChannelImpl", "sun.nio.ch.FileChannelImpl", 
        "sun.nio.ch.GuestVMNativePollArrayWrapper", 
        "sun.nio.ch.GuestVMNativePollArrayWrapper$PollOut", "sun.nio.ch.GuestVMNativePollArrayWrapper$PollThread",
        "sun.nio.ch.JDK_sun_nio_ch_IOUtil"
    };
    
    public Package() {
        super(classes);
        final String[] args = {"Datagram", "ServerSocket", "Socket", "Sink", "Source", "File"};
        for (String arg : args) {
            Extensions.resetField("sun.nio.ch." + arg + "ChannelImpl", "nd");
        }
    }

    @Override
    public boolean containsMethodSubstitutions() {
        return true;
    }
}
