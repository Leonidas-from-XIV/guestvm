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
package test.java.net.cs;

/**
 * Test programs for communication using UDP or TCP.
 *
 * This is a client-server system where client threads
 * send data to server threads, with optional acknowledgement.
 * The number of threads is variable as is the protocol used, i.e., UDP or TCP.
 *
 * The ServerThread class denotes a server which will be paired with
 * a ClientThread. ServerThread.PORT defines the port for the first thread,
 * with subsequent threads using ServerThread.PORT +1, etc.
 *
 * The ServerMain class is the main program entry point for a server. It
 * accepts the following arguments (optionally preceded by a '-'):
 *
 * bs n        set data block size to n (default 1000)
 * onerun    terminate after receiving one data block
 * check     validate the sent  data (default no check)
 * sync        validate (check) the data immediately on receipt (default false)
 * noack      do not ack the data
 * th n          create n server threads (default 1)
 * buffers n   create n buffers for received data
 * verbose   verbose output on what is happening
 * type t       protocol (UDP or TCP, default UDP)
 *
 * A server thread, which is identified with the thread name Sn, where n is the server id (0, 1, ...)
 * buffers received data in a set of buffers (default set size 100) each of size given by the bs option.
 * If sync && check the received data is validated immediately on receipt, before any ACK
 * packet is sent back to the client.  If check && !sync the data is validated after the ACK is sent.
 * To simulate an application, a consumer thread is registered with the server thread. The consumer
 * repeatedly calls the getData method which copies the next unread buffer into the buffer provided
 * by the consumer. If no buffers are available the consumer blocks. If the consumer does not process
 * the data fast enough the server thread will block until a buffer is available.
 * If onerun is set the thread terminates when the client stops sending, otherwise it loops waiting for
 * a new connection. N.B. in UDP mode the client sends a zero length packet to indicate the end of the session.
 *
 * The Client Main class is the main program entry point for a client.
 * It accepts essentially the same arguments as ServerMain, specifically
 * bs, noack, th, verbose, type and adds:
 *
 * delay d    wait d milliseconds between data transfers (default 0)
 *
 * @author Mick Jordan
 *
 *
*/
