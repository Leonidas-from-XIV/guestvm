package test.com.sun.guestvm.ajtrace;

import com.sun.guestvm.ajtrace.*;

public class TraceLogTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			TraceLog log = TraceLogFactory.create();
			exercise(log);
		} catch (Exception ex) {
			System.err.println(ex);
		}
	}
	
	private static void exercise(TraceLog log) {
		log.init(System.nanoTime());
		log.defineThread(1, "myThread");
		log.defineThread(1, "methodName");
		log.enter(1, System.nanoTime(), 100, 50, 1, 1);
		log.exit(1, System.nanoTime(), 105, 55, 1, 1);
		log.fini(System.nanoTime());
	}

}
