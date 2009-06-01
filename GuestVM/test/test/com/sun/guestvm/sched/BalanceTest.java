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
package test.com.sun.guestvm.sched;

/**
 * Test the thread<->cpu balancing algorithm used in the VM scheduler.
 * The code here is equivalent to that in StdScheduler but sanitized for running on a conventional JVM.
 * Args:
 * t n     number of active threads
 * c n m1 .. mc  n cpus with initial thread assignment m1 to cpu 0, ..., mc to cpu c-1
 * x n    n is cpu on which balancing is executing
 *
 * @author Mick Jordan
 *
 */
public class BalanceTest {
    private int _numActiveThreads; // running or runnable
    private Q[] _ready;
    private int[] _assignCurrent;
    private int[] _assignNew;
    private int _numCpus;

    /**
     * @param args
     */
    public static void main(String[] args) {
        new BalanceTest().run(args);
    }

    private void run(String[] args) {
        int xcpu = 0;
        // Checkstyle: stop modified control variable check
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.equals("t")) {
                _numActiveThreads = Integer.parseInt(args[++i]);
            } else if (arg.equals("c")) {
                _numCpus = Integer.parseInt(args[++i]);
                _assignCurrent = new int[_numCpus];
                _assignNew = new int[_numCpus];
                _ready = new Q[_numCpus];
                for (int c = 0; c < _numCpus; c++) {
                    _ready[c] = new Q(Integer.parseInt(args[++i]));
                    _assignCurrent[c] = _ready[c].size();
                }
            } else if (arg.equals("x")) {
                xcpu = Integer.parseInt(args[++i]);
            }
        }
        // Checkstyle: resume modified control variable check
        display("current", _assignCurrent);
        rebalance(xcpu);
        display("new", _assignNew);

    }

    private void display(String state, int[] a) {
        System.out.print(state + ": ");
        for (int i = 0; i < a.length; i++) {
            System.out.print(a[i] + " ");
        }
        System.out.println();
    }

    private void rebalance(int xcpu) {
        if (_numCpus <= 1) {
            return;
        }
        int rem = 0;
        final int ideal = _numActiveThreads / _numCpus;
        final int x =  ideal * _numCpus;
        if (x != _numActiveThreads) {
            rem = _numActiveThreads - x;
        }
        int cpu;
        // Could maintain _assignCurrent on the fly
        for (cpu = 0; cpu < _numCpus; cpu++) {
            _assignCurrent[cpu] = _ready[cpu].size();
            _assignNew[cpu] = ideal;
        }
        // rem cpus have to have one more than ideal.
        // If any are in that state already, then that helps
        // and it would be counterproductive to reduce their
        // load as it would cause needless switching
        while (rem > 0) {
            cpu = findCpuWithN(ideal + 1);
            if (cpu >= 0) {
                _assignNew[cpu]++;
                rem--;
            } else {
                break;
            }
        }
        if (rem > 0) {
            cpu = 0;
            while (rem > 0) {
                _assignNew[cpu]++;
                cpu++;
                rem--;
            }
        }
        // now migrate threads to meet new assignment
        for (cpu = 0; cpu < _numCpus; cpu++) {
            while (_assignCurrent[cpu] > _assignNew[cpu]) {
                final int cpu2 = findMigratee();
                System.out.println("JM " + cpu + " " + cpu2);
                //migrate(cpu, cpu2, xcpu);
                _assignCurrent[cpu]--;
            }
        }

    }
    /**
     * Find a cpu whose current assignment is n.
     */
    private int findCpuWithN(int n) {
        int cpu = 0;
        for (cpu = 0; cpu < _numCpus; cpu++) {
            if (_assignCurrent[cpu] == n) {
                return cpu;
            }
        }
        return -1;
    }

    /**
     * Find a cpu whose current assignment is below new assignment.
     */
    private int findMigratee() {
        for (int cpu = 0; cpu < _numCpus; cpu++) {
            if (_assignCurrent[cpu] < _assignNew[cpu]) {
                return cpu;
            }
        }
        return -1;
    }

    static class Q {
        private int _size;
        Q(int size) {
            _size = size;
        }

        int size() {
            return _size;
        }
    }

}
