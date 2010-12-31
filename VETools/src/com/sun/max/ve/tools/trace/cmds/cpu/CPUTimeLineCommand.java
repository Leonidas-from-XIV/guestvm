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
package com.sun.max.ve.tools.trace.cmds.cpu;

import java.util.*;

import com.sun.max.ve.tools.trace.CPUState;
import com.sun.max.ve.tools.trace.CPUStateDuration;
import com.sun.max.ve.tools.trace.Command;
import com.sun.max.ve.tools.trace.CommandHelper;
import com.sun.max.ve.tools.trace.TimeFormat;
import com.sun.max.ve.tools.trace.TraceElement;
import com.sun.max.ve.tools.trace.TraceKind;
import com.sun.max.ve.tools.trace.TraceMain;
import com.sun.max.ve.tools.trace.TraceReader;


public class CPUTimeLineCommand extends CommandHelper implements Command {
    private static final String SUMMARY = "summary";

    @Override
    public void doIt(List<TraceElement> traces, String[] args) throws Exception {
        final boolean summary = booleanArgValue(args, SUMMARY);
        final int cpu = TraceMain.CPU.getValue();
        checkTimeFormat(args);
        if (cpu < 0) {
            for (int c = 0; c < TraceElement.getCpus(); c++) {
                process(traces, c, summary);
            }
        } else {
            process(traces, cpu, summary);
        }

    }

    private void process(List<TraceElement> traces, int id, boolean summary) {
        final List<CPUStateDuration> history = new ArrayList<CPUStateDuration>();
        CPUStateDuration current = null;
        for (TraceElement trace : traces) {
            if (trace.getTraceKind() == TraceKind.RI) {
                // first execution of the idle thread
                if (trace.getCpu() == id) {
                    current = newCPUStateDuration(current, CPUState.RUNNING, trace, history);
                }
            } else if (trace.getTraceKind() == TraceKind.BI) {
                // block idle thread
                if (trace.getCpu() == id) {
                    current = newCPUStateDuration(current, CPUState.IDLE, trace, history);
                }
            } else if (trace.getTraceKind() == TraceKind.WI) {
                // wake idle thread
                if (trace.getCpu() == id) {
                    current = newCPUStateDuration(current, CPUState.RUNNING, trace, history);
                }
            }
        }
        if (current.getStop() == 0) {
            current.setStop(TraceReader.lastTrace().getTimestamp());
        }

        final long[] totals = new long[CPUState.values().length];
        for (int i = 0; i < totals.length; i++) {
            totals[i] = 0;
        }

        System.out.println("Timeline for CPU " + id);
        for (CPUStateDuration sd : history) {
            if (!summary) {
                System.out.print(sd.getState().name());
                System.out.println(" " + sd.getStart() + " " + sd.getStop() + " (" + (sd.getStop() - sd.getStart()) + ")");
            }
            totals[sd.getState().ordinal()] += sd.getStop() - sd.getStart();
        }
        System.out.print("Summary:");
        System.out.print(" RUNNING " + TimeFormat.byKind(totals[CPUState.RUNNING.ordinal()], _timeFormat));
        System.out.println(" IDLE " + TimeFormat.byKind(totals[CPUState.IDLE.ordinal()], _timeFormat));
    }

    private CPUStateDuration newCPUStateDuration(CPUStateDuration oldCPUStateDuration, CPUState state, TraceElement traceElement, List<CPUStateDuration> history) {
        if (oldCPUStateDuration != null) {
            oldCPUStateDuration.setStop(traceElement.getTimestamp());
        }
        final CPUStateDuration result = new CPUStateDuration(state, traceElement.getTimestamp());
        history.add(result);
        return result;
    }

}
