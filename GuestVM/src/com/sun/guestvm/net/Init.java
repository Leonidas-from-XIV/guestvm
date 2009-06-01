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
package com.sun.guestvm.net;

/**
 * Initialization of the network stack.
 *
 * It is convenient to build the basic network configuration settings, e.g. IP address,
 * into the image. This is done at image build time via a property that specifies a properties file
 * that contains the default settings. These can be overridden selectively at runtime.
 *
 * @author Mick Jordan
 */

import java.io.*;
import java.util.*;
import com.sun.guestvm.net.arp.*;
import com.sun.guestvm.net.device.*;
import com.sun.guestvm.net.dns.DNS;
import com.sun.guestvm.net.dhcp.*;
import com.sun.guestvm.net.ip.*;
import com.sun.guestvm.net.udp.*;
import com.sun.guestvm.net.tcp.*;
import com.sun.guestvm.net.protocol.ether.*;
import com.sun.max.vm.runtime.FatalError;

public class Init {
    // Interpreted at image build time
    private static final String NETWORK_CONFIG_FILE_PROPERTY = "guestvm.net.config.file";

    // Can be overridden at runtime
    private static final String USE_DHCP_PROPERTY = "guestvm.net.dhcp";
    private static final String IPADDRESS_IDENTIFY_PROPERTY = "guestvm.net.ipaddress.identify";
    private static final String IPADDRESS_PROPERTY = "guestvm.net.ipaddress";
    private static final String GATEWAY_PROPERTY = "guestvm.net.gateway";
    private static final String NETMASK_PROPERTY = "guestvm.net.netmask";
    private static final String NAMESERVER_PROPERTY = "guestvm.net.nameserver";
    private static final String DOMAINNAME_PROPERTY = "guestvm.net.domainname";

    private static String _defaultLocalAddress;
    private static String _defaultOwnGateway;
    private static String _defaultNetMask;
    private static String _defaultNameServer;
    private static String _defaultDomainName;
    private static boolean _identify;

    private static DNS _dns;
    private static ARP _arp;
    private static Ether _ether;
    private static IP _ip;
    private static boolean _useDHCP;
    private static IPAddress _localAddress;
    private static IPAddress _ownGateway;
    private static IPAddress _netMask;
    private static IPAddress _nameServer;
    private static String _domainName;
    private static String _myHostname;
    private static NetDevice[] _netDevices;
    private static LoopbackDevice _loopbackDevice;
    private static Map<NetDevice, IPAddress> _ipMap = new HashMap<NetDevice, IPAddress>();
    private static Init _singleton;

    static {
        readConfigFile();
    }

    private static void readConfigFile() {
        FileReader fs = null;
        final String configFile = System.getProperty(NETWORK_CONFIG_FILE_PROPERTY);
        if (configFile != null) {
            try {
                fs = new FileReader(configFile);
                final Properties netProps = new Properties();
                netProps.load(fs);
                final String identifyProperty = (String) netProps.get(IPADDRESS_IDENTIFY_PROPERTY);
                _identify = identifyProperty != null && emptyOrTrue(identifyProperty);
                final String dhcpProperty = (String) netProps.get(USE_DHCP_PROPERTY);
                _useDHCP = dhcpProperty != null && emptyOrTrue(dhcpProperty);
                if (!_useDHCP) {
                    _defaultLocalAddress = getDefaultProperty(netProps, IPADDRESS_PROPERTY);
                    _defaultOwnGateway = getDefaultProperty(netProps, GATEWAY_PROPERTY);
                    _defaultNetMask = getDefaultProperty(netProps, NETMASK_PROPERTY);
                    _defaultNameServer = getDefaultProperty(netProps, NAMESERVER_PROPERTY);
                    _defaultDomainName = getDefaultProperty(netProps, DOMAINNAME_PROPERTY);
                }
            } catch (IOException ex) {
                System.out.println("WARNING: I/O failure processing network config file: " + configFile);
            } finally {
                if (fs != null) {
                    try {
                        fs.close();
                    } catch (Exception ex) {
                    }
                }
            }
        } else {
            System.out.println("WARNING: no network configuration file specified");
        }
    }

    /**
     * Returns true if the property value is "" or "true".
     * @param propertyValue
     * @return truth value
     */
    private static boolean emptyOrTrue(String propertyValue) {
        return propertyValue.equals("") || propertyValue.equals("true");
    }

    private static String getDefaultProperty(Properties props, String property) {
        final String value = props.getProperty(property);
        if (value == null) {
            FatalError.unexpected("network config property " + property + " not specified");
        }
        return value;
    }

    public Init(NetDevice[] nics) {
        _singleton = this;
        // It is possible that the network interface is not active
        // in which case this is all pointless
        if (!nics[0].active()) {
            return;
        }

        _netDevices = new NetDevice[nics.length + 1];
        System.arraycopy(nics, 0, _netDevices, 0, nics.length);
        _loopbackDevice = new LoopbackDevice();
        _netDevices[nics.length] = _loopbackDevice;

        _ether = new Ether(nics[0]);
        UDP.initialize();
        _arp = ARP.getARP(_ether);
        _ip = IP.getIP(_ether, _arp);
        // connect IP and ARP with Ether
        _ether.registerHandler(_arp, "ARP");
        _ether.registerHandler(_ip, "IP");

        checkConfig();

        _ipMap.put(_netDevices[0], _localAddress);
        _ipMap.put(_netDevices[nics.length], IPAddress.loopback());
        IP.init(_localAddress.addressAsInt(), _netMask.addressAsInt());
        ProtocolStack.setRoute(_ownGateway.addressAsInt());

        _dns = new DNS(_nameServer, _domainName);
        _myHostname = _dns.reverseLookup(_localAddress);

        TCP.init();
    }

    public static Init get() {
        return _singleton;
    }

    public static NetDevice[] getNetDevices() {
        return _netDevices;
    }

    public static String hostName() {
        return _myHostname;
    }

    /**
     * Check the network configuration properties and set configuration based on them.
     */
    public static void checkConfig() {
        _useDHCP = emptyOrTrue(getStringProperty(USE_DHCP_PROPERTY, _useDHCP ? "true" : "false"));
        _identify = emptyOrTrue(getStringProperty(IPADDRESS_IDENTIFY_PROPERTY, _identify ? "true" : "false"));
        if (_useDHCP) {
            final DHCP dhcp = DHCP.getDHCP(_ether.getMacAddress(), null);
            _localAddress = dhcp.sendRequest();
            if (_localAddress == null) {
                FatalError.crash("failed to get IP address from DHCP");
            }
            _ownGateway = dhcp.gateway();
            _netMask = dhcp.netmask();
            _nameServer = dhcp.resolver();
            _domainName = dhcp.domainName();
        } else {
            _localAddress = getIPAddress(IPADDRESS_PROPERTY, _defaultLocalAddress);
            _ownGateway = getIPAddress(GATEWAY_PROPERTY, _defaultOwnGateway);
            _netMask = getIPAddress(NETMASK_PROPERTY, _defaultNetMask);
            _nameServer = getIPAddress(NAMESERVER_PROPERTY, _defaultNameServer);
            _domainName = getStringProperty(DOMAINNAME_PROPERTY, _defaultDomainName);
        }
        if (_identify) {
            System.out.println("Guest VM domain IP address: " + _localAddress);
        }

    }

    private static void checkDefault(String property, String value) {
        if (value == null) {
            FatalError.crash("no value for network property " + property);
        }
    }

    private static IPAddress getIPAddress(String propertyName, String defaultValue) throws NumberFormatException {
        String ipAddressString = System.getProperty(propertyName);
        if (ipAddressString == null) {
            checkDefault(propertyName, defaultValue);
            ipAddressString = defaultValue;
        }
        return IPAddress.parse(ipAddressString);
    }

    private static String getStringProperty(String propertyName, String defaultValue) {
        String value = System.getProperty(propertyName);
        if (value == null) {
            checkDefault(propertyName, defaultValue);
            value = defaultValue;
        }
        return value;
    }

    public static DNS getDNS() {
        return _dns;
    }

    public static ARP getARP() {
        return _arp;
    }

    public static IPAddress getLocalAddress() {
        return _localAddress;
    }

    public static IPAddress getLocalAddress(NetDevice netDevice) {
        return _ipMap.get(netDevice);
    }

    public static IPAddress getNetMask() {
        return _netMask;
    }

    public static IPAddress getGateway() {
        return _ownGateway;
    }

    public static NetDevice getLoopbackDevice() {
        return _loopbackDevice;
    }

}
