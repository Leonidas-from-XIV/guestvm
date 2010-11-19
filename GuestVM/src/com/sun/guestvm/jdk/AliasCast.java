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

import static com.sun.cri.bytecode.Bytecodes.*;

import com.sun.cri.bytecode.INTRINSIC;


/**
 * Collect all the unsafe casts used by clients of {@link ALIAS} methods in the JDK substitution classes.
 * 
 * @author Mick Jordan
 *
 */
final class AliasCast {
    @INTRINSIC(UNSAFE_CAST) static native JDK_java_io_FileDescriptor asJDK_java_io_FileDescriptor(Object obj);    
    @INTRINSIC(UNSAFE_CAST) static native JDK_java_io_FileInputStream asJDK_java_io_FileInputStream(Object obj);
    @INTRINSIC(UNSAFE_CAST) static native JDK_java_io_FileOutputStream asJDK_java_io_FileOutputStream(Object obj);    
    @INTRINSIC(UNSAFE_CAST) static native JDK_java_io_RandomAccessFile asJDK_java_io_RandomAccessFile(Object obj);    
    @INTRINSIC(UNSAFE_CAST) static native JDK_java_net_PlainDatagramSocketImpl asJDK_java_net_PlainDatagramSocketImpl(Object obj);    
    @INTRINSIC(UNSAFE_CAST) static native JDK_java_net_PlainSocketImpl asJDK_java_net_PlainSocketImpl(Object obj);    
    @INTRINSIC(UNSAFE_CAST) static native JDK_java_net_Inet4AddressImpl asJDK_java_net_Inet4AddressImpl(Object obj);
    @INTRINSIC(UNSAFE_CAST) static native JDK_java_net_NetworkInterface asJDK_java_net_NetworkInterface(Object obj);
    @INTRINSIC(UNSAFE_CAST) static native JDK_sun_nio_ch_FileChannelImpl asJDK_sun_nio_ch_FileChannelImpl(Object obj);
    @INTRINSIC(UNSAFE_CAST) static native JDK_sun_nio_ch_FileKey asJDK_sun_nio_ch_FileKey(Object obj);

}
