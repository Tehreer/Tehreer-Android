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

internal class SubTable(
    private val table: SfntTable,
    private val globalOffset: Int
) : AbstractSfntTable() {
    override fun readBytes(offset: Int, count: Int): ByteArray {
        return table.readBytes(globalOffset + offset, count)
    }

    override fun readInt8(offset: Int): Byte {
        return table.readInt8(globalOffset + offset)
    }

    override fun readUInt8(offset: Int): Short {
        return table.readUInt8(globalOffset + offset)
    }

    override fun readInt16(offset: Int): Short {
        return table.readInt16(globalOffset + offset)
    }

    override fun readInt32(offset: Int): Int {
        return table.readInt32(globalOffset + offset)
    }

    override fun readUInt16(offset: Int): Int {
        return table.readUInt16(globalOffset + offset)
    }

    override fun readUInt32(offset: Int): Long {
        return table.readUInt32(globalOffset + offset)
    }

    override fun readInt64(offset: Int): Long {
        return table.readInt64(globalOffset + offset)
    }

    override fun subTable(offset: Int): SfntTable {
        return SubTable(table, globalOffset + offset)
    }
}
