/*
 * Copyright (c) 2010 Sun Microsystems, Inc., 4150 Network Circle, Santa
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
package test.java.lang.management;

import java.lang.management.*;
import java.lang.reflect.*;
import java.util.*;

public class MXTest {
    private static Map<String, Info> _commands = new HashMap<String, Info>();
    /**
     * @param args
     */
    public static void main(String[] args) {
        final String[] ops = new String[10];
        final String[] opArgs1 = new String[10];
        final String[] opArgs2 = new String[10];
        int opCount = 0;
        // Checkstyle: stop modified control variable check
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.equals("a") || arg.equals("a1")) {
                opArgs1[opCount] = args[++i];
            } else if (arg.equals("a2")) {
                opArgs2[opCount] = args[++i];
            } else if (arg.equals("op")) {
                ops[opCount++] = args[++i];
                opArgs1[opCount] = opArgs1[opCount - 1];
                opArgs2[opCount] = opArgs2[opCount - 1];
            }
        }
        // Checkstyle: resume modified control variable check

        if (opCount == 0) {
            System.out.println("no operations given");
            return;
        }

        try {
            enterMXBeanCommands("R", RuntimeMXBean.class, ManagementFactory.getRuntimeMXBean());
            enterMXBeanCommands("T", ThreadMXBean.class, ManagementFactory.getThreadMXBean());
            enterMXBeanCommands("OS", OperatingSystemMXBean.class, ManagementFactory.getOperatingSystemMXBean());
            enterMXBeanCommands("M", MemoryMXBean.class, ManagementFactory.getMemoryMXBean());
            enterMXBeanCommands("MP", MemoryPoolMXBean.class, ManagementFactory.getMemoryPoolMXBeans().get(0));
            enterMXBeanCommands("MM", MemoryManagerMXBean.class, ManagementFactory.getMemoryManagerMXBeans().get(0));
            enterMXBeanCommands("GC", GarbageCollectorMXBean.class, ManagementFactory.getGarbageCollectorMXBeans().get(0));
            enterMXBeanCommands("CC", CompilationMXBean.class, ManagementFactory.getCompilationMXBean());
            enterMXBeanCommands("CL", ClassLoadingMXBean.class, ManagementFactory.getClassLoadingMXBean());

            for (int j = 0; j < opCount; j++) {
                final String opArg1 = opArgs1[j];
                final String opArg2 = opArgs2[j];
                final String op = ops[j];

                final Info info = _commands.get(op);
                if (info == null) {
                    throw new Exception("method " + op + "  not found");
                }
                System.out.println("invoking " + op);
                final Object result = info._m.invoke(info._bean);
                System.out.println(result);
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }

    }

    static class Info {
        Object _bean;
        Method _m;
        Info(Object bean, Method m) {
            _bean = bean;
            _m = m;
        }

    }

    private static void enterMXBeanCommands(String prefix, Class<?> klass, Object mxbean) {
        final Method[] methods = klass.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            final String defaultCommandName = prefix + "." + methods[i].getName();
            String commandName = defaultCommandName;
            int suffix = 1;
            while (_commands.get(commandName) != null) {
                commandName = defaultCommandName + "_" + suffix++;
            }
            System.out.println("entering " + commandName);
            _commands.put(commandName, new Info(mxbean, methods[i]));
        }
    }

}
