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
package com.sun.guestvm.sched;

import java.util.*;
import com.sun.max.vm.thread.*;
import com.sun.max.vm.runtime.*;

/**
 * GuestVM implementation of the VMThreadFactory.
 * Although we don't know the exact subclass that will be created, we build
 * a cache of instances at image build time for runtime use by newVmThread.
 * This is to ensure that all VmThread instances are in the boot heap
 * and therefore avoid issues with entry to the scheduler while GC is taking place.
 * N.B. Even if the GC threads are non pre-emptable entry to the scheduler
 * can still occur due to an external event which typically requires touching
 * a VmThread instance.
 *
 * @author Mick Jordan
 *
 */

public class GUKVmThreadFactory extends VmThreadFactory {

    private static final int MAX_THREADS = 16384;
    private static List<VmThread> _vmThreadCache;
    private static int _vmThreadCacheSize;

    static void populateVmThreadCache() {
        _vmThreadCache = new LinkedList<VmThread>();
        for (int i = 0; i < MAX_THREADS; i++) {
            _vmThreadCache.add(RunQueueFactory.getInstance().newVmThread());
        }
    }

    protected VmThread newVmThread(Thread javaThread) {
        if (_vmThreadCache == null) {
            populateVmThreadCache();
        }
        if (_vmThreadCacheSize >= MAX_THREADS) {
            FatalError.crash("thread limit exceeded");
        }
        _vmThreadCacheSize++;
        return _vmThreadCache.remove(0).setJavaThread(javaThread);
    }

}
