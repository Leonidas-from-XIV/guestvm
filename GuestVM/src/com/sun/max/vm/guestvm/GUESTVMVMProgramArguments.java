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
package com.sun.max.vm.guestvm;

import com.sun.max.annotate.*;
import com.sun.max.vm.MaxineVM;
import com.sun.max.vm.*;

/**
 * This class permits additional GuestVM-specific command line arguments.
 *
 *
 * @author Mick Jordan
 */

public final class GUESTVMVMProgramArguments {

    @SuppressWarnings({"unused"})
    private static final VMOption _debugOption = new VMOption("-XX:GUKDebug", "Enables Guest VM debug mode.", MaxineVM.Phase.PRISTINE);
    @SuppressWarnings({"unused"})
    private static final VMOption _xenTraceOption = new VMOption("-XX:GUKTrace", "Enables GuestVM microkernel tracing", MaxineVM.Phase.PRISTINE) {
        @Override
        public void printHelp() {
            VMOptions.printHelpForOption("-XX:GUKTrace[:subsys1:subsys2:...[:buffer][:toring]]", "", _help);
        }
    };

    private static final VMOption _upcallOption = new VMOption("-XX:GUKAS", "Enables the Guest VM Java thread scheduler", MaxineVM.Phase.PRISTINE);
    @SuppressWarnings({"unused"})
    private static final VMOption _timesliceOption = new VMIntOption("-XX:GUKTS=", 10, "Set Scheduler Time Slice (ms)", MaxineVM.Phase.PRISTINE);
    @SuppressWarnings({"unused"})
    private static final VMOption _traceCpuOption = new VMIntOption("-XX:GUKCT=", -1, "Reserves a CPU for tracing", MaxineVM.Phase.PRISTINE);

    private GUESTVMVMProgramArguments() {
    }

    @INLINE
    public static boolean upcallsActive() {
        return _upcallOption.isPresent();
    }

}
