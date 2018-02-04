/*
 * Copyright (C) 2018 Muhammad Tayyab Akram
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

package com.mta.tehreer.internal.layout;

import android.text.Spanned;
import android.text.style.ReplacementSpan;

import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.sfnt.SfntTag;
import com.mta.tehreer.sfnt.ShapingEngine;
import com.mta.tehreer.sfnt.ShapingResult;
import com.mta.tehreer.sfnt.WritingDirection;
import com.mta.tehreer.unicode.BaseDirection;
import com.mta.tehreer.unicode.BidiAlgorithm;
import com.mta.tehreer.unicode.BidiParagraph;
import com.mta.tehreer.unicode.BidiRun;

import java.util.List;

public class ShapeResolver {

    public static void fillRuns(String text, Spanned spanned, List<Object> defaultSpans, byte[] breaks,
                                List<BidiParagraph> paragraphs, List<IntrinsicRun> runs) {
        // TODO: Analyze script runs.

        BidiAlgorithm bidiAlgorithm = null;
        ShapingEngine shapingEngine = null;

        try {
            bidiAlgorithm = new BidiAlgorithm(text);
            shapingEngine = new ShapingEngine();

            ShapingRunLocator locator = new ShapingRunLocator(spanned, defaultSpans);

            BaseDirection baseDirection = BaseDirection.DEFAULT_LEFT_TO_RIGHT;
            byte forwardType = BreakResolver.typeMode(BreakResolver.PARAGRAPH, true);
            byte backwardType = BreakResolver.typeMode(BreakResolver.PARAGRAPH, false);

            int paragraphStart = 0;
            int suggestedEnd = text.length();

            while (paragraphStart != suggestedEnd) {
                BidiParagraph paragraph = bidiAlgorithm.createParagraph(paragraphStart, suggestedEnd, baseDirection);
                for (BidiRun bidiRun : paragraph.getLogicalRuns()) {
                    int scriptTag = SfntTag.make(bidiRun.isRightToLeft() ? "arab" : "latn");
                    WritingDirection writingDirection = ShapingEngine.getScriptDirection(scriptTag);

                    locator.reset(bidiRun.charStart, bidiRun.charEnd);

                    shapingEngine.setScriptTag(scriptTag);
                    shapingEngine.setWritingDirection(writingDirection);

                    resolveTypefaces(text, spanned, runs, locator, shapingEngine, bidiRun.embeddingLevel);
                }
                paragraphs.add(paragraph);

                breaks[paragraph.getCharStart()] |= backwardType;
                breaks[paragraph.getCharEnd() - 1] |= forwardType;

                paragraphStart = paragraph.getCharEnd();
            }
        } finally {
            if (shapingEngine != null) {
                shapingEngine.dispose();
            }
            if (bidiAlgorithm != null) {
                bidiAlgorithm.dispose();
            }
        }
    }

    private static void resolveTypefaces(String text, Spanned spanned, List<IntrinsicRun> runs,
                                         ShapingRunLocator locator, ShapingEngine engine, byte bidiLevel) {
        while (locator.moveNext()) {
            int runStart = locator.getRunStart();
            int runEnd = locator.getRunEnd();

            Typeface typeface = locator.getTypeface();
            float typeSize = locator.getTypeSize();

            if (typeface == null) {
                throw new IllegalArgumentException("No typeface is specified for range ["
                                                   + runStart + ".." + runEnd + ")");
            }

            ReplacementSpan replacement = locator.getReplacement();
            IntrinsicRun intrinsicRun;

            if (replacement == null) {
                engine.setTypeface(typeface);
                engine.setTypeSize(typeSize);

                ShapingResult shapingResult = null;

                try {
                    shapingResult = engine.shapeText(text, runStart, runEnd);

                    WritingDirection writingDirection = engine.getWritingDirection();
                    boolean isBackward = shapingResult.isBackward();
                    int[] glyphIds = shapingResult.getGlyphIds().toArray();
                    float[] offsets = shapingResult.getGlyphOffsets().toArray();
                    float[] advances = shapingResult.getGlyphAdvances().toArray();
                    int[] clusterMap = shapingResult.getClusterMap().toArray();

                    float scaleX = locator.getScaleX();
                    if (Float.compare(scaleX, 1.0f) != 0) {
                        for (int i = 0; i < glyphIds.length; i++) {
                            offsets[i * 2] *= scaleX;
                            advances[i] *= scaleX;
                        }
                    }

                    float baselineShift = locator.getBaselineShift();
                    if (Float.compare(baselineShift, 0.0f) != 0) {
                        for (int i = 0; i < glyphIds.length; i++) {
                            offsets[(i * 2) + 1] += baselineShift;
                        }
                    }

                    intrinsicRun = new IntrinsicRun(runStart, runEnd, isBackward, bidiLevel,
                                                    typeface, typeSize, writingDirection,
                                                    glyphIds, offsets, advances, clusterMap);
                } finally {
                    if (shapingResult != null) {
                        shapingResult.dispose();
                    }
                }
            } else {
                int spaceGlyph = typeface.getGlyphId(' ');
                int replacementSize = replacement.getSize(null, spanned, runStart, runEnd, null);

                WritingDirection writingDirection = engine.getWritingDirection();
                int[] glyphIds = new int[] { spaceGlyph };
                float[] offsets = new float[] { 0.0f, 0.0f };
                float[] advances = new float[] { replacementSize };
                int[] clusterMap = new int[runEnd - runStart];

                intrinsicRun = new IntrinsicRun(runStart, runEnd, false, bidiLevel,
                                                typeface, typeSize, writingDirection,
                                                glyphIds, offsets, advances, clusterMap);
            }

            runs.add(intrinsicRun);
        }
    }
}
