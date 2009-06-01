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
 * Trace kind definition and parsing methods.
 * Every trace is of the form:
 *   Time CPU Thread TraceKind [args]
 *
 * @author Mick Jordan
 *
 */

public enum TraceKind {

    IS, RI, BI, WI, TI, US,

    CT {

        public TraceElement process(String[] args) {
            final CreateThreadTraceElement traceElement = new CreateThreadTraceElement();
            processPrefix(this, args, traceElement);
            traceElement.setId(Integer.parseInt(args[UAX]));
            traceElement.setName(args[UAX + 1]).setInitialCpu(Integer.parseInt(args[UAX + 2], 16)).setFlags(Integer.parseInt(args[UAX + 3])).setStack(Long.parseLong(args[UAX + 4], 16));
            return traceElement;
        }
    },

    SS {

        public TraceElement process(String[] args) {
            final SetTimerTraceElement traceElement = new SetTimerTraceElement();
            processPrefix(this, args, traceElement);
            traceElement.setTime(Long.parseLong(args[UAX]));
            return traceElement;
        }
    },

    ST {

        public TraceElement process(String[] args) {
            final SetTimerTraceElement traceElement = new SetTimerTraceElement();
            processPrefix(this, args, traceElement);
            traceElement.setTime(Long.parseLong(args[UAX]));
            return traceElement;
        }
    },

    TS {

        public TraceElement process(String[] args) {
            final ThreadSwitchTraceElement traceElement = new ThreadSwitchTraceElement();
            processPrefix(this, args, traceElement);
            traceElement.setFromId(Integer.parseInt(args[UAX])).setToId(Integer.parseInt(args[UAX + 1])).setRunningTime(Long.parseLong(args[UAX + 2])).setSchedStartTime(Long.parseLong(args[UAX + 3]));
            return traceElement;
        }
    },

    PS {

        public TraceElement process(String[] args) {
            return processThreadId(this, args);
        }

    },

    DT {

        public TraceElement process(String[] args) {
            return processThreadId(this, args);
        }

    },

    WK {

        public TraceElement process(String[] args) {
            return processThreadId(this, args);
        }

    },

    TX {

        public TraceElement process(String[] args) {
            return processThreadId(this, args);
        }

    },
    BK {

        public TraceElement process(String[] args) {
            return processThreadId(this, args);
        }

    },

    KC {

        public TraceElement process(String[] args) {
            final SMPTraceElement traceElement = new SMPTraceElement();
            processPrefix(this, args, traceElement);
            traceElement.setCpuCount(Integer.parseInt(args[UAX]));
            return traceElement;
        }
    },

    SMP {

        public TraceElement process(String[] args) {
            final SMPTraceElement traceElement = new SMPTraceElement();
            processPrefix(this, args, traceElement);
            traceElement.setCpuCount(Integer.parseInt(args[UAX]));
            return traceElement;
        }

    },

    // Allocation tracing

    AME {
        public TraceElement process(String[] args) {
            final MAllocTraceElement traceElement = new MAllocTraceElement();
            processPrefix(this, args, traceElement);
            traceElement.setSize(Integer.parseInt(args[UAX]));
            traceElement.setAdjSize(Integer.parseInt(args[UAX + 1]));
            return traceElement;
        }
    },

    AMX {
        public TraceElement process(String[] args) {
            final MAllocTraceElement traceElement = new MAllocTraceElement();
            processAllocPrefix(this, args, traceElement);
            traceElement.setSize(Integer.parseInt(args[UAX + 1]));
            traceElement.setAdjSize(Integer.parseInt(args[UAX + 2]));
            return traceElement;
        }
    },

    API {
        public TraceElement process(String[] args) {
            final PagePoolTraceElement traceElement = new PagePoolTraceElement();
            processPrefix(this, args, traceElement);
            traceElement.setStart(Integer.parseInt(args[UAX]));
            traceElement.setEnd(Integer.parseInt(args[UAX + 1]));
            traceElement.setMax(Integer.parseInt(args[UAX + 2]));
            return traceElement;
        }
    },

    APE {
        public TraceElement process(String[] args) {
            final AllocPagesTraceElement traceElement = new AllocPagesTraceElement();
            processPrefix(this, args, traceElement);
            traceElement.setPages(Integer.parseInt(args[UAX]));
            traceElement.setType(Integer.parseInt(args[UAX + 1]));
            return traceElement;
        }
    },

    APX {
        public TraceElement process(String[] args) {
            final AllocPagesTraceElement traceElement = new AllocPagesTraceElement();
            processAllocPrefix(this, args, traceElement);
            traceElement.setPages(Integer.parseInt(args[UAX + 1]));
            traceElement.setFirstFreePage(Integer.parseInt(args[UAX + 2]));
            traceElement.setHwmAllocPage(Integer.parseInt(args[UAX + 3]));
            return traceElement;
        }
    },

    FME {
        public TraceElement process(String[] args) {
            final FreeTraceElement traceElement = new FreeTraceElement();
            processAllocPrefix(this, args, traceElement);
            return traceElement;
        }

     },

      FMX {
        public TraceElement process(String[] args) {
            final FreeTraceElement traceElement = new FreeTraceElement();
            processAllocPrefix(this, args, traceElement);
            return traceElement;
        }

     },

     FPE {
        public TraceElement process(String[] args) {
            final AllocPagesTraceElement traceElement = new AllocPagesTraceElement();
            processAllocPrefix(this, args, traceElement);
            traceElement.setPages(Integer.parseInt(args[UAX + 1]));
            return traceElement;
        }

     },

     FPX {
        public TraceElement process(String[] args) {
            final AllocPagesTraceElement traceElement = new AllocPagesTraceElement();
            processAllocPrefix(this, args, traceElement);
            traceElement.setPages(Integer.parseInt(args[UAX + 1]));
            return traceElement;
        }

     },

     USER {
        public TraceElement process(String[] args) {
            final UserTraceElement traceElement = new UserTraceElement(args[TKX]);
            processPrefix(this, args, traceElement);
            return traceElement;
        }
     };

    public static final int TKX = 3;
    private static final int UAX = 4;

    public TraceElement process(String[] args) {
        return processPrefix(this, args, new TraceElement());
    }

    private static TraceElement processPrefix(TraceKind traceKind, String[] args, TraceElement traceElement) {
        traceElement.init(traceKind, Long.parseLong(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
        return traceElement;
    }

    private static TraceElement processThreadId(TraceKind traceKind, String[] args) {
        final ThreadIdTraceElement traceElement = new ThreadIdTraceElement();
        processPrefix(traceKind, args, traceElement);
        traceElement.setId(Integer.parseInt(args[UAX]));
        return traceElement;
    }

    private static TraceElement processAllocPrefix(TraceKind traceKind, String[] args, AllocTraceElement traceElement) {
        processPrefix(traceKind, args, traceElement);
        traceElement.setAddress(Long.parseLong(args[UAX], 16));
        return traceElement;
    }

}
