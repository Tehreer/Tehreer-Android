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

class TypeWidthTest {
    @Test
    fun testOrdinals() {
        assertEquals(TypeWidth.ULTRA_CONDENSED.ordinal, 0)
        assertEquals(TypeWidth.EXTRA_CONDENSED.ordinal, 1)
        assertEquals(TypeWidth.CONDENSED.ordinal, 2)
        assertEquals(TypeWidth.SEMI_CONDENSED.ordinal, 3)
        assertEquals(TypeWidth.NORMAL.ordinal, 4)
        assertEquals(TypeWidth.SEMI_EXPANDED.ordinal, 5)
        assertEquals(TypeWidth.EXPANDED.ordinal, 6)
        assertEquals(TypeWidth.EXTRA_EXPANDED.ordinal, 7)
        assertEquals(TypeWidth.ULTRA_EXPANDED.ordinal, 8)
    }

    @Test
    fun testValuesArray() {
        val array: Array<TypeWidth> = arrayOf(TypeWidth.ULTRA_CONDENSED,
            TypeWidth.EXTRA_CONDENSED,
            TypeWidth.CONDENSED,
            TypeWidth.SEMI_CONDENSED,
            TypeWidth.NORMAL,
            TypeWidth.SEMI_EXPANDED,
            TypeWidth.EXPANDED,
            TypeWidth.EXTRA_EXPANDED,
            TypeWidth.ULTRA_EXPANDED)

        assertArrayEquals(TypeWidth.values(), array)
    }

    @Test
    fun testAssociatedValues() {
        assertEquals(TypeWidth.ULTRA_CONDENSED.value, 1)
        assertEquals(TypeWidth.EXTRA_CONDENSED.value, 2)
        assertEquals(TypeWidth.CONDENSED.value, 3)
        assertEquals(TypeWidth.SEMI_CONDENSED.value, 4)
        assertEquals(TypeWidth.NORMAL.value, 5)
        assertEquals(TypeWidth.SEMI_EXPANDED.value, 6)
        assertEquals(TypeWidth.EXPANDED.value, 7)
        assertEquals(TypeWidth.EXTRA_EXPANDED.value, 8)
        assertEquals(TypeWidth.ULTRA_EXPANDED.value, 9)
    }

    @Test
    fun testValueOf() {
        assertEquals(TypeWidth.valueOf(-1), TypeWidth.ULTRA_CONDENSED)
        assertEquals(TypeWidth.valueOf(0), TypeWidth.ULTRA_CONDENSED)
        assertEquals(TypeWidth.valueOf(1), TypeWidth.ULTRA_CONDENSED)
        assertEquals(TypeWidth.valueOf(2), TypeWidth.EXTRA_CONDENSED)
        assertEquals(TypeWidth.valueOf(3), TypeWidth.CONDENSED)
        assertEquals(TypeWidth.valueOf(4), TypeWidth.SEMI_CONDENSED)
        assertEquals(TypeWidth.valueOf(5), TypeWidth.NORMAL)
        assertEquals(TypeWidth.valueOf(6), TypeWidth.SEMI_EXPANDED)
        assertEquals(TypeWidth.valueOf(7), TypeWidth.EXPANDED)
        assertEquals(TypeWidth.valueOf(8), TypeWidth.EXTRA_EXPANDED)
        assertEquals(TypeWidth.valueOf(9), TypeWidth.ULTRA_EXPANDED)
        assertEquals(TypeWidth.valueOf(10), TypeWidth.ULTRA_EXPANDED)
    }

}


