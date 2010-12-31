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

import java.util.*;

import com.sun.max.ve.tools.trace.Command;
import com.sun.max.ve.tools.trace.CommandHelper;
import com.sun.max.ve.tools.trace.CreateThreadTraceElement;
import com.sun.max.ve.tools.trace.ThreadState;
import com.sun.max.ve.tools.trace.ThreadStateInterval;
import com.sun.max.ve.tools.trace.TimeFormat;
import com.sun.max.ve.tools.trace.TraceElement;

/**
 * This command outputs the timeline of a thread's state from creation.
 * E.g. a line of the form :
 *   ONCPU cpu a b
 * means the thread was running on CPU cpu from a to b.
 * A summary of the total time in each state is given at the end.
 * If summary argument is given, only the summary is output.
 * @author Mick Jordan
 *
 */
public class ThreadTimeLineCommand extends CommandHelper implements Command {
    private static final String THREAD_ID = "id=";
    private static final String SUMMARY = "summary";

    @Override
    public void doIt(List<TraceElement> traces, String[] args) throws Exception {
        final String id = stringArgValue(args, THREAD_ID);
        final boolean summary = booleanArgValue(args, SUMMARY);
        checkTimeFormat(args);
        if (id == null) {
            for (CreateThreadTraceElement te : CreateThreadTraceElement.getThreadIterable()) {
                process(traces, te.getId(), summary);
            }
        } else {
            process(traces, Integer.parseInt(id), summary);
        }
    }

    private void process(List<TraceElement> traces, int id, boolean summary) {
        final List<ThreadStateInterval> history = ThreadStateInterval.createIntervals(traces, id);
        final long[] totals = new long[ThreadState.values().length];
        for (int i = 0; i < totals.length; i++) {
            totals[i] = 0;
        }
        if (!summary) {
            System.out.println("\nTimeline for thread " + id);
        }
        for (ThreadStateInterval threadStateInterval : history) {
            if (!summary) {
                System.out.print(threadStateInterval.getState().name());
                if (threadStateInterval.getState() == ThreadState.ONCPU) {
                    System.out.print(" " + threadStateInterval.getCpu());
                }
                System.out.println(" " + threadStateInterval.getStart() + " " + threadStateInterval.getEnd() + " (" + (threadStateInterval.getEnd() - threadStateInterval.getStart()) + ")");
            }
            totals[threadStateInterval.getState().ordinal()] += threadStateInterval.getEnd() - threadStateInterval.getStart();
        }
        System.out.print("Totals for thread " + id);
        System.out.print(": TOTAL " + TimeFormat.byKind(ThreadStateInterval.intervalLength(history), _timeFormat));
        System.out.print(", ONCPU " + TimeFormat.byKind(totals[ThreadState.ONCPU.ordinal()], _timeFormat));
        System.out.print(", OFFCPU " + TimeFormat.byKind(totals[ThreadState.OFFCPU.ordinal()], _timeFormat));
        System.out.print(", BLOCKED " + TimeFormat.byKind(totals[ThreadState.BLOCKED.ordinal()], _timeFormat));
        System.out.println(", INSCHED " + TimeFormat.byKind(totals[ThreadState.INSCHED.ordinal()], _timeFormat));
        final long extra = unaccounted(totals, ThreadStateInterval.intervalLength(history));
        if (extra != 0) {
            System.out.println("** UNACCOUNTED: " + extra);
        }
    }


    private long unaccounted(long[] totals, long total) {
        final long sum = totals[ThreadState.ONCPU.ordinal()] + totals[ThreadState.OFFCPU.ordinal()] +
            totals[ThreadState.BLOCKED.ordinal()] + totals[ThreadState.INSCHED.ordinal()];
        return total - sum;
    }
}
