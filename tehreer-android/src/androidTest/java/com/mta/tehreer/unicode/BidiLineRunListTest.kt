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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.mockito.Mockito.`when`

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class BidiLineRunListTest {
    private val DEFAULT_SIZE: Int = 2

    @Mock
    private lateinit var line: BidiLine
    private lateinit var subject: BidiLine.RunList

    @Before
    fun setUp() {
        `when`(line.runCount).thenReturn(DEFAULT_SIZE)

        subject = BidiLine.RunList(line)
    }

    @Test
    fun testSize() {
        // When
        val size = subject.size

        // Then
        assertEquals(size, DEFAULT_SIZE)
    }

    @Test(expected = IndexOutOfBoundsException::class)
    fun testGetForNegativeIndex() {
        // When
        subject.get(-1)
    }

    @Test(expected = IndexOutOfBoundsException::class)
    fun testGetForLimitIndex() {
        // When
        subject.get(DEFAULT_SIZE)
    }

    @Test
    fun testGetForFirstIndex() {
        val anyRun = BidiRun()
        `when`(line.getVisualRun(0)).thenReturn(anyRun)

        // When
        val run = subject.get(0)

        // Then
        assertSame(run, anyRun)
    }

    @Test
    fun testGetForLastIndex() {
        val anyRun = BidiRun()
        `when`(line.getVisualRun(1)).thenReturn(anyRun)

        // When
        val run = subject.get(1)

        // Then
        assertSame(run, anyRun)
    }
}


