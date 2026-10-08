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

import com.mta.tehreer.util.Assert.assertThrows
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue

import com.mta.tehreer.DisposableTestSuite
import com.mta.tehreer.collections.ByteList
import com.mta.tehreer.internal.collections.Int8BufferByteList
import com.mta.tehreer.subject.UnsafeSubjectBuilder
import com.mta.tehreer.util.DescriptionBuilder

import org.junit.Ignore
import org.junit.Test

abstract class BidiParagraphTestSuite(defaultMode: DefaultMode) : DisposableTestSuite<BidiParagraph>(BidiParagraphBuilder(), defaultMode) {
    private companion object {
        private val DEFAULT_TEXT: String = "abcdابجد"
        private val DEFAULT_LEVELS: ByteArray = byteArrayOf(0, 0, 0, 0, 1, 1, 1, 1)
    }

    class BidiParagraphBuilder : UnsafeSubjectBuilder<BidiParagraph>(BidiParagraph::class.java) {
        var text: String = DEFAULT_TEXT
        var startIndex: Int = 0
        var endIndex: Int = text.length
        var baseLevel: Byte = 0

        override fun buildSubject(): BidiParagraph {
            val bidiAlgorithm = BidiAlgorithm.finalizable(BidiAlgorithm(text))
            return bidiAlgorithm.createParagraph(startIndex, endIndex, baseLevel)
        }
    }

    val text = DEFAULT_TEXT
    var startIndex = 0
    var endIndex = text.length
    var baseLevel: Byte = 0

    init {
        onPreBuildSubject = { builder ->
            val subjectBuilder = builder as BidiParagraphBuilder
            subjectBuilder.text = text
            subjectBuilder.startIndex = startIndex
            subjectBuilder.endIndex = endIndex
            subjectBuilder.baseLevel = baseLevel
        }
    }

    @Test
    @Ignore
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
            assertEquals(subject.baseLevel, baseLevel)
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
            assertEquals(subject.baseLevel, baseLevel)
        })
    }

    @Test
    @Ignore
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
            assertEquals(subject.baseLevel, baseLevel)
        })
    }

    @Test
    fun testNativePointers() {
        buildSubject({ subject ->
            assertNotEquals(subject.nativeBuffer, 0)
            assertNotEquals(subject.nativeParagraph, 0)
        })
    }

    @Test
    fun testGetCharLevels() {
        buildSubject({ subject ->
            // When
            val charLevels = subject.charLevels

            // Then
            assertTrue(charLevels is Int8BufferByteList)
            assertEquals(charLevels.size(), text.length)
            assertArrayEquals(charLevels.toArray(), DEFAULT_LEVELS)
        })
    }

    @Test
    fun testGetOnwardRunForLeftToRightRun() {
        buildSubject({ subject ->
            // When
            val run = subject.getOnwardRun(0)

            // Then
            assertNotNull(run)
            assertEquals(run!!.charStart, 0)
            assertEquals(run!!.charEnd, 4)
            assertEquals(run!!.embeddingLevel, 0.toByte())
        })
    }

    @Test
    fun testGetOnwardRunForRightToLeftRun() {
        buildSubject({ subject ->
            // When
            val run = subject.getOnwardRun(5)

            // Then
            assertNotNull(run)
            assertEquals(run!!.charStart, 5)
            assertEquals(run!!.charEnd, 8)
            assertEquals(run!!.embeddingLevel, 1.toByte())
        })
    }

    @Test
    fun testGetOnwardRunFromEndIndex() {
        buildSubject({ subject ->
            // When
            val run = subject.getOnwardRun(8)

            // Then
            assertNull(run)
        })
    }

    @Test
    fun testGetLogicalRuns() {
        buildSubject({ subject ->
            // When
            val iterator: Iterator<BidiRun> = subject.logicalRuns

            // Then
            assertTrue(iterator is BidiParagraph.RunIterator)
            assertSame((iterator as BidiParagraph.RunIterator).owner, subject)
        })
    }

    @Test
    fun testCreateLineForInvalidRange() {
        buildSubject({ subject ->
            // Invalid Start
            assertThrows(IllegalArgumentException::class.java,
                         "Char Start: -1, Paragraph Range: [0, 8)",
                         { subject.createLine(-1, 8) })

            // Invalid End
            assertThrows(IllegalArgumentException::class.java,
                         "Char End: 9, Paragraph Range: [0, 8)",
                         { subject.createLine(0, 9) })

            // Empty Range
            assertThrows(IllegalArgumentException::class.java, "Bad Range: [0, 0)",
                         { subject.createLine(0, 0) })
        })
    }

    @Test
    fun testCreateLineForFullRange() {
        buildSubject({ subject ->
            // When
            val line = subject.createLine(startIndex, endIndex)

            // Then
            assertNotNull(line)
        })
    }

    @Test
    fun testToString() {
        buildSubject({ subject ->
            val description = DescriptionBuilder
                    .of(BidiParagraph::class.java)
                    .put("charStart", startIndex)
                    .put("charEnd", endIndex)
                    .put("baseLevel", baseLevel)
                    .put("charLevels", DEFAULT_LEVELS)
                    .put("logicalRuns", subject.logicalRuns)
                    .build()

            // When
            val string = subject.toString()

            // Then
            assertEquals(string, description)
        })
    }
}

