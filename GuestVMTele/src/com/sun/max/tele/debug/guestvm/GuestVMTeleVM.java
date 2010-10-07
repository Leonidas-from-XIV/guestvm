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
package com.sun.max.tele.debug.guestvm;

import static com.sun.max.platform.Platform.*;

import java.io.*;

import com.sun.max.program.*;
import com.sun.max.tele.*;
import com.sun.max.tele.debug.*;
import com.sun.max.unsafe.*;
import com.sun.max.vm.hosted.BootImage;
import com.sun.max.vm.hosted.BootImageException;

public class GuestVMTeleVM extends TeleVM {

    public GuestVMTeleVM(File bootImageFile, BootImage bootImage, Classpath sourcepath, String[] commandlineArguments) throws BootImageException {
        super(bootImageFile, bootImage, sourcepath, commandlineArguments);
    }

    @Override
    protected TeleProcess createTeleProcess(String[] commandLineArguments) throws BootImageException {
        throw new BootImageException("domain creation not supported from the Inspector");
    }

    @Override
    protected TeleProcess attachToTeleProcess() {
        return new GuestVMTeleDomain(this, platform(), targetLocation().id);
    }

    @Override
    protected Pointer loadBootImage() throws BootImageException {
    	// the only reason we override this is to ensure we go via GuestVMDBChannel to get the lock
        return GuestVMXenDBChannel.getBootHeapStart();
    }

}
