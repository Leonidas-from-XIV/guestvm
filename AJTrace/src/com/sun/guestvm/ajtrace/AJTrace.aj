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
package com.sun.guestvm.ajtrace;

import java.lang.management.*;
import java.util.*;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;

/**
 * An abstract Aspect that defines before /after advice for tracing method entry/exit with timings.
 * 
 * @author Mick Jordan
 *
 */

//@Aspect
public abstract aspect AJTrace {
	public static final String TIME_PROPERTY = "guestvm.ajtrace.timing";
	public static final String CPUTIME_PROPERTY = "guestvm.ajtrace.cputime";
	public static final String SYSTIME_PROPERTY = "guestvm.ajtrace.systime";
	public static final String OFF_PROPERTY = "guestvm.ajtrace.off";

	private static boolean recordTime = false;
	private static boolean recordCputime = false;
	private static boolean recordSystime = false;
	private static boolean off;
	
	private static boolean init;
	private static ThreadMXBean threadMXBean;
	private static StateThreadLocal state;

	private static Map<Long, Long> threadMap = new HashMap<Long, Long>();
	private static Map<String, Integer> methodNameMap = new HashMap<String, Integer>();
	private static int nextMethodId = 1;
	
	protected static AJTraceLog traceLog;

	public static synchronized void initialize() {
		if (!init) {
			initProperties();
			if (!off) {
				threadMap.clear();
				methodNameMap.clear();			
				threadMXBean = ManagementFactory.getThreadMXBean();
				state = new StateThreadLocal();
				traceLog = AJTraceLogFactory.create();
				traceLog.init(time());
				Runtime.getRuntime().addShutdownHook(new FiniHook());
			}
			init = true;
		}
	}
	
	private static void initProperties() {
		recordTime = System.getProperty(TIME_PROPERTY) != null;
		recordCputime = System.getProperty(CPUTIME_PROPERTY) != null;
		recordSystime = System.getProperty(SYSTIME_PROPERTY) != null;
		off = System.getProperty(OFF_PROPERTY) != null;
	}
	
	static class FiniHook extends Thread {
		public void run() {
			traceLog.fini(time());
		}
	}

	static class State {
		private long userTime;
		private long sysTime;
		int depth = 1;
		boolean inAdvice;

		void getTimes() {
			userTime = threadMXBean.getCurrentThreadUserTime();
			if (recordSystime) {
				final long totalTime = threadMXBean.getCurrentThreadCpuTime();
				// this is inherently inaccurate as it takes some user time to
				// make the second call, but it will be a positive value!
				sysTime = totalTime - userTime;
			}
		}
	}

	static class StateThreadLocal extends ThreadLocal<State> {
		public State initialValue() {
			return new State();
		}
	}

	protected AJTrace() {
	}

	//@Pointcut("")
	//public abstract void execAll();
	public abstract pointcut execAll();

	//@Before("execAll()")

	  before() : execAll() {
		    doBefore(thisJoinPoint);
		  }

	public void doBefore(JoinPoint jp) {
		if (!init) {
			initialize();
		}
		if (off) return;
		final State s = state.get();
		if (!s.inAdvice) {
			s.inAdvice = true;
			long userTime = 0;
			long sysTime = 0;
			if (recordCputime) {
				s.getTimes();
				userTime = s.userTime;
				sysTime = s.sysTime;
			}
			final long threadId = getCurrentThreadName();
			final int methodId = getMethodName(jp);
			traceLog.enter(s.depth++, recordTime ? time() : 0, userTime, sysTime,
					threadId, methodId);
			s.inAdvice = false;
		}
	}

	// N.B this does not catch returning constructors
	/*
	@AfterReturning(pointcut="execAll()", returning="result")
	public void doAfterNormalReturn(JoinPoint jp, Object result) {
		doAfter(jp, result, true);
	}

	@AfterThrowing(pointcut="execAll()", throwing="result")
	public void doAfterExceptionReturn(JoinPoint jp, Object result) {
		doAfter(jp, result, false);
	}
	*/

    after() returning() : execAll() {
		doAfter(thisJoinPoint, true);
    }

    after() throwing(Throwable t) : execAll() {
		doAfter(thisJoinPoint, false);
    }

	protected void doAfter(JoinPoint jp,  boolean normalReturn) {
		if (off) return;
		final State s = state.get();
		if (!s.inAdvice) {
			s.depth--;
			final long threadId = getCurrentThreadName();
			final int methodId = getMethodName(jp);
			long userTime = 0;
			long sysTime = 0;
			if (recordCputime) {
				s.getTimes();
				userTime = s.userTime;
				sysTime = s.sysTime;
			}
			traceLog.exit(s.depth, recordTime ? time() : 0, userTime, sysTime,
					threadId, methodId);
		}
	}

	private static long time() {
        return System.nanoTime();
	}

	private synchronized long getCurrentThreadName() {
		final Thread ct = Thread.currentThread();
		final long id = ct.getId();
		if (threadMap.get(id) ==null) {
			traceLog.defineThread(id, ct.getName());
			threadMap.put(id, id);
		}
		return id;
	}

	private int getMethodName(JoinPoint jp) {
		Signature sig = jp.getSignature();
		String fullName = sig.getDeclaringType().getName() + "." + sig.getName();
		Integer shortForm = methodNameMap.get(fullName);
		if (shortForm == null) {
			shortForm = new Integer(nextMethodId++);
			methodNameMap.put(fullName, shortForm);
			traceLog.defineMethod(shortForm, fullName);
		}
		return shortForm;
	}


}


