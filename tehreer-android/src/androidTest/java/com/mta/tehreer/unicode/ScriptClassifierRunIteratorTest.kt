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

import com.mta.tehreer.collections.IntList

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

import org.junit.Before
import org.junit.Test

import java.util.NoSuchElementException

class ScriptClassifierRunIteratorTest {
    private lateinit var subject: ScriptClassifier.RunIterator

    private val scripts = IntList.of(Script.LATIN, Script.COMMON, Script.ARABIC)

    @Before
    fun setUp() {
        subject = ScriptClassifier.RunIterator(scripts, 0, scripts.size())
    }

    @Test
    fun testHasNextForValidIndexes() {
        subject.index = 0
        assertTrue(subject.hasNext())

        subject.index = 1
        assertTrue(subject.hasNext())

        subject.index = 2
        assertTrue(subject.hasNext())
    }

    @Test
    fun testHasNextForEndingIndex() {
        subject.index = scripts.size()
        assertFalse(subject.hasNext())
    }

    @Test
    fun testNextForFirstRun() {
        var scriptRun: ScriptRun

        // Given
        subject.index = 0

        // When
        scriptRun = subject.next()

        // Then
        assertEquals(subject.index, 1)
        assertEquals(scriptRun.charStart, 0)
        assertEquals(scriptRun.charEnd, 1)
        assertEquals(scriptRun.script, Script.LATIN)
    }

    @Test
    fun testNextForMidRun() {
        var scriptRun: ScriptRun

        // Given
        subject.index = 1

        // When
        scriptRun = subject.next()

        // Then
        assertEquals(subject.index, 2)
        assertEquals(scriptRun.charStart, 1)
        assertEquals(scriptRun.charEnd, 2)
        assertEquals(scriptRun.script, Script.COMMON)
    }

    @Test
    fun testNextForLastRun() {
        var scriptRun: ScriptRun

        // Given
        subject.index = 2

        // When
        scriptRun = subject.next()

        // Then
        assertEquals(subject.index, 3)
        assertEquals(scriptRun.charStart, 2)
        assertEquals(scriptRun.charEnd, 3)
        assertEquals(scriptRun.script, Script.ARABIC)
    }

    @Test(expected = NoSuchElementException::class)
    fun testNextForNoAvailableRun() {
        // Given
        subject.index = 3

        // When
        subject.next()
    }

}


