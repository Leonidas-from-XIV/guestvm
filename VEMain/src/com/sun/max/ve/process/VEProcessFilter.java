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
package com.sun.max.ve.process;

import java.util.*;

import com.sun.max.ve.error.*;
import com.sun.max.ve.fs.ErrorDecoder;

/**
 * A class that manages and supports classes that act as filters for exec calls.
 * The PROCESS_FILTER_CLASS_PROPERTY is a list of classes that
 * are instantiated, each of which defines one or more strings that are used
 * to match exec calls. Note that a filter may decide handle the call internally
 * or adjust the call and still have it handled externally.
 *
 * @author Mick Jordan
 *
 */
public class VEProcessFilter {
    private static final String PROCESS_FILTER_CLASS_PROPERTY = "max.ve.process.filterclasses";
    private static Map<String, VEProcessFilter> _filters = new HashMap<String, VEProcessFilter>();
    private static boolean _init = false;
    static class State {
        State(VEProcessFilter filter, int fd) {
            _filter = filter;
            _fd = fd;
        }
        VEProcessFilter _filter;
        int _fd;
        byte[] _data;
    }

    private static int _nextId;
    protected String _name;
    private String[] _names;
    private static Map<Integer, State> _stateMap = new HashMap<Integer, State>();
    enum StdIO {
        IN, OUT, ERR;
    }

    public String[] names() {
        return _names;
    }

    public static VEProcessFilter filter(byte[] prog) {
        init();
        return _filters.get(stripNull(prog));
    }

    /**
     * Return the filter corresponding to key or null if none.
     * @param key
     * @return
     */
    public static VEProcessFilter getFilter(int key) {
        final State state = _stateMap.get(key);
        /* state will only be null if nextId was not called, which implies that the filter
         * elected to have the exec call handled externally. */
        if (state == null) {
            return null;
        }
        return state._filter;
    }

    /**
     * Constructor for a class the overrides the names method.
     */
    protected VEProcessFilter() {

    }

    protected VEProcessFilter(String name) {
        _name = name;
        _names = new String[] {name};
    }

    /**
     * Execute the process that this filter handles with the given arguments.
     * The return value is either negative, indicating a failure to exec or a
     * positive value that will be passed to the @see FilterFileSystem when
     * creating the file descriptors for stdin, stdout and stderr.
     * The default implementation just fails.
     * @param prog
     * @param argBlock
     * @param argc
     * @param envBlock
     * @param envc
     * @param dir
     * @return a negative value indicating error or a positive integer identifying the exec instance
     */
    public int exec(byte[] prog, byte[] argBlock, int argc, byte[] envBlock, int envc, byte[] dir) {
        return -1;
    }

    public int waitForProcessExit(int key) {
        return 0;
    }

    public void destroyProcess(int key) {

    }

    /**
     * A unique id across all filters that can also encode the three file descriptors.
     * @return
     */
    final int nextId() {
        final int result = _nextId;
        _nextId += 3;
        final int[] fds = VEProcessFilter.getFds(result);
        for (int i = 0; i < fds.length; i++) {
            final int fd = VEProcessFilter.keyToFd(fds[i]);
            _stateMap.put(fds[i], new State(this, fd));
        }
        return result;
    }

    /**
     * Converts a null-separated, null-terminated byte array into an array of Strings.
     * @param data
     * @param optional working directory that, if present, is pre-pended to the resulting array
     * @return
     */
    public static String[] cmdArgs(byte[] data, byte[] wdir) {
        String[] result;
        int numArgs = 0;
        for (int i = 0; i < data.length; i++) {
            if (data[i] == 0) {
                numArgs++;
            }
        }
        if (wdir != null) {
            numArgs++;
        }
        result = new String[numArgs];
        if (numArgs > 0) {
            final int x = wdir == null ? 0 : 1;
            if (x > 0) {
                result[0] = new String(wdir, 0, wdir.length - 1);
            }
            int k = 0;
            int j = 0;
            for (int i = 0; i < data.length; i++) {
                if (data[i] == 0) {
                    result[k + x] = new String(data, j, i - j);
                    j = i + 1;
                    k++;
                }
            }
        }
        return result;
    }

    public static String[] cmdArgs(byte[] data) {
        return cmdArgs(data, null);
    }

    public static int invokeClose0(int key) {
        final State state = _stateMap.get(key);
        final int rc = state._filter.close0(state);
        _stateMap.remove(key);
        return rc;
    }

    public static int invokeReadBytes(int key, byte[] bytes, int offset, int length, long fileOffset) {
        final int fd = VEProcessFilter.keyToFd(key);
        /* N.B. StdIO.IN is the exec filter's input, so applications write to it, and read from OUT and ERR */
        if (fd == StdIO.IN.ordinal()) {
            return -ErrorDecoder.Code.EBADF.getCode();
        }
        final State state = _stateMap.get(key);
        if (state._data == null) {
            return -1;
        }
        return state._filter.readBytes(state, bytes, offset, length, fileOffset);
    }

    protected void setData(int key, StdIO stdIO, byte[] data) {
        _stateMap.get(key + stdIO.ordinal())._data = data;
    }

    /* This method may be overridden by the concrete filter. The default implementation works with the
     * _data field in the state class, that is set by the setData method.
     */
    protected int readBytes(State state, byte[] bytes, int offset, int length, long fileOffset) {
        if (state._fd == StdIO.ERR.ordinal()) {
            return -1;
        } else if (state._fd == StdIO.OUT.ordinal()) {
            final int available = state._data == null ? 0 : state._data.length - (int) fileOffset;
            if (available <= 0) {
                return -1;
            } else {
                final int rlength = length < available ? length : available;
                System.arraycopy(state._data, (int) fileOffset, bytes, offset, rlength);
                return rlength;
            }
        } else {
            assert false;
            return -1;
        }
    }

    /* This may be overridden by the concrete filter, if there is necessary close action.
     */
    protected int close0(State state) {
        return 0;
    }

    /**
     * Get file descriptors for stdin/out/err.
     * We encode the fd in the key.
     * E.g., 0, 0+1, 0+2, 3, 3+1, 3+2, ..., 3n, 3n+1, 3n+2
     *
     * @param key
     * @return
     */
    public static final int[] getFds(int key) {
        return new int[] {key, key + 1, key + 2};
    }

    public static int keyToFd(int key) {
        return key % 3;
    }

    public static int keyToExecId(int key) {
        return (key / 3) * 3;
    }


    public static String stripNull(byte[] prog) {
        /* prog is null terminated, but String will treat the null as a character. */
        return new String(prog, 0, prog.length - 1);
    }

    private static void init() {
        if (!_init) {
            final String prop = System.getProperty(PROCESS_FILTER_CLASS_PROPERTY);
            if (prop != null) {
                final String[] classNames = prop.split(",");
                for (String className : classNames) {
                    try {
                        final Class<?> klass = Class.forName(className);
                        final VEProcessFilter filter = (VEProcessFilter) klass.newInstance();
                        for (String name : filter.names()) {
                            _filters.put(name, filter);
                        }
                    } catch (Exception ex) {
                        VEError.unexpected("failed to load process filter class: " + prop);
                    }
                }
            }
            _init = true;
        }
    }
}
