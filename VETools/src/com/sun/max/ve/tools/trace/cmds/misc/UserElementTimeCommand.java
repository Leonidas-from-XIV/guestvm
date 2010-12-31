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
package com.sun.max.ve.tools.trace.cmds.misc;

import java.util.*;

import com.sun.max.ve.tools.trace.Command;
import com.sun.max.ve.tools.trace.CommandHelper;
import com.sun.max.ve.tools.trace.TimeFormat;
import com.sun.max.ve.tools.trace.TraceElement;
import com.sun.max.ve.tools.trace.TraceKind;
import com.sun.max.ve.tools.trace.UserTraceElement;

/**
 * Command to collect durations between pairs of user traces of the form NAME_ENTER and NAME_EXIT.
 * N.B. Nested or unbalanced pairs are not handled correctly at present.
 *
 * @author Mick Jordan
 *
 */

public class UserElementTimeCommand extends CommandHelper implements Command {
    private static final String USER_TRACE = "user=";

    @Override
    public void doIt(List<TraceElement> traces, String[] args) throws Exception {
        final List<Long> durations = new ArrayList<Long>();
        final String userTrace = stringArgValue(args, USER_TRACE);
        if (userTrace == null) {
            throw new Exception("user trace name missing");
        }
        final TimeFormat.Kind kind = TimeFormat.checkFormat(args);
        final String enterName = userTrace + "_ENTER";
        final String exitName = userTrace + "_EXIT";

        // This does not handle nesting.
        long start = 0;
        for (TraceElement trace : traces) {
            if (trace.getTraceKind() == TraceKind.USER) {
                final UserTraceElement utrace = (UserTraceElement) trace;
                if (utrace.getName().equals(enterName)) {
                    start = utrace.getTimestamp();
                } else if (utrace.getName().equals(exitName)) {
                    durations.add(utrace.getTimestamp() - start);
                    start = 0;
                }
            }
        }

        long sum = 0;
        long max = 0;
        long min = Long.MAX_VALUE;
        for (Long duration : durations) {
            sum += duration;
            if (duration > max) {
                max = duration;
            }
            if (duration < min) {
                min = duration;
            }
        }
        System.out.println("max: " + TimeFormat.byKind(max, kind) + ", min: " + TimeFormat.byKind(min, kind) + ", avg: " + TimeFormat.byKind(sum / durations.size(), kind));

    }

}
