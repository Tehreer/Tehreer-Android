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
class BidiLineMirrorIteratorTest {
    @Mock
    private lateinit var line: BidiLine
    @Mock
    private lateinit var locator: BidiMirrorLocator
    private lateinit var subject: BidiLine.MirrorIterator

    @Before
    fun setUp() {
        subject = BidiLine.MirrorIterator(line, locator)
    }

    @Test
    fun testHasNextForAvailablePair() {
        // Given
        subject.pair = mock(BidiPair::class.java)

        // When
        val hasNext = subject.hasNext()

        // Then
        assertTrue(hasNext)
    }

    @Test
    fun testHasNextForNoAvailablePair() {
        // Given
        subject.pair = null

        // When
        val hasNext = subject.hasNext()

        // Then
        assertFalse(hasNext)
    }

    @Test
    fun testNextForAvailablePair() {
        val firstPair = BidiPair(0, '('.code, ')'.code)
        val secondPair = BidiPair(1, ')'.code, '('.code)

        `when`(locator.nextPair()).thenReturn(secondPair)

        // Given
        subject.pair = firstPair

        // When
        val bidiPair = subject.next()

        // Then
        assertSame(bidiPair, firstPair)
        assertSame(subject.pair, secondPair)
    }

    @Test(expected = NoSuchElementException::class)
    fun testNextForNoAvailablePair() {
        // Given
        subject.pair = null

        // When
        subject.next()
    }

}
