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
import com.sun.max.collect.AppendableSequence;
import com.sun.max.collect.LinkSequence;
import com.sun.max.vm.actor.holder.*;
import com.sun.max.vm.run.extendimage.ExtendImageRunScheme;
import com.sun.max.vm.*;
import com.sun.guestvm.guk.*;
import com.sun.guestvm.memory.HeapPool;
import com.sun.guestvm.sched.*;
import com.sun.guestvm.net.guk.*;
import com.sun.guestvm.error.*;

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

    private static boolean _netInit;
    private static boolean _launcherReset;
    private static AppendableSequence<ClassActor> _netReinitClasses = new LinkSequence<ClassActor>();
    private static final String PRE_RUN_CLASSES_PROPERTY = "guestvm.prerun.classes";

    public GuestVMRunScheme(VMConfiguration vmConfiguration) {
        super(vmConfiguration);
    }

    @Override
    public void initialize(MaxineVM.Phase phase) {
        super.initialize(phase);

        if (MaxineVM.isHosted()) {
            forceSchedulerScheme();
            forceNetReInit();
        }

        if (phase == MaxineVM.Phase.PRIMORDIAL) {
            GUKScheduler.initialize();
        } else if (phase == MaxineVM.Phase.RUNNING) {
            SchedulerFactory.scheduler().starting();
            GUKPagePool.createTargetMemoryThread(GUKPagePool.getCurrentReservation() * 4096);
            netInit();
            preRunClasses();
        }
    }

    private static void netInit() {
        if (!_netInit) {
            for (ClassActor classActor : _netReinitClasses) {
                classActor.callInitializer();
            }
            NetInit.init();
            _netInit = true;
        }
    }

    private static void preRunClasses() {
        final String prop = System.getProperty(PRE_RUN_CLASSES_PROPERTY);
        if (prop != null) {
            final String[] classNames = prop.split(",");
            for (String className : classNames) {
                try {
                    final Class<?> klass = Class.forName(className);
                    klass.newInstance();
                } catch (Exception ex) {
                    GuestVMError.unexpected("failed to load/execute pre-run class: " + prop + " " + ex.getMessage());
                }
            }
        }
    }

    @HOSTED_ONLY
    private void forceNetReInit() {
        _netReinitClasses.append(doForceInitClass("java.net.NetworkInterface", false));
        _netReinitClasses.append(doForceInitClass("java.net.PlainDatagramSocketImpl", false));
        _netReinitClasses.append(doForceInitClass("java.net.PlainSocketImpl", false));
    }

    @Override
    protected void resetLauncher(ClassActor launcherClassActor) {
        // must initialize network before the launcher reset, which accesses the file systems, potentially including NFS
        netInit();
        super.resetLauncher(launcherClassActor);
        _launcherReset = true;
    }

    public static boolean launcherInit() {
        return _launcherReset;
    }

    @HOSTED_ONLY
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
