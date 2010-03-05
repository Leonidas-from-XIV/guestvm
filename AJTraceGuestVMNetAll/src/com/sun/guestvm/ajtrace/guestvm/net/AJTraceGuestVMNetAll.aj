package com.sun.guestvm.ajtrace.guestvm.net;

import com.sun.guestvm.ajtrace.AJTrace;

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

public aspect AJTraceGuestVMNetAll extends AJTrace {

	pointcut dns() : execution(* com.sun.guestvm.net.dns.*.*(..)) || execution(com.sun.guestvm.net.dns.*.new(..));
	pointcut ip() :   execution(* com.sun.guestvm.net.ip.*.*(..)) || execution(com.sun.guestvm.net.ip.*.new(..));
	pointcut udp(): execution(* com.sun.guestvm.net.udp.*.*(..)) || execution(com.sun.guestvm.net.udp.*.new(..));
	pointcut arp(): execution(* com.sun.guestvm.net.arp.*.*(..)) || execution(com.sun.guestvm.net.arp.*.new(..));
	pointcut tcp(): execution(* com.sun.guestvm.net.tcp.*.*(..)) || execution(com.sun.guestvm.net.tcp.*.new(..));
	pointcut tcpn(): execution(* com.sun.guestvm.net.tcp.TCP.*.*(..)) || execution(com.sun.guestvm.net.tcp.TCP.*.new(..));
	pointcut dev(): execution(* com.sun.guestvm.net.guk.GUKNetDevice.transmit(..));
	pointcut net(): (execution(* com.sun.guestvm.net.*.*(..)) && ! execution(* com.sun.guestvm.net.Packet.inline*(..))) || execution(com.sun.guestvm.net.*.new(..)) ;
	
	public pointcut execAll() : dns() || ip() || udp() || arp() || tcp() || tcpn() || net() || dev();
	
}
