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

package com.mta.tehreer.unicode

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

internal class BreakClassifierTest {
    private val text = "Hello\nWorld!"

    private lateinit var subject: BreakClassifier

    @Before
    fun setUp() {
        subject = BreakClassifier(text)
    }

    @Test
    fun getForwardGraphemeBreaks_shouldReturnCorrectIterator() {
        val iterator = subject.getForwardGraphemeBreaks(3, 8)

        assertEquals(4, iterator.nextInt())
        assertEquals(5, iterator.nextInt())
        assertEquals(6, iterator.nextInt())
        assertEquals(7, iterator.nextInt())
        assertEquals(8, iterator.nextInt())
        assertFalse(iterator.hasNext())
    }

    @Test
    fun getBackwardGraphemeBreaks_shouldReturnCorrectIterator() {
        val iterator = subject.getBackwardGraphemeBreaks(3, 8)

        assertEquals(7, iterator.nextInt())
        assertEquals(6, iterator.nextInt())
        assertEquals(5, iterator.nextInt())
        assertEquals(4, iterator.nextInt())
        assertEquals(3, iterator.nextInt())
        assertFalse(iterator.hasNext())
    }

    @Test
    fun getForwardLineBreaks_shouldReturnCorrectIterator() {
        val iterator = subject.getForwardLineBreaks(2, 9)

        assertEquals(6, iterator.nextInt())
        assertEquals(9, iterator.nextInt())
        assertFalse(iterator.hasNext())
    }

    @Test
    fun getBackwardLineBreaks_shouldReturnCorrectIterator() {
        val iterator = subject.getBackwardLineBreaks(2, 9)

        assertEquals(6, iterator.nextInt())
        assertEquals(2, iterator.nextInt())
        assertFalse(iterator.hasNext())
    }
}
