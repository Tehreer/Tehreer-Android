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

class TypeWeightTest {
    @Test
    fun testOrdinals() {
        assertEquals(TypeWeight.THIN.ordinal, 0)
        assertEquals(TypeWeight.EXTRA_LIGHT.ordinal, 1)
        assertEquals(TypeWeight.LIGHT.ordinal, 2)
        assertEquals(TypeWeight.REGULAR.ordinal, 3)
        assertEquals(TypeWeight.MEDIUM.ordinal, 4)
        assertEquals(TypeWeight.SEMI_BOLD.ordinal, 5)
        assertEquals(TypeWeight.BOLD.ordinal, 6)
        assertEquals(TypeWeight.EXTRA_BOLD.ordinal, 7)
        assertEquals(TypeWeight.HEAVY.ordinal, 8)
    }

    @Test
    fun testValuesArray() {
        val array: Array<TypeWeight> = arrayOf(TypeWeight.THIN,
            TypeWeight.EXTRA_LIGHT,
            TypeWeight.LIGHT,
            TypeWeight.REGULAR,
            TypeWeight.MEDIUM,
            TypeWeight.SEMI_BOLD,
            TypeWeight.BOLD,
            TypeWeight.EXTRA_BOLD,
            TypeWeight.HEAVY)

        assertArrayEquals(TypeWeight.values(), array)
    }

    @Test
    fun testAssociatedValues() {
        assertEquals(TypeWeight.THIN.value, 100)
        assertEquals(TypeWeight.EXTRA_LIGHT.value, 200)
        assertEquals(TypeWeight.LIGHT.value, 300)
        assertEquals(TypeWeight.REGULAR.value, 400)
        assertEquals(TypeWeight.MEDIUM.value, 500)
        assertEquals(TypeWeight.SEMI_BOLD.value, 600)
        assertEquals(TypeWeight.BOLD.value, 700)
        assertEquals(TypeWeight.EXTRA_BOLD.value, 800)
        assertEquals(TypeWeight.HEAVY.value, 900)
    }

    @Test
    fun testValueOf() {
        assertEquals(TypeWeight.valueOf(-1), TypeWeight.THIN)
        assertEquals(TypeWeight.valueOf(0), TypeWeight.THIN)
        assertEquals(TypeWeight.valueOf(1), TypeWeight.THIN)
        assertEquals(TypeWeight.valueOf(50), TypeWeight.THIN)
        assertEquals(TypeWeight.valueOf(100), TypeWeight.THIN)
        assertEquals(TypeWeight.valueOf(149), TypeWeight.THIN)
        assertEquals(TypeWeight.valueOf(150), TypeWeight.EXTRA_LIGHT)
        assertEquals(TypeWeight.valueOf(200), TypeWeight.EXTRA_LIGHT)
        assertEquals(TypeWeight.valueOf(249), TypeWeight.EXTRA_LIGHT)
        assertEquals(TypeWeight.valueOf(250), TypeWeight.LIGHT)
        assertEquals(TypeWeight.valueOf(300), TypeWeight.LIGHT)
        assertEquals(TypeWeight.valueOf(349), TypeWeight.LIGHT)
        assertEquals(TypeWeight.valueOf(350), TypeWeight.REGULAR)
        assertEquals(TypeWeight.valueOf(400), TypeWeight.REGULAR)
        assertEquals(TypeWeight.valueOf(449), TypeWeight.REGULAR)
        assertEquals(TypeWeight.valueOf(450), TypeWeight.MEDIUM)
        assertEquals(TypeWeight.valueOf(500), TypeWeight.MEDIUM)
        assertEquals(TypeWeight.valueOf(549), TypeWeight.MEDIUM)
        assertEquals(TypeWeight.valueOf(550), TypeWeight.SEMI_BOLD)
        assertEquals(TypeWeight.valueOf(600), TypeWeight.SEMI_BOLD)
        assertEquals(TypeWeight.valueOf(649), TypeWeight.SEMI_BOLD)
        assertEquals(TypeWeight.valueOf(650), TypeWeight.BOLD)
        assertEquals(TypeWeight.valueOf(700), TypeWeight.BOLD)
        assertEquals(TypeWeight.valueOf(749), TypeWeight.BOLD)
        assertEquals(TypeWeight.valueOf(750), TypeWeight.EXTRA_BOLD)
        assertEquals(TypeWeight.valueOf(800), TypeWeight.EXTRA_BOLD)
        assertEquals(TypeWeight.valueOf(849), TypeWeight.EXTRA_BOLD)
        assertEquals(TypeWeight.valueOf(850), TypeWeight.HEAVY)
        assertEquals(TypeWeight.valueOf(900), TypeWeight.HEAVY)
        assertEquals(TypeWeight.valueOf(1000), TypeWeight.HEAVY)
        assertEquals(TypeWeight.valueOf(1001), TypeWeight.HEAVY)
        assertEquals(TypeWeight.valueOf(1050), TypeWeight.HEAVY)
    }

}


