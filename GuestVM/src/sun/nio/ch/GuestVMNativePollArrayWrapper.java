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
package sun.nio.ch;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

import com.sun.guestvm.fs.*;
import com.sun.max.unsafe.*;
import com.sun.max.vm.reference.*;
import com.sun.max.vm.VMConfiguration;
import com.sun.max.annotate.*;

/**
 * Implementation of the native methods of PollArrayWrapper leveraging
 * access to package private definition of PollArrayWrapper.
 *
 * @author Mick Jordan
 *
 */

public class GuestVMNativePollArrayWrapper {

    /**
     * Implements the substituted native method. This is forwarded from the substituted native method in
     * JDK_sun_nio_ch.PollArrayWrapper.
     *
     * @param pObj
     *                the "this" argument in the substituted method, i.e., the PollArrayWrapper instance
     * @param pollAddress
     *                native address of the actual pollfd_t instance to work with
     * @param numfds ??
     * @param timeout
     * @return
     */
    public static int poll0(Object pObj, long pollAddress, int numfds, long timeout) throws IOException {
        final PollArrayWrapper p = (PollArrayWrapper) pObj;
        // pollAddress is already offset from the value of p.pollArrayAddress to the start of the pollfd array elements
        // so we must use the p.getXXX methods with index starting at 0.
        final VirtualFileSystem[] vfsArray = timeout == 0 ? null : new VirtualFileSystem[numfds];
        int count = 0;
        for (int i = 0; i < numfds; i++) {
            final int fd = p.getDescriptor(i);
            final int eventOps = p.getEventOps(i);
            final VirtualFileSystem vfs = VirtualFileSystemId.getVfs(fd);
            final int reventOps = vfs.poll0(VirtualFileSystemId.getFd(fd), eventOps, 0);
            count += checkMatch(p, i, reventOps);
            if (timeout != 0) {
                vfsArray[i] = vfs;
            }
        }
        debug(timeout, count);
        if (timeout == 0 || count > 0) {
            return count;
        }
        // now wait for timeout for one event
        if (numfds == 1) {
            // we can wait for one fd on the current thread
            final int reventOps = vfsArray[0].poll0(VirtualFileSystemId.getFd(p.getDescriptor(0)), p.getEventOps(0), timeout);
            return checkMatch(p, 0, reventOps);
        }
        // multiple file descriptors to wait for, need a thread for each
        final PollOut pollOut = new PollOut(numfds);
        final PollThread[] pollThreads = new PollThread[numfds];
        for (int t = 0; t < numfds; t++) {
            pollThreads[t] = PollThread.getThread();
            // this sets the thread going
            pollThreads[t].setInfo(vfsArray, p, t, timeout, pollOut);
        }
        synchronized (pollOut) {
            try {
                while (pollOut._waiterCount > 0 && pollOut._index < 0) {
                    pollOut.wait();
                    if (pollOut._index >= 0) {
                        return 1;
                    }
                }
                // if all waiters return without a match we drop through and return 0
            } catch (InterruptedException ex) {
                return -ErrorDecoder.Code.EINTR.getCode();
            } finally {
                if (pollOut._waiterCount > 0) {
                    PollThread.cancelThreads(pollThreads, pollOut);
                }
            }
        }
        return 0;
    }

    @NEVER_INLINE
    static void debug(long t, int c) {

    }

    static class PollOut {
        int _index;
        int _reventOps;
        int _waiterCount;
        PollOut(int waiterCount) {
            _waiterCount = waiterCount;
            _index = -1;
        }
    }

    private static int checkMatch(PollArrayWrapper p, int i, int reventOps) throws IOException {
        if (reventOps < 0) {
            throw new IOException("poll failed");
        } else {
            return match(p, i, reventOps);
        }
    }

    private static int match(PollArrayWrapper p, int i, int reventOps) {
        if (reventOps < 0) {
            return reventOps;
        } else {
            if ((reventOps & p.getEventOps(i)) != 0) {
                p.putReventOps(i, reventOps);
                return 1;
            }
        }
        return 0;

    }

    static class PollThread extends Thread {
        static List<PollThread> _workers = new ArrayList<PollThread>(0);
        static int _nextWorkerId;

        VirtualFileSystem[] _vfsArray;
        PollArrayWrapper _p;
        int _index;
        long _timeout;
        PollOut _pollOut;

        PollThread() {
            setDaemon(true);
            setName("PollThread-" + _nextWorkerId++);
        }

        static synchronized PollThread getThread() {
            PollThread result = null;
            for (int i = 0; i < _workers.size(); i++) {
                final PollThread thread = _workers.get(i);
                if (thread.idle()) {
                    result = thread;
                    break;
                }
            }
            if (result == null) {
                result = new PollThread();
                _workers.add(result);
                result.start();
            }
            return result;
        }

        static void cancelThreads(PollThread[] pollThreads, PollOut pollOut) {
            for (PollThread pollThread : pollThreads) {
                synchronized (pollThread) {
                    // if it was working for caller, interrupt it
                    if (pollThread._pollOut == pollOut) {
                        pollThread.interrupt();
                    }
                }
            }
        }


        synchronized void setInfo(VirtualFileSystem[] vfsArray, PollArrayWrapper p, int index, long timeout, PollOut pollOut) {
            _vfsArray = vfsArray;
            _p = p;
            _index = index;
            _timeout = timeout;
            _pollOut = pollOut;
            notify();
        }

        synchronized boolean idle() {
            return _pollOut == null;
        }

        public void run() {
            while (true) {
                try {
                    synchronized (this) {
                        while (_pollOut == null) {
                            wait();
                        }
                    }
                    final int reventOps = _vfsArray[_index].poll0(VirtualFileSystemId.getFd(_p.getDescriptor(_index)), _p.getEventOps(_index), _timeout);
                    synchronized (_pollOut) {
                        if (_pollOut._index < 0 && match(_p, _index, reventOps) > 0) {
                            _pollOut._reventOps = reventOps;
                            _pollOut._index = _index;
                        }
                        // wake up the coordinator - either there was a match or we are the last thread
                        _pollOut._waiterCount--;
                        _pollOut.notify();
                    }
                } catch (InterruptedException ex) {

                } finally {
                    // we are done whatever
                    synchronized (this) {
                        _pollOut = null;
                    }
                }
            }
        }
    }

    private static final ByteBuffer _fakeBuffer = ByteBuffer.allocate(1);

    public static void interrupt(int fd) throws IOException {
        final VirtualFileSystem vfs = VirtualFileSystemId.getVfs(fd);
        // Following the native implementation of PollArrayWrapper, we write one byte to fd.
        // This all seems a bit convoluted, there must be a better way.
        _fakeBuffer.put((byte) 1);
        vfs.writeBytes(VirtualFileSystemId.getFd(fd), _fakeBuffer, 0);
    }

}
