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

import com.sun.guestvm.sched.std.StdSchedulerFactory;
import com.sun.max.program.*;

/**
 * A factory that permits subclasses of Scheduler to be created. To create instances of a {@code Scheduler} subclass,
 * the {@link #MUTEX_FACTORY_CLASS_PROPERTY_NAME} property needs to be defined at image build time.
 *
 * @author Mick Jordan
 *
 */

public abstract class SchedulerFactory {

    /**
     * The name of the system property specifying a subclass of {@link SchedulerFactory} that is
     * to be instantiated and used to create Scheduler instances.
     */
    public static final String SCHEDULER_FACTORY_CLASS_PROPERTY_NAME = "guestvm.scheduler.factory.class";

    protected static SchedulerFactory _instance;
    protected static Scheduler _scheduler;

    protected SchedulerFactory() {
    }

    static {
        final String factoryClassName = System.getProperty(SCHEDULER_FACTORY_CLASS_PROPERTY_NAME);
        if (factoryClassName == null) {
            _instance = new StdSchedulerFactory();
        } else {
            try {
                _instance = (SchedulerFactory) Class.forName(factoryClassName).newInstance();
            } catch (Exception exception) {
                throw ProgramError.unexpected("Error instantiating " + factoryClassName, exception);
            }
        }
        // Scheduler must be created at image build time
        _scheduler = _instance.createScheduler();
    }

    public static SchedulerFactory instance() {
        return _instance;
    }

    public abstract Scheduler createScheduler();

    public static Scheduler scheduler() {
        if (_scheduler == null) {
            _scheduler = _instance.createScheduler();
        }
        return _scheduler;
    }
}
