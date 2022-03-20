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

import static com.mta.tehreer.util.Assert.assertThrows;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.mta.tehreer.DisposableTestSuite;
import com.mta.tehreer.collections.ByteList;
import com.mta.tehreer.internal.collections.Int8BufferByteList;
import com.mta.tehreer.sut.UnsafeSUTBuilder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class BidiParagraphTestSuite extends DisposableTestSuite<BidiParagraph, BidiParagraph.Finalizable> {
    private static final String DEFAULT_TEXT = "abcdابجد";
    private static final byte[] DEFAULT_LEVELS = { 0, 0, 0, 0, 1, 1, 1, 1 };

    private static class BidiParagraphBuilder extends UnsafeSUTBuilder<BidiParagraph, BidiParagraph.Finalizable> {
        String text = DEFAULT_TEXT;
        int startIndex = 0;
        int endIndex = text.length();
        byte baseLevel = 0;

        protected BidiParagraphBuilder() {
            super(BidiParagraph.class, BidiParagraph.Finalizable.class);
        }

        @Override
        public BidiParagraph buildSUT() {
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

        setOnPreBuildSUT((builder) -> {
            BidiParagraphBuilder sutBuilder = (BidiParagraphBuilder) builder;
            sutBuilder.text = text;
            sutBuilder.startIndex = startIndex;
            sutBuilder.endIndex = endIndex;
            sutBuilder.baseLevel = baseLevel;
        });
    }

    public static class StaticTest extends StaticTestSuite<BidiParagraph, BidiParagraph.Finalizable> {
        public StaticTest() {
            super(new BidiParagraphBuilder());
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

    public static abstract class GeneralTestSuite extends BidiParagraphTestSuite {
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
                assertEquals(sut.getBaseLevel(), baseLevel);
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
                assertEquals(sut.getBaseLevel(), baseLevel);
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
                assertEquals(sut.getBaseLevel(), baseLevel);
            });
        }

        @Test
        public void testNativePointers() {
            buildSUT((sut) -> {
                assertNotEquals(sut.nativeBuffer, 0);
                assertNotEquals(sut.nativeParagraph, 0);
            });
        }

        @Test
        public void testGetCharLevels() {
            buildSUT((sut) -> {
                // When
                ByteList charLevels = sut.getCharLevels();

                // Then
                assertTrue(charLevels instanceof Int8BufferByteList);
                assertEquals(charLevels.size(), text.length());
                assertArrayEquals(charLevels.toArray(), DEFAULT_LEVELS);
            });
        }

        @Test
        public void testGetOnwardRunForLeftToRightRun() {
            buildSUT((sut) -> {
                // When
                BidiRun run = sut.getOnwardRun(0);

                // Then
                assertNotNull(run);
                assertEquals(run.charStart, 0);
                assertEquals(run.charEnd, 4);
                assertEquals(run.embeddingLevel, 0);
            });
        }

        @Test
        public void testGetOnwardRunForRightToLeftRun() {
            buildSUT((sut) -> {
                // When
                BidiRun run = sut.getOnwardRun(5);

                // Then
                assertNotNull(run);
                assertEquals(run.charStart, 5);
                assertEquals(run.charEnd, 8);
                assertEquals(run.embeddingLevel, 1);
            });
        }

        @Test
        public void testGetOnwardRunFromEndIndex() {
            buildSUT((sut) -> {
                // When
                BidiRun run = sut.getOnwardRun(8);

                // Then
                assertNull(run);
            });
        }

        @Test
        public void testGetLogicalRuns() {
            buildSUT((sut) -> {
                // When
                Iterable<BidiRun> iterable = sut.getLogicalRuns();

                // Then
                assertTrue(iterable instanceof BidiParagraph.RunIterable);
                assertSame(((BidiParagraph.RunIterable) iterable).owner, sut);
            });
        }

        @Test
        public void testCreateLineForInvalidRange() {
            buildSUT((sut) -> {
                // Invalid Start
                assertThrows(IllegalArgumentException.class,
                             "Char Start: -1, Paragraph Range: [0, 8)",
                             () -> sut.createLine(-1, 8));

                // Invalid End
                assertThrows(IllegalArgumentException.class,
                             "Char End: 9, Paragraph Range: [0, 8)",
                             () -> sut.createLine(0, 9));

                // Empty Range
                assertThrows(IllegalArgumentException.class, "Bad Range: [0, 0)",
                             () -> sut.createLine(0, 0));
            });
        }

        @Test
        public void testCreateLineForFullRange() {
            buildSUT((sut) -> {
                // When
                BidiLine line = sut.createLine(startIndex, endIndex);

                // Then
                assertNotNull(line);
            });
        }
    }

    @RunWith(MockitoJUnitRunner.class)
    public static class RunIteratorTest {
        @Mock
        private BidiParagraph paragraph;
        private BidiParagraph.RunIterator sut;

        @Before
        public void setUp() {
            sut = new BidiParagraph.RunIterator(paragraph);
        }

        @Test
        public void testHasNextForAvailableRun() {
            // Given
            sut.run = mock(BidiRun.class);

            // When
            boolean hasNext = sut.hasNext();

            // Then
            assertTrue(hasNext);
        }

        @Test
        public void testHasNextForNoAvailableRun() {
            // Given
            sut.run = null;

            // When
            boolean hasNext = sut.hasNext();

            // Then
            assertFalse(hasNext);
        }

        @Test
        public void testNextForAvailableRun() {
            BidiRun firstRun = new BidiRun(0, 4, (byte) 0);
            BidiRun secondRun = new BidiRun(4, 8, (byte) 1);

            when(paragraph.getOnwardRun(4)).thenReturn(secondRun);

            // Given
            sut.run = firstRun;

            // When
            BidiRun bidiRun = sut.next();

            // Then
            assertSame(bidiRun, firstRun);
            assertSame(sut.run, secondRun);
        }

        @Test(expected = NoSuchElementException.class)
        public void testNextForNoAvailableRun() {
            // Given
            sut.run = null;

            // When
            sut.next();
        }
    }

    @RunWith(MockitoJUnitRunner.class)
    public static class RunIterableTest {
        @Mock
        private BidiParagraph paragraph;
        private BidiParagraph.RunIterable sut;

        @Before
        public void setUp() {
            sut = new BidiParagraph.RunIterable(paragraph);
        }

        @Test
        public void testIterator() {
            // When
            Iterator<BidiRun> iterator = sut.iterator();

            // Then
            assertTrue(iterator instanceof BidiParagraph.RunIterator);
            assertSame(((BidiParagraph.RunIterator) iterator).owner, paragraph);
        }
    }
}
