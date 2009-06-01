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
package com.sun.guestvm.guk;

import com.sun.max.annotate.*;
import com.sun.max.unsafe.*;
import com.sun.max.vm.*;
import com.sun.max.vm.reference.*;

/**
 * Low-level access to the GUK tracing mechanism.
 * Unfortunately, we can't do the varargs thing (allocation is a no-no).
 * Format strings must be allocated at image build time to allow the
 * associated byte array pointers to be passed to the GKernel without GC concerns.
 * All scalar arguments are passed as longs.
 *
 * Values are cached here, so any changes made via back doors won't be observed.
 *
 * @author Mick Jordan
 *
 */

public class GUKTrace {

    /**
     * The order must match that in guk/trace.h.
     *
     */
    public enum  Name {
        SCHED, STARTUP, BLK, DB_BACK, FS_FRONT, GNTTAB, MM, MMPT, NET, SERVICE, SMP, XENBUS, TRAPS;
        boolean _value;
    }

    /**
     * The offset of the byte array data from the byte array object's origin.
     */
    private static final Offset _dataOffset = VMConfiguration.target().layoutScheme().byteArrayLayout().getElementOffsetFromOrigin(0);

    public static boolean setTraceState(Name name, boolean value) {
        final int previous = GUK.guk_set_trace_state(name.ordinal(), value ? 1 : 0);
        name._value = value;
        return previous != 0;
    }

    public static boolean getTraceState(Name name) {
        if (!_cached) {
            populateCache();
        }
        return name._value;
    }

    private static boolean _cached;
    private static void populateCache() {
        if (!_cached) {
            for (Name name : Name.values())  {
                final int state =  GUK.guk_get_trace_state(name.ordinal());
                name._value = state != 0;
            }
        }
    }

    @INLINE
    private static Pointer toPointer(byte[] fmt) {
        return Reference.fromJava(fmt).toOrigin().plus(_dataOffset);
    }

    @INLINE
    public static void print(byte[] fmt) {
        GUK.guk_ttprintk0(toPointer(fmt));
    }

    @INLINE
    public static void print1(byte[] fmt, long arg) {
        GUK.guk_ttprintk1(toPointer(fmt), arg);
    }

    @INLINE
    public static void print2(byte[] fmt, long arg1, long arg2) {
        GUK.guk_ttprintk2(toPointer(fmt), arg1, arg2);
    }

    @INLINE
    public static void print3(byte[] fmt, long arg1, long arg2, long arg3) {
        GUK.guk_ttprintk3(toPointer(fmt), arg1, arg2, arg3);
    }

    @INLINE
    public static void print4(byte[] fmt, long arg1, long arg2, long arg3, long arg4) {
        GUK.guk_ttprintk4(toPointer(fmt), arg1, arg2, arg3, arg4);
    }

    @INLINE
    public static void print5(byte[] fmt, long arg1, long arg2, long arg3, long arg4, long arg5) {
        GUK.guk_ttprintk5(toPointer(fmt), arg1, arg2, arg3, arg4, arg5);
    }

    public static final byte[] TEST_TRACE = "USER_TT".getBytes();

}
