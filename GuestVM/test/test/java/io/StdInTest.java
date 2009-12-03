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
package test.java.io;

import java.io.*;

public class StdInTest {
    public static void main(String[] args) throws IOException {
        boolean echo = false;
        boolean lineReader = false;
        String prompt = null;
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.equals("e")) {
                echo = true;
            } else if (arg.equals("l")) {
                lineReader = true;
            } else if (arg.equals("p")) {
                prompt = args[++i];
            }
        }
        if (lineReader) {
            final BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                if (prompt != null) {
                    System.out.print(prompt);
                }
                final String line = r.readLine();
                if (line == null || (line.length() > 0 && line.charAt(0) == 'q')) {
                    break;
                }
                if (echo) {
                    System.out.println(line);
                }
            }
        } else {
            boolean needPrompt = true;
            while (true) {
                if (prompt != null && needPrompt) {
                    System.out.print(prompt);
                }
                final int b = (char) System.in.read();
                if (b < 0 || b == 'q') {
                    break;
                }
                needPrompt = b == '\n';
                if (echo) {
                    System.out.write(b);
                }
            }
        }
    }

}