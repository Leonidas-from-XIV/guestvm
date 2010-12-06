package com.sun.max.config.guestvm;

import com.sun.max.config.BootImagePackage;
import com.sun.max.vm.hosted.*;

/**
 * Redirections for the Virtual Edition extension packages.
 * 
 * @author Mick Jordan
 *
 */

public class Package extends BootImagePackage {
    public Package() {
        super("com.sun.guestvm.**", "gnu.java.util.zip.**", "org.jnode.**", "sun.nio.ch.*");
        // methods we want compiled into the image
        Extensions.registerVMEntryPoint("com.sun.guestvm.net.ip.IP", null);
        Extensions.registerVMEntryPoint("com.sun.guestvm.net.Packet", null);
        Extensions.registerVMEntryPoint("com.sun.guestvm.net.guk.GUKNetDevice", "copyPacket");
        Extensions.registerVMEntryPoint("com.sun.guestvm.blk.guk.GUKBlkDevice", null);
        Extensions.resetField("com.sun.guestvm.logging.Logger", "_singleton");
   }
        
}
