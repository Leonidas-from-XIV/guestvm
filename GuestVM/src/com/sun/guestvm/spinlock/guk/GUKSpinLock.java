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
package com.sun.guestvm.spinlock.guk;

import java.lang.ref.*;

import com.sun.guestvm.guk.*;
import com.sun.guestvm.spinlock.*;
import com.sun.max.annotate.*;
import com.sun.max.unsafe.Pointer;

/**
 * Delegates to ukernel allocated spin locks, so has native state that
 * must be allocated at the appropriate time. To support spinlocks
 * that are created during image build, so obviously can't have the
 * native component created then, the native spinlock is created
 * by the initialize method.
 *
 * @author Mick Jordan
 *
 */
public class GUKSpinLock extends SpinLock {

    private static ReferenceQueue<GUKSpinLock> _refQueue = new ReferenceQueue<GUKSpinLock>();

    static class NativeReference extends WeakReference<GUKSpinLock> {

        @CONSTANT_WHEN_NOT_ZERO
        protected Pointer _spinlock;

        NativeReference(GUKSpinLock m) {
            super(m, _refQueue);
        }

        private void disposeNative() {
            GUKScheduler.destroySpinLock(_spinlock);
        }
    }

    protected NativeReference _native;

    GUKSpinLock() {
        // Create the NativeReference now to avoid heap allocation
        // during initialize, which might be inconvenient, e.g.., during VM startup.
        _native = new NativeReference(this);
    }

    @Override
    public boolean isNative() {
        return true;
    }

    @Override
    public NativeSpinLockSupport initialize() {
        _native._spinlock = GUKScheduler.createSpinLock();
        return this;
    }

    @Override
    public void cleanup() {
        _native.disposeNative();
    }

    @Override
    public void lock() {
        GUKSpinLock.spinLock(_native._spinlock);
    }

    @Override
    public void unlock() {
        GUKSpinLock.spinUnlock(_native._spinlock);
    }

    /**
     * Release spin lock created elsewhere.
     * @param lock
     */
    @INLINE
    public static void spinUnlock(Pointer lock) {
        GUK.guk_spin_unlock(lock);
    }

    /**
     * Acquire lock created elsewhere.
     * @param lock
     */
    @INLINE
    public static void spinLock(Pointer lock) {
        GUK.guk_spin_lock(lock);
    }

}
