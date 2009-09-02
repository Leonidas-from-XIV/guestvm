package test.java.lang;

import java.util.Random;

public class RandomTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        final Random r = new Random();
        for (int i = 0; i < 10; i++) {
            System.out.println("nextLong: " + r.nextLong());
        }

    }

}
