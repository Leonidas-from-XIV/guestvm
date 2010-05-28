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
package test.com.sun.guestvm.sched;

import com.sun.guestvm.sched.*;
import com.sun.guestvm.test.*;
import com.sun.max.vm.thread.*;
import com.sun.max.lang.*;

import test.util.*;

/**
 * Test the functionality of the {@link GukVMThread} interface.
 *
 * @author Mick Jordan
 *
 */
public class GUKVmThreadTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        final ArgsHandler h = ArgsHandler.process(args);
        if (h._opCount == 0) {
            System.out.println("no operations given");
            return;
        }
        for (int j = 0; j < h._opCount; j++) {
            final String opArg1 = h._opArgs1[j];
            final String opArg2 = h._opArgs2[j];
            final String op = h._ops[j];

            try {
                final GUKVmThread vmThread = (GUKVmThread) VmThreadTestHelper.current();
                if (op.equals("runningTime")) {
                    System.out.println("current thread running time is " + vmThread.getRunningTime());
                } else if (op.equals("stack")) {

                } else if (op.equals("list")) {
                    VmThreadMap.ACTIVE.forAllThreads(null, new Lister());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

    static class Lister implements Procedure<VmThread> {
        public void run(VmThread thread) {
            final GUKVmThread t = (GUKVmThread) thread;
            System.out.println("id " + t.id() + ", cpu " + t.getCpu());
        }
    }

}
