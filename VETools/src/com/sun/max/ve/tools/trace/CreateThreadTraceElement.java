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
package com.sun.max.ve.tools.trace;

/**
 * Thread ids are kernel ids:
 * -1: no thread (kernel startup)
 * 0-31: idle threads for CPUs 0-31
 * 32: debug handler thread
 * 33 - : kernel and Java threads
 *
 * @author Mick Jordan
 *
 */

import java.util.*;

public class CreateThreadTraceElement extends ThreadIdTraceElement {
    private String _name;
    private int _cpu;
    private long _stack;
    private int  _flags;
    private static ThreadIterable _myIterable = new ThreadIterable();

    static {
        final CreateThreadTraceElement startUp = new CreateThreadTraceElement();
        startUp.setId(-1);
        startUp.setName("kernel-startup");
        startUp.setInitialCpu(0);
    }


    public CreateThreadTraceElement() {
        _myIterable.addElement(this);
    }

    public CreateThreadTraceElement setName(String name) {
        _name = name;
        return this;
    }

    public CreateThreadTraceElement setInitialCpu(int cpu) {
        _cpu = cpu;
        return this;
    }

    public CreateThreadTraceElement setStack(long stack) {
        _stack = stack;
        return this;
    }

    public CreateThreadTraceElement setFlags(int flags) {
        _flags = flags;
        return this;
    }

    public String getName() {
        return _name;
    }

    public int getInitialCpu() {
        return _cpu;
    }

    public String toString() {
        return super.toString() + " " + _name + " " + _cpu + " " + _flags + " " + Long.toHexString(_stack);
    }

    public static Iterable<CreateThreadTraceElement> getThreadIterable(boolean includeKernel) {
        _myIterable.includeKernel(includeKernel);
        return _myIterable;
    }

    public static Iterable<CreateThreadTraceElement> getThreadIterable() {
        return getThreadIterable(false);
    }

    public static CreateThreadTraceElement find(int id) {
        for (CreateThreadTraceElement t : _myIterable) {
            if (t.getId() == id) {
                return t;
            }
        }
        throw new RuntimeException("thread id " + id + " not found");
    }

    public static final class ThreadIterable implements Iterable<CreateThreadTraceElement> {
        private boolean _includeKernel = false;
        public static List<CreateThreadTraceElement> _list = new ArrayList<CreateThreadTraceElement>();

        private ThreadIterable() {
        }

        private void includeKernel(boolean includeKernel) {
            _includeKernel = includeKernel;
        }

        public Iterator<CreateThreadTraceElement> iterator() {
            return new ThreadIterator(_list.listIterator(), _includeKernel);
        }

        private void addElement(CreateThreadTraceElement t) {
            _list.add(t);
        }
    }

    private static class ThreadIterator implements Iterator<CreateThreadTraceElement> {

        ListIterator<CreateThreadTraceElement> _iter;

        ThreadIterator(ListIterator<CreateThreadTraceElement> iter, boolean includeKernel) {
            _iter = iter;
            if (!includeKernel) {
                _iter.next();
            }
        }

        public boolean hasNext() {
            return _iter.hasNext();
        }

        public void remove() {

        }

        public CreateThreadTraceElement next() {
            return _iter.next();
        }
    }

}
