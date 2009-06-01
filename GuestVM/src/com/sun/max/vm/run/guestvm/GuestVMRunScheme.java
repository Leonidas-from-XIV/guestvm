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
package com.sun.max.vm.run.guestvm;

import com.sun.max.annotate.*;
import com.sun.max.vm.run.extendimage.ExtendImageRunScheme;
import com.sun.max.vm.*;
import com.sun.guestvm.guk.*;
import com.sun.guestvm.memory.HeapPool;
import com.sun.guestvm.sched.*;
import com.sun.guestvm.net.guk.*;

/**
 * This run scheme is used to launch a GuestVM application.
 * It performs some important initialization prior to the loading
 * of the application's main method.
 *
 * At image build time, it also forces the loading of the classes
 * in the chosen scheduler scheme.
 *
 * @author Mick Jordan
 *
 */

public class GuestVMRunScheme extends ExtendImageRunScheme {

    public GuestVMRunScheme(VMConfiguration vmConfiguration) {
        super(vmConfiguration);
    }

    @Override
    public void initialize(MaxineVM.Phase phase) {
        super.initialize(phase);

        if (MaxineVM.isPrototyping()) {
            forceSchedulerScheme();
        }

        if (phase == MaxineVM.Phase.PRIMORDIAL) {
            GUKScheduler.initialize();
        } else if (phase == MaxineVM.Phase.RUNNING) {
            SchedulerFactory.scheduler().starting();
            GUKPagePool.createTargetMemoryThread(GUKPagePool.getCurrentReservation() * 4096);
            NetInit.init();
        }
    }

    @PROTOTYPE_ONLY
    private void forceSchedulerScheme() {
        final String schedulerFactory = System.getProperty("guestvm.scheduler.factory.class");
        if (schedulerFactory != null) {
            final int index = schedulerFactory.lastIndexOf('.');
            this.forceLoadPackage(schedulerFactory.substring(0, index));
            this.forceClassInit(schedulerFactory);
        }
    }

    @Override
    public void run() {
        HeapPool.setInitialHeapSize();
        super.run();
    }

}
