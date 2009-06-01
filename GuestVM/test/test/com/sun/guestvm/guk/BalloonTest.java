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
package test.com.sun.guestvm.guk;

import java.util.*;
import com.sun.max.unsafe.*;
import com.sun.max.memory.VirtualMemory;
import com.sun.guestvm.guk.*;
import com.sun.guestvm.guk.x64.*;
import com.sun.guestvm.test.VMTestHelper;

/**
 * A stress test for the microkernel memory ballon mechanism.
 *
 * @author Mick Jordan
 *
 */

public class BalloonTest {

    private static Random _rand = new Random();
    private static boolean _verbose = false;
    private static boolean _veryVerbose = false;
    private static long _runtime = 30;
    private static long _sleep = 0;
    private static Map<Integer, Command> _commands = new HashMap<Integer, Command>();
    private static int _numCommands;
    private static long _memMax;
    private static Map<Long, Integer> _allocList = new HashMap<Long, Integer>();
    private static final int BULK_ALLOCATION = 512;
    /**
     * @param args
     */
    public static void main(String[] args) {
        int cmax = 0;
        // Checkstyle: stop modified control variable check
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.equals("v")) {
                _verbose = true;
            } else if (arg.equals("vv")) {
                _veryVerbose = true;
            } else if (arg.equals("r")) {
                _runtime = Long.parseLong(args[++i]);
            } else if (arg.equals("c")) {
                cmax = Integer.parseInt(args[++i]);
            } else if (arg.equals("s")) {
                _sleep = Long.parseLong(args[++i]);
            }
        }
        // Checkstyle: resume modified control variable check
        _commands.put(0, new IncreasePoolCommand("IncreasePool"));
        _commands.put(1, new DecreasePoolCommand("DecreasePool"));
        _commands.put(2, new AllocatePagesCommand("AllocatePages"));
        _commands.put(3, new DeallocatePagesCommand("DeallocatePages"));
        _numCommands = cmax > 0 ? cmax : _commands.size();
        _memMax = GUKPagePool.getMaximumReservation();
        runTest();
    }

    private static void runTest() {
        final long start = System.currentTimeMillis();
        final long end = start + _runtime * 1000;
        long now = start;
        while (now < end) {
            final int commandIndex = _rand.nextInt(_numCommands);
            final Command command = _commands.get(commandIndex);
            if (_verbose) {
                verboseOut("Invoking " + command.getName());
            }
            command.invoke();
            if (_veryVerbose) {
                GUKPagePool.logState();
            }
            if (_sleep > 0) {
                try {
                    Thread.sleep(_sleep * 1000);
                } catch (InterruptedException ex) {
                }
            }
            now = System.currentTimeMillis();
        }
    }

    private static void verboseOut(String s) {
        if (_verbose) {
            System.out.println(s);
        }
    }

    interface Command {
        void invoke();
        String getName();
    }

    abstract static class AbstractCommand implements Command {
        private String _name;

        AbstractCommand(String name) {
            _name = name;
        }
        public String getName() {
            return _name;
        }
    }

    static class AllocatePagesCommand extends AbstractCommand implements Command {
        AllocatePagesCommand(String name) {
            super(name);
        }
        public void invoke() {
            final long numFree = GUKPagePool.getFreeBulkPages();
            if (numFree <= BULK_ALLOCATION) {
                verboseOut("  insufficient bulk pages: " + numFree);
                return;
            }

            final int n = _rand.nextInt((int) numFree  - BULK_ALLOCATION) + BULK_ALLOCATION + 1;
            final Pointer p = VirtualMemory.allocate(VMTestHelper.fromInt(n * X64VM.PAGE_SIZE), VirtualMemory.Type.DATA);
            final long va = VMTestHelper.toLong(p);
            verboseOut("  allocate: " + n + " result " + Long.toHexString(va) + ", pn: " + (va / X64VM.PAGE_SIZE));
            if (va != 0) {
                _allocList.put(va, n * X64VM.PAGE_SIZE);
            }
        }
    }

    static class DeallocatePagesCommand extends AbstractCommand implements Command {
        DeallocatePagesCommand(String name) {
            super(name);
        }
        public void invoke() {
            final int listSize = _allocList.size();
            if (listSize == 0) {
                verboseOut("  nothing to deallocate");
                return;
            }
            int n = _rand.nextInt(listSize);
            long va = 0;
            int size = 0;
            for (Map.Entry<Long, Integer> entry : _allocList.entrySet()) {
                if (n == 0) {
                    va = entry.getKey();
                    size = entry.getValue();
                    break;
                }
                n--;
            }

            VirtualMemory.deallocate(VMTestHelper.fromLong(va), VMTestHelper.fromInt(size), VirtualMemory.Type.DATA);
            verboseOut("  deallocate: " + Long.toHexString(va) + ", size " + size / X64VM.PAGE_SIZE);
            _allocList.remove(va);
        }
    }

    static class IncreasePoolCommand extends AbstractCommand implements Command {
        IncreasePoolCommand(String name) {
            super(name);
        }
        public void invoke() {
            final long current = GUKPagePool.getCurrentReservation();
            final long maxIncrease = _memMax - current;
            if (maxIncrease > 0) {
                final long increase = _rand.nextInt((int) maxIncrease);
                verboseOut("  increase " + increase + " result " + GUKPagePool.increasePagePool(increase));
            } else {
                verboseOut("  cannot increase pool");
            }
        }
    }

    static class DecreasePoolCommand extends AbstractCommand implements Command {
        DecreasePoolCommand(String name) {
            super(name);
        }
        public void invoke() {
            final long maxDecrease = GUKPagePool.decreaseablePagePool();
            if (maxDecrease > 0) {
                final long decrease = _rand.nextInt((int) maxDecrease) + 1;
                verboseOut("  decrease " + decrease + " result " + GUKPagePool.decreasePagePool(decrease));
            } else {
                verboseOut("  cannot decrease pool");
            }
        }
    }
}
