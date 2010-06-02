package com.sun.max.tele.debug.guestvm.dbchannel.dump;

import java.nio.*;
import java.util.*;

import com.sun.max.collect.*;
import com.sun.max.program.*;
import com.sun.max.elf.xen.section.prstatus.X86_64Registers;
import com.sun.max.tele.debug.*;
import com.sun.max.tele.debug.guestvm.*;
import com.sun.max.tele.debug.guestvm.dbchannel.*;
import com.sun.max.vm.thread.VmThreadLocal;
import com.sun.max.tele.MaxThreadState;

import static com.sun.max.vm.thread.VmThreadLocal.*;
import static com.sun.guestvm.sched.GUKVmThread.*;
import static com.sun.max.tele.MaxThreadState.*;
import static com.sun.max.tele.thread.NativeThreadLocal.*;

/**
 * Accesses the GUK thread list to support the gathering of threads by the Inspector.
 *
 * @author Mick Jordan
 *
 */
public class GUKThreadListAccess {
    private SimpleProtocol protocol;
    private final static int MAXINE_THREAD_ID = 40;
    private static final int NATIVE_THREAD_LOCALS_STRUCT_SIZE = 72;
    private long threadListAddress;
    private int threadLocalsAreaSize;

    public static class ThreadInfo {
        public final int id;
        public final int flags;
        public final int cpu;
        public long rsp;
        public long rip;

        ThreadInfo(int id, int flags, int cpu, long rip, long rsp) {
            this.id = id;
            this.flags = flags;
            this.cpu = cpu;
            this.rip = rip;
            this.rsp =rsp;
        }
    }

    public GUKThreadListAccess(SimpleProtocol protocol, long threadListAddress, int threadLocalsAreaSize) {
        this.protocol = protocol;
        this.threadListAddress = threadListAddress;
        this.threadLocalsAreaSize = threadLocalsAreaSize;
    }

    @SuppressWarnings("unchecked")
    public boolean gatherThreads(Object teleDomainObject, Object threadSeq, long threadLocalsList, long primordialThreadLocals) {
        final ByteBuffer threadLocals = ByteBuffer.allocate(threadLocalsAreaSize).order(ByteOrder.LITTLE_ENDIAN);
        final ByteBuffer nativeThreadLocals = ByteBuffer.allocate(NATIVE_THREAD_LOCALS_STRUCT_SIZE).order(ByteOrder.LITTLE_ENDIAN);

        List<ThreadInfo> threads = gatherGUKThreads(threadListAddress);
        for (ThreadInfo threadInfo : threads) {
            final boolean found = findThreadLocals(threadLocalsList, primordialThreadLocals, threadInfo.rsp, threadLocals, nativeThreadLocals);
            int id = threadInfo.id;
            if (!found) {
                /* Make id negative to indicate no thread locals were available for the thread.
                 * This will be the case for a native thread or a Java thread that has not yet
                 * executed past the point in VmThread.run() where it is added to the active
                 * thread list.
                 */
                id = id < 0 ? id : -id;
                setInStruct(threadLocals, VmThreadLocal.ID.offset, id);
            }
            try {
                SimpleProtocol.GatherThreadData t = new SimpleProtocol.GatherThreadData(
                        (int) getFromStruct(threadLocals, ID.offset),
                        threadInfo.id,
                        getFromStruct(nativeThreadLocals, HANDLE.offset),
                        toThreadState(threadInfo.flags).ordinal(), threadInfo.rip,
                        getFromStruct(nativeThreadLocals, STACKBASE.offset), getFromStruct(nativeThreadLocals, STACKSIZE.offset),
                        getFromStruct(nativeThreadLocals, TLBLOCK.offset), getFromStruct(nativeThreadLocals, TLBLOCKSIZE.offset),
                        threadLocalsAreaSize
                        );
                Trace.line(1, "calling jniGatherThread id=" + t.id + ", lh=" + t.localHandle + ", h=" + Long.toHexString(t.handle) + ", st=" + t.state +
                        ", ip=" + Long.toHexString(t.instructionPointer) + ", sb=" + Long.toHexString(t.stackBase) + ", ss=" + Long.toHexString(t.stackSize) +
                        ", tlb=" + Long.toHexString(t.tlb) + ", tlbs=" + t.tlbSize + ", tlas=" + t.tlaSize);
                GuestVMTeleDomain teleDomain = (GuestVMTeleDomain) teleDomainObject;
                teleDomain.jniGatherThread((AppendableSequence<TeleNativeThread>) threadSeq, t.id, t.localHandle, t.handle, t.state, t.instructionPointer, t.stackBase, t.stackSize, t.tlb, t.tlbSize, t.tlaSize);
            } catch (Exception ex) {
                ProgramError.unexpected("invoke failure on jniGatherThread", ex);
            }
        }
        return true;
    }

    static MaxThreadState toThreadState(int state) {
        if ((state & AUX1_FLAG) != 0) {
            return MONITOR_WAIT;
        }
        if ((state & AUX2_FLAG) != 0) {
            return NOTIFY_WAIT;
        }
        if ((state & JOIN_FLAG) != 0) {
            return JOIN_WAIT;
        }
        if ((state & SLEEP_FLAG) != 0) {
            return SLEEPING;
        }
        if ((state & WATCH_FLAG) != 0) {
            return WATCHPOINT;
        }
        // default
        return SUSPENDED;
    }

    public List<ThreadInfo> gatherGUKThreads(long threadListAddress) {
        final ArrayList<ThreadInfo> result = new ArrayList<ThreadInfo>();
        final ByteBuffer listHeadBuffer = ByteBuffer.allocate(STRUCT_LIST_HEAD_SIZE).order(ByteOrder.LITTLE_ENDIAN);
        int n = protocol.readBytes(threadListAddress, listHeadBuffer.array(), 0, STRUCT_LIST_HEAD_SIZE);
        assert n == STRUCT_LIST_HEAD_SIZE;
        long threadStructAddress = listHeadBuffer.getLong(NEXT_OFFSET);
        final ByteBuffer threadStructBuffer = ByteBuffer.allocate(STRUCT_THREAD_SIZE).order(ByteOrder.LITTLE_ENDIAN);
        while (threadStructAddress != threadListAddress) {
            threadStructAddress -= THREAD_LIST_OFFSET;
            n = protocol.readBytes(threadStructAddress, threadStructBuffer.array(), 0, STRUCT_THREAD_SIZE);
            assert n == STRUCT_THREAD_SIZE;
            final int flags = threadStructBuffer.getInt(FLAGS_OFFSET);
            final int id = threadStructBuffer.getShort(ID_OFFSET);
            if ((id == MAXINE_THREAD_ID) || ((flags & UKERNEL_FLAG) == 0)) {
                final int cpu = threadStructBuffer.getInt(CPU_OFFSET);
                long rip;
                long rsp;
                if ((flags & RUNNING_FLAG) != 0) {
                    // full register set is available
                    final byte[] integerRegisters = new byte[128];
                    final byte[] floatingPointRegisters = new byte[128];
                    final byte[] stateRegisters = new byte[16];
                    protocol.readRegisters(id, integerRegisters, integerRegisters.length,
                            floatingPointRegisters, floatingPointRegisters.length,
                            stateRegisters, stateRegisters.length);
                    final ByteBuffer stateRegBuffer = ByteBuffer.wrap(stateRegisters).order(ByteOrder.LITTLE_ENDIAN);
                    final ByteBuffer intRegBuffer = ByteBuffer.wrap(integerRegisters).order(ByteOrder.LITTLE_ENDIAN);
                    rip = stateRegBuffer.getLong(0);
                    rsp = intRegBuffer.getLong(X86_64Registers.IntegerRegister.RSP.getCanonicalIndex());
                } else {
                    // at last reschedule, rip and rsp were saved in thread struct
                    rsp = threadStructBuffer.getLong(SP_OFFSET);
                    rip = threadStructBuffer.getLong(IP_OFFSET);
                }
                result.add(new ThreadInfo(id, flags, cpu, rip, rsp));
            }
            threadStructAddress = threadStructBuffer.getLong(THREAD_LIST_OFFSET);
        }

        return result;
    }

    private static void zeroBuffer(ByteBuffer bb) {
        byte[] b = bb.array();
        for (int i = 0; i < b.length; i++) {
            b[i] = 0;
        }
    }

    static long getFromStruct(ByteBuffer bb, int offset) {
        return bb.getLong(offset);
    }

    static void setInStruct(ByteBuffer bb, int offset, long value) {
        bb.putLong(offset, value);
    }

    boolean isThreadLocalsForStackPointer(long stackPointer, long tl, ByteBuffer tlCopy, ByteBuffer ntlCopy) {
        long ntl;

        int n = protocol.readBytes(tl, tlCopy.array(), 0, threadLocalsAreaSize);
        assert n == threadLocalsAreaSize;
        ntl = getFromStruct(tlCopy, NATIVE_THREAD_LOCALS.offset);
        n = protocol.readBytes(ntl, ntlCopy.array(), 0, NATIVE_THREAD_LOCALS_STRUCT_SIZE);
//        Trace.line(1, "findThreadLocals : " + Long.toHexString(stackPointer));
        long stackBase = ntlCopy.getLong(STACKBASE.offset);
        long stackSize = ntlCopy.getLong(STACKSIZE.offset);
        return stackBase <= stackPointer && stackPointer < (stackBase + stackSize);
    }



    /**
     * Searches the thread locals list in the VM's address space for an entry 'tl' such that:
     *
     *   tl.stackBase <= stackPointer && stackPointer < (tl.stackBase + tl.stackSize)
     *
     * If such an entry is found, then its contents are copied from the VM to the structs pointed to by 'tlCopy' and 'ntlCopy'.
     *
     * @param threadLocalsList the head of the thread locals list in the VM's address space
     * @param primordialThreadLocals the primordial thread locals in the VM's address space
     * @param stackPointer the stack pointer to search with
     * @param tlCopy pointer to storage for a set of thread locals into which the found entry
     *        (if any) will be copied from the VM's address space
     * @param ntlCopy pointer to storage for a NativeThreadLocalsStruct into which the native thread locals of the found entry
     *        (if any) will be copied from the VM's address space
     * @return {@code true} if the entry was found, {@code false} otherwise
     */
    boolean findThreadLocals(long threadLocalsList, long primordialThreadLocals, long stackPointer, ByteBuffer tlCopy, ByteBuffer ntlCopy) {
        zeroBuffer(tlCopy);
        zeroBuffer(ntlCopy);
        if (threadLocalsList != 0) {
            long tl = threadLocalsList;
            while (tl != 0) {
                if (isThreadLocalsForStackPointer(stackPointer, tl, tlCopy, ntlCopy)) {
                    return true;
                }
                tl = getFromStruct(tlCopy, FORWARD_LINK.offset);
            };
        }
        if (primordialThreadLocals != 0) {
            if (isThreadLocalsForStackPointer(stackPointer, primordialThreadLocals, tlCopy, ntlCopy)) {
                return true;
            }
        }
        return false;
    }
}
