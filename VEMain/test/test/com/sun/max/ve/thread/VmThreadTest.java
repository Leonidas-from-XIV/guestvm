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
package test.com.sun.max.ve.thread;

import com.sun.max.ve.sched.*;
import com.sun.max.ve.test.VmThreadTestHelper;

/**
 * A class to test the various ways to get the current VmThread and the current native thread.
 *
 * @author Mick Jordan
 *
 */

public class VmThreadTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        long count = 1000000;
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.equals("c")) {
                count = Long.parseLong(args[++i]);
            }
        }

        System.out.println("current: " + VmThreadTestHelper.current());
        System.out.println("currentAsAddress: " + VmThreadTestHelper.currentAsAddress());
        System.out.println("idLocal: " + VmThreadTestHelper.idLocal());
        System.out.println("idCurrent: " + VmThreadTestHelper.idCurrent());
        System.out.println("nativeCurrent: " + Long.toHexString(VmThreadTestHelper.nativeCurrent()));
        System.out.println("nativeUKernel: " + Long.toHexString(VmThreadTestHelper.nativeUKernel()));

        System.out.println("timed current: " + new CurrentProcedure().timedRun(count) + "ns");
        System.out.println("timed nativeCurrent: " + new NativeCurrentProcedure().timedRun(count) + "ns");
        System.out.println("timed nativeUKernel: " + new NativeUKernelProcedure().timedRun(count) + "ns");
    }

    abstract static class Procedure {
        long timedRun(long count) {
            final long start = System.nanoTime();
            for (long i = 0; i < count; i++) {
                run();
            }
            return System.nanoTime() - start;
        }
        protected abstract void run();
    }

    static class CurrentProcedure extends Procedure {
        protected void run() {
            VmThreadTestHelper.current();
        }
    }

     static class NativeCurrentProcedure extends Procedure {
        protected void run() {
            VmThreadTestHelper.nativeCurrent();
        }
    }

    static class NativeUKernelProcedure extends Procedure {
        protected void run() {
            VmThreadTestHelper.nativeUKernel();
        }
    }
}
