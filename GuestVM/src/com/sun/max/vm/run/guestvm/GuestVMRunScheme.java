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

import sun.nio.ch.BBNativeDispatcher;
import sun.rmi.registry.RegistryImpl;

import com.sun.max.annotate.*;
import com.sun.max.vm.heap.Heap;
import com.sun.max.vm.run.java.JavaRunScheme;
import com.sun.max.vm.*;
import com.sun.guestvm.*;
import com.sun.guestvm.fs.FSTable;
import com.sun.guestvm.fs.nfs.NFSExports;
import com.sun.guestvm.guk.*;
import com.sun.guestvm.memory.HeapPool;
import com.sun.guestvm.sched.*;
import com.sun.guestvm.net.guk.*;
import com.sun.guestvm.attach.AttachListener;
import com.sun.guestvm.error.*;
import com.sun.guestvm.profiler.*;


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

public class GuestVMRunScheme extends JavaRunScheme {

    private static final String RMIREGISTRY_PROPERTY = "guestvm.rmiregistry";
    private static final String TICK_PROFILER_PROPERTY = "guestvm.profiler";
    private static final String GUK_TRACE_PROPERTY = "guestvm.guktrace";

    @HOSTED_ONLY
    public GuestVMRunScheme() {
        super();
    }
    
    @Override
    public void initialize(MaxineVM.Phase phase) {
        if (phase == MaxineVM.Phase.STARTING) {
            // make sure we have console output in case of exceptions
            FSTable.basicInit();
        }
        super.initialize(phase);

        if (MaxineVM.isHosted() && phase == MaxineVM.Phase.BOOTSTRAPPING) {
            Heap.registerHeapSizeInfo(HeapPool.getHeapSizeInfo());
        }

        if (phase == MaxineVM.Phase.PRIMORDIAL) {
            GUK.initialize();
            GUKScheduler.initialize();
        } else if (phase == MaxineVM.Phase.RUNNING) {
            System.setProperty("guestvm.version", Version.ID);
            System.setProperty("os.version", Version.ID);
            SchedulerFactory.scheduler().starting();
            GUKPagePool.createTargetMemoryThread(GUKPagePool.getCurrentReservation() * 4096);
            BBNativeDispatcher.resetNativeDispatchers();
            NetInit.init();
            NFSExports.initNFSExports();
            checkRmiRegistry();
            AttachListener.create();
            checkGUKTrace();
            checkTickProfiler();
        }
    }

    public static boolean launcherInit() {
        GuestVMError.unexpected("FIX THIS");
        return false;
    }

    private static void checkRmiRegistry() {
        final String rmiRegistryProperty = System.getProperty(RMIREGISTRY_PROPERTY);
        String portArg = null;
        if (rmiRegistryProperty != null) {
            if (rmiRegistryProperty.length() > 0) {
                portArg = rmiRegistryProperty;
            }
            final String[] args = portArg == null ? new String[0] : new String[] {portArg};
            final Thread registry = new Thread() {
                public void run() {
                    try {
                        RegistryImpl.main(args);
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                        GuestVMError.unexpected("rmiregistry failed");
                    }
                }
            };
            registry.setName("RMIRegistry");
            registry.setDaemon(true);
            registry.start();
        }
    }

    private static void checkTickProfiler() {
        final String prop = System.getProperty(TICK_PROFILER_PROPERTY);
        if (prop != null) {
            int interval = 0;
            int depth = 4;
            int dumpPeriod = 0;
            if (prop.length() > 0) {
                final String[] options = prop.split(",");
                for (String option : options) {
                    if (option.startsWith("interval")) {
                        interval = getTickOption(option);
                    } else if (option.startsWith("depth")) {
                        depth = getTickOption(option);
                    } else if (option.startsWith("dump")) {
                        dumpPeriod = getTickOption(option);
                    } else {
                        tickUsage();
                    }
                }
            }
            if (interval < 0 || depth < 0) {
                tickUsage();
            }
            Tick.create(interval, depth, dumpPeriod);
        }
    }

    private static int getTickOption(String s) {
        final int index = s.indexOf('=');
        if (index < 0) {
            return index;
        }
        return Integer.parseInt(s.substring(index + 1));
    }

    private static void tickUsage() {
        GuestVMError.exit("usage: " + TICK_PROFILER_PROPERTY + "[=interval=i,depth=d,dump=t]");
    }

    private static void checkGUKTrace() {
        final String prop = System.getProperty(GUK_TRACE_PROPERTY);
        if (prop != null) {
            final String[] parts = prop.split(":");
            for (String name : parts) {
                try {
                    GUKTrace.setTraceState(GUKTrace.Name.valueOf(name), true);
                } catch (Exception ex) {
                    System.err.println("no GUK trace element '" + name + "' ignoring");
                }
            }
        }

    }
}
