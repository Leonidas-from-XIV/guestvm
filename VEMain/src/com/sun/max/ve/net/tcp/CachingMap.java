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
package com.sun.max.ve.net.tcp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author Puneeet Lakhina
 *
 */
public class CachingMap<K, V> extends ConcurrentHashMap<K, V> {

    /**
     *
     */
    private static final long serialVersionUID = -51271534294813025L;
    private int _cacheSize;
    private List<Entry<K, V>> _cache;

    public CachingMap(int cacheSize) {
        this._cacheSize = cacheSize;
        this._cache = new ArrayList<Entry<K, V>>();
    }

    static class Entry<K, V> implements Map.Entry<K, V> {

        private K _key;
        private V _value;

        public Entry(K key, V value) {
            this._key = key;
            this._value = value;
        }

        /*
         * (non-Javadoc)
         *
         * @see java.util.Map.Entry#getKey()
         */
        @Override
        public K getKey() {
            return _key;
        }

        /*
         * (non-Javadoc)
         *
         * @see java.util.Map.Entry#getValue()
         */
        @Override
        public V getValue() {
            return _value;
        }

        /*
         * (non-Javadoc)
         *
         * @see java.util.Map.Entry#setValue(java.lang.Object)
         */
        @Override
        public V setValue(V value) {
            this._value = value;
            return this._value;
        }

    }



    /*
     * (non-Javadoc)
     *
     * @see java.util.HashMap#get(java.lang.Object)
     */
    @Override
    public V get(Object key) {
        for (Map.Entry<K, V> entry : _cache) {
            if (entry.getKey().equals(key)) {
                return entry.getValue();
            }
        }
        final V val = super.get(key);
        addToCache((K) key, val);
        return val;
    }

    private void addToCache(K key, V value) {
        if(value == null) {
            return;
        }
        final Entry<K, V> entry = new Entry<K, V>(key, value);
        if (_cache.size() == _cacheSize) {
            // evict first element
            _cache.set(0, entry);
        } else {
            _cache.add(entry);
        }
    }


}
