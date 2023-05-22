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
import com.mta.tehreer.subject.UnsafeSubjectBuilder;
import com.mta.tehreer.util.DescriptionBuilder;

import org.junit.Test;

public abstract class BidiAlgorithmTestSuite extends DisposableTestSuite<BidiAlgorithm, BidiAlgorithm.Finalizable> {
    private static final String DEFAULT_TEXT = "abcdابجد";

    protected static class BidiAlgorithmBuilder extends UnsafeSubjectBuilder<BidiAlgorithm, BidiAlgorithm.Finalizable> {
        String text = DEFAULT_TEXT;

        protected BidiAlgorithmBuilder() {
            super(BidiAlgorithm.class, BidiAlgorithm.Finalizable.class);
        }

        @Override
        public BidiAlgorithm buildSubject() {
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

        setOnPreBuildSubject((builder) -> {
            BidiAlgorithmBuilder subjectBuilder = (BidiAlgorithmBuilder) builder;
            subjectBuilder.text = text;
        });
    }

    @Test
    public void testNativePointers() {
        buildSubject((subject) -> {
            assertNotEquals(subject.nativeBuffer, 0);
            assertNotEquals(subject.nativeAlgorithm, 0);
        });
    }

    @Test
    public void testGetCharBidiClasses() {
        buildSubject((subject) -> {
            // When
            IntList bidiClasses = subject.getCharBidiClasses();

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
        buildSubject((subject) -> {
            testRangeExceptions (subject::getParagraphBoundary);
        });
    }

    @Test
    public void testGetParagraphBoundaryForFullRange() {
        buildSubject((subject) -> {
            // Given
            int startIndex = 0;
            int endIndex = text.length();

            // When
            int paragraphBoundary = subject.getParagraphBoundary(startIndex, endIndex);

            // Then
            assertEquals(paragraphBoundary, endIndex);
        });
    }

    @Test
    public void testCreateParagraphWithBaseDirectionForInvalidRange() {
        buildSubject((subject) -> {
            testRangeExceptions((startIndex, endIndex) -> {
                subject.createParagraph(startIndex, endIndex, BaseDirection.LEFT_TO_RIGHT);
            });
        });
    }

    @Test
    public void testCreateParagraphForFullRangeAndLTRBaseDirection() {
        buildSubject((subject) -> {
            // Given
            int startIndex = 0;
            int endIndex = text.length();
            BaseDirection baseDirection = BaseDirection.LEFT_TO_RIGHT;

            // When
            BidiParagraph paragraph = subject.createParagraph(startIndex, endIndex, baseDirection);

            // Then
            assertNotNull(paragraph);
        });
    }

    @Test
    public void testCreateParagraphWithBaseLevelForInvalidRange() {
        buildSubject((subject) -> {
            testRangeExceptions((startIndex, endIndex) -> {
                subject.createParagraph(startIndex, endIndex, (byte) 0);
            });
        });
    }

    @Test
    public void testCreateParagraphForInvalidBaseLevel() {
        buildSubject((subject) -> {
            // Negative Value
            assertThrows(IllegalArgumentException.class, "Base Level: -1",
                         () -> subject.createParagraph(0, text.length(), (byte) -1));

            // > MAX Value
            assertThrows(IllegalArgumentException.class, "Base Level: 126",
                         () -> subject.createParagraph(0, text.length(), (byte) 126));
        });
    }

    @Test
    public void testCreateParagraphForFullRangeAndZeroBaseLevel() {
        buildSubject((subject) -> {
            // Given
            int startIndex = 0;
            int endIndex = text.length();
            byte baseLevel = 0;

            // When
            BidiParagraph paragraph = subject.createParagraph(startIndex, endIndex, baseLevel);

            // Then
            assertNotNull(paragraph);
        });
    }

    @Test
    public void testToString() {
        buildSubject((subject) -> {
            String description = DescriptionBuilder
                    .of(BidiAlgorithm.class)
                    .put("text", text)
                    .put("charBidiClasses", values)
                    .build();

            // When
            String string = subject.toString();

            // Then
            assertEquals(string, description);
        });
    }
}
