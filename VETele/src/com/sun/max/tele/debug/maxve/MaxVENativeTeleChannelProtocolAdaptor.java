package com.sun.max.tele.debug.maxve;

import com.sun.max.program.ProgramError;
import com.sun.max.tele.channel.natives.TeleChannelNatives;

public abstract class MaxVENativeTeleChannelProtocolAdaptor implements MaxVETeleChannelProtocol {
	protected TeleChannelNatives natives;
    protected int threadLocalsAreaSize;
	
	public MaxVENativeTeleChannelProtocolAdaptor() {
		natives = new TeleChannelNatives();
	}
	
    @Override
    public boolean initialize(int threadLocalsAreaSize, boolean bigEndian) {
    	this.threadLocalsAreaSize = threadLocalsAreaSize;
        return true;
    }
    
    @Override
    public void setNativeAddresses(long threadListAddress, long bootHeapStartAddress, long resumeAddress) {
    	// by default, nothing to do.
    }

	@Override
	public long create(String programFile, String[] commandLineArguments) {
		return -1;
	}
	
    @Override
    public boolean kill() {
    	return false;
    }
    
    @Override
    public int gatherThreads(long threadLocalsList, long primordialThreadLocals) {
        ProgramError.unexpected("TeleChannelProtocol.gatherThreads(int, int) should not be called in this configuration");
        return 0;
    }

    @Override
    public int readThreads(int size, byte[] gatherThreadsData) {
        ProgramError.unexpected("TeleChannelProtocol.readThreads should not be called in this configuration");
        return 0;
    }
    

}
