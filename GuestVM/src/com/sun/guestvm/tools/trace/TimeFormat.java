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

import java.text.*;

public class TimeFormat {
    private static final String FORMAT = "format=";

    public static enum Kind {
        SECONDS {
            public String convert(long nanos) {
                return divNd(nanos, 1000000000, 9);
            }
        },

        MILLIS {
            public String convert(long nanos) {
                return divNd(nanos, 1000000, 6);
            }
        },

        MICROS {
            public String convert(long nanos) {
                return divNd(nanos, 1000, 3);
            }
        },

        NANOS {
            public String convert(long nanos) {
                return Long.toString(nanos);
            }
        };

        public String convert(long nanos) {
            return null;
        }

    }

    public static Kind checkFormat(String[] args) {
        final String s = CommandHelper.stringArgValue(args, FORMAT);
        if (s == null) {
            return Kind.NANOS;
        } else {
            return Kind.valueOf(s);
        }
    }

    public static String byKind(long nanos, Kind kind) {
        return kind.convert(nanos);
    }

    public static String seconds(long nanos) {
        return TimeFormat.byKind(nanos, Kind.SECONDS);
    }

    public static String millis(long nanos) {
        return TimeFormat.byKind(nanos, Kind.MILLIS);
    }

    public static String micros(long nanos) {
        return TimeFormat.byKind(nanos, Kind.MICROS);
    }

    public static String div2d(long a, long b) {
        return divNd(a, b, 2);
    }

    public static String div3d(long a, long b) {
        return divNd(a, b, 3);
    }

    private static final DecimalFormat[] formats = new DecimalFormat[10];

    static {
        final StringBuilder format = new StringBuilder("###,###,###.");
        for (int i = 0; i < formats.length; i++) {
            formats[i] = new DecimalFormat(format.toString());
            format.append('#');
        }
    }

    public static String divNd(long a, long b, int n) {
        return formats[n].format((double) a / (double) b);
    }

}
