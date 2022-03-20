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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import com.mta.tehreer.DisposableTestSuite;
import com.mta.tehreer.sut.UnsafeSUTBuilder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

public abstract class BidiLineTestSuite extends DisposableTestSuite<BidiLine, BidiLine.Finalizable> {
    private static final String DEFAULT_TEXT = "abcdابجد";
    private static final BidiRun[] DEFAULT_VISUAL_RUNS = {
        new BidiRun(0, 4, (byte) 0),
        new BidiRun(4, 8, (byte) 1),
    };

    private static class BidiLineBuilder extends UnsafeSUTBuilder<BidiLine, BidiLine.Finalizable> {
        String text = DEFAULT_TEXT;
        int startIndex = 0;
        int endIndex = text.length();
        byte baseLevel = 0;

        protected BidiLineBuilder() {
            super(BidiLine.class, BidiLine.Finalizable.class);
        }

        @Override
        public BidiLine buildSUT() {
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

        setOnPreBuildSUT((builder) -> {
            BidiLineBuilder sutBuilder = (BidiLineBuilder) builder;
            sutBuilder.text = text;
            sutBuilder.startIndex = startIndex;
            sutBuilder.endIndex = endIndex;
            sutBuilder.baseLevel = baseLevel;
        });
    }

    public static class StaticTest extends DisposableTestSuite.StaticTestSuite<BidiLine, BidiLine.Finalizable> {
        public StaticTest() {
            super(new BidiLineBuilder());
        }
    }

    public static class DisposableTest extends GeneralTestSuite {
        public DisposableTest() {
            super(DefaultMode.DISPOSABLE);
        }
    }

    public static class FinalizableTest extends GeneralTestSuite {
        public FinalizableTest() {
            super(DefaultMode.SAFE);
        }
    }

    public static abstract class GeneralTestSuite extends BidiLineTestSuite {
        protected GeneralTestSuite(DefaultMode defaultMode) {
            super(defaultMode);
        }

        @Test
        public void testCreatorForLeftToRightHalf() {
            // Given
            startIndex = 0;
            endIndex = 4;
            baseLevel = 0;

            // When
            buildSUT((sut) -> {
                // Then
                assertEquals(sut.getCharStart(), startIndex);
                assertEquals(sut.getCharEnd(), endIndex);
            });
        }

        @Test
        public void testCreatorForRightToLeftHalf() {
            // Given
            startIndex = 4;
            endIndex = 8;
            baseLevel = 1;

            // When
            buildSUT((sut) -> {
                // Then
                assertEquals(sut.getCharStart(), startIndex);
                assertEquals(sut.getCharEnd(), endIndex);
            });
        }

        @Test
        public void testCreatorForMixedDirectionHalf() {
            // Given
            startIndex = 2;
            endIndex = 6;
            baseLevel = 0;

            // When
            buildSUT((sut) -> {
                // Then
                assertEquals(sut.getCharStart(), startIndex);
                assertEquals(sut.getCharEnd(), endIndex);
            });
        }

        @Test
        public void testGetRunCount() {
            buildSUT((sut) -> {
                // When
                int runCount = sut.getRunCount();

                // Then
                assertEquals(runCount, 2);
            });
        }

        @Test
        public void testGetVisualRunForFirstIndex() {
            buildSUT((sut) -> {
                // When
                BidiRun run = sut.getVisualRun(0);

                // Then
                assertNotNull(run);
                assertEquals(run.charStart, 0);
                assertEquals(run.charEnd, 4);
                assertEquals(run.embeddingLevel, 0);
            });
        }

        @Test
        public void testGetVisualRunForLastIndex() {
            buildSUT((sut) -> {
                // When
                BidiRun run = sut.getVisualRun(1);

                // Then
                assertNotNull(run);
                assertEquals(run.charStart, 4);
                assertEquals(run.charEnd, 8);
                assertEquals(run.embeddingLevel, 1);
            });
        }

        @Test
        public void testGetVisualRuns() {
            buildSUT((sut) -> {
                // When
                List<BidiRun> visualRuns = sut.getVisualRuns();

                // Then
                assertTrue(visualRuns instanceof BidiLine.RunList);
                assertSame(((BidiLine.RunList) visualRuns).owner, sut);
                assertSame(((BidiLine.RunList) visualRuns).size, sut.getRunCount());
                assertArrayEquals(visualRuns.toArray(new BidiRun[0]), DEFAULT_VISUAL_RUNS);
            });
        }
    }

    @RunWith(MockitoJUnitRunner.class)
    public static class RunListTest {
        private static final int DEFAULT_SIZE = 2;

        @Mock
        private BidiLine line;
        private BidiLine.RunList sut;

        @Before
        public void setUp() {
            when(line.getRunCount()).thenReturn(DEFAULT_SIZE);

            sut = new BidiLine.RunList(line);
        }

        @Test
        public void testSize() {
            // When
            int size = sut.size();

            // Then
            assertEquals(size, DEFAULT_SIZE);
        }

        @Test(expected = IndexOutOfBoundsException.class)
        public void testGetForNegativeIndex() {
            // When
            sut.get(-1);
        }

        @Test(expected = IndexOutOfBoundsException.class)
        public void testGetForLimitIndex() {
            // When
            sut.get(DEFAULT_SIZE);
        }

        @Test
        public void testGetForFirstIndex() {
            BidiRun anyRun = new BidiRun();
            when(line.getVisualRun(0)).thenReturn(anyRun);

            // When
            BidiRun run = sut.get(0);

            // Then
            assertSame(run, anyRun);
        }

        @Test
        public void testGetForLastIndex() {
            BidiRun anyRun = new BidiRun();
            when(line.getVisualRun(1)).thenReturn(anyRun);

            // When
            BidiRun run = sut.get(1);

            // Then
            assertSame(run, anyRun);
        }
    }
}
