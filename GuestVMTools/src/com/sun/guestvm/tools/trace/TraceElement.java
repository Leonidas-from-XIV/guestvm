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
package com.sun.guestvm.tools.trace;


public class TraceElement {
    protected long _absTimestamp;
    private long _relTimestamp;
    private int _cpu;
    private int _thread;
    private TraceKind _traceKind;

    private static long _absStartTime;
    private static boolean _useRelTimestamp = true;
    private static int _numCpus;
    private static boolean _firstTrace = true;

    public TraceElement init(TraceKind traceKind, long timestamp, int cpu, int thread) {
        _traceKind = traceKind;
        _absTimestamp = timestamp;
        _cpu = cpu;
        _thread = thread;
        if (_firstTrace) {
            _absStartTime = timestamp;
            _firstTrace = false;
        }
        if (traceKind == TraceKind.RI) {
            _numCpus++;
        }
        _relTimestamp = _absTimestamp - _absStartTime;
        return this;
    }

    public static void setUseAbsTimestamp(boolean on) {
        _useRelTimestamp = !on;
    }

    public static int getCpus() {
        return _numCpus;
    }

    public long getTimestamp() {
        return _useRelTimestamp  ? _relTimestamp : _absTimestamp;
    }

    public int getCpu() {
        return _cpu;
    }

    public int getThread() {
        return _thread;
    }

    public TraceKind getTraceKind() {
        return _traceKind;
    }

    public String toString() {
        return getTimestamp() + " " + _cpu + " " + _thread + " " + _traceKind.name();
    }

}
