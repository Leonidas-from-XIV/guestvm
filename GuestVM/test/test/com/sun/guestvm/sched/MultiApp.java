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
package test.com.sun.guestvm.sched;

/**
 * Runs multiple "apps", i.e., tests, each as a separate thread. Useful for testing the scheduler.
 *
 * @author Mick Jordan
 *
 */

import java.lang.reflect.*;
import java.util.*;

public class MultiApp {

    /**
     * @param args
     */
    public static void main(String[] args) {
        final List<App> apps = new ArrayList<App>();
        App app = null;
        // Checkstyle: stop modified control variable check
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("app")) {
                app = new App(args[++i]);
                apps.add(app);
            } else {
                app.addArg(arg);
            }
        }
        // Checkstyle: resume modified control variable check

        for (App a : apps) {
            a.start();
        }
        for (App a : apps) {
            a.join();
        }
    }

    static class App implements Runnable {
        String _className;
        List<String> _args = new ArrayList<String>();
        Thread _thread;

        App(String className) {
            _className = className;
        }

        void addArg(String arg) {
            _args.add(arg);
        }

        void start() {
            _thread = new Thread(this);
            _thread.setName("App:" + _className);
            _thread.start();
        }

        void join() {
            try {
                _thread.join();
            } catch (InterruptedException ex) {

            }
        }

        public void run() {
            final String[] args = _args.toArray(new String[_args.size()]);
            try {
                final Class<?> klass = Class.forName(_className);
                final Method main = klass.getDeclaredMethod("main", String[].class);
                main.invoke(null, new Object[]{args});
            } catch (Exception ex) {
                System.err.println("failed to run app " + _className);
                ex.printStackTrace();
            }
        }
    }

}
