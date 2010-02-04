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
    private static boolean _verbose;
    /**
     * @param args
     */
    public static void main(String[] args) {
        final String[] ops = new String[10];
        final String[] opArgs1 = new String[10];
        final String[] opArgs2 = new String[10];
        final String[] type1 = new String[10];
        final String[] type2 = new String[10];
        int opCount = 0;
        for (int i = 0; i < ops.length; i++) {
            type1[i] = "s";
            type2[i] = "s";
        }
        // Checkstyle: stop modified control variable check
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.equals("a1")) {
                opArgs1[opCount] = args[++i];
            } else if (arg.equals("a2")) {
                opArgs2[opCount] = args[++i];
            } else if (arg.equals("op")) {
                ops[opCount++] = args[++i];
            } else if (arg.equals("t1")) {
                type1[opCount] = args[++i];
            } else if (arg.equals("t2")) {
                type2[opCount] = args[++i];
            } else if (arg.equals("v")) {
                _verbose = true;
            }
        }
        // Checkstyle: resume modified control variable check

        if (opCount == 0 && !_verbose) {
            System.out.println("no operations given");
            return;
        }

        try {
            // the singletons
            enterMXBeanCommands("R", RuntimeMXBean.class, ManagementFactory.getRuntimeMXBean());
            enterMXBeanCommands("T", ThreadMXBean.class, ManagementFactory.getThreadMXBean());
            enterMXBeanCommands("OS", OperatingSystemMXBean.class, ManagementFactory.getOperatingSystemMXBean());
            enterMXBeanCommands("M", MemoryMXBean.class, ManagementFactory.getMemoryMXBean());
            enterMXBeanCommands("CC", CompilationMXBean.class, ManagementFactory.getCompilationMXBean());
            enterMXBeanCommands("CL", ClassLoadingMXBean.class, ManagementFactory.getClassLoadingMXBean());
            // these calls can all return multiple instances
            final List<MemoryManagerMXBean> theMemoryManagerMXBeans = ManagementFactory.getMemoryManagerMXBeans();
            for (int i = 0; i < theMemoryManagerMXBeans.size(); i++) {
                enterMXBeanCommands("MM", MemoryManagerMXBean.class, i, theMemoryManagerMXBeans.get(i));
            }
            final List<GarbageCollectorMXBean> theGarbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
            for (int i = 0; i < theGarbageCollectorMXBeans.size(); i++) {
                enterMXBeanCommands("GC", GarbageCollectorMXBean.class, i, theGarbageCollectorMXBeans.get(i));
            }
            final List<MemoryPoolMXBean> theMemoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
            for (int i = 0; i < theMemoryPoolMXBeans.size(); i++) {
                enterMXBeanCommands("MP", MemoryPoolMXBean.class, i, theMemoryPoolMXBeans.get(i));;
            }

            for (int j = 0; j < opCount; j++) {
                final String opArg1 = opArgs1[j];
                final String opArg2 = opArgs2[j];
                final String op = ops[j];

                final Info info = _commands.get(op);
                if (info != null) {
                    System.out.println("invoking " + op);
                    try {
                        final Object[] opArgs = opArg1 == null ? null : (opArg2 == null ? new Object[1] : new Object[2]);
                        if (opArg1 != null) {
                            opArgs[0] = processType(opArg1, type1[j]);
                        }
                        if (opArg2 != null) {
                            opArgs[1] = processType(opArg2, type2[j]);
                        }
                        final Object result = info._m.invoke(info._bean, opArgs);
                        if (result instanceof String[]) {
                            final String[] resultArray = (String[]) result;
                            for (String r : resultArray) {
                                System.out.print(r); System.out.print(" ");
                            }
                            System.out.println();
                        } else if (result instanceof long[]) {
                            final long[] resultArray = (long[]) result;
                            for (long l : resultArray) {
                                System.out.print(l); System.out.print(" ");
                            }
                            System.out.println();
                        } else {
                            System.out.println(result);
                        }
                    } catch (InvocationTargetException ex) {
                        System.out.println(ex);
                        final Throwable cause = ex.getCause();
                        System.out.println(cause);
                        cause.printStackTrace();
                    } catch (Throwable t) {
                        System.out.println(t);
                        t.printStackTrace();
                    }
                } else {
                    if (op.equals("GC")) {
                        System.gc();
                    } else {
                        throw new Exception("method " + op + "  not found");
                    }
                }
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

    private static Object processType(String arg, String type) throws Exception {
        if (type.equals("s")) {
            return arg;
        } else if (type.equals("l")) {
            return Long.parseLong(arg);
        } else if (type.equals("i")) {
            return Integer.parseInt(arg);
        } else if (type.equals("b")) {
            return Boolean.parseBoolean(arg);
        } else {
            throw new Exception("uninterpreted type  " + type);
        }
    }

    private static void enterMXBeanCommands(String prefix, Class<?> klass, Object mxbean) {
        enterMXBeanCommands(prefix, klass, -1, mxbean);
    }

    private static void enterMXBeanCommands(String prefix, Class<?> klass, int x, Object mxbean) {
        final Method[] methods = klass.getDeclaredMethods();
        String prefixNum = x < 0 ? prefix : prefix + "." + x ;
        for (int i = 0; i < methods.length; i++) {
            final String defaultCommandName = prefixNum + "." + methods[i].getName();
            String commandName = defaultCommandName;
            int suffix = 1;
            while (_commands.get(commandName) != null) {
                commandName = defaultCommandName + "_" + suffix++;
            }
            if (_verbose) {
                System.out.println("entering " + commandName + " for " + methods[i].toGenericString());
            }
            _commands.put(commandName, new Info(mxbean, methods[i]));
        }
    }

}
