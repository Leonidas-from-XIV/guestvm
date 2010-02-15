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
package com.sun.guestvm.attach;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.*;
import java.util.*;
import java.util.jar.JarFile;

import sun.misc.Launcher;

import com.sun.guestvm.error.GuestVMError;
import com.sun.max.vm.run.java.JavaRunScheme;

/**
 * Support for attaching to this VM.
 * We run a thread that listens on a socket.
 *
 * @author Mick Jordan
 *
 */
public final class AttachListener implements Runnable {
    private static AttachListener _attachListener;
    private static final String DEBUG_PROPERTY = "guestvm.attach.debug";
    private static boolean _debug;

    private AttachListener() {
        _debug = System.getProperty(DEBUG_PROPERTY) != null;
        final Thread attachListenerThread = new Thread(this, "Attach Listener");
        attachListenerThread.setDaemon(true);
        attachListenerThread.start();
    }

    public static void create() {
        if (_attachListener == null) {
            _attachListener = new AttachListener();
        }
    }

    public void run() {
        try {
            final ServerSocket attachSocket = new ServerSocket(AttachPort.getPort());
            // each command is a separate accept, as the close is the signal to the client
            for (;;) {
                InputStream in = null;
                OutputStream out = null;
                try {
                    final Socket sock = attachSocket.accept();
                    System.out.println("connection accepted on " + sock.getLocalPort() + " from " + sock.getInetAddress());
                    in = sock.getInputStream();
                    out = sock.getOutputStream();
                    // read a command
                    final int numArgs = in.read();
                    final String[] args = new String[numArgs];
                    for (int i = 0; i < numArgs; i++) {
                        args[i] = readString(in);
                    }
                    debug(args);
                    final String cmd = args[0];
                    if (cmd.equals("properties")) {
                        final Map<String, String> sysProps = ManagementFactory.getRuntimeMXBean().getSystemProperties();
                        for (Map.Entry<String, String> entry : sysProps.entrySet()) {
                            writeString(out, entry.getKey());
                            out.write('=');
                            writeString(out, entry.getValue());
                            out.write('\n');
                        }
                    } else if (cmd.equals("load")) {
                        // load jarpath isabsolute options
                        final String jarPath = args[1];
                        final String isAbsolute = args[2];
                        final String options = args[3];
                        JarFile jarFile = null;
                        char error = '0';
                        try {
                            jarFile = new JarFile(jarPath);
                            final String agentClassName = JavaRunScheme.findClassAttributeInJarFile(jarFile, "Agent-Class");
                            if (agentClassName == null) {
                                error = '1';
                            } else {
                                final URL url = new URL("file://" + jarPath);
                                Launcher.addURLToAppClassLoader(url);
                                JavaRunScheme.invokeAgentMethod(url, agentClassName, "agentmain", options);
                            }
                        } catch (Exception ex) {
                            System.out.println(ex);
                            ex.printStackTrace();
                            error = '2';
                        } finally {
                            if (jarFile != null) {
                                jarFile.close();
                            }
                        }
                        out.write(error);
                    }
                } catch (IOException ex) {
                    // just abandon this command
                } finally {
                    if (in != null) {
                        in.close();
                    }
                    if (out != null) {
                        out.close();
                    }
                }
            }
        } catch (IOException ex) {
            GuestVMError.unexpected("attach listener failed");
        }
    }

    private static String readString(InputStream in) throws IOException {
        final int length = in.read();
        if (length == 0) {
            return null;
        }
        final byte[] b = new byte[length];
        in.read(b);
        return new String(b, "UTF-8");
    }

    private static void writeString(OutputStream out, String s)  throws IOException {
        final byte[] b = s.getBytes("UTF-8");
        out.write(b);
    }

    private static void debug(String[] args) {
        if (_debug) {
            System.out.print("execute:");
            for (Object arg : args) {
                System.out.print(" ");
                System.out.print(arg);
            }
            System.out.println();
        }
    }
}
