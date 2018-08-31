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

import android.support.annotation.NonNull;
import android.text.Spanned;

import com.mta.tehreer.internal.collections.JFloatArrayList;
import com.mta.tehreer.internal.collections.JFloatArrayPointList;
import com.mta.tehreer.internal.collections.JIntArrayList;
import com.mta.tehreer.internal.layout.BreakResolver;
import com.mta.tehreer.internal.layout.CaretEdgeList;
import com.mta.tehreer.internal.layout.ClusterMap;
import com.mta.tehreer.internal.layout.IntrinsicRun;
import com.mta.tehreer.internal.layout.ParagraphCollection;
import com.mta.tehreer.internal.layout.RunCollection;
import com.mta.tehreer.internal.util.StringUtils;
import com.mta.tehreer.unicode.BidiRun;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class LineResolver {
    private Spanned mSpanned;
    private ParagraphCollection mBidiParagraphs;
    private RunCollection mIntrinsicRuns;

    LineResolver() {
    }

    void reset(Spanned spanned, ParagraphCollection paragraphs, RunCollection runs) {
        mSpanned = spanned;
        mBidiParagraphs = paragraphs;
        mIntrinsicRuns = runs;
    }

    static GlyphRun createGlyphRun(@NonNull IntrinsicRun intrinsicRun, int spanStart, int spanEnd,
                                   @NonNull Object[] spans) {
        int clusterStart = intrinsicRun.clusterStart(spanStart);
        int clusterEnd = intrinsicRun.clusterEnd(spanEnd - 1);

        int startExtra = spanStart - clusterStart;
        int endExtra = clusterEnd - spanEnd;

        int[] glyphRange = new int[2];
        intrinsicRun.loadGlyphRange(spanStart, spanEnd, glyphRange);

        int glyphOffset = glyphRange[0];
        int glyphCount = glyphRange[1] - glyphOffset;

        int chunkOffset = clusterStart - intrinsicRun.charStart;
        int chunkLength = clusterEnd - clusterStart;

        return new GlyphRun(spanStart, spanEnd, startExtra, endExtra, Arrays.asList(spans),
                            intrinsicRun.isBackward, intrinsicRun.bidiLevel,
                            intrinsicRun.writingDirection, intrinsicRun.typeface, intrinsicRun.typeSize,
                            intrinsicRun.ascent, intrinsicRun.descent, intrinsicRun.leading,
                            new JIntArrayList(intrinsicRun.glyphIds, glyphOffset, glyphCount),
                            new JFloatArrayPointList(intrinsicRun.glyphOffsets, glyphOffset, glyphCount),
                            new JFloatArrayList(intrinsicRun.glyphAdvances, glyphOffset, glyphCount),
                            new ClusterMap(intrinsicRun.clusterMap, chunkOffset, chunkLength, glyphOffset),
                            new CaretEdgeList(intrinsicRun.charExtents, chunkOffset, chunkLength,
                                              startExtra, endExtra, intrinsicRun.isBackward, intrinsicRun.isRTL()));
    }

    static @NonNull ComposedLine createComposedLine(@NonNull CharSequence text, int charStart, int charEnd,
                                                    @NonNull List<GlyphRun> runList, byte paragraphLevel) {
        float lineAscent = 0.0f;
        float lineDescent = 0.0f;
        float lineLeading = 0.0f;
        float lineExtent = 0.0f;

        int trailingWhitespaceStart = StringUtils.getTrailingWhitespaceStart(text, charStart, charEnd);
        float trailingWhitespaceExtent = 0.0f;

        int runCount = runList.size();
        for (int i = 0; i < runCount; i++) {
            GlyphRun glyphRun = runList.get(i);
            glyphRun.setOriginX(lineExtent);

            float runAscent = glyphRun.getAscent();
            float runDescent = glyphRun.getDescent();
            float runLeading = glyphRun.getLeading();

            int runCharStart = glyphRun.getCharStart();
            int runCharEnd = glyphRun.getCharEnd();
            int runGlyphCount = glyphRun.getGlyphCount();
            float runExtent = glyphRun.getWidth();

            if (trailingWhitespaceStart >= runCharStart && trailingWhitespaceStart < runCharEnd) {
                int wsLeadingIndex = glyphRun.getLeadingGlyphIndex(trailingWhitespaceStart);
                int wsTrailingIndex = glyphRun.getTrailingGlyphIndex(runCharEnd - 1);

                int wsGlyphStart = Math.min(wsLeadingIndex, wsTrailingIndex);
                int wsGlyphEnd = Math.max(wsLeadingIndex, wsTrailingIndex) + 1;
                float wsExtent = glyphRun.computeTypographicExtent(wsGlyphStart, wsGlyphEnd);

                trailingWhitespaceExtent += wsExtent;
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

    @NonNull ComposedLine createSimpleLine(int start, int end) {
        final List<GlyphRun> runList = new ArrayList<>();
        mBidiParagraphs.forEachLineRun(start, end, new ParagraphCollection.RunConsumer() {
            @Override
            public void accept(@NonNull BidiRun bidiRun) {
                int visualStart = bidiRun.charStart;
                int visualEnd = bidiRun.charEnd;

                addVisualRuns(visualStart, visualEnd, runList);
            }
        });

        return createComposedLine(mSpanned, start, end, runList,
                                  mBidiParagraphs.charLevel(start));
    }

    @NonNull ComposedLine createCompactLine(int start, int end, float extent,
                                            @NonNull byte[] breaks,
                                            @NonNull BreakMode mode,
                                            @NonNull TruncationPlace place,
                                            @NonNull ComposedLine token) {
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

    private class TruncationHandler implements ParagraphCollection.RunConsumer {
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
        public void accept(@NonNull BidiRun bidiRun) {
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
            mBidiParagraphs.forEachLineRun(charStart, charEnd, this);
        }
    }

    private @NonNull ComposedLine createStartTruncatedLine(int start, int end, float tokenlessWidth,
                                                           @NonNull byte[] breaks,
                                                           @NonNull BreakMode mode,
                                                           @NonNull ComposedLine token) {
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
                                      mBidiParagraphs.charLevel(truncatedStart));
        }

        return createSimpleLine(truncatedStart, end);
    }

    private @NonNull ComposedLine createMiddleTruncatedLine(int start, int end, float tokenlessWidth,
                                                            @NonNull byte[] breaks,
                                                            @NonNull BreakMode mode,
                                                            @NonNull ComposedLine token) {
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
                                      mBidiParagraphs.charLevel(start));
        }

        return createSimpleLine(start, end);
    }

    private @NonNull ComposedLine createEndTruncatedLine(int start, int end, float tokenlessWidth,
                                                         @NonNull byte[] breaks,
                                                         @NonNull BreakMode mode,
                                                         @NonNull ComposedLine token) {
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
                                      mBidiParagraphs.charLevel(start));
        }

        return createSimpleLine(start, truncatedEnd);
    }

    private void addTokenRuns(@NonNull ComposedLine token, @NonNull List<GlyphRun> runList, int insertIndex) {
        for (GlyphRun truncationRun : token.getRuns()) {
            GlyphRun modifiedRun = new GlyphRun(truncationRun);
            runList.add(insertIndex, modifiedRun);

            insertIndex++;
        }
    }

    private void addVisualRuns(int visualStart, int visualEnd, @NonNull List<GlyphRun> runList) {
        if (visualStart < visualEnd) {
            // ASSUMPTIONS:
            //      - Visual range may fall in one or more glyph runs.
            //      - Consecutive intrinsic runs may have same bidi level.

            int insertIndex = runList.size();
            IntrinsicRun previousRun = null;

            do {
                int runIndex = mIntrinsicRuns.binarySearch(visualStart);

                IntrinsicRun intrinsicRun = mIntrinsicRuns.get(runIndex);
                int feasibleStart = Math.max(intrinsicRun.charStart, visualStart);
                int feasibleEnd = Math.min(intrinsicRun.charEnd, visualEnd);

                byte bidiLevel = intrinsicRun.bidiLevel;
                boolean isForwardRun = ((bidiLevel & 1) == 0);

                if (previousRun != null) {
                    if (bidiLevel != previousRun.bidiLevel || isForwardRun) {
                        insertIndex = runList.size();
                    }
                }

                int spanStart = feasibleStart;
                while (spanStart < feasibleEnd) {
                    int spanEnd = mSpanned.nextSpanTransition(spanStart, feasibleEnd, Object.class);
                    Object[] spans = mSpanned.getSpans(spanStart, spanEnd, Object.class);

                    GlyphRun glyphRun = createGlyphRun(intrinsicRun, spanStart, spanEnd, spans);
                    runList.add(insertIndex, glyphRun);

                    if (isForwardRun) {
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
