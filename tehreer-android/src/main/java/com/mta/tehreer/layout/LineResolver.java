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

package com.mta.tehreer.layout;

import android.text.Spanned;

import com.mta.tehreer.internal.collections.SafeFloatList;
import com.mta.tehreer.internal.collections.SafeIntList;
import com.mta.tehreer.internal.collections.SafePointList;
import com.mta.tehreer.internal.layout.BreakResolver;
import com.mta.tehreer.internal.layout.ClusterMap;
import com.mta.tehreer.internal.layout.IntrinsicRun;
import com.mta.tehreer.internal.util.Paragraphs;
import com.mta.tehreer.internal.util.Runs;
import com.mta.tehreer.internal.util.StringUtils;
import com.mta.tehreer.unicode.BidiParagraph;
import com.mta.tehreer.unicode.BidiRun;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class LineResolver {

    private Spanned mSpanned;
    private List<BidiParagraph> mBidiParagraphs;
    private List<IntrinsicRun> mIntrinsicRuns;

    LineResolver(Spanned spanned, List<BidiParagraph> paragraphs, List<IntrinsicRun> runs) {
        mSpanned = spanned;
        mBidiParagraphs = paragraphs;
        mIntrinsicRuns = runs;
    }

    static GlyphRun createGlyphRun(IntrinsicRun intrinsicRun, int charStart, int charEnd, Object[] spans) {
        int glyphOffset = intrinsicRun.charGlyphStart(charStart);
        int glyphCount = intrinsicRun.charGlyphEnd(charEnd - 1) - glyphOffset;

        return new GlyphRun(charStart, charEnd, Arrays.asList(spans),
                            intrinsicRun.isBackward, intrinsicRun.bidiLevel,
                            intrinsicRun.typeface, intrinsicRun.typeSize, intrinsicRun.writingDirection,
                            new SafeIntList(intrinsicRun.glyphIds, glyphOffset, glyphCount),
                            new SafePointList(intrinsicRun.glyphOffsets, glyphOffset, glyphCount),
                            new SafeFloatList(intrinsicRun.glyphAdvances, glyphOffset, glyphCount),
                            new ClusterMap(intrinsicRun.clusterMap,
                                           charStart - intrinsicRun.charStart,
                                           charEnd - charStart, glyphOffset));
    }

    static ComposedLine createComposedLine(CharSequence text, int charStart, int charEnd,
                                           List<GlyphRun> runList, byte paragraphLevel) {
        float lineAscent = 0.0f;
        float lineDescent = 0.0f;
        float lineLeading = 0.0f;
        float lineExtent = 0.0f;

        int trailingWhitespaceStart = StringUtils.getTrailingWhitespaceStart(text, charStart, charEnd);
        float trailingWhitespaceExtent = 0.0f;

        for (GlyphRun glyphRun : runList) {
            glyphRun.setOriginX(lineExtent);

            float runAscent = glyphRun.getAscent();
            float runDescent = glyphRun.getDescent();
            float runLeading = glyphRun.getLeading();

            int runCharStart = glyphRun.getCharStart();
            int runCharEnd = glyphRun.getCharEnd();
            int runGlyphCount = glyphRun.getGlyphCount();
            float runExtent = glyphRun.computeTypographicExtent(0, runGlyphCount);

            if (trailingWhitespaceStart >= runCharStart && trailingWhitespaceStart < runCharEnd) {
                int whitespaceGlyphStart = glyphRun.getCharGlyphStart(trailingWhitespaceStart);
                int whitespaceGlyphEnd = glyphRun.getCharGlyphEnd(runCharEnd - 1);
                float whitespaceExtent = glyphRun.computeTypographicExtent(whitespaceGlyphStart, whitespaceGlyphEnd);

                trailingWhitespaceExtent += whitespaceExtent;
            }

            lineAscent = Math.max(lineAscent, runAscent);
            lineDescent = Math.max(lineDescent, runDescent);
            lineLeading = Math.max(lineLeading, runLeading);
            lineExtent += runExtent;
        }

        return new ComposedLine(charStart, charEnd, paragraphLevel,
                                lineAscent, lineDescent, lineLeading, lineExtent,
                                trailingWhitespaceExtent, Collections.unmodifiableList(runList));
    }

    ComposedLine createSimpleLine(int start, int end) {
        final List<GlyphRun> runList = new ArrayList<>();
        Paragraphs.iterateLineRuns(mBidiParagraphs, start, end, new Paragraphs.RunConsumer() {

            @Override
            public void accept(BidiRun bidiRun) {
                int visualStart = bidiRun.charStart;
                int visualEnd = bidiRun.charEnd;

                addVisualRuns(visualStart, visualEnd, runList);
            }
        });

        return createComposedLine(mSpanned, start, end, runList,
                                  Paragraphs.levelOfChar(mBidiParagraphs, start));
    }

    ComposedLine createCompactLine(int start, int end, float extent, byte[] breaks, BreakMode mode,
                                   TruncationPlace place, ComposedLine token) {
        float tokenlessWidth = extent - token.getWidth();

        switch (place) {
        case START:
            return createStartTruncatedLine(start, end, tokenlessWidth, breaks, mode, token);

        case MIDDLE:
            return createMiddleTruncatedLine(start, end, tokenlessWidth, breaks, mode, token);

        case END:
            return createEndTruncatedLine(start, end, tokenlessWidth, breaks, mode, token);
        }

        return null;
    }

    private class TruncationHandler implements Paragraphs.RunConsumer {

        final int charStart;
        final int charEnd;
        final int skipStart;
        final int skipEnd;
        final List<GlyphRun> runList;

        int leadingTokenIndex = -1;
        int trailingTokenIndex = -1;

        TruncationHandler(int charStart, int charEnd, int skipStart, int skipEnd, List<GlyphRun> runList) {
            this.charStart = charStart;
            this.charEnd = charEnd;
            this.skipStart = skipStart;
            this.skipEnd = skipEnd;
            this.runList = runList;
        }

        @Override
        public void accept(BidiRun bidiRun) {
            int visualStart = bidiRun.charStart;
            int visualEnd = bidiRun.charEnd;

            if (bidiRun.isRightToLeft()) {
                // Handle second part of characters.
                if (visualEnd >= skipEnd) {
                    addVisualRuns(Math.max(visualStart, skipEnd), visualEnd, runList);

                    if (visualStart < skipEnd) {
                        trailingTokenIndex = runList.size();
                    }
                }

                // Handle first part of characters.
                if (visualStart <= skipStart) {
                    if (visualEnd > skipStart) {
                        leadingTokenIndex = runList.size();
                    }

                    addVisualRuns(visualStart, Math.min(visualEnd, skipStart), runList);
                }
            } else {
                // Handle first part of characters.
                if (visualStart <= skipStart) {
                    addVisualRuns(visualStart, Math.min(visualEnd, skipStart), runList);

                    if (visualEnd > skipStart) {
                        leadingTokenIndex = runList.size();
                    }
                }

                // Handle second part of characters.
                if (visualEnd >= skipEnd) {
                    if (visualStart < skipEnd) {
                        trailingTokenIndex = runList.size();
                    }

                    addVisualRuns(Math.max(visualStart, skipEnd), visualEnd, runList);
                }
            }
        }

        void addAllRuns() {
            Paragraphs.iterateLineRuns(mBidiParagraphs, charStart, charEnd, this);
        }
    }

    private ComposedLine createStartTruncatedLine(int start, int end, float tokenlessWidth,
                                                  byte[] breaks, BreakMode mode, ComposedLine token) {
        int truncatedStart = BreakResolver.suggestBackwardBreak(mSpanned, mIntrinsicRuns, breaks, start, end, tokenlessWidth, mode);
        if (truncatedStart > start) {
            ArrayList<GlyphRun> runList = new ArrayList<>();
            int tokenInsertIndex = 0;

            if (truncatedStart < end) {
                TruncationHandler truncationHandler = new TruncationHandler(start, end, start, truncatedStart, runList);
                truncationHandler.addAllRuns();

                tokenInsertIndex = truncationHandler.trailingTokenIndex;
            }
            addTokenRuns(token, runList, tokenInsertIndex);

            return createComposedLine(mSpanned, truncatedStart, end, runList,
                                      Paragraphs.levelOfChar(mBidiParagraphs, truncatedStart));
        }

        return createSimpleLine(truncatedStart, end);
    }

    private ComposedLine createMiddleTruncatedLine(int start, int end, float tokenlessWidth,
                                                   byte[] breaks, BreakMode mode, ComposedLine token) {
        float halfWidth = tokenlessWidth / 2.0f;
        int firstMidEnd = BreakResolver.suggestForwardBreak(mSpanned, mIntrinsicRuns, breaks, start, end, halfWidth, mode);
        int secondMidStart = BreakResolver.suggestBackwardBreak(mSpanned, mIntrinsicRuns, breaks, start, end, halfWidth, mode);

        if (firstMidEnd < secondMidStart) {
            // Exclude inner whitespaces as truncation token replaces them.
            firstMidEnd = StringUtils.getTrailingWhitespaceStart(mSpanned, start, firstMidEnd);
            secondMidStart = StringUtils.getLeadingWhitespaceEnd(mSpanned, secondMidStart, end);

            ArrayList<GlyphRun> runList = new ArrayList<>();
            int tokenInsertIndex = 0;

            if (start < firstMidEnd || secondMidStart < end) {
                TruncationHandler truncationHandler = new TruncationHandler(start, end, firstMidEnd, secondMidStart, runList);
                truncationHandler.addAllRuns();

                tokenInsertIndex = truncationHandler.leadingTokenIndex;
            }
            addTokenRuns(token, runList, tokenInsertIndex);

            return createComposedLine(mSpanned, start, end, runList,
                                      Paragraphs.levelOfChar(mBidiParagraphs, start));
        }

        return createSimpleLine(start, end);
    }

    private ComposedLine createEndTruncatedLine(int start, int end, float tokenlessWidth,
                                                byte[] breaks, BreakMode mode, ComposedLine token) {
        int truncatedEnd = BreakResolver.suggestForwardBreak(mSpanned, mIntrinsicRuns, breaks, start, end, tokenlessWidth, mode);
        if (truncatedEnd < end) {
            // Exclude trailing whitespaces as truncation token replaces them.
            truncatedEnd = StringUtils.getTrailingWhitespaceStart(mSpanned, start, truncatedEnd);

            ArrayList<GlyphRun> runList = new ArrayList<>();
            int tokenInsertIndex = 0;

            if (start < truncatedEnd) {
                TruncationHandler truncationHandler = new TruncationHandler(start, end, truncatedEnd, end, runList);
                truncationHandler.addAllRuns();

                tokenInsertIndex = truncationHandler.leadingTokenIndex;
            }
            addTokenRuns(token, runList, tokenInsertIndex);

            return createComposedLine(mSpanned, start, truncatedEnd, runList,
                                      Paragraphs.levelOfChar(mBidiParagraphs, start));
        }

        return createSimpleLine(start, truncatedEnd);
    }

    private void addTokenRuns(ComposedLine token, List<GlyphRun> runList, int insertIndex) {
        for (GlyphRun truncationRun : token.getRuns()) {
            GlyphRun modifiedRun = new GlyphRun(truncationRun);
            runList.add(insertIndex, modifiedRun);

            insertIndex++;
        }
    }

    private void addVisualRuns(int visualStart, int visualEnd, List<GlyphRun> runList) {
        if (visualStart < visualEnd) {
            // ASSUMPTIONS:
            //      - Visual range may fall in one or more glyph runs.
            //      - Consecutive intrinsic runs may have same bidi level.

            int insertIndex = runList.size();
            IntrinsicRun previousRun = null;

            do {
                int runIndex = Runs.binarySearch(mIntrinsicRuns, visualStart);

                IntrinsicRun intrinsicRun = mIntrinsicRuns.get(runIndex);
                int feasibleStart = Math.max(intrinsicRun.charStart, visualStart);
                int feasibleEnd = Math.min(intrinsicRun.charEnd, visualEnd);

                boolean forward = false;

                if (previousRun != null) {
                    byte bidiLevel = intrinsicRun.bidiLevel;
                    if (bidiLevel != previousRun.bidiLevel || (bidiLevel & 1) == 0) {
                        insertIndex = runList.size();
                        forward = true;
                    }
                }

                int spanStart = feasibleStart;
                while (spanStart < feasibleEnd) {
                    int spanEnd = mSpanned.nextSpanTransition(spanStart, feasibleEnd, Object.class);
                    Object[] spans = mSpanned.getSpans(spanStart, spanEnd, Object.class);

                    GlyphRun glyphRun = createGlyphRun(intrinsicRun, spanStart, spanEnd, spans);
                    runList.add(insertIndex, glyphRun);

                    if (forward) {
                        insertIndex++;
                    }

                    spanStart = spanEnd;
                }

                previousRun = intrinsicRun;
                visualStart = feasibleEnd;
            } while (visualStart != visualEnd);
        }
    }
}
