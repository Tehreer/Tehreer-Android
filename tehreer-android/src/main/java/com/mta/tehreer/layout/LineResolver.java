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

import com.mta.tehreer.internal.layout.BreakResolver;
import com.mta.tehreer.internal.layout.IntrinsicRun;
import com.mta.tehreer.internal.util.Paragraphs;
import com.mta.tehreer.internal.util.Runs;
import com.mta.tehreer.internal.util.StringUtils;
import com.mta.tehreer.unicode.BidiParagraph;
import com.mta.tehreer.unicode.BidiRun;

import java.util.ArrayList;
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

    ComposedLine createSimpleLine(int charStart, int charEnd) {
        ArrayList<GlyphRun> lineRuns = new ArrayList<>();
        addContinuousLineRuns(charStart, charEnd, lineRuns);

        return new ComposedLine(mSpanned, charStart, charEnd, lineRuns,
                Paragraphs.levelOfChar(mBidiParagraphs, charStart));
    }

    ComposedLine createCompactLine(int charStart, int charEnd, float maxWidth,
                                   byte[] breakRecord, BreakMode breakMode,
                                   TruncationPlace truncationPlace, ComposedLine truncationToken) {
        float tokenlessWidth = maxWidth - truncationToken.getWidth();

        switch (truncationPlace) {
        case START:
            return createStartTruncatedLine(charStart, charEnd, tokenlessWidth,
                                            breakRecord, breakMode, truncationToken);

        case MIDDLE:
            return createMiddleTruncatedLine(charStart, charEnd, tokenlessWidth,
                                             breakRecord, breakMode, truncationToken);

        case END:
            return createEndTruncatedLine(charStart, charEnd, tokenlessWidth,
                                          breakRecord, breakMode, truncationToken);
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

    private ComposedLine createStartTruncatedLine(int charStart, int charEnd, float tokenlessWidth,
                                                  byte[] breakRecord, BreakMode breakMode, ComposedLine truncationToken) {
        int truncatedStart = BreakResolver.suggestBackwardBreak(mSpanned, mIntrinsicRuns, breakRecord, charStart, charEnd, tokenlessWidth, breakMode);
        if (truncatedStart > charStart) {
            ArrayList<GlyphRun> runList = new ArrayList<>();
            int tokenInsertIndex = 0;

            if (truncatedStart < charEnd) {
                TruncationHandler truncationHandler = new TruncationHandler(charStart, charEnd, charStart, truncatedStart, runList);
                truncationHandler.addAllRuns();

                tokenInsertIndex = truncationHandler.trailingTokenIndex;
            }
            addTruncationTokenRuns(truncationToken, runList, tokenInsertIndex);

            return new ComposedLine(mSpanned, truncatedStart, charEnd, runList,
                                    Paragraphs.levelOfChar(mBidiParagraphs, truncatedStart));
        }

        return createSimpleLine(truncatedStart, charEnd);
    }

    private ComposedLine createMiddleTruncatedLine(int charStart, int charEnd, float tokenlessWidth,
                                                   byte[] breakRecord, BreakMode breakMode, ComposedLine truncationToken) {
        float halfWidth = tokenlessWidth / 2.0f;
        int firstMidEnd = BreakResolver.suggestForwardBreak(mSpanned, mIntrinsicRuns, breakRecord, charStart, charEnd, halfWidth, breakMode);
        int secondMidStart = BreakResolver.suggestBackwardBreak(mSpanned, mIntrinsicRuns, breakRecord, charStart, charEnd, halfWidth, breakMode);

        if (firstMidEnd < secondMidStart) {
            // Exclude inner whitespaces as truncation token replaces them.
            firstMidEnd = StringUtils.getTrailingWhitespaceStart(mSpanned, charStart, firstMidEnd);
            secondMidStart = StringUtils.getLeadingWhitespaceEnd(mSpanned, secondMidStart, charEnd);

            ArrayList<GlyphRun> runList = new ArrayList<>();
            int tokenInsertIndex = 0;

            if (charStart < firstMidEnd || secondMidStart < charEnd) {
                TruncationHandler truncationHandler = new TruncationHandler(charStart, charEnd, firstMidEnd, secondMidStart, runList);
                truncationHandler.addAllRuns();

                tokenInsertIndex = truncationHandler.leadingTokenIndex;
            }
            addTruncationTokenRuns(truncationToken, runList, tokenInsertIndex);

            return new ComposedLine(mSpanned, charStart, charEnd, runList,
                                    Paragraphs.levelOfChar(mBidiParagraphs, charStart));
        }

        return createSimpleLine(charStart, charEnd);
    }

    private ComposedLine createEndTruncatedLine(int charStart, int charEnd, float tokenlessWidth,
                                                byte[] breakRecord, BreakMode breakMode, ComposedLine truncationToken) {
        int truncatedEnd = BreakResolver.suggestForwardBreak(mSpanned, mIntrinsicRuns, breakRecord, charStart, charEnd, tokenlessWidth, breakMode);
        if (truncatedEnd < charEnd) {
            // Exclude trailing whitespaces as truncation token replaces them.
            truncatedEnd = StringUtils.getTrailingWhitespaceStart(mSpanned, charStart, truncatedEnd);

            ArrayList<GlyphRun> runList = new ArrayList<>();
            int tokenInsertIndex = 0;

            if (charStart < truncatedEnd) {
                TruncationHandler truncationHandler = new TruncationHandler(charStart, charEnd, truncatedEnd, charEnd, runList);
                truncationHandler.addAllRuns();

                tokenInsertIndex = truncationHandler.leadingTokenIndex;
            }
            addTruncationTokenRuns(truncationToken, runList, tokenInsertIndex);

            return new ComposedLine(mSpanned, charStart, truncatedEnd, runList,
                                    Paragraphs.levelOfChar(mBidiParagraphs, charStart));
        }

        return createSimpleLine(charStart, truncatedEnd);
    }

    private void addTruncationTokenRuns(ComposedLine truncationToken, ArrayList<GlyphRun> runList, int insertIndex) {
        for (GlyphRun truncationRun : truncationToken.getRuns()) {
            GlyphRun modifiedRun = new GlyphRun(truncationRun);
            runList.add(insertIndex, modifiedRun);

            insertIndex++;
        }
    }

    private void addContinuousLineRuns(int charStart, int charEnd, final List<GlyphRun> runList) {
        Paragraphs.iterateLineRuns(mBidiParagraphs, charStart, charEnd, new Paragraphs.RunConsumer() {
            @Override
            public void accept(BidiRun bidiRun) {
                int visualStart = bidiRun.charStart;
                int visualEnd = bidiRun.charEnd;

                addVisualRuns(visualStart, visualEnd, runList);
            }
        });
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

                    GlyphRun glyphRun = new GlyphRun(intrinsicRun, spanStart, spanEnd, spans);
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
