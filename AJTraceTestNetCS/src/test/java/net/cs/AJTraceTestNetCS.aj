package test.java.net.cs;

import com.sun.guestvm.ajtrace.AJTrace;

public aspect AJTraceTestNetCS extends AJTrace {

	public pointcut execAll(): execution (* test.java.net.cs.*.*(..));

}
