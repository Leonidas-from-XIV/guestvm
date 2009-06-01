package com.sun.guestvm.logging;

import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * A much simplified variant of java.util.logging.Handler/
 *
 * @author Mick Jordan
 *
 */
public abstract class Handler {
    public abstract void println(String msg);

    public void publish(LogRecord record) {
        final Level level = record.getLevel();
        final StringBuilder sb = new StringBuilder();
        sb.append(level.toString()).append(": ").append(record.getMillis()).append(' ');
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
