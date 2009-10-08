package com.sun.guestvm.logging;

import com.sun.max.vm.*;
/**
 * This is a hold-over from attempts to use java,util.logging directly - it is simplified here.
 *
 * This handler is extremely simple to minimize the amount of code needed in the image for logging.
 * It publishes the log records using Maxine's Log class.
 *
 * @author Mick Jordan
 *
 */

public class MaxineLogHandler extends Handler{

    public void println(String msg) {
        Log.println(msg);
    }
}

