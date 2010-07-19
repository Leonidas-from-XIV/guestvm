package com.sun.guestvm.profiler;

import java.util.*;
/**
 * Tick profiler. Runs a thread that periodically wakes up, stops all the threads, and records their state.
 *
 * @author Mick Jordan
 *
 */
public class Tick implements Runnable {

    private Random rand;
    private int frequency;
    private int jiggle;

    /**
     * Create a tick profiler with given frequency.
     * @param frequency
     */
    Tick(int frequency) {
        this.frequency = frequency;
        jiggle = frequency / 8;
    }

    public void run() {
        while (true) {
            try {
                final int thisJiggle = rand.nextInt(jiggle);
                final int thisPeriod = frequency + (rand.nextBoolean() ? thisJiggle : -thisJiggle);
                Thread.sleep(thisPeriod);

            } catch (InterruptedException ex) {
            }

        }
    }

}
