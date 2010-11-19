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

import java.nio.*;
import java.util.*;

import com.sun.max.platform.*;
import com.sun.max.program.*;
import com.sun.max.tele.*;
import com.sun.max.tele.debug.*;
import com.sun.max.tele.debug.TeleNativeThread.*;
import com.sun.max.tele.memory.*;
import com.sun.max.tele.page.*;
import com.sun.max.unsafe.*;

public class GuestVMTeleDomain extends TeleProcess {

    private int domainId;
    private final DataAccess dataAccess;

    protected GuestVMTeleDomain(TeleVM teleVM, Platform platform, int id) {
        super(teleVM, platform, ProcessState.STOPPED);
        this.domainId = id;
        dataAccess = new PageDataAccess(this, platform.dataModel);
        GuestVMXenDBChannel.attach(this, id);
    }

    @Override
    public DataAccess dataAccess() {
        return dataAccess;
    }

    @Override
    protected TeleNativeThread createTeleNativeThread(Params params) {
        /* Need to align and skip over the guard page at the base of the stack.
         * N.B. "base" is low address (i.e., actually the end of the stack!).
         */
        final int pageSize = platform().pageSize;
        final long stackBottom = pageAlign(params.stackRegion.start().toLong(), pageSize) + pageSize;
        final long adjStackSize = params.stackRegion.size().toLong() - (stackBottom - params.stackRegion.start().toLong());
        final TeleFixedMemoryRegion adjStack = new TeleFixedMemoryRegion(vm(), params.stackRegion.regionName(), Address.fromLong(stackBottom), Size.fromLong(adjStackSize));
        params.stackRegion = adjStack;
        return new GuestVMNativeThread(this, params);
    }

    private static long pageAlign(long address, int pageSize) {
        final long alignment = pageSize - 1;
        return (long) (address + alignment) & ~alignment;

    }

    @Override
    protected void kill() throws OSExecutionRequestException {
    	if (!TeleVM.isDump()) {
            ProgramWarning.message("unimplemented: " + "cannot kill target domain from Inspector");
    	}
    }

    // In the current synchronous connection with the target domain, we only ever stop at a breakpoint
    // and control does not return to the inspector until that happens (see GuestVMDBChannel.nativeResume)

    @Override
    protected ProcessState waitUntilStopped() {
    	return GuestVMXenDBChannel.waitUntilStopped();
    }

    @Override
    protected void resume() throws OSExecutionRequestException {
    	GuestVMXenDBChannel.resume(domainId);
    }

    @Override
    public void suspend() throws OSExecutionRequestException {
        if (!GuestVMXenDBChannel.suspendAll()) {
            throw new OSExecutionRequestException("Could not suspend the VM");
        }
    }

    @Override
    public void setTransportDebugLevel(int level) {
        GuestVMXenDBChannel.setTransportDebugLevel(level);
        super.setTransportDebugLevel(level);
    }

    @Override
    protected int read0(Address address, ByteBuffer buffer, int offset, int length) {
        return GuestVMXenDBChannel.readBytes(address, buffer, offset, length);
    }

    @Override
    protected int write0(ByteBuffer buffer, int offset, int length, Address address) {
        return GuestVMXenDBChannel.writeBytes(buffer, offset, length, address);
    }

    @Override
    protected void gatherThreads(List<TeleNativeThread> threads) {
        final Word primordialThreadLocals = dataAccess().readWord(vm().bootImageStart().plus(vm().bootImage().header.primordialETLAOffset));
        final Word threadLocalsList = dataAccess().readWord(vm().bootImageStart().plus(vm().bootImage().header.tlaListHeadOffset));
        GuestVMXenDBChannel.gatherThreads(threads, threadLocalsList.asAddress().toLong(), primordialThreadLocals.asAddress().toLong());
    }

    @Override
    public int platformWatchpointCount() {
        // not sure how many are supported; we'll try this
        return Integer.MAX_VALUE;
    }

    @Override
    protected boolean activateWatchpoint(TeleWatchpoint teleWatchpoint) {
        return GuestVMXenDBChannel.activateWatchpoint(domainId, teleWatchpoint);
    }

    @Override
    protected boolean deactivateWatchpoint(TeleWatchpoint teleWatchpoint) {
        return GuestVMXenDBChannel.deactivateWatchpoint(domainId, teleWatchpoint.memoryRegion());
    }

    @Override
    protected long readWatchpointAddress() {
        return GuestVMXenDBChannel.readWatchpointAddress(domainId);
    }

    @Override
    protected int readWatchpointAccessCode() {
        int code = GuestVMXenDBChannel.readWatchpointAccessCode(domainId);
        if (code == 1) {
            return 1;
        } else if (code == 2) {
            return 2;
        } else if (code == 4) {
            return 3;
        }
        return 0;
    }
}
