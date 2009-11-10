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
package com.sun.guestvm.net.tcp;
//
// TCPRecvQueue.java
//
// Implementation of the TCP state machine receive queue.
//
// sritchie -- Apr 96
//
// notes
//
// The receive queue is currently implemented by a FIFO byte array
// that we copy all data into.  We might want to get fancy and
// piggyback on the user's buffer in the future.
//
// There is an issue of how much memory to allow this queue to consume.
//

/*
 * There is no (additional) synchronization necessary in this class as all calls are made holding the
 * lock on the associated TCP instance.
 */

import com.sun.guestvm.net.Packet;


public class TCPRecvQueue {

    int bytesQueued;

    private int start;
    private int end;

    private byte buf[];

    private static boolean checked;

    public TCPRecvQueue(int size) {
        if (!checked) {
            debug = System.getProperty("guestvm.net.tcp.debug") != null;
            checked = true;
        }
        buf = new byte[size];
    }

    void append(Packet pkt) {

        int len = pkt.dataLength();

        //dprint("append " + len + " bytes to queue of " + bytesQueued);

        int n = len;
        if (n > buf.length - end ) {
            n = buf.length - end;
        }

        pkt.getBytes(0, buf, end, n);

        if (len > n) {
            end = len - n;
            pkt.getBytes(n, buf, 0, end);
        } else {
            end += len;
            if (end >= buf.length) {
                end = 0;
            }
        }

        bytesQueued += len;
    }

    int read(byte dst[], int dst_off, int len) {

        if (len > bytesQueued) {
            len = bytesQueued;
        }

        int n = len;
        if (n > buf.length - start) {
            n = buf.length - start;
        }

        System.arraycopy(buf, start, dst, dst_off, n);

        if (len > n) {
            start = len - n;
            System.arraycopy(buf, 0, dst, dst_off + n, start);
        } else {
            start += len;
            if (start >= buf.length) {
                start = 0;
            }
        }

        bytesQueued -= len;
        return len;
    }

    void cleanup() {
        buf = null;
    }

    //------------------------------------------------------------------------

    private static boolean debug = false;

    private static void dprint(String mess) {
        if (debug == true) {
            System.err.println("TCPRecvQueue: [" + Thread.currentThread().getName() + "]" + mess);
        }
    }

    private static void err(String mess) {
        System.err.println("TCPRecvQueue: " + mess);
    }
}
