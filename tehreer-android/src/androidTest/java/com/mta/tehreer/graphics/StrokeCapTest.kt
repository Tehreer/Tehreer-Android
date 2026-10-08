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

package com.mta.tehreer.graphics

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals

import org.junit.Test

class StrokeCapTest {
    @Test
    fun testOrdinals() {
        assertEquals(StrokeCap.BUTT.ordinal, 0)
        assertEquals(StrokeCap.ROUND.ordinal, 1)
        assertEquals(StrokeCap.SQUARE.ordinal, 2)
    }

    @Test
    fun testValuesArray() {
        val array: Array<StrokeCap> = arrayOf(StrokeCap.BUTT,
            StrokeCap.ROUND,
            StrokeCap.SQUARE)

        assertArrayEquals(StrokeCap.values(), array)
    }

    @Test
    fun testAssociatedValues() {
        assertEquals(StrokeCap.BUTT.value, 0)
        assertEquals(StrokeCap.ROUND.value, 1)
        assertEquals(StrokeCap.SQUARE.value, 2)
    }
}


