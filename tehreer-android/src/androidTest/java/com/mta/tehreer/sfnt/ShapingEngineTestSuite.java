/*
 * Copyright (C) 2022-2023 Muhammad Tayyab Akram
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

package com.mta.tehreer.sfnt;

import static com.mta.tehreer.util.Assert.assertThrows;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.mta.tehreer.DisposableTestSuite;
import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.subject.UnsafeSubjectBuilder;
import com.mta.tehreer.util.DescriptionBuilder;
import com.mta.tehreer.util.TypefaceStore;

import org.junit.Test;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class ShapingEngineTestSuite extends DisposableTestSuite<ShapingEngine, ShapingEngine.Finalizable> {
    private static final String DEFAULT_TEXT = "abcd";

    protected static class ShapingEngineBuilder extends UnsafeSubjectBuilder<ShapingEngine, ShapingEngine.Finalizable> {
        Typeface typeface;

        protected ShapingEngineBuilder() {
            super(ShapingEngine.class, ShapingEngine.Finalizable.class);
        }

        @Override
        public ShapingEngine buildSubject() {
            ShapingEngine shapingEngine = new ShapingEngine();
            if (typeface != null) {
                shapingEngine.setTypeface(typeface);
            }

            return shapingEngine;
        }
    }

    protected String text = DEFAULT_TEXT;
    protected Typeface typeface;

    protected ShapingEngineTestSuite(DefaultMode defaultMode) {
        super(new ShapingEngineBuilder(), defaultMode);

        setOnPreBuildSubject((builder) -> {
            ShapingEngineBuilder subjectBuilder = (ShapingEngineBuilder) builder;
            subjectBuilder.typeface = typeface;
        });
    }

    @Test
    public void testNativePointers() {
        buildSubject((subject) -> {
            assertNotEquals(subject.nativeEngine, 0);
        });
    }

    @Test
    public void testInitialPropertyValues() {
        buildSubject((subject) -> {
            assertNull(subject.getTypeface());
            assertEquals(subject.getTypeSize(), 16.0f, 0.0f);
            assertEquals(subject.getScriptTag(), SfntTag.make("DFLT"));
            assertEquals(subject.getLanguageTag(), SfntTag.make("dflt"));
            assertTrue(subject.getOpenTypeFeatures().isEmpty());
            assertEquals(subject.getWritingDirection(), WritingDirection.LEFT_TO_RIGHT);
            assertEquals(subject.getShapingOrder(), ShapingOrder.FORWARD);
        });
    }

    @Test
    public void testTypeSizePropertyForNegativeValue() {
        buildSubject((subject) -> {
            assertThrows(IllegalArgumentException.class,
                         () -> subject.setTypeSize(-1.0f));
        });
    }

    @Test
    public void testTypeSizePropertyForPositiveValue() {
        buildSubject((subject) -> {
            // Given
            float typeSize = 64.0f;

            // When
            subject.setTypeSize(typeSize);

            // Then
            assertEquals(subject.getTypeSize(), typeSize, 0.0f);
        });
    }

    @Test
    public void testTypeSizePropertyForFractionalValue() {
        buildSubject((subject) -> {
            // Given
            float typeSize = 100.0f / 3.0f;

            // When
            subject.setTypeSize(typeSize);

            // Then
            assertEquals(subject.getTypeSize(), typeSize, 0.0f);
        });
    }

    @Test
    public void testScriptTagProperty() {
        buildSubject((subject) -> {
            // Given
            int scriptTag = SfntTag.make("arab");

            // When
            subject.setScriptTag(scriptTag);

            // Then
            assertEquals(subject.getScriptTag(), scriptTag);
        });
    }

    @Test
    public void testLanguageTagProperty() {
        buildSubject((subject) -> {
            // Given
            int languageTag = SfntTag.make("URD ");

            // When
            subject.setLanguageTag(languageTag);

            // Then
            assertEquals(subject.getLanguageTag(), languageTag);
        });
    }

    @Test(expected = NullPointerException.class)
    public void testOpenTypeFeaturesPropertyForNullSet() {
        buildSubject((subject) -> {
            // Given
            Set<OpenTypeFeature> features = null;

            // When
            subject.setOpenTypeFeatures(features);
        });
    }

    @Test
    public void testOpenTypeFeaturesPropertyForEmptySet() {
        buildSubject((subject) -> {
            // Given
            Set<OpenTypeFeature> features = Collections.emptySet();

            // When
            subject.setOpenTypeFeatures(features);

            // Then
            assertTrue(subject.getOpenTypeFeatures().isEmpty());
        });
    }

    @Test
    public void testOpenTypeFeaturesPropertyForCustomValues() {
        buildSubject((subject) -> {
            // Given
            Set<OpenTypeFeature> features = new LinkedHashSet<>();
            features.add(OpenTypeFeature.of(SfntTag.make("aalt"), 3));
            features.add(OpenTypeFeature.of(SfntTag.make("liga"), 0));
            features.add(OpenTypeFeature.of(SfntTag.make("cswh"), 1));

            // When
            subject.setOpenTypeFeatures(features);

            // Then
            assertEquals(subject.getOpenTypeFeatures(), features);
        });
    }

    @Test(expected = NullPointerException.class)
    public void testWritingDirectionPropertyForNullValue() {
        buildSubject((subject) -> {
            // Given
            WritingDirection writingDirection = null;

            // When
            subject.setWritingDirection(writingDirection);
        });
    }

    @Test
    public void testWritingDirectionPropertyForValidValue() {
        buildSubject((subject) -> {
            // Given
            WritingDirection writingDirection = WritingDirection.RIGHT_TO_LEFT;

            // When
            subject.setWritingDirection(writingDirection);

            // Then
            assertEquals(subject.getWritingDirection(), writingDirection);
        });
    }

    @Test(expected = NullPointerException.class)
    public void testShapingOrderPropertyForNullValue() {
        buildSubject((subject) -> {
            // Given
            ShapingOrder shapingOrder = null;

            // When
            subject.setShapingOrder(shapingOrder);
        });
    }

    @Test
    public void testShapingOrderPropertyForValidValue() {
        buildSubject((subject) -> {
            // Given
            ShapingOrder shapingOrder = ShapingOrder.BACKWARD;

            // When
            subject.setShapingOrder(shapingOrder);

            // Then
            assertEquals(subject.getShapingOrder(), shapingOrder);
        });
    }

    @Test(expected = IllegalStateException.class)
    public void testShapeTextWithoutSpecifyingTypeface() {
        buildSubject((subject) -> {
            // When
            subject.shapeText(text, 0, text.length());
        });
    }

    @Test(expected = NullPointerException.class)
    public void testShapeTextForNullString() {
        // Given
        typeface = TypefaceStore.getNafeesWeb();
        text = null;

        buildSubject((subject) -> {
            // When
            subject.shapeText(text, 0, text.length());
        });
    }

    @Test
    public void testShapeTextForInvalidRanges() {
        typeface = TypefaceStore.getNafeesWeb();

        buildSubject((subject) -> {
            int length = text.length();

            // Invalid Start
            assertThrows(IllegalArgumentException.class, "From Index: -1",
                         () -> subject.shapeText(text, -1, length));

            // Invalid End
            assertThrows(IllegalArgumentException.class,
                         String.format("To Index: %d, Text Length: %d", length + 1, length),
                         () -> subject.shapeText(text, 0, length + 1));

            // Empty Range
            assertThrows(IllegalArgumentException.class, "Bad Range: [1, 0)",
                         () -> subject.shapeText(text, 1, 0));
        });
    }

    @Test
    public void testShapeTextForFullRange() {
        typeface = TypefaceStore.getNafeesWeb();

        buildSubject((subject) -> {
            // When
            ShapingResult result = subject.shapeText(text, 0, text.length());

            // Then
            assertNotNull(result);
        });
    }

    @Test
    public void testToString() {
        buildSubject((subject) -> {
            String description = DescriptionBuilder
                    .of(ShapingEngine.class)
                    .put("typeface", subject.getTypeface())
                    .put("typeSize", subject.getTypeSize())
                    .put("scriptTag", SfntTag.toString(subject.getScriptTag()))
                    .put("languageTag", SfntTag.toString(subject.getLanguageTag()))
                    .put("openTypeFeatures", subject.getOpenTypeFeatures())
                    .put("writingDirection", subject.getWritingDirection())
                    .put("shapingOrder", subject.getShapingOrder())
                    .build();

            // When
            String string = subject.toString();

            // Then
            assertEquals(string, description);
        });
    }
}
