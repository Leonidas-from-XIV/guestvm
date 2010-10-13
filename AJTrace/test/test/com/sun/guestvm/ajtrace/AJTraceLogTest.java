package test.com.sun.guestvm.ajtrace;

import com.sun.guestvm.ajtrace.*;

public class AJTraceLogTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			AJTraceLog log = AJTraceLogFactory.create();
			exercise(log);
		} catch (Exception ex) {
			System.err.println(ex);
		}
	}

	private static void exercise(AJTraceLog log) {
		log.init(System.nanoTime());
		log.defineThread(1, "myThread");
		log.defineThread(1, "methodName");
		log.enter(1, System.nanoTime(), 100, 50, 1, 1, null);
		log.exit(1, System.nanoTime(), 105, 55, 1, 1, null);
		log.fini(System.nanoTime());
	}

}
