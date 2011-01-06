/*
 * Copyright (c) 2009, 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.sun.max.ve.logging;

import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * We can't use java.util.logging in the low levels of MaxVE because it causes nasty circularities,
 * so we have a much simpler version here.
 *
 * @author Mick Jordan
 *
 */
public class Logger {
    private static final String LEVEL_PROPERTY = "max.ve.logging.level";
    private static final String HANDLER_PROPERTY = "max.ve.logging.handler";
    private static final String DEFAULT_HANDLER = "com.sun.max.ve.logging.MaxineLogHandler";
    private static final int OFFVALUE = Level.OFF.intValue();
    private static Logger _singleton;
    private Level _levelObject = Level.WARNING;;
    private volatile int _levelValue;  // current effective level value
    private Handler _handler;

    private Logger() {
        final String levelName = System.getProperty(LEVEL_PROPERTY);
        if (levelName != null) {
            try {
                _levelObject = Level.parse(levelName);
            } catch (IllegalArgumentException ex) {
                System.err.println("invalid argument " + levelName + " for property max.ve.logging.level");
            }
        }
        _levelValue = _levelObject.intValue();
        String handlerName = System.getProperty(HANDLER_PROPERTY);
        if (handlerName == null) {
            handlerName = DEFAULT_HANDLER;
        }
        try {
            Class< ? > klass = Class.forName(handlerName);
            _handler = (Handler) klass.newInstance();
        } catch (Exception ex) {
            System.err.println("failed to instantiate handler class " + handlerName);
        }
    }

    public static Logger getLogger(String name) {
        if (_singleton == null) {
            _singleton = new Logger();
        }
        return _singleton;
    }

    public boolean isLoggable(Level level) {
        if (level.intValue() < _levelValue || _levelValue == OFFVALUE) {
            return false;
        }
        return true;
    }

    public void log(Level level, String msg) {
        LogRecord lr = new LogRecord(level, msg);
        log(lr);
    }

    public void log(Level level, String msg, Throwable thrown) {
        LogRecord lr = new LogRecord(level, msg);
        lr.setThrown(thrown);
        log(lr);
    }

    private void log(LogRecord record) {
        if (record.getLevel().intValue() < _levelValue || _levelValue == OFFVALUE) {
            return;
        }
        inferCaller(record);
        _handler.publish(record);
    }

    public void warning(String msg) {
        if (Level.WARNING.intValue() < _levelValue) {
            return;
        }
        log(Level.WARNING, msg);
    }

    public void info(String msg) {
        if (Level.INFO.intValue() < _levelValue) {
            return;
        }
        log(Level.INFO, msg);
    }

    public Level getLevel() {
        return _levelObject;
    }

    public void setLevel(Level newLevel) {
        _levelObject = newLevel;
        _levelValue = _levelObject.intValue();
    }

    // Private method to infer the caller's class and method names
    private void inferCaller(LogRecord logRecord) {
        // Get the stack trace.
        StackTraceElement stack[] = (new Throwable()).getStackTrace();
        // Now search for the first frame before this ("Logger") class.
        int ix =  0;
        while (ix < stack.length) {
            StackTraceElement frame = stack[ix];
            String cname = frame.getClassName();
            if (!cname.equals("com.sun.max.ve.logging.Logger")) {
                // We've found the relevant frame.
                logRecord.setSourceClassName(cname);
                logRecord.setSourceMethodName(frame.getMethodName());
                return;
            }
            ix++;
        }
        // We haven't found a suitable frame, so just punt.  This is
        // OK as we are only committed to making a "best effort" here.
    }
}
