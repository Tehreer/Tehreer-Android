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
import org.junit.Before

internal class JByteArrayIntListTest : IntListTestSuite<JByteArrayIntList>(
    JByteArrayIntList::class.java
) {
    private fun toByteArray(values: IntArray): ByteArray {
        val length = values.size
        val array = ByteArray(length)

        for (i in 0 until length) {
            array[i] = values[i].toByte()
        }

        return array
    }

    private fun buildList(values: IntArray): JByteArrayIntList {
        return JByteArrayIntList(toByteArray(values), 0, values.size)
    }

    override fun buildIdentical(list: JByteArrayIntList): JByteArrayIntList {
        return buildList(list.toArray())
    }

    @Before
    fun setUp() {
        values = intArrayOf(
            0, 28, 56, 84, 112, -116, -88, -60, -32, -4
        )
        subject = buildList(values)
    }
}
