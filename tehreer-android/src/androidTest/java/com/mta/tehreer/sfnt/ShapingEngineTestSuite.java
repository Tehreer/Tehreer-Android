/*
 * Copyright (C) 2022 Muhammad Tayyab Akram
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
import com.mta.tehreer.sut.UnsafeSUTBuilder;
import com.mta.tehreer.util.DescriptionBuilder;
import com.mta.tehreer.util.TypefaceStore;

import org.junit.Test;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class ShapingEngineTestSuite extends DisposableTestSuite<ShapingEngine, ShapingEngine.Finalizable> {
    private static final String DEFAULT_TEXT = "abcd";

    protected static class ShapingEngineBuilder extends UnsafeSUTBuilder<ShapingEngine, ShapingEngine.Finalizable> {
        Typeface typeface;

        protected ShapingEngineBuilder() {
            super(ShapingEngine.class, ShapingEngine.Finalizable.class);
        }

        @Override
        public ShapingEngine buildSUT() {
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

        setOnPreBuildSUT((builder) -> {
            ShapingEngineBuilder sutBuilder = (ShapingEngineBuilder) builder;
            sutBuilder.typeface = typeface;
        });
    }

    @Test
    public void testNativePointers() {
        buildSUT((sut) -> {
            assertNotEquals(sut.nativeEngine, 0);
        });
    }

    @Test
    public void testInitialPropertyValues() {
        buildSUT((sut) -> {
            assertNull(sut.getTypeface());
            assertEquals(sut.getTypeSize(), 16.0f, 0.0f);
            assertEquals(sut.getScriptTag(), SfntTag.make("DFLT"));
            assertEquals(sut.getLanguageTag(), SfntTag.make("dflt"));
            assertTrue(sut.getOpenTypeFeatures().isEmpty());
            assertEquals(sut.getWritingDirection(), WritingDirection.LEFT_TO_RIGHT);
            assertEquals(sut.getShapingOrder(), ShapingOrder.FORWARD);
        });
    }

    @Test
    public void testTypeSizePropertyForNegativeValue() {
        buildSUT((sut) -> {
            assertThrows(IllegalArgumentException.class,
                         () -> sut.setTypeSize(-1.0f));
        });
    }

    @Test
    public void testTypeSizePropertyForPositiveValue() {
        buildSUT((sut) -> {
            // Given
            float typeSize = 64.0f;

            // When
            sut.setTypeSize(typeSize);

            // Then
            assertEquals(sut.getTypeSize(), typeSize, 0.0f);
        });
    }

    @Test
    public void testTypeSizePropertyForFractionalValue() {
        buildSUT((sut) -> {
            // Given
            float typeSize = 100.0f / 3.0f;

            // When
            sut.setTypeSize(typeSize);

            // Then
            assertEquals(sut.getTypeSize(), typeSize, 0.0f);
        });
    }

    @Test
    public void testScriptTagProperty() {
        buildSUT((sut) -> {
            // Given
            int scriptTag = SfntTag.make("arab");

            // When
            sut.setScriptTag(scriptTag);

            // Then
            assertEquals(sut.getScriptTag(), scriptTag);
        });
    }

    @Test
    public void testLanguageTagProperty() {
        buildSUT((sut) -> {
            // Given
            int languageTag = SfntTag.make("URD ");

            // When
            sut.setLanguageTag(languageTag);

            // Then
            assertEquals(sut.getLanguageTag(), languageTag);
        });
    }

    @Test(expected = NullPointerException.class)
    public void testOpenTypeFeaturesPropertyForNullSet() {
        buildSUT((sut) -> {
            // Given
            Set<OpenTypeFeature> features = null;

            // When
            sut.setOpenTypeFeatures(features);
        });
    }

    @Test
    public void testOpenTypeFeaturesPropertyForEmptySet() {
        buildSUT((sut) -> {
            // Given
            Set<OpenTypeFeature> features = Collections.emptySet();

            // When
            sut.setOpenTypeFeatures(features);

            // Then
            assertTrue(sut.getOpenTypeFeatures().isEmpty());
        });
    }

    @Test
    public void testOpenTypeFeaturesPropertyForCustomValues() {
        buildSUT((sut) -> {
            // Given
            Set<OpenTypeFeature> features = new LinkedHashSet<>();
            features.add(OpenTypeFeature.of(SfntTag.make("aalt"), 3));
            features.add(OpenTypeFeature.of(SfntTag.make("liga"), 0));
            features.add(OpenTypeFeature.of(SfntTag.make("cswh"), 1));

            // When
            sut.setOpenTypeFeatures(features);

            // Then
            assertEquals(sut.getOpenTypeFeatures(), features);
        });
    }

    @Test(expected = NullPointerException.class)
    public void testWritingDirectionPropertyForNullValue() {
        buildSUT((sut) -> {
            // Given
            WritingDirection writingDirection = null;

            // When
            sut.setWritingDirection(writingDirection);
        });
    }

    @Test
    public void testWritingDirectionPropertyForValidValue() {
        buildSUT((sut) -> {
            // Given
            WritingDirection writingDirection = WritingDirection.RIGHT_TO_LEFT;

            // When
            sut.setWritingDirection(writingDirection);

            // Then
            assertEquals(sut.getWritingDirection(), writingDirection);
        });
    }

    @Test(expected = NullPointerException.class)
    public void testShapingOrderPropertyForNullValue() {
        buildSUT((sut) -> {
            // Given
            ShapingOrder shapingOrder = null;

            // When
            sut.setShapingOrder(shapingOrder);
        });
    }

    @Test
    public void testShapingOrderPropertyForValidValue() {
        buildSUT((sut) -> {
            // Given
            ShapingOrder shapingOrder = ShapingOrder.BACKWARD;

            // When
            sut.setShapingOrder(shapingOrder);

            // Then
            assertEquals(sut.getShapingOrder(), shapingOrder);
        });
    }

    @Test(expected = IllegalStateException.class)
    public void testShapeTextWithoutSpecifyingTypeface() {
        buildSUT((sut) -> {
            // When
            sut.shapeText(text, 0, text.length());
        });
    }

    @Test(expected = NullPointerException.class)
    public void testShapeTextForNullString() {
        // Given
        typeface = TypefaceStore.getNafeesWeb();
        text = null;

        buildSUT((sut) -> {
            // When
            sut.shapeText(text, 0, text.length());
        });
    }

    @Test
    public void testShapeTextForInvalidRanges() {
        typeface = TypefaceStore.getNafeesWeb();

        buildSUT((sut) -> {
            int length = text.length();

            // Invalid Start
            assertThrows(IllegalArgumentException.class, "From Index: -1",
                         () -> sut.shapeText(text, -1, length));

            // Invalid End
            assertThrows(IllegalArgumentException.class,
                         String.format("To Index: %d, Text Length: %d", length + 1, length),
                         () -> sut.shapeText(text, 0, length + 1));

            // Empty Range
            assertThrows(IllegalArgumentException.class, "Bad Range: [1, 0)",
                         () -> sut.shapeText(text, 1, 0));
        });
    }

    @Test
    public void testShapeTextForFullRange() {
        typeface = TypefaceStore.getNafeesWeb();

        buildSUT((sut) -> {
            // When
            ShapingResult result = sut.shapeText(text, 0, text.length());

            // Then
            assertNotNull(result);
        });
    }

    @Test
    public void testToString() {
        buildSUT((sut) -> {
            String description = DescriptionBuilder
                    .of(ShapingEngine.class)
                    .put("typeface", sut.getTypeface())
                    .put("typeSize", sut.getTypeSize())
                    .put("scriptTag", SfntTag.toString(sut.getScriptTag()))
                    .put("languageTag", SfntTag.toString(sut.getLanguageTag()))
                    .put("openTypeFeatures", sut.getOpenTypeFeatures())
                    .put("writingDirection", sut.getWritingDirection())
                    .put("shapingOrder", sut.getShapingOrder())
                    .build();

            // When
            String string = sut.toString();

            // Then
            assertEquals(string, description);
        });
    }
}
