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
 * Varieties of test and set spin locks.
 * We elide the NP prefix in the concrete classes, i.e., the lack of a P prefix implies
 * that thread pre-emption is disabled while the lock is held.
 *
 * TAS: Simple test and set.
 * TTAS: Test and test and set.
 * TAST: Test and set and test.
 *
 * C prefix: Count number of spins.
 * P prefix: Allow thread pre-emption while holding lock.
 *
 * The P variants are in the tas.p subpackage, the C variants in the tas.c subpackage and
 * the PC variants in the tas.cp subpackage.
 *
 * The difference between TTAS and TAST is that the latter tries to get the lock
 * first and, if unsuccessful, spins waiting for it to appear free, whereas the former checks first
 * and then tries to get the lock. If TAST fails to get the lock on the first attempt,
 * it therefore becomes a TTAS lock.
 *
 * E.g. TASSpinLock: Test and set spin lock that disables pre-emption while holding the lock.
 *        PCTTASSpinLock: Test and test and set spin lock, with pre-emption enabled and counts spins.
 *
 * Each spin lock class has an associated factory class to create instances.
 *
  */

package com.sun.guestvm.spinlock.tas;
