/*
 * Copyright (C) 2016-2021 Muhammad Tayyab Akram
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

package com.mta.tehreer.internal.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;

public abstract class LruCache<K> {
    //
    // HashMap:
    //  - 1 pointer for map entry
    //  - 3 pointers for key, value and next
    //  - 1 integer for hash code
    //
    // Node:
    //  - 5 pointers for segment, key, value, previous and next
    //
    // Total:
    //  - 9 pointers
    //  - 1 integer
    //
    // Size: (9 * 4) + (1 * 4) = 40
    //
    protected static final int NODE_OVERHEAD = 40;

    private static class Node<K> {
        public final Segment<K> segment;
        public final K key;
        public Object value;
        public Node<K> previous;
        public Node<K> next;

        public Node(Segment<K> segment, K key, Object value) {
            this.segment = segment;
            this.key = key;
            this.value = value;
        }
    }

    private static class List<K> {
        final @NonNull Node<K> header = new Node<>(null, null, null);

        public List() {
            header.previous = header.next = header;
        }

        public Node<K> last() {
            return header.previous;
        }

        public void makeFirst(@NonNull Node<K> node) {
            remove(node);
            addFirst(node);
        }

        public void addFirst(@NonNull Node<K> node) {
            node.previous = header;
            node.next = header.next;
            header.next.previous = node;
            header.next = node;
        }

        public void remove(@NonNull Node<K> node) {
            node.previous.next = node.next;
            node.next.previous = node.previous;
            node.next = node.previous = null;
        }

        public void clear() {
            header.previous = header.next = header;
        }
    }

    protected static class Segment<K> {
        protected final @NonNull LruCache<K> cache;
        private final @NonNull HashMap<K, Node<K>> map = new HashMap<>();

        public Segment(@NonNull LruCache<K> cache) {
            this.cache = cache;
        }

        protected int sizeOf(@NonNull K key, @NonNull Object value) {
            return 1;
        }

        public final @Nullable Object get(@NonNull K key) {
            synchronized (cache) {
                Node<K> node = map.get(key);
                if (node != null) {
                    cache.list.makeFirst(node);
                    return node.value;
                }
            }

            return null;
        }

        public final void put(@NonNull K key, @NonNull Object value) {
            synchronized (cache) {
                Node<K> newNode = new Node<>(this, key, value);
                Node<K> oldNode = map.put(key, newNode);
                if (oldNode != null) {
                    throw new IllegalArgumentException("An entry with same key has already been added");
                }

                cache.size += sizeOf(key, value);
                cache.list.addFirst(newNode);
            }

            cache.trimToSize(cache.capacity);
        }

        public final void remove(@NonNull K key) {
            synchronized (cache) {
                Node<K> node = map.remove(key);
                if (node != null) {
                    cache.size -= sizeOf(key, node.value);
                    cache.list.remove(node);
                }
            }
        }
    }

    private final @NonNull List<K> list;
    private int capacity;
    private int size;

    public LruCache(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Invalid Capacity: " + capacity);
        }

        this.list = new List<>();
        this.capacity = capacity;
        this.size = 0;
    }

    public synchronized final int capacity() {
        return capacity;
    }

    public synchronized final int size() {
        return size;
    }

    public synchronized void clear() {
        list.clear();
    }

    public void trimToSize(int maxSize) {
        while (true) {
            synchronized (this) {
                if (size <= maxSize) {
                    break;
                }

                Node<K> toEvict = list.last();
                if (toEvict == list.header) {
                    break;
                }

                Segment<K> segment = toEvict.segment;
                K key = toEvict.key;
                segment.remove(key);
            }
        }
    }
}
