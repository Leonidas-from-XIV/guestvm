package com.sun.guestvm.logging;

import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * We can't use java.util.logging in the low levels of GuestVM because it causes nasty circularities,
 * so we have a much simpler version here.
 *
 * @author Mick Jordan
 *
 */
public class Logger {
    private static final String LEVEL_PROPERTY = "guestvm.logging.level";
    private static final String HANDLER_PROPERTY = "guestvm.logging.handler";
    private static final String DEFAULT_HANDLER = "com.sun.guestvm.logging.DebugHandler";
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
                System.err.println("invalid argument " + levelName + " for property guestvm.logging.level");
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
            if (!cname.equals("com.sun.guestvm.logging.Logger")) {
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
