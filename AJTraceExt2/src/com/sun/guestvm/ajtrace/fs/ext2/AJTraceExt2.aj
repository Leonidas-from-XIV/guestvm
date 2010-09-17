package com.sun.guestvm.ajtrace.fs.ext2;

import com.sun.guestvm.ajtrace.AJTrace;

/**
 * Pointcuts to trace almost everything in the ext2 file system stack.
 * The _avoid pointcuts are essential to allow FSTable to initialize and avoid circularities.
 * 
 * @author Mick Jordan
 *
 */

public aspect AJTraceExt2 extends AJTrace {

	pointcut dev(): execution(* com.sun.guestvm.blk.guk.GUKBlkDevice.read(..)) || execution(* com.sun.guestvm.blk.guk.GUKBlkDevice.write(..));
	pointcut fs_ext2_avoid(): execution(com.sun.guestvm.fs.ext2.Ext2FileSystem.new(..)) || execution(* com.sun.guestvm.fs.ext2.Ext2FileSystem.create(..)) ||
	                                       execution(com.sun.guestvm.fs.ext2.JNodeFSBlockDeviceAPIBlkImpl.new(..)) || execution(* com.sun.guestvm.fs.ext2.JNodeFSBlockDeviceAPIBlkImpl.create(..));
	pointcut fs_ext2_include(): execution(* com.sun.guestvm.fs.ext2.*.*(..));
	pointcut fs_ext2(): fs_ext2_include() /*&& ! fs_ext2_avoid()*/;
	
	pointcut jnode_avoid(): execution(org.jnode.driver.Device.new(..)) || execution(* org.jnode.driver.Device.*(..)) || 
	                                    execution(org.jnode.fs.ext2.Ext2FileSystemType.new(..)) || execution(* org.jnode.fs.ext2.Ext2FileSystemType.create(..)) ||
	                                    execution(org.jnode.fs.ext2.Ext2FileSystem.new(..)) || execution(org.jnode.fs.spi.AbstractFileSystem.new(..)) ||
	                                    execution(org.jnode.fs.ext2.cache.BlockCache.new(..)) || execution(org.jnode.fs.ext2.cache.INodeCache.new(..));
	pointcut jnode_include(): execution(* org.jnode..*.*(..)) || execution(org.jnode..*.new(..));
	pointcut jnode():  jnode_include() /*&& ! jnode_avoid()*/;
	
	public pointcut execAll() : dev() || fs_ext2() || jnode();

}
