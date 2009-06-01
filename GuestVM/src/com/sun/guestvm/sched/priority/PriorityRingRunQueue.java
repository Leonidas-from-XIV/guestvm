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
package com.sun.guestvm.sched.priority;

import java.util.*;

import com.sun.guestvm.sched.RingRunQueue;
import com.sun.guestvm.sched.RingRunQueueEntry;
import com.sun.guestvm.sched.RunQueue;
import com.sun.max.lang.StaticLoophole;

/**
 * This class supports O(1) prioritized insertion of elements into the logical queue.
 * It is implemented as an array of @see RingRunQueue objects.
 *
 * @author Mick Jordan
 *
 * @param <T>
 */

public class PriorityRingRunQueue<T extends PriorityRingRunQueueEntry> extends RunQueue<T> {

    private RingRunQueue<T>[] _queues;
    private int _min;
    private int _max;
    private int _entries;  // total number of entries
    private int _lwm;      // low water mark of non-empty queue
    private int _hwm;     // high water mark of non-empty queue
    private LevelRingQueueCreator<T> _creator;

    public interface LevelRingQueueCreator<T extends PriorityRingRunQueueEntry> {
        RingRunQueue<T> create();
    }

    public PriorityRingRunQueue(int min, int max, LevelRingQueueCreator<T> creator) {
        _min = min;
        _max = max;
        _hwm = _min;
        _lwm = _max;
        _creator = creator;
    }

    public void buildtimeInitialize() {
        // allocate a queue for each priority level
        _queues = StaticLoophole.cast(new RingRunQueue[_max - _min + 1]);
        for (int q = 0; q < _queues.length; q++) {
            _queues[q] = _creator.create();
        }
    }

    @Override
    public void insert(T entry) {
        final int p = entry.getPriority();
        final RingRunQueue<T> queue = _queues[p - _min];
        queue.insert(entry);
        _entries++;
        if (p > _hwm) {
            _hwm = p;
        }
        if (p < _lwm) {
            _lwm = p;
        }
    }

    @Override
    public T head() {
        // return head of highest priority queue
        for (int i = _hwm; i >= _lwm; i--) {
            final RingRunQueue<T> queue = _queues[i - _min];
            final T head = queue.head();
            if (head != null) {
                return head;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T next() {
        // this spans priorities
        T theHead = null;
        for (int i = _hwm; i >= _lwm; i--) {
            final RingRunQueue<T> queue = _queues[i - _min];
            final T head = queue.head();
            if (head != null) {
                if (theHead != null) {
                    return head;
                }
                theHead = head;
                final RingRunQueueEntry next = head.getNext();
                if (next != head)  {
                    return (T) next;
                }
                // otherwise keep on looking at lower priority
            }
        }
        return null;
    }

    @Override
    public boolean empty() {
        return _entries == 0;
    }

    @Override
    public int size() {
        return _entries;
    }

    @Override
    public void moveHeadToEnd() {
        // only moves entries in the same priority queue
        for (int i = _hwm; i >= _lwm; i--) {
            final RingRunQueue<T> queue = _queues[i - _min];
            final T head = queue.head();
            if (head != null) {
                queue.moveHeadToEnd();
                return;
            }
        }
    }

    @Override
    public void remove(T entry) {
        final int p = entry.getPriority();
        RingRunQueue<T> queue = _queues[p - _min];
        queue.remove(entry);
        _entries--;
        final T head = queue.head();
        if (head == null) {
            // queue became empty, adjust high/low watermarks
            // if we find nothing, we reset
            if (p == _hwm) {
                _hwm = _min;
                for (int i = p; i >= _lwm; i--) {
                    queue = _queues[i - _min];
                    if (queue.head() != null) {
                        _hwm = i;
                        break;
                    }
                }
            }
            if (p == _lwm) {
                _lwm = _max;
                for (int i = p; i <= _hwm; i++) {
                    queue = _queues[i - _min];
                    if (queue.head() != null) {
                        _lwm = i;
                        break;
                    }
                }
            }
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new QIterator();
    }

    class QIterator implements Iterator<T> {
        private RingRunQueueEntry _next;
        private RingRunQueueEntry _head;
        private int _level;

        QIterator() {
            for (int i = _hwm; i >= _lwm; i--) {
                final RingRunQueue<T> queue = _queues[i - _min];
                final T head = queue.head();
                if (head != null) {
                    _next = head;
                    _head = head;
                    _level = i;
                    return;
                }
            }
            _next = null;
        }

        public boolean hasNext() {
            return _next != null;
        }

        public T next() {
            @SuppressWarnings("unchecked")
            final T result = (T) _next;
            _next = _next.getNext();
            if (_next == _head) {
                // exhausted this level, look lower
                _head = null;
                for (int i = _level - 1; i >= _lwm; i--) {
                    final RingRunQueue<T> queue = _queues[i - _min];
                    final T head = queue.head();
                    if (head != null) {
                        _next = head;
                        _head = head;
                        _level = i;
                        break;
                    }
                }
                if (_head == null) {
                    // done
                    _next = null;
                }
            }
            return result;
        }

        public void remove() {

        }
    }

}
