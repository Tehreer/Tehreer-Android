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

package com.mta.tehreer.unicode

import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

import java.util.NoSuchElementException

@RunWith(MockitoJUnitRunner::class)
class BidiParagraphRunIteratorTest {
    @Mock
    private lateinit var paragraph: BidiParagraph
    private lateinit var subject: BidiParagraph.RunIterator

    @Before
    fun setUp() {
        subject = BidiParagraph.RunIterator(paragraph)
    }

    @Test
    fun testHasNextForAvailableRun() {
        // Given
        subject.run = mock(BidiRun::class.java)

        // When
        val hasNext = subject.hasNext()

        // Then
        assertTrue(hasNext)
    }

    @Test
    fun testHasNextForNoAvailableRun() {
        // Given
        subject.run = null

        // When
        val hasNext = subject.hasNext()

        // Then
        assertFalse(hasNext)
    }

    @Test
    fun testNextForAvailableRun() {
        val firstRun = BidiRun(0, 4, 0.toByte())
        val secondRun = BidiRun(4, 8, 1.toByte())

        `when`(paragraph.getOnwardRun(4)).thenReturn(secondRun)

        // Given
        subject.run = firstRun

        // When
        val bidiRun = subject.next()

        // Then
        assertSame(bidiRun, firstRun)
        assertSame(subject.run, secondRun)
    }

    @Test(expected = NoSuchElementException::class)
    fun testNextForNoAvailableRun() {
        // Given
        subject.run = null

        // When
        subject.next()
    }

}


