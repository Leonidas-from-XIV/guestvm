package test.nfs;

import com.sun.nfs.*;

public class NfsTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        String server = "sml-ha-vol16.sfbay";
        String[] ops = new String[32];
        String[] fileNames = new String[32];
        String[] fileNames2 = new String[32];
        int opCount = 0;
        boolean echo = false;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("f")) {
                fileNames[opCount] = args[++i];
            } else if (arg.equals("f2")) {
                fileNames2[opCount] = args[++i];
            } else if (arg.equals("op")) {
                ops[opCount++] = args[++i];
                fileNames[opCount] = fileNames[opCount - 1];
            } else if (arg.equals("echo")) {
                echo = true;
            } else if (arg.equals("server")) {
                server = args[++i];
            }
        }
        if (opCount == 0) {
            System.out.println("no operations given");
            return;
        }
        Nfs nfs = null;

        for (int j = 0; j < opCount; j++) {
            try {
                final String fileName = fileNames[j];
                final String op = ops[j];
                if (echo) {
                    System.out.println("command: " + op + " " + fileName);
                }
                if (op.equals("connect")) {
                    nfs = NfsConnect.connectToNfs(server, NfsConnect.NFS_PORT, fileNames[j]);
                    System.out.println(nfs);
                } else if (op.equals("readdir")) {
                    String[] entries = nfs.readdir();
                    for (String entry : entries) {
                        System.out.println(entry);
                    }
                } else if (op.equals("lookup")) {
                    Nfs nfsl = nfs.lookup(fileNames[j]);
                    System.out.println(nfsl);
                }
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }
    }
}
