/*
 * Copyright (c) 2010, 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.labs.ajtrace.max.ve.net;

import com.oracle.labs.ajtrace.AJTrace;

/**
 * Pointcuts to trace almost everything in the network stack.
 * 
 * Note that the GUKNetDevice.copyPacket method must NOT be traced as it is not allowed to make any external calls.
 * Note also that the GUKNetDevice.DeviceHandler class cannot be traced as it uses hidden access methods which are
 * themselves traced (don't know how to prevent this) and some of these execute with interrupts disabled, which precludes any
 * entry to the scheduler (which will happen if the trace system is called).
 * 
 * @author Mick Jordan
 *
 */

public aspect AJTraceMaxVENet extends AJTrace {

	public pointcut dns() : execution(* com.sun.max.ve.net.dns.*.*(..)) || execution(com.sun.max.ve.net.dns.*.new(..));
	public pointcut ip() :   execution(* com.sun.max.ve.net.ip.*.*(..)) || execution(com.sun.max.ve.net.ip.*.new(..));
	public pointcut udp(): execution(* com.sun.max.ve.net.udp.*.*(..)) || execution(com.sun.max.ve.net.udp.*.new(..));
	public pointcut arp(): execution(* com.sun.max.ve.net.arp.*.*(..)) || execution(com.sun.max.ve.net.arp.*.new(..));
	public pointcut tcp(): execution(* com.sun.max.ve.net.tcp.*.*(..)) || execution(com.sun.max.ve.net.tcp.*.new(..));
	public pointcut tcpn(): execution(* com.sun.max.ve.net.tcp.TCP.*.*(..)) || execution(com.sun.max.ve.net.tcp.TCP.*.new(..));
	public pointcut dev(): execution(* com.sun.max.ve.net.guk.GUKNetDevice.transmit(..));
	public pointcut net(): (execution(* com.sun.max.ve.net.*.*(..)) && ! execution(* com.sun.max.ve.net.Packet.inline*(..))) || execution(com.sun.max.ve.net.*.new(..)) ;
	
	public pointcut execAll() : dns() || ip() || udp() || arp() || tcp() || tcpn() || net() || dev();
	
}
