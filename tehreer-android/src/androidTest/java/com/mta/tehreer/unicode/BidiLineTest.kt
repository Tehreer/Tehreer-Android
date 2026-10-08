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

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue

import com.mta.tehreer.SubjectTestSuite
import com.mta.tehreer.subject.SubjectBuilder
import com.mta.tehreer.util.DescriptionBuilder

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class BidiLineTest : SubjectTestSuite<BidiLine>(BidiLineBuilder()) {
    private companion object {
        private val DEFAULT_TEXT: String = "abcdابجد"
        private val DEFAULT_VISUAL_RUNS: Array<BidiRun> = arrayOf(
        BidiRun(0, 4, 0.toByte()),
        BidiRun(4, 8, 1.toByte())
        )
    }

    class BidiLineBuilder : SubjectBuilder<BidiLine> {
        var text: String = DEFAULT_TEXT
        var startIndex: Int = 0
        var endIndex: Int = text.length
        var baseLevel: Byte = 0

        override fun buildSubject(): BidiLine {
            val bidiAlgorithm = BidiAlgorithm(text)
            val bidiParagraph = bidiAlgorithm.createParagraph(0, text.length, baseLevel)

            return bidiParagraph.createLine(startIndex, endIndex)
        }
    }

    val text = DEFAULT_TEXT
    var startIndex = 0
    var endIndex = text.length
    var baseLevel: Byte = 0

    init {
        onPreBuildSubject = { builder ->
            val subjectBuilder = builder as BidiLineBuilder
            subjectBuilder.text = text
            subjectBuilder.startIndex = startIndex
            subjectBuilder.endIndex = endIndex
            subjectBuilder.baseLevel = baseLevel
        }
    }

    @Test
    fun testCreatorForLeftToRightHalf() {
        // Given
        startIndex = 0
        endIndex = 4
        baseLevel = 0

        // When
        buildSubject({ subject ->
            // Then
            assertEquals(subject.charStart, startIndex)
            assertEquals(subject.charEnd, endIndex)
        })
    }

    @Test
    fun testCreatorForRightToLeftHalf() {
        // Given
        startIndex = 4
        endIndex = 8
        baseLevel = 1

        // When
        buildSubject({ subject ->
            // Then
            assertEquals(subject.charStart, startIndex)
            assertEquals(subject.charEnd, endIndex)
        })
    }

    @Test
    fun testCreatorForMixedDirectionHalf() {
        // Given
        startIndex = 2
        endIndex = 6
        baseLevel = 0

        // When
        buildSubject({ subject ->
            // Then
            assertEquals(subject.charStart, startIndex)
            assertEquals(subject.charEnd, endIndex)
        })
    }

    @Test
    fun testGetRunCount() {
        buildSubject({ subject ->
            // When
            val runCount = subject.runCount

            // Then
            assertEquals(runCount, 2)
        })
    }

    @Test
    fun testGetVisualRunForFirstIndex() {
        buildSubject({ subject ->
            // When
            val run = subject.getVisualRun(0)

            // Then
            assertNotNull(run)
            assertEquals(run.charStart, 0)
            assertEquals(run.charEnd, 4)
            assertEquals(run.embeddingLevel, 0.toByte())
        })
    }

    @Test
    fun testGetVisualRunForLastIndex() {
        buildSubject({ subject ->
            // When
            val run = subject.getVisualRun(1)

            // Then
            assertNotNull(run)
            assertEquals(run.charStart, 4)
            assertEquals(run.charEnd, 8)
            assertEquals(run.embeddingLevel, 1.toByte())
        })
    }

    @Test
    fun testGetVisualRuns() {
        buildSubject({ subject ->
            // When
            val visualRuns: List<BidiRun> = subject.visualRuns

            // Then
            assertTrue(visualRuns is BidiLine.RunList)
            assertSame((visualRuns as BidiLine.RunList).owner, subject)
            assertEquals(visualRuns.size, subject.runCount)
            assertArrayEquals(visualRuns.toTypedArray(), DEFAULT_VISUAL_RUNS)
        })
    }

    @Test
    fun testGetMirroringPairs() {
        buildSubject({ subject ->
            // When
            val mirroringPairs: Iterable<BidiPair> = subject.mirroringPairs

            // Then
            assertTrue(mirroringPairs is BidiLine.MirrorIterable)
            assertSame((mirroringPairs as BidiLine.MirrorIterable).owner, subject)
        })
    }

    @Test
    fun testToString() {
        buildSubject({ subject ->
            val description = DescriptionBuilder
                    .of(BidiLine::class.java)
                    .put("charStart", startIndex)
                    .put("charEnd", endIndex)
                    .put("visualRuns", DEFAULT_VISUAL_RUNS)
                    .put("mirroringPairs", subject.mirroringPairs)
                    .build()

            // When
            val string = subject.toString()

            // Then
            assertEquals(string, description)
        })
    }
}

