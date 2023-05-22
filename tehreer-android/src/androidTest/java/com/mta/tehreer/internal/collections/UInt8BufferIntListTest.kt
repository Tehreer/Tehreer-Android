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

package com.mta.tehreer.internal.collections

import com.mta.tehreer.collections.IntListTestSuite
import com.mta.tehreer.internal.Memory
import org.junit.After
import org.junit.Before
import org.junit.Ignore

@Ignore
internal class UInt8BufferIntListTest : IntListTestSuite<UInt8BufferIntList>(
    UInt8BufferIntList::class.java
) {
    private val pointers = mutableListOf<Long>()

    private fun toByteArray(values: IntArray): ByteArray {
        val length = values.size
        val array = ByteArray(length)

        for (i in 0 until length) {
            array[i] = values[i].toByte()
        }

        return array
    }

    private fun buildList(values: IntArray): UInt8BufferIntList {
        val pointer = Memory.allocate(values.size.toLong())
        pointers.add(pointer)

        val buffer = Memory.buffer(pointer, values.size.toLong())
        buffer.put(toByteArray(values))

        return UInt8BufferIntList(this, pointer, values.size)
    }

    override fun buildIdentical(list: UInt8BufferIntList): UInt8BufferIntList {
        return buildList(list.toArray())
    }

    @Before
    fun setUp() {
        values = intArrayOf(
            0x00, 0x1C, 0x38, 0x54, 0x70, 0x8C, 0xA8, 0xC4, 0xE0, 0xFC
        )
        subject = buildList(values)
    }

    @After
    fun tearDown() {
        for (pointer in pointers) {
            Memory.dispose(pointer)
        }
    }
}
