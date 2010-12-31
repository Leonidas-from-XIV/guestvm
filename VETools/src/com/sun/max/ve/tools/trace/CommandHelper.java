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
package com.sun.max.ve.tools.trace;


public class CommandHelper {

    protected static TimeFormat.Kind _timeFormat;

    public static String stringArgValue(String[] args, String arg) {
        String result = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith(arg)) {
                result = args[i].substring(arg.length());
                break;
            }
        }
        return result;
    }

    public static boolean booleanArgValue(String[] args, String arg) {
        boolean result = false;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(arg)) {
                result = true;
                break;
            }
        }
        return result;
    }

    protected void checkTimeFormat(String[] args) {
        _timeFormat = TimeFormat.checkFormat(args);
    }
}
