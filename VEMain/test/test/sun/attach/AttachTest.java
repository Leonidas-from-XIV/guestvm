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
package test.sun.attach;

import java.util.*;

import com.sun.tools.attach.*;
import sun.tools.attach.*;
import test.util.*;


public class AttachTest {

    private static boolean _verbose;
    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        final ArgsHandler h = ArgsHandler.process(args);
        if (h._opCount == 0) {
            System.out.println("no operations given");
            return;
        }
        _verbose = h._verbose;
        final VirtualMachineDescriptor vmd = findMaxVEVMD();
        assert vmd != null;
        VirtualMachine vm = null;

        for (int j = 0; j < h._opCount; j++) {
            final String opArg1 = h._opArgs1[j];
            final String opArg2 = h._opArgs2[j];
            final String op = h._ops[j];

            try {
                if (op.equals("attach")) {
                    vm = vmd.provider().attachVirtualMachine(vmd);
                } else if (op.equals("getSystemProperties")) {
                    final Properties sysProps = vm.getSystemProperties();
                    for (Map.Entry<Object, Object> entry : sysProps.entrySet()) {
                        System.out.print(entry.getKey());
                        System.out.print('=');
                        System.out.println(entry.getValue());
                    }
                } else if (op.equals("loadAgent")) {
                    vm.loadAgent(opArg1, opArg2);
                    System.out.println("agent: " + opArg1 + " loaded with arg: " + opArg2);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    static VirtualMachineDescriptor findMaxVEVMD() {
        final List<VirtualMachineDescriptor> list = VirtualMachine.list();
        for (VirtualMachineDescriptor vmd : list) {
            if (_verbose) {
                System.out.println(vmd.toString());
            }
            if (vmd.provider() instanceof MaxVEAttachProvider) {
                return vmd;
            }
        }
        return null;
    }
}
