/*
 * Copyright (c) 2007 Sun Microsystems, Inc.  All rights reserved.
 *
 * Sun Microsystems, Inc. has intellectual property rights relating to technology embodied in the product
 * that is described in this document. In particular, and without limitation, these intellectual property
 * rights may include one or more of the U.S. patents listed at http://www.sun.com/patents and one or
 * more additional patents or pending patent applications in the U.S. and in other countries.
 *
 * U.S. Government Rights - Commercial software. Government users are subject to the Sun
 * Microsystems, Inc. standard license agreement and applicable provisions of the FAR and its
 * supplements.
 *
 * Use is subject to license terms. Sun, Sun Microsystems, the Sun logo, Java and Solaris are trademarks or
 * registered trademarks of Sun Microsystems, Inc. in the U.S. and other countries. All SPARC trademarks
 * are used under license and are trademarks or registered trademarks of SPARC International, Inc. in the
 * U.S. and other countries.
 *
 * UNIX is a registered trademark in the U.S. and other countries, exclusively licensed through X/Open
 * Company, Ltd.
 */
package com.sun.max.tele.debug.guestvm.dbchannel.dump;

import com.sun.max.tele.debug.guestvm.dbchannel.ImageFileHandler;
import com.sun.max.tele.debug.guestvm.dbchannel.tcp.TCPXGProtocol;

/**
 * A protocol used to test the Inspector in core dump mode, that uses the XG protocol to
 * communicate with a paused VM domain, and rejects any methods that assume that the VM is active.
 * @author Mick Jordan
 *
 */

public class XGDumpTestProtocol extends TCPXGProtocol {
    public XGDumpTestProtocol(ImageFileHandler imageFileHandler, String hostAndPort) {
        super(imageFileHandler, hostAndPort);
    }

    @Override
    public boolean singleStep(int threadId) {
        DumpProtocol.inappropriate("singleStep");
        return false;
    }

    @Override
    public boolean suspend(int threadId) {
        DumpProtocol.inappropriate("suspend");
        return false;
    }

    @Override
    public boolean suspendAll() {
        DumpProtocol.inappropriate("suspendAll");
        return false;
    }

    @Override
    public int writeBytes(long dst, byte[] src, int srcOffset, int length) {
        DumpProtocol.inappropriate("writeBytes");
        return 0;
    }

    @Override
    public int readWatchpointAccessCode() {
        DumpProtocol.inappropriate("readWatchpointAccessCode");
        return 0;
    }

    @Override
    public long readWatchpointAddress() {
        DumpProtocol.inappropriate("readWatchpointAddress");
        return 0;
    }

    @Override
    public int resume() {
        DumpProtocol.inappropriate("resume");
        return 0;
    }

    @Override
    public int setInstructionPointer(int threadId, long ip) {
        DumpProtocol.inappropriate("setInstructionPointer");
        return 0;
    }

    @Override
    public boolean deactivateWatchpoint(long start, long size) {
        DumpProtocol.inappropriate("deactivateWatchpoint");
        return false;
    }

}
