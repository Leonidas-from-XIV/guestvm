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

import javax.management.remote.*;
import javax.management.*;

import com.sun.tools.attach.*;
import sun.tools.attach.*;
import test.util.*;

public class ManageAttachTest {

    private static String _jarFile = "/max.ve/java/jdk1.6.0_20/jre/lib/management-agent.jar";
    private static String _agentProps;
    private static String _host = "javaguest7";

    /**
     * @param args
     */
    public static void main(String[] args) {
        final ArgsHandler h = ArgsHandler.process(args);
        if (h._opCount == 0) {
            System.out.println("no operations given");
            return;
        }
        final VirtualMachineDescriptor vmd = AttachTest.findMaxVEVMD();
        assert vmd != null;
        try {
            final VirtualMachine vm = vmd.provider().attachVirtualMachine(vmd);
            MBeanServerConnection mbc = null;
            String agentProps = null;

            for (int j = 0; j < h._opCount; j++) {
                final String opArg1 = h._opArgs1[j];
                final String opArg2 = h._opArgs2[j];
                final String op = h._ops[j];

                if (op.equals("port")) {
                    addArg("com.sun.management.jmxremote.port=" + opArg1);
                } else if (op.equals("ssl")) {
                    addArg("com.sun.management.jmxremote.ssl=" + opArg1);
                } else if (op.equals("auth")) {
                    addArg("com.sun.management.jmxremote.authenticate=" + opArg1);
                } else if (op.equals("localonly")) {
                    addArg("com.sun.management.jmxremote.local.only=" + opArg1);
                } else if (op.equals("connect")) {
                    vm.loadAgent(_jarFile, _agentProps);
                    final JMXServiceURL u = new JMXServiceURL(
                                    "service:jmx:rmi:///jndi/rmi://" + _host + ":" + opArg1 + "/jmxrmi");
                    System.out.println("connecting to: " + u.toString());
                    final JMXConnector c = JMXConnectorFactory.connect(u);
                    System.out.println("connector id: " + c.getConnectionId());
                    mbc = c.getMBeanServerConnection();

                } else if (op.equals("names")) {
                    final Set<ObjectName> objectNames = mbc.queryNames(null, null);
                    System.out.println("ObjectNames");
                    for (ObjectName objectName : objectNames) {
                        System.out.println(objectName.toString());
                    }
                } else if (op.equals("instances")) {
                    final Set<ObjectInstance> objectInstances = mbc.queryMBeans(null, null);
                    for (ObjectInstance objectInstance : objectInstances) {
                        System.out.println(objectInstance.toString());
                    }
                } else if (op.equals("namepattern")) {
                    final Set<ObjectName> objectNames = mbc.queryNames(new ObjectName(opArg1), null);
                    System.out.println("ObjectNames");
                    for (ObjectName objectName : objectNames) {
                        System.out.println(objectName.toString());
                    }
                } else if (op.equals("invoke")) {
                    final ObjectName objectName = new ObjectName(opArg1);
                    try {
                        final Object result = mbc.invoke(objectName, opArg2, new Object[0], new String[0]);
                        System.out.println(result);
                    } catch (Exception ex) {
                        System.out.println(ex);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private static void addArg(String arg) {
        if (_agentProps == null) {
            _agentProps = arg;
        } else {
            _agentProps += "," + arg;
        }
    }
}
