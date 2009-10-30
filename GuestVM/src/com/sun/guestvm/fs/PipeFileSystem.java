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
package com.sun.guestvm.fs;

import java.util.*;
import com.sun.max.annotate.*;
import com.sun.max.lang.*;
import com.sun.max.unsafe.*;
import com.sun.guestvm.util.*;

/**
 * Not really a file system, just supports NIO pipes.
 * @author Mick Jordan
 *
 */

public class PipeFileSystem extends UnimplementedFileSystemImpl implements VirtualFileSystem {
    private static PipeFileSystem _singleton;
    private static int _nextFd;
    private static Map<Integer, Pipe> _pipes = Collections.synchronizedMap(new HashMap<Integer, Pipe>());

    private static class Pipe {
        static final int BUFFER_SIZE = 128;
        byte[] _buffer = new byte[BUFFER_SIZE];
        int _readIndex;
        int _writeIndex;
        int _available;
        boolean _readClosed;
        boolean _writeClosed;
        boolean _blocking;
        Thread _waiter;

        Pipe(boolean blocking) {
            _blocking = blocking;
        }

        @INLINE
        final boolean full() {
            return _available >= _buffer.length;
        }

        @INLINE
        final boolean empty() {
            return _available == 0;
        }

        @INLINE
        final int free() {
            return BUFFER_SIZE - _available;
        }

        @INLINE
        final int available() {
            return _available;
        }

        @INLINE
        final boolean readClosed() {
            return _readClosed;
        }

        @INLINE
        final void closeRead() {
            _readClosed = true;
        }

        @INLINE
        final boolean writeClosed() {
            return _readClosed;
        }

        @INLINE
        final void closeWrite() {
            _readClosed = true;
        }

        @INLINE
        final boolean blocking() {
            return _blocking;
        }

        @INLINE
        final byte consumeOne() {
            final byte result = _buffer[_readIndex];
            _readIndex = Unsigned.irem(_readIndex + 1, _buffer.length);
            _available--;
            return result;
        }

        @INLINE
        final void produceOne(byte b) {
            _buffer[_writeIndex] = b;
            _writeIndex = Unsigned.irem(_writeIndex + 1, _buffer.length);
            _available++;
        }
    }

    public static PipeFileSystem create() {
        if (_singleton == null) {
            _singleton = new PipeFileSystem();
        }
        return _singleton;
    }

    /**
     * Create a pipe.
     * @param fds
     */
    public synchronized void createPipe(int[] fds, boolean blocking) {
        final int fd = _nextFd;
        fds[0] = VirtualFileSystemId.getUniqueFd(this, fd);
        fds[1] = VirtualFileSystemId.getUniqueFd(this, fd + 1);
        final Pipe pipe = new Pipe(blocking);
        _pipes.put(fd, pipe);
        _pipes.put(fd + 1, pipe);
        _nextFd += 2;
    }

    @Override
    public int readBytes(int fd, long address, int offset, int length, long fileOffset) {
        final Pipe pipe = _pipes.get(fd);
        final Pointer pAddress = Pointer.fromLong(address);
        int read = 0;
        int wOffset = offset;
        // If no data is available we check for a close write end first, which means that
        // we never wait on a closed pipe. That means that a close that happens while
        // we are waiting will terminate the wait (by the notify)
        synchronized (pipe) {
             // if no data available block
            int available = pipe.available();
            if (available == 0) {
                if (pipe.writeClosed()) {
                    return 0;  // EOF
                }
                try {
                    pipe.wait();
                    available = pipe.available();
                    if (available == 0) {
                        if (pipe.writeClosed()) {
                            return 0;  // EOF
                        }
                        return -ErrorDecoder.Code.EAGAIN.getCode();
                    }
                } catch (InterruptedException ex) {
                    return -ErrorDecoder.Code.EINTR.getCode();
                }
            }
            // read what is available up to requested length
            int toRead = available < length ? available : length;
            while (toRead > 0) {
                pAddress.writeByte(wOffset++, pipe.consumeOne());
                toRead--;
                read++;
            }
            pipe.notifyAll();
        }
        return read;
    }

    @Override
    public int writeBytes(int fd, long address, int offset, int length, long fileOffset) {
        final Pipe pipe = _pipes.get(fd);
        final Pointer pAddress = Pointer.fromLong(address);
        int toDo = length;
        int wOffset = offset;
        // Writer blocks until all data is written or read end of the pipe is closed.
        // As per read a blocked write will be woken up by a close of the read end.
        // We are not precisely implementing POSIX semantics here regarding blocking,
        // as we only write atomically the number of bytes given by pipe.free() and not PIPE_BUF.
        while (toDo > 0) {
            synchronized (pipe) {
                if (pipe.readClosed()) {
                    return -ErrorDecoder.Code.EPIPE.getCode();
                }
                while (pipe.full()) {
                    try {
                        pipe.wait();
                        if (pipe.readClosed()) {
                            return -ErrorDecoder.Code.EPIPE.getCode();
                        }
                    } catch (InterruptedException ex) {
                        return -ErrorDecoder.Code.EINTR.getCode();
                    }
                }
                // write as much as we can then notify readers and give up lock
                int canWrite = pipe.free();
                while (toDo > 0 && canWrite > 0) {
                    pipe.produceOne(pAddress.readByte(wOffset++));
                    toDo--;
                    canWrite--;
                }
                pipe.notifyAll();
            }
        }
        return length;
    }

    @Override
    public int close0(int fd) {
        final Pipe pipe = _pipes.get(fd);
        synchronized (pipe) {
            if ((fd & 1) == 0) {
                // read end
                pipe.closeRead();
            } else {
                // write end
                pipe.closeWrite();
            }
            // wake up any waiting readers or writers
            pipe.notifyAll();
        }
        return 0;
    }

    @Override
    public int poll0(int fd, int eventOps, long timeout) {
        final Pipe pipe = _pipes.get(fd);
        synchronized (pipe) {
            if ((fd & 1) == 0) {
                // read end, anything available?
                if (pipe.available() > 0) {
                    return VirtualFileSystem.POLLIN;
                }
                if (timeout == 0) {
                    return 0;
                }
                final TimeLimitedProc timedProc = new TimeLimitedProc() {
                    protected int proc(long remaining) throws InterruptedException {
                        pipe.wait(remaining);
                        if (pipe.available() > 0) {
                            return terminate(VirtualFileSystem.POLLIN);
                        } else if (pipe.available() == 0) {
                            if (pipe.writeClosed()) {
                                return terminate(0); // EOF
                            }
                        }
                        return 0;
                    }
                };
                return timedProc.run(timeout);
            } else {
                // write end, can we write?
                if (!pipe.full()) {
                    return VirtualFileSystem.POLLOUT;
                }
                if (timeout == 0) {
                    return 0;
                }
                final TimeLimitedProc timedProc = new TimeLimitedProc() {
                    protected int proc(long remaining) throws InterruptedException {
                        pipe.wait(remaining);
                        if (pipe.readClosed()) {
                            return terminate(0);
                        }
                        if (!pipe.full()) {
                            return terminate(VirtualFileSystem.POLLOUT);
                        }
                        return 0;
                    }
                };
                return timedProc.run(timeout);
            }
        }

    }

}
