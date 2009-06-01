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
import com.sun.max.annotate.*;
import com.sun.max.program.*;

/**
 * Substitutions for  @see sun.nio.ch.IOUtil.
 * @author Mick Jordan
 *
 */

@SuppressWarnings("unused")

@METHOD_SUBSTITUTIONS(hiddenClass = "sun.nio.ch.IOUtil")
public class JDK_sun_nio_ch_IOUtil {
    @SUBSTITUTE
    private static void initIDs() {

    }

    @SUBSTITUTE
    static boolean randomBytes(byte[] someBytes) {
        Problem.unimplemented("sun.nio.ch.IOUtil.randomBytes");
        return false;
    }

    @SUBSTITUTE
    static void initPipe(int[] fda, boolean blocking) {
        Problem.unimplemented("sun.nio.ch.IOUtil.initPipe");
    }

    @SUBSTITUTE
    static boolean drain(int fd) throws IOException {
        Problem.unimplemented("sun.nio.ch.IOUtil.drain");
        return false;
    }

    @SUBSTITUTE
    static void configureBlocking(FileDescriptor fd, boolean blocking) throws IOException {
        Problem.unimplemented("sun.nio.ch.IOUtil.configureBlocking");
    }

    @SUBSTITUTE
    static int fdVal(FileDescriptor fd) {
        Problem.unimplemented("sun.nio.ch.IOUtil.fdVal");
        return -1;
    }

    @SUBSTITUTE
    static void setfdVal(FileDescriptor fd, int value) {
        Problem.unimplemented("sun.nio.ch.IOUtil.setfdVal");
    }

}
