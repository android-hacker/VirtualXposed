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

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Helper for writing standard Java collection interfaces to a data
 * structure like {@link ArrayMap}.
 * @hide
 */
abstract class MapCollections<K, V> {
    EntrySet mEntrySet;
    KeySet mKeySet;
    ValuesCollection mValues;

    public static <K, V> boolean containsAllHelper(Map<K, V> map, Collection<?> collection) {
        Iterator<?> it = collection.iterator();
        while (it.hasNext()) {
            if (!map.containsKey(it.next())) {
                return false;
            }
        }
        return true;
    }

    public static <K, V> boolean removeAllHelper(Map<K, V> map, Collection<?> collection) {
        int oldSize = map.size();
        Iterator<?> it = collection.iterator();
        while (it.hasNext()) {
            map.remove(it.next());
        }
        return oldSize != map.size();
    }

        public static <K, V> boolean retainAllHelper(Map<K, V> map, Collection<?> collection) {
        int oldSize = map.size();
        Iterator<K> it = map.keySet().iterator();
        while (it.hasNext()) {
            if (!collection.contains(it.next())) {
                it.remove();
            }
        }
        return oldSize != map.size();
    };

        public static <T> boolean equalsSetHelper(Set<T> set, Object object) {
        if (set == object) {
            return true;
        }
        if (object instanceof Set) {
            Set<?> s = (Set<?>) object;

            try {
                return set.size() == s.size() && set.containsAll(s);
            } catch (NullPointerException ignored) {
                return false;
            } catch (ClassCastException ignored) {
                return false;
            }
        }
        return false;
    };

        public Object[] toArrayHelper(int offset) {
        final int N = colGetSize();
        Object[] result = new Object[N];
        for (int i=0; i<N; i++) {
            result[i] = colGetEntry(i, offset);
        }
        return result;
    };

    public <T> T[] toArrayHelper(T[] array, int offset) {
        final int N  = colGetSize();
        if (array.length < N) {
            @SuppressWarnings("unchecked") T[] newArray
                = (T[]) Array.newInstance(array.getClass().getComponentType(), N);
            array = newArray;
        }
        for (int i=0; i<N; i++) {
            array[i] = (T)colGetEntry(i, offset);
        }
        if (array.length > N) {
            array[N] = null;
        }
        return array;
    }

    public Set<Map.Entry<K, V>> getEntrySet() {
        if (mEntrySet == null) {
            mEntrySet = new EntrySet();
        }
        return mEntrySet;
    }

    public Set<K> getKeySet() {
        if (mKeySet == null) {
            mKeySet = new KeySet();
        }
        return mKeySet;
    }

    public Collection<V> getValues() {
        if (mValues == null) {
            mValues = new ValuesCollection();
        }
        return mValues;
    }

    protected abstract int colGetSize();

    protected abstract Object colGetEntry(int index, int offset);

    protected abstract int colIndexOfKey(Object key);

    protected abstract int colIndexOfValue(Object key);

    protected abstract Map<K, V> colGetMap();

    protected abstract void colPut(K key, V value);

    protected abstract V colSetValue(int index, V value);

    protected abstract void colRemoveAt(int index);

    protected abstract void colClear();

    final class ArrayIterator<T> implements Iterator<T> {
        final int mOffset;
        int mSize;
        int mIndex;
        boolean mCanRemove = false;

        ArrayIterator(int offset) {
            mOffset = offset;
            mSize = colGetSize();
        }

        @Override
        public boolean hasNext() {
            return mIndex < mSize;
        }

        @Override
        public T next() {
            Object res = colGetEntry(mIndex, mOffset);
            mIndex++;
            mCanRemove = true;
            return (T)res;
        }

        @Override
        public void remove() {
            if (!mCanRemove) {
                throw new IllegalStateException();
            }
            mIndex--;
            mSize--;
            mCanRemove = false;
            colRemoveAt(mIndex);
        }
    }

    final class MapIterator implements Iterator<Map.Entry<K, V>>, Map.Entry<K, V> {
        int mEnd;
        int mIndex;
        boolean mEntryValid = false;

        MapIterator() {
            mEnd = colGetSize() - 1;
            mIndex = -1;
        }

        @Override
        public boolean hasNext() {
            return mIndex < mEnd;
        }

        @Override
        public Map.Entry<K, V> next() {
            mIndex++;
            mEntryValid = true;
            return this;
        }

        @Override
        public void remove() {
            if (!mEntryValid) {
                throw new IllegalStateException();
            }
            colRemoveAt(mIndex);
            mIndex--;
            mEnd--;
            mEntryValid = false;
        }

        @Override
        public K getKey() {
            if (!mEntryValid) {
                throw new IllegalStateException(
                        "This container does not support retaining Map.Entry objects");
            }
            return (K)colGetEntry(mIndex, 0);
        }

        @Override
        public V getValue() {
            if (!mEntryValid) {
                throw new IllegalStateException(
                        "This container does not support retaining Map.Entry objects");
            }
            return (V)colGetEntry(mIndex, 1);
        }

        @Override
        public V setValue(V object) {
            if (!mEntryValid) {
                throw new IllegalStateException(
                        "This container does not support retaining Map.Entry objects");
            }
            return colSetValue(mIndex, object);
        }

        @Override
        public final boolean equals(Object o) {
            if (!mEntryValid) {
                throw new IllegalStateException(
                        "This container does not support retaining Map.Entry objects");
            }
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            return ContainerHelpers.equal(e.getKey(), colGetEntry(mIndex, 0))
                    && ContainerHelpers.equal(e.getValue(), colGetEntry(mIndex, 1));
        }

        @Override
        public final int hashCode() {
            if (!mEntryValid) {
                throw new IllegalStateException(
                        "This container does not support retaining Map.Entry objects");
            }
            final Object key = colGetEntry(mIndex, 0);
            final Object value = colGetEntry(mIndex, 1);
            return (key == null ? 0 : key.hashCode()) ^
                    (value == null ? 0 : value.hashCode());
        }

        @Override
        public final String toString() {
            return getKey() + "=" + getValue();
        }
    }

final class EntrySet implements Set<Map.Entry<K, V>> {
        @Override
        public boolean add(Map.Entry<K, V> object) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(Collection<? extends Map.Entry<K, V>> collection) {
            int oldSize = colGetSize();
            for (Map.Entry<K, V> entry : collection) {
                colPut(entry.getKey(), entry.getValue());
            }
            return oldSize != colGetSize();
        }

        @Override
        public void clear() {
            colClear();
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            int index = colIndexOfKey(e.getKey());
            if (index < 0) {
                return false;
            }
            Object foundVal = colGetEntry(index, 1);
            return ContainerHelpers.equal(foundVal, e.getValue());
        }

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

        @Override
        public boolean isEmpty() {
            return colGetSize() == 0;
        }

        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return new MapIterator();
        }

        @Override
        public boolean remove(Object object) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(Collection<?> collection) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(Collection<?> collection) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int size() {
            return colGetSize();
        }

        @Override
        public Object[] toArray() {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T[] toArray(T[] array) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean equals(Object object) {
            return equalsSetHelper(this, object);
        }

        @Override
        public int hashCode() {
            int result = 0;
            for (int i=colGetSize()-1; i>=0; i--) {
                final Object key = colGetEntry(i, 0);
                final Object value = colGetEntry(i, 1);
                result += ( (key == null ? 0 : key.hashCode()) ^
                        (value == null ? 0 : value.hashCode()) );
            }
            return result;
        }
    }

final class KeySet implements Set<K> {

        @Override
        public boolean add(K object) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(Collection<? extends K> collection) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            colClear();
        }

        @Override
        public boolean contains(Object object) {
            return colIndexOfKey(object) >= 0;
        }

        @Override
        public boolean containsAll(Collection<?> collection) {
            return containsAllHelper(colGetMap(), collection);
        }

        @Override
        public boolean isEmpty() {
            return colGetSize() == 0;
        }

        @Override
        public Iterator<K> iterator() {
            return new ArrayIterator<K>(0);
        }

        @Override
        public boolean remove(Object object) {
            int index = colIndexOfKey(object);
            if (index >= 0) {
                colRemoveAt(index);
                return true;
            }
            return false;
        }

        @Override
        public boolean removeAll(Collection<?> collection) {
            return removeAllHelper(colGetMap(), collection);
        }

        @Override
        public boolean retainAll(Collection<?> collection) {
            return retainAllHelper(colGetMap(), collection);
        }

        @Override
        public int size() {
            return colGetSize();
        }

        @Override
        public Object[] toArray() {
            return toArrayHelper(0);
        }

        @Override
        public <T> T[] toArray(T[] array) {
            return toArrayHelper(array, 0);
        }

        @Override
        public boolean equals(Object object) {
            return equalsSetHelper(this, object);
        }

        @Override
        public int hashCode() {
            int result = 0;
            for (int i=colGetSize()-1; i>=0; i--) {
                Object obj = colGetEntry(i, 0);
                result += obj == null ? 0 : obj.hashCode();
            }
            return result;
        }
    }

final class ValuesCollection implements Collection<V> {

        @Override
        public boolean add(V object) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(Collection<? extends V> collection) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            colClear();
        }

        @Override
        public boolean contains(Object object) {
            return colIndexOfValue(object) >= 0;
        }

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

        @Override
        public boolean isEmpty() {
            return colGetSize() == 0;
        }

        @Override
        public Iterator<V> iterator() {
            return new ArrayIterator<V>(1);
        }

        @Override
        public boolean remove(Object object) {
            int index = colIndexOfValue(object);
            if (index >= 0) {
                colRemoveAt(index);
                return true;
            }
            return false;
        }

        @Override
        public boolean removeAll(Collection<?> collection) {
            int N = colGetSize();
            boolean changed = false;
            for (int i=0; i<N; i++) {
                Object cur = colGetEntry(i, 1);
                if (collection.contains(cur)) {
                    colRemoveAt(i);
                    i--;
                    N--;
                    changed = true;
                }
            }
            return changed;
        }

        @Override
        public boolean retainAll(Collection<?> collection) {
            int N = colGetSize();
            boolean changed = false;
            for (int i=0; i<N; i++) {
                Object cur = colGetEntry(i, 1);
                if (!collection.contains(cur)) {
                    colRemoveAt(i);
                    i--;
                    N--;
                    changed = true;
                }
            }
            return changed;
        }

        @Override
        public int size() {
            return colGetSize();
        }

        @Override
        public Object[] toArray() {
            return toArrayHelper(1);
        }

        @Override
        public <T> T[] toArray(T[] array) {
            return toArrayHelper(array, 1);
        }
    }
}
