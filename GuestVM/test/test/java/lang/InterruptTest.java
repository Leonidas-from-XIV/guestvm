package test.java.lang;


public class InterruptTest  {

    /**
     * @param args
     */
    public static void main(String[] args) {
        boolean waitTest = false;
        boolean sleepTest = false;
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.equals("s")) {
                sleepTest = true;
            } else if (arg.equals("w")) {
                waitTest = true;
            }
        }
        if (waitTest) {
            final Thread waitInterruptee = new WaitInterruptee();
            waitInterruptee.setName("interruptee");
            waitInterruptee.start();
            waitInterruptee.interrupt();
            try {
                waitInterruptee.join();
            } catch (InterruptedException ex) {
                System.out.println("[" + Thread.currentThread().getName() + "] caught InterruptedException on join, status " + Thread.currentThread().isInterrupted());
            }
        }

        if (sleepTest) {
            final Thread sleepInterruptee = new SleepInterruptee();
            sleepInterruptee.setName("interruptee");
            sleepInterruptee.start();
            sleepInterruptee.interrupt();
            try {
                sleepInterruptee.join();
            } catch (InterruptedException ex) {
                System.out.println("[" + Thread.currentThread().getName() + "] caught InterruptedException on join, status " + Thread.currentThread().isInterrupted());
            }
        }
    }

    static class WaitInterruptee extends Thread {
        public void run() {
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                    System.out.println("[" + Thread.currentThread().getName() + "] caught InterruptedException on wait, status " + Thread.currentThread().isInterrupted());
                }
            }
        }
    }

    static class SleepInterruptee extends Thread {
        public void run() {
            synchronized (this) {
                try {
                    sleep(1000);
                } catch (InterruptedException ex) {
                    System.out.println("[" + Thread.currentThread().getName() + "] caught InterruptedException on sleep, status " + Thread.currentThread().isInterrupted());
                }
            }
        }
    }

}
