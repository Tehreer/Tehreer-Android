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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.mta.tehreer.DisposableTestSuite;
import com.mta.tehreer.collections.FloatList;
import com.mta.tehreer.collections.IntList;
import com.mta.tehreer.collections.PointList;
import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.internal.layout.CaretEdgesBuilder;
import com.mta.tehreer.subject.UnsafeSubjectBuilder;
import com.mta.tehreer.util.DescriptionBuilder;
import com.mta.tehreer.util.TypefaceStore;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

public abstract class ShapingResultTestSuite extends DisposableTestSuite<ShapingResult, ShapingResult.Finalizable> {
    private static final Typeface DEFAULT_TYPEFACE = TypefaceStore.getNafeesWeb();
    private static final float DEFAULT_TYPE_SIZE = DEFAULT_TYPEFACE.getUnitsPerEm() / 2.0f;
    private static final String DEFAULT_SCRIPT_TAG = "arab";
    private static final String DEFAULT_LANGUAGE_TAG = "dflt";
    private static final Set<OpenTypeFeature> DEFAULT_OPEN_TYPE_FEATURES = Collections.emptySet();
    private static final WritingDirection DEFAULT_WRITING_DIRECTION = WritingDirection.RIGHT_TO_LEFT;
    private static final ShapingOrder DEFAULT_SHAPING_ORDER = ShapingOrder.FORWARD;

    private static final String DEFAULT_TEXT = "ابجد ہوز حطی";
    private static final IntList DEFAULT_GLYPH_IDS = IntList.of(
        5, 49, 83, 117, 242, 74, 140, 20, 242, 56, 91, 145
    );
    private static final PointList DEFAULT_GLYPH_OFFSETS = PointList.of(
        0.0f, 0.0f,  0.0f, 0.0f,  0.0f, 0.0f,  0.0f, 0.0f,  0.0f, 0.0f,  0.0f, 0.0f,
        0.0f, 0.0f,  0.0f, 0.0f,  0.0f, 0.0f,  0.0f, 0.0f,  0.0f, 0.0f,  0.0f, 0.0f
    );
    private static final FloatList DEFAULT_GLYPH_ADVANCES = FloatList.of(
        181.0f, 183.5f, 487.0f, 507.5f, 127.5f, 453.0f,
        312.5f, 249.0f, 127.5f, 499.5f, 565.5f, 597.0f
    );
    private static final IntList DEFAULT_CLUSTER_MAP = IntList.of(
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11
    );
    private static final FloatList DEFAULT_CARET_EDGES = FloatList.of(
        4290.5f, 4109.5f, 3926.0f, 3439.0f, 2931.5f, 2804.0f,
        2351.0f, 2038.5f, 1789.5f, 1662.0f, 1162.5f, 597.0f, 0.0f
    );

    protected static class ShapingResultBuilder extends UnsafeSubjectBuilder<ShapingResult, ShapingResult.Finalizable> {
        Typeface typeface = DEFAULT_TYPEFACE;
        float typeSize = DEFAULT_TYPE_SIZE;
        String scriptTag = DEFAULT_SCRIPT_TAG;
        String languageTag = DEFAULT_LANGUAGE_TAG;
        Set<OpenTypeFeature> openTypeFeatures = DEFAULT_OPEN_TYPE_FEATURES;
        WritingDirection writingDirection = DEFAULT_WRITING_DIRECTION;
        ShapingOrder shapingOrder = DEFAULT_SHAPING_ORDER;
        String text = DEFAULT_TEXT;
        int startIndex = 0;
        int endIndex = DEFAULT_TEXT.length();

        protected ShapingResultBuilder() {
            super(ShapingResult.class, ShapingResult.Finalizable.class);
        }

        @Override
        public ShapingResult buildSubject() {
            ShapingEngine shapingEngine = ShapingEngine.finalizable(
                new ShapingEngine()
            );
            shapingEngine.setTypeface(typeface);
            shapingEngine.setTypeSize(typeSize);
            shapingEngine.setScriptTag(SfntTag.make(scriptTag));
            shapingEngine.setLanguageTag(SfntTag.make(languageTag));
            shapingEngine.setOpenTypeFeatures(openTypeFeatures);
            shapingEngine.setWritingDirection(writingDirection);
            shapingEngine.setShapingOrder(shapingOrder);

            return shapingEngine.shapeText(text, startIndex, endIndex);
        }
    }

    protected Typeface typeface = DEFAULT_TYPEFACE;
    protected float typeSize = DEFAULT_TYPE_SIZE;
    protected String scriptTag = DEFAULT_SCRIPT_TAG;
    protected String languageTag = DEFAULT_LANGUAGE_TAG;
    protected Set<OpenTypeFeature> openTypeFeatures = DEFAULT_OPEN_TYPE_FEATURES;
    protected WritingDirection writingDirection = DEFAULT_WRITING_DIRECTION;
    protected ShapingOrder shapingOrder = DEFAULT_SHAPING_ORDER;
    protected String text = DEFAULT_TEXT;
    protected int startIndex = 0;
    protected int endIndex = DEFAULT_TEXT.length();

    protected ShapingResultTestSuite(DisposableTestSuite.DefaultMode defaultMode) {
        super(new ShapingResultBuilder(), defaultMode);

        setOnPreBuildSubject((builder) -> {
            ShapingResultBuilder subjectBuilder = (ShapingResultBuilder) builder;
            subjectBuilder.typeface = typeface;
            subjectBuilder.typeSize = typeSize;
            subjectBuilder.scriptTag = scriptTag;
            subjectBuilder.languageTag = languageTag;
            subjectBuilder.openTypeFeatures = openTypeFeatures;
            subjectBuilder.writingDirection = writingDirection;
            subjectBuilder.shapingOrder = shapingOrder;
            subjectBuilder.text = text;
            subjectBuilder.startIndex = startIndex;
            subjectBuilder.endIndex = endIndex;
        });
    }

    @Test
    public void testIsBackwardForForwardShapingOrder() {
        // Given
        shapingOrder = ShapingOrder.FORWARD;

        buildSubject((subject) -> {
            assertFalse(subject.isBackward());
        });
    }

    @Test
    public void testGetGlyphIdForAllElements() {
        buildSubject((subject) -> {
            IntList values = DEFAULT_GLYPH_IDS;

            int glyphCount = subject.getGlyphCount();
            assertEquals(glyphCount, values.size());

            for (int i = 0; i < glyphCount; i++) {
                assertEquals(subject.getGlyphId(i), values.get(i));
            }
        });
    }

    @Test
    public void testCopyGlyphIdsOnMatchingArray() {
        buildSubject((subject) -> {
            IntList values = DEFAULT_GLYPH_IDS;

            int[] glyphIds = new int[values.size()];
            Arrays.fill(glyphIds, -1);

            // When
            subject.copyGlyphIds(0, values.size(), glyphIds, 0);

            // Then
            assertArrayEquals(glyphIds, values.toArray());
        });
    }

    @Test
    public void testCopyGlyphIdsOnSmallArray() {
        buildSubject((subject) -> {
            IntList values = DEFAULT_GLYPH_IDS;
            int length = values.size() / 2;
            int startIndex = length / 2;
            int endIndex = startIndex + length;

            values = values.subList(startIndex, endIndex);

            int[] glyphIds = new int[values.size()];
            Arrays.fill(glyphIds, -1);

            // When
            subject.copyGlyphIds(startIndex, values.size(), glyphIds, 0);

            // Then
            assertArrayEquals(glyphIds, values.toArray());
        });
    }

    @Test
    public void testCopyGlyphIdsOnLargeArray() {
        buildSubject((subject) -> {
            IntList values = DEFAULT_GLYPH_IDS;

            int extraLength = values.size() / 2;
            int finalLength = values.size() + extraLength;

            int startIndex = extraLength / 2;
            int endIndex = startIndex + values.size();

            int[] glyphIds = new int[finalLength];
            Arrays.fill(glyphIds, -1);

            int[] firstChunk = Arrays.copyOfRange(glyphIds, 0, startIndex);
            int[] lastChunk = Arrays.copyOfRange(glyphIds, endIndex, finalLength);

            // When
            subject.copyGlyphIds(0, values.size(), glyphIds, startIndex);

            // Then
            assertArrayEquals(Arrays.copyOfRange(glyphIds, 0, startIndex), firstChunk);
            assertArrayEquals(Arrays.copyOfRange(glyphIds, startIndex, endIndex), values.toArray());
            assertArrayEquals(Arrays.copyOfRange(glyphIds, endIndex, finalLength), lastChunk);
        });
    }

    @Test
    public void testGetGlyphIds() {
        buildSubject((subject) -> {
            // When
            IntList glyphIds = subject.getGlyphIds();

            // Then
            assertNotNull(glyphIds);
            assertTrue(glyphIds instanceof ShapingResult.GlyphIdList);
            assertSame(((ShapingResult.GlyphIdList) glyphIds).owner, subject);
            assertEquals(((ShapingResult.GlyphIdList) glyphIds).offset, 0);
            assertEquals(((ShapingResult.GlyphIdList) glyphIds).size, subject.getGlyphCount());
        });
    }

    @Test
    public void testGetGlyphOffsetForAllElements() {
        buildSubject((subject) -> {
            PointList values = DEFAULT_GLYPH_OFFSETS;

            int glyphCount = subject.getGlyphCount();
            assertEquals(glyphCount, values.size());

            for (int i = 0; i < glyphCount; i++) {
                assertEquals(subject.getGlyphXOffset(i), values.getX(i), 0.0f);
                assertEquals(subject.getGlyphYOffset(i), values.getY(i), 0.0f);
            }
        });
    }

    @Test
    public void testCopyGlyphOffsetsOnMatchingArray() {
        buildSubject((subject) -> {
            PointList values = DEFAULT_GLYPH_OFFSETS;

            float[] glyphOffsets = new float[values.size() * 2];
            Arrays.fill(glyphOffsets, -1.0f);

            // When
            subject.copyGlyphOffsets(0, values.size(), glyphOffsets, 0);

            // Then
            assertArrayEquals(glyphOffsets, values.toArray(), 0.0f);
        });
    }

    @Test
    public void testCopyGlyphOffsetsOnSmallArray() {
        buildSubject((subject) -> {
            PointList values = DEFAULT_GLYPH_OFFSETS;
            int length = values.size() / 2;
            int startIndex = length / 2;
            int endIndex = startIndex + length;

            values = values.subList(startIndex, endIndex);

            float[] glyphOffsets = new float[values.size() * 2];
            Arrays.fill(glyphOffsets, -1.0f);

            // When
            subject.copyGlyphOffsets(startIndex, values.size(), glyphOffsets, 0);

            // Then
            assertArrayEquals(glyphOffsets, values.toArray(), 0.0f);
        });
    }

    @Test
    public void testCopyGlyphOffsetsOnLargeArray() {
        buildSubject((subject) -> {
            PointList values = DEFAULT_GLYPH_OFFSETS;

            int actualLength = values.size() * 2;
            int extraLength = actualLength / 2;
            int finalLength = actualLength + extraLength;

            int startIndex = extraLength / 2;
            int endIndex = startIndex + actualLength;

            float[] glyphOffsets = new float[finalLength];
            Arrays.fill(glyphOffsets, -1.0f);

            float[] firstChunk = Arrays.copyOfRange(glyphOffsets, 0, startIndex);
            float[] lastChunk = Arrays.copyOfRange(glyphOffsets, endIndex, finalLength);

            // When
            subject.copyGlyphOffsets(0, values.size(), glyphOffsets, startIndex);

            // Then
            assertArrayEquals(Arrays.copyOfRange(glyphOffsets, 0, startIndex), firstChunk, 0.0f);
            assertArrayEquals(Arrays.copyOfRange(glyphOffsets, startIndex, endIndex), values.toArray(), 0.0f);
            assertArrayEquals(Arrays.copyOfRange(glyphOffsets, endIndex, finalLength), lastChunk, 0.0f);
        });
    }

    @Test
    public void testGetGlyphOffsets() {
        buildSubject((subject) -> {
            // When
            PointList glyphOffsets = subject.getGlyphOffsets();

            // Then
            assertNotNull(glyphOffsets);
            assertTrue(glyphOffsets instanceof ShapingResult.GlyphOffsetList);
            assertSame(((ShapingResult.GlyphOffsetList) glyphOffsets).owner, subject);
            assertEquals(((ShapingResult.GlyphOffsetList) glyphOffsets).offset, 0);
            assertEquals(((ShapingResult.GlyphOffsetList) glyphOffsets).size, subject.getGlyphCount());
        });
    }

    @Test
    public void testGetGlyphAdvanceForAllElements() {
        buildSubject((subject) -> {
            FloatList values = DEFAULT_GLYPH_ADVANCES;

            int glyphCount = subject.getGlyphCount();
            assertEquals(glyphCount, values.size());

            for (int i = 0; i < glyphCount; i++) {
                assertEquals(subject.getGlyphAdvance(i), values.get(i), 0.0f);
            }
        });
    }

    @Test
    public void testCopyGlyphAdvancesOnMatchingArray() {
        buildSubject((subject) -> {
            FloatList values = DEFAULT_GLYPH_ADVANCES;

            float[] glyphAdvances = new float[values.size()];
            Arrays.fill(glyphAdvances, -1.0f);

            // When
            subject.copyGlyphAdvances(0, values.size(), glyphAdvances, 0);

            // Then
            assertArrayEquals(glyphAdvances, values.toArray(), 0.0f);
        });
    }

    @Test
    public void testCopyGlyphAdvancesOnSmallArray() {
        buildSubject((subject) -> {
            FloatList values = DEFAULT_GLYPH_ADVANCES;
            int length = values.size() / 2;
            int startIndex = length / 2;
            int endIndex = startIndex + length;

            values = values.subList(startIndex, endIndex);

            float[] glyphAdvances = new float[values.size()];
            Arrays.fill(glyphAdvances, -1.0f);

            // When
            subject.copyGlyphAdvances(startIndex, values.size(), glyphAdvances, 0);

            // Then
            assertArrayEquals(glyphAdvances, values.toArray(), 0.0f);
        });
    }

    @Test
    public void testCopyGlyphAdvancesOnLargeArray() {
        buildSubject((subject) -> {
            FloatList values = DEFAULT_GLYPH_ADVANCES;

            int extraLength = values.size() / 2;
            int finalLength = values.size() + extraLength;

            int startIndex = extraLength / 2;
            int endIndex = startIndex + values.size();

            float[] glyphAdvances = new float[finalLength];
            Arrays.fill(glyphAdvances, -1.0f);

            float[] firstChunk = Arrays.copyOfRange(glyphAdvances, 0, startIndex);
            float[] lastChunk = Arrays.copyOfRange(glyphAdvances, endIndex, finalLength);

            // When
            subject.copyGlyphAdvances(0, values.size(), glyphAdvances, startIndex);

            // Then
            assertArrayEquals(Arrays.copyOfRange(glyphAdvances, 0, startIndex), firstChunk, 0.0f);
            assertArrayEquals(Arrays.copyOfRange(glyphAdvances, startIndex, endIndex), values.toArray(), 0.0f);
            assertArrayEquals(Arrays.copyOfRange(glyphAdvances, endIndex, finalLength), lastChunk, 0.0f);
        });
    }

    @Test
    public void testGetGlyphAdvances() {
        buildSubject((subject) -> {
            // When
            FloatList glyphAdvances = subject.getGlyphAdvances();

            // Then
            assertNotNull(glyphAdvances);
            assertTrue(glyphAdvances instanceof ShapingResult.GlyphAdvanceList);
            assertSame(((ShapingResult.GlyphAdvanceList) glyphAdvances).owner, subject);
            assertEquals(((ShapingResult.GlyphAdvanceList) glyphAdvances).offset, 0);
            assertEquals(((ShapingResult.GlyphAdvanceList) glyphAdvances).size, subject.getGlyphCount());
        });
    }

    @Test
    public void testGetClusterMap() {
        buildSubject((subject) -> {
            // When
            IntList clusterMap = subject.getClusterMap();

            // Then
            assertNotNull(clusterMap);
            assertTrue(clusterMap instanceof ShapingResult.ClusterMap);
            assertSame(((ShapingResult.ClusterMap) clusterMap).owner, subject);
            assertTrue(((ShapingResult.ClusterMap) clusterMap).pointer != 0);
            assertEquals(((ShapingResult.ClusterMap) clusterMap).size, subject.getCharEnd() - subject.getCharStart());
        });
    }

    @Test
    public void testGetCaretEdges() {
        buildSubject((subject) -> {
            subject = spy (subject);

            // When
            FloatList caretEdges = subject.getCaretEdges();

            // Then
            verify (subject).getCaretEdges(null);
            assertNotNull(caretEdges);
            assertEquals(caretEdges.size(), subject.getCharEnd() - subject.getCharStart() + 1);
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetCaretEdgesForLessCaretStops() {
        buildSubject((subject) -> {
            // Given
            boolean[] caretStops = new boolean [subject.getGlyphCount() - 1];

            // When
            subject.getCaretEdges(caretStops);
        });
    }

    @Test
    public void testGetCaretEdgesWithBuilderInput() {
        buildSubject((subject) -> {
            subject = spy (subject);

            boolean isBackward = false;
            boolean isRTL = true;
            FloatList glyphAdvances = DEFAULT_GLYPH_ADVANCES;
            IntList clusterMap = DEFAULT_CLUSTER_MAP;
            boolean[] caretStops = new boolean[text.length()];
            CaretEdgesBuilder caretEdgesBuilder = spy(new CaretEdgesBuilder());
            FloatList caretEdges = FloatList.of();

            doReturn(isBackward).when (subject).isBackward();
            doReturn(isRTL).when (subject).isRTL();
            doReturn(glyphAdvances).when (subject).getGlyphAdvances();
            doReturn(clusterMap).when (subject).getClusterMap();
            doReturn(caretEdgesBuilder).when (subject).createCaretEdgesBuilder();
            doReturn(caretEdges).when(caretEdgesBuilder).build();

            // When
            FloatList list = subject.getCaretEdges(caretStops);

            // Then
            assertSame(list, caretEdges);
            verify(caretEdgesBuilder).setBackward(isBackward);
            verify(caretEdgesBuilder).setRTL(isRTL);
            verify(caretEdgesBuilder).setGlyphAdvances(glyphAdvances);
            verify(caretEdgesBuilder).setClusterMap(clusterMap);
            verify(caretEdgesBuilder).setCaretStops(caretStops);
            verify(caretEdgesBuilder).build();
        });
    }

    @Test
    public void testToString() {
        buildSubject((subject) -> {
            String description = DescriptionBuilder
                    .of(ShapingResult.class)
                    .put("isBackward", subject.isBackward())
                    .put("charStart", subject.getCharStart())
                    .put("charEnd", subject.getCharEnd())
                    .put("glyphCount", subject.getGlyphCount())
                    .put("glyphIds", subject.getGlyphIds())
                    .put("glyphOffsets", subject.getGlyphOffsets())
                    .put("glyphAdvances", subject.getGlyphAdvances())
                    .put("clusterMap", subject.getClusterMap())
                    .build();

            // When
            String string = subject.toString();

            // Then
            assertEquals(string, description);
        });
    }

    @Test
    public void testResultsForFullText() {
        buildSubject((subject) -> {
            assertFalse(subject.isBackward());
            assertEquals(subject.getCharStart(), startIndex);
            assertEquals(subject.getCharEnd(), endIndex);
            assertEquals(subject.getGlyphCount(), text.length());
            assertEquals(subject.getGlyphIds(), DEFAULT_GLYPH_IDS);
            assertEquals(subject.getGlyphOffsets(), DEFAULT_GLYPH_OFFSETS);
            assertEquals(subject.getGlyphAdvances(), DEFAULT_GLYPH_ADVANCES);
            assertEquals(subject.getClusterMap(), DEFAULT_CLUSTER_MAP);
            assertEquals(subject.getCaretEdges(), DEFAULT_CARET_EDGES);
        });
    }

    @Test
    public void testResultsForFirstWord() {
        // Given
        startIndex = 0;
        endIndex = 4;

        buildSubject((subject) -> {
            // Then
            assertFalse(subject.isBackward());
            assertEquals(subject.getCharStart(), startIndex);
            assertEquals(subject.getCharEnd(), endIndex);
            assertEquals(subject.getGlyphCount(), endIndex - startIndex);
            assertEquals(subject.getGlyphIds(), DEFAULT_GLYPH_IDS.subList(startIndex, endIndex));
            assertEquals(subject.getGlyphOffsets(), DEFAULT_GLYPH_OFFSETS.subList(startIndex, endIndex));
            assertEquals(subject.getGlyphAdvances(), DEFAULT_GLYPH_ADVANCES.subList(startIndex, endIndex));
            assertEquals(subject.getClusterMap(), DEFAULT_CLUSTER_MAP.subList(0, endIndex - startIndex));
        });
    }

    @Test
    public void testResultsForMidWord() {
        // Given
        startIndex = 5;
        endIndex = 8;

        buildSubject((subject) -> {
            // Then
            assertFalse(subject.isBackward());
            assertEquals(subject.getCharStart(), startIndex);
            assertEquals(subject.getCharEnd(), endIndex);
            assertEquals(subject.getGlyphCount(), endIndex - startIndex);
            assertEquals(subject.getGlyphIds(), DEFAULT_GLYPH_IDS.subList(startIndex, endIndex));
            assertEquals(subject.getGlyphOffsets(), DEFAULT_GLYPH_OFFSETS.subList(startIndex, endIndex));
            assertEquals(subject.getGlyphAdvances(), DEFAULT_GLYPH_ADVANCES.subList(startIndex, endIndex));
            assertEquals(subject.getClusterMap(), DEFAULT_CLUSTER_MAP.subList(0, endIndex - startIndex));
        });
    }

    @Test
    public void testResultsForLastWord() {
        // Given
        startIndex = 9;
        endIndex = 12;

        buildSubject((subject) -> {
            // Then
            assertFalse(subject.isBackward());
            assertEquals(subject.getCharStart(), startIndex);
            assertEquals(subject.getCharEnd(), endIndex);
            assertEquals(subject.getGlyphCount(), endIndex - startIndex);
            assertEquals(subject.getGlyphIds(), DEFAULT_GLYPH_IDS.subList(startIndex, endIndex));
            assertEquals(subject.getGlyphOffsets(), DEFAULT_GLYPH_OFFSETS.subList(startIndex, endIndex));
            assertEquals(subject.getGlyphAdvances(), DEFAULT_GLYPH_ADVANCES.subList(startIndex, endIndex));
            assertEquals(subject.getClusterMap(), DEFAULT_CLUSTER_MAP.subList(0, endIndex - startIndex));
        });
    }
}
