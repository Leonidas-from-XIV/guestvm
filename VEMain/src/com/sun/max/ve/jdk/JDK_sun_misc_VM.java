/*
 * Copyright (c) 2010 Sun Microsystems, Inc., 4150 Network Circle, Santa
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

import com.sun.max.annotate.*;
import com.sun.max.vm.jni.JVMFunctions;

/**
 * Substitutions for the @see sun.misc.VM class.
 *
 * @author Mick Jordan
 *
 */

@SuppressWarnings("unused")

@METHOD_SUBSTITUTIONS(sun.misc.VM.class)
final class JDK_sun_misc_VM {
    @SUBSTITUTE
    private static void getThreadStateValues(int[][] vmThreadStateValues,
                    String[][] vmThreadStateNames) {
        /*
         * As I understand the purpose of this method, it is to allow for a finer grain
         * set of thread status values than provided by Thread.State and to map those
         * fine grain values to Thread.State values. E.g., there could be several reasons for
         * WAITING, each with their own integer thread status, all of which would be mapped
         * to (the ordinal value of) WAITING.
         *
         * Currently, our map is 1-1.
         */
        final Thread.State[] ts = Thread.State.values();
        assert ts.length == vmThreadStateValues.length;
        for (int i = 0; i < vmThreadStateValues.length; i++) {
            vmThreadStateValues[i] = JVMFunctions.GetThreadStateValues(i);
            vmThreadStateNames[i] = JVMFunctions.GetThreadStateNames(i, vmThreadStateValues[i]);
        }
    }
}
