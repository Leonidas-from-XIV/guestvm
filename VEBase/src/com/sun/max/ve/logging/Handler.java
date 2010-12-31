package com.sun.max.ve.logging;

import java.util.logging.LogRecord;

/**
 * A much simplified variant of java.util.logging.Handler.
 *
 * @author Mick Jordan
 *
 */
public abstract class Handler {
    public static final String THREAD_PROPERTY = "max.ve.logging.thread";
    private static boolean _logThread;
    private static boolean _init;

    public abstract void println(String msg);

    public void publish(LogRecord record) {
        if (!_init) {
            _logThread = System.getProperty(THREAD_PROPERTY) != null;
            _init = true;
        }
        final StringBuilder sb = new StringBuilder();
        if (_logThread) {
            sb.append(Thread.currentThread().getName()).append(": ");
        }
        sb.append(record.getLevel().toString()).append(": ").append(record.getMillis()).append(' ');
        if (record.getSourceClassName() != null) {
            sb.append(record.getSourceClassName());
        } else {
            sb.append(record.getLoggerName());
        }
        if (record.getSourceMethodName() != null) {
            sb.append(" ");
            sb.append(record.getSourceMethodName());
        }
        sb.append(": ").append(record.getMessage());
        println(sb.toString());
    }

}
