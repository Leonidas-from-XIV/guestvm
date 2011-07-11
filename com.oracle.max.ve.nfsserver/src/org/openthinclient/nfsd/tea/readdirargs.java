/*******************************************************************************
 * openthinclient.org ThinClient suite
 *
 * Copyright (C) 2004, 2007 levigo holding GmbH. All Rights Reserved.
 *
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 *******************************************************************************/
/*
 * Automatically generated by jrpcgen 1.0.5 on 29.04.05 21:09 jrpcgen is part of
 * the "Remote Tea" ONC/RPC package for Java See
 * http://remotetea.sourceforge.net for details
 */
package org.openthinclient.nfsd.tea;

import java.io.IOException;

import org.acplt.oncrpc.OncRpcException;
import org.acplt.oncrpc.XdrAble;
import org.acplt.oncrpc.XdrDecodingStream;
import org.acplt.oncrpc.XdrEncodingStream;

public class readdirargs implements XdrAble {
    public nfscookie cookie;
    public int count;
    public nfs_fh dir;

    public readdirargs() {
    }

    public readdirargs(XdrDecodingStream xdr) throws OncRpcException,
                                             IOException {
        xdrDecode(xdr);
    }

    public void xdrDecode(XdrDecodingStream xdr) throws OncRpcException,
                                                IOException {
        dir = new nfs_fh(xdr);
        cookie = new nfscookie(xdr);
        count = xdr.xdrDecodeInt();
    }

    public void xdrEncode(XdrEncodingStream xdr) throws OncRpcException,
                                                IOException {
        dir.xdrEncode(xdr);
        cookie.xdrEncode(xdr);
        xdr.xdrEncodeInt(count);
    }

}
// End of readdirargs.java
