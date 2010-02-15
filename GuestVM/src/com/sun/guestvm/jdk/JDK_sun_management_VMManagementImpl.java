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

import java.lang.reflect.*;
import com.sun.max.annotate.*;
import com.sun.max.vm.*;
import com.sun.max.vm.actor.holder.ClassActor;
import com.sun.max.vm.actor.member.FieldActor;
import com.sun.max.vm.heap.*;
import com.sun.max.vm.management.*;
import com.sun.max.vm.object.TupleAccess;
import com.sun.guestvm.error.*;

/**
 * Substitutions for the native methods in @see sun.management.VMManagementImpl.
 * Most of these are unimplemented as yet.
 *
 * @author Mick Jordan
 *
 */

@SuppressWarnings("unused")

@METHOD_SUBSTITUTIONS(hiddenClass = "sun.management.VMManagementImpl")

final class JDK_sun_management_VMManagementImpl {

    private static String[] _supportedOptions = {"currentThreadCpuTimeSupport", "otherThreadCpuTimeSupport"};

    private static boolean isSupported(String name) {
        for (String s : _supportedOptions) {
            if (s.equals(name)) {
                return true;
            }
        }
        return false;
    }

    @SUBSTITUTE
    private static String getVersion0() {
        return "0.0";
    }

    @SUBSTITUTE
    private static void initOptionalSupportFields() {
        try {
            final Class<?> klass = Class.forName("sun.management.VMManagementImpl");
            final Object staticTuple = ClassActor.fromJava(klass).staticTuple();
            final Field[] fields = klass.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                final Field field = fields[i];
                final String fieldName = field.getName();
                if (fieldName.endsWith("Support")) {
                    TupleAccess.writeBoolean(staticTuple, FieldActor.fromJava(field).offset(), isSupported(fieldName));
                }
            }
        } catch (Exception ex) {
            GuestVMError.unexpected("problem initializing sun.management.VMManagementImpl " + ex);
        }
    }

    @SUBSTITUTE
    private boolean isThreadContentionMonitoringEnabled() {
        return false;
    }

    @SUBSTITUTE
    private boolean isThreadCpuTimeEnabled() {
        return true;
    }

    @SUBSTITUTE
    private long getTotalClassCount() {
        return ClassLoadingManagement.getTotalClassCount();
    }

    @SUBSTITUTE
    private long getUnloadedClassCount() {
        return ClassLoadingManagement.getUnloadedClassCount();
    }

    @SUBSTITUTE
    private boolean getVerboseClass() {
        return VMOptions.verboseOption.verboseClass;
    }

    @SUBSTITUTE
    private boolean getVerboseGC() {
        return Heap.verbose();
    }

    @SUBSTITUTE
    private int getProcessId() {
        final String pid = System.getProperty("guestvm.pid");
        return pid == null ? 0 : Integer.parseInt(pid);
    }

    @SUBSTITUTE
    private String getVmArguments0() {
        return RuntimeManagement.getVmArguments();
    }

    @SUBSTITUTE
    private long getStartupTime() {
        return RuntimeManagement.getStartupTime();
    }

    @SUBSTITUTE
    private int getAvailableProcessors() {
        return Runtime.getRuntime().availableProcessors();
    }

    @SUBSTITUTE
    private long getTotalCompileTime() {
        unimplemented("getTotalCompileTime");
        return 0;
    }

    @SUBSTITUTE
    private long getTotalThreadCount() {
        return ThreadManagement.getTotalThreadCount();
    }

    @SUBSTITUTE
    private int  getLiveThreadCount() {
        return ThreadManagement.getThreads().length;
    }

    @SUBSTITUTE
    private int  getPeakThreadCount() {
        return ThreadManagement.getPeakThreadCount();
    }

    @SUBSTITUTE
    private int  getDaemonThreadCount() {
        return ThreadManagement.getDaemonThreadCount();
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
        GuestVMError.unimplemented("unimplemented sun.management.VMManagementImpl." + name);
    }
}
