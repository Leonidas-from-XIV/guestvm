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

/**
 * A tool to read and process a trace from the GuestVM microkernel tracing system..
 * The general form of a trace line is:
 *
 * Timestamp CPU Thread Command args
 *
 * @author Mick Jordan
 *
 */

import java.io.*;
import java.util.*;

public class TraceReader {

    private static final Map<String, TraceKind> _traceKindMap = new HashMap<String, TraceKind>();
    private static TraceElement _last;
     /**
     * @param args
     */
    public static List<TraceElement> readTrace() throws Exception {
        java.io.BufferedReader reader = null;
        try {
            reader =  new BufferedReader(new FileReader(TraceMain.getTraceFileName()));
            initialize();
            return processTrace(reader);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                }
            }
        }
    }

    private static List<TraceElement> processTrace(BufferedReader reader) throws Exception {
        final List<TraceElement> result = new ArrayList<TraceElement>();
        int lineCount = 0;
        while (true) {
            final String line = reader.readLine();
            lineCount++;
            if (line == null) {
                break;
            }
            if (line.length() == 0) {
                continue;
            }
            final String[] parts = line.split(" ");
            final String traceName = parts[TraceKind.TKX];
            final TraceKind traceKind = _traceKindMap.get(traceName);

            TraceElement traceElement;
            if (traceKind == null) {
                traceElement = TraceKind.USER.process(parts);
            } else {
                traceElement = traceKind.process(parts);
            }
            result.add(traceElement);
            _last = traceElement;
        }
        return result;
    }

    public static TraceElement lastTrace() {
        return _last;
    }

    private static void initialize() {
        for (TraceKind traceKind : TraceKind.values()) {
            _traceKindMap.put(traceKind.name(), traceKind);
        }

    }

}
