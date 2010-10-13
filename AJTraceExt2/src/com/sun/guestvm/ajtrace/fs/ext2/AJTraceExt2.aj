package com.sun.guestvm.ajtrace.fs.ext2;

import com.sun.guestvm.ajtrace.AJTrace;

/**
 * Pointcuts to trace almost everything in the ext2 file system stack.
 * Obviously the AJTrace system itself must operate independent from
 * ext2 for this to work (avoid bootstrap circularities).
 * 
 * @author Mick Jordan
 *
 */

public aspect AJTraceExt2 extends AJTrace {

	pointcut dev(): execution(* com.sun.guestvm.blk.guk.GUKBlkDevice.read(..)) || execution(* com.sun.guestvm.blk.guk.GUKBlkDevice.write(..));
	pointcut fs_ext2(): execution(* com.sun.guestvm.fs.ext2.*.*(..));
	
	pointcut jnode(): execution(* org.jnode..*.*(..));
	
	public pointcut execAll() : dev() || fs_ext2() || jnode();

}
