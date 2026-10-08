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

package com.mta.tehreer.internal.layout

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals

import org.junit.Test

class ParagraphsTest {
    @Test
    fun testBoundsOfEmptyText() {
        assertArrayEquals(IntArray(0), Paragraphs.boundsOf(""))
    }

    @Test
    fun testBoundsOfSingleParagraph() {
        assertArrayEquals(intArrayOf(0, 5), Paragraphs.boundsOf("hello"))
    }

    @Test
    fun testSeparatorBelongsToItsParagraph() {
        assertArrayEquals(intArrayOf(0, 3, 3, 6), Paragraphs.boundsOf("ab\ncd\n"))
        assertArrayEquals(intArrayOf(0, 3, 3, 5), Paragraphs.boundsOf("ab\ncd"))
    }

    @Test
    fun testCarriageReturnAndLineFeedMakeOneSeparator() {
        assertArrayEquals(intArrayOf(0, 4, 4, 6), Paragraphs.boundsOf("ab\r\ncd"))
        assertArrayEquals(intArrayOf(0, 3, 3, 6), Paragraphs.boundsOf("ab\rcd\r"))
    }

    @Test
    fun testOtherSeparators() {
        assertArrayEquals(intArrayOf(0, 3, 3, 4, 4, 6), Paragraphs.boundsOf("ab\u2029\u0085cd"))
    }

    @Test
    fun testIndexOf() {
        val bounds = Paragraphs.boundsOf("ab\ncd\nef")

        assertEquals(0, Paragraphs.indexOf(bounds, 0))
        assertEquals(0, Paragraphs.indexOf(bounds, 2))
        assertEquals(1, Paragraphs.indexOf(bounds, 3))
        assertEquals(1, Paragraphs.indexOf(bounds, 5))
        assertEquals(2, Paragraphs.indexOf(bounds, 6))
        assertEquals(2, Paragraphs.indexOf(bounds, 8))
    }
}


