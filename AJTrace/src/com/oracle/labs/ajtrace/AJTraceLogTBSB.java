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

import java.util.*;
/**
 * This version has a per-thread string builder, so no sync needed, but trace logs are
 * output per thread.
 *
 * @author Mick Jordan
 *
 */
public class AJTraceLogTBSB extends AJTraceLogBSB {

	private static StringBuilderThreadLocal _sbtl;
	private static ArrayList<StringBuilder> _sbList = new ArrayList<StringBuilder>(64);
	private static int _ptLogSize;

	static class StringBuilderThreadLocal extends ThreadLocal<StringBuilder> {
		public StringBuilder initialValue() {
			final StringBuilder sb = new StringBuilder(_ptLogSize);
			_sbList.add(sb);
			return sb;
		}
	}

	@Override
	public void init(long startTime) {
		initPS();
		initLogSize();
		_ptLogSize = _logSize / 64;
		_sbtl = new StringBuilderThreadLocal();
		final StringBuilder sb = _sbtl.get();
		initLog(startTime, sb);
	}

	@Override
	public void fini(long endTime) {
		for (StringBuilder sb : _sbList) {
			_ps.print(sb);
		}
		super.fini(endTime);
	}

	@Override
	public void defineParam(int id, String fullName) {
		defineParamLog(id, fullName, _sbtl.get());
	}

	@Override
	public void defineMethod(int id, String fullName) {
		defineMethodLog(id, fullName, _sbtl.get());
	}

	@Override
	public void defineThread(long id, String fullName) {
		defineThreadLog(id, fullName, _sbtl.get());
	}

	@Override
	public void enter(int depth, long tod, long user, long sys, long threadId, int methodId, Object target, boolean isCons, Object[] args) {
		enterLog(depth, tod, user, sys, threadId, methodId, target, isCons, args, _sbtl.get());

	}

	@Override
	public void exit(int depth, long tod, long user, long sys, long threadId, int methodId, Object result) {
		exitLog(depth, tod, user, sys, threadId, methodId, result, _sbtl.get());
	}

	@Override
	public void exit(int depth, long tod, long user, long sys, long threadId, int methodId) {
		exitLog(depth, tod, user, sys, threadId, methodId, _sbtl.get());
	}

	public void call(int depth, long tod, long threadId, long methodId, Object target, Object[] args) {
		callLog(depth, tod, threadId, methodId, target, args, _sbtl.get());
	}

}
