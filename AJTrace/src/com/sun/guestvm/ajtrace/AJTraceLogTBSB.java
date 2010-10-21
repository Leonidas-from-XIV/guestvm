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
