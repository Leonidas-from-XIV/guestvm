/*
 * Copyright (c) 2010, 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.labs.ajtrace;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

public abstract aspect AJTraceArgs extends AJTrace {
	public static final String TRACEARGS_PROPERTY = "ajtrace.args";
	public static final String PLAINARGS_PROPERTY = "ajtrace.plainargs";

	private static boolean traceArgs;
    static boolean plainArgs;
	
	@Override
	protected void initProperties() {
		super.initProperties();
		traceArgs = System.getProperty(TRACEARGS_PROPERTY) != null;
	}
	
	@Override
	protected Object getTarget(JoinPoint jp) {
		return traceArgs ? jp.getTarget() : null;
	}
    
	@Override
	protected Object[] getArgs(JoinPoint jp) {
		return traceArgs ? jp.getArgs() : null;
	}

	@Override
	protected boolean preBeforeCheck() {
		if (!super.preBeforeCheck()) {
			return false;
		}
    	final State s = state.get();
    	return !s.inAdvice;
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
	protected void doAfter(JoinPoint jp) {
		/* This is the override of the plain "after" advice, which ideally would
		 * never happen, but constructors that return results (but may throw exceptions).
		 * I.e., the plain "after" advice is necessary to catch a normal constructor return.
		 * As constructors don't return results, the plain "after" pointcut from AJTrace
		 * is invoked, which is how we get here. [Regular methods match the
		 * "after returning" or "after throwing" pointcut and take that advice (but then
		 * also take this advice, so we have to ignore that].
		 * 
		 * EXCEPT, if the constructor throws an exception, we do match the
		 * "after throwing" pointcut and then we have to avoid repeating the advice here.
		 * It's worse because there doesn't seem to be a way to control the ordering
		*/
		if (jp.getKind() == JoinPoint.CONSTRUCTOR_EXECUTION) {
			final State s = state.get();
			if (s.constructorException) {
				s.constructorException = false;
			} else {
			    super.doAfter(jp);
			}
		}
	}
	
	@Override
	protected void doAfterWithReturn(JoinPoint jp, Object resultOrThrowable, boolean normalReturn) {
		/*
		 * If normalReturn == true, then this must be a method return, with a result, unless the method returns void.
		 * If normalReturn == false, then this is either a method throwing an exception or
		 * a constructor throwing an exception (in resultOrThrowable).
		 */
		if (preAfterCheck()) {
			final State s = state.get();
			boolean isvr = false;
			if (normalReturn) {
				isvr = isVoidReturn(jp);
			} else {
				if (jp.getKind() == JoinPoint.CONSTRUCTOR_EXECUTION) {
					s.constructorException = true;
				}
			}
			super.doAfterCheckVoid(jp, resultOrThrowable, isvr);
		}
	}
	
	private static boolean isVoidReturn(JoinPoint jp) {
		MethodSignature ms = (MethodSignature) jp.getSignature();
		return ms.getReturnType() == void.class;
	}

}
