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
import com.sun.guestvm.tools.trace.Command;
import com.sun.guestvm.tools.trace.CommandHelper;
import com.sun.guestvm.tools.trace.TraceElement;


public class PagePoolCommand extends CommandHelper implements Command {

    @Override
    public void doIt(List<TraceElement> traces, String[] args) throws Exception {
        int maxHwm = 0;
        for (TraceElement te : traces) {
            if (te instanceof AllocPagesTraceElement) {
                final AllocPagesTraceElement apte = (AllocPagesTraceElement) te;
                if (apte.getHwmAllocPage() > maxHwm) {
                    maxHwm = apte.getHwmAllocPage();
                }
            }
        }
        System.out.println("max hwm " + maxHwm);
    }

}
