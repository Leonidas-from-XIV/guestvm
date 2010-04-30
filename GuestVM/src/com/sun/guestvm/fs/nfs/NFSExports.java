/**
 *
 */
package com.sun.guestvm.fs.nfs;

import java.io.File;
import java.io.IOException;

import org.acplt.oncrpc.OncRpcException;
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
                    logBadEntry("Improper entry in the nfs exports. " + entry + " Does not contain both the local and the export path separated by \""+NFS_EXPORTS_LOCAL_EXPORT_SEPARATOR + "\"");
                }
                final String localPath  =  localExportPath[0];
                final String exportPath  =  localExportPath[1];
                exporter.addExport(new NFSExport(exportPath,new File(localPath)));
            }
            System.out.println("Starting portmap server");
            try {
                final jportmap pm  =  new jportmap();
                pm.run(pm.transports);
                _portMapThread  =  new Thread() {
                    @Override
                    public void run() {
                        try {
                            pm.run();
                        } catch (Exception e){
                            e.printStackTrace();
                            logFailureAndExit("PortMap server error.");
                        }
                    }
                };
                _portMapThread.start();
                System.out.println("Starting NFS server");
                final PathManager pathManager  =  new PathManager(new File("nfs-handles.db"),exporter);
                final NFSServer nfs  =  new NFSServer(pathManager, 0, 0);
                _nfsServerThread  =  new Thread() {
                    @Override
                    public void run() {
                        try {
                            nfs.run();
                        } catch (Exception e){
                            e.printStackTrace();
                            logFailureAndExit("NFS server error.");
                        }
                    }
                };
                _nfsServerThread.start();

                final MountDaemon mountd  =  new MountDaemon(pathManager, exporter, 0, 0);
                _mountdThread  =  new Thread() {
                    @Override
                    public void run() {
                        try {
                            nfs.run();
                        } catch (Exception e){
                            e.printStackTrace();
                            logFailureAndExit("NFS server error.");
                        }
                    }
                };
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

    public static void stopNFSExports() {
        _mountdThread.interrupt();
        _nfsServerThread.interrupt();
        _portMapThread.interrupt();
    }
}
