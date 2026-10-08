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
import org.junit.Assert.assertTrue
import org.junit.Assert.fail

import com.mta.tehreer.collections.IntList
import com.mta.tehreer.internal.collections.UInt8BufferIntList
import com.mta.tehreer.util.DescriptionBuilder

import org.junit.Before
import org.junit.Test


class ScriptClassifierTest {
    private lateinit var subject: ScriptClassifier

    private val text: String = "abcdابجد"

    @Before
    fun setUp() {
        subject = ScriptClassifier(text)
    }

    @Test
    fun testGetText() {
        assertEquals(subject.text, text)
    }

    @Test
    fun testGetCharScripts() {
        val charScripts = subject.charScripts

        assertTrue(charScripts is UInt8BufferIntList)
    }

    @Test
    fun testGetScriptRunsForInvalidRange() {
        // Invalid Start
        try {
            subject.getScriptRuns(-1, 8)
            fail()
        } catch (exception: IllegalArgumentException) {
            assertEquals(exception.message, "Char Start: -1")
        }

        // Invalid End
        try {
            subject.getScriptRuns(0, 9)
            fail()
        } catch (exception: IllegalArgumentException) {
            assertEquals(exception.message, "Char End: 9, Text Length: 8")
        }

        // Empty Range
        try {
            subject.getScriptRuns(0, 0)
            fail()
        } catch (exception: IllegalArgumentException) {
            assertEquals(exception.message, "Bad Range: [0, 0)")
        }
    }

    @Test
    fun testGetScriptRunsForPartialRange() {
        val iterator: Iterator<ScriptRun> = subject.getScriptRuns(1, 7)
        assertTrue(iterator is ScriptClassifier.RunIterator)

        val runIterator = iterator as ScriptClassifier.RunIterator
        assertEquals(runIterator.index, 1)
        assertEquals(runIterator.end, 7)
    }

    @Test
    fun testGetScriptRunsForFullRange() {
        val iterator: Iterator<ScriptRun> = subject.scriptRuns
        assertTrue(iterator is ScriptClassifier.RunIterator)

        val runIterator = iterator as ScriptClassifier.RunIterator
        assertEquals(runIterator.index, 0)
        assertEquals(runIterator.end, 8)
    }

    @Test
    fun testToString() {
        val description = DescriptionBuilder
                .of(ScriptClassifier::class.java)
                .put("text", text)
                .put("charScripts", subject.charScripts)
                .put("scriptRuns", subject.scriptRuns)
                .build()

        // When
        val string = subject.toString()

        // Then
        assertEquals(string, description)
    }
}


