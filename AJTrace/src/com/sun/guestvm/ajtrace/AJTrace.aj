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
	public static final String TRACEARGS_PROPERTY = "guestvm.ajtrace.args";
	public static final String PLAINARGS_PROPERTY = "guestvm.ajtrace.plainargs";
	public static final String FLAG_ERRORS_PROPERTY = "guestvm.ajtrace.flagerrors";

	private static boolean recordTime = false;
	private static boolean recordCputime = false;
	private static boolean recordSystime = false;
	private static boolean off;
	private static boolean traceArgs;
	private static boolean plainArgs;
	private static boolean flagErrors;

	private static boolean init;
	private static ThreadMXBean threadMXBean;
	private static StateThreadLocal state;

	private static Map<Long, Long> threadMap = new HashMap<Long, Long>();
	private static Map<String, Integer> methodNameMap = new HashMap<String, Integer>();
	private static Map<String, Integer>  paramMap = new HashMap<String, Integer> ();
	private static int nextParamId = 1;
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
		traceArgs = System.getProperty(TRACEARGS_PROPERTY) != null;
		plainArgs = System.getProperty(PLAINARGS_PROPERTY) != null;
		flagErrors = System.getProperty(FLAG_ERRORS_PROPERTY) != null;
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
			String[] args = null;
			if (traceArgs) {
				args = getParameters(jp);
			}
			traceLog.enter(s.depth++, recordTime ? time() : 0, userTime, sysTime,
					threadId, methodId, args);
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

	protected void doAfter(JoinPoint jp,  Object resultOrThrowable, boolean normalReturn) {
		if (off) return;
		final State s = state.get();
		if (!s.inAdvice) {
			s.inAdvice = true;
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
			String result = traceArgs && normalReturn ?  getParameter(resultOrThrowable): null;
			traceLog.exit(s.depth, recordTime ? time() : 0, userTime, sysTime,
					threadId, methodId, result);
			s.inAdvice = false;
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

	private String[] getParameters(JoinPoint jp) {
		Object[] args = jp.getArgs();
		final String[] result = new String[args.length + 1];
		int i = 0;
		result[i++] = getParameter(jp.getTarget());
		for (Object arg: args) {
			result[i++] = getParameter(arg);
		}
		return result;
	}

	private String getParameter(Object value) {
		if (value == null) {
			return "null";
		} else if (value instanceof String) {
			return "\"" + replaceNL((String)value) + "\"";
		} else if (value instanceof Character) {
			return "'" + value + "'";
		} else if (value instanceof Number || value instanceof Boolean) {
			return value.toString();
		} else {
			if (plainArgs) {
				return objectToString(value);
			} else {
				try {
			        return replaceNL(customize(value));
				} catch (Exception ex) {
					if (flagErrors) {
					    System.err.println("AJTrace: exception tracing argument");
					    ex.printStackTrace();
					}
					return "???";
				}
			}
		}
	}

	private int getParamShortName(String p) {
		Integer shortForm = paramMap.get(p);
		if (shortForm == null) {
			shortForm = new Integer(nextParamId++);
			methodNameMap.put(p, shortForm);
			traceLog.defineParam(shortForm, p);
		}
		return shortForm;
	}

	protected String objectToString(Object obj) {
		// hashcode sometimes fails if object is still in the process of being
		// initialized yet being passed as an argument to a method (which we are tracing)
		if (obj == null) {
			return "null";
		}
		String hc = "?";
		try {
			hc = Integer.toHexString(obj.hashCode());
		} catch (Exception ex) {
			if (flagErrors) {
				System.err.println("AJTrace: exception calling hashCode");
				ex.printStackTrace();
			}
		}
		return obj.getClass().getName() + "@" + hc;
	}

	/**
	 * Replace newlines with \n
	 */
	protected String replaceNL(String  str) {
		String result = str;
		if (str != null) {
			int ix = str.indexOf('\n');
			if (ix >= 0) {
				int pix = 0;
				StringBuffer strb = new StringBuffer();
				while (ix >= 0) {
					strb.append(str.substring(pix, ix));
					strb.append('\\');
					strb.append('n');
					pix = ix + 1;
					ix = str.indexOf('\n', pix);
				}
				if (pix < str.length())
					strb.append(str.substring(pix));
				result = strb.toString();
			}
		}
		return result;
	}

	protected String customize(Object value) {
		return value.toString();
	}

}


