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
package com.sun.guestvm.jdk;

import com.sun.max.annotate.*;
import com.sun.max.program.*;

/**
 * Substitutions for the native methods in @see sun.management.VMManagementImpl.
 * Most of these are unimplemented as yet.
 *
 * @author Mick Jordan
 *
 */

@SuppressWarnings("unused")

@METHOD_SUBSTITUTIONS(hiddenClass = "sun.management.VMManagementImpl")

public class JDK_sun_management_VMManagementImpl {
    @SUBSTITUTE
    private String getVersion0() {
        return "0.0";
    }

    @SUBSTITUTE
    private void initOptionalSupportFields() {
        unimplemented("initOptionalSupportFields");
    }

    @SUBSTITUTE
    private boolean isThreadContentionMonitoringEnabled() {
        return false;
    }

    @SUBSTITUTE
    private boolean isThreadCpuTimeEnabled() {
        return false;
    }

    @SUBSTITUTE
    private long getTotalClassCount() {
        unimplemented("getTotalClassCount");
        return 0;
    }

    @SUBSTITUTE
    private long getUnloadedClassCount() {
        unimplemented("getUnloadedClassCount");
        return 0;
    }

    @SUBSTITUTE
    private boolean getVerboseClass() {
        unimplemented("getVerboseClass");
        return false;
    }

    @SUBSTITUTE
    private boolean getVerboseGC() {
        unimplemented("getVerboseGC");
        return false;
    }

    @SUBSTITUTE
    private int getProcessId() {
        unimplemented("getProcessId");
        return 0;
    }

    @SUBSTITUTE
    private String getVmArguments0() {
        unimplemented("getVmArguments0");
        return null;
    }

    @SUBSTITUTE
    private long getStartupTime() {
        unimplemented("getStartupTime");
        return 0;
    }

    @SUBSTITUTE
    private int getAvailableProcessors() {
        unimplemented("getAvailableProcessors");
        return 0;
    }

    @SUBSTITUTE
    private long getTotalCompileTime() {
        unimplemented("getTotalCompileTime");
        return 0;
    }

    @SUBSTITUTE
    private long getTotalThreadCount() {
        unimplemented("getTotalThreadCount");
        return 0;
    }

    @SUBSTITUTE
    private int  getLiveThreadCount() {
        unimplemented("getLiveThreadCount");
        return 0;
    }

    @SUBSTITUTE
    private int  getPeakThreadCount() {
        unimplemented("getPeakThreadCount");
        return 0;
    }

    @SUBSTITUTE
    private int  getDaemonThreadCount() {
        unimplemented(" getDaemonThreadCount");
        return 0;
    }

    @SUBSTITUTE
    private long getSafepointCount() {
        unimplemented("getSafepointCount");
        return 0;
    }

    @SUBSTITUTE
    private long getTotalSafepointTime() {
        unimplemented("getTotalSafepointTime");
        return 0;
    }

    @SUBSTITUTE
    private long getSafepointSyncTime() {
        unimplemented("getSafepointSyncTime");
        return 0;
    }

    @SUBSTITUTE
    private long getTotalApplicationNonStoppedTime() {
        unimplemented("getTotalApplicationNonStoppedTime");
        return 0;
    }

    @SUBSTITUTE
    private long getLoadedClassSize() {
        unimplemented("getLoadedClassSize");
        return 0;
    }

    @SUBSTITUTE
    private long getUnloadedClassSize() {
        unimplemented("getUnloadedClassSize");
        return 0;
    }

    @SUBSTITUTE
    private long getClassLoadingTime() {
        unimplemented("getClassLoadingTime");
        return 0;
    }

    @SUBSTITUTE
    private long getMethodDataSize() {
        unimplemented("getMethodDataSize");
        return 0;
    }

    @SUBSTITUTE
    private long getInitializedClassCount() {
        unimplemented("getInitializedClassCount");
        return 0;
    }

    @SUBSTITUTE
    private long getClassInitializationTime() {
        unimplemented("getClassInitializationTime");
        return 0;
    }

    @SUBSTITUTE
    private long getClassVerificationTime() {
        unimplemented("getClassVerificationTime");
        return 0;
    }

    private static void unimplemented(String name) {
        ProgramError.unexpected("unimplemented sun.management.VMManagementImpl." + name);
    }
}
