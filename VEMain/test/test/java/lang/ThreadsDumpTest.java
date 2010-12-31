package test.java.lang;

import java.util.*;

public class ThreadsDumpTest {

    private static boolean all = true;
    public static void main(String[] args) throws Exception {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("one")) {
                all = false;
            } else if (args[i].equals("all")) {
                all = true;
            }
        }
        final Thread myThread = new MyThread();
        myThread.start();
        Thread.sleep(1000);
        if (all) {
            final Map<Thread, StackTraceElement[]> traces = Thread.getAllStackTraces();
            for (Map.Entry<Thread, StackTraceElement[]> entry : traces.entrySet()) {
                System.out.println("Stack for thread " + entry.getKey().getName());
                final StackTraceElement[] trace = entry.getValue();
                for (int i = 0; i < trace.length; i++) {
                    System.out.println("\tat " + trace[i]);
                }
            }
        }
    }

    private static class MyThread extends Thread {
        MyThread() {
            setDaemon(all);
        }

        public void run() {
            while (true) {
                if (!all) {
                    final StackTraceElement[] trace = this.getStackTrace();
                    System.out.println("Stack for thread " + this.getName());
                    for (int i = 0; i < trace.length; i++) {
                        System.out.println("\tat " + trace[i]);
                    }
                    return;
                }
            }
        }
    }

}
