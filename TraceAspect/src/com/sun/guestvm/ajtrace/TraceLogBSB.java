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

/**
 * This version buffers the trace logs in a single pre-allocated buffer, and hence must synchronize
 * between threads, and flushes it on fini.
 * 
 * @author Mick Jordan
 *
 */

public class TraceLogBSB extends TraceLogSB {

	private StringBuilder _sb;
	protected int _logSize;
	private static final String LOGSIZE_PROPERTY = "guestvm.ajtrace.logbuffersize";
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

	@Override
	public synchronized void defineMethod(int id, String fullName) {
		defineMethodLog(id, fullName, _sb);
	}

	@Override
	public synchronized void defineThread(long id, String fullName) {
		defineThreadLog(id, fullName, _sb);
	}

	@Override
	public synchronized void enter(int depth, long tod, long user, long sys, long threadId, int methodId) {
		enterLog(depth, tod, user, sys, threadId, methodId, _sb);

	}

	@Override
	public synchronized void exit(int depth, long tod, long user, long sys, long threadId, int methodId) {
		exitLog(depth, tod, user, sys, threadId, methodId, _sb);
	}

}
