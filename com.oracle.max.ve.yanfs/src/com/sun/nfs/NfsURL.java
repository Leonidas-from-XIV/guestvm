/*
 * Copyright (c) 1998, 2007, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.sun.nfs;

import java.net.MalformedURLException;

/**
 * This is just a dumb URL parser class.
 * I wrote it because I got fed up with the
 * JDK URL class calling NFS URL's "invalid"
 * simply because the Handler wasn't installed.
 *
 * This URL parser also handles undocumented
 * testing options inserted in the URL in the
 * port field.  The following sequence of option
 * letters may appear before or after the port
 * number, or alone if the port number is not
 * given.
 *         vn	- NFS version, e.g. "v3"
 *         u	- Force UDP - normally TCP is preferred
 *         t	- Force TDP - don't fall back to UDP
 *         m    - Force Mount protocol.  Normally public filehandle
 *                is preferred
 *
 * Option ordering is not important.
 *
 * Example:
 *         nfs://server:123v2um/path
 *
 *            Use port 123 with NFS v2 over UDP and Mount protocol
 *
 *         nfs://server:m/path
 *
 *            Use default port, prefer V3/TCP but use Mount protocol
 *
 * @author Brent Callaghan
 */

public class NfsURL {

    private String url;
    private String protocol;
    private String host;
    private String location;
    private int port;
    private String file;

    /*
     * Undocumented testing options
     */
    private int version;
    private String proto;
    private boolean pub = true;

    public NfsURL(String url) throws MalformedURLException {
        int p, q, r;

        url = url.trim();	// remove leading & trailing spaces
        this.url = url;
        int end = url.length();

        p = url.indexOf(':');
        if (p < 0)
            throw new MalformedURLException("colon expected");
        protocol = url.substring(0, p);
        p++;	// skip colon
        if (url.regionMatches(p, "//", 0, 2)) {	// have hostname
            p += 2;
            q = url.indexOf('/', p);
            if (q < 0)
                q = end;
            location = url.substring(0, q);
            r = url.indexOf(':', p);
            if (r > 0 && r < q) {
                byte[] opts = url.substring(r + 1, q).toLowerCase().getBytes();
                for (int i = 0; i < opts.length; i++) {
                    if (opts[i] >= '0' && opts[i] <= '9') {
                        port = (port * 10) + (opts[i] - '0');
                    } else {
                        switch (opts[i]) {
                        case 'v':	// NFS version
                            version = opts[++i] - '0';
                            break;
                        case 't':	// Force TCP only
                            proto = "tcp";
                            break;
                        case 'u':	// Force UDP only
                            proto = "udp";
                            break;
                        case 'w':	// Force WebNFS only
                            pub = true;
                            break;
                        case 'm':	// Force MOUNT protocol only
                            pub = false;
                            break;
                        default:
                            throw new MalformedURLException(
                                "invalid port number");
                        }
                    }
                }
            } else {
                r = q;	// no port
            }
            if (p < r)
                host = url.substring(p, r);
        } else {
             q = p;
        }

        if (q < end)
            file = url.substring(q + 1, end);

    }

    public String getProtocol() {
        return (protocol);
    }

    public String getLocation() {
        return (location);
    }

    public String getHost() {
        return (host);
    }

    public int getPort() {
        return (port);
    }

    public String getFile() {
        return (file);
    }

    /*
     * Undocumented options for testing
     */
    int getVersion() {
        return (version);
    }

    String getProto() {
        return (proto);
    }

    boolean getPub() {
        return (pub);
    }


    @Override
    public String toString() {
        String s = getProtocol() + ":";

        if (host != null)
            s += "//" + host;

        if (port > 0)
            s += ":" + port;

        if (file != null)
            s += "/" + file;

        return (s);
    }
}
