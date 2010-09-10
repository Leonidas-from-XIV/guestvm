package com.sun.max.tele.debug.guestvm;

import com.sun.max.tele.channel.TeleChannelProtocol;

/**
 * GuestVM-specific extension of the standard {@link TeleChannelProtocol}.
 *
 * @author Mick Jordan
 *
 */

public interface GuestVMTeleChannelProtocol extends TeleChannelProtocol {
	/**
	 * It may be necessary to inform the native layer of certain key addresses.
	 */
	void setNativeAddresses(long threadListAddress, long bootHeapStartAddress, long resumeAddress);
}
