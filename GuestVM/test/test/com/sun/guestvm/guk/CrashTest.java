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
package test.com.sun.guestvm.guk;

import com.sun.guestvm.guk.*;
import com.sun.guestvm.spinlock.guk.*;

/**
 * This is a simple test that forces a GUK crash by calling the scheduler in a thread that
 * holds a spin-lock, and therefore, has pre-emption disabled. It can be used to test
 * whether the Inspector gains control (if the domain is running under the Inspector).
 *
 * @author Mick Jordan
 *
 */
public class CrashTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        final GUKSpinLock spinLock = (GUKSpinLock) GUKSpinLockFactory.createAndInit();
        System.out.println("Testing lock");
        test(spinLock);
        System.out.println("Tested lock ok, going for crash");
        spinLock.lock();
        GUKScheduler.schedule();
    }

    private static void test(GUKSpinLock spinLock) {
        for (int i = 0; i < 100; i++) {
            spinLock.lock();
            spinLock.unlock();
        }
    }

}
