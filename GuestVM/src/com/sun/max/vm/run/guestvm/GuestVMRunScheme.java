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

import java.lang.reflect.*;
import java.util.*;
import sun.nio.ch.BBNativeDispatcher;
import sun.rmi.registry.RegistryImpl;

import com.sun.max.annotate.*;
import com.sun.max.vm.actor.holder.*;
import com.sun.max.vm.object.TupleAccess;
import com.sun.max.vm.actor.member.*;
import com.sun.max.vm.heap.Heap;
import com.sun.max.vm.run.extendimage.ExtendImageRunScheme;
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

public class GuestVMRunScheme extends ExtendImageRunScheme {

    private static boolean _netInit;
    private static boolean _launcherReset;
    private static List<ClassActor> _netReinitClasses = new LinkedList<ClassActor>();
    private static final String RMIREGISTRY_PROPERTY = "guestvm.rmiregistry";
    private static final String TICK_PROFILER_PROPERTY = "guestvm.profiler";
    private static final String GUK_TRACE_PROPERTY = "guestvm.guktrace";

    public GuestVMRunScheme(VMConfiguration vmConfiguration) {
        super(vmConfiguration);
    }
    
    @Override
    public void initialize(MaxineVM.Phase phase) {
        if (phase == MaxineVM.Phase.STARTING) {
            // make sure we have console output in case of exceptions
            FSTable.basicInit();
        }
        super.initialize(phase);

        if (MaxineVM.isHosted()) {
            Heap.registerHeapSizeInfo(HeapPool.getHeapSizeInfo());
            forceSchedulerScheme();
            forceNetReInit();
        }

        if (phase == MaxineVM.Phase.PRIMORDIAL) {
            GUK.initialize();
            GUKScheduler.initialize();
        } else if (phase == MaxineVM.Phase.RUNNING) {
            System.setProperty("guestvm.version", Version.ID);
            System.setProperty("os.version", Version.ID);
            SchedulerFactory.scheduler().starting();
            GUKPagePool.createTargetMemoryThread(GUKPagePool.getCurrentReservation() * 4096);
            resetNativeDispatchers();
            netInit();
            NFSExports.initNFSExports();
            checkRmiRegistry();
            AttachListener.create();
            checkGUKTrace();
            checkTickProfiler();
        }
    }

    private static void netInit() {
        if (!_netInit) {
            for (ClassActor classActor : _netReinitClasses) {
                try {
                    classActor.callInitializer();
                } catch (InvocationTargetException ex) {
                    GuestVMError.unexpected("failed to reinitialize network classes");
                }
            }
            NetInit.init();
            _netInit = true;
        }
    }

    @HOSTED_ONLY
    private void forceNetReInit() {
        _netReinitClasses.add(doForceInitClass("java.net.NetworkInterface", false));
        _netReinitClasses.add(doForceInitClass("java.net.PlainDatagramSocketImpl", false));
        _netReinitClasses.add(doForceInitClass("java.net.PlainSocketImpl", false));
    }

    @Override
    protected void resetLauncher(ClassActor launcherClassActor) {
        // must initialize network before the launcher reset, which accesses the file systems, potentially including NFS
        netInit();
        super.resetLauncher(launcherClassActor);
        _launcherReset = true;
    }

    void resetNativeDispatchers() {
        final BBNativeDispatcher bbnd = new BBNativeDispatcher();
        resetNativeDispatcher("sun.nio.ch.DatagramChannelImpl", bbnd);
        resetNativeDispatcher("sun.nio.ch.ServerSocketChannelImpl", bbnd);
        resetNativeDispatcher("sun.nio.ch.SocketChannelImpl", bbnd);
        resetNativeDispatcher("sun.nio.ch.SinkChannelImpl", bbnd);
        resetNativeDispatcher("sun.nio.ch.SourceChannelImpl", bbnd);
        resetNativeDispatcher("sun.nio.ch.FileChannelImpl", bbnd);
    }

    void resetNativeDispatcher(String name, BBNativeDispatcher nd) {
        try {
            final FieldActor rfa = ClassActor.fromJava(Class.forName(name)).findLocalStaticFieldActor("nd");
            assert rfa != null;
            TupleAccess.writeObject(rfa.holder().staticTuple(), rfa.offset(), nd);
        } catch (ClassNotFoundException ex) {
            GuestVMError.unexpected("problem with Class.forName: " + name);
        }
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
