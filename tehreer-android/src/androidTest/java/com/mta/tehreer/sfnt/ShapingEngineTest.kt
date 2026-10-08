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

import com.mta.tehreer.util.Assert.assertThrows
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue

import com.mta.tehreer.SubjectTestSuite
import com.mta.tehreer.graphics.Typeface
import com.mta.tehreer.subject.SubjectBuilder
import com.mta.tehreer.util.DescriptionBuilder
import com.mta.tehreer.util.TypefaceStore

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

import java.util.Collections
import java.util.LinkedHashSet

@RunWith(MockitoJUnitRunner::class)
class ShapingEngineTest : SubjectTestSuite<ShapingEngine>(ShapingEngineBuilder()) {
    private companion object {
        private val DEFAULT_TEXT: String = "abcd"
    }

    class ShapingEngineBuilder : SubjectBuilder<ShapingEngine> {
        var typeface: Typeface? = null

        override fun buildSubject(): ShapingEngine {
            val shapingEngine = ShapingEngine()
            if (typeface != null) {
                shapingEngine.typeface = typeface
            }

            return shapingEngine
        }
    }

    var text = DEFAULT_TEXT
    var typeface: Typeface? = null

    init {
        onPreBuildSubject = { builder ->
            val subjectBuilder = builder as ShapingEngineBuilder
            subjectBuilder.typeface = typeface
        }
    }

    @Test
    fun testNativePointers() {
        buildSubject({ subject ->
            assertNotEquals(subject.nativeEngine, 0)
        })
    }

    @Test
    fun testInitialPropertyValues() {
        buildSubject({ subject ->
            assertNull(subject.typeface)
            assertEquals(subject.typeSize, 16.0f, 0.0f)
            assertEquals(subject.scriptTag, SfntTag.make("DFLT"))
            assertEquals(subject.languageTag, SfntTag.make("dflt"))
            assertTrue(subject.openTypeFeatures.isEmpty())
            assertEquals(subject.writingDirection, WritingDirection.LEFT_TO_RIGHT)
            assertEquals(subject.shapingOrder, ShapingOrder.FORWARD)
        })
    }

    @Test
    fun testTypeSizePropertyForNegativeValue() {
        buildSubject({ subject ->
            assertThrows(IllegalArgumentException::class.java,
                         { subject.typeSize = -1.0f })
        })
    }

    @Test
    fun testTypeSizePropertyForPositiveValue() {
        buildSubject({ subject ->
            // Given
            val typeSize: Float = 64.0f

            // When
            subject.typeSize = typeSize

            // Then
            assertEquals(subject.typeSize, typeSize, 0.0f)
        })
    }

    @Test
    fun testTypeSizePropertyForFractionalValue() {
        buildSubject({ subject ->
            // Given
            val typeSize: Float = 100.0f / 3.0f

            // When
            subject.typeSize = typeSize

            // Then
            assertEquals(subject.typeSize, typeSize, 0.0f)
        })
    }

    @Test
    fun testScriptTagProperty() {
        buildSubject({ subject ->
            // Given
            val scriptTag = SfntTag.make("arab")

            // When
            subject.scriptTag = scriptTag

            // Then
            assertEquals(subject.scriptTag, scriptTag)
        })
    }

    @Test
    fun testLanguageTagProperty() {
        buildSubject({ subject ->
            // Given
            val languageTag = SfntTag.make("URD ")

            // When
            subject.languageTag = languageTag

            // Then
            assertEquals(subject.languageTag, languageTag)
        })
    }

    @Test
    fun testOpenTypeFeaturesPropertyForEmptySet() {
        buildSubject({ subject ->
            // Given
            val features: Set<OpenTypeFeature> = Collections.emptySet()

            // When
            subject.openTypeFeatures = features

            // Then
            assertTrue(subject.openTypeFeatures.isEmpty())
        })
    }

    @Test
    fun testOpenTypeFeaturesPropertyForCustomValues() {
        buildSubject({ subject ->
            // Given
            val features = LinkedHashSet<OpenTypeFeature>()
            features.add(OpenTypeFeature.of(SfntTag.make("aalt"), 3))
            features.add(OpenTypeFeature.of(SfntTag.make("liga"), 0))
            features.add(OpenTypeFeature.of(SfntTag.make("cswh"), 1))

            // When
            subject.openTypeFeatures = features

            // Then
            assertEquals(subject.openTypeFeatures, features)
        })
    }

    @Test
    fun testWritingDirectionPropertyForValidValue() {
        buildSubject({ subject ->
            // Given
            val writingDirection = WritingDirection.RIGHT_TO_LEFT

            // When
            subject.writingDirection = writingDirection

            // Then
            assertEquals(subject.writingDirection, writingDirection)
        })
    }

    @Test
    fun testShapingOrderPropertyForValidValue() {
        buildSubject({ subject ->
            // Given
            val shapingOrder = ShapingOrder.BACKWARD

            // When
            subject.shapingOrder = shapingOrder

            // Then
            assertEquals(subject.shapingOrder, shapingOrder)
        })
    }

    @Test(expected = IllegalStateException::class)
    fun testShapeTextWithoutSpecifyingTypeface() {
        buildSubject({ subject ->
            // When
            subject.shapeText(text, 0, text.length)
        })
    }

    @Test
    fun testShapeTextForInvalidRanges() {
        typeface = TypefaceStore.nafeesWeb

        buildSubject({ subject ->
            val length = text.length

            // Invalid Start
            assertThrows(IllegalArgumentException::class.java, "From Index: -1",
                         { subject.shapeText(text, -1, length) })

            // Invalid End
            assertThrows(IllegalArgumentException::class.java,
                         String.format("To Index: %d, Text Length: %d", length + 1, length),
                         { subject.shapeText(text, 0, length + 1) })

            // Empty Range
            assertThrows(IllegalArgumentException::class.java, "Bad Range: [1, 0)",
                         { subject.shapeText(text, 1, 0) })
        })
    }

    @Test
    fun testShapeTextForFullRange() {
        typeface = TypefaceStore.nafeesWeb

        buildSubject({ subject ->
            // When
            val result = subject.shapeText(text, 0, text.length)

            // Then
            assertNotNull(result)
        })
    }

    @Test
    fun testToString() {
        buildSubject({ subject ->
            val description = DescriptionBuilder
                    .of(ShapingEngine::class.java)
                    .put("typeface", subject.typeface)
                    .put("typeSize", subject.typeSize)
                    .put("scriptTag", SfntTag.toString(subject.scriptTag))
                    .put("languageTag", SfntTag.toString(subject.languageTag))
                    .put("openTypeFeatures", subject.openTypeFeatures)
                    .put("writingDirection", subject.writingDirection)
                    .put("shapingOrder", subject.shapingOrder)
                    .build()

            // When
            val string = subject.toString()

            // Then
            assertEquals(string, description)
        })
    }

    @Test
    fun testGetScriptDirectionForArabic() {
        // Given
        val scriptTag = SfntTag.make("arab")

        // When
        val writingDirection = ShapingEngine.getScriptDirection(scriptTag)

        // Then
        assertEquals(writingDirection, WritingDirection.RIGHT_TO_LEFT)
    }

    @Test
    fun testGetScriptDirectionForLatin() {
        // Given
        val scriptTag = SfntTag.make("latn")

        // When
        val writingDirection = ShapingEngine.getScriptDirection(scriptTag)

        // Then
        assertEquals(writingDirection, WritingDirection.LEFT_TO_RIGHT)
    }
}
