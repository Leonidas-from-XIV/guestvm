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
package com.sun.max.tele.debug.maxve;

import com.sun.max.tele.debug.*;
import com.sun.max.unsafe.*;
import com.sun.max.vm.runtime.*;

public class MaxVENativeThread extends TeleNativeThread {

    @Override
    public MaxVETeleDomain teleProcess() {
        return (MaxVETeleDomain) super.teleProcess();
    }

    protected MaxVENativeThread(MaxVETeleDomain teleDomain, Params params) {
        super(teleDomain, params);
    }

    @Override
    protected boolean readRegisters(byte[] integerRegisters, byte[] floatingPointRegisters, byte[] stateRegisters) {
        return MaxVEXenDBChannel.readRegisters((int) localHandle(),
                        integerRegisters, integerRegisters.length,
                        floatingPointRegisters, floatingPointRegisters.length,
                        stateRegisters, stateRegisters.length);
    }

    @Override
    protected boolean updateInstructionPointer(Address address) {
        return MaxVEXenDBChannel.setInstructionPointer((int) localHandle(), address.toLong());
    }

    @Override
    protected boolean singleStep() {
        return MaxVEXenDBChannel.singleStep((int) localHandle());
    }

    // In the current synchronous connection with the target domain, we only ever stop at a breakpoint
    // and control does not return to the inspector until that happens.

    @Override
    public boolean threadSuspend() {
        return MaxVEXenDBChannel.suspend((int) localHandle());
    }

    @Override
    protected boolean threadResume() {
        throw FatalError.unimplemented();
    }
}
