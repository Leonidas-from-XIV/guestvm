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
package com.sun.guestvm.tools.trace.cmds.memory;

import java.util.List;

import com.sun.guestvm.tools.trace.AllocPagesTraceElement;
import com.sun.guestvm.tools.trace.AllocTraceElement;
import com.sun.guestvm.tools.trace.Command;
import com.sun.guestvm.tools.trace.CommandHelper;
import com.sun.guestvm.tools.trace.CreateThreadTraceElement;
import com.sun.guestvm.tools.trace.TraceElement;
import com.sun.guestvm.tools.trace.TraceKind;

/**
 * Show mallocs that allocate pages.
 *
 * @author Mick Jordan
 *
 */

public class MallocPageCommand extends CommandHelper implements Command {
    private static final String THREAD_ID = "id=";

    @Override
    public void doIt(List<TraceElement> traces, String[] args) throws Exception {
        final String id = stringArgValue(args, THREAD_ID);
        if (id == null) {
            for (CreateThreadTraceElement te : CreateThreadTraceElement.getThreadIterable(true)) {
                process(traces, te.getId());
            }
        } else {
            process(traces, Integer.parseInt(id));
        }

    }

    private void process(List<TraceElement> traces, int id) {
        int ix = 0;
        while (ix < traces.size()) {
            final TraceElement trace = traces.get(ix);
            if (trace instanceof AllocTraceElement) {
                final AllocTraceElement atrace = (AllocTraceElement) trace;
                // Looking for allocations by a particular thread
                if (atrace.getThread() == id && (atrace.getTraceKind() == TraceKind.AME)) {
                    final int ex = AllocByThreadCommand.findMatch(traces, atrace.getTraceKind(), ix);
                    // check for an intervening APE
                    ix++;
                    while (ix < ex) {
                        final TraceElement xtrace = traces.get(ix);
                        if (xtrace.getThread() == id && (xtrace.getTraceKind() == TraceKind.APE)) {
                            final AllocPagesTraceElement aptrace = (AllocPagesTraceElement) xtrace;
                            System.out.println(atrace);
                            System.out.println(aptrace + "(" + aptrace.getPages() * AllocPagesTraceElement.getPageSize() + ")");
                        }
                        ix++;
                    }
                }
            }
            ix++;
        }

    }

}
