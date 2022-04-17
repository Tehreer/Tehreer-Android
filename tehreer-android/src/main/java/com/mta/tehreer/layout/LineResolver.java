/*
 * Copyright (C) 2018-2021 Muhammad Tayyab Akram
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

import androidx.annotation.NonNull;

import com.mta.tehreer.collections.FloatList;
import com.mta.tehreer.internal.layout.BreakResolver;
import com.mta.tehreer.internal.layout.IntrinsicRun;
import com.mta.tehreer.internal.layout.JustifiedRun;
import com.mta.tehreer.internal.layout.ParagraphCollection;
import com.mta.tehreer.internal.layout.ReplacementRun;
import com.mta.tehreer.internal.layout.RunCollection;
import com.mta.tehreer.internal.layout.IntrinsicRunSlice;
import com.mta.tehreer.internal.layout.TextRun;
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

    static @NonNull GlyphRun createGlyphRun(@NonNull TextRun textRun, int spanStart, int spanEnd,
                                            @NonNull Object[] spans) {
        if (textRun instanceof IntrinsicRun) {
            textRun = new IntrinsicRunSlice((IntrinsicRun) textRun, spanStart, spanEnd,
                                              Arrays.asList(spans));
        }

        return new GlyphRun(textRun);
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

            int runStart = glyphRun.getCharStart();
            int runEnd = glyphRun.getCharEnd();
            float runExtent = glyphRun.getWidth();

            int wsStart = Math.max(runStart, trailingWhitespaceStart);
            int wsEnd = Math.min(runEnd, charEnd);
            if (wsStart < wsEnd) {
                trailingWhitespaceExtent = glyphRun.computeRangeDistance(wsStart, wsEnd);
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
            TextRun previousRun = null;

            do {
                int runIndex = mIntrinsicRuns.binarySearch(visualStart);

                TextRun textRun = mIntrinsicRuns.get(runIndex);
                int feasibleStart = Math.max(textRun.getCharStart(), visualStart);
                int feasibleEnd = Math.min(textRun.getCharEnd(), visualEnd);

                byte bidiLevel = textRun.getBidiLevel();
                boolean isForwardRun = ((bidiLevel & 1) == 0);

                if (previousRun != null) {
                    if (bidiLevel != previousRun.getBidiLevel() || isForwardRun) {
                        insertIndex = runList.size();
                    }
                }

                int spanStart = feasibleStart;
                while (spanStart < feasibleEnd) {
                    int spanEnd = mSpanned.nextSpanTransition(spanStart, feasibleEnd, Object.class);
                    Object[] spans = mSpanned.getSpans(spanStart, spanEnd, Object.class);

                    GlyphRun glyphRun = createGlyphRun(textRun, spanStart, spanEnd, spans);
                    runList.add(insertIndex, glyphRun);

                    if (isForwardRun) {
                        insertIndex++;
                    }

                    spanStart = spanEnd;
                }

                previousRun = textRun;
                visualStart = feasibleEnd;
            } while (visualStart != visualEnd);
        }
    }

    public ComposedLine createJustifiedLine(int charStart, int charEnd,
                                            float justificationFactor,
                                            float justificationWidth) {
        final int wordStart = StringUtils.getLeadingWhitespaceEnd(mSpanned, charStart, charEnd);
        final int wordEnd = StringUtils.getTrailingWhitespaceStart(mSpanned, charStart, charEnd);

        final float actualWidth = mIntrinsicRuns.measureChars(charStart, charEnd);
        final float extraWidth = justificationWidth - actualWidth;
        final float availableWidth = extraWidth * justificationFactor;

        final int innerSpaceCount = computeSpaceCount(wordStart, wordEnd);
        final float spaceAddition = availableWidth / innerSpaceCount;

        final List<GlyphRun> runList = new ArrayList<>();
        mBidiParagraphs.forEachLineRun(charStart, charEnd, new ParagraphCollection.RunConsumer() {
            @Override
            public void accept(@NonNull BidiRun bidiRun) {
                addVisualRuns(bidiRun.charStart, bidiRun.charEnd, runList);
            }
        });

        final int runCount = runList.size();
        for (int i = 0; i < runCount; i++) {
            final GlyphRun glyphRun = runList.get(i);
            final TextRun textRun = glyphRun.getTextRun();
            if (textRun instanceof ReplacementRun) {
                continue;
            }

            float[] glyphAdvances = glyphRun.getGlyphAdvances().toArray();

            final int runStart = Math.max(wordStart, glyphRun.getCharStart());
            final int runEnd = Math.min(wordEnd, glyphRun.getCharEnd());

            for (int j = runStart; j < runEnd;) {
                final int spaceStart = StringUtils.getNextSpace(mSpanned, j, runEnd);
                final int spaceEnd = StringUtils.getLeadingWhitespaceEnd(mSpanned, spaceStart, runEnd);

                j = spaceEnd;

                if (spaceStart == spaceEnd) {
                    continue;
                }

                final int[] glyphRange = textRun.getGlyphRangeForChars(spaceStart, spaceEnd);
                final int glyphStart = glyphRange[0];
                final int glyphEnd = glyphRange[1];

                final int spaceCount = spaceEnd - spaceStart;
                final int glyphCount = glyphEnd - glyphStart;

                final float distribution = (float) spaceCount / glyphCount;
                final float advanceAddition = spaceAddition * distribution;

                for (int k = glyphStart; k < glyphEnd; k++) {
                    glyphAdvances[k] += advanceAddition;
                }
            }

            final FloatList justifiedAdvances = FloatList.of(glyphAdvances);
            final TextRun justifiedRun = new JustifiedRun(textRun, justifiedAdvances);
            glyphRun.setTextRun(justifiedRun);
        }

        final byte paragraphLevel = mBidiParagraphs.charLevel(charStart);

        return createComposedLine(mSpanned, charStart, charEnd, runList, paragraphLevel);
    }

    private int computeSpaceCount(int startIndex, int endIndex) {
        int spaceCount = 0;

        // FIXME: Exclude replacement runs.

        for (int i = startIndex; i < endIndex; i++) {
            final int spaceStart = StringUtils.getNextSpace(mSpanned, i, endIndex);
            final int spaceEnd = StringUtils.getLeadingWhitespaceEnd(mSpanned, spaceStart, endIndex);

            spaceCount += spaceEnd - spaceStart;

            i = spaceEnd;
        }

        return spaceCount;
    }
}
