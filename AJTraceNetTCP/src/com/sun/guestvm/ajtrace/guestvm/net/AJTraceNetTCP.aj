package com.sun.guestvm.ajtrace.guestvm.net;

import com.sun.guestvm.ajtrace.*;

/**
 * Pointcuts to the TCP stack.
 * 
 * @author Mick Jordan
 *
 */
public aspect AJTraceNetTCP extends AJTraceArgs {
	pointcut tcpignore(): execution(* com.sun.guestvm.net.tcp.TCP.*print(..)) || execution(* com.sun.guestvm.net.tcp.TCP.*String(..)) || 
	                               execution(* com.sun.guestvm.net.tcp.TCP.*.*String(..)) || execution(* com.sun.guestvm.net.tcp.TCP.toUnsigned(..)) ||
	                               execution(* com.sun.guestvm.net.tcp.TCP.*SWITCH_TABLE*(..)) || execution(* com.sun.guestvm.net.tcp.TCP.access$*(..)) ||
	                               execution(* com.sun.guestvm.net.tcp.TCPConnectionKey.*(..)) || execution(com.sun.guestvm.net.tcp.TCPConnectionKey.new(..));
	pointcut tcp(): execution(* com.sun.guestvm.net.tcp..*(..)) || execution(com.sun.guestvm.net.tcp..new(..));
	
    public pointcut execAll() : tcp() && !tcpignore();
    //public pointcut callAll() : call(void  com.sun.guestvm.net.tcp.TCP.trace);
	
}

