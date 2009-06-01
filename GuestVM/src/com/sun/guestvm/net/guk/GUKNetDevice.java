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
package com.sun.guestvm.net.guk;

import com.sun.guestvm.guk.*;
import com.sun.guestvm.net.*;
import com.sun.guestvm.net.debug.*;
import com.sun.guestvm.net.device.*;
import com.sun.max.annotate.*;
import com.sun.max.vm.compiler.*;
import com.sun.max.memory.*;
import com.sun.max.program.*;
import com.sun.max.unsafe.*;
import com.sun.max.util.*;
import com.sun.max.vm.actor.holder.*;


/**
 * This class provides a singleton object that manages the interaction between the
 * low level GUK network driver and the JDK. It manages a ring of packet buffers
 * that are filled by the @see copyPacket upcall from uKernel. This call happens
 * in interrupt mode and so must return promptly without rescheduling, which means no use
 * of Java synchronization, unfortunately. TODO: find a clever way around this problem?
 * So we use spin locks to protect the ring buffer and native thread block/wake calls.
 * This depends on there being precisely one Java thread consuming the packets.
 *
 * In interrupt handler mode, we are not in a proper Java thread context, in particular
 * code with safepoint instructions must not be executed, since that can cause
 * a block if a safepoint has been triggered.  So copyPacket is marked
 * as an interrupt handler and compiled specially. Further, since GC may be
 * ongoing when the interrupt happens the code must not touch any references
 * that might be moved by the GC, which means that the ring buffer and the
 * byte arrays must be allocated in the boot heap at image build time. Therefore,
 * the property that controls the ring buffer size is interpreted at image build time
 * and not run time.
 *
 * @author Mick Jordan
 *
 */
public final class GUKNetDevice implements NetDevice {

    private static final int MAC_ADDRESS_LENGTH = 6;
    private static final int MTU = 1514;
    private static final int DEFAULT_RING_SIZE = 4;
    private static final String RING_SIZE_PROPERTY = "guestvm.net.device.ringsize";
    private static final String DEBUG_PROPERTY = "guestvm.net.device.debug";
    private static int _ringSize = DEFAULT_RING_SIZE;
    private static boolean _debug = false;
    private static GUKNetDevice _device;

  //  private static Pointer _nativeThread;
    private static Pointer _spinlock;
    private static Pointer _completion;
    private static Packet [] _ring;
    private static volatile int _entryCount;
    private static volatile int _readIndex;
    private static volatile int _writeIndex;
    private static boolean _deviceHandlerStarted;
    private static Handler _handler;
    private static long _dropCount;
    private static long _pktCount;
    private static long _truncateCount;
    private static Pointer _transmitBuffer;
    private static boolean _active;

    static {
        final String ringSizeProperty = System.getProperty(RING_SIZE_PROPERTY);
        if (ringSizeProperty != null) {
            _ringSize = Integer.parseInt(ringSizeProperty);
        }
        _ring = new Packet[_ringSize];
        for (int i = 0; i < _ringSize; i++) {
            _ring[i] = Packet.get(MTU);
        }
    }

    private GUKNetDevice() {
        _debug = System.getProperty(DEBUG_PROPERTY) != null;
        _transmitBuffer = Memory.allocate(MTU);
        _spinlock = GUKScheduler.createSpinLock();
        _completion = GUKScheduler.createCompletion();
        final DeviceHandler deviceHandler = new DeviceHandler();
        final Thread deviceThread = new Thread(deviceHandler, "NetDevice");
        deviceThread.setDaemon(true);
        deviceThread.start();
        // We must wait until the thread has really started before we can let copyPacket execute
        synchronized (deviceHandler) {
            while (!_deviceHandlerStarted) {
                try {
                    deviceHandler.wait();
                } catch (InterruptedException ex) {
                }
            }
        }
        // Have to pass the address of copyPacket down to the kernel
        final ClassActor classActor = ClassActor.fromJava(getClass());
        final Address copyMethodAddress = CompilationScheme.Static.getCriticalEntryPoint(classActor.findLocalStaticMethodActor("copyPacket"), CallEntryPoint.C_ENTRY_POINT);
        _active = guestvmXen_netStart(copyMethodAddress);
        _device = this;
    }

    public static GUKNetDevice create() {
        if (_device == null) {
            new GUKNetDevice();
        }
        return _device;
    }

    public boolean active() {
        return _active;
    }

    public byte[] getMACAddress() {
        final Pointer nativeBytes = guestvmXen_getMacAddress();
        if (nativeBytes.isZero()) {
            return null;
        }
        final byte[] result = new byte[MAC_ADDRESS_LENGTH];
        Memory.readBytes(nativeBytes, MAC_ADDRESS_LENGTH, result);
        return result;
    }

    public String getNICName() {
        final Pointer nativeBytes = guestvmXen_getNicName();
        if (nativeBytes.isZero()) {
            return null;
        }
        try {
            return CString.utf8ToJava(nativeBytes);
        } catch (Utf8Exception ex) {
            return null;
        }
    }

    public int getMTU() {
        return MTU;
    }

    public void setReceiveMode(int mode) {
        ProgramError.unexpected("not implemented");
    }

    public synchronized void transmit(Packet pkt) {
        if (!_active) {
            if (_debug) {
                dprintln("device not active");
            }
            return;
        }
        int length = pkt.length();
        if (_debug) {
            dprintln("transmit " + length);
        }
        if (length > MTU) {
            length = MTU;
        }
        for (int i = 0; i < length; i++) {
            _transmitBuffer.writeByte(i, pkt.getByteIgnoringHeaderOffset(i));
        }
        GUK.guk_netfront_xmit(_transmitBuffer, length);
    }

    public void transmit1(Packet buf, int offset, int size) {
        ProgramError.unexpected("not implemented");
    }

    public void registerHandler(Handler handler) {
        _handler = handler;
    }

    public long dropCount() {
        return _dropCount;
    }

    public long pktCount() {
        return _pktCount;
    }

    public long truncateCount() {
        return _truncateCount;
    }

    static class DeviceHandler implements Runnable {
        public void run() {
            synchronized (this) {
                _deviceHandlerStarted = true;
                notify();
            }
            long flags = GUKScheduler.spinLockDisableInterrupts(_spinlock);
            while (true) {
                // Since this is the only thread checking _entryCount we do not need a while loop to recheck
                // that _entryCount has not changed since we woke up (unlike Object.wait)
                // N.B. we hold _spinlock here
                if (_entryCount == 0) {
                    GUKScheduler.spinUnlockEnableInterrupts(_spinlock, flags);
                    GUKScheduler.waitCompletion(_completion);
                } else {
                    GUKScheduler.spinUnlockEnableInterrupts(_spinlock, flags);
                }

                final Packet packet = _ring[_readIndex];
                _readIndex = (_readIndex + 1) % _ringSize;
                if (_handler != null) {
                    _handler.handle(packet);
                }

                flags = GUKScheduler.spinLockDisableInterrupts(_spinlock);
                _entryCount--;
            }
        }
    }

    /**
     * This is upcalled from the network handler. It must not block nor may it call
     * any methods that are compiled with safepoint code installed, because it
     * is in IRQ mode.
     */
    @SuppressWarnings({"unused"})
    @C_FUNCTION(isInterruptHandler = true)
    private static void copyPacket(Pointer p, int pktLength) {
        int length = pktLength;

        // All Packet calls are inlined
        if (_entryCount != _ring.length) {
            final Packet pkt = _ring[_writeIndex];
            pkt.reset();
            if (length > pkt.length()) {
                length = pkt.length();
                _truncateCount++;
            }
            for (int i = 0; i < length; i++) {
                pkt.inlinePutByteIgnoringHdrOffset(p.readByte(i), i);
            }
            _ring[_writeIndex].setLength(length);
            _writeIndex = (_writeIndex + 1) % _ringSize;

            _pktCount++;
            GUKScheduler.spinLock(_spinlock);
            if (_entryCount++ == 0) {
                // wake up DeviceHandler
                GUKScheduler.complete(_completion);
            }
            GUKScheduler.spinUnlock(_spinlock);
        } else {
            _dropCount++;
            // full, drop packet
        }
    }

    private void dprintln(String m) {
        Debug.println("GUKNetDevice [" + Thread.currentThread().getName() + "] " + m);
    }

    @C_FUNCTION
    private static native boolean guestvmXen_netStart(Address address);
    @C_FUNCTION
    private static native Pointer guestvmXen_getMacAddress();
    @C_FUNCTION
    private static native Pointer guestvmXen_getNicName();

}
