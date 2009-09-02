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
package com.sun.guestvm.process;

/**
 * A filter for whoami, which is given by the user.name property.
 *
 * @author Mick Jordan
 *
 */

public class WhoamiProcessFilter extends ProcessFilterHelper {

    public WhoamiProcessFilter() {
        super("whoami");
    }

    public int exec(byte[] prog, byte[] argBlock, int argc, byte[] envBlock, int envc, byte[] dir)  {
        final String userName = System.getProperty("user.name");
        if (userName == null) {
            return -1;
        }
        return nextId();
    }

    protected int readBytes(int fd, byte[] bytes, int offset, int length, long fileOffset) {
        if (fd == StdIO.ERR.ordinal()) {
            return 0;
        } else if (fd == StdIO.OUT.ordinal()) {
            final byte[] userName = System.getProperty("user.name").getBytes();
            final int available = userName.length - (int) fileOffset;
            if (available <= 0) {
                return 0;
            } else {
                final int rlength = length < available ? length : available;
                System.arraycopy(userName, (int) fileOffset, bytes, offset, rlength);
                return rlength;
            }
        } else {
            assert false;
            return 0;
        }
    }

}
