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

package com.mta.tehreer.sfnt

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify

import com.mta.tehreer.SubjectTestSuite
import com.mta.tehreer.collections.FloatList
import com.mta.tehreer.collections.IntList
import com.mta.tehreer.collections.PointList
import com.mta.tehreer.graphics.Typeface
import com.mta.tehreer.subject.SubjectBuilder
import com.mta.tehreer.util.DescriptionBuilder
import com.mta.tehreer.util.TypefaceStore

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

import java.util.Arrays
import java.util.Collections

@RunWith(MockitoJUnitRunner::class)
class ShapingResultTest : SubjectTestSuite<ShapingResult>(ShapingResultBuilder()) {
    private companion object {
        private val DEFAULT_TYPEFACE: Typeface = TypefaceStore.nafeesWeb
        private val DEFAULT_TYPE_SIZE: Float = DEFAULT_TYPEFACE.unitsPerEm / 2.0f
        private val DEFAULT_SCRIPT_TAG: String = "arab"
        private val DEFAULT_LANGUAGE_TAG: String = "dflt"
        private val DEFAULT_OPEN_TYPE_FEATURES: Set<OpenTypeFeature> = Collections.emptySet()
        private val DEFAULT_WRITING_DIRECTION: WritingDirection = WritingDirection.RIGHT_TO_LEFT
        private val DEFAULT_SHAPING_ORDER: ShapingOrder = ShapingOrder.FORWARD
        private val DEFAULT_TEXT: String = "ابجد ہوز حطی"
        private val DEFAULT_GLYPH_IDS: IntList = IntList.of(
        5, 49, 83, 117, 242, 74, 140, 20, 242, 56, 91, 145
        )
        private val DEFAULT_GLYPH_OFFSETS: PointList = PointList.of(
        0.0f, 0.0f,  0.0f, 0.0f,  0.0f, 0.0f,  0.0f, 0.0f,  0.0f, 0.0f,  0.0f, 0.0f,
        0.0f, 0.0f,  0.0f, 0.0f,  0.0f, 0.0f,  0.0f, 0.0f,  0.0f, 0.0f,  0.0f, 0.0f
        )
        private val DEFAULT_GLYPH_ADVANCES: FloatList = FloatList.of(
        181.0f, 183.5f, 487.0f, 507.5f, 127.5f, 453.0f,
        312.5f, 249.0f, 127.5f, 499.5f, 565.5f, 597.0f
        )
        private val DEFAULT_CLUSTER_MAP: IntList = IntList.of(
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11
        )
        private val DEFAULT_CARET_EDGES: FloatArray = floatArrayOf(4290.5f, 4109.5f, 3926.0f, 3439.0f, 2931.5f, 2804.0f,
        2351.0f, 2038.5f, 1789.5f, 1662.0f, 1162.5f, 597.0f, 0.0f)
    }

    class ShapingResultBuilder : SubjectBuilder<ShapingResult> {
        var typeface: Typeface = DEFAULT_TYPEFACE
        var typeSize: Float = DEFAULT_TYPE_SIZE
        var scriptTag: String = DEFAULT_SCRIPT_TAG
        var languageTag: String = DEFAULT_LANGUAGE_TAG
        var openTypeFeatures: Set<OpenTypeFeature> = DEFAULT_OPEN_TYPE_FEATURES
        var writingDirection: WritingDirection = DEFAULT_WRITING_DIRECTION
        var shapingOrder: ShapingOrder = DEFAULT_SHAPING_ORDER
        var text: String = DEFAULT_TEXT
        var startIndex: Int = 0
        var endIndex: Int = DEFAULT_TEXT.length

        override fun buildSubject(): ShapingResult {
            val shapingEngine = ShapingEngine()
            shapingEngine.typeface = typeface
            shapingEngine.typeSize = typeSize
            shapingEngine.scriptTag = SfntTag.make(scriptTag)
            shapingEngine.languageTag = SfntTag.make(languageTag)
            shapingEngine.openTypeFeatures = openTypeFeatures
            shapingEngine.writingDirection = writingDirection
            shapingEngine.shapingOrder = shapingOrder

            return shapingEngine.shapeText(text, startIndex, endIndex)
        }
    }

    val typeface = DEFAULT_TYPEFACE
    val typeSize: Float = DEFAULT_TYPE_SIZE
    val scriptTag = DEFAULT_SCRIPT_TAG
    val languageTag = DEFAULT_LANGUAGE_TAG
    val openTypeFeatures: Set<OpenTypeFeature> = DEFAULT_OPEN_TYPE_FEATURES
    val writingDirection = DEFAULT_WRITING_DIRECTION
    var shapingOrder = DEFAULT_SHAPING_ORDER
    val text = DEFAULT_TEXT
    var startIndex = 0
    var endIndex = DEFAULT_TEXT.length

    init {
        onPreBuildSubject = { builder ->
            val subjectBuilder = builder as ShapingResultBuilder
            subjectBuilder.typeface = typeface
            subjectBuilder.typeSize = typeSize
            subjectBuilder.scriptTag = scriptTag
            subjectBuilder.languageTag = languageTag
            subjectBuilder.openTypeFeatures = openTypeFeatures
            subjectBuilder.writingDirection = writingDirection
            subjectBuilder.shapingOrder = shapingOrder
            subjectBuilder.text = text
            subjectBuilder.startIndex = startIndex
            subjectBuilder.endIndex = endIndex
        }
    }

    @Test
    fun testIsBackwardForForwardShapingOrder() {
        // Given
        shapingOrder = ShapingOrder.FORWARD

        buildSubject({ subject ->
            assertFalse(subject.isBackward)
        })
    }


    @Test
    fun testGetGlyphIds() {
        buildSubject { subject ->
            // When
            val glyphIds = subject.glyphIds

            // Then
            assertNotNull(glyphIds)
            assertEquals(glyphIds, DEFAULT_GLYPH_IDS)
        }
    }


    @Test
    fun testGetGlyphOffsets() {
        buildSubject { subject ->
            // When
            val glyphOffsets = subject.glyphOffsets

            // Then
            assertNotNull(glyphOffsets)
            assertEquals(glyphOffsets, DEFAULT_GLYPH_OFFSETS)
        }
    }


    @Test
    fun testGetGlyphAdvances() {
        buildSubject { subject ->
            // When
            val glyphAdvances = subject.glyphAdvances

            // Then
            assertNotNull(glyphAdvances)
            assertEquals(glyphAdvances, DEFAULT_GLYPH_ADVANCES)
        }
    }

    @Test
    fun testGetClusterMap() {
        buildSubject { subject ->
            // When
            val clusterMap = subject.clusterMap

            // Then
            assertNotNull(clusterMap)
            assertEquals(clusterMap, DEFAULT_CLUSTER_MAP)
        }
    }

    @Test
    fun testGetCaretEdges() {
        buildSubject({ subject ->
            val spied = spy(subject)

            // When
            val caretEdges = spied.getCaretEdges()

            // Then
            verify(spied).getCaretEdges(null)
            assertNotNull(caretEdges)
            assertEquals(caretEdges.size, subject.charEnd - subject.charStart + 1)
        })
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGetCaretEdgesForLessCaretStops() {
        buildSubject({ subject ->
            // Given
            val caretStops = BooleanArray(subject.glyphCount - 1)

            // When
            subject.getCaretEdges(caretStops)
        })
    }

    @Test
    fun testGetCaretEdgesWithCaretStops() {
        buildSubject({ subject ->
            // Given
            val caretStops = BooleanArray((subject.charEnd - subject.charStart))
            Arrays.fill(caretStops, true)

            // When
            val withStops = subject.getCaretEdges(caretStops)
            val withoutStops = subject.getCaretEdges(null)

            // Then: a stop before every code unit is the same as having no stops at all.
            assertEquals((subject.charEnd - subject.charStart) + 1, withStops.size)
            assertArrayEquals(withoutStops, withStops, 0.0f)
        })
    }

    @Test
    fun testToString() {
        buildSubject({ subject ->
            val description = DescriptionBuilder
                    .of(ShapingResult::class.java)
                    .put("isBackward", subject.isBackward)
                    .put("charStart", subject.charStart)
                    .put("charEnd", subject.charEnd)
                    .put("glyphCount", subject.glyphCount)
                    .put("glyphIds", subject.glyphIds)
                    .put("glyphOffsets", subject.glyphOffsets)
                    .put("glyphAdvances", subject.glyphAdvances)
                    .put("clusterMap", subject.clusterMap)
                    .build()

            // When
            val string = subject.toString()

            // Then
            assertEquals(string, description)
        })
    }

    @Test
    fun testResultsForFullText() {
        buildSubject({ subject ->
            assertFalse(subject.isBackward)
            assertEquals(subject.charStart, startIndex)
            assertEquals(subject.charEnd, endIndex)
            assertEquals(subject.glyphCount, text.length)
            assertEquals(subject.glyphIds, DEFAULT_GLYPH_IDS)
            assertEquals(subject.glyphOffsets, DEFAULT_GLYPH_OFFSETS)
            assertEquals(subject.glyphAdvances, DEFAULT_GLYPH_ADVANCES)
            assertEquals(subject.clusterMap, DEFAULT_CLUSTER_MAP)
            assertArrayEquals(subject.getCaretEdges(), DEFAULT_CARET_EDGES, 0.0f)
        })
    }

    @Test
    fun testResultsForFirstWord() {
        // Given
        startIndex = 0
        endIndex = 4

        buildSubject({ subject ->
            // Then
            assertFalse(subject.isBackward)
            assertEquals(subject.charStart, startIndex)
            assertEquals(subject.charEnd, endIndex)
            assertEquals(subject.glyphCount, endIndex - startIndex)
            assertEquals(subject.glyphIds, DEFAULT_GLYPH_IDS.subList(startIndex, endIndex))
            assertEquals(subject.glyphOffsets, DEFAULT_GLYPH_OFFSETS.subList(startIndex, endIndex))
            assertEquals(subject.glyphAdvances, DEFAULT_GLYPH_ADVANCES.subList(startIndex, endIndex))
            assertEquals(subject.clusterMap, DEFAULT_CLUSTER_MAP.subList(0, endIndex - startIndex))
        })
    }

    @Test
    fun testResultsForMidWord() {
        // Given
        startIndex = 5
        endIndex = 8

        buildSubject({ subject ->
            // Then
            assertFalse(subject.isBackward)
            assertEquals(subject.charStart, startIndex)
            assertEquals(subject.charEnd, endIndex)
            assertEquals(subject.glyphCount, endIndex - startIndex)
            assertEquals(subject.glyphIds, DEFAULT_GLYPH_IDS.subList(startIndex, endIndex))
            assertEquals(subject.glyphOffsets, DEFAULT_GLYPH_OFFSETS.subList(startIndex, endIndex))
            assertEquals(subject.glyphAdvances, DEFAULT_GLYPH_ADVANCES.subList(startIndex, endIndex))
            assertEquals(subject.clusterMap, DEFAULT_CLUSTER_MAP.subList(0, endIndex - startIndex))
        })
    }

    @Test
    fun testResultsForLastWord() {
        // Given
        startIndex = 9
        endIndex = 12

        buildSubject({ subject ->
            // Then
            assertFalse(subject.isBackward)
            assertEquals(subject.charStart, startIndex)
            assertEquals(subject.charEnd, endIndex)
            assertEquals(subject.glyphCount, endIndex - startIndex)
            assertEquals(subject.glyphIds, DEFAULT_GLYPH_IDS.subList(startIndex, endIndex))
            assertEquals(subject.glyphOffsets, DEFAULT_GLYPH_OFFSETS.subList(startIndex, endIndex))
            assertEquals(subject.glyphAdvances, DEFAULT_GLYPH_ADVANCES.subList(startIndex, endIndex))
            assertEquals(subject.clusterMap, DEFAULT_CLUSTER_MAP.subList(0, endIndex - startIndex))
        })
    }
}

