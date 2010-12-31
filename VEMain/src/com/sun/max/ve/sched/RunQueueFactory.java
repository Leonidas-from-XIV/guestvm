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
package com.sun.max.ve.sched;

import com.sun.max.program.*;
import com.sun.max.ve.sched.nopriority.*;
import com.sun.max.vm.thread.*;

/**
 * An abstract Factory to create compatible threads and run queue for the MaxVE scheduler.
 *
 * @author Harald Roeck
 * @author Mick Jordan
 *
 */
public abstract class RunQueueFactory {
    /**
     * The name of the system property specifying a subclass of {@link RunQueueFactory} that is
     * to be instantiated and used at runtime to create Scheduler run queue instances and their
     * associated subclass of GVmThread.
     * The choice of class is made at image build time.
     */
    public static final String RUNQUEUE_FACTORY_CLASS_PROPERTY_NAME = "max.ve.scheduler.runqueue.factory.class";

    protected static RunQueueFactory _instance = null;

    protected RunQueueFactory() {
    }

    private static void instantiateFactory() {
        final String factoryClassName = System.getProperty(RUNQUEUE_FACTORY_CLASS_PROPERTY_NAME);
        if (factoryClassName == null) {
            _instance = new RingRunQueueFactory();
        } else {
            try {
                _instance = (RunQueueFactory) Class.forName(factoryClassName).newInstance();
            } catch (Exception exception) {
                throw ProgramError.unexpected("Error instantiating " + factoryClassName, exception);
            }
        }
    }

    public static RunQueueFactory getInstance() {
        if (_instance == null) {
            instantiateFactory();
        }
        return _instance;
    }

    public abstract VmThread newVmThread();

    public abstract RunQueue<GUKVmThread> createRunQueue();
}
