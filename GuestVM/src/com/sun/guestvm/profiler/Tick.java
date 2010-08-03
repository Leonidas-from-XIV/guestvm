package com.sun.guestvm.profiler;

import java.util.*;

import com.sun.max.vm.compiler.target.*;
import com.sun.max.vm.*;
import com.sun.max.vm.runtime.*;
import com.sun.max.vm.stack.*;
import com.sun.max.vm.thread.VmThread;
import com.sun.max.unsafe.*;

/**
 * Tick profiler. Runs a thread that periodically wakes up, stops all the threads, and records their state.
 *
 * @author Mick Jordan
 *
 */
public final class Tick extends Thread {

    private static final int DEFAULT_FREQUENCY = 50;
    private static final Random rand = new Random();;
    private int frequency;
    private int jiggle;

    /**
     * Create a tick profiler with given frequency.
     * @param frequency
     */
    private Tick(int frequency) {
        this.frequency = frequency == 0 ? DEFAULT_FREQUENCY : frequency;
        jiggle = this.frequency / 8;
        setName("Tick-Profiler");
        setDaemon(true);
    }

    public static void create(int frequency) {
        new Tick(frequency).start();
    }

    public void run() {
        while (true) {
            try {
                final int thisJiggle = rand.nextInt(jiggle);
                final int thisPeriod = frequency + (rand.nextBoolean() ? thisJiggle : -thisJiggle);
                Thread.sleep(thisPeriod);
                final long start = System.nanoTime();
                stackTraceGatherer.run();
                final long end = System.nanoTime();
                Log.print("profile at: "); Log.print(start); Log.print(", duration "); Log.println(end - start);
            } catch (InterruptedException ex) {
            }
        }
    }

    private static final class StackTraceGatherer extends FreezeThreads {
        StackTraceGatherer(Pointer.Predicate p) {
            super("Tick Profiler", p);
        }
        @Override
        public void doThread(Pointer threadLocals, Pointer ip, Pointer sp, Pointer fp) {
            final VmStackFrameWalker stackFrameWalker = VmThread.fromVmThreadLocals(threadLocals).stackDumpStackFrameWalker();
            stackFrameWalker.inspect(ip, sp, fp, stackFrameDumper);
        }
    }

    private static final StackTraceGatherer stackTraceGatherer = new StackTraceGatherer(new IsNotCurrentThread());

    public static final class IsNotCurrentThread implements Pointer.Predicate {

        public boolean evaluate(Pointer vmThreadLocals) {
            return vmThreadLocals != VmThread.current().vmThreadLocals();
        }
    }

    private static final FrameDumper stackFrameDumper = new FrameDumper();

    private static class FrameDumper implements RawStackFrameVisitor {
        public boolean visitFrame(TargetMethod targetMethod, Pointer ip, Pointer sp, Pointer fp, boolean isTopFrame) {
            return true;
        }
    }
}
