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
package com.sun.max.ve.igmp;

import java.util.*;

/**
 * Class representing each active multicast group
 *
 * One of these is created for every group we're a member of
 * (except "all-hosts")
 * When created, we send out an unsolicited membership report to announce
 * to local routers that we exist.
 */

public class GroupEntry extends TimerTask {
    private static final long IGMP_MAX_DELAY = 10000; // 10 seconds

    private static java.util.Random     rand;

    int                 group;          // 28-bit group number
    Timer     timer;          // non-null indicates "delaying" mode
    int                 useCount;       // Keep track of # of clients

    /**
     * Constructor
     *
     * @param         g        Group number
     */
    public GroupEntry(int g) {
        group = g;
        useCount = 1;
        timer = createTimer();
        rand = new java.util.Random(group);
        startReport();
    }

    private Timer createTimer() {
        return new Timer("IGMP_Report", true);
    }

    /**
     * Called when report timer times out
     */
    public void run() {
        timer.cancel();                   // destroy timer
        IGMP.sendReport(group);
    }

    /**
     * Set up so that we send a membership report some random
     * amount of time in the future (less than 10 seconds)
     * If we can't allocate a timer or random # generator,
     * then use default times
     */
    void startReport() {
        if (timer != null) {              // In delaying state already, return
            return;
        }

        timer = createTimer();
        timer.schedule(this, (rand.nextLong() % IGMP_MAX_DELAY));
    }

    /**
     * Clean up so that timer doesn't fire in the future and do
     * bad things.
     */
    void cancelReport() {               // Called to clean up outstanding
        if (timer != null) {
            // timer.stop();
            timer.cancel();
            timer = null;
        }
    }
}
