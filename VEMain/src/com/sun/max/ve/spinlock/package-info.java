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
/**
 * Basic support for spin locks.
 *
 * All the classes are abstract, either specifying interfaces or proving support methods for concrete subclasses.
 * The most basic concrete subclasses are in the guk sub-package, and delegate to the C implementation
 * in the microkernel, which is a test and set and test lock that disables pre-emption.
 *
 * The abstract class SpinLock provides the basic lock/unlock abstract methods.
 *
 * For pedagogical reasons the class hierarchy is split into two branches, one that disables
 * thread pre-emption (NP) while holding the lock and one that doesn't (P). In practice it is a bad
 * idea to allow pre-emption while holding a spin lock and this can be demonstrated by
 * testing with the pre-empting variants. Of course, in a context where pre-emption is already disabled
 * the P variants are useful.
 *
 * OPSpinLock adds the abstracts methods to disable/enable pre-emption. PSpinLock
 * implements these with empty methods, whereas NPSpinLock invokes the (inlined) code in GVmThread
 * to actually disable/enable pre-emption in the microkernel.
 *
 * In order to be able to inline as much code as possible, the class hierarchy is duplicated into P and NP
 * variants that have identical code except that the superclass follows the P or NP track.
 * In principle it would be possible to avoid this duplication if the compiler was smart enough to duplicate
 * the compiled code of the intermediate classes in the hierarchy, e.g., NPSpinLock, and inline it appropriately.
 *
 * The P/NPFieldSpinLock subclasses provide the basic lock and unlock implementation in terms of
 * a volatile int lock field and a compare and exchange instruction, courtesy of Maxine. They also handle
 * the enabling and disabling of pre-emption.
 *
 * For measurement purposes, the subclasses P/NPCountingSpinLock add a field that counts the number
 * of times a thread spins trying to acquire the lock. The max count observed can be accessed by the
 * getMaxCount method of the CountingSpinLock interface.
 *
 * Concrete classes are created elsewhere in Guest VM using the SpinLockFactory class or its subclasses.
 * The default subclass to use can be specified by a system property, "max.ve.spinlock.factory.class".
 *
 */

package com.sun.max.ve.spinlock;
