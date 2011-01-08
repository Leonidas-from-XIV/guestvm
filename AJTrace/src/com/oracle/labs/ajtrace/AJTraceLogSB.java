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
package com.oracle.labs.ajtrace;

import java.io.*;
import java.util.*;

/**
 * This implementation is inefficient in that it allocates a StringBuilder per trace and
 * writes it out per trace. So trace log is ordered by time, with thread traces interleaved and
 * no sync required (other than that implicit on the output stream).
 *
 * It defines protected methods that log the data to a StringBuilder for use by subclasses
 * that might, for example, buffer the output.
 *
 * @author Mick Jordan
*/

public class AJTraceLogSB extends AJTraceLog {

	public static final String NLARGS_PROPERTY = "ajtrace.nlargs";
	public static final String SHORTFORM_PROPERTY = "ajtrace.shortargs";
	
	private static boolean nlArgs;
	private static int shortFormLimit = 8;
	private static Map<String, Integer>  paramMap = new HashMap<String, Integer> ();
	private static int nextParamId = 1;
	protected PrintStream _ps;
	private long _startTime;

	@Override
	public void init(long startTime) {
		final String prop = System.getProperty(SHORTFORM_PROPERTY);
		if (prop != null) {
			shortFormLimit = Integer.parseInt(prop);
		}
		initPS();
		final StringBuilder sb = new StringBuilder();
		initLog(startTime, sb);
		_ps.print(sb);
	}

	protected void initPS() {
		final OutputStream logStream = AJTraceLogFile.create();
		if (logStream == null) {
			_ps = System.out;
		} else {
			_ps = new PrintStream(logStream);
		}
	}

	protected void initLog(long startTime, StringBuilder sb) {
		_startTime = startTime;
		sb.append("0 S S ");
		sb.append(startTime);
		sb.append('\n');
		checkFlush(sb);
	}

	@Override
	public void fini(long endTime) {
		_ps.flush();
		if (_ps != System.out) {
			_ps.close();
		}
	}

	/**
	 * Determines the size at which checkFlush will flush the StringBuilder.
	 * Subclasses that buffer should override this method.
	 * @return size at which to flush the buffer
	 */
	protected int flushSize() {
		return 0;
	}

	protected boolean checkFlush(StringBuilder sb) {
		// conservative check that will (usually) flush before StringBuilder increases capacity
		if (sb.length() >= flushSize() - 64) {
			_ps.print(sb);
			sb.setLength(0);
			return true;
		}
		return false;
	}

	@Override
	public void defineThread(long id, String fullName) {
		final StringBuilder sb = new StringBuilder();
		defineThreadLog(id, fullName, sb);
	}

	protected void defineThreadLog(long id, String fullName, StringBuilder sb) {
		sb.append("0 D T");
		sb.append(id);
		sb.append(' ');
		sb.append(fullName);
		sb.append('\n');
		checkFlush(sb);
	}

	@Override
	public void defineParam(int id, String fullName) {
		final StringBuilder sb = new StringBuilder();
		defineParamLog(id, fullName, sb);
	}

	protected void defineParamLog(int id, String fullName, StringBuilder sb) {
		sb.append("0 P A");
		sb.append(id);
		sb.append(' ');
		sb.append(fullName);
		sb.append('\n');
		checkFlush(sb);
	}

	@Override
	public void defineMethod(int id, String fullName) {
		final StringBuilder sb = new StringBuilder();
		defineMethodLog(id, fullName, sb);
	}

	protected void defineMethodLog(int id, String fullName, StringBuilder sb) {
		sb.append("0 M M");
		sb.append(id);
		sb.append(' ');
		sb.append(fullName);
		sb.append('\n');
		checkFlush(sb);
	}

	@Override
	public void enter(int depth, long tod, long user, long sys, long threadId, int methodId, Object target, boolean isCons, Object[] args) {
		final StringBuilder sb = new StringBuilder();
		enterLog(depth, tod, user, sys, threadId, methodId, target, false, args, sb);
	}

	protected void enterLog(int depth, long tod, long user, long sys,
			long threadId, int methodId, Object target, boolean isCons, Object[] args, StringBuilder sb) {
	    StringBuilder argsSb = null;
	    // we do this here in case argument processing generates any short form definitions
		if (args != null) {
			argsSb = getArgs(target, isCons, args);
		}
		sb.append(depth);
		sb.append(' ');
		sb.append('E');
		appendTime(tod, user, sys, sb);
		sb.append(" T");
		sb.append(threadId);
		sb.append(" M");
		sb.append(methodId);
		if (argsSb != null) {
			sb.append(argsSb);
		}
		sb.append('\n');
		checkFlush(sb);
	}

	@Override
	public void exit(int depth, long tod, long user, long sys, long threadId, int methodId, Object result) {
		final StringBuilder sb = new StringBuilder();
		exitLog(depth, tod, user, sys, threadId, methodId, result, sb);
	}

	@Override
	public void exit(int depth, long tod, long user, long sys, long threadId, int methodId) {
		final StringBuilder sb = new StringBuilder();
		exitLog(depth, tod, user, sys, threadId, methodId, sb);
	}

	protected void exitLog(int depth, long tod, long user, long sys, long threadId, int methodId, Object result, StringBuilder sb) {
		doExitLogPrefix(false, depth, tod, user, sys, threadId, methodId, sb);
		if (result != null) {
			sb.append('(');
			sb.append(result);
			sb.append(')');
		}
		sb.append('\n');
		checkFlush(sb);
	}
	
	protected void exitLog(int depth, long tod, long user, long sys, long threadId, int methodId, StringBuilder sb) {
		doExitLogPrefix(true, depth, tod, user, sys, threadId, methodId, sb);
		sb.append('\n');
		checkFlush(sb);		
	}
	
	private void doExitLogPrefix(boolean isVoidReturn, int depth, long tod, long user, long sys, long threadId, int methodId, StringBuilder sb) {
		sb.append(depth);
		sb.append(' ');
		sb.append(isVoidReturn ? 'V' : 'R');
		appendTime(tod, user, sys, sb);
		sb.append(" T");
		sb.append(threadId);
		sb.append(" M");
		sb.append(methodId);
	}
	
	@Override
    public void call(int depth, long tod, long threadId, long methodId, Object target, Object[] args) {
		final StringBuilder sb = new StringBuilder();
        callLog(depth, tod, threadId, methodId, target, args, sb);
	}
	
	protected void callLog(int depth, long tod, long threadId, long methodId, Object target, Object[] args, StringBuilder sb) {
	    StringBuilder argsSb = null;
	    // we do this here in case argument processing generates any short form definitions
		if (args != null) {
			argsSb = getArgs(target, false, args);
		}
		sb.append(depth);
		sb.append(' ');
		sb.append('C');
		appendTime(tod, 0, 0, sb);
		sb.append(" T");
		sb.append(threadId);
		sb.append(" M");
		sb.append(methodId);
		if (argsSb != null) {
			sb.append(argsSb);
		}
	    sb.append('\n');
	    checkFlush(sb);
	}

	private void appendTime(long tod, long user, long sys,
			StringBuilder sb) {
		if (tod != 0) {
			sb.append(tod - _startTime);
			if (user != 0) {
				sb.append(',');
				sb.append(user);
				sb.append(',');
				sb.append(sys);
			}
		}
	}
	
	private StringBuilder getArgs(Object target, boolean isCons, Object[] args) {
		final StringBuilder sb = new StringBuilder();
		sb.append('(');
		sb.append(getParameter(target, isCons));
		if (args.length > 0) {
		    sb.append(',');
		}
		for (int i = 0; i < args.length; i++) {
			sb.append(getParameter(args[i], false));
			if (i != args.length-1) {
				sb.append(',');
			}
		}
		sb.append(')');		
		return sb;
	}
	
	private String getParameter(Object value, boolean isCons) {
		String result = null;
		if (value == null) {
			result = "null";
		} else if (value instanceof String) {
			result = "\"" + replaceNL((String) value) + "\"";
		} else if (value instanceof Character) {
			result = "'" + value + "'";
		} else if (value instanceof Number || value instanceof Boolean) {
			result = value.toString();
		} else {
			if (isCons || AJTraceArgs.plainArgs) {
				result = objectToString(value);
			} else {
				try {
			        result = "\"" + replaceNL(customize(value)) + "\"";
				} catch (Exception ex) {
					if (AJTrace.flagErrors) {
					    System.err.println("AJTrace: exception tracing argument");
					    ex.printStackTrace();
					}
					result = "???";
				}
			}
		}
		if (result.length() >= shortFormLimit) {
			Integer shortForm = paramMap.get(result);
			if (shortForm == null) {
				shortForm = new Integer(nextParamId++);
				paramMap.put(result, shortForm);
				defineParam(shortForm, result);
			}
			result = "A" + shortForm.toString();
		}
		return result;
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
			if (AJTrace.flagErrors) {
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
		if (!nlArgs) {
			return str;
		}
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
