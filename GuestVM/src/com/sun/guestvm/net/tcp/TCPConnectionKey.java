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
package com.sun.guestvm.net.tcp;

import com.sun.guestvm.net.ip.IPAddress;


/**
 * A unique, immutable, key for a connection that is used in maps.
 * 
 * @author Puneeet Lakhina
 * @author Mick Jordan
 *
 */
public class TCPConnectionKey {

    final private int _localPort;
    final private int _remotePort;
    final private long _remoteIp;

    public TCPConnectionKey(int localPort,int remotePort, long remoteIp) {
        _localPort = localPort;
        _remotePort = remotePort;
        _remoteIp = remoteIp;
    }
    
    @Override
    public boolean equals(Object other) {
        if (other != null && other instanceof TCPConnectionKey) {
            TCPConnectionKey otherkey = (TCPConnectionKey) other;
            return otherkey._localPort == _localPort && otherkey._remotePort == _remotePort && otherkey._remoteIp == _remoteIp;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hcode = 17 * 37;
        hcode += hcode * 37 + _remotePort;
        hcode += hcode * 37 + _remoteIp;
        hcode += hcode * 37 + _localPort;
        return hcode;
    }

    @Override
    public String toString() {
        return String.format("Local port:%d Remote IP:%s Remote Port: %d",_localPort,IPAddress.toString((int)_remoteIp),_remotePort);
    }


}
