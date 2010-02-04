/*
 * Copyright (c) 2010 Sun Microsystems, Inc., 4150 Network Circle, Santa
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
package com.sun.guestvm.jdk;

import java.lang.management.*;
import com.sun.max.annotate.*;
import com.sun.max.vm.management.ThreadManagement;
import com.sun.guestvm.error.*;

/**
 * Substitutions for the native methods in @see sun.management.ThreadImpl.
 * Most of these are unimplemented as yet.
 *
 * @author Mick Jordan
 *
 */

@METHOD_SUBSTITUTIONS(hiddenClass = "sun.management.ThreadImpl")
final class JDK_sun_management_ThreadImpl {
    @SUBSTITUTE
    private static Thread[] getThreads() {
        return ThreadManagement.getThreads();
    }

    @SUBSTITUTE
    private static void getThreadInfo0(long[] ids, int maxDepth, ThreadInfo[] result) {
        ThreadManagement.getThreadInfo(ids, maxDepth, result);
    }

    @SUBSTITUTE
    private static long getThreadTotalCpuTime0(long id) {
        unimplemented("getThreadTotalCpuTime0");
        return 0;
    }

    @SUBSTITUTE
    private static long getThreadUserCpuTime0(long id) {
        unimplemented("getThreadUserCpuTime0");
        return 0;
    }

    @SUBSTITUTE
    private static void setThreadCpuTimeEnabled0(boolean enable) {
        ThreadManagement.setThreadCpuTimeEnabled(enable);
    }

    @SUBSTITUTE
    private static void setThreadContentionMonitoringEnabled0(boolean enable) {
        ThreadManagement.setThreadCpuTimeEnabled(enable);
    }

    @SUBSTITUTE
    private static Thread[] findMonitorDeadlockedThreads0() {
        unimplemented("findMonitorDeadlockedThreads0");
        return null;
    }

    @SUBSTITUTE
    private static Thread[] findDeadlockedThreads0() {
        unimplemented("findDeadlockedThreads0");
        return null;
    }

    @SUBSTITUTE
    private static void resetPeakThreadCount0() {
        ThreadManagement.resetPeakThreadCount();
    }

    @SUBSTITUTE
    private static ThreadInfo[] dumpThreads0(long[] ids, boolean lockedMonitors, boolean lockedSynchronizers) {
        return ThreadManagement.dumpThreads(ids, lockedMonitors, lockedSynchronizers);
    }

    @SUBSTITUTE
    private static void resetContentionTimes0(long tid) {
        unimplemented("resetContentionTimes0");
    }

    private static void unimplemented(String name) {
        GuestVMError.unimplemented("unimplemented sun.management.ThreadImpl." + name);
    }

}
