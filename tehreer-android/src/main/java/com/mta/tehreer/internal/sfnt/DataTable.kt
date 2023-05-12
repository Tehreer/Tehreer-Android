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

internal class DataTable(
    private val data: ByteArray
) : SfntTable {
    override fun readBytes(offset: Int, count: Int): ByteArray {
        val array = ByteArray(count)
        System.arraycopy(data, offset, array, 0, count)
        return array
    }

    private inline fun getAsInt(offset: Int) = data[offset].toInt()

    private inline fun getAsLong(offset: Int) = data[offset].toLong()

    override fun readInt8(offset: Int): Byte {
        return data[offset]
    }

    override fun readUInt8(offset: Int): Short {
        return (getAsInt(offset) and 0xFF).toShort()
    }

    override fun readInt16(offset: Int): Short {
        return ((getAsInt(offset + 0) and 0xFF) shl 8
             or (getAsInt(offset + 1) and 0xFF)).toShort()
    }

    override fun readUInt16(offset: Int): Int {
        return (getAsInt(offset + 0) and 0xFF shl 8
            or (getAsInt(offset + 1) and 0xFF)) and 0xFFFF
    }

    override fun readInt32(offset: Int): Int {
        return (getAsInt(offset + 0) and 0xFF shl 24
            or (getAsInt(offset + 1) and 0xFF shl 16)
            or (getAsInt(offset + 2) and 0xFF shl 8)
            or (getAsInt(offset + 3) and 0xFF))
    }

    override fun readUInt32(offset: Int): Long {
        return (getAsLong(offset + 0) and 0xFF shl 24
            or (getAsLong(offset + 1) and 0xFF shl 16)
            or (getAsLong(offset + 2) and 0xFF shl 8)
            or (getAsLong(offset + 3) and 0xFF)) and 0xFFFFFFFFL
    }

    override fun readInt64(offset: Int): Long {
        return (readUInt32(offset + 0) shl 32
             or readUInt32(offset + 4))
    }
}
