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

public class fattr implements XdrAble {
    public nfstime atime;
    public int blocks;
    public int blocksize;
    public nfstime ctime;
    public int fileid;
    public int fsid;
    public int gid;
    public int mode;
    public nfstime mtime;
    public int nlink;
    public int rdev;
    public int size;
    public int type;
    public int uid;

    public fattr() {
    }

    public fattr(XdrDecodingStream xdr) throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrDecode(XdrDecodingStream xdr) throws OncRpcException,
                                                IOException {
        type = xdr.xdrDecodeInt();
        mode = xdr.xdrDecodeInt();
        nlink = xdr.xdrDecodeInt();
        uid = xdr.xdrDecodeInt();
        gid = xdr.xdrDecodeInt();
        size = xdr.xdrDecodeInt();
        blocksize = xdr.xdrDecodeInt();
        rdev = xdr.xdrDecodeInt();
        blocks = xdr.xdrDecodeInt();
        fsid = xdr.xdrDecodeInt();
        fileid = xdr.xdrDecodeInt();
        atime = new nfstime(xdr);
        mtime = new nfstime(xdr);
        ctime = new nfstime(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr) throws OncRpcException,
                                                IOException {
        xdr.xdrEncodeInt(type);
        xdr.xdrEncodeInt(mode);
        xdr.xdrEncodeInt(nlink);
        xdr.xdrEncodeInt(uid);
        xdr.xdrEncodeInt(gid);
        xdr.xdrEncodeInt(size);
        xdr.xdrEncodeInt(blocksize);
        xdr.xdrEncodeInt(rdev);
        xdr.xdrEncodeInt(blocks);
        xdr.xdrEncodeInt(fsid);
        xdr.xdrEncodeInt(fileid);
        atime.xdrEncode(xdr);
        mtime.xdrEncode(xdr);
        ctime.xdrEncode(xdr);
    }

}
// End of fattr.java
