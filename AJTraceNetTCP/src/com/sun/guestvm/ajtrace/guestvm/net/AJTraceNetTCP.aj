package com.sun.guestvm.ajtrace.guestvm.net;

import com.sun.guestvm.ajtrace.AJTrace;

/**
 * Pointcuts to the TCP stack.
 * 
 * @author Mick Jordan
 *
 */
public aspect AJTraceNetTCP extends AJTrace {
	pointcut tcp(): execution(* com.sun.guestvm.net.tcp..*(..)) || execution(com.sun.guestvm.net.tcp..new(..));
	
    public pointcut execAll() : tcp();
	
}

