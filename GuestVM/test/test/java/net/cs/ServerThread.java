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

import java.util.*;
import java.io.*;

/**
 * Server thread abstract class.
 * @author Mick Jordan
 *
 */
public abstract class ServerThread extends Thread {
    /**
     * Base port number for first server thread.
     */
    public static final int PORT = 10000;
    /**
     * The data that is returned on an ackowledgement.
     */
    public static final byte[] ACK_BYTES = {0, 1, 2, 3 };

    protected int _threadNum;
    protected SessionData _sessionData;
    protected int _blobSize;
    protected int _nbuffers;
    protected boolean _oneRun = false;
    protected boolean _checkData = false;
    protected boolean _syncCheck = true;
    protected boolean _ack = true;
    protected List<byte[]> _buffer;
    protected int _bufferPut = 0;
    protected int _bufferGet = 0;
    protected int _bufferCount = 0;
    protected boolean _verbose;
    protected int _serverOutOfBuffersCount;
    protected String _type;

    protected ServerThread(int threadNum, SessionData sessionData, int blobSize,
            int nbuffers, boolean oneRun, boolean checkData, boolean syncCheck,
            boolean ack, boolean verbose, String type) {
        super("Server-" + threadNum);
        _threadNum = threadNum;
        _sessionData = sessionData;
        _blobSize = blobSize;
        _nbuffers = nbuffers;
        _oneRun = oneRun;
        _checkData = checkData;
        _syncCheck = syncCheck;
        _ack = ack;
        _verbose = verbose;
        _type = type;
        _nbuffers = nbuffers;
        _buffer = new ArrayList<byte[]>(nbuffers);
        for (int i = 0; i < nbuffers; i++) {
            _buffer.add(new byte[blobSize]);
        }
    }

    public int getData(byte[] data) {
        synchronized (_buffer) {
            while (_bufferCount <= 0) {
                try {
                    _buffer.wait();
                } catch (InterruptedException e) {
                    if (_verbose) {
                        System.out.print("Server: interrupted");
                    }
                }
            }
            System.arraycopy((byte[]) _buffer.get(_bufferGet), 0, data, 0,
                    _blobSize);
            _bufferGet++;
            if (_bufferGet >= _nbuffers) {
                _bufferGet = 0; // wrap
            }
            _bufferCount--;
            _buffer.notifyAll();
        }
        return 1;
    }

    protected abstract void doAck() throws IOException;

    protected byte[] getBuffer() {
        byte[] data = null;

        synchronized (_buffer) {
            while (_bufferCount >= _nbuffers) {
                try {
                    _serverOutOfBuffersCount++;
                    verbose("waiting for buffer");
                    _buffer.wait();
                } catch (InterruptedException e) {
                }
            }
            data = (byte[]) _buffer.get(_bufferPut);
            _bufferPut++;
            if (_bufferPut >= _nbuffers) {
                _bufferPut = 0;
            }
            _bufferCount++;
        }
        return data;
    }

    protected void check(byte[] data, int totalRead)  throws IOException {
        verbose("read " + totalRead + " bytes");

        if (_syncCheck) {
            if (_checkData) {
                if (!_sessionData.compare(data)) {
                    verbose("session data mismatch");
                }
            }
        }
        if (_ack) {
            doAck();
        }

        synchronized (_buffer) {
            _buffer.notifyAll();
        }

        if (!_syncCheck) {
            if (_checkData) {
                if (!_sessionData.compare(data)) {
                    verbose("session data mismatch");
                }
            }
        }

    }

    protected void info(String msg) {
        System.out.println(Thread.currentThread() + ": " + msg);
    }

    protected void verbose(String msg) {
        if (_verbose) {
            System.out.println(Thread.currentThread() + ": " + msg);
        }
    }

    public void run() {
        info("starting " + _type + " server on port " + PORT + _threadNum);
    }

}
