/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lody.virtual.helper.collection;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * ArrayMap is a generic key->value mapping data structure that is
 * designed to be more memory efficient than a traditional {@link java.util.HashMap},
 * this implementation is a version of the platform's
 * {@link android.util.ArrayMap} that can be used on older versions of the platform.
 * It keeps its mappings in an array data structure -- an integer array of hash
 * codes for each item, and an Object array of the key/value pairs.  This allows it to
 * avoid having to create an extra object for every entry put in to the map, and it
 * also tries to control the growth of the size of these arrays more aggressively
 * (since growing them only requires copying the entries in the array, not rebuilding
 * a hash map).
 *
 * <p>If you don't need the standard Java container APIs provided here (iterators etc),
 * consider using {@link SimpleArrayMap} instead.</p>
 *
 * <p>Note that this implementation is not intended to be appropriate for data structures
 * that may contain large numbers of items.  It is generally slower than a traditional
 * HashMap, since lookups require a binary search and adds and removes require inserting
 * and deleting entries in the array.  For containers holding up to hundreds of items,
 * the performance difference is not significant, less than 50%.</p>
 *
 * <p>Because this container is intended to better balance memory use, unlike most other
 * standard Java containers it will shrink its array as items are removed from it.  Currently
 * you have no control over this shrinking -- if you set a capacity and then remove an
 * item, it may reduce the capacity to better match the current size.  In the future an
 * explicit call to set the capacity should turn off this aggressive shrinking behavior.</p>
 */
public class ArrayMap<K, V> extends SimpleArrayMap<K, V> implements Map<K, V> {
    MapCollections<K, V> mCollections;

    public ArrayMap() {
        super();
    }

    /**
     * Create a new ArrayMap with a given initial capacity.
     */
    public ArrayMap(int capacity) {
        super(capacity);
    }

    /**
     * Create a new ArrayMap with the mappings from the given ArrayMap.
     */
    public ArrayMap(SimpleArrayMap map) {
        super(map);
    }

    private MapCollections<K, V> getCollection() {
        if (mCollections == null) {
            mCollections = new MapCollections<K, V>() {
                @Override
                protected int colGetSize() {
                    return mSize;
                }

                @Override
                protected Object colGetEntry(int index, int offset) {
                    return mArray[(index<<1) + offset];
                }

                @Override
                protected int colIndexOfKey(Object key) {
                    return indexOfKey(key);
                }

                @Override
                protected int colIndexOfValue(Object value) {
                    return indexOfValue(value);
                }

                @Override
                protected Map<K, V> colGetMap() {
                    return ArrayMap.this;
                }

                @Override
                protected void colPut(K key, V value) {
                    put(key, value);
                }

                @Override
                protected V colSetValue(int index, V value) {
                    return setValueAt(index, value);
                }

                @Override
                protected void colRemoveAt(int index) {
                    removeAt(index);
                }

                @Override
                protected void colClear() {
                    clear();
                }
            };
        }
        return mCollections;
    }

    /**
     * Determine if the array map contains all of the keys in the given collection.
     * @param collection The collection whose contents are to be checked against.
     * @return Returns true if this array map contains a key for every entry
     * in <var>collection</var>, else returns false.
     */
    public boolean containsAll(Collection<?> collection) {
        return MapCollections.containsAllHelper(this, collection);
    }

    /**
     * Perform a {@link #put(Object, Object)} of all key/value pairs in <var>map</var>
     * @param map The map whose contents are to be retrieved.
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        ensureCapacity(mSize + map.size());
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Remove all keys in the array map that exist in the given collection.
     * @param collection The collection whose contents are to be used to remove keys.
     * @return Returns true if any keys were removed from the array map, else false.
     */
    public boolean removeAll(Collection<?> collection) {
        return MapCollections.removeAllHelper(this, collection);
    }

    /**
     * Remove all keys in the array map that do <b>not</b> exist in the given collection.
     * @param collection The collection whose contents are to be used to determine which
     * keys to keep.
     * @return Returns true if any keys were removed from the array map, else false.
     */
    public boolean retainAll(Collection<?> collection) {
        return MapCollections.retainAllHelper(this, collection);
    }

    /**
     * Return a {@link java.util.Set} for iterating over and interacting with all mappings
     * in the array map.
     *
     * <p><b>Note:</b> this is a very inefficient way to access the array contents, it
     * requires generating a number of temporary objects.</p>
     *
     * <p><b>Note:</b></p> the semantics of this
     * Set are subtly different than that of a {@link java.util.HashMap}: most important,
     * the {@link java.util.Map.Entry Map.Entry} object returned by its iterator is a single
     * object that exists for the entire iterator, so you can <b>not</b> hold on to it
     * after calling {@link java.util.Iterator#next() Iterator.next}.</p>
     */
    @Override
    public Set<Entry<K, V>> entrySet() {
        return getCollection().getEntrySet();
    }

    /**
     * Return a {@link java.util.Set} for iterating over and interacting with all keys
     * in the array map.
     *
     * <p><b>Note:</b> this is a fairly inefficient way to access the array contents, it
     * requires generating a number of temporary objects.</p>
     */
    @Override
    public Set<K> keySet() {
        return getCollection().getKeySet();
    }

    /**
     * Return a {@link java.util.Collection} for iterating over and interacting with all values
     * in the array map.
     *
     * <p><b>Note:</b> this is a fairly inefficient way to access the array contents, it
     * requires generating a number of temporary objects.</p>
     */
    @Override
    public Collection<V> values() {
        return getCollection().getValues();
    }
}
