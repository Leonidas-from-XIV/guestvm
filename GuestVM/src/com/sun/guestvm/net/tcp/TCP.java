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

/* Although this state machine was implemented from scratch, you
 * may find a line or two of the BSD code here.  The following copyright
 * notice therefore also applies:
 */

/*
 * Copyright (c) 1982, 1986, 1988, 1990, 1993, 1994
 *        The Regents of the University of California.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following Conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of Conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of Conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *        This product includes software developed by the University of
 *        California, Berkeley and its contributors.
 * 4. Neither the name of the University nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 *        @(#)tcp_input.c        8.5 (Berkeley) 4/10/94
 */
package com.sun.guestvm.net.tcp;

import java.io.*;
import java.util.*;

import com.sun.guestvm.net.*;
import com.sun.guestvm.net.debug.*;
import com.sun.guestvm.net.icmp.*;
import com.sun.guestvm.net.ip.*;


/* Here's the big TCP state machine.  This class does lots of things.

   Packets are received from IP into the state machine by input().  The local
   instance variable 'state' describes which method should process the
   packet after input().

   read(), write(), connect() and close() are implemented here.  A receive
   queue buffers packets until they are read by users

   The method output() handles packet formatting and output to the IP layer.

   TCPSendQueue manages bytes that are sent but not acknowledged.
   TCPRecvQueue manages bytes that are received but not delivered to
   the application.
*/

public final class TCP extends IP {
    private static boolean _debug;

    // The TCP connection state.
    enum State {
        CLOSED,
        LISTEN,
        SYN_SENT,
        SYN_RCVD,
        ESTABLISHED,
        CLOSE_WAIT,
        FIN_WAIT_1,
        CLOSING,
        LAST_ACK,
        FIN_WAIT_2,
        TIME_WAIT,
        NEW
    }

    // the state that this connection is in.
    private State _state;

    // The connection endpoint addresses.  local_ip is assumed to be
    // the local host address, so we don't need to store it here.
    // Not having local_ip here means we can't support more than one
    // local IP address at a time (useful for multiple network interfaces).
    // However, we do save the cost of storing and bookkeeping the value.
    int _localPort;
    int _remotePort;
    int _remoteIp;

    // these two variables control the acceptance of incoming connections.
    private TCP _listener;
    private TCP _incomingConnection;

    // timer management
    private static final int DELAYED_ACK_MSEC = 50;
    private TCPTimer _retransmitTimer;
    private TCPTimer _delayedAckTimer;

    private TCPRecvQueue _recvQueue;

    // Used to cache the packet header size for outgoing packets
    // on a connection.
    private int _hdrLen;

    private TCP _next;     // next TCP object in list
    private TCP _prev;     // prev TCP object in list

    // TCP sequence space state variables.
    private int _snd_una;   // lowest unacknowledged sequence number
    private int _snd_max;  // maximum send sequence number
    private int _snd_wnd;  // window size offered from receiver
    private int _snd_wl1;    // seq number used for last window update
    private int _sndWl2;   // ack number used for last window update
    private int _iss;           // initial send sequence number

    private int rcv_wnd;   // bytes unused in receive window
    private int rcv_nxt;    // next expected receive sequence number
    private int _irs;          // initial receive sequence number
    private int _prev_ack;  // last acknowledgement we have sent
    private boolean _ack_after_read;

    // Number of segments to receive before generating an ACK.
    // Used for the "ack-every-other-segment" algorithm.  Change
    // this to 3 if you want to ACK every third segment, etc.
    private static final int ACK_SEGMENTS = 2;
    private int _ack_segment;  // counter for "ack-every-other-segment"

     // some random constants and variables
    private static int _startTime;   // used to create initial sequence numbers
    private static final int MAXSEGSIZE = 1460;     // maximum segment size
    private static final int RECEIVE_WINDOW = 8760;  // default recv window size

    // this scratch state is used for interactions when we don't
    // have a connection.
    private static TCP _scratchTCP;

    private int _connectFailure;                // Why connect failed
    public  static final int CONN_FAIL_TIMEOUT = 1;
    public  static final int CONN_FAIL_REFUSED = 2;

    private static final int MIN_TCP_HEADER_SIZE = 20;
    private static final int TTL = 64;
    private static final int TOS = 0;

    // TCP header flag bits
    private static final int FIN = 0x1;
    private static final int SYN = 0x2;
    private static final int RST = 0x4;
    private static final int PSH = 0x8;
    private static final int ACK = 0x10;
    private static final int URG = 0x20;

    //----------------------------------------------------------------------

    // some interesting TCP header field offsets
    private static final int SRCPORT_OFFSET = 0;
    private static final int DSTPORT_OFFSET = 2;
    private static final int SEQ_OFFSET     = 4;
    private static final int ACK_OFFSET     = 8;
    private static final int HLEN_OFFSET    = 12;
    private static final int FLAGS_OFFSET   = 13;
    private static final int WINDOW_OFFSET  = 14;
    private static final int CKSUM_OFFSET   = 16;
    private static final int MSS_OFFSET     = 20;

    //----------------------------------------------------------------------

    // state storage for the incoming packet.
    private static int inp_seq;
    private static int inp_ack;
    private static int inp_flags;
    private static int inp_wnd;

    private static int inp_len;   // length of data portion of input segment

    //----------------------------------------------------------------------

    private static final int MAX_TCP_CONNECTIONS = 20;

    // linked list of active TCP objects
    private static TCP tcps;

    //----------------------------------------------------------------------

    //
    // This variable is the number of times retransmission occurred since
    // we last checked this route. This is mainly for dead gateway detection.
    // When the retransmit number is is more than 5, we call Route.checkRoute
    // and reset this counter.
    //
    private int retransmits;

    private int rtt_seq;       // sequence number of the packet we are timing.
    private int rtt_start;     // initial timestamp from rtt of timed segment.
    private int srtt;          // smoothed round trip time estimator
    private int rttvar;        // mean deviation

    // current timeout value in msec.
    private int rtx_timeout;

    // current rtt timestamp clock, automatically updated
    // by the RoundTripTimer.
    static int rtt_clock = 0;

    private static Timer rttTimer;
    private static final long RTT_TICK_MSEC = 500;


    // minimum of 1 second timeout, maximum 64 second timeout.
    private static final int RTX_TIMEOUT_MIN  = 1000;
    private static final int RTX_TIMEOUT_MAX  = 64000;
    private static final int RTX_TIMEOUT_INIT = 1000;
    private static final int RTTVAR_INIT = RTX_TIMEOUT_INIT * 4;

    // maximum number of timeouts before aborting the connection.
    private static final int MAX_TIMEOUTS = 3;

    // Reason for aborting the connection, to be used for exception
    // reporting to upper layer. (eg, "Connection timed out")
    private String _reason;

    private static Random _random;

    /**
     * Initialization of the TCP universe.
     */
    public static void init() {
        _debug = System.getProperty("guestvm.net.tcp.debug") != null;
        _scratchTCP = new TCP();
    }

    private TCP() {
        if (rttTimer == null) {
            _random = new Random();
            _startTime = (int) System.currentTimeMillis();
            rttTimer = new Timer("TCP Round Trip Timer", true);
            rttTimer.scheduleAtFixedRate(new RoundTripTask(), RTT_TICK_MSEC, RTT_TICK_MSEC);
        }

        _state = State.NEW;
        _localPort = 0;

        _retransmitTimer = new TCPTimer("TCP Retransmit Timer");
        _delayedAckTimer = new TCPTimer("TCP DelayedAck Timer");

        // cache the TCP packet header length.
        _hdrLen = headerHint();

        _recvQueue = new TCPRecvQueue(RECEIVE_WINDOW);

        rtx_timeout = RTX_TIMEOUT_INIT;
        rtt_start = 0;
        srtt = 0;
        rttvar = RTTVAR_INIT;
    }


    static TCP get() {

        final TCP t = new TCP();

        // stick this new TCP object on the list of active TCP objects
        if (tcps != null) {
            t._next = tcps;
            tcps._prev = t;
        }
        tcps = t;

        return t;
    }

    // Remove this TCP object from the list of active tcp objects.
    private void recycle() {

        // This is a simple removal from a double-linked list.
        if (this == tcps) {
            tcps = _next;
        } else {
            _prev._next = _next;
        }
        if (_next != null) {
            _next._prev = _prev;
        }
        _next = null;
        _prev = null;
    }


    //----------------------------------------------------------------------


    // General TCP output routine.
    // Please try to keep the common path of this routine as
    // uncluttered as possible.  Any code you add here will likely
    // slow things down.
    private void output(Packet pkt, int flags, int seq, int ack)
        throws NetworkException {

        tcpOutSegs++;

        if ((flags & ACK) != 0) {
            _delayedAckTimer.cancelTask();
            _prev_ack = ack;  // remember this ACK for delayed ACK processing.
            _ack_segment = 0; // reset "ack-every-other-segment" counter.
        }

        //
        // make room for TCP header and fill in stuff that is specific for
        // SYN segments, IP Option MSS related stuff.
        //
        if ((flags & SYN) != 0) {

            pkt.shiftHeader(-24);
            pkt.putByte(6 << 4, HLEN_OFFSET);     // set header length
            pkt.putShort(0x0204, MSS_OFFSET);

            //
            // The MSS we announce is decided as follows.
            // First,
            // IP.getRouteMSS() will return to us one of three values
            // 1460 or 536 or the MSS of the serial line interface.
            // Next,
            // 1) Active Open: (Indicated by absence of ACK)
            //               - val from Route.GetRouteMSS().
            // 2) Passive Open: (Indicated by presence of ACK)
            //               - mss = min(maxSegSize and val from Route.GetRouteMSS().
            //

            short route_mss = IP.getRouteMSS(_remoteIp);

            if ((flags & ACK) != 0) {
                // Passive Open.
                pkt.putShort(min(route_mss, MAXSEGSIZE), 22);
            } else {
                // Active Open.
                pkt.putShort(route_mss, 22);
            }

        } else {
            pkt.shiftHeader(-20);
            pkt.putByte(5 << 4, HLEN_OFFSET);     // set header length
        }

        // put source and destination port nunpers
        pkt.putShort(_localPort, SRCPORT_OFFSET);
        pkt.putShort(_remotePort, DSTPORT_OFFSET);

        pkt.putInt(seq, SEQ_OFFSET);          // set sequence number
        pkt.putInt(ack, ACK_OFFSET);          // set acknowledgement number
        pkt.putByte(flags, FLAGS_OFFSET);     // set the flags
        pkt.putShort(rcv_wnd, WINDOW_OFFSET); // set window size
        pkt.putInt(0, CKSUM_OFFSET);          // clear cksum and urg pointer

        int length = pkt.dataLength();

        // Build the pseudo-header before computing checksum.
        pkt.putInt((IP.IPPROTO_TCP << 16) | length, -12);
        pkt.putInt(IP.getLocalAddress(), -8);
        pkt.putInt(_remoteIp, -4);

        // compute packet checksum and stick it into the header
        int cksum = pkt.cksum(-12, length+12);
        pkt.putShort(cksum, CKSUM_OFFSET);

        if (_debug) dprint("output dst:" + IPAddress.toString(_remoteIp) +
           " state:" + _state + " " + flagsToString(flags) +
            "seq:" + seq + " ack:" + ack + " wnd:" + rcv_wnd);

        // We're finished building the TCP header.  Now send it down to IP,
        // passing our time-to-live and type of service.
         IP.output(pkt, _remoteIp, length,  (TTL<<24) | (IP.IPPROTO_TCP<<16), TOS);
    }


    // General input routine for packets coming up from IP.
    public static void input(Packet pkt, int src_ip) {

        tcpInSegs++;

        try {

            int length = pkt.dataLength();

            // Compute the checksum for the pseudo-header and data before
            // doing anything else.  Create the pseudo header and do cksum().
            pkt.putInt((IP.IPPROTO_TCP << 16) | length, -12);
            if (pkt.cksum(-12, length+12) != 0) {
                if (_debug) dprint("bad checksum!");
                tcpInErrs++;
                return;
            }

            // sanity check header length
            int headerLength = (pkt.getByte(HLEN_OFFSET) & 0xf0) >> 2;
            if (headerLength < MIN_TCP_HEADER_SIZE || headerLength > length) {
                if (_debug) dprint("bad header length: " + headerLength);
                tcpInErrs++;
                return;
            }

            // get some fundamental fields from the header.
            int src_port = pkt.getShort(SRCPORT_OFFSET);
            int dst_port = pkt.getShort(DSTPORT_OFFSET);

            inp_seq = pkt.getInt(SEQ_OFFSET);
            inp_ack = pkt.getInt(ACK_OFFSET);
            inp_flags = pkt.getByte(FLAGS_OFFSET) & 0x3f;
            inp_wnd = pkt.getShort(WINDOW_OFFSET);

            inp_len = length - headerLength;

            // increment past TCP header, ignoring any options for now.
            pkt.shiftHeader(headerLength);


            if (_debug) {
                if (_debug) dprint("input src "+IPAddress.toString(src_ip)+":"+src_port+
                       " dst localhost:" + dst_port +
                       " flags: " + flagsToString(inp_flags) + "seq: " + inp_seq + " ack:" + inp_ack + " wnd:" + inp_wnd + " len:" + inp_len);
            }

            // find the connection object that belongs to this src/dest tuple.
            TCP tcp = find(dst_port, src_ip, src_port);

            if (tcp == null) {

                // There is no connection associated with this segment.
                if (_debug) dprint("can't find state");

                tcp = _scratchTCP;

                tcp._remoteIp = src_ip;
                tcp._remotePort = src_port;
                tcp._localPort = dst_port;

                // Reply with a RST segment.
                tcp.outputRst();
                return;
            }

            if (_debug) {
                if (_debug) dprint("RCVD "+ flagsToString(inp_flags) + "segment in state " + tcp._state);
            }

            switch (tcp._state) {

            case LISTEN:
                // pass the sender's endpoint addresses, but reset
                // it to zero afterwards (tcp is the listener connection).
                tcp._remotePort = src_port;
                tcp._remoteIp = src_ip;
                try {
                    tcp.doListen();
                } catch (Exception ex) {
                    // finish setting up the connection state
                }
                tcp._remotePort = 0;
                tcp._remoteIp = 0;
                break;

            case SYN_SENT:
                tcp.doSynSent(pkt);
                break;

            case SYN_RCVD:
                tcp.doSynRcvd(pkt);
                break;

            case ESTABLISHED:
                tcp.doEstablished(pkt);
                break;

            case CLOSE_WAIT:
                tcp.doCloseWait(pkt);
                break;

            case CLOSING:
                tcp.doClosing(pkt);
                break;

            case TIME_WAIT:
                tcp.doTimeWait(pkt);
                break;

            case FIN_WAIT_1:
            case FIN_WAIT_2:
                tcp.doFinWait(pkt);
                break;

            case LAST_ACK:
                tcp.doLastAck(pkt);
                break;

            case CLOSED:
                // Silently drop the segment.  Don't send RST, because this tcp
                // is valid, but connect() or listen() has not been called yet.
                // Let the remote re-xmit and maybe we will have done listen()
                // or connect() by then.
                return;

            default:
                if (_debug) dprint("unknown state");
            }
        } catch (NetworkException ex) {
            // our callers don't really care
            return;
        }
    }

    //----------------------------------------------------------------------

    // Transmit an RST segment in response to the current segment.
    private void outputRst() throws NetworkException {

        if ((inp_flags & RST) != 0) {
            // the input segment contains a RST, so don't send a reply.
            return;
        }

        Packet pkt = Packet.getTx(_remoteIp, _hdrLen, 0);
        if (pkt == null) {
            return;
        }

        if ((inp_flags & ACK) == 0) {

            int ack = inp_seq + inp_len;

            // if we got a SYN, acknowledge it in this RST segment
            if ((inp_flags & SYN) != 0) {
                ack++;
            }

            output(pkt, RST|ACK, 0, ack);
        } else {
            output(pkt, RST, inp_ack, 0);
        }

        tcpOutRsts++;
    }

    //----------------------------------------------------------------------


    // Test the acceptibility of a segment's Sequence number.
    // Trim off segment data that falls before (duplicate) or
    // beyond our receive window.
    private boolean verifySeq(Packet pkt) throws NetworkException {

        if (inp_seq > rcv_nxt) {
            if (_debug) dprint("verifySeq: ignoring out of sequence");
            return false;
        }

        int todrop = rcv_nxt - inp_seq;

        if (_debug) dprint("verifySeq todrop:" + todrop + " inp_len:" + inp_len);

        if (todrop > 0) {

            if ((inp_flags & SYN) != 0) {
                inp_flags &= ~SYN;  // remove SYN bit
                inp_seq++;          // add for SYN
                todrop--;           // remove one for SYN
            }

            // check if the entire segment is duplicate data
            if ((todrop > inp_len) ||
                ((todrop == inp_len) && ((inp_flags & FIN) == 0))) {

                if (_debug) dprint(_state + " port:" + _localPort +
                    " total duplicate todrop:" + todrop + " rcv_wnd:"+rcv_wnd);

                // Entire segment is duplicate.  The remote probably
                // never got our ACK, so send another.
                if ((inp_flags & RST) == 0) {
                    if (_debug) dprint("sending ACK");
                    send(ACK, _snd_max, rcv_nxt);
                }

                // Abort processing and drop the segment.
                return false;
            }

            // Adjust the segment data offset and sequence number
            // by the amount of dropped data.
            pkt.shiftHeader(todrop);
            inp_seq += todrop;
            inp_len -= todrop;
        }

        if (rcv_wnd == 0) {

            if (inp_len == 0 && inp_seq == rcv_nxt) {
                // Receive window is zero, there is no data in this
                // segment, so continue processing to see if there
                // is a valid ACK, URG, or RST.
                return true;
            }

            if (_debug) dprint(_state + " port:" + _localPort + " inp_len:"+
            inp_len + " todrop:" + todrop + " rcv_wnd:" + rcv_wnd +
            " persist packet?");

        } else if (inp_len >= 0 && rcv_wnd > 0) {

            if (rcv_nxt <= inp_seq && inp_seq < rcv_nxt+rcv_wnd) {
                // Segment data fits within our receive window.
                // Continue processing.
                return true;
            }
        } else {
            if (_debug) dprint(_state + " port:" + _localPort + " inp_len:"+
                  inp_len + " todrop:" + todrop + " rcv_wnd:" + rcv_wnd);
        }

        if ((inp_flags & RST) == 0) {
            // This will acknowledge any out-of-sequence or
            // persist packets.  But it won't acknowledge an RST.
            send(ACK, _snd_max, rcv_nxt);
        }

        return false;
    }


    //----------------------------------------------------------------------

    // Initialize the timing of a segment for round-trip time calculations.
    private final void rttStart(int seq) {

        if (rtt_start == 0) {
            rtt_start = rtt_clock;    // take a timestamp from the rtt clock
            rtt_seq = seq;      // remember seq num of this timed segment
        }
    }


    private static final int SRTT_SHIFT = 3;
    private static final int RTTVAR_SHIFT = 2;

    private void rttUpdate() {

        // get the total time for this segment
        int time = rtt_clock - rtt_start;

        if (srtt != 0) {

            int delta = time - (srtt >> SRTT_SHIFT);

            srtt += delta;
            if (srtt <= 0) {
                    srtt = 1;
            }

            if (delta < 0) {
                delta -= delta;
            }

            delta -= (rttvar >> RTTVAR_SHIFT);

            rttvar += delta;
            if (rttvar <= 0) {
                    rttvar = 1;
            }
        } else {
            srtt = time << SRTT_SHIFT;
            rttvar = time << (RTTVAR_SHIFT-1);
        }

        // Compute the actual timeout value in msec.  Multiply by
        // 500 because the timestamp units come from the rtt counter,
        // which is incremented every 500 msec.
        rtx_timeout = (((srtt >> SRTT_SHIFT-1) + rttvar) >> 1) * 500;

        // err("local_port:" + local_port + " time:" + time + " srtt:" +
        //     srtt + " rttvar:" + rttvar + " timeout:" + rtx_timeout);

        // Make sure timeout doesn't go below our minimum.
        if (rtx_timeout < RTX_TIMEOUT_MIN) {
            rtx_timeout = RTX_TIMEOUT_MIN;
        }

        // reset the round trip timestamp
        rtt_start = 0;
    }


    //----------------------------------------------------------------------

    // Check to see whether the segment's acknowledgement falls within
    // our unacknowledged send window.  If it does, we should be able
    // drop some data from the unacknowledged send queue.
    private boolean verifyAck() {

        // if we didn't get an ACK, ignore this segment.
        if ((inp_flags & ACK) == 0) {
            return false;
        }

        if (_snd_una <= inp_ack && inp_ack <= _snd_max) {

            // This ACK acknowledges some data we sent.  See if this
            // includes our round-trip timer measurement.
            if (rtt_start != 0 && inp_ack > rtt_seq) {
                // use this new timestamp to update our rtt estimate
                rttUpdate();
            }

            // Advance our unacked send pointer and drop the acknowledged
            // data from our send queue.
            sendQueue.drop(inp_ack - _snd_una);
            _snd_una = inp_ack;

            // this is a useful acknowledgement, so we can zero
            // the retransmit counter.
            retransmits = 0;

            if (_snd_una == _snd_max) {
                // All our data has been acked, so stop retrans timer.
                // retransmitTimer.stop();
                _retransmitTimer.cancelTask();
            }

            // Update the send window.  This test prevents old segments
            // from updating the window.
            if (_snd_wl1 < inp_seq ||
                (_snd_wl1 == inp_seq && _sndWl2 <= inp_ack)) {

                _snd_wnd = inp_wnd;
                _snd_wl1 = inp_seq;
                _sndWl2 = inp_ack;
            }
        } else if (inp_ack > _snd_max) {

            // This ack is for data we haven't sent yet.  Reply with
            // an ACK to help resynch.
            //out_flags |= ACK;
            if (_debug) dprint("verifyAck inp_ack:" + inp_ack + " > snd_max:" + _snd_max);
            return false;

        } //else { the ack is a duplicate and can be ignored. }

        return true;
    }


    //----------------------------------------------------------------------

    // Simple utility routine to send a segment.
    private void send(int flags, int seq, int ack) throws NetworkException {

        int len = _hdrLen;
        if ((flags & SYN) != 0) {
            len += 4;  // add 4 for MSS option
        }

        Packet pkt = Packet.getTx(_remoteIp, len, 0);
        if (pkt != null) {
            output(pkt, flags, seq, ack);
        }
    }


    //----------------------------------------------------------------------

    private void doListen() throws NetworkException {

        // ignore any RST packets
        if ((inp_flags & RST) != 0) {
            return;
        }

        if ((inp_flags & ACK) != 0) {
            outputRst();
            return;
        }

        // throw away anything that is not SYN (and only SYN)
        if (inp_flags != SYN) {
            return;
        }

        // Check if there is already a connection pending
        // on the incoming connection queue.
        if (_incomingConnection != null) {
            // There is already a connection pending.  Ignore this
            // connection request for now (they will retransmit and
            // hopefully then we will be ready for it).
            return;
        }

        // May want to move this get() into TCPEndpoint.
        TCP tcp = get();
        if (tcp == null) {
            if (_debug) dprint("LISTEN: no more connections");
            tcp.outputRst();
            return;
        }

        // insert the new incoming connection in our simple queue.
        _incomingConnection = tcp;

        // Make a backpointer from the new connection to the listener
        // so that when the new connection completes, the listener
        // can be notified.
        tcp._listener = this;

        // save the remote port number and address
        tcp._remotePort = _remotePort;
        tcp._remoteIp = _remoteIp;
        tcp._localPort = _localPort;

        inp_flags = inp_flags & ~SYN;    // remove SYN and
        inp_seq++;                       // increment past it.

        tcp.rcv_nxt = inp_seq;
        tcp._irs = inp_seq;

        // choose and advance the initial sequence number.
        // tcp.iss = ((int) System.currentTimeMillis() - startTime) * 128;
        tcp._iss = ((int) _random.nextLong() - _startTime) * 128;

        tcp._snd_una = tcp._iss;
        tcp._snd_max = tcp._iss+1;
        tcp._snd_wnd = inp_wnd;
        tcp.rcv_wnd = RECEIVE_WINDOW;

        tcp.sendQueue = new TCPSendQueue(inp_wnd);

        tcp._state = State.SYN_RCVD;
        tcpPassiveOpens++;

        tcp.send(SYN|ACK, tcp._iss, tcp.rcv_nxt);

        // take the initial rto timestamp for this connection.
        rttStart(tcp._iss);

        tcp._retransmitTimer.schedule(new RetransmitTask(), rtx_timeout);
    }

    private void doSynSent(Packet pkt) throws NetworkException {

        boolean got_ack = false;

        if ((inp_flags & ACK) != 0) {
            if (inp_ack <= _iss || inp_ack > _snd_max) {
                if (_debug) dprint("SYN_SENT iss:"+_iss+" max:"+_snd_max+" inp_ack:"+inp_ack);
                outputRst();
                return;
            }

            got_ack = true;

            // our SYN is acknowledged, so stop retrans timer.
            _retransmitTimer.cancelTask();
       }

        if ((inp_flags & RST) != 0) {
            if (got_ack == true) {
                if (_debug) dprint(_state + " got RST -- connection refused");
                _connectFailure = CONN_FAIL_REFUSED;
                cleanup("Connection refused");
            }

            return;
        }

        if ((inp_flags & SYN) != 0) {

            if (_debug) dprint(_state + " got SYN");

            rcv_nxt = inp_seq+1;
            _irs = inp_seq;

            _snd_una = inp_ack;
            _snd_wnd = inp_wnd;
            _snd_wl1 = inp_seq;
            _sndWl2 = inp_ack;

            sendQueue = new TCPSendQueue(_snd_wnd);

            if (_snd_una > _iss) {
                // our SYN has been ACKed
                if (_debug) dprint("SYN is acked, going to ESTABLISHED");
                _state = State.ESTABLISHED;

                _delayedAckTimer.schedule(DELAYED_ACK_MSEC*4);

                syncNotify();

            } else {
                if (_debug) dprint("SIMULTANEOUS OPEN, going to SYN_RCVD");
                _state = State.SYN_RCVD;

                send(SYN|ACK, _iss, rcv_nxt);

                 _retransmitTimer.schedule(rtx_timeout);
            }
        }
    }

    private synchronized void syncNotify() {
        notify();
    }

    private void doSynRcvd(Packet pkt) throws NetworkException {

        if (verifySeq(pkt) != true) {
            if (_debug) dprint("verifySeq() failed");
            return;
        }

        // kill our TCP connection if we get a RST
        if ((inp_flags & RST) != 0) {
            cleanup("Connection reset by peer");
            return;
        }

        // SYN here is also catastrophic -- kill our connection
        if ((inp_flags & SYN) != 0) {
            try {
                outputRst();
            } finally {
                cleanup("Connection aborted");
            }
            return;
        }

        // if we didn't get an ACK, ignore this segment.
        if ((inp_flags & ACK) == 0) {
            return;
        }

        // if the ACK is not what we were expecting,
        // throw it away and respond with RST.
        if (inp_ack < _snd_una || inp_ack > _snd_max) {
            outputRst();
            return;
        }

        // update our notion of the sender's advertised window.
        _snd_wnd = inp_wnd;
        _snd_wl1 = inp_seq;
        _sndWl2 = inp_ack;

        if (_debug) dprint("SYN_RCVD got ACK, going to ESTABLISHED");
        _state = State.ESTABLISHED;

        _retransmitTimer.cancelTask();

        // Tell any threads blocked on accept() that a new connection
        // is available.
        synchronized (_listener) {
            _listener.notify();
        }
        _listener = null;  // don't need listener after this point.

        // continue to do processing in the ESTABLISHED state.
        doEstablished(pkt);
    }

    private void doEstablished(Packet pkt) throws NetworkException {

        // Check the sequence number and data length in the segment.
        // Trim off any excess and filter out really bad ones.
        if (verifySeq(pkt) != true) {
            if (_debug) dprint("verifySeq(pkt) false");
            return;
        }

        // At this point we are guaranteed to have a valid sequence
        // field with any data overlap removed.

        if ((inp_flags & RST) != 0) {
            cleanup("Connected reset by peer");
            return;
        }

        if ((inp_flags & SYN) != 0) {
            outputRst();
            cleanup("Connection aborted");
            return;
        }

        if (verifyAck() == false) {
            if (_debug) dprint("verifyAck() false");
            return;
        }

        if ((inp_flags & URG) != 0) {
            if (_debug) dprint("ESTABLISHED ignoring URG flag");
        }

        if (inp_len > 0) {
            recvData(pkt);
        }

        if ((inp_flags & FIN) != 0) {

            // advance rcv_nxt over the FIN and do a delayed
            // acknowledgement for it (we will probably call close()
            // soon which will piggyback an ACK).
            rcv_nxt++;
            _delayedAckTimer.schedule(DELAYED_ACK_MSEC*4);

            _state = State.CLOSE_WAIT;

            // tell the upper layer that the receive stream has been closed.
            syncNotify();
        }
    }


    private void doCloseWait(Packet pkt) throws NetworkException {

        if (verifySeq(pkt) != true) {
            return;
        }

        if ((inp_flags & RST) != 0) {
            cleanup("Connection reset by peer");
            return;
        }

        if ((inp_flags & SYN) != 0) {
            outputRst();
            cleanup("Connected aborted");
            return;
        }

        if (verifyAck() == false) {
            return;
        }

        if ((inp_flags & (URG|FIN)) != 0) {
            if (_debug) dprint("CLOSE_WAIT ignoring " + flagsToString(inp_flags & (URG|FIN)));
        }

        if (inp_len > 0) {
            if (_debug) dprint("CLOSE_WAIT ignoring data len = " + inp_len);
        }
    }


    private void doClosing(Packet pkt) throws NetworkException {

        if (verifySeq(pkt) != true) {
            return;
        }

        if ((inp_flags & RST) != 0) {
            cleanup("Connection reset by peer");
            return;
        }

        if ((inp_flags & SYN) != 0) {
            outputRst();
            cleanup("Connection aborted");
            return;
        }

        if (verifyAck() == false) {
            return;
        }

        // If this segment acknowledges our FIN, we are done.
        if (inp_ack >= _snd_una) {
            //state = State.TIME_WAIT;
            cleanup("Connection closed");
            return;
        }

        if ((inp_flags & (URG|FIN)) != 0) {
            if (_debug) dprint("CLOSING ignoring " + flagsToString(inp_flags & (URG|FIN)));
        }

        if (inp_len > 0) {
            if (_debug) dprint("CLOSING ignoring data len = " + inp_len);
        }
    }

    // The processing of FIN_WAIT_1 is so similar to FIN_WAIT_2 that we can
    // use the same routine with some minor special cases.
    private void doFinWait(Packet pkt) throws NetworkException {

        if (verifySeq(pkt) != true) {
            return;
        }

        if ((inp_flags & RST) != 0) {
            cleanup("Connection reset by peer");
            return;
        }

        if ((inp_flags & SYN) != 0) {
            outputRst();
            cleanup("Connection aborted");
            return;
        }

        if (verifyAck() == false) {
            return;
        }

        if (_debug) dprint(_state + " ack:" + inp_ack + " snd_una:" + _snd_una);

        // if this segment acknowledges the FIN we sent, go to FIN_WAIT_2
        if (inp_ack >= _snd_una) {
            _state = State.FIN_WAIT_2;
        }

        if ((inp_flags & URG) != 0) {
            if (_debug) dprint(_state + " ignoring URG");
        }

        if (inp_len > 0) {
            // Since our API doesn't support half-close, any data received
            // in this segment will never get read.  Therefore this connection
            // will stay in limbo forever, or until the remote times out.
            // To avoid this, we send a RST and blow away the connection.

            //err(state + " port:" + local_port + " half-close inp_len:"+
            //        inp_len + " rcv_wnd:" + rcv_wnd);

            outputRst();
            cleanup("Connection aborted");

            return;

            // Uncomment this if we need to get half-close recvData working.
            // process any data received in this segment.
            //recvData(pkt);
        }

        if ((inp_flags & FIN) != 0) {

            // advance rcv_nxt over the FIN and acknowledge it.
            rcv_nxt++;
            send(ACK, _snd_max, rcv_nxt);

            if (_state == State.FIN_WAIT_2) {
                //state = State.TIME_WAIT;
                cleanup("Connection closed");
            } else {
                _state = State.CLOSING;
            }

            syncNotify();
        }
    }

    // we don't implement TIME_WAIT just yet.
    private void doTimeWait(Packet pkt) {

        if (_debug) dprint("TIME_WAIT got segment flags = " + flagsToString(inp_flags));
    }


    private void doLastAck(Packet pkt) throws NetworkException {

        if (verifySeq(pkt) != true) {
            return;
        }

        if ((inp_flags & RST) != 0) {
            cleanup("Connection reset by peer");
            return;
        }

        if ((inp_flags & SYN) != 0) {
            cleanup("Connection aborted");
            return;
        }

        if (verifyAck() != true) {
            return;
        }

        if (_snd_una == _snd_max) {
            if (_debug) dprint("LAST_ACK closing connection");
            cleanup("Connection closed");
        }
    }

    //----------------------------------------------------------------------

    private void cleanup(String str) throws NetworkException {

        _reason = str;

        if (_debug) dprint("cleanup() local:" + _localPort + " reason: " + _reason);

        // If TCP was in LISTEN state, it might have received a
        // connection that is still not accepted by the upper layer.
        // Need to cleanup this connection too.
        if (_state == State.LISTEN && _incomingConnection != null) {

          //err("cleanup pending connection " +
          //    states[incomingConnection.state] + " port:" + local_port);

            // To cleanup pending incoming connections, the spec says
            // to send a FIN and go through the usual four way handshake.
            // We can't do that for pending connections in the SYN_RCVD
            // state because we don't have the logic to handle
            // SYN_RCVD --> FIN_WAIT_1 properly. (Can't reliably retransmit
            // our SYN if it got lost.)
            if (_incomingConnection._state == State.SYN_RCVD) {
                // It would be nice to send RST here, but we don't know
                // what ack or seq values to use.  The peer will retransmit
                // and then receive a RST.
                _incomingConnection.cleanup("Connection closed");
            } else {
                // close the pending connection gracefully.
                _incomingConnection.close();
            }
            _incomingConnection = null;
        }

        // Update some SNMP stats
        if (_state == State.SYN_SENT || _state == State.SYN_RCVD) {
            tcpAttemptFails++;
            }
        else if (_state == State.ESTABLISHED || _state == State.CLOSE_WAIT) {
            tcpEstabResets++;
            }

        _state = State.CLOSED;

         _retransmitTimer.cancelTask();
        _delayedAckTimer.cancelTask();

        if (sendQueue != null) {
            sendQueue.cleanup();
        }
        if (_recvQueue != null) {
            _recvQueue.cleanup();
        }

        // make sure anyone blocked on read() or waitForConnection() gets
        // woken up.
        synchronized (this) {
            notifyAll();
        }

        _listener = null;

        // remove this object from the TCP state list
        recycle();
    }


    //----------------------------------------------------------------------

    // Called when the retransmit timer goes off.  This mainly involves
    // figuring out what state we are in, and retransmitting the packet
    // that is appropriate in that state.

    void retransmit() throws NetworkException {

        // Invalidate the round-trip timer.  rtt must only be calculated
        // for non-retransmitted segments.
        rtt_start = 0;

        // double the next retransmission timeout, up to a limit.
        rtx_timeout = rtx_timeout << 1;
        if (rtx_timeout > RTX_TIMEOUT_MAX) {
            rtx_timeout = RTX_TIMEOUT_MAX;
        }

        if (_debug) dprint("TIMEOUT:" + retransmits + " to:" + rtx_timeout + " local:" +
            _localPort + " state: " + _state);

        retransmits++;
        if (retransmits % 5 == 0) {
            Route.checkRoute(_remoteIp);
        } else if (retransmits >= MAX_TIMEOUTS) {
            _connectFailure = CONN_FAIL_TIMEOUT;        // to remember why connect failed
            cleanup("Connection timed out");
            return;
        }

        if (_debug) dprint("retransmit: " + _state);
        switch (_state) {

        case SYN_SENT:
            // send a new SYN packet.
            send(SYN, _iss, 0);
            break;

        case SYN_RCVD:
            // send a new SYN|ACK packet.
            send(SYN|ACK, _iss, rcv_nxt);
            break;

        case ESTABLISHED:
        case CLOSE_WAIT:
            // dump the output queue to the net.
            outputData(_snd_una, _snd_max);
            break;

        case FIN_WAIT_1:
            // it would be cool if we could retransmit SYN here
            // to handle the SYN_RCVD --> FIN_WAIT_1 transition...
        case LAST_ACK:
            // Send any data still in our transmit queue.
            outputData(_snd_una, _snd_max-1);  // subtract 1 for FIN
            tcpRetransSegs++;

            // send a FIN segment, subtracting 1 for the previously sent FIN
            send(FIN|ACK, _snd_max-1, rcv_nxt);
            break;

        default:
            if (_debug) dprint("BAD RETRANSMIT STATE: " + _state);

            // don't restart the timer.
            return;
        }

        tcpRetransSegs++;

        _retransmitTimer.schedule(new RetransmitTask(), rtx_timeout);
    }


    // This is called when the delayed ACK timer goes off.  Send an ACK
    // segment.  Don't restart the delayed ACK timer.
    void delayedAck() throws NetworkException {

        //err("delayedAck port:"+local_port);

        // We could try to get fancy here about piggybacking this ACK
        // with data, but for now we keep it simple.
        send(ACK, _snd_max, rcv_nxt);
    }


    //----------------------------------------------------------------------

    protected static int headerHint() {
        return 14 + 20 /* IP.headerHint() */ + MIN_TCP_HEADER_SIZE;
    }

    /**
     * Establish a TCP connection to a remote endpoint
     *
     * @param        addr IP address of remote endpoint
     * @param        p    port number
     * @return         0    if connected
     *                     TCP.CONN_FAIL_REFUSED if the connection is refused
     *                     TCP.CONN_FAIL_TIMEOUT if the connection request times out
     */
    int connect(int addr, int p) throws InterruptedException, NetworkException {

        if (_state != State.NEW || addr == 0 || addr == 0xffffffff) {
            return CONN_FAIL_REFUSED;
        }

        // If the user hasn't done a bind(), then we don't have a
        // local port yet.
        if (_localPort == 0) {
            setLocalPort(0);
        }

        // substitute our real local address for localhost.
        if (addr == 0x7f000001) {
            addr = IP.getLocalAddress();
        }

        // initialize the TCP state and send a SYN segment.
        _remotePort = p;
        _remoteIp = addr;

        // choose and advance the initial sequence number.
        // This computation has the effect of incrementing the iss
        // by 128000 every second, which is good according to BSD.
        _iss = ((int) System.currentTimeMillis() - _startTime) * 128;
        // iss = ((int) random.nextLong() - startTime) * 128;

        if (_debug) dprint("connect: ISS: " + _iss);

        _snd_una = _iss;
        _snd_max = _iss+1;
        _snd_wnd = 0;
        rcv_wnd = RECEIVE_WINDOW;

        _state = State.SYN_SENT;
        tcpActiveOpens++;

        _retransmitTimer.schedule(rtx_timeout);

        // Send a SYN segment out the network
        send(SYN, _iss, 0);

        // take the initial rto timestamp for this connection.
        rttStart(_iss);

        // now wait for the connection to be established
        while (_state != State.ESTABLISHED) {
            if (_state == State.CLOSED) {
                return _connectFailure;
            }

            synchronized (this) {
                wait();
            }
       }
        return 0;
    }

    boolean close() throws NetworkException {
        if (_state == State.ESTABLISHED) {
            _state = State.FIN_WAIT_1;
        } else if (_state == State.CLOSE_WAIT || _state == State.LAST_ACK) {
            _state = State.LAST_ACK;
        } else if (_state == State.NEW) {
            _state = State.CLOSED;
            return true;
        } else if (_state == State.LISTEN || _state == State.SYN_SENT) {
            cleanup("Connection closed");
            return true;
        } else {
            return false;
        }

        // send a FIN segment
        send(FIN|ACK, _snd_max, rcv_nxt);
        rttStart(_snd_max);

        _snd_max++;   // add one for FIN

        _retransmitTimer.schedule(rtx_timeout);

        return true;
    }

    private void sendUnConditionalRST() {
        Packet pkt = Packet.getTx(_remoteIp, _hdrLen, 0);
        if (pkt != null) {
            try {
                output(pkt, RST|ACK, _snd_max, rcv_nxt);
            } catch (NetworkException ex) {
            }
        }

    }

    static void closeAll() {
        TCP nxt = null;
        TCP cur = tcps;

        while (cur != null) {
            nxt = cur._next;
            if (cur._state == State.ESTABLISHED) {
                cur.sendUnConditionalRST();
            }
            cur = nxt;
        }
    }

    private TCPSendQueue sendQueue;

    private static final int min(int a, int b) {
        if (a < b) {
            return a;
        }
        return b;
    }

    // output bytes in the send queue between the two given sequence numbers
    private void outputData(int from, int to) throws NetworkException {

        int numBytes = to - from;
        int snd_nxt = from;

        if (_debug) dprint("outputData numBytes:" + numBytes);

        while (numBytes > 0) {

            int n = min(numBytes, MAXSEGSIZE);

            Packet pkt = sendQueue.getPacket(_remoteIp, snd_nxt - _snd_una, _hdrLen, n);
            if (pkt == null) {
                // No packets are available, so abort sending data for now.
                // We will eventually retransmit.
                return;
            }

            output(pkt, ACK|PSH, snd_nxt, rcv_nxt);
            rttStart(snd_nxt);

            snd_nxt += n;
            numBytes -= n;
        }
    }

    boolean write(byte buf[], int off, int len)
        throws InterruptedException, NetworkException {

        if (_debug) dprint("write length = " + len);

        if (_state != State.ESTABLISHED && _state != State.CLOSE_WAIT) {
            return false;
        }

        // loop until we have queued and transmitted all data.
        while (len > 0) {

            // this will block until room is available
            int bytesAppended = sendQueue.append(buf, off, len);

            outputData(_snd_max, _snd_max + bytesAppended);

            // start the retransmit timer if necessary.
            _retransmitTimer.schedule(rtx_timeout);

            _snd_max += bytesAppended;
            off += bytesAppended;
            len -= bytesAppended;
        }

        if (_debug) dprint("done write");
        return true;
    }


    //----------------------------------------------------------------------


    private void recvData(Packet pkt) throws NetworkException {

        _recvQueue.append(pkt);

        rcv_nxt += inp_len;     // advance next expected seq number.
        rcv_wnd -= inp_len;     // decrement receive window size

        if (rcv_wnd < MAXSEGSIZE) {
            _ack_after_read = true;
        }

        // Tell the user thread blocked on read() that there is some
        // data available.
        syncNotify();

        // Figure out if we need to ACK this segment or not.  Various
        // specs say we should ACK every other segment in a stream of
        // full size segments.
        _ack_segment++;
        if (_ack_segment >= ACK_SEGMENTS) {
            send(ACK, _snd_max, rcv_nxt);
        } else {
             _delayedAckTimer.schedule(DELAYED_ACK_MSEC);
        }
    }

    int read(byte buf[], int off, int len, int timeout)
            throws InterruptedException, InterruptedIOException,
            NetworkException {

        if (_state != State.ESTABLISHED) {
            if (_state != State.CLOSE_WAIT || _recvQueue.bytesQueued == 0) {
                return 0;
            }
        }

        if (_recvQueue.bytesQueued == 0) {
            synchronized (this) {
                wait(timeout);
            }

            if (_recvQueue.bytesQueued == 0) {
                throw new InterruptedIOException("read timeout");
            }
        }

        len = _recvQueue.read(buf, off, len);

        // open up the receive window by the number of bytes read.
        rcv_wnd += len;

        // Only send an ACK in ESTABLISHED state. This is to reduce
        // unnecessary ACKs if we read data in while in CLOSE_WAIT
        // for example.
        if (_state == State.ESTABLISHED
                && ((rcv_wnd > MAXSEGSIZE) && _ack_after_read)) {
            _ack_after_read = false;
            send(ACK, _snd_max, rcv_nxt);
        }

        return len;
    }

    // Return the number of bytes immediately available for reading.
    int available() {

        // return an error Condition if the TCP is not in a state
        // for reading.
        if (_state != State.ESTABLISHED) {
            if (_state != State.CLOSE_WAIT || _recvQueue.bytesQueued == 0) {
                return 0;
            }
        }

        return _recvQueue.bytesQueued;
    }


    //----------------------------------------------------------------------

    boolean listen(int count) {

        if (_state != State.NEW) {
            return false;
        }

        _incomingConnection = null;
        _state = State.LISTEN;

        return true;
    }

    TCP waitForConnection(int timeout) throws InterruptedException,
            InterruptedIOException {
        while (_incomingConnection == null) {
            synchronized (this) {
                wait(timeout);
            }
        }
        if (_incomingConnection == null) {
            throw new InterruptedIOException("accept timeout");
        } else {
            TCP t = _incomingConnection;
            _incomingConnection = null;
            return t;
        }
    }

    // simple method to find a connection with the given local port.
    private static TCP find(int port) {

        TCP tcp = tcps;

        while (tcp != null) {
            if (tcp._localPort == port) {
                return tcp;
            }

            tcp = tcp._next;
        }

        return null;
    }

    // allocate a random number generator for the port number chooser.
    private static java.util.Random rand = new java.util.Random();

    // Choose the next available unused port number.
    private int chooseNextPort() {

        int port;

        for (;;) {

            // choose a random port between in the range 32768-65535
            port = (Math.abs(rand.nextInt()) % 32767) + 32768;

            // see if this port is unused.
            if (find(port + 32768) == null) {
                break;
            }
        }

        return port;
    }

    int setLocalPort(int port) {
        if (port == 0) {
            _localPort = chooseNextPort();
        } else if (find(port) == null) {
            _localPort = port;
        } else {
            // TODO: what to do?
        }
        return _localPort;
    }

    static TCP cache = null;

    // Searches through the list of TCP state objects for a match.
    // Returns the object if found, or null otherwise.
    private static final TCP find(int local_port, int remote_ip,
                                  int remote_port) {

        if (_debug) {
            if (_debug) dprint("finding "+local_port+" "+IPAddress.toString(remote_ip)+":"+remote_port);
        }

        if (cache != null &&
            cache._localPort == local_port &&
            cache._remoteIp == remote_ip &&
            cache._remotePort == remote_port) {
            return cache;
        }

        TCP listening = null;

        for (TCP tcp = tcps; tcp != null; tcp = tcp._next) {

            if (tcp._localPort != local_port) {
                continue;
            }

            if (tcp._state == State.LISTEN) {
                listening = tcp;
            }

            if (tcp._remotePort != remote_port) {
                continue;
            }

            if (tcp._remoteIp != remote_ip) {
                continue;
            }

            // found an exact match
            cache = tcp;
            return tcp;
        }

        return listening;
    }


    //----------------------------------------------------------------------

    public static void tcpDestinationUnreachable(int dst_ip, int dst_port,
                                                 int src_port, int code) {

        //err("destUnreach local:" + src_port + " dst_ip:" +
        //    IPAddress.toString(dst_ip) + ":" + dst_port);

        switch(code) {
            //
            // Per rfc1122, we should be blowing away only pcbs in the
            // PROTO_UNREACH and FRAG_REQUIRED states, but we get wedged
            // in SYN_SENT state for other cases such as HOST_UNREACHABLE too.
            // After we implement the timeout for SYN_SENT state we may want to
            // revisit this code.
            //
        case ICMP.NET_UNREACHABLE:
        case ICMP.HOST_UNREACHABLE:
        case ICMP.PROTO_UNREACHABLE:
        case ICMP.PORT_UNREACHABLE:
        case ICMP.FRAG_REQUIRED:
        case ICMP.SOURCE_ROUTE_FAIL:
            //
            // Blow the connection away if we are in SYN_SENT state.
            //

            TCP tcp = TCP.find(src_port, dst_ip, dst_port);
            if (tcp != null) {
                if (tcp._state == State.SYN_SENT) {
                    try {
                        tcp.cleanup("Destination unreachable");
                    } catch (NetworkException e) {
                    }
                }
            }
            break;
        default:
        }

    }

    //----------------------------------------------------------------------

    private static String flagsToString(int flags) {

        String str = "";

        if ((flags & FIN) != 0) str = "FIN ";
        if ((flags & SYN) != 0) str += "SYN ";
        if ((flags & RST) != 0) str += "RST ";
        if ((flags & PSH) != 0) str += "PSH ";
        if ((flags & ACK) != 0) str += "ACK ";
        if ((flags & URG) != 0) str += "URG ";

        return str;
    }

    public String toString() {
        return IPAddress.toString(IP.getLocalAddress()) + ":" +
                _localPort + "   " + IPAddress.toString(_remoteIp) + ":" +
                _remotePort + "    " + _snd_wnd + "  " +
                "n/a  " + rcv_wnd + "  " + _recvQueue.bytesQueued +
                "  " + _state;
    }

    class TCPTimer extends Timer {
        private TimerTask _task;
        TCPTimer(String name) {
            super(name, true);
        }

        void schedule(long delay) {
            schedule(new RetransmitTask(), delay);
        }

        @Override
        public synchronized void schedule(TimerTask task, long delay) {
            _task = task;
            super.schedule(task, delay);
        }

        synchronized void  cancelTask() {
            if (_task != null) {
                 _task.cancel();
                 _task = null;
            }
        }
    }

    class RetransmitTask extends TimerTask {
        public void run() {
            try {
                if (_debug) dprint("retransmit()");
                retransmit();
            } catch (NetworkException ex) {
                return;
            }
        }
    }

    class DelayedAckTask extends TimerTask {
        public void run() {
            try {
                delayedAck();
            } catch (NetworkException ex) {
                return;
            }
        }
    }

    static class RoundTripTask extends TimerTask {
        public void run() {
            rtt_clock++;
        }
    }


    public static void report(java.io.PrintStream out) {

        out.print("TCP stats\n\n");
        out.print("Local Address       Remote Address    tx wnd tx Q  rx wnd rx Q State\n");
        out.print("------------------- ----------------- ------ ----- ------ ---- -----\n");


        TCP tcp = tcps;
        while (tcp != null) {
            out.print(tcp.toString() + "\n");
            tcp = tcp._next;
        }
    }


    //----------------------------------------------------------------------

    static void dprint(String str) {
        if (_debug == true) {
            err(str);
        }
    }

    private static void err(String mess) {
        Debug.println("TCP: [" + Thread.currentThread() + ": " + mess);
    }


    //
    // snmp stuff
    //
    private static int tcpActiveOpens;
    private static int tcpPassiveOpens;
    private static int tcpAttemptFails;
    private static int tcpEstabResets;
    private static int tcpInSegs;
    private static int tcpOutSegs;
    private static int tcpRetransSegs;
    private static int tcpInErrs;
    private static int tcpOutRsts;

    public static int getStatistic(int index){
        switch(index){
        case 1:  // tcpRtoAlgorithm
            return 4;
        case 2:  // tcpRtoMin
            return RTX_TIMEOUT_MIN;
        case 3:  // tcpRtoMax
            return RTX_TIMEOUT_MAX;
        case 4:  // tcpMaxConn
            return -1;
        case 5:
            return tcpActiveOpens;
        case 6:
            return tcpPassiveOpens;
        case 7:
            return tcpAttemptFails;
        case 8:
            return tcpEstabResets;
        case 9:
            {
            TCP tcp;
            int tcpCurrEstab = 0;
            for(tcp = tcps;tcp != null;tcp = tcp._next)
                if(tcp._state == State.ESTABLISHED || tcp._state == State.CLOSE_WAIT)
                    tcpCurrEstab++;
            return tcpCurrEstab;
            }
        case 10:
            return tcpInSegs;
        case 11:
            return tcpOutSegs - tcpOutRsts - tcpRetransSegs;
        case 12:
            return tcpRetransSegs;
        case 14:
            return tcpInErrs;
        case 15:
            return tcpOutRsts;
        default:
            return -1;
        }
    }

    static int getNumConns(){
        TCP tcp;
        int numConns;

        numConns = 0;
        for(tcp = tcps;tcp != null;tcp = tcp._next)
            numConns++;
        return numConns;
    }

    static int getConns(int[][] arr){
        int localIP;
        TCP tcp;
        int i;
        localIP = IP.getLocalAddress();
        for(i=0,tcp = tcps;i<arr.length && tcp != null;i++,tcp = tcp._next){
            arr[i][0] = localIP;
            arr[i][1] = tcp._localPort;
            arr[i][2] = tcp._remoteIp;
            arr[i][3] = tcp._remotePort;
            arr[i][4] = tcp._state.ordinal();
        }
        return i;
    }
}
