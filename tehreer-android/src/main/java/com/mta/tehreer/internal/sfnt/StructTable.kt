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

package com.mta.tehreer.internal.sfnt

import com.mta.tehreer.internal.Raw.copyInt8Buffer
import com.mta.tehreer.internal.Raw.getInt16Value
import com.mta.tehreer.internal.Raw.getInt32Value
import com.mta.tehreer.internal.Raw.getInt8Value

internal open class StructTable(
    /**
     * Represents the source from which the struct pointer was obtained. Keep the source in memory
     * so that it does not accidentally get disposed by the GC when in use.
     */
    private val source: Any?,
    /**
     * Represents the pointer to a native struct modeling an open type table.
     */
    protected val pointer: Long
) : SfntTable {
    protected open fun pointerOf(offset: Int): Long {
        return pointer + offset
    }

    override fun readBytes(offset: Int, count: Int): ByteArray {
        val array = ByteArray(count)
        copyInt8Buffer(pointerOf(offset), array, 0, count)

        return array
    }

    override fun readInt8(offset: Int): Byte {
        return getInt8Value(pointerOf(offset))
    }

    override fun readUInt8(offset: Int): Short {
        return (getInt8Value(pointerOf(offset)).toInt() and 0xFF).toShort()
    }

    override fun readInt16(offset: Int): Short {
        return getInt16Value(pointerOf(offset))
    }

    override fun readUInt16(offset: Int): Int {
        return getInt16Value(pointerOf(offset)).toInt() and 0xFFFF
    }

    override fun readInt32(offset: Int): Int {
        return getInt32Value(pointerOf(offset))
    }

    override fun readUInt32(offset: Int): Long {
        return getInt32Value(pointerOf(offset)).toLong() and 0xFFFFFFFFL
    }

    override fun readInt64(offset: Int): Long {
        return (getInt32Value(pointerOf(offset + 0)).toLong() and 0xFFFFFFFFL shl 32
            or (getInt32Value(pointerOf(offset + 4)).toLong() and 0xFFFFFFFFL))
    }
}
