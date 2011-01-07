/*
 * Copyright (c) 2009, 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
/*
 * $Id: BlockCache.java 4975 2009-02-02 08:30:52Z lsantha $
 *
 * Copyright (C) 2003-2009 JNode.org
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

package org.jnode.fs.ext2.cache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import java.util.logging.Level;

import com.sun.max.ve.logging.*;

/**
 * @author Andras Nagy
 */
public final class BlockCache extends LinkedHashMap<Object, Block> {
    // at most MAX_SIZE blocks fit in the cache
    static final int MAX_SIZE = 10;

    private static final Logger log = Logger.getLogger(BlockCache.class.getName());

    private ArrayList<CacheListener> cacheListeners;

    public BlockCache(int initialCapacity, float loadFactor) {
        super(Math.min(MAX_SIZE, initialCapacity), loadFactor, true);
        cacheListeners = new ArrayList<CacheListener>();
    }

    public void addCacheListener(CacheListener listener) {
        cacheListeners.add(listener);
    }

    /*
     * private boolean containsKey(Integer key) { boolean result =
     * super.containsKey(key); if(result) log.log(Level.FINEST, "CACHE HIT, size:"+size());
     * else log.log(Level.FINEST, "CACHE MISS"); return result; }
     */

    protected synchronized boolean removeEldestEntry(Map.Entry<Object, Block> eldest) {
        if (log.isLoggable(Level.FINEST)) {
            log.log(Level.FINEST, "BlockCache size: " + size());
        }
        if (size() > MAX_SIZE) {
            try {
                eldest.getValue().flush();
                // notify the listeners
                final CacheEvent event = new CacheEvent(eldest.getValue(),
                        CacheEvent.REMOVED);
                for (CacheListener l : cacheListeners) {
                    l.elementRemoved(event);
                }
            } catch (IOException e) {
                log.log(Level.SEVERE, "Exception when flushing a block from the cache", e);
            }
            return true;
        } else {
            return false;
        }
    }
}
