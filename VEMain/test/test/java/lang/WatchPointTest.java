package test.java.lang;


public class WatchPointTest {

    private volatile int _data1;
    private volatile long _data2;

    public static void main(String[] args) {
        new WatchPointTest().run();
    }

    public void run() {
        while (true) {
            _data1++;
            _data2++;
        }
    }
}
