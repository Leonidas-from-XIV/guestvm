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
package com.sun.max.ve.spinlock;

import com.sun.max.program.ProgramError;

/**
 * A factory that permits subclasses of SpinLock to b created. To create instances of a {@code SpinLock} subclass,
 * the {@link #SPINLOCK_FACTORY_CLASS_PROPERTY_NAME} property needs to be defined at image build time.
 *
 * @author Mick Jordan
 */
public abstract class SpinLockFactory {
    /**
     * The name of the system property specifying a subclass of {@link SpinLockFactory} that is
     * to be instantiated and used at runtime to create SpinLock instances. If not specified,
     * then the instance must be set at runtime via setInstance.
     */
    public static final String SPINLOCK_FACTORY_CLASS_PROPERTY_NAME = "max.ve.spinlock.factory.class";

    private static SpinLockFactory _instance = null;

    static {
        final String factoryClassName = System.getProperty(SPINLOCK_FACTORY_CLASS_PROPERTY_NAME);
        if (factoryClassName != null) {
            try {
                _instance = (SpinLockFactory) Class.forName(factoryClassName).newInstance();
            } catch (Exception exception) {
                throw ProgramError.unexpected("Error instantiating " + factoryClassName, exception);
            }
        }
    }

    /**
     * Subclasses override this method to instantiate objects of a SpinLock subclass.
     *
     */
    protected abstract SpinLock newSpinLock();

    public static SpinLockFactory getInstance() {
        return _instance;
    }

    public static void setInstance(SpinLockFactory instance) {
        _instance = instance;
    }

    /**
     * Creates a SpinLock object.
     *
     * @return a particular subclass of a SpinLock
     */
    public static SpinLock create() {
        return _instance.newSpinLock();
    }

    /**
     * Creates a SpinLock object and initializes it.
     *
     * @return a particular subclass of a SpinLock
     */
    public static SpinLock createAndInit() {
        return (SpinLock) _instance.newSpinLock().initialize();
    }

}
