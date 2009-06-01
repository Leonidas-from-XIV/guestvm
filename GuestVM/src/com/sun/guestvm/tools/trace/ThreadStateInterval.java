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

import java.util.ArrayList;
import java.util.List;


public class ThreadStateInterval {
    private ThreadState _state;
    private long _start;
    private long _end;
    private int _cpu;

    public ThreadStateInterval(ThreadState state, long start) {
        _state = state;
        _start = start;
    }

    public ThreadState getState() {
        return _state;
    }

    public long getStart() {
        return _start;
    }

    public long getEnd() {
        return _end;
    }

    public void setEnd(long end) {
        _end = end;
    }

    public int getCpu() {
        return _cpu;
    }

    public void setCpu(int cpu) {
        _cpu = cpu;
    }

    public String toString() {
        String result = _state.name();
        if (_state == ThreadState.ONCPU) {
            result += " " + _cpu;
        }
        return result;
    }

    public static long intervalLength(List<ThreadStateInterval> intervals) {
        final ThreadStateInterval te = intervals.get(intervals.size() - 1);
        long end;
        if (te.getState() == ThreadState.REAPED) {
            end = intervals.get(intervals.size() - 2).getStart();
        } else if (te.getState() == ThreadState.EXITED) {
            end = te.getStart();
        } else {
            end = te.getEnd();
        }
        return end - intervals.get(0).getStart();
    }

    public static List<ThreadStateInterval> createIntervals(List<TraceElement> traces, int id) {
        final List<ThreadStateInterval> history = new ArrayList<ThreadStateInterval>();
        ThreadStateInterval current = null;
        for (TraceElement trace : traces) {
            if (trace.getTraceKind() == TraceKind.BK) {
                final ThreadIdTraceElement ctrace = (ThreadIdTraceElement) trace;
                if (ctrace.getId() == id) {
                    if (current.getState() != ThreadState.EXITED && current.getState() != ThreadState.INSCHED) {
                        current = newThreadStateInterval(current, ThreadState.INSCHED, ctrace, history);
                    }
                }
            } else if (trace.getTraceKind() == TraceKind.WK) {
                final ThreadIdTraceElement ctrace = (ThreadIdTraceElement) trace;
                if (ctrace.getId() == id) {
                    if (current.getState() != ThreadState.INSCHED) {
                        current = newThreadStateInterval(current, ThreadState.INSCHED, ctrace, history);
                    }
                }
            } else if (trace.getTraceKind() == TraceKind.TS) {
                final ThreadSwitchTraceElement ctrace = (ThreadSwitchTraceElement) trace;
                if (ctrace.getFromId() == id && (ctrace.getToId() != ctrace.getFromId())) {
                    // switching off cpu
                    ThreadState newState = current.getState();
                    switch (current.getState()) {
                        case ONCPU:
                            newState = ThreadState.OFFCPU;
                            break;
                        case EXITED:
                            continue;
                        case INSCHED:
                            newState = ThreadState.BLOCKED;
                            break;
                        default:
                            System.err.println("Unexpected state in TS: " + current.getState() + ", @ " + ctrace.getTimestamp());
                    }
                    current = newThreadStateInterval(current, newState, ctrace, history);
                } else if (ctrace.getToId() == id) {
                    // switching on cpu
                    current = newThreadStateInterval(current, ThreadState.ONCPU, ctrace, history);
                    current.setCpu(ctrace.getCpu());
                }
            } else if (trace.getTraceKind() == TraceKind.CT) {
                final CreateThreadTraceElement ctrace = (CreateThreadTraceElement) trace;
                if (ctrace.getId() == id) {
                    current = newThreadStateInterval(current, ThreadState.INSCHED, ctrace, history);
                }
            } else if (trace.getTraceKind() == TraceKind.TX) {
                final ThreadIdTraceElement ctrace = (ThreadIdTraceElement) trace;
                if (ctrace.getId() == id) {
                    current = newThreadStateInterval(current, ThreadState.EXITED, ctrace, history);
                }
            } else if (trace.getTraceKind() == TraceKind.DT) {
                final ThreadIdTraceElement ctrace = (ThreadIdTraceElement) trace;
                if (ctrace.getId() == id) {
                    current = newThreadStateInterval(current, ThreadState.REAPED, ctrace, history);
                    break;
                }
            } else if (trace.getTraceKind() == TraceKind.RI) {
                // special variant of TS, first execution of the idle thread
                if (trace.getCpu() == id) {
                    current = newThreadStateInterval(current, ThreadState.ONCPU, trace, history);
                    current.setCpu(trace.getCpu());
                }
            } else if (trace.getTraceKind() == TraceKind.BI) {
                // block idle thread
                if (trace.getCpu() == id) {
                    current = newThreadStateInterval(current, ThreadState.BLOCKED, trace, history);
                }
            } else if (trace.getTraceKind() == TraceKind.WI) {
                // wake idle thread
                if (trace.getCpu() == id) {
                    current = newThreadStateInterval(current, ThreadState.INSCHED, trace, history);
                }
            }
        }
        if (current.getEnd() == 0) {
            current.setEnd(TraceReader.lastTrace().getTimestamp());
        }
        return history;
    }

    private static ThreadStateInterval newThreadStateInterval(ThreadStateInterval oldThreadStateInterval, ThreadState state, TraceElement traceElement, List<ThreadStateInterval> history) {
        if (oldThreadStateInterval != null) {
            oldThreadStateInterval.setEnd(traceElement.getTimestamp());
        }
        final ThreadStateInterval result = new ThreadStateInterval(state, traceElement.getTimestamp());
        history.add(result);
        return result;
    }
}
