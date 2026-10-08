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

package com.mta.tehreer.internal.collections

import com.mta.tehreer.collections.IntListTestSuite
import com.mta.tehreer.internal.Memory
import com.mta.tehreer.internal.Raw

import org.junit.After
import org.junit.Before
import org.junit.Ignore

import java.nio.ByteOrder

@Ignore
internal class UInt16BufferIntListTest : IntListTestSuite<UInt16BufferIntList>(UInt16BufferIntList::class.java) {
    private val pointers = mutableListOf<Long>()

    private fun buildList(values: IntArray): UInt16BufferIntList {
        val byteSize = values.size.toLong() * Raw.INT16_SIZE
        val pointer = Memory.allocate(byteSize)
        pointers.add(pointer)

        val buffer = Memory.buffer(pointer, byteSize).order(ByteOrder.nativeOrder())
        for (value in values) {
            buffer.putShort(value.toShort())
        }

        return UInt16BufferIntList(this, pointer, values.size)
    }

    override fun buildIdentical(obj: UInt16BufferIntList): UInt16BufferIntList {
        return buildList(obj.toArray())
    }

    @Before
    fun setUp() {
        values = intArrayOf(0x0000, 0x1C1C, 0x3838, 0x5454, 0x7070, 0x8C8C, 0xA8A8, 0xC4C4, 0xE0E0, 0xFFFF)
        subject = buildList(values)
    }

    @After
    fun tearDown() {
        for (pointer in pointers) {
            Memory.dispose(pointer)
        }
    }
}
