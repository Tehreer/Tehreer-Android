/*
 * Copyright (C) 2018-2019 Muhammad Tayyab Akram
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

import android.graphics.Paint;
import android.text.Spanned;
import android.text.style.ReplacementSpan;

import androidx.annotation.NonNull;

import com.mta.tehreer.collections.FloatList;
import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.sfnt.ShapingEngine;
import com.mta.tehreer.sfnt.ShapingOrder;
import com.mta.tehreer.sfnt.ShapingResult;
import com.mta.tehreer.sfnt.WritingDirection;
import com.mta.tehreer.unicode.BaseDirection;
import com.mta.tehreer.unicode.BidiAlgorithm;
import com.mta.tehreer.unicode.BidiParagraph;
import com.mta.tehreer.unicode.BidiRun;
import com.mta.tehreer.unicode.Script;
import com.mta.tehreer.unicode.ScriptClassifier;
import com.mta.tehreer.unicode.ScriptRun;

import java.util.List;

import static com.mta.tehreer.internal.util.Preconditions.checkArgument;

public class ShapeResolver {
    public static void fillRuns(@NonNull String text, @NonNull Spanned spanned,
                                @NonNull List<Object> defaultSpans, @NonNull byte[] breaks,
                                @NonNull List<BidiParagraph> paragraphs, @NonNull List<IntrinsicRun> runs) {
        BidiAlgorithm bidiAlgorithm = null;
        ShapingEngine shapingEngine = null;

        try {
            bidiAlgorithm = new BidiAlgorithm(text);
            shapingEngine = new ShapingEngine();

            ScriptClassifier scriptClassifier = new ScriptClassifier(text);
            ShapingRunLocator locator = new ShapingRunLocator(spanned, defaultSpans);

            BaseDirection baseDirection = BaseDirection.DEFAULT_LEFT_TO_RIGHT;
            byte forwardType = BreakResolver.typeMode(BreakResolver.PARAGRAPH, true);
            byte backwardType = BreakResolver.typeMode(BreakResolver.PARAGRAPH, false);

            int paragraphStart = 0;
            int suggestedEnd = text.length();

            while (paragraphStart != suggestedEnd) {
                BidiParagraph paragraph = bidiAlgorithm.createParagraph(paragraphStart, suggestedEnd, baseDirection);
                for (BidiRun bidiRun : paragraph.getLogicalRuns()) {
                    for (ScriptRun scriptRun : scriptClassifier.getScriptRuns(bidiRun.charStart, bidiRun.charEnd)) {
                        int scriptTag = Script.getOpenTypeTag(scriptRun.script);
                        WritingDirection writingDirection = ShapingEngine.getScriptDirection(scriptTag);

                        boolean isOddLevel = ((bidiRun.embeddingLevel & 1) == 1);
                        boolean isBackward = (isOddLevel && writingDirection == WritingDirection.LEFT_TO_RIGHT)
                                           | (!isOddLevel && writingDirection == WritingDirection.RIGHT_TO_LEFT);
                        ShapingOrder shapingOrder = (isBackward ? ShapingOrder.BACKWARD : ShapingOrder.FORWARD);

                        locator.reset(scriptRun.charStart, scriptRun.charEnd);

                        shapingEngine.setScriptTag(scriptTag);
                        shapingEngine.setWritingDirection(writingDirection);
                        shapingEngine.setShapingOrder(shapingOrder);

                        resolveTypefaces(text, spanned, runs, locator, shapingEngine, bidiRun.embeddingLevel);
                    }
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

    private static void resolveTypefaces(@NonNull String text, @NonNull Spanned spanned,
                                         @NonNull List<IntrinsicRun> runs,
                                         @NonNull ShapingRunLocator locator,
                                         @NonNull ShapingEngine engine, byte bidiLevel) {
        Paint.FontMetricsInt metrics = null;

        while (locator.moveNext()) {
            int runStart = locator.getRunStart();
            int runEnd = locator.getRunEnd();

            Typeface typeface = locator.getTypeface();
            checkArgument(typeface != null, "No typeface is specified for range [" + runStart + ", " + runEnd + ')');

            float typeSize = locator.getTypeSize();
            float sizeByEm = typeSize / typeface.getUnitsPerEm();
            float ascent = typeface.getAscent() * sizeByEm;
            float descent = typeface.getDescent() * sizeByEm;
            float leading = typeface.getLeading() * sizeByEm;

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
                    FloatList caretEdges = shapingResult.getCaretEdges(null);

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
                                                    writingDirection, typeface, typeSize,
                                                    ascent, descent, leading,
                                                    glyphIds, offsets, advances,
                                                    clusterMap, caretEdges);
                } finally {
                    if (shapingResult != null) {
                        shapingResult.dispose();
                    }
                }
            } else {
                if (metrics == null) {
                    metrics = new Paint.FontMetricsInt();
                }

                metrics.ascent = (int) -(ascent + 0.5f);
                metrics.descent = (int) (descent + 0.5f);
                metrics.leading = (int) (leading + 0.5f);

                int spaceGlyph = typeface.getGlyphId(' ');
                int replacementSize = replacement.getSize(null, spanned, runStart, runEnd, metrics);

                WritingDirection writingDirection = engine.getWritingDirection();
                int[] glyphIds = new int[] { spaceGlyph };
                float[] offsets = new float[] { 0.0f, 0.0f };
                float[] advances = new float[] { replacementSize };
                int[] clusterMap = new int[runEnd - runStart];
                FloatList caretEdges = FloatList.of(new float[runEnd - runStart + 1]);

                intrinsicRun = new IntrinsicRun(runStart, runEnd, false, bidiLevel,
                                                writingDirection, typeface, typeSize,
                                                -metrics.ascent, metrics.descent, metrics.leading,
                                                glyphIds, offsets, advances,
                                                clusterMap, caretEdges);
            }

            runs.add(intrinsicRun);
        }
    }
}
