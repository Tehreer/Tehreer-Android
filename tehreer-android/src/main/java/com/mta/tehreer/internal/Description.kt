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

package com.mta.tehreer.internal

import com.mta.tehreer.collections.ByteList
import com.mta.tehreer.internal.collections.JByteArrayList
import com.mta.tehreer.collections.IntList
import com.mta.tehreer.collections.FloatList
import com.mta.tehreer.collections.PointList
import java.lang.StringBuilder

internal class Description private constructor() {
    private val builder = StringBuilder()
    private var begun = false

    private fun begin() {
        builder.append("[")
        begun = true
    }

    private fun append(value: String) {
        if (!begun) {
            builder.append(", ")
        }
        builder.append(value)
        begun = false
    }

    private fun end() {
        builder.append("]")
    }

    override fun toString(): String {
        return builder.toString()
    }

    companion object {
        private const val NULL = "null"

        @JvmStatic
        fun forObject(`object`: Any?): String {
            return `object`?.toString() ?: NULL
        }

        @JvmStatic
        fun forByteArray(array: ByteArray): String {
            return forByteList(JByteArrayList(array, 0, array.size))
        }

        @JvmStatic
        fun forByteList(list: ByteList?): String {
            if (list == null) {
                return NULL
            }

            val description = Description()
            description.begin()
            for (i in 0 until list.size()) {
                description.append(list[i].toString())
            }
            description.end()

            return description.toString()
        }

        @JvmStatic
        fun forIntList(list: IntList?): String {
            if (list == null) {
                return NULL
            }

            val description = Description()
            description.begin()
            for (i in 0 until list.size()) {
                description.append(list[i].toString())
            }
            description.end()

            return description.toString()
        }

        @JvmStatic
        fun forFloatList(list: FloatList?): String {
            if (list == null) {
                return NULL
            }

            val description = Description()
            description.begin()
            for (i in 0 until list.size()) {
                description.append(list[i].toString())
            }
            description.end()

            return description.toString()
        }

        @JvmStatic
        fun forPointList(list: PointList?): String {
            if (list == null) {
                return NULL
            }

            val description = Description()
            description.begin()
            for (i in 0 until list.size()) {
                description.append(list.getX(i).toString())
                description.append(list.getY(i).toString())
            }
            description.end()

            return description.toString()
        }

        @JvmStatic
        fun <T> forIterator(iterator: Iterator<T>?): String {
            if (iterator == null) {
                return NULL
            }

            val description = Description()
            description.begin()
            for (element in iterator) {
                description.append(element.toString())
            }
            description.end()

            return description.toString()
        }

        @JvmStatic
        fun <T> forIterable(iterable: Iterable<T>): String {
            return forIterator(iterable.iterator())
        }
    }
}
