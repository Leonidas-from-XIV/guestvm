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
package com.sun.max.ve.tools.trace.cmds.thread;

import java.util.List;

import com.sun.max.ve.tools.trace.CPUState;
import com.sun.max.ve.tools.trace.Command;
import com.sun.max.ve.tools.trace.CommandHelper;
import com.sun.max.ve.tools.trace.CreateThreadTraceElement;
import com.sun.max.ve.tools.trace.ThreadState;
import com.sun.max.ve.tools.trace.ThreadStateInterval;
import com.sun.max.ve.tools.trace.TimeFormat;
import com.sun.max.ve.tools.trace.TraceElement;
import com.sun.max.ve.tools.trace.TraceKind;

/**
 * This command reports on the state of a thread at a given time (default is end of trace)
 * and other misc queries.
 *
 * @see ThreadState
 *
 * @author Mick Jordan
 *
 */

public class ThreadStateCommand extends CommandHelper implements Command {
    private static final String THREAD_ID = "id=";
    private static final String TIME = "time=";
    private static final String LASTONCPU = "lastoncpu";
    private static final String CREATEINFO = "create";

    @Override
    public void doIt(List<TraceElement> traces, String[] args) throws Exception {
        long endTime = Long.MAX_VALUE;
        final String time = stringArgValue(args, TIME);
        if (time != null) {
            endTime = Long.parseLong(time);
        }
        final boolean lastoncpu = booleanArgValue(args, LASTONCPU);
        final boolean create = booleanArgValue(args, CREATEINFO);
        final String id = stringArgValue(args, THREAD_ID);
        checkTimeFormat(args);

        if (id == null) {
            for (CreateThreadTraceElement te : CreateThreadTraceElement.getThreadIterable()) {
                if (lastoncpu) {
                    processLastOnCpu(traces, te.getId());
                } else if (create) {
                    processCreateInfo(traces, te.getId());
                } else {
                    processTime(traces, te.getId(), endTime);
                }
            }
        } else {
            if (lastoncpu) {
                processLastOnCpu(traces, Integer.parseInt(id));
            } else if (create) {
                processCreateInfo(traces, Integer.parseInt(id));
            } else {
                processTime(traces, Integer.parseInt(id), endTime);
            }
        }

    }

    private void processTime(List<TraceElement> traces, int id, long endTime) {
        final List<ThreadStateInterval> history = ThreadStateInterval.createIntervals(traces, id);
        final Match match = findMatch(history, id, endTime);
        ThreadStateInterval tsi = match._match;
        if (tsi == null) {
            if (endTime == Long.MAX_VALUE && match._last != null) {
                tsi = match._last;
            }
        }
        System.out.println("Thread " + id + " state at " + endTime + " is " + (tsi == null ? "NON_EXISTENT" : tsi));
    }

    static class Match {
        ThreadStateInterval _match;
        ThreadStateInterval _last;
    }

    private Match findMatch(List<ThreadStateInterval> history, int id, long endTime) {
        final Match result = new Match();
        for (ThreadStateInterval tsi : history) {
            if (endTime >= tsi.getStart() && endTime <= tsi.getEnd()) {
                result._match = tsi;
                break;
            }
            result._last = tsi;
        }
        return result;
    }

    private void processLastOnCpu(List<TraceElement> traces, int id) {
        final List<ThreadStateInterval> history = ThreadStateInterval.createIntervals(traces, id);
        final int endIndex = history.size() - 1;
        for (int i = endIndex; i >= 0; i--) {
            final ThreadStateInterval tsi = history.get(i);
            if (tsi.getState() == ThreadState.ONCPU) {
                System.out.println("Thread " + id + " last ONCPU at " + TimeFormat.byKind(tsi.getEnd(), _timeFormat));
                return;
            }
        }
        System.out.println("Thread " + id + " never ONCPU");
    }

    private void processCreateInfo(List<TraceElement> traces, int id) {
        for (int i = 0; i < traces.size(); i++) {
            final TraceElement t = traces.get(i);
            if (t.getTraceKind() == TraceKind.CT) {
                final CreateThreadTraceElement ct = (CreateThreadTraceElement) t;
                if (ct.getId() == id) {
                    System.out.println(ct);
                    // what was the state of the CPUs and other threads at this point?
                    for (int cpu = 0; cpu < TraceElement.getCpus(); cpu++) {
                        System.out.println("CPU " + cpu + " " + getCPUState(traces, cpu, i).name());
                    }
                    for (CreateThreadTraceElement ctx : CreateThreadTraceElement.getThreadIterable()) {
                        if (ctx != ct) {
                            final List<ThreadStateInterval> history = ThreadStateInterval.createIntervals(traces, ctx.getId());
                            final Match match = findMatch(history, ctx.getId(), ct.getTimestamp());
                            if (match._match != null) {
                                System.out.println("Thread " + ctx.getId() + " is " + match._match.getState());
                            }
                        }
                    }

                }
            }
        }
    }

    private CPUState getCPUState(List<TraceElement> traces, int cpu, int index) {
        for (int i = index; i > 0; i--) {
            final TraceElement t = traces.get(i);
            if ((t.getTraceKind() == TraceKind.WI || t.getTraceKind() == TraceKind.RI) && t.getCpu() == cpu) {
                return CPUState.RUNNING;

            } else if (t.getTraceKind() == TraceKind.BI && t.getCpu() == cpu) {
                return CPUState.IDLE;
            }
        }
        return CPUState.UNKNOWN;
    }

}
