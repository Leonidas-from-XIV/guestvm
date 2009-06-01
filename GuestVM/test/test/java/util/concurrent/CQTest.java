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
package test.java.util.concurrent;

import java.util.*;
import com.sun.max.lang.StaticLoophole;

public class CQTest {

    static MyQueue<Long> _q;
    static boolean _running = true;
    static Long[] _elements;
    private static final int ELEMENTS_SIZE = 1024;
    static int _delay = 0;
    static boolean _verbose = false;

    public static void main(String[] args) {
        int producerCount = 1;
        int consumerCount = 1;
        int runTime = 5;
        boolean needSync = false;

        String qImpl = "java.util.LinkedList";
        // Checkstyle: stop modified control variable check
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.equals("p")) {
                producerCount = Integer.parseInt(args[++i]);
            } else if (arg.equals("c")) {
                consumerCount = Integer.parseInt(args[++i]);
            } else if (arg.equals("t")) {
                runTime = Integer.parseInt(args[++i]);
            } else if (arg.equals("q")) {
                qImpl = args[++i];
            } else if (arg.equals("s")) {
                needSync = true;
            } else if (arg.equals("d")) {
                _delay = Integer.parseInt(args[++i]);
            } else if (arg.equals("v")) {
                _verbose = true;
            }
        }
        // Checkstyle: resume modified control variable check

        _elements = new Long[ELEMENTS_SIZE];
        for (int i = 0; i < ELEMENTS_SIZE; i++) {
            _elements[i] = new Long(i);
        }

        System.out.println("Instantiating " + qImpl);
        Queue<Long> queue = null;
        try {
            final Class< ? > qClass = Class.forName(qImpl);
            queue = StaticLoophole.cast(qClass.newInstance());
        } catch (Exception ex) {
            System.out.println("failed to instantiate " + qImpl);
        }

        if (needSync) {
            _q = synchronizedQueue(queue);
        } else {
            _q = unSynchronizedQueue(queue);
        }

        final Consumer[] consumers = new Consumer[consumerCount];
        final Producer[] producers = new Producer[producerCount];

        System.out.println("Running for " + runTime + " seconds");
        for (int p = 0; p < producerCount; p++) {
            producers[p] = new Producer();
            producers[p].setName("Producer[" + p + "]");
            producers[p].start();
        }
        for (int c = 0; c < consumerCount; c++) {
            consumers[c] = new Consumer();
            consumers[c].setName("Consumer[" + c + "]");
            consumers[c].start();
        }
        try {
            for (int i = 0; i < runTime; i++) {
                Thread.sleep(1000);
                if (_verbose) {
                    for (Producer producer : producers) {
                        System.out.println(producer.getName() + ", count: " + producer.count());
                    }
                    for (Consumer consumer : consumers) {
                        System.out.println(consumer.getName() + ", polls: " + consumer.polls() + ", count: " + consumer.count());
                    }
                }
            }
            _running = false;
            for (Producer producer : producers) {
                producer.join();
                System.out.println(producer.getName() + ", count: " + producer.count());
            }
            for (Consumer consumer : consumers) {
                consumer.join();
                System.out.println(consumer.getName() + ", polls: " + consumer.polls() + ", count: " + consumer.count());
            }
        } catch (InterruptedException ex) {
        }

    }

    static MyQueue<Long> synchronizedQueue(Queue<Long> q) {
        return new SynchronizedQueue<Long>(q);
    }

    static MyQueue<Long> unSynchronizedQueue(Queue<Long> q) {
        return new UnSynchronizedQueue<Long>(q);
    }

    interface MyQueue<E> {
        void add(E elem);
        E poll();
    }

    static class SynchronizedQueue<E> implements MyQueue<E> {
        private Queue<E> _queue;
        SynchronizedQueue(Queue<E> q) {
            _queue = q;
        }

        public synchronized void add(E elem) {
            _queue.add(elem);
        }

        public synchronized E poll() {
            return _queue.poll();
        }
    }

    static class UnSynchronizedQueue<E> implements MyQueue<E> {
        private Queue<E> _queue;
        UnSynchronizedQueue(Queue<E> q) {
            _queue = q;
        }

        public void add(E elem) {
            _queue.add(elem);
        }

        public E poll() {
            return _queue.poll();
        }
    }

    static class Consumer extends Thread {
        long _p;
        long _c;
        public void run() {
            while (_running) {
                _p++;
                final Long l = _q.poll();
                if (l != null) {
                    _c++;
                }
            }
        }

        long polls() {
            return _p;
        }

        long count() {
            return _c;
        }
    }

    static class Producer extends Thread {
        long _c = 0;
        public void run() {
            while (_running) {
                _q.add(_elements[(int) _c % ELEMENTS_SIZE]);
                _c++;
                if (_delay > 0) {
                    try {
                        Thread.sleep(_delay);
                    } catch (InterruptedException ex) {

                    }
                }
            }
        }

        long count() {
            return _c;
        }
    }

}
