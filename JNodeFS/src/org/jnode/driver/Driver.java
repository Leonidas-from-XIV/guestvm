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
 * Modified from JNode original by Mick Jordan, May 2009.
 *
 */
/*
 * $Id: Driver.java 4973 2009-02-02 07:52:47Z lsantha $
 *
 * Copyright (C) 2003-2009 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jnode.driver;

/**
 * Abstract driver of a Device.
 * <p/>
 * Every device driver must extend this class directly or indirectly.
 * <p/>
 * A suitable driver for a specific Device is found by a DeviceToDriverMapper.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @see org.jnode.driver.Device
 * @see org.jnode.driver.DeviceToDriverMapper
 */
public abstract class Driver {

    /**
     * The device this driver it to control
     */
    private Device device;

    /**
     * Default constructor
     */
    public Driver() {
    }

    /**
     * Sets the device this driver is to control.
     *
     * @param device The device to control, never null
     *               from the device.
     * @throws DriverException
     */
    protected final void connect(Device device)
        throws DriverException {
        if (this.device != null) {
            throw new DriverException("This driver is already connected to a device");
        }
        verifyConnect(device);
        this.device = device;
        afterConnect(device);
    }

    /**
     * Gets the device this driver is to control.
     *
     * @return The device I'm driving
     */
    public final Device getDevice() {
        return device;
    }

    /**
     * This method is called just before a new device is set to this driver.
     * If we should refuse the given device, throw a DriverException.
     *
     * @param device
     * @throws DriverException
     */
    protected void verifyConnect(Device device)
        throws DriverException {
        /* do nothing for now */
    }

    /**
     * This method is called after a new device is set to this driver.
     * You can initialize the driver and/or the device here.
     * Note not to start the device yet.
     *
     * @param device
     */
    protected void afterConnect(Device device) {
        /* do nothing for now */
    }

    /**
     * Start the device.
     *
     * @throws DriverException
     */
    protected abstract void startDevice()
        throws DriverException;

    /**
     * Stop the device.
     *
     * @throws DriverException
     */
    protected abstract void stopDevice()
        throws DriverException;

}
