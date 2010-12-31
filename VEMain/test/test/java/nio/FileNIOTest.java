/*
 * Copyright (c) 2009 Sun Microsystems, Inc., 4150 Network Circle, Santa Clara, California 95054, U.S.A. All rights
 * reserved.
 *
 * U.S. Government Rights - Commercial software. Government users are subject to the Sun Microsystems, Inc. standard
 * license agreement and applicable provisions of the FAR and its supplements.
 *
 * Use is subject to license terms.
 *
 * This distribution may include materials developed by third parties.
 *
 * Parts of the product may be derived from Berkeley BSD systems, licensed from the University of California. UNIX is a
 * registered trademark in the U.S. and in other countries, exclusively licensed through X/Open Company, Ltd.
 *
 * Sun, Sun Microsystems, the Sun logo and Java are trademarks or registered trademarks of Sun Microsystems, Inc. in the
 * U.S. and other countries.
 *
 * This product is covered and controlled by U.S. Export Control laws and may be subject to the export or import laws in
 * other countries. Nuclear, missile, chemical biological weapons or nuclear maritime end uses or end users, whether
 * direct or indirect, are strictly prohibited. Export or reexport to countries subject to U.S. embargo or to entities
 * identified on U.S. export exclusion lists, including, but not limited to, the denied persons and specially designated
 * nationals lists is strictly prohibited.
 */

/**
 *
 */
package test.java.nio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;


/**
 * @author Puneeet Lakhina
 *
 */
public class FileNIOTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        if(args.length < 0) {
            fail("Usage test.java.nio.FileNIOTest scratchdirectory");
        }
        File scratchDir = new File(args[0]);
        if(!scratchDir.exists()) {
            if(!scratchDir.mkdirs()) {
                fail("Couldnt create scratch directory");
            }
        }else if(!scratchDir.isDirectory()) {
            fail(args[0] + " is not a directory");
        }

        //Test File Writing using File Channels
        File tempFile = new File(scratchDir,"tempfile");
        try {
            log("Testing Simple read/write");
            String content="some content";
            tempFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(tempFile);
            FileChannel fch = fos.getChannel();
            fch.write(ByteBuffer.wrap(content.getBytes()));
            fos.close();
            fch.close();
            log("Simple write success");
            FileInputStream fis = new FileInputStream(tempFile);
            fch=fis.getChannel();
            byte[] readArr =new byte[content.getBytes().length];
            fis.read(readArr);
            String readContent = new String(readArr);
            if(!content.equals(readContent)) {
                fail("Writtent and read values not same");
            }
            fis.close();
            fch.close();
            log("Simple read success");
            log("FileNIOTest Simple read/write test passed");

            log("Testing Offseted read/write");
            fos = new FileOutputStream(tempFile);
            fch=fos.getChannel();
            fch.position(content.getBytes().length);
            fch.write(ByteBuffer.wrap(content.getBytes()));
            fch.close();
            fos.close();
            log("Offseted write success");
            fis = new FileInputStream(tempFile);
            fch=fis.getChannel();
            fch.position(content.getBytes().length);
            readArr =new byte[content.getBytes().length];
            fis.read(readArr);
            readContent = new String(readArr);
            if(!content.equals(readContent)) {
                fail("Writtent and read values not same");
            }
            log("Offseted read success");
            log("FileNIOTest Offseted read/write test passed");

        } catch (IOException ioe) {
            ioe.printStackTrace();
            fail(ioe.getMessage());
        }


    }
    private static void fail(String msg) {
        System.err.println(msg);
        System.exit(1);
    }

    private static void log(String msg) {
        System.out.println("FileNIOTest: "+msg);
    }
}
