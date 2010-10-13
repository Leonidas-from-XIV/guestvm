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
 * An abstract Aspect that defines before/after advice for tracing method
 * entry/exit with timings. The before/after advice provides some support for a
 * subclass (which used to be integrated with this class) {@link AJTraceArgs} to
 * extend the basic tracing with arguments/results, .
 * 
 * @author Mick Jordan
 * 
 */

//@Aspect
public abstract aspect AJTrace {
	public static final String WALLTIME_PROPERTY = "guestvm.ajtrace.timing";
	public static final String CPUTIME_PROPERTY = "guestvm.ajtrace.cputime";
	public static final String SYSTIME_PROPERTY = "guestvm.ajtrace.systime";
	public static final String OFF_PROPERTY = "guestvm.ajtrace.off";
	public static final String FLAG_ERRORS_PROPERTY = "guestvm.ajtrace.flagerrors";

	private static boolean recordWallTime = false;
	private static boolean recordCputime = false;
	private static boolean recordSystime = false;
	protected static volatile boolean off;
	protected static boolean flagErrors;

	private static boolean init;
	private static boolean inInit;
	private static ThreadMXBean threadMXBean;
	protected static StateThreadLocal state;

	private static Map<Long, Long> threadMap = new HashMap<Long, Long>();
	private static Map<String, Integer> methodNameMap = new HashMap<String, Integer>();
	private static int nextMethodId = 1;

	protected static AJTraceLog traceLog;

	protected synchronized void initialize() {
		if (!init) {
			// Check for recursive initialization, which is quite easy to achieve, unfortunately!
			if (inInit) {
				System.err.println("initialization cycle in AJTrace");
				System.exit(1);
			}
			inInit = true;
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
			inInit = false;
			init = true;
		}
	}

	protected void initProperties() {
		recordWallTime = System.getProperty(WALLTIME_PROPERTY) != null;
		recordCputime = System.getProperty(CPUTIME_PROPERTY) != null;
		recordSystime = System.getProperty(SYSTIME_PROPERTY) != null;
		off = System.getProperty(OFF_PROPERTY) != null;
	}

	static class FiniHook extends Thread {
		public void run() {
			off = true;
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

	// @Before("execAll()")

	before() : execAll() {
		doBefore(thisJoinPoint);
	}
	
	protected String[] getArgs(JoinPoint jp) {
		return null;
	}
	
	protected boolean preBeforeCheck() {
		if (!init) {
			initialize();
		}
		return !off;
	}
	
	public void doBefore(JoinPoint jp) {
		if (preBeforeCheck()) {
			final State s = state.get();
			long userTime = 0;
			long sysTime = 0;
			if (recordCputime) {
				s.getTimes();
				userTime = s.userTime;
				sysTime = s.sysTime;
			}
			final long threadId = getCurrentThreadName();
			final int methodId = getMethodName(jp);
			String[] args = getArgs(jp);
			traceLog.enter(s.depth++, recordWallTime ? time() : 0, userTime,
					sysTime, threadId, methodId, args);
		}
	}

    after() : execAll() {
    	doAfter(thisJoinPoint, null);
    }
    
    protected boolean preAfterCheck() {
    	return !off;
    }

	protected void doAfter(JoinPoint jp, String result) {
		if (preAfterCheck()) {
			final State s = state.get();
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
			traceLog.exit(s.depth, recordWallTime ? time() : 0, userTime, sysTime,
					threadId, methodId, result);
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
		String fullName = null;
		try {
			Signature sig = jp.getSignature();
			fullName = sig.getDeclaringType().getName() + "." + sig.getName();
		} catch (Exception ex) {
			if (flagErrors) {
				System.err.println("AJTrace: exception get method name");
				ex.printStackTrace();
			}
			fullName = "ERROR_GETTING_NAME";
		}
		Integer shortForm = methodNameMap.get(fullName);
		if (shortForm == null) {
			shortForm = new Integer(nextMethodId++);
			methodNameMap.put(fullName, shortForm);
			traceLog.defineMethod(shortForm, fullName);
		}
		return shortForm;
	}


}


