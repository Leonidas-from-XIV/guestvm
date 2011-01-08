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
package com.oracle.labs.ajtrace.max.ve.net.tcp;

import com.oracle.labs.ajtrace.*;

/**
 * Pointcuts to the TCP stack.
 * 
 * @author Mick Jordan
 *
 */
public aspect AJTraceMaxVENetTCP extends AJTraceArgs {
	pointcut tcpignore(): execution(* com.sun.max.ve.net.tcp.TCP.*print(..)) || execution(* com.sun.max.ve.net.tcp.TCP.*String(..)) || 
	                               execution(* com.sun.max.ve.net.tcp.TCP.*.*String(..)) || execution(* com.sun.max.ve.net.tcp.TCP.toUnsigned(..)) ||
	                               execution(* com.sun.max.ve.net.tcp.TCP.*SWITCH_TABLE*(..)) || execution(* com.sun.max.ve.net.tcp.TCP.access$*(..)) ||
	                               execution(* com.sun.max.ve.net.tcp.TCPConnectionKey.*(..)) || execution(com.sun.max.ve.net.tcp.TCPConnectionKey.new(..));
	pointcut tcp(): execution(* com.sun.max.ve.net.tcp..*(..)) || execution(com.sun.max.ve.net.tcp..new(..));
	
    public pointcut execAll() : tcp() && !tcpignore();
	
}

