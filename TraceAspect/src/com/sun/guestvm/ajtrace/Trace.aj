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
public abstract aspect Trace {
	public static final String TIME_PROPERTY = "guestvm.ajtrace.timing";
	public static final String CPUTIME_PROPERTY = "guestvm.ajtrace.cputime";
	public static final String SYSTIME_PROPERTY = "guestvm.ajtrace.systime";

	private static boolean recordTime = false;
	private static boolean recordCputime = false;
	private static boolean recordSystime = false;
	
	private static boolean init;
	private static ThreadMXBean threadMXBean;
	private static StateThreadLocal state;

	private static Map<Thread, Integer> threadMap = new HashMap<Thread, Integer>();
	private static int nextThreadId = 1;
	private static Map<String, Integer> methodNameMap = new HashMap<String, Integer>();
	private static int nextMethodId = 1;
	
	protected static TraceLog traceLog;

	public static synchronized void initialize() {
		if (!init) {
			initProperties();
			threadMXBean = ManagementFactory.getThreadMXBean();
			state = new StateThreadLocal();
			traceLog = TraceLogFactory.create();
			traceLog.init(time());
			Runtime.getRuntime().addShutdownHook(new FiniHook());
			init = true;
		}
	}
	
	private static void initProperties() {
		recordTime = System.getProperty(TIME_PROPERTY) != null;
		recordCputime = System.getProperty(CPUTIME_PROPERTY) != null;
		recordSystime = System.getProperty(SYSTIME_PROPERTY) != null;
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

	protected Trace() {
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
			final int threadId = getCurrentThreadName();
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

    after() returning(Object result) : execAll() {
		doAfter(thisJoinPoint, result, true);
    }

    after() throwing(Throwable t) : execAll() {
		doAfter(thisJoinPoint, t, false);
    }

	protected void doAfter(JoinPoint jp, Object result, boolean normalReturn) {
		final State s = state.get();
		if (!s.inAdvice) {
			s.depth--;
			final int threadId = getCurrentThreadName();
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

	private synchronized int getCurrentThreadName() {
		Thread ct = Thread.currentThread();
		Integer shortForm = threadMap.get(ct);
		if (shortForm == null) {
			shortForm = new Integer(nextThreadId++);
			traceLog.defineThread(shortForm, ct.getName());
			threadMap.put(ct, shortForm);
		}
		return shortForm;
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


