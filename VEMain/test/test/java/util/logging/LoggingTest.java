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
package test.java.util.logging;

import java.util.logging.*;

public class LoggingTest {

    private static Level[] _levels = new Level[] {Level.SEVERE, Level.WARNING, Level.INFO, Level.CONFIG, Level.FINE, Level.FINER, Level.FINEST};
    /**
     * @param args
     */
    public static void main(String[] args) {
        final Logger globalLogger = globalLogger();
        showLevel(globalLogger);
        final Logger myLoggerA = Logger.getLogger("LoggingTest.A");
        showLevel(myLoggerA);
        final Logger myLoggerB = Logger.getLogger("LoggingTest.B");
        showLevel(myLoggerB);
        iterate(globalLogger);
        iterate(myLoggerA);
        iterate(myLoggerB);
    }

    private static Logger globalLogger() {
        return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    }
    
    private static void iterate(Logger logger) {
        for (Level level : _levels) {
            logger.log(level, "Logger \"" + logger.getName() + "\" logging at level " + level);
        }
    }
    
    private static void showLevel(Logger logger) {
        System.out.println("Logger \"" + logger.getName() +  "\" level is " + logger.getLevel());
    }

}
