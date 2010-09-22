package com.sun.guestvm.ajtrace;

import java.util.*;
import org.aspectj.lang.JoinPoint;

public abstract aspect AJTraceArgs extends AJTrace {
	public static final String TRACEARGS_PROPERTY = "guestvm.ajtrace.args";
	public static final String PLAINARGS_PROPERTY = "guestvm.ajtrace.plainargs";

	private static boolean traceArgs;
	private static boolean plainArgs;
	private static Map<String, Integer>  paramMap = new HashMap<String, Integer> ();
	private static int nextParamId = 1;
	
	@Override
	protected void initProperties() {
		super.initProperties();
		traceArgs = System.getProperty(TRACEARGS_PROPERTY) != null;
		plainArgs = System.getProperty(PLAINARGS_PROPERTY) != null;		
		flagErrors = System.getProperty(FLAG_ERRORS_PROPERTY) != null;
	}
	
	@Override
	protected String[] getArgs(JoinPoint jp) {
		String[] result = null;
		if (traceArgs) {
	    	final State s = state.get();
	    	s.inAdvice = true;
	    	result = getParameters(jp);
	    	s.inAdvice = false;
		}
		return result;
	}

	@Override
	protected boolean preBeforeCheck() {
		if (!super.preBeforeCheck()) {
			return false;
		}
    	final State s = state.get();
    	return !s.inAdvice;
	}
	
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
    
	@Override
	protected boolean preAfterCheck() {
		if (!super.preAfterCheck()) {
			return false;
		}
    	final State s = state.get();
    	return !s.inAdvice;
	}
	
	@Override
	protected void doAfter(JoinPoint jp, String result) {
		// we don't want to do anything unless it is a constructor exit
		// as we will get the "after returning" or "after throwable" call.
		if (jp.getKind() == JoinPoint.CONSTRUCTOR_EXECUTION) {
			super.doAfter(jp, null);
		}
	}
	
	protected void doAfter(JoinPoint jp,  Object resultOrThrowable, boolean normalReturn) {
		if (preAfterCheck()) {
			final State s = state.get();
			String resultString = null;
			if (normalReturn) {
				s.inAdvice = true;
				resultString = getParameter(resultOrThrowable);
				s.inAdvice = false;
			}
			super.doAfter(jp, resultString);
		}
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
			paramMap.put(p, shortForm);
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
