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
package com.sun.guestvm.process;

import java.io.*;
import java.util.*;

import com.sun.max.program.*;

/**
 * A class that manages classes that act ass filters for exec calls..
 *
 * @author Mick Jordan
 *
 */
public class GuestVMProcess {
    private static final String PROCESS_FILTER_CLASS_PROPERTY = "guestvm.process.filterclasses";
    private static Map<String, GuestVMProcessFilter> _filters = new HashMap<String, GuestVMProcessFilter>();
    private static boolean _init = false;

    public static GuestVMProcessFilter filter(byte[] prog) {
        init();
        return _filters.get(stripNull(prog));
    }

    private static String stripNull(byte[] prog) {
        /* prog is null terminated, but String will treat the null as a character. */
        return new String(prog, 0, prog.length - 1);
    }

    private static void init() {
        if (!_init) {
            final String prop = System.getProperty(PROCESS_FILTER_CLASS_PROPERTY);
            if (prop != null) {
                final String[] classNames = prop.split(",");
                for (String className : classNames) {
                    try {
                        final Class<?> klass = Class.forName(className);
                        final GuestVMProcessFilter filter = (GuestVMProcessFilter) klass.newInstance();
                        for (String name : filter.names()) {
                            _filters.put(name, filter);
                        }
                    } catch (Exception ex) {
                        ProgramError.unexpected("failed to load process filter class: " + prop);
                    }
                }
            }
            _init = true;
        }
    }
}
