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

/**
 * This version buffers the trace logs in a single pre-allocated buffer, and hence must synchronize
 * between threads, It flushes when it reaches the log buffer size and also on fini.
 *
 * @author Mick Jordan
 *
 */

public class AJTraceLogBSB extends AJTraceLogSB {

	private StringBuilder _sb;
	protected int _logSize;
	private static final String LOGSIZE_PROPERTY = "ajtrace.logbuffersize";
	private static final int DEFAULT_LOGSIZE = 1024 * 1024;

	@Override
	public void init(long startTime) {
		initPS();
		initLogSize();
		_sb = new StringBuilder(_logSize);
		initLog(startTime, _sb);
	}

	protected int initLogSize() {
		final String s = System.getProperty(LOGSIZE_PROPERTY);
		_logSize = DEFAULT_LOGSIZE;
		if (s != null) {
			_logSize = Integer.parseInt(s);
		}
		return _logSize;
	}

	@Override
	public void fini(long endTime) {
		if (_sb != null) {
			_ps.print(_sb);
		}
		super.fini(endTime);
	}

	protected int flushSize() {
		return _logSize;
	}

	@Override
	public synchronized void defineMethod(int id, String fullName) {
		defineMethodLog(id, fullName, _sb);
	}

	@Override
	public synchronized void defineParam(int id, String fullName) {
		defineParamLog(id, fullName, _sb);
	}

	@Override
	public synchronized void defineThread(long id, String fullName) {
		defineThreadLog(id, fullName, _sb);
	}

	@Override
	public synchronized void enter(int depth, long tod, long user, long sys, long threadId, int methodId, Object target, boolean isCons, Object[] args) {
		enterLog(depth, tod, user, sys, threadId, methodId, target, isCons, args, _sb);

	}

	@Override
	public synchronized void exit(int depth, long tod, long user, long sys, long threadId, int methodId, Object result) {
		exitLog(depth, tod, user, sys, threadId, methodId, result, _sb);
	}

	@Override
	public synchronized void exit(int depth, long tod, long user, long sys, long threadId, int methodId) {
		exitLog(depth, tod, user, sys, threadId, methodId, _sb);
	}

	@Override
	public void call(int depth, long tod, long threadId, long methodId, Object target, Object[] args) {
		callLog(depth, tod, threadId, methodId, target, args, _sb);
	}
}
