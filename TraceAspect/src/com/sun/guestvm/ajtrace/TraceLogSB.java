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

import java.io.*;

/**
 * Legacy format expected by TraceAnalyzer tool.
 * [] means optional
 * 0 S S t                  start time
 * 0 D TX fn               define short name TX for thread fn
 * 0 M MX fn             define short name MX for method fn
 * d E[t] T M              method M entry [at time t,u,s] in thread T
 * d R[t] M                 method M return [at time t,u,s]
 * 
 * This implementation is inefficient in that it allocates a StringBuilder per trace and
 * writes it out per trace. So trace log is ordered by time, with thread traces interleaved and
 * no sync required (other than that implicit on the output stream).
 * 
 * It defines protected methods that log the data to a StringBuilder for use by subclasses
 * that might, for example, buffer the output.
 * 
 * @author Mick Jordan
*/ 

public class TraceLogSB extends TraceLog {

	protected PrintStream _ps;
	private long _startTime;
	
	@Override
	public void init(long startTime) {
		initPS();
		final StringBuilder sb = new StringBuilder();
		initLog(startTime, sb);
		_ps.print(sb);
	}
	
	protected void initPS() {
		final OutputStream logStream = TraceLogFile.create();
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
	}
	
	@Override
	public void fini(long endTime) {
		_ps.flush();
		if (_ps != System.out) {
			_ps.close();
		}
	}

	@Override
	public void defineThread(int id, String fullName) {
		final StringBuilder sb = new StringBuilder();
		defineThreadLog(id, fullName, sb);
		_ps.print(sb);
	}
	
	protected void defineThreadLog(int id, String fullName, StringBuilder sb) {
		sb.append("0 D T");
		sb.append(id);
		sb.append(' ');
		sb.append(fullName);
		sb.append('\n');
	}
	
	@Override
	public void defineMethod(int id, String fullName) {
		final StringBuilder sb = new StringBuilder();
		defineMethodLog(id, fullName, sb);
		_ps.print(sb);
	}
	
	protected void defineMethodLog(int id, String fullName, StringBuilder sb) {
		sb.append("0 M M");
		sb.append(id);
		sb.append(' ');
		sb.append(fullName);
		sb.append('\n');
	}
	
	@Override
	public void enter(int depth, long tod, long user, long sys, int threadId, int methodId) {
		final StringBuilder sb = new StringBuilder();
		enterLog(depth, tod, user, sys, threadId, methodId, sb);
		_ps.print(sb);
	}
	
	protected void enterLog(int depth, long tod, long user, long sys, int threadId, int methodId, StringBuilder sb) {
		sb.append(depth);
		sb.append(' ');
		sb.append('E');
		appendTime(tod, user, sys, sb);
		sb.append(" T");
		sb.append(threadId);
		sb.append(" M");
		sb.append(methodId);
		sb.append('\n');
	}
	
	@Override
	public void exit(int depth, long tod, long user, long sys, int threadId, int methodId) {
		final StringBuilder sb = new StringBuilder();
		exitLog(depth, tod, user, sys, threadId, methodId, sb);
		_ps.print(sb);
	}
	
	protected void exitLog(int depth, long tod, long user, long sys, int threadId, int methodId, StringBuilder sb) {
		sb.append(depth);
		sb.append(' ');
		sb.append('R');
		appendTime(tod, user, sys, sb);
		sb.append(" T");
		sb.append(threadId);
		sb.append(" M");
		sb.append(methodId);
		sb.append('\n');
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
}
