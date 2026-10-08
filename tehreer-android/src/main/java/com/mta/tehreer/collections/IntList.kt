/*
 * Copyright (C) 2017-2026 Muhammad Tayyab Akram
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

package com.mta.tehreer.collections

import com.mta.tehreer.internal.Description
import com.mta.tehreer.internal.collections.JIntArrayList

/**
 * Represents a primitive list of integers.
 */
abstract class IntList : Primitive {
    /**
     * Returns the number of integers in this list.
     *
     * @return The number of integers in this list.
     */
    abstract fun size(): Int

    /**
     * Returns the integer at the specified index in this list.
     *
     * @param index Index of the integer to return.
     * @return The integer at the specified index in this list.
     *
     * @throws IndexOutOfBoundsException if the index is out of range (`index < 0 || index
     *         >= size()`).
     */
    abstract operator fun get(index: Int): Int

    /**
     * Copies all of the integers in this list to an array, starting at the specified index of the
     * target array.
     *
     * @param array The array into which the integers of this list are to be copied.
     * @param atIndex The index in the target array at which copying begins.
     *
     * @throws NullPointerException if `array` is null.
     * @throws IndexOutOfBoundsException for an illegal endpoint index value (`atIndex < 0
     *         || (array.length - atIndex) < size()`).
     */
    abstract fun copyTo(array: IntArray, atIndex: Int)

    /**
     * Returns a view of the portion of this list between the specified `fromIndex`,
     * inclusive, and `toIndex`, exclusive.
     *
     * @param fromIndex Low endpoint (inclusive) of the sub list.
     * @param toIndex High endpoint (exclusive) of the sub list.
     * @return A view of the specified range within this list.
     *
     * @throws IndexOutOfBoundsException for an illegal endpoint index value (`fromIndex < 0
     *         || toIndex > size() || fromIndex > toIndex`).
     */
    abstract fun subList(fromIndex: Int, toIndex: Int): IntList

    /**
     * Returns a new array containing all of the integers in this list in proper sequence (from
     * first to last element).
     *
     * @return A new array containing all of the integers in this list in proper sequence.
     */
    fun toArray(): IntArray {
        val array = IntArray(size())
        copyTo(array, 0)

        return array
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other !is IntList) {
            return false
        }

        val size = other.size()
        if (size() != size) {
            return false
        }

        for (i in 0 until size) {
            if (get(i) != other.get(i)) {
                return false
            }
        }

        return true
    }

    override fun hashCode(): Int {
        var result = 1

        for (i in 0 until size()) {
            result = 31 * result + get(i)
        }

        return result
    }

    override fun toString(): String {
        return Description.forIntList(this)
    }

    companion object {
        /**
         * Returns an integers list whose elements are the specified values.
         *
         * @param values The elements of the integer list.
         * @return A new integer list.
             */
        @JvmStatic
        fun of(vararg values: Int): IntList {
            return JIntArrayList(values, 0, values.size)
        }
    }
}
