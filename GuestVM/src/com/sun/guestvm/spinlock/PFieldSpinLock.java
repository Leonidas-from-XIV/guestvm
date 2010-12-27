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
package com.sun.guestvm.spinlock;

import com.sun.guestvm.spinlock.tas.inline.ISpinLock;
import com.sun.max.annotate.*;
import com.sun.max.unsafe.*;
import com.sun.max.vm.actor.holder.ClassActor;
import com.sun.max.vm.actor.member.FieldActor;
import com.sun.max.vm.classfile.constant.*;
import com.sun.max.vm.reference.*;

/**
 * N.B. This code is identical to NPFieldSpinLock except for the parent class to get the pre-emption calls inlined.
 *
 * Holds the lock as a field and uses Maxine's Reference.compareAndSwapInt method to grab the lock.
 * Also handles pre-emption, abstractly, depending on the superclass chosen.
 *
 * In principle a globally optimizing compiler could duplicate the code for this class with the appropriate
 * inlined code and we could delay the choice of parent until later in the subclass hierarchy.
 *
 * @author Mick Jordan
 *
 */
public abstract class PFieldSpinLock extends PSpinLock {
    protected volatile int _lock;
    private static final Offset _lockOffset = Offset.fromInt(ClassActor.fromJava(PFieldSpinLock.class).findFieldActor(SymbolTable.makeSymbol("_lock"), null).offset());

    /**
     * Unlocks and re-enables pre-emption.
     */
    @Override
    public void unlock() {
        _lock = 0;
        enablePreemption();
    }

    /**
     * Tries to acquire the lock, disabling pre-emption first.
     * If acquisition fails re-enables pre-emption.
     * @return true iff the lock is acquire, false otherwise.
     */
    @INLINE
    protected final boolean canLock() {
        disablePreemption();
        final boolean r = Reference.fromJava(this).compareAndSwapInt(_lockOffset, 0, 1) == 0;
        if (!r) {
            enablePreemption();
        }
        return r;
    }
}
