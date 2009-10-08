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
 * $Id: FileSystem.java 3762 2008-02-05 22:56:13Z fduminy $
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

package org.jnode.fs;

import java.io.IOException;

import org.jnode.driver.Device;

/**
 * @author epr
 */
public interface FileSystem<T extends FSEntry> {

	public FileSystemType<? extends FileSystem<T>> getType();

    /**
     * Gets the device this FS driver operates on.
     */
    public Device getDevice();

    /**
     * Gets the root entry of this filesystem. This is usually a directory, but
     * this is not required.
     */
    public T getRootEntry() throws IOException;

    /**
     * Is the filesystem mounted in readonly mode ?
     */
    public boolean isReadOnly();

    /**
     * Close this filesystem. After a close, all invocations of method of this
     * filesystem or objects created by this filesystem will throw an
     * IOException.
     *
     * @throws IOException
     */
    public void close() throws IOException;

    /**
     * Is this filesystem closed.
     */
    public boolean isClosed();

	public long getTotalSpace() throws IOException;

	public long getFreeSpace() throws IOException;

	public long getUsableSpace() throws IOException;

	/**
	 * The most generic form of rename, really a move and (optional) rename.
	 * As such it has to be a FileSystem level operation.
	 *
	 * @param from The entry that is being renamed.
	 * @param to The entry (directory) that from is being moved to.
	 * @param newName a new name for the entry after it is moved, may be null to indicate no change.
	 * @throws IOException
	 */
	public void rename(T from, T to, String newName) throws IOException;
}
