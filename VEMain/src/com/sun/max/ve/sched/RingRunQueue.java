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
package com.sun.max.ve.sched;

import java.util.*;

/**
 * A ring implementation for the run queue. We implement our own to make sure no allocation
 * and no synchronization is done. This works with objects that implement the RingEntry interface,
 * which is a doubly-linked list abstraction, i.e., the queue is actually a ring, with one entry
 * specially designated as the head. Moving down the queue involves following the "next"
 * link, moving up the queue involves following the "link". The last entry of the queue
 * can be accessed simply by following the "prev" link from the head entry.
 *
 * The empty queue is denoted by _head == null;
 * A queue of one entry has the next and prev links pointing to itself.
 *
 * @author Harald Roeck
 * @suthor Mick Jordan
 *
 */

public class RingRunQueue<T extends RingRunQueueEntry> extends  RunQueue<T> {

    private T _head;
    private int _entries;

    public void buildtimeInitialize() {

    }

    public RingRunQueue() {
        _head = null;
    }

    @Override
    public void insert(T entry) {
        if (_head == null) {
            _head = entry;
            entry.setNext(entry); // point next at ourself
            entry.setPrev(entry); // point prev at ourself
        } else {
            entry.setPrev(_head.getPrev()); // old last is prev to entry
            entry.setNext(_head); // next for entry is head
            _head.getPrev().setNext(entry); // old last now points at entry
            _head.setPrev(entry); // entry is now the last
        }
        _entries++;
    }

    @Override
    public T head() {
        return (T) _head;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T next() {
        if (_head == null) {
            return null;
        } else {
            final RingRunQueueEntry next = _head.getNext();
            if (next == _head) {
                return null;
            } else {
                return (T) next;
            }
        }
    }

    @Override
    public boolean empty() {
        return _head == null;
    }

    @Override
    public int size() {
        return _entries;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void remove(T entry) {
        if (_head == entry) {
            _head = (T) entry.getNext();
        }
        if (_head == entry) {
            _head = null;
        } else {
            entry.getNext().setPrev(entry.getPrev());
            entry.getPrev().setNext(entry.getNext());
            entry.setNext(entry);
            entry.setPrev(entry);
        }
        _entries--;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void moveHeadToEnd() {
        if (_head != null) {
            _head = (T) _head.getNext();
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new QIterator();
    }

    class QIterator implements Iterator<T> {
        private RingRunQueueEntry _next;

        QIterator() {
            _next = _head;
        }

        public boolean hasNext() {
            return _next != null;
        }

        @SuppressWarnings("unchecked")
        public T next() {
            final T result = (T) _next;
            _next = _next.getNext();
            if (_next == _head) {
                _next = null;
            }
            return result;
        }

        public void remove() {

        }
    }
}
