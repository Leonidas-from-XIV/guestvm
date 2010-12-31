package com.sun.max.config.max.ve;

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
        super("com.sun.max.ve.**", "gnu.java.util.zip.**", "org.jnode.**", "sun.nio.ch.*");
        // methods we want compiled into the image
        Extensions.registerVMEntryPoint("com.sun.max.ve.net.ip.IP", null);
        Extensions.registerVMEntryPoint("com.sun.max.ve.net.Packet", null);
        Extensions.registerVMEntryPoint("com.sun.max.ve.net.guk.GUKNetDevice", "copyPacket");
        Extensions.registerVMEntryPoint("com.sun.max.ve.blk.guk.GUKBlkDevice", null);
        Extensions.resetField("com.sun.max.ve.logging.Logger", "_singleton");
   }
        
}
