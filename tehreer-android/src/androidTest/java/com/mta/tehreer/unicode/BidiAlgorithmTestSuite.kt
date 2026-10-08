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

import com.mta.tehreer.unicode.BidiClass.ARABIC_LETTER
import com.mta.tehreer.unicode.BidiClass.LEFT_TO_RIGHT
import com.mta.tehreer.util.Assert.assertThrows
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue

import com.mta.tehreer.internal.collections.UInt8BufferIntList
import com.mta.tehreer.DisposableTestSuite
import com.mta.tehreer.subject.UnsafeSubjectBuilder
import com.mta.tehreer.util.DescriptionBuilder

import org.junit.Test

abstract class BidiAlgorithmTestSuite(defaultMode: DefaultMode) : DisposableTestSuite<BidiAlgorithm>(BidiAlgorithmBuilder(), defaultMode) {
    private companion object {
        private val DEFAULT_TEXT: String = "abcdابجد"
    }

    class BidiAlgorithmBuilder : UnsafeSubjectBuilder<BidiAlgorithm>(BidiAlgorithm::class.java) {
        var text: String = DEFAULT_TEXT

        override fun buildSubject(): BidiAlgorithm {
            return BidiAlgorithm(text)
        }
    }

    var text = DEFAULT_TEXT
    protected val values = intArrayOf(
        LEFT_TO_RIGHT, LEFT_TO_RIGHT, LEFT_TO_RIGHT, LEFT_TO_RIGHT,
        ARABIC_LETTER, ARABIC_LETTER, ARABIC_LETTER, ARABIC_LETTER,
    )

    init {
        onPreBuildSubject = { builder ->
            val subjectBuilder = builder as BidiAlgorithmBuilder
            subjectBuilder.text = text
        }
    }

    @Test
    fun testNativePointers() {
        buildSubject({ subject ->
            assertNotEquals(subject.nativeBuffer, 0)
            assertNotEquals(subject.nativeAlgorithm, 0)
        })
    }

    @Test
    fun testGetCharBidiClasses() {
        buildSubject({ subject ->
            // When
            val bidiClasses = subject.charBidiClasses

            // Then
            assertTrue(bidiClasses is UInt8BufferIntList)
            assertEquals(bidiClasses.size(), text.length)
            assertArrayEquals(bidiClasses.toArray(), values)
        })
    }

    private fun testRangeExceptions(consumer: (Int, Int) -> Unit) {
        // Invalid Start
        assertThrows(IllegalArgumentException::class.java, "Char Start: -1",
                     { consumer(-1, 8) })

        // Invalid End
        assertThrows(IllegalArgumentException::class.java, "Char End: 9, Text Length: 8",
                     { consumer(0, 9) })

        // Empty Range
        assertThrows(IllegalArgumentException::class.java, "Bad Range: [0, 0)",
                     { consumer(0, 0) })
    }

    @Test
    fun testGetParagraphBoundaryForInvalidRange() {
        buildSubject({ subject ->
            testRangeExceptions { startIndex, endIndex ->
                subject.getParagraphBoundary(startIndex, endIndex)
            }
        })
    }

    @Test
    fun testGetParagraphBoundaryForFullRange() {
        buildSubject({ subject ->
            // Given
            val startIndex = 0
            val endIndex = text.length

            // When
            val paragraphBoundary = subject.getParagraphBoundary(startIndex, endIndex)

            // Then
            assertEquals(paragraphBoundary, endIndex)
        })
    }

    @Test
    fun testCreateParagraphWithBaseDirectionForInvalidRange() {
        buildSubject({ subject ->
            testRangeExceptions({ startIndex, endIndex ->
                subject.createParagraph(startIndex, endIndex, BaseDirection.LEFT_TO_RIGHT)
            })
        })
    }

    @Test
    fun testCreateParagraphForFullRangeAndLTRBaseDirection() {
        buildSubject({ subject ->
            // Given
            val startIndex = 0
            val endIndex = text.length
            val baseDirection = BaseDirection.LEFT_TO_RIGHT

            // When
            val paragraph = subject.createParagraph(startIndex, endIndex, baseDirection)

            // Then
            assertNotNull(paragraph)
        })
    }

    @Test
    fun testCreateParagraphWithBaseLevelForInvalidRange() {
        buildSubject({ subject ->
            testRangeExceptions({ startIndex, endIndex ->
                subject.createParagraph(startIndex, endIndex, 0.toByte())
            })
        })
    }

    @Test
    fun testCreateParagraphForInvalidBaseLevel() {
        buildSubject({ subject ->
            // Negative Value
            assertThrows(IllegalArgumentException::class.java, "Base Level: -1",
                         { subject.createParagraph(0, text.length, (-1).toByte()) })

            // > MAX Value
            assertThrows(IllegalArgumentException::class.java, "Base Level: 126",
                         { subject.createParagraph(0, text.length, 126.toByte()) })
        })
    }

    @Test
    fun testCreateParagraphForFullRangeAndZeroBaseLevel() {
        buildSubject({ subject ->
            // Given
            val startIndex = 0
            val endIndex = text.length
            val baseLevel: Byte = 0

            // When
            val paragraph = subject.createParagraph(startIndex, endIndex, baseLevel)

            // Then
            assertNotNull(paragraph)
        })
    }

    @Test
    fun testToString() {
        buildSubject({ subject ->
            val description = DescriptionBuilder
                    .of(BidiAlgorithm::class.java)
                    .put("text", text)
                    .put("charBidiClasses", values)
                    .build()

            // When
            val string = subject.toString()

            // Then
            assertEquals(string, description)
        })
    }
}

