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
package test.java.net.cs;

import java.io.*;

public class FileSessionData extends SessionData {

    private String _pathname;

    FileSessionData(String pathname) {
        _pathname = pathname;
    }

    public byte[] getSessionData() {
        FileInputStream fs = null;
        byte[] data = null;
        try {
            final File f = new File(_pathname);
            data = new byte[(int) f.length()];
            fs = new FileInputStream(f);
            int count = 0;
            int offset = 0;
            // CheckStyle: stop inner assignment check
            while ((offset < data.length) && (count = fs.read(data, offset, data.length - offset)) > 0) {
                offset += count;
            }
         // CheckStyle: resume inner assignment check
            if (offset != data.length) {
                throw new IOException("partial read of serialized file");
            }
        } catch (IOException e) {
            System.out.print(e);
            System.exit(-1);
        } finally {
            if (fs != null) {
                try {
                    fs.close();
                } catch (IOException e2) {
                }
            }
        }
        return data;
    }
}

