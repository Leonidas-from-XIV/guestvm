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
package sun.tools.attach;

import java.io.IOException;
import java.util.*;

/**
 * An implementation of @see Attachprovider for Guest VM.
 *
 * @author Mick Jordan
 */

import com.sun.tools.attach.*;
import com.sun.tools.attach.spi.AttachProvider;

public class GuestVMAttachProvider extends HotSpotAttachProvider {

    private static final String ATTACH_ID_PROPERTY = "guestvm.attach.id";
    private static Map<String, GuestVMVirtualMachineDescriptor> _vmMap = new HashMap<String, GuestVMVirtualMachineDescriptor>();

    public GuestVMAttachProvider() {

    }

    public String name() {
        return "sun";
    }

    public String type() {
        return "socket";
    }

    public VirtualMachine attachVirtualMachine(String vmid) throws AttachNotSupportedException, IOException {
        return new GuestVMVirtualMachine(this, vmid, _vmMap.get(vmid));
    }

    @Override
    public List<VirtualMachineDescriptor> listVirtualMachines() {
        final List<VirtualMachineDescriptor> result = new ArrayList<VirtualMachineDescriptor>();
        /*
         * For the local environment the complete way to do this would be to access the Xenstore to find all the Guest VM domains.
         * That requires a native library so, for now, we require the host:domainid to be passed as a property, guestvm.attach.id.
         */
        final String attachProperty = System.getProperty(ATTACH_ID_PROPERTY);
        if (attachProperty != null) {
            final int cx = attachProperty.indexOf(':');
            if (cx > 0) {
                final String domainIdString = attachProperty.substring(cx + 1);
                final String host = attachProperty.substring(0, cx);
                final int domainId = Integer.parseInt(domainIdString);
                final GuestVMVirtualMachineDescriptor vmd = new GuestVMVirtualMachineDescriptor(this, attachProperty, "guestvm domain " + attachProperty, host, domainId);
                _vmMap.put(attachProperty, vmd);
                result.add(vmd);
            }
        }
        return result;
    }

}
