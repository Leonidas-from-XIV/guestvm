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
package test.util;

/**
 * Some methods that can be called in any OS environment but call GUK via reflection if that's where we are running.
 * @author Mick Jordan
 */

import java.lang.reflect.*;

public final class OSSpecific implements Runnable {

    private static boolean _initialized;
    private static Method _threadStatsMethod;
    private static Method _setTimeSliceMethod;
    private static Method _setTraceStateMethod;
    private static Object[] _nullArgs = new Object[0];
    private int _period;

    public static void printThreadStats() {
        if (!_initialized) {
            initialize();
        }
        if (_threadStatsMethod != null) {
            try {
                _threadStatsMethod.invoke(null, _nullArgs);
            } catch (Throwable t) {

            }
        }
    }

    public static void setTimeSlice(Thread thread, int time) {
        if (!_initialized) {
            initialize();
        }
        if (_setTimeSliceMethod != null) {
            try {
                _setTimeSliceMethod.invoke(null, new Object[] {thread, time});
            } catch (Throwable t) {

            }
        }
    }

    public static boolean setTraceState(int ordinal, boolean value) {
        if (!_initialized) {
            initialize();
        }
        if (_setTraceStateMethod != null) {
            try {
                final Boolean result = (Boolean) _setTraceStateMethod.invoke(null, new Object[] {ordinal, value});
                return result.booleanValue();
            } catch (Throwable t) {

            }
        }
        return false;
    }

    private OSSpecific(int period) {
        _period = period;
    }

    /**
     * Periodically print thread statistics.
     *
     * @param period
     *            in millisecs
     */
    public static void periodicThreadStats(int period) {
        if (!_initialized) {
            initialize();
        }
        if (_threadStatsMethod != null) {
            final Thread t = new Thread(new OSSpecific(period), "ThreadStats");
            t.setDaemon(true);
            t.start();
        }
    }

    public void run() {
        while (true) {
            try {
                Thread.sleep(_period);
            } catch (InterruptedException ex) {

            }
            printThreadStats();
        }
    }

    private static void initialize() {
        final String os = System.getProperty("os.name");
        if (os.equals("GuestVM")) {
            try {
                final Class<?> schedClass = Class.forName("com.sun.guestvm.guk.GUKScheduler");
                final Class<?> traceClass = Class.forName("com.sun.guestvm.guk.GUKTrace");
                final Class<?> vmThreadClass = Class.forName("com.sun.max.vm.thread.VmThread");
                _threadStatsMethod = schedClass.getMethod("printRunQueue", (Class<?>[]) null);
                _setTimeSliceMethod = schedClass.getMethod("setThreadTimeSlice", new Class<?>[] {vmThreadClass, int.class});
                _setTraceStateMethod = traceClass.getMethod("setTraceState", new Class<?>[] {int.class, boolean.class});

            } catch (Throwable t) {
                System.err.println("OSSpecific: error finding GUK methods: " + t);
            }
        }
        _initialized = true;
    }
}
