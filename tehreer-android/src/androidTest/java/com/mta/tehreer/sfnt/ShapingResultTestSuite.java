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
import com.mta.tehreer.sut.UnsafeSUTBuilder;
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

    protected static class ShapingResultBuilder extends UnsafeSUTBuilder<ShapingResult, ShapingResult.Finalizable> {
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
        public ShapingResult buildSUT() {
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

        setOnPreBuildSUT((builder) -> {
            ShapingResultBuilder sutBuilder = (ShapingResultBuilder) builder;
            sutBuilder.typeface = typeface;
            sutBuilder.typeSize = typeSize;
            sutBuilder.scriptTag = scriptTag;
            sutBuilder.languageTag = languageTag;
            sutBuilder.openTypeFeatures = openTypeFeatures;
            sutBuilder.writingDirection = writingDirection;
            sutBuilder.shapingOrder = shapingOrder;
            sutBuilder.text = text;
            sutBuilder.startIndex = startIndex;
            sutBuilder.endIndex = endIndex;
        });
    }

    @Test
    public void testIsBackwardForForwardShapingOrder() {
        // Given
        shapingOrder = ShapingOrder.FORWARD;

        buildSUT((sut) -> {
            assertFalse(sut.isBackward());
        });
    }

    @Test
    public void testGetGlyphIdForAllElements() {
        buildSUT((sut) -> {
            IntList values = DEFAULT_GLYPH_IDS;

            int glyphCount = sut.getGlyphCount();
            assertEquals(glyphCount, values.size());

            for (int i = 0; i < glyphCount; i++) {
                assertEquals(sut.getGlyphId(i), values.get(i));
            }
        });
    }

    @Test
    public void testCopyGlyphIdsOnMatchingArray() {
        buildSUT((sut) -> {
            IntList values = DEFAULT_GLYPH_IDS;

            int[] glyphIds = new int[values.size()];
            Arrays.fill(glyphIds, -1);

            // When
            sut.copyGlyphIds(0, values.size(), glyphIds, 0);

            // Then
            assertArrayEquals(glyphIds, values.toArray());
        });
    }

    @Test
    public void testCopyGlyphIdsOnSmallArray() {
        buildSUT((sut) -> {
            IntList values = DEFAULT_GLYPH_IDS;
            int length = values.size() / 2;
            int startIndex = length / 2;
            int endIndex = startIndex + length;

            values = values.subList(startIndex, endIndex);

            int[] glyphIds = new int[values.size()];
            Arrays.fill(glyphIds, -1);

            // When
            sut.copyGlyphIds(startIndex, values.size(), glyphIds, 0);

            // Then
            assertArrayEquals(glyphIds, values.toArray());
        });
    }

    @Test
    public void testCopyGlyphIdsOnLargeArray() {
        buildSUT((sut) -> {
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
            sut.copyGlyphIds(0, values.size(), glyphIds, startIndex);

            // Then
            assertArrayEquals(Arrays.copyOfRange(glyphIds, 0, startIndex), firstChunk);
            assertArrayEquals(Arrays.copyOfRange(glyphIds, startIndex, endIndex), values.toArray());
            assertArrayEquals(Arrays.copyOfRange(glyphIds, endIndex, finalLength), lastChunk);
        });
    }

    @Test
    public void testGetGlyphIds() {
        buildSUT((sut) -> {
            // When
            IntList glyphIds = sut.getGlyphIds();

            // Then
            assertNotNull(glyphIds);
            assertTrue(glyphIds instanceof ShapingResult.GlyphIdList);
            assertSame(((ShapingResult.GlyphIdList) glyphIds).owner, sut);
            assertEquals(((ShapingResult.GlyphIdList) glyphIds).offset, 0);
            assertEquals(((ShapingResult.GlyphIdList) glyphIds).size, sut.getGlyphCount());
        });
    }

    @Test
    public void testGetGlyphOffsetForAllElements() {
        buildSUT((sut) -> {
            PointList values = DEFAULT_GLYPH_OFFSETS;

            int glyphCount = sut.getGlyphCount();
            assertEquals(glyphCount, values.size());

            for (int i = 0; i < glyphCount; i++) {
                assertEquals(sut.getGlyphXOffset(i), values.getX(i), 0.0f);
                assertEquals(sut.getGlyphYOffset(i), values.getY(i), 0.0f);
            }
        });
    }

    @Test
    public void testCopyGlyphOffsetsOnMatchingArray() {
        buildSUT((sut) -> {
            PointList values = DEFAULT_GLYPH_OFFSETS;

            float[] glyphOffsets = new float[values.size() * 2];
            Arrays.fill(glyphOffsets, -1.0f);

            // When
            sut.copyGlyphOffsets(0, values.size(), glyphOffsets, 0);

            // Then
            assertArrayEquals(glyphOffsets, values.toArray(), 0.0f);
        });
    }

    @Test
    public void testCopyGlyphOffsetsOnSmallArray() {
        buildSUT((sut) -> {
            PointList values = DEFAULT_GLYPH_OFFSETS;
            int length = values.size() / 2;
            int startIndex = length / 2;
            int endIndex = startIndex + length;

            values = values.subList(startIndex, endIndex);

            float[] glyphOffsets = new float[values.size() * 2];
            Arrays.fill(glyphOffsets, -1.0f);

            // When
            sut.copyGlyphOffsets(startIndex, values.size(), glyphOffsets, 0);

            // Then
            assertArrayEquals(glyphOffsets, values.toArray(), 0.0f);
        });
    }

    @Test
    public void testCopyGlyphOffsetsOnLargeArray() {
        buildSUT((sut) -> {
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
            sut.copyGlyphOffsets(0, values.size(), glyphOffsets, startIndex);

            // Then
            assertArrayEquals(Arrays.copyOfRange(glyphOffsets, 0, startIndex), firstChunk, 0.0f);
            assertArrayEquals(Arrays.copyOfRange(glyphOffsets, startIndex, endIndex), values.toArray(), 0.0f);
            assertArrayEquals(Arrays.copyOfRange(glyphOffsets, endIndex, finalLength), lastChunk, 0.0f);
        });
    }

    @Test
    public void testGetGlyphOffsets() {
        buildSUT((sut) -> {
            // When
            PointList glyphOffsets = sut.getGlyphOffsets();

            // Then
            assertNotNull(glyphOffsets);
            assertTrue(glyphOffsets instanceof ShapingResult.GlyphOffsetList);
            assertSame(((ShapingResult.GlyphOffsetList) glyphOffsets).owner, sut);
            assertEquals(((ShapingResult.GlyphOffsetList) glyphOffsets).offset, 0);
            assertEquals(((ShapingResult.GlyphOffsetList) glyphOffsets).size, sut.getGlyphCount());
        });
    }

    @Test
    public void testGetGlyphAdvanceForAllElements() {
        buildSUT((sut) -> {
            FloatList values = DEFAULT_GLYPH_ADVANCES;

            int glyphCount = sut.getGlyphCount();
            assertEquals(glyphCount, values.size());

            for (int i = 0; i < glyphCount; i++) {
                assertEquals(sut.getGlyphAdvance(i), values.get(i), 0.0f);
            }
        });
    }

    @Test
    public void testCopyGlyphAdvancesOnMatchingArray() {
        buildSUT((sut) -> {
            FloatList values = DEFAULT_GLYPH_ADVANCES;

            float[] glyphAdvances = new float[values.size()];
            Arrays.fill(glyphAdvances, -1.0f);

            // When
            sut.copyGlyphAdvances(0, values.size(), glyphAdvances, 0);

            // Then
            assertArrayEquals(glyphAdvances, values.toArray(), 0.0f);
        });
    }

    @Test
    public void testCopyGlyphAdvancesOnSmallArray() {
        buildSUT((sut) -> {
            FloatList values = DEFAULT_GLYPH_ADVANCES;
            int length = values.size() / 2;
            int startIndex = length / 2;
            int endIndex = startIndex + length;

            values = values.subList(startIndex, endIndex);

            float[] glyphAdvances = new float[values.size()];
            Arrays.fill(glyphAdvances, -1.0f);

            // When
            sut.copyGlyphAdvances(startIndex, values.size(), glyphAdvances, 0);

            // Then
            assertArrayEquals(glyphAdvances, values.toArray(), 0.0f);
        });
    }

    @Test
    public void testCopyGlyphAdvancesOnLargeArray() {
        buildSUT((sut) -> {
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
            sut.copyGlyphAdvances(0, values.size(), glyphAdvances, startIndex);

            // Then
            assertArrayEquals(Arrays.copyOfRange(glyphAdvances, 0, startIndex), firstChunk, 0.0f);
            assertArrayEquals(Arrays.copyOfRange(glyphAdvances, startIndex, endIndex), values.toArray(), 0.0f);
            assertArrayEquals(Arrays.copyOfRange(glyphAdvances, endIndex, finalLength), lastChunk, 0.0f);
        });
    }

    @Test
    public void testGetGlyphAdvances() {
        buildSUT((sut) -> {
            // When
            FloatList glyphAdvances = sut.getGlyphAdvances();

            // Then
            assertNotNull(glyphAdvances);
            assertTrue(glyphAdvances instanceof ShapingResult.GlyphAdvanceList);
            assertSame(((ShapingResult.GlyphAdvanceList) glyphAdvances).owner, sut);
            assertEquals(((ShapingResult.GlyphAdvanceList) glyphAdvances).offset, 0);
            assertEquals(((ShapingResult.GlyphAdvanceList) glyphAdvances).size, sut.getGlyphCount());
        });
    }

    @Test
    public void testGetClusterMap() {
        buildSUT((sut) -> {
            // When
            IntList clusterMap = sut.getClusterMap();

            // Then
            assertNotNull(clusterMap);
            assertTrue(clusterMap instanceof ShapingResult.ClusterMap);
            assertSame(((ShapingResult.ClusterMap) clusterMap).owner, sut);
            assertTrue(((ShapingResult.ClusterMap) clusterMap).pointer != 0);
            assertEquals(((ShapingResult.ClusterMap) clusterMap).size, sut.getCharEnd() - sut.getCharStart());
        });
    }

    @Test
    public void testGetCaretEdges() {
        buildSUT((sut) -> {
            sut = spy(sut);

            // When
            FloatList caretEdges = sut.getCaretEdges();

            // Then
            verify(sut).getCaretEdges(null);
            assertNotNull(caretEdges);
            assertEquals(caretEdges.size(), sut.getCharEnd() - sut.getCharStart() + 1);
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetCaretEdgesForLessCaretStops() {
        buildSUT((sut) -> {
            // Given
            boolean[] caretStops = new boolean[sut.getGlyphCount() - 1];

            // When
            sut.getCaretEdges(caretStops);
        });
    }

    @Test
    public void testGetCaretEdgesWithBuilderInput() {
        buildSUT((sut) -> {
            sut = spy(sut);

            boolean isBackward = false;
            boolean isRTL = true;
            FloatList glyphAdvances = DEFAULT_GLYPH_ADVANCES;
            IntList clusterMap = DEFAULT_CLUSTER_MAP;
            boolean[] caretStops = new boolean[text.length()];
            CaretEdgesBuilder caretEdgesBuilder = spy(new CaretEdgesBuilder());
            FloatList caretEdges = FloatList.of();

            doReturn(isBackward).when(sut).isBackward();
            doReturn(isRTL).when(sut).isRTL();
            doReturn(glyphAdvances).when(sut).getGlyphAdvances();
            doReturn(clusterMap).when(sut).getClusterMap();
            doReturn(caretEdgesBuilder).when(sut).createCaretEdgesBuilder();
            doReturn(caretEdges).when(caretEdgesBuilder).build();

            // When
            FloatList list = sut.getCaretEdges(caretStops);

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
        buildSUT((sut) -> {
            String description = DescriptionBuilder
                    .of(ShapingResult.class)
                    .put("isBackward", sut.isBackward())
                    .put("charStart", sut.getCharStart())
                    .put("charEnd", sut.getCharEnd())
                    .put("glyphCount", sut.getGlyphCount())
                    .put("glyphIds", sut.getGlyphIds())
                    .put("glyphOffsets", sut.getGlyphOffsets())
                    .put("glyphAdvances", sut.getGlyphAdvances())
                    .put("clusterMap", sut.getClusterMap())
                    .build();

            // When
            String string = sut.toString();

            // Then
            assertEquals(string, description);
        });
    }

    @Test
    public void testResultsForFullText() {
        buildSUT((sut) -> {
            assertFalse(sut.isBackward());
            assertEquals(sut.getCharStart(), startIndex);
            assertEquals(sut.getCharEnd(), endIndex);
            assertEquals(sut.getGlyphCount(), text.length());
            assertEquals(sut.getGlyphIds(), DEFAULT_GLYPH_IDS);
            assertEquals(sut.getGlyphOffsets(), DEFAULT_GLYPH_OFFSETS);
            assertEquals(sut.getGlyphAdvances(), DEFAULT_GLYPH_ADVANCES);
            assertEquals(sut.getClusterMap(), DEFAULT_CLUSTER_MAP);
            assertEquals(sut.getCaretEdges(), DEFAULT_CARET_EDGES);
        });
    }

    @Test
    public void testResultsForFirstWord() {
        // Given
        startIndex = 0;
        endIndex = 4;

        buildSUT((sut) -> {
            // Then
            assertFalse(sut.isBackward());
            assertEquals(sut.getCharStart(), startIndex);
            assertEquals(sut.getCharEnd(), endIndex);
            assertEquals(sut.getGlyphCount(), endIndex - startIndex);
            assertEquals(sut.getGlyphIds(), DEFAULT_GLYPH_IDS.subList(startIndex, endIndex));
            assertEquals(sut.getGlyphOffsets(), DEFAULT_GLYPH_OFFSETS.subList(startIndex, endIndex));
            assertEquals(sut.getGlyphAdvances(), DEFAULT_GLYPH_ADVANCES.subList(startIndex, endIndex));
            assertEquals(sut.getClusterMap(), DEFAULT_CLUSTER_MAP.subList(0, endIndex - startIndex));
        });
    }

    @Test
    public void testResultsForMidWord() {
        // Given
        startIndex = 5;
        endIndex = 8;

        buildSUT((sut) -> {
            // Then
            assertFalse(sut.isBackward());
            assertEquals(sut.getCharStart(), startIndex);
            assertEquals(sut.getCharEnd(), endIndex);
            assertEquals(sut.getGlyphCount(), endIndex - startIndex);
            assertEquals(sut.getGlyphIds(), DEFAULT_GLYPH_IDS.subList(startIndex, endIndex));
            assertEquals(sut.getGlyphOffsets(), DEFAULT_GLYPH_OFFSETS.subList(startIndex, endIndex));
            assertEquals(sut.getGlyphAdvances(), DEFAULT_GLYPH_ADVANCES.subList(startIndex, endIndex));
            assertEquals(sut.getClusterMap(), DEFAULT_CLUSTER_MAP.subList(0, endIndex - startIndex));
        });
    }

    @Test
    public void testResultsForLastWord() {
        // Given
        startIndex = 9;
        endIndex = 12;

        buildSUT((sut) -> {
            // Then
            assertFalse(sut.isBackward());
            assertEquals(sut.getCharStart(), startIndex);
            assertEquals(sut.getCharEnd(), endIndex);
            assertEquals(sut.getGlyphCount(), endIndex - startIndex);
            assertEquals(sut.getGlyphIds(), DEFAULT_GLYPH_IDS.subList(startIndex, endIndex));
            assertEquals(sut.getGlyphOffsets(), DEFAULT_GLYPH_OFFSETS.subList(startIndex, endIndex));
            assertEquals(sut.getGlyphAdvances(), DEFAULT_GLYPH_ADVANCES.subList(startIndex, endIndex));
            assertEquals(sut.getClusterMap(), DEFAULT_CLUSTER_MAP.subList(0, endIndex - startIndex));
        });
    }
}
