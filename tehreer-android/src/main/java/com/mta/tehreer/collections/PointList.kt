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

import androidx.annotation.Size
import com.mta.tehreer.internal.Description
import com.mta.tehreer.internal.collections.JFloatArrayPointList

/**
 * Represents a primitive list of points.
 */
abstract class PointList : Primitive {
    /**
     * Returns the number of points in this list.
     *
     * @return The number of points in this list.
     */
    abstract fun size(): Int

    /**
     * Returns the x- coordinate of the point at the specified index in this list.
     *
     * @param index Index of the point whose x- coordinate is returned.
     * @return The x- coordinate of the point at the specified index in this list.
     *
     * @throws IndexOutOfBoundsException if the index is out of range (`index < 0 || index
     *         >= size()`).
     */
    abstract fun getX(index: Int): Float

    /**
     * Returns the y- coordinate of the point at the specified index in this list.
     *
     * @param index Index of the point whose y- coordinate is returned.
     * @return The y- coordinate of the point at the specified index in this list.
     *
     * @throws IndexOutOfBoundsException if the index is out of range (`index < 0 || index
     *         >= size()`).
     */
    abstract fun getY(index: Int): Float

    /**
     * Copies all of the points in this list to an array, starting at the specified index of the
     * target array. Each x- coordinate will be followed by y- coordinate of the point in the target
     * array.
     *
     * @param array The array into which the points of this list are to be copied.
     * @param atIndex The index in the target array at which copying begins.
     *
     * @throws NullPointerException if `array` is null.
     * @throws IndexOutOfBoundsException for an illegal endpoint index value (`atIndex < 0
     *         || (array.length - atIndex) < size() * 2`).
     */
    abstract fun copyTo(array: FloatArray, atIndex: Int)

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
    abstract fun subList(fromIndex: Int, toIndex: Int): PointList

    /**
     * Returns a new array containing all of the elements in this list in proper sequence (from
     * first to last element). Even numbered array entries will be the x- coordinates while odd
     * numbered array entries will be the y- coordinates.
     *
     * @return A new array containing all of the elements in this list in proper sequence.
     */
    @Size(multiple = 2)
    fun toArray(): FloatArray {
        val array = FloatArray(size() * 2)
        copyTo(array, 0)

        return array
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other !is PointList) {
            return false
        }

        val size = other.size()
        if (size() != size) {
            return false
        }

        for (i in 0 until size) {
            if (getX(i) != other.getX(i) || getY(i) != other.getY(i)) {
                return false
            }
        }

        return true
    }

    override fun hashCode(): Int {
        var result = 1

        for (i in 0 until size()) {
            result = 31 * result + getX(i).toBits()
            result = 31 * result + getY(i).toBits()
        }

        return result
    }

    override fun toString(): String {
        return Description.forPointList(this)
    }

    companion object {
        /**
         * Returns a point list whose elements are the specified values. Even numbered entries will
         * become the x- coordinates while odd numbered entries will become the y- coordinates.
         *
         * @param values The elements of the point list.
         * @return A new point list.
             */
        @JvmStatic
        fun of(@Size(multiple = 2) vararg values: Float): PointList {
            return JFloatArrayPointList(values, 0, values.size / 2)
        }
    }
}
