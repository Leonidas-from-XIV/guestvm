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

/**
 * Export file systems as NFS exports
 */
package com.sun.guestvm.fs.nfs;

import java.io.File;

import org.acplt.oncrpc.apps.jportmap.jportmap;
import org.openthinclient.mountd.ListExporter;
import org.openthinclient.mountd.MountDaemon;
import org.openthinclient.mountd.NFSExport;
import org.openthinclient.nfsd.NFSServer;
import org.openthinclient.nfsd.PathManager;

import com.sun.guestvm.error.GuestVMError;


/**
 * @author Puneeet Lakhina
 *
 */
public class NFSExports {
    private static final String NFS_EXPORTS_PROPERTY = "guestvm.nfs.exports";
    private static final String NFS_EXPORTS_LOCAL_EXPORT_SEPARATOR = ":";
    private static final String NFS_EXPORTS_SEPARATOR = ";";
    private static boolean _initNFSExports  =  false;
    private static Thread _portMapThread;
    private static Thread _mountdThread;
    private static Thread _nfsServerThread;
    /**Initializes the NFS Exports.
     *
     */
    public static void initNFSExports() {
        if (_initNFSExports) {
            return;
        }
        final String exporttable  =  System.getProperty(NFS_EXPORTS_PROPERTY);
        if (exporttable !=  null) {
            final String[] exportTableEntries  =  exporttable.split(NFS_EXPORTS_SEPARATOR);
            final ListExporter exporter  =  new ListExporter();
            for (String entry : exportTableEntries) {
                final String[] localExportPath  =  entry.split(NFS_EXPORTS_LOCAL_EXPORT_SEPARATOR);
                if (localExportPath.length !=  2) {
                    logBadEntry("Improper entry in the nfs exports. " + entry + " Does not contain both the local and the export path separated by \"" + NFS_EXPORTS_LOCAL_EXPORT_SEPARATOR + "\"");
                }
                final String localPath  =  localExportPath[0];
                final String exportPath  =  localExportPath[1];
                exporter.addExport(new NFSExport(exportPath, new File(localPath)));
            }
            System.out.println("Starting portmap server");
            try {
                final jportmap pm  =  new jportmap();
                _portMapThread  =  new Thread() {
                    @Override
                    public void run() {
                        try {
                            pm.run(pm.transports);
                        } catch (Exception e) {
                            e.printStackTrace();
                            logFailureAndExit("PortMap server error.");
                        }
                    }
                };
                _portMapThread.setDaemon(true);
                _portMapThread.start();
                System.out.println("Starting NFS server");
                final PathManager pathManager  =  new PathManager(new File("nfs-handles.db"), exporter);
                final NFSServer nfs  =  new NFSServer(pathManager, 0, 0);
                _nfsServerThread  =  new Thread() {
                    @Override
                    public void run() {
                        try {
                            nfs.run();
                        } catch (Exception e) {
                            e.printStackTrace();
                            logFailureAndExit("NFS server error.");
                        }
                    }
                };
                _nfsServerThread.setDaemon(true);
                _nfsServerThread.start();
                System.out.println("Starting mount server");
                final MountDaemon mountd  =  new MountDaemon(pathManager, exporter, 0, 0);
                _mountdThread  =  new Thread() {
                    @Override
                    public void run() {
                        try {
                            mountd.run();
                        } catch (Exception e) {
                            e.printStackTrace();
                            logFailureAndExit("NFS server error.");
                        }
                    }
                };
                _mountdThread.setDaemon(true);
                _mountdThread.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private static void logBadEntry(String msg) {
        GuestVMError.exit(msg);
    }
    private static void logFailureAndExit(String msg) {
        GuestVMError.exit(msg);
    }
    /**Forcefully shutdown NFS Server and related threads. Not used currently anywhere. The shutdown in the VM is achieved  by marking the threads daemon
     *
     */
    public static void stopNFSExports() {
        _mountdThread.interrupt();
        _nfsServerThread.interrupt();
        _portMapThread.interrupt();
    }
}
