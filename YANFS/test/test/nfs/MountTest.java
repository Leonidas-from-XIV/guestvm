package test.nfs;

import com.sun.nfs.Mount;

public class MountTest {

    public static void main(String[] args) {
        String server = "sml-ha-vol16.sfbay";
        for (int i = 0; i < args.length; i++) {

        }
        try {
        String[] exports = Mount.getExports(server);
        if (exports == null) {
            System.out.println("exports returned null");
        } else {
            for (String export : exports) {
                System.out.println(export);
            }
        }
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }
}
