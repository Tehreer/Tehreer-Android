/*
 * Copyright (C) 2026 Muhammad Tayyab Akram
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

package com.mta.tehreer.util

import com.mta.tehreer.collections.ByteList
import com.mta.tehreer.collections.FloatList
import com.mta.tehreer.collections.IntList
import com.mta.tehreer.collections.PointList
import com.mta.tehreer.internal.Description

class DescriptionBuilder<T> private constructor(private val clazz: Class<T>) {
    private val properties = LinkedHashMap<String, Any?>()

    fun put(name: String, value: Any?): DescriptionBuilder<T> {
        properties[name] = value
        return this
    }

    private fun getDescription(value: Any?): String {
        return when (value) {
            is ByteArray -> Description.forByteArray(value)
            is IntArray -> Description.forIntList(IntList.of(*value))
            is FloatArray -> Description.forFloatList(FloatList.of(*value))
            is Array<*> -> Description.forIterable(value.asList())
            is ByteList -> Description.forByteList(value)
            is IntList -> Description.forIntList(value)
            is FloatList -> Description.forFloatList(value)
            is PointList -> Description.forPointList(value)
            is Iterator<*> -> Description.forIterator(value)
            is Iterable<*> -> Description.forIterable(value)
            else -> value.toString()
        }
    }

    fun build(): String {
        val builder = StringBuilder()
        builder.append(clazz.simpleName)
            .append("{")

        var isFirst = true
        for ((name, value) in properties) {
            builder.append(if (isFirst) "" else ", ")
            builder.append(name).append("=").append(getDescription(value))
            isFirst = false
        }

        builder.append("}")
        return builder.toString()
    }

    companion object {
        @JvmStatic
        fun <T> of(clazz: Class<T>): DescriptionBuilder<T> {
            return DescriptionBuilder(clazz)
        }
    }
}
