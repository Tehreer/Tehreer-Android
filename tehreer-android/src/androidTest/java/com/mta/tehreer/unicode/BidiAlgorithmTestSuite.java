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

package com.mta.tehreer.unicode;

import static com.mta.tehreer.unicode.BidiClass.ARABIC_LETTER;
import static com.mta.tehreer.unicode.BidiClass.LEFT_TO_RIGHT;
import static com.mta.tehreer.util.Assert.assertThrows;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.annotation.NonNull;

import com.mta.tehreer.collections.IntList;
import com.mta.tehreer.internal.collections.UInt8BufferIntList;
import com.mta.tehreer.DisposableTestSuite;
import com.mta.tehreer.sut.UnsafeSUTBuilder;

import org.junit.Test;

public abstract class BidiAlgorithmTestSuite extends DisposableTestSuite<BidiAlgorithm, BidiAlgorithm.Finalizable> {
    private static final String DEFAULT_TEXT = "abcdابجد";

    private static class BidiAlgorithmBuilder extends UnsafeSUTBuilder<BidiAlgorithm, BidiAlgorithm.Finalizable> {
        String text = DEFAULT_TEXT;

        protected BidiAlgorithmBuilder() {
            super(BidiAlgorithm.class, BidiAlgorithm.Finalizable.class);
        }

        @Override
        public BidiAlgorithm buildSUT() {
            return new BidiAlgorithm(text);
        }
    }

    protected String text = DEFAULT_TEXT;
    protected @BidiClass.Value int[] values = {
        LEFT_TO_RIGHT, LEFT_TO_RIGHT, LEFT_TO_RIGHT, LEFT_TO_RIGHT,
        ARABIC_LETTER, ARABIC_LETTER, ARABIC_LETTER, ARABIC_LETTER,
    };

    protected BidiAlgorithmTestSuite(DefaultMode defaultMode) {
        super(new BidiAlgorithmBuilder(), defaultMode);

        setOnPreBuildSUT((builder) -> {
            BidiAlgorithmBuilder sutBuilder = (BidiAlgorithmBuilder) builder;
            sutBuilder.text = text;
        });
    }

    public static class StaticTest extends StaticTestSuite<BidiAlgorithm, BidiAlgorithm.Finalizable> {
        public StaticTest() {
            super(new BidiAlgorithmBuilder());
        }

        @Test
        public void testMaxLevel() {
            assertEquals(BidiAlgorithm.MAX_LEVEL, 125);
        }
    }

    public static class DisposableTest extends GeneralTestSuite {
        public DisposableTest() {
            super(DefaultMode.DISPOSABLE);
        }

        @Test
        public void testConstructorForNullText() {
            // Given
            text = null;

            // Then
            assertThrows(NullPointerException.class, "text",
                         () -> buildSUT((sut) -> { }));
        }

        @Test
        public void testConstructorForEmptyText() {
            // Given
            text = "";

            // Then
            assertThrows(IllegalArgumentException.class, "Text is empty",
                         () -> buildSUT((sut) -> { }));
        }
    }

    public static class FinalizableTest extends GeneralTestSuite {
        public FinalizableTest() {
            super(DefaultMode.SAFE);
        }
    }

    public static abstract class GeneralTestSuite extends BidiAlgorithmTestSuite {
        protected GeneralTestSuite(DefaultMode defaultMode) {
            super(defaultMode);
        }

        @Test
        public void testNativePointers() {
            buildSUT((sut) -> {
                assertNotEquals(sut.nativeBuffer, 0);
                assertNotEquals(sut.nativeAlgorithm, 0);
            });
        }

        @Test
        public void testGetCharBidiClasses() {
            buildSUT((sut) -> {
                // When
                IntList bidiClasses = sut.getCharBidiClasses();

                // Then
                assertTrue(bidiClasses instanceof UInt8BufferIntList);
                assertEquals(bidiClasses.size(), text.length());
                assertArrayEquals(bidiClasses.toArray(), values);
            });
        }

        private interface RangeConsumer {
            void accept(int startIndex, int endIndex);
        }

        private void testRangeExceptions(@NonNull RangeConsumer consumer) {
            // Invalid Start
            assertThrows(IllegalArgumentException.class, "Char Start: -1",
                         () -> consumer.accept(-1, 8));

            // Invalid End
            assertThrows(IllegalArgumentException.class, "Char End: 9, Text Length: 8",
                         () -> consumer.accept(0, 9));

            // Empty Range
            assertThrows(IllegalArgumentException.class, "Bad Range: [0, 0)",
                         () -> consumer.accept(0, 0));
        }

        @Test
        public void testGetParagraphBoundaryForInvalidRange() {
            buildSUT((sut) -> {
                testRangeExceptions(sut::getParagraphBoundary);
            });
        }

        @Test
        public void testGetParagraphBoundaryForFullRange() {
            buildSUT((sut) -> {
                // Given
                int startIndex = 0;
                int endIndex = text.length();

                // When
                int paragraphBoundary = sut.getParagraphBoundary(startIndex, endIndex);

                // Then
                assertEquals(paragraphBoundary, endIndex);
            });
        }

        @Test
        public void testCreateParagraphWithBaseDirectionForInvalidRange() {
            buildSUT((sut) -> {
                testRangeExceptions((startIndex, endIndex) -> {
                    sut.createParagraph(startIndex, endIndex, BaseDirection.LEFT_TO_RIGHT);
                });
            });
        }

        @Test
        public void testCreateParagraphForFullRangeAndLTRBaseDirection() {
            buildSUT((sut) -> {
                // Given
                int startIndex = 0;
                int endIndex = text.length();
                BaseDirection baseDirection = BaseDirection.LEFT_TO_RIGHT;

                // When
                BidiParagraph paragraph = sut.createParagraph(startIndex, endIndex, baseDirection);

                // Then
                assertNotNull(paragraph);
            });
        }

        @Test
        public void testCreateParagraphWithBaseLevelForInvalidRange() {
            buildSUT((sut) -> {
                testRangeExceptions((startIndex, endIndex) -> {
                    sut.createParagraph(startIndex, endIndex, (byte) 0);
                });
            });
        }

        @Test
        public void testCreateParagraphForInvalidBaseLevel() {
            buildSUT((sut) -> {
                // Negative Value
                assertThrows(IllegalArgumentException.class, "Base Level: -1",
                             () -> sut.createParagraph(0, text.length(), (byte) -1));

                // > MAX Value
                assertThrows(IllegalArgumentException.class, "Base Level: 126",
                             () -> sut.createParagraph(0, text.length(), (byte) 126));
            });
        }

        @Test
        public void testCreateParagraphForFullRangeAndZeroBaseLevel() {
            buildSUT((sut) -> {
                // Given
                int startIndex = 0;
                int endIndex = text.length();
                byte baseLevel = 0;

                // When
                BidiParagraph paragraph = sut.createParagraph(startIndex, endIndex, baseLevel);

                // Then
                assertNotNull(paragraph);
            });
        }
    }
}
