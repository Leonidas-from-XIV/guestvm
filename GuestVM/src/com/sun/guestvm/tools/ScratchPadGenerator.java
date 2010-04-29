/*
 * Copyright (c) 2009 Sun Microsystems, Inc., 4150 Network Circle, Santa Clara, California 95054, U.S.A. All rights
 * reserved.
 *
 * U.S. Government Rights - Commercial software. Government users are subject to the Sun Microsystems, Inc. standard
 * license agreement and applicable provisions of the FAR and its supplements.
 *
 * Use is subject to license terms.
 *
 * This distribution may include materials developed by third parties.
 *
 * Parts of the product may be derived from Berkeley BSD systems, licensed from the University of California. UNIX is a
 * registered trademark in the U.S. and in other countries, exclusively licensed through X/Open Company, Ltd.
 *
 * Sun, Sun Microsystems, the Sun logo and Java are trademarks or registered trademarks of Sun Microsystems, Inc. in the
 * U.S. and other countries.
 *
 * This product is covered and controlled by U.S. Export Control laws and may be subject to the export or import laws in
 * other countries. Nuclear, missile, chemical biological weapons or nuclear maritime end uses or end users, whether
 * direct or indirect, are strictly prohibited. Export or reexport to countries subject to U.S. embargo or to entities
 * identified on U.S. export exclusion lists, including, but not limited to, the denied persons and specially designated
 * nationals lists is strictly prohibited.
 */

/**
 *
 */
package com.sun.guestvm.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import com.sun.guestvm.tools.ext2.Ext2FileTool;

/**
 * Generates a xen domain config file, a run script (that uses bin/run) and disk files for running guestvm based on the properties file provided.
 * Syntax of the properties file
 * <pre>
 * disk1=<disk file path>
 * disk1.init=<true | false> #Create disk and copy JDK dirs and jars onto it.
 * disk1.mntpoint = <mount point in GuestVM> #The run script if generated will contain the appropriate guestvm.fs.table string based on all disks and mount points
 * disk1.copypaths=<localpath:virtualdiskpath,localpath:virtualdiskpath> #specify files or directories to be copied onto the virtual disk.
 * disk1.ro=<true|false> #should this be mounted as readonly. if false/null, defaults to auto
 * xenconfig=<the suffix of the xen domain config file.> #This file will be created in the directory ./xmconfigs/ with the name domain_config_suffix
 * xenconfig.initmem=<initial memory allocated to the GuestVM domain. Defaults to 256>
 * xenconfig.maxmem=<Maximum memory allocated to the GuestVM domain. Defaults to 1024>
 *
 * runscript=<the name of the run script file generated>
 *
 * <pre>
 * @author Puneeet Lakhina
 *
 */
public class ScratchPadGenerator {

    static class DiskImage {

        File _imageFile;
        boolean _init;
        String _mountpoint;
        String _guestvmFsTableString;
        boolean _readOnly;
        String _frontEndDevice;
        Map<File, File> _copyPaths;
    }

    static class XenConfig {

        String _fileSuffix;
        int _initMemory;
        int _maxMemory;
        String _macAddress;
        String _vifBridge;
        List<DiskImage> _diskImageConfigs;
    }

    private static final String JAVA_HOME_PROPERTY="JAVA_HOME";
    private static final String XEN_CONFIG_PROPERTY="xenconfig";
    private static final String XEN_CONFIG_MAXMEM_PROPERTY="xenconfig.maxmem";
    private static final String XEN_CONFIG_INITMEM_PROPERTY="xenconfig.initmem";
    private static final String DISK_CONFIG_PROPERTY_PREFIX="disk";
    private static final String DISK_CONFIG_INIT_PROPERTY_SUFFIX="init";
    private static final String DISK_CONFIG_MOUNT_POINT_PROPERTY_SUFFIX="mnpoint";
    private static final String DISK_CONFIG_RO_PROPERTY_SUFFIX="ro";
    private static final String DISK_CONFIG_COPYPATHS_PROPERTY_SUFFIX="copypaths";
    private static final String RUNSCRIPT_FILE_NAME_PROPERTY="runscript";
    private static final String GUESTVM_FSTABLE_STRING_SEPARATOR=";";

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        final Properties p = new Properties();
        p.load(new FileInputStream(args[0]));
        boolean error = false;
        final String javaHome = System.getenv(JAVA_HOME_PROPERTY);
        if (javaHome == null) {
            System.err.println(JAVA_HOME_PROPERTY + " not defined");
            error = true;
        }
        final XenConfig config = new XenConfig();
        if (p.containsKey(XEN_CONFIG_PROPERTY)) {
            config._fileSuffix = p.getProperty(XEN_CONFIG_PROPERTY);
            if (p.containsKey(XEN_CONFIG_INITMEM_PROPERTY)) {
                config._initMemory = Integer.parseInt(p.getProperty(XEN_CONFIG_INITMEM_PROPERTY));
            } else {
                config._initMemory = 256;
            }
            if (p.containsKey(XEN_CONFIG_MAXMEM_PROPERTY)) {
                config._maxMemory = Integer.parseInt(p.getProperty(XEN_CONFIG_MAXMEM_PROPERTY));
            } else {
                if (config._initMemory < 1024) {
                    config._maxMemory = 1024;
                } else {
                    config._maxMemory = config._initMemory + 256;
                }
            }
        } else {
            System.err.println("No Xen Config Specified");
            error = true;
        }
        final List<DiskImage> diskImageConfigs = new ArrayList<DiskImage>();
        config._diskImageConfigs = diskImageConfigs;
        // Disk Image operations
        for (int i = 1; i <= 5; i++) {
            final String propPrefix = DISK_CONFIG_PROPERTY_PREFIX + i;
            if (p.containsKey(DISK_CONFIG_PROPERTY_PREFIX + i)) {
                final File imageFile = new File(p.getProperty(propPrefix));
// if (!imageFile.exists()) {
// System.err.println("Image File:" + imageFile.getAbsolutePath() + " does not exist");
// error = true;
// }
                final DiskImage diskImage = new DiskImage();
                diskImage._imageFile = imageFile;
                diskImage._frontEndDevice = "sda" + i;
                diskImage._init = "true".equals(p.get(propPrefix + DISK_CONFIG_INIT_PROPERTY_SUFFIX));
                diskImage._mountpoint = p.getProperty(propPrefix + DISK_CONFIG_MOUNT_POINT_PROPERTY_SUFFIX);
                diskImage._readOnly = "true".equals(p.get(propPrefix + DISK_CONFIG_RO_PROPERTY_SUFFIX));
                if (diskImage._mountpoint == null) {
                    System.err.println("A mount point must be specified for disk" + i);
                    error = true;
                }
                final Map<File, File> copyPaths = new HashMap<File, File>();
                if (p.containsKey(propPrefix + DISK_CONFIG_COPYPATHS_PROPERTY_SUFFIX)) {
                    final String[] copyDirsString = p.getProperty(propPrefix + DISK_CONFIG_COPYPATHS_PROPERTY_SUFFIX).split(",");
                    for (String pair : copyDirsString) {
                        final String[] fsgvPathPair = pair.split(":");
                        final File fileSystemPath = new File(fsgvPathPair[0]);
                        if (!fileSystemPath.exists()) {
                            System.err.println("File/Directory " + fileSystemPath + "doesnt exist");
                            error = true;
                        }
                        if ("".equals(fsgvPathPair[1])) {
                            System.err.println("Virtual Disk Image Path for " + fileSystemPath + " not Provided");
                            error = true;
                        }
                        copyPaths.put(fileSystemPath, new File(fsgvPathPair[1]));
                    }
                    diskImage._copyPaths = copyPaths;
                }
                diskImage._guestvmFsTableString = String.format("ext2:/blk/%d:%s:%s", i - 1, diskImage._mountpoint, diskImage._readOnly ? "ro" : "auto");
                diskImageConfigs.add(diskImage);
            } else {
                break;
            }
        }
        if (diskImageConfigs.isEmpty()) {
            System.err.println("No Disk Images specified");
            error = true;
        }

        if (error) {
            System.err.println("Error occured. Exiting");
            return;
        }
        String guestvmJavaHome = null;
        final StringBuilder guestvmFsString = new StringBuilder();
        // Create Disk Image File.
        for (DiskImage diskImage : diskImageConfigs) {
            final String imageFilePath = diskImage._imageFile.getAbsolutePath();
            guestvmFsString.append(diskImage._guestvmFsTableString + GUESTVM_FSTABLE_STRING_SEPARATOR);
            if (diskImage._init) {

                System.out.println("Creating disk file:" + imageFilePath);
                diskImage._imageFile.delete();
                Runtime.getRuntime().exec("mkfile 256m " + imageFilePath);
                Ext2FileTool.main(new String[] {"format", "-disk", imageFilePath});
                // Copy Java onto it.
                System.out.println("Copying JDK jars");
                final String ext2JavaHome = javaHome.substring(javaHome.lastIndexOf('/'));
                guestvmJavaHome = diskImage._mountpoint + ext2JavaHome + "/jre";
                Ext2FileTool.main(("mkdir -disk " + imageFilePath + " -ext2path " + ext2JavaHome).split("\\s"));
                Ext2FileTool.main(("mkdir -disk " + imageFilePath + " -ext2path " + ext2JavaHome + "/lib").split("\\s"));
                Ext2FileTool.main(("mkdir -disk " + imageFilePath + " -ext2path " + ext2JavaHome + "/jre").split("\\s"));
                Ext2FileTool.main(("mkdir -disk " + imageFilePath + " -ext2path " + ext2JavaHome + "/jre/lib").split("\\s"));
                Ext2FileTool.main(("mkdir -disk " + imageFilePath + " -ext2path " + ext2JavaHome + "/jre/bin").split("\\s"));
                Ext2FileTool.main(("mkfile -disk " + imageFilePath + " -ext2path " + ext2JavaHome + "/jre/bin/java").split("\\s"));
                Ext2FileTool.main(("copyin -disk " + imageFilePath + " -from " + javaHome + "/lib/tools.jar -ext2path " + ext2JavaHome + "/lib").split("\\s"));
                final String[] jreLibFiles = new String[] {"rt.jar", "jce.jar", "jsse.jar", "resources.jar", "charsets.jar", "management-agent.jar"};
                for (String jreLibFile : jreLibFiles) {
                    Ext2FileTool.main(("copyin -disk " + imageFilePath + " -from " + javaHome + "/jre/lib/" + jreLibFile + " -ext2path " + ext2JavaHome + "/jre/lib/" + jreLibFile).split("\\s"));
                }
                // Copy properties files.
                System.out.println("Copying JDK Properties files");
                for (String f : new File(javaHome + "/jre/lib/").list(new FilenameFilter() {

                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".properties");
                    }
                })) {
                    Ext2FileTool.main(("copyin -disk " + imageFilePath + " -from " + javaHome + "/jre/lib/" + f + " -ext2path " + ext2JavaHome + "/jre/lib/" + f).split("\\s"));
                }
                System.out.println("Copying JDK directories");
                final String[] copyDirectoryies = new String[] {"security", "zi", "management", "ext"};
                for (String copydirectory : copyDirectoryies) {
                    System.out.println("Copying: " + copydirectory);
                    Ext2FileTool.main(("mkdir -disk " + imageFilePath + " -ext2path " + ext2JavaHome + "/jre/lib/" + copydirectory).split("\\s"));
                    Ext2FileTool.main(("copyin -r -disk " + imageFilePath + " -from " + javaHome + "/jre/lib/" + copydirectory + " -ext2path " + ext2JavaHome + "/jre/lib/" + copydirectory).split("\\s"));
                }
            }

            System.out.println("Copying files if any");
            if (diskImage._copyPaths != null) {
                for (Entry<File, File> diskImageCopyPath : diskImage._copyPaths.entrySet()) {
                    System.out.println("Copying: " + diskImageCopyPath.getKey());
                    if (diskImageCopyPath.getKey().isDirectory()) {
                        Ext2FileTool.main(("mkdir -disk " + imageFilePath + " -ext2path " + diskImageCopyPath.getValue()).split("\\s"));
                        Ext2FileTool.main(("copyin -r -disk " + imageFilePath + " -from " + diskImageCopyPath.getKey().getAbsolutePath() + " -ext2path " + diskImageCopyPath.getValue()).split("\\s"));
                    } else {
                        Ext2FileTool.main(("copyin -disk " + imageFilePath + " -from " + diskImageCopyPath.getKey().getAbsolutePath() + " -ext2path " + diskImageCopyPath.getValue()).split("\\s"));
                    }
                }
            }
        }
        System.out.println("Creating Xen Config");
        // Create Xen Config
        final StringBuilder builder = new StringBuilder("kernel= \"guestvm\"\n");
        builder.append("memory = \"" + config._initMemory + "\"\n");
        builder.append("maxmem = \"" + config._maxMemory + "\"\n");

        if (!diskImageConfigs.isEmpty()) {
            final StringBuilder diskStringBuilder = new StringBuilder("disk = [");
            for (DiskImage dic : diskImageConfigs) {
                diskStringBuilder.append("\"file:///" + dic._imageFile.getAbsolutePath() + "," + dic._frontEndDevice + ",w\",");
            }
            diskStringBuilder.setLength(diskStringBuilder.length() - 1);
            diskStringBuilder.append("]\n");
            builder.append(diskStringBuilder.toString());
        }
        builder.append("name = \"GuestVM-\" + os.environ.get(\"USER\")\n");
        builder.append("on_crash = 'destroy'\n");
        System.out.println("Generating Xen Config");
        final FileWriter fw = new FileWriter("xmconfigs/domain_config_" + config._fileSuffix);
        fw.write(builder.toString());
        fw.close();

        System.out.println("Creating run script");
        // Create run script
        if (p.containsKey(RUNSCRIPT_FILE_NAME_PROPERTY)) {
            final File runScriptFile = new File(p.getProperty(RUNSCRIPT_FILE_NAME_PROPERTY));
            final BufferedWriter bw = new BufferedWriter(new FileWriter(runScriptFile));
            bw.write("#!/bin/bash\n");
            if (guestvmJavaHome != null) {
                bw.write(String.format("javahomearg=\"-J-Djava.home=%s\"\n", guestvmJavaHome));
            }
            if (!diskImageConfigs.isEmpty()) {
                guestvmFsString.setLength(guestvmFsString.length() - 1);
                bw.write(String.format("fstable=\"-J-Dguestvm.fs.table=%s\"\n", guestvmFsString.toString()));
            }
            bw.write(String.format("./bin/run -cf %s $javahomearg $fstable $*\n", config._fileSuffix));
            bw.close();
            runScriptFile.setExecutable(true);
        }
    }

}
