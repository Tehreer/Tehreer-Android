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

import static com.mta.tehreer.util.Assert.assertThrows;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.mta.tehreer.DisposableTestSuite;
import com.mta.tehreer.collections.ByteList;
import com.mta.tehreer.internal.collections.Int8BufferByteList;
import com.mta.tehreer.subject.UnsafeSubjectBuilder;
import com.mta.tehreer.util.DescriptionBuilder;

import org.junit.Ignore;
import org.junit.Test;

import java.util.Iterator;

public abstract class BidiParagraphTestSuite extends DisposableTestSuite<BidiParagraph, BidiParagraph.Finalizable> {
    private static final String DEFAULT_TEXT = "abcdابجد";
    private static final byte[] DEFAULT_LEVELS = { 0, 0, 0, 0, 1, 1, 1, 1 };

    protected static class BidiParagraphBuilder extends UnsafeSubjectBuilder<BidiParagraph, BidiParagraph.Finalizable> {
        String text = DEFAULT_TEXT;
        int startIndex = 0;
        int endIndex = text.length();
        byte baseLevel = 0;

        protected BidiParagraphBuilder() {
            super(BidiParagraph.class, BidiParagraph.Finalizable.class);
        }

        @Override
        public BidiParagraph buildSubject() {
            BidiAlgorithm bidiAlgorithm = BidiAlgorithm.finalizable(new BidiAlgorithm(text));
            return bidiAlgorithm.createParagraph(startIndex, endIndex, baseLevel);
        }
    }

    protected String text = DEFAULT_TEXT;
    protected int startIndex = 0;
    protected int endIndex = text.length();
    protected byte baseLevel = 0;

    protected BidiParagraphTestSuite(DefaultMode defaultMode) {
        super(new BidiParagraphBuilder(), defaultMode);

        setOnPreBuildSubject((builder) -> {
            BidiParagraphBuilder subjectBuilder = (BidiParagraphBuilder) builder;
            subjectBuilder.text = text;
            subjectBuilder.startIndex = startIndex;
            subjectBuilder.endIndex = endIndex;
            subjectBuilder.baseLevel = baseLevel;
        });
    }

    @Test
    @Ignore
    public void testCreatorForLeftToRightHalf() {
        // Given
        startIndex = 0;
        endIndex = 4;
        baseLevel = 0;

        // When
        buildSubject((subject) -> {
            // Then
            assertEquals(subject.getCharStart(), startIndex);
            assertEquals(subject.getCharEnd(), endIndex);
            assertEquals(subject.getBaseLevel(), baseLevel);
        });
    }

    @Test
    public void testCreatorForRightToLeftHalf() {
        // Given
        startIndex = 4;
        endIndex = 8;
        baseLevel = 1;

        // When
        buildSubject((subject) -> {
            // Then
            assertEquals(subject.getCharStart(), startIndex);
            assertEquals(subject.getCharEnd(), endIndex);
            assertEquals(subject.getBaseLevel(), baseLevel);
        });
    }

    @Test
    @Ignore
    public void testCreatorForMixedDirectionHalf() {
        // Given
        startIndex = 2;
        endIndex = 6;
        baseLevel = 0;

        // When
        buildSubject((subject) -> {
            // Then
            assertEquals(subject.getCharStart(), startIndex);
            assertEquals(subject.getCharEnd(), endIndex);
            assertEquals(subject.getBaseLevel(), baseLevel);
        });
    }

    @Test
    public void testNativePointers() {
        buildSubject((subject) -> {
            assertNotEquals(subject.nativeBuffer, 0);
            assertNotEquals(subject.nativeParagraph, 0);
        });
    }

    @Test
    public void testGetCharLevels() {
        buildSubject((subject) -> {
            // When
            ByteList charLevels = subject.getCharLevels();

            // Then
            assertTrue(charLevels instanceof Int8BufferByteList);
            assertEquals(charLevels.size(), text.length());
            assertArrayEquals(charLevels.toArray(), DEFAULT_LEVELS);
        });
    }

    @Test
    public void testGetOnwardRunForLeftToRightRun() {
        buildSubject((subject) -> {
            // When
            BidiRun run = subject.getOnwardRun(0);

            // Then
            assertNotNull(run);
            assertEquals(run.charStart, 0);
            assertEquals(run.charEnd, 4);
            assertEquals(run.embeddingLevel, 0);
        });
    }

    @Test
    public void testGetOnwardRunForRightToLeftRun() {
        buildSubject((subject) -> {
            // When
            BidiRun run = subject.getOnwardRun(5);

            // Then
            assertNotNull(run);
            assertEquals(run.charStart, 5);
            assertEquals(run.charEnd, 8);
            assertEquals(run.embeddingLevel, 1);
        });
    }

    @Test
    public void testGetOnwardRunFromEndIndex() {
        buildSubject((subject) -> {
            // When
            BidiRun run = subject.getOnwardRun(8);

            // Then
            assertNull(run);
        });
    }

    @Test
    public void testGetLogicalRuns() {
        buildSubject((subject) -> {
            // When
            Iterator<BidiRun> iterator = subject.getLogicalRuns();

            // Then
            assertTrue(iterator instanceof BidiParagraph.RunIterator);
            assertSame(((BidiParagraph.RunIterator) iterator).owner, subject);
        });
    }

    @Test
    public void testCreateLineForInvalidRange() {
        buildSubject((subject) -> {
            // Invalid Start
            assertThrows(IllegalArgumentException.class,
                         "Char Start: -1, Paragraph Range: [0, 8)",
                         () -> subject.createLine(-1, 8));

            // Invalid End
            assertThrows(IllegalArgumentException.class,
                         "Char End: 9, Paragraph Range: [0, 8)",
                         () -> subject.createLine(0, 9));

            // Empty Range
            assertThrows(IllegalArgumentException.class, "Bad Range: [0, 0)",
                         () -> subject.createLine(0, 0));
        });
    }

    @Test
    public void testCreateLineForFullRange() {
        buildSubject((subject) -> {
            // When
            BidiLine line = subject.createLine(startIndex, endIndex);

            // Then
            assertNotNull(line);
        });
    }

    @Test
    public void testToString() {
        buildSubject((subject) -> {
            String description = DescriptionBuilder
                    .of(BidiParagraph.class)
                    .put("charStart", startIndex)
                    .put("charEnd", endIndex)
                    .put("baseLevel", baseLevel)
                    .put("charLevels", DEFAULT_LEVELS)
                    .put("logicalRuns", subject.getLogicalRuns())
                    .build();

            // When
            String string = subject.toString();

            // Then
            assertEquals(string, description);
        });
    }
}
