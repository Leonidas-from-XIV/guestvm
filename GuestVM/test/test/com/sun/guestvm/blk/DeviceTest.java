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
package test.com.sun.guestvm.blk;

import java.util.*;
import com.sun.guestvm.blk.device.*;
import com.sun.guestvm.blk.guk.*;

public class DeviceTest {

    static BlkDevice _device;
    static boolean _verbose;
    static final int SEED = 24793;
    static int _runTime = 10;
    static boolean _done;
    static int _sectorSize;

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        final String[] ops = new String[10];
        final String[] devices = new String[10];
        final String[] sectors = new String[10];
        int opCount = 0;

        devices[0] = "0";
        sectors[0] = "0";

        Filler filler = new IXFiller();

        // Checkstyle: stop modified control variable check
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.equals("op")) {
                ops[opCount++] = args[++i];
                sectors[opCount] = sectors[opCount - 1];
                devices[opCount] = devices[opCount - 1];
            } else if (arg.equals("s")) {
                sectors[opCount] = args[++i];
            } else if (arg.equals("d")) {
                devices[opCount] = args[++i];
            } else if (arg.equals("v")) {
                _verbose = true;
            } else if (arg.equals("f")) {
                final String fillerName = args[++i];
                filler = (Filler) (Class.forName("test.com.sun.guestvm.blk.DeviceTest$" + fillerName + "Filler").newInstance());
            } else if (arg.equals("t")) {
                _runTime = Integer.parseInt(args[++i]);
            }
        }
        // Checkstyle: resume modified control variable check

        _device = GUKBlkDevice.create();
        final int n = _device.getDevices();
        System.out.println("Devices: " + n);
        final int[] sectorCount = new int[n];
        for (int i = 0; i < n; i++) {
            sectorCount[i] = _device.getSectors(i);
            System.out.println("  device " + i + " has " + sectorCount[i] + " sectors");
        }
        _sectorSize = _device.getSectorSize();

        for (int j = 0; j < opCount; j++) {
            final String op = ops[j];
            final long address = Long.parseLong(sectors[j]);
            final int device = Integer.parseInt(devices[j]);
            if (op.equals("ra")) {
                readAll(device, sectorCount[0], filler);
            } else if (op.equals("wa")) {
                writeAll(device, sectorCount[0], filler);
            } else if (op.equals("r")) {
                read(device, address);
            } else if (op.equals("rr")) {
                readRandom(device, sectorCount[0], filler);
            }
        }
    }

    abstract static class Filler {
        abstract void fill(byte[] data, Object xtra);
    }

    static class SNFiller extends Filler {
        int _sn;
        void fill(byte[] data, Object xtra) {
            final int sn = xtra == null ? _sn : (Integer) xtra;
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) (sn & 0xFF);
            }
            _sn++;
        }
    }

    static class IXFiller extends Filler {
        void fill(byte[] data, Object xtra) {
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) (i & 0xFF);
            }
        }
    }

    static class RNFiller extends Filler {
        Random _random;

        RNFiller() {
            _random = new Random(SEED);
        }

        RNFiller(Random random) {
            _random = random;
        }

        void fill(byte[] data, Object xtra) {
            final Random random = xtra == null ? _random : (Random) xtra;
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) random.nextInt(256);
            }
        }
    }

    private static void writeAll(int device, int sectors, Filler filler) {
        final int sectorSize = _device.getSectorSize();
        final byte[] data = new byte[sectorSize];
        for (int i = 0; i < sectorSize; i++) {
            data[i] = (byte) (i & 0xFF);
        }
        for (int i = 0; i < sectors; i++) {
            filler.fill(data, null);
            _device.write(device, i * sectorSize, data, 0, data.length);
            if (_verbose) {
                System.out.println("wrote sector " + i);
            }
        }
    }

    private static void readAll(int device, int sectors, Filler filler) {
        final byte[] data = new byte[_sectorSize];
        final byte[] checkData = new byte[_sectorSize];
        for (int i = 0; i < sectors; i++) {
            readSector(device, data, checkData, i, filler, null);
        }
    }

    private static void readSector(int device, byte[] data, byte[] checkData, int sector, Filler filler, Object xtra) {
        _device.read(device, sector * _sectorSize, data, 0, data.length);
        filler.fill(checkData, xtra);
        for (int j = 0; j < data.length; j++) {
            if (data[j] != checkData[j]) {
                System.out.println("data mismatch: sector " + sector + ", offset " + j + "read " + data[j] + " check " + checkData[j]);
            }
        }
        if (_verbose) {
            System.out.println("read sector " + sector);
        }
    }

    private static void readRandom(int device, int sectors, Filler filler) {
        final byte[] data = new byte[_sectorSize];
        final byte[] checkData = new byte[_sectorSize];
        final Random random = new Random();
        final Timer timer = new Timer(true);
        timer.schedule(new RunTimerTask(), _runTime * 1000);

        while (!_done) {
            final int sector = random.nextInt(sectors);
            readSector(device, data, checkData, sector, filler, sector);
        }
        System.out.println("Test terminated");
    }

    static class RunTimerTask extends TimerTask {
        public void run() {
            _done = true;
        }
    }

    private static void read(int device, long sector) {
        final int sectorSize = _device.getSectorSize();
        final byte[] data = new byte[sectorSize];
        _device.read(device, sector * sectorSize, data, 0, sectorSize);
        System.out.println("Contents of sector " + sector);
        int c = 0;
        for (int j = 0; j < sectorSize; j++) {
            System.out.print(" 0x" + Integer.toHexString(data[j] & 0xFF));
            if (c++ == 16) {
                System.out.println();
                c = 0;
            }
        }
        System.out.println();
    }

}
