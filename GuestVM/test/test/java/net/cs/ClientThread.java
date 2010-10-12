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
package test.java.net.cs;

import java.io.*;

public abstract class ClientThread extends Thread {
    ClientThread(String host, int threadNum, SessionData sessionData,
            long timeToRun, boolean ack, long delay, boolean verbose, String type) {
        super("Client-" + threadNum);
        _host = host;
        _threadNum = threadNum;
        _sessionData = sessionData;
        _ack = ack;
        if (timeToRun < 0) {
            _iterations = -timeToRun;
            timeToRun = Long.MAX_VALUE;
        } else {
            _timeToRun = timeToRun;
            _iterations = Long.MAX_VALUE;
        }
        _delay = delay;
        _verbose = verbose;
        _type = type;
        _ackBytes = new byte[ServerThread.ACK_BYTES.length];
    }
    /**
     * Total number of operations for his client.
     */
    long _totalOps;
    /**
     * The minimum latency measured for an operation.
     */
    long _minLatency = Long.MAX_VALUE;
    /**
     * The maximum latency measured for an operation.
     */
    long _maxLatency = 0;
    /**
     * The total time the client was active in the test.
     */
    long _totalTime = 0;

    protected SessionData _sessionData;
    protected  boolean _ack;
    protected byte[] _ackBytes;
    protected long _timeToRun;
    protected long _iterations;
    protected int _threadNum;
    protected String _host;
    protected long _delay;
    protected boolean _verbose;
    protected String _type;

    protected abstract void doSend(byte[] data) throws IOException;

    /**
     * Write the data to the server repeatedly until the run time is reached.
     * Optionally delay between sends. Record min/max latency.
     * @throws IOException
     */
    protected void writeLoop()  throws IOException {
        final long startTime = System.currentTimeMillis();
        final long endTime = startTime + _timeToRun * 1000;
        long startWriteTime;
        byte[] sessionDataBytes = null;
        while ((startWriteTime = System.currentTimeMillis()) < endTime && _iterations > 0) {
            sessionDataBytes = _sessionData.getSessionData();

            doSend(sessionDataBytes);

            final long endWriteTime = System.currentTimeMillis();
            final long durTime = endWriteTime - startWriteTime;
            if (durTime < _minLatency) {
                _minLatency = durTime;
            }
            if (durTime > _maxLatency) {
                _maxLatency = durTime;
            }
            _totalTime += durTime;
            _totalOps++;
            if (_delay > 0) {
                try {
                    Thread.sleep(_delay);
                } catch (InterruptedException ex) {

                }
            }
            _iterations--;
        }
    }

    public void run() {
        info("connecting to " + _type + " server at " + _host + " on port " + ServerThread.PORT + _threadNum);
    }

    protected void info(String msg) {
        System.out.println(Thread.currentThread() + ": " + msg);
    }

    protected void verbose(String msg) {
        if (_verbose) {
            System.out.println(Thread.currentThread() + ": " + msg);
        }
    }

}
