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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.mta.tehreer.DisposableTestSuite;
import com.mta.tehreer.subject.UnsafeSubjectBuilder;
import com.mta.tehreer.util.DescriptionBuilder;

import org.junit.Test;

import java.util.List;

public abstract class BidiLineTestSuite extends DisposableTestSuite<BidiLine, BidiLine.Finalizable> {
    private static final String DEFAULT_TEXT = "abcdابجد";
    private static final BidiRun[] DEFAULT_VISUAL_RUNS = {
        new BidiRun(0, 4, (byte) 0),
        new BidiRun(4, 8, (byte) 1),
    };

    protected static class BidiLineBuilder extends UnsafeSubjectBuilder<BidiLine, BidiLine.Finalizable> {
        String text = DEFAULT_TEXT;
        int startIndex = 0;
        int endIndex = text.length();
        byte baseLevel = 0;

        protected BidiLineBuilder() {
            super(BidiLine.class, BidiLine.Finalizable.class);
        }

        @Override
        public BidiLine buildSubject() {
            BidiAlgorithm bidiAlgorithm = BidiAlgorithm.finalizable(new BidiAlgorithm(text));
            BidiParagraph bidiParagraph = BidiParagraph.finalizable(
                    bidiAlgorithm.createParagraph(0, text.length(), baseLevel));

            return bidiParagraph.createLine(startIndex, endIndex);
        }
    }

    protected String text = DEFAULT_TEXT;
    protected int startIndex = 0;
    protected int endIndex = text.length();
    protected byte baseLevel = 0;

    protected BidiLineTestSuite(DisposableTestSuite.DefaultMode defaultMode) {
        super(new BidiLineBuilder(), defaultMode);

        setOnPreBuildSubject((builder) -> {
            BidiLineBuilder subjectBuilder = (BidiLineBuilder) builder;
            subjectBuilder.text = text;
            subjectBuilder.startIndex = startIndex;
            subjectBuilder.endIndex = endIndex;
            subjectBuilder.baseLevel = baseLevel;
        });
    }

    @Test
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
        });
    }

    @Test
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
        });
    }

    @Test
    public void testGetRunCount() {
        buildSubject((subject) -> {
            // When
            int runCount = subject.getRunCount();

            // Then
            assertEquals(runCount, 2);
        });
    }

    @Test
    public void testGetVisualRunForFirstIndex() {
        buildSubject((subject) -> {
            // When
            BidiRun run = subject.getVisualRun(0);

            // Then
            assertNotNull(run);
            assertEquals(run.charStart, 0);
            assertEquals(run.charEnd, 4);
            assertEquals(run.embeddingLevel, 0);
        });
    }

    @Test
    public void testGetVisualRunForLastIndex() {
        buildSubject((subject) -> {
            // When
            BidiRun run = subject.getVisualRun(1);

            // Then
            assertNotNull(run);
            assertEquals(run.charStart, 4);
            assertEquals(run.charEnd, 8);
            assertEquals(run.embeddingLevel, 1);
        });
    }

    @Test
    public void testGetVisualRuns() {
        buildSubject((subject) -> {
            // When
            List<BidiRun> visualRuns = subject.getVisualRuns();

            // Then
            assertTrue(visualRuns instanceof BidiLine.RunList);
            assertSame(((BidiLine.RunList) visualRuns).owner, subject);
            assertSame(((BidiLine.RunList) visualRuns).size, subject.getRunCount());
            assertArrayEquals(visualRuns.toArray(new BidiRun[0]), DEFAULT_VISUAL_RUNS);
        });
    }

    @Test
    public void testGetMirroringPairs() {
        buildSubject((subject) -> {
            // When
            Iterable<BidiPair> mirroringPairs = subject.getMirroringPairs();

            // Then
            assertTrue(mirroringPairs instanceof BidiLine.MirrorIterable);
            assertSame(((BidiLine.MirrorIterable) mirroringPairs).owner, subject);
        });
    }

    @Test
    public void testToString() {
        buildSubject((subject) -> {
            String description = DescriptionBuilder
                    .of(BidiLine.class)
                    .put("charStart", startIndex)
                    .put("charEnd", endIndex)
                    .put("visualRuns", DEFAULT_VISUAL_RUNS)
                    .put("mirroringPairs", subject.getMirroringPairs())
                    .build();

            // When
            String string = subject.toString();

            // Then
            assertEquals(string, description);
        });
    }
}
