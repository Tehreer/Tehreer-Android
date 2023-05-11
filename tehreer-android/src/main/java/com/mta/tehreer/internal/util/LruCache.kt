/*
 * Copyright (C) 2023 Muhammad Tayyab Akram
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

package com.mta.tehreer.internal.util

import java.util.HashMap
import kotlin.jvm.Synchronized

internal abstract class LruCache<K>(capacity: Int) {
    private class Node<K>(
        val segment: Segment<K>?,
        val key: K?,
        var value: Any?
    ) {
        var previous: Node<K>? = null
        var next: Node<K>? = null
    }

    private class List<K> {
        val header = Node<K>(null, null, null)

        init {
            header.next = header
            header.previous = header.next
        }

        fun last(): Node<K> {
            return header.previous!!
        }

        fun makeFirst(node: Node<K>) {
            remove(node)
            addFirst(node)
        }

        fun addFirst(node: Node<K>) {
            node.previous = header
            node.next = header.next
            header.next!!.previous = node
            header.next = node
        }

        fun remove(node: Node<K>) {
            node.previous!!.next = node.next
            node.next!!.previous = node.previous
            node.previous = null
            node.next = null
        }

        fun clear() {
            header.next = header
            header.previous = header.next
        }
    }

    protected open class Segment<K>(
        protected val cache: LruCache<K>
    ) {
        private val map = HashMap<K, Node<K>>()

        protected open fun sizeOf(key: K, value: Any?): Int {
            return 1
        }

        operator fun get(key: K): Any? {
            synchronized(cache) {
                val node = map[key]
                if (node != null) {
                    cache.list.makeFirst(node)
                    return node.value
                }
            }

            return null
        }

        fun put(key: K, value: Any) {
            synchronized(cache) {
                val newNode = Node(this, key, value)
                val oldNode = map.put(key, newNode)
                require(oldNode == null) { "An entry with same key has already been added" }

                cache.size += sizeOf(key, value)
                cache.list.addFirst(newNode)
            }

            cache.trimToSize(cache.capacity)
        }

        fun remove(key: K) {
            synchronized(cache) {
                val node = map.remove(key)
                if (node != null) {
                    cache.size -= sizeOf(key, node.value)
                    cache.list.remove(node)
                }
            }
        }
    }

    private val list: List<K>
    private val capacity: Int
    private var size: Int

    init {
        require(capacity > 0) { "Invalid Capacity: $capacity" }

        this.list = List()
        this.capacity = capacity
        this.size = 0
    }

    @Synchronized
    fun capacity(): Int {
        return capacity
    }

    @Synchronized
    fun size(): Int {
        return size
    }

    @Synchronized
    open fun clear() {
        list.clear()
    }

    fun trimToSize(maxSize: Int) {
        while (true) {
            synchronized(this) {
                if (size <= maxSize) {
                    return
                }

                val toEvict: Node<K> = list.last()
                if (toEvict === list.header) {
                    return
                }

                val segment = toEvict.segment!!
                val key = toEvict.key!!
                segment.remove(key)
            }
        }
    }

    companion object {
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
        protected const val NODE_OVERHEAD = 40
    }
}
