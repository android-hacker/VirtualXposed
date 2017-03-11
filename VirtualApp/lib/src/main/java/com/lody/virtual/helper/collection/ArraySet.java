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

import android.util.Log;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * ArraySet is a generic set data structure that is designed to be more memory efficient than a
 * traditional {@link java.util.HashSet}.  The design is very similar to
 * {@link ArrayMap}, with all of the caveats described there.  This implementation is
 * separate from ArrayMap, however, so the Object array contains only one item for each
 * entry in the set (instead of a pair for a mapping).
 *
 * <p>Note that this implementation is not intended to be appropriate for data structures
 * that may contain large numbers of items.  It is generally slower than a traditional
 * HashSet, since lookups require a binary search and adds and removes require inserting
 * and deleting entries in the array.  For containers holding up to hundreds of items,
 * the performance difference is not significant, less than 50%.</p>
 *
 * <p>Because this container is intended to better balance memory use, unlike most other
 * standard Java containers it will shrink its array as items are removed from it.  Currently
 * you have no control over this shrinking -- if you set a capacity and then remove an
 * item, it may reduce the capacity to better match the current size.  In the future an
 * explicit call to set the capacity should turn off this aggressive shrinking behavior.</p>
 */
public final class ArraySet<E> implements Collection<E>, Set<E> {
    private static final boolean DEBUG = false;
    private static final String TAG = "ArraySet";

    /**
     * The minimum amount by which the capacity of a ArraySet will increase.
     * This is tuned to be relatively space-efficient.
     */
    private static final int BASE_SIZE = 4;

    /**
     * Maximum number of entries to have in array caches.
     */
    private static final int CACHE_SIZE = 10;

    /**
     * Caches of small array objects to avoid spamming garbage.  The cache
     * Object[] variable is a pointer to a linked list of array objects.
     * The first entry in the array is a pointer to the next array in the
     * list; the second entry is a pointer to the int[] hash code array for it.
     */
    static Object[] mBaseCache;
    static int mBaseCacheSize;
    static Object[] mTwiceBaseCache;
    static int mTwiceBaseCacheSize;

    int[] mHashes;
    Object[] mArray;
    int mSize;
    MapCollections<E, E> mCollections;

    /**
     * Create a new empty ArraySet.  The default capacity of an array map is 0, and
     * will grow once items are added to it.
     */
    public ArraySet() {
        mHashes = ContainerHelpers.EMPTY_INTS;
        mArray = ContainerHelpers.EMPTY_OBJECTS;
        mSize = 0;
    }

    /**
     * Create a new ArraySet with a given initial capacity.
     */
    public ArraySet(int capacity) {
        if (capacity == 0) {
            mHashes = ContainerHelpers.EMPTY_INTS;
            mArray = ContainerHelpers.EMPTY_OBJECTS;
        } else {
            allocArrays(capacity);
        }
        mSize = 0;
    }

    /**
     * Create a new ArraySet with the mappings from the given ArraySet.
     */
    public ArraySet(ArraySet<E> set) {
        this();
        if (set != null) {
            addAll(set);
        }
    }

    /** {@hide} */
    public ArraySet(Collection<E> set) {
        this();
        if (set != null) {
            addAll(set);
        }
    }

    private static void freeArrays(final int[] hashes, final Object[] array, final int size) {
        if (hashes.length == (BASE_SIZE*2)) {
            synchronized (ArraySet.class) {
                if (mTwiceBaseCacheSize < CACHE_SIZE) {
                    array[0] = mTwiceBaseCache;
                    array[1] = hashes;
                    for (int i=size-1; i>=2; i--) {
                        array[i] = null;
                    }
                    mTwiceBaseCache = array;
                    mTwiceBaseCacheSize++;
                    if (DEBUG) Log.d(TAG, "Storing 2x cache " + array
                            + " now have " + mTwiceBaseCacheSize + " entries");
                }
            }
        } else if (hashes.length == BASE_SIZE) {
            synchronized (ArraySet.class) {
                if (mBaseCacheSize < CACHE_SIZE) {
                    array[0] = mBaseCache;
                    array[1] = hashes;
                    for (int i=size-1; i>=2; i--) {
                        array[i] = null;
                    }
                    mBaseCache = array;
                    mBaseCacheSize++;
                    if (DEBUG) Log.d(TAG, "Storing 1x cache " + array
                            + " now have " + mBaseCacheSize + " entries");
                }
            }
        }
    }

    private int indexOf(Object key, int hash) {
        final int N = mSize;

        // Important fast case: if nothing is in here, nothing to look for.
        if (N == 0) {
            return ~0;
        }

        int index = ContainerHelpers.binarySearch(mHashes, N, hash);

        // If the hash code wasn't found, then we have no entry for this key.
        if (index < 0) {
            return index;
        }

        // If the key at the returned index matches, that's what we want.
        if (key.equals(mArray[index])) {
            return index;
        }

        // Search for a matching key after the index.
        int end;
        for (end = index + 1; end < N && mHashes[end] == hash; end++) {
            if (key.equals(mArray[end])) return end;
        }

        // Search for a matching key before the index.
        for (int i = index - 1; i >= 0 && mHashes[i] == hash; i--) {
            if (key.equals(mArray[i])) return i;
        }

        // Key not found -- return negative value indicating where a
        // new entry for this key should go.  We use the end of the
        // hash chain to reduce the number of array entries that will
        // need to be copied when inserting.
        return ~end;
    }

    private int indexOfNull() {
        final int N = mSize;

        // Important fast case: if nothing is in here, nothing to look for.
        if (N == 0) {
            return ~0;
        }

        int index = ContainerHelpers.binarySearch(mHashes, N, 0);

        // If the hash code wasn't found, then we have no entry for this key.
        if (index < 0) {
            return index;
        }

        // If the key at the returned index matches, that's what we want.
        if (null == mArray[index]) {
            return index;
        }

        // Search for a matching key after the index.
        int end;
        for (end = index + 1; end < N && mHashes[end] == 0; end++) {
            if (null == mArray[end]) return end;
        }

        // Search for a matching key before the index.
        for (int i = index - 1; i >= 0 && mHashes[i] == 0; i--) {
            if (null == mArray[i]) return i;
        }

        // Key not found -- return negative value indicating where a
        // new entry for this key should go.  We use the end of the
        // hash chain to reduce the number of array entries that will
        // need to be copied when inserting.
        return ~end;
    }

    private void allocArrays(final int size) {
        if (size == (BASE_SIZE*2)) {
            synchronized (ArraySet.class) {
                if (mTwiceBaseCache != null) {
                    final Object[] array = mTwiceBaseCache;
                    mArray = array;
                    mTwiceBaseCache = (Object[])array[0];
                    mHashes = (int[])array[1];
                    array[0] = array[1] = null;
                    mTwiceBaseCacheSize--;
                    if (DEBUG) Log.d(TAG, "Retrieving 2x cache " + mHashes
                            + " now have " + mTwiceBaseCacheSize + " entries");
                    return;
                }
            }
        } else if (size == BASE_SIZE) {
            synchronized (ArraySet.class) {
                if (mBaseCache != null) {
                    final Object[] array = mBaseCache;
                    mArray = array;
                    mBaseCache = (Object[])array[0];
                    mHashes = (int[])array[1];
                    array[0] = array[1] = null;
                    mBaseCacheSize--;
                    if (DEBUG) Log.d(TAG, "Retrieving 1x cache " + mHashes
                            + " now have " + mBaseCacheSize + " entries");
                    return;
                }
            }
        }

        mHashes = new int[size];
        mArray = new Object[size];
    }

    /**
     * Make the array map empty.  All storage is released.
     */
    @Override
    public void clear() {
        if (mSize != 0) {
            freeArrays(mHashes, mArray, mSize);
            mHashes = ContainerHelpers.EMPTY_INTS;
            mArray = ContainerHelpers.EMPTY_OBJECTS;
            mSize = 0;
        }
    }

    /**
     * Ensure the array map can hold at least <var>minimumCapacity</var>
     * items.
     */
    public void ensureCapacity(int minimumCapacity) {
        if (mHashes.length < minimumCapacity) {
            final int[] ohashes = mHashes;
            final Object[] oarray = mArray;
            allocArrays(minimumCapacity);
            if (mSize > 0) {
                System.arraycopy(ohashes, 0, mHashes, 0, mSize);
                System.arraycopy(oarray, 0, mArray, 0, mSize);
            }
            freeArrays(ohashes, oarray, mSize);
        }
    }

    /**
     * Check whether a value exists in the set.
     *
     * @param key The value to search for.
     * @return Returns true if the value exists, else false.
     */
    @Override
    public boolean contains(Object key) {
        return indexOf(key) >= 0;
    }

    /**
     * Returns the index of a value in the set.
     *
     * @param key The value to search for.
     * @return Returns the index of the value if it exists, else a negative integer.
     */
    public int indexOf(Object key) {
        return key == null ? indexOfNull() : indexOf(key, key.hashCode());
    }

    /**
     * Return the value at the given index in the array.
     * @param index The desired index, must be between 0 and {@link #size()}-1.
     * @return Returns the value stored at the given index.
     */
    public E valueAt(int index) {
        return (E)mArray[index];
    }

    /**
     * Return true if the array map contains no items.
     */
    @Override
    public boolean isEmpty() {
        return mSize <= 0;
    }

    /**
     * Adds the specified object to this set. The set is not modified if it
     * already contains the object.
     *
     * @param value the object to add.
     * @return {@code true} if this set is modified, {@code false} otherwise.
     * @throws ClassCastException
     *             when the class of the object is inappropriate for this set.
     */
    @Override
    public boolean add(E value) {
        final int hash;
        int index;
        if (value == null) {
            hash = 0;
            index = indexOfNull();
        } else {
            hash = value.hashCode();
            index = indexOf(value, hash);
        }
        if (index >= 0) {
            return false;
        }

        index = ~index;
        if (mSize >= mHashes.length) {
            final int n = mSize >= (BASE_SIZE*2) ? (mSize+(mSize>>1))
                    : (mSize >= BASE_SIZE ? (BASE_SIZE*2) : BASE_SIZE);

            if (DEBUG) Log.d(TAG, "add: grow from " + mHashes.length + " to " + n);

            final int[] ohashes = mHashes;
            final Object[] oarray = mArray;
            allocArrays(n);

            if (mHashes.length > 0) {
                if (DEBUG) Log.d(TAG, "add: copy 0-" + mSize + " to 0");
                System.arraycopy(ohashes, 0, mHashes, 0, ohashes.length);
                System.arraycopy(oarray, 0, mArray, 0, oarray.length);
            }

            freeArrays(ohashes, oarray, mSize);
        }

        if (index < mSize) {
            if (DEBUG) Log.d(TAG, "add: move " + index + "-" + (mSize-index)
                    + " to " + (index+1));
            System.arraycopy(mHashes, index, mHashes, index + 1, mSize - index);
            System.arraycopy(mArray, index, mArray, index + 1, mSize - index);
        }

        mHashes[index] = hash;
        mArray[index] = value;
        mSize++;
        return true;
    }

    /**
     * Perform a {@link #add(Object)} of all values in <var>array</var>
     * @param array The array whose contents are to be retrieved.
     */
    public void addAll(ArraySet<? extends E> array) {
        final int N = array.mSize;
        ensureCapacity(mSize + N);
        if (mSize == 0) {
            if (N > 0) {
                System.arraycopy(array.mHashes, 0, mHashes, 0, N);
                System.arraycopy(array.mArray, 0, mArray, 0, N);
                mSize = N;
            }
        } else {
            for (int i=0; i<N; i++) {
                add(array.valueAt(i));
            }
        }
    }

    /**
     * Removes the specified object from this set.
     *
     * @param object the object to remove.
     * @return {@code true} if this set was modified, {@code false} otherwise.
     */
    @Override
    public boolean remove(Object object) {
        final int index = indexOf(object);
        if (index >= 0) {
            removeAt(index);
            return true;
        }
        return false;
    }

    /**
     * Remove the key/value mapping at the given index.
     * @param index The desired index, must be between 0 and {@link #size()}-1.
     * @return Returns the value that was stored at this index.
     */
    public E removeAt(int index) {
        final Object old = mArray[index];
        if (mSize <= 1) {
            // Now empty.
            if (DEBUG) Log.d(TAG, "remove: shrink from " + mHashes.length + " to 0");
            freeArrays(mHashes, mArray, mSize);
            mHashes = ContainerHelpers.EMPTY_INTS;
            mArray = ContainerHelpers.EMPTY_OBJECTS;
            mSize = 0;
        } else {
            if (mHashes.length > (BASE_SIZE*2) && mSize < mHashes.length/3) {
                // Shrunk enough to reduce size of arrays.  We don't allow it to
                // shrink smaller than (BASE_SIZE*2) to avoid flapping between
                // that and BASE_SIZE.
                final int n = mSize > (BASE_SIZE*2) ? (mSize + (mSize>>1)) : (BASE_SIZE*2);

                if (DEBUG) Log.d(TAG, "remove: shrink from " + mHashes.length + " to " + n);

                final int[] ohashes = mHashes;
                final Object[] oarray = mArray;
                allocArrays(n);

                mSize--;
                if (index > 0) {
                    if (DEBUG) Log.d(TAG, "remove: copy from 0-" + index + " to 0");
                    System.arraycopy(ohashes, 0, mHashes, 0, index);
                    System.arraycopy(oarray, 0, mArray, 0, index);
                }
                if (index < mSize) {
                    if (DEBUG) Log.d(TAG, "remove: copy from " + (index+1) + "-" + mSize
                            + " to " + index);
                    System.arraycopy(ohashes, index + 1, mHashes, index, mSize - index);
                    System.arraycopy(oarray, index + 1, mArray, index, mSize - index);
                }
            } else {
                mSize--;
                if (index < mSize) {
                    if (DEBUG) Log.d(TAG, "remove: move " + (index+1) + "-" + mSize
                            + " to " + index);
                    System.arraycopy(mHashes, index + 1, mHashes, index, mSize - index);
                    System.arraycopy(mArray, index + 1, mArray, index, mSize - index);
                }
                mArray[mSize] = null;
            }
        }
        return (E)old;
    }

    /**
     * Perform a {@link #remove(Object)} of all values in <var>array</var>
     * @param array The array whose contents are to be removed.
     */
    public boolean removeAll(ArraySet<? extends E> array) {
        // TODO: If array is sufficiently large, a marking approach might be beneficial. In a first
        //       pass, use the property that the sets are sorted by hash to make this linear passes
        //       (except for hash collisions, which means worst case still n*m), then do one
        //       collection pass into a new array. This avoids binary searches and excessive memcpy.
        final int N = array.mSize;

        // Note: ArraySet does not make thread-safety guarantees. So instead of OR-ing together all
        //       the single results, compare size before and after.
        final int originalSize = mSize;
        for (int i = 0; i < N; i++) {
            remove(array.valueAt(i));
        }
        return originalSize != mSize;
    }

    /**
     * Return the number of items in this array map.
     */
    @Override
    public int size() {
        return mSize;
    }

    @Override
    public Object[] toArray() {
        Object[] result = new Object[mSize];
        System.arraycopy(mArray, 0, result, 0, mSize);
        return result;
    }

    @Override
    public <T> T[] toArray(T[] array) {
        if (array.length < mSize) {
            @SuppressWarnings("unchecked") T[] newArray
                = (T[]) Array.newInstance(array.getClass().getComponentType(), mSize);
            array = newArray;
        }
        System.arraycopy(mArray, 0, array, 0, mSize);
        if (array.length > mSize) {
            array[mSize] = null;
        }
        return array;
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation returns false if the object is not a set, or
     * if the sets have different sizes.  Otherwise, for each value in this
     * set, it checks to make sure the value also exists in the other set.
     * If any value doesn't exist, the method returns false; otherwise, it
     * returns true.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof Set) {
            Set<?> set = (Set<?>) object;
            if (size() != set.size()) {
                return false;
            }

            try {
                for (int i=0; i<mSize; i++) {
                    E mine = valueAt(i);
                    if (!set.contains(mine)) {
                        return false;
                    }
                }
            } catch (NullPointerException ignored) {
                return false;
            } catch (ClassCastException ignored) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int[] hashes = mHashes;
        int result = 0;
        for (int i = 0, s = mSize; i < s; i++) {
            result += hashes[i];
        }
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation composes a string by iterating over its values. If
     * this set contains itself as a value, the string "(this Set)"
     * will appear in its place.
     */
    @Override
    public String toString() {
        if (isEmpty()) {
            return "{}";
        }

        StringBuilder buffer = new StringBuilder(mSize * 14);
        buffer.append('{');
        for (int i=0; i<mSize; i++) {
            if (i > 0) {
                buffer.append(", ");
            }
            Object value = valueAt(i);
            if (value != this) {
                buffer.append(value);
            } else {
                buffer.append("(this Set)");
            }
        }
        buffer.append('}');
        return buffer.toString();
    }

    // ------------------------------------------------------------------------
    // Interop with traditional Java containers.  Not as efficient as using
    // specialized collection APIs.
    // ------------------------------------------------------------------------

    private MapCollections<E, E> getCollection() {
        if (mCollections == null) {
            mCollections = new MapCollections<E, E>() {
                @Override
                protected int colGetSize() {
                    return mSize;
                }

                @Override
                protected Object colGetEntry(int index, int offset) {
                    return mArray[index];
                }

                @Override
                protected int colIndexOfKey(Object key) {
                    return indexOf(key);
                }

                @Override
                protected int colIndexOfValue(Object value) {
                    return indexOf(value);
                }

                @Override
                protected Map<E, E> colGetMap() {
                    throw new UnsupportedOperationException("not a map");
                }

                @Override
                protected void colPut(E key, E value) {
                    add(key);
                }

                @Override
                protected E colSetValue(int index, E value) {
                    throw new UnsupportedOperationException("not a map");
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
     * Return an {@link java.util.Iterator} over all values in the set.
     *
     * <p><b>Note:</b> this is a fairly inefficient way to access the array contents, it
     * requires generating a number of temporary objects and allocates additional state
     * information associated with the container that will remain for the life of the container.</p>
     */
    @Override
    public Iterator<E> iterator() {
        return getCollection().getKeySet().iterator();
    }

    /**
     * Determine if the array set contains all of the values in the given collection.
     * @param collection The collection whose contents are to be checked against.
     * @return Returns true if this array set contains a value for every entry
     * in <var>collection</var>, else returns false.
     */
    @Override
    public boolean containsAll(Collection<?> collection) {
        Iterator<?> it = collection.iterator();
        while (it.hasNext()) {
            if (!contains(it.next())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Perform an {@link #add(Object)} of all values in <var>collection</var>
     * @param collection The collection whose contents are to be retrieved.
     */
    @Override
    public boolean addAll(Collection<? extends E> collection) {
        ensureCapacity(mSize + collection.size());
        boolean added = false;
        for (E value : collection) {
            added |= add(value);
        }
        return added;
    }

    /**
     * Remove all values in the array set that exist in the given collection.
     * @param collection The collection whose contents are to be used to remove values.
     * @return Returns true if any values were removed from the array set, else false.
     */
    @Override
    public boolean removeAll(Collection<?> collection) {
        boolean removed = false;
        for (Object value : collection) {
            removed |= remove(value);
        }
        return removed;
    }

    /**
     * Remove all values in the array set that do <b>not</b> exist in the given collection.
     * @param collection The collection whose contents are to be used to determine which
     * values to keep.
     * @return Returns true if any values were removed from the array set, else false.
     */
    @Override
    public boolean retainAll(Collection<?> collection) {
        boolean removed = false;
        for (int i=mSize-1; i>=0; i--) {
            if (!collection.contains(mArray[i])) {
                removeAt(i);
                removed = true;
            }
        }
        return removed;
    }
}
