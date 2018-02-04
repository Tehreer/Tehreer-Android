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

import android.graphics.RectF;
import android.text.SpannableString;
import android.text.Spanned;

import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.internal.layout.BreakResolver;
import com.mta.tehreer.internal.layout.IntrinsicRun;
import com.mta.tehreer.internal.layout.ShapeResolver;
import com.mta.tehreer.internal.layout.TokenResolver;
import com.mta.tehreer.internal.util.Paragraphs;
import com.mta.tehreer.internal.util.Runs;
import com.mta.tehreer.internal.util.StringUtils;
import com.mta.tehreer.layout.style.TypeSizeSpan;
import com.mta.tehreer.layout.style.TypefaceSpan;
import com.mta.tehreer.unicode.BidiParagraph;
import com.mta.tehreer.unicode.BidiRun;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a typesetter which performs text layout. It can be used to create lines, perform line
 * breaking, and do other contextual analysis based on the characters in the string.
 */
public class Typesetter {

    private class Finalizable {

        @Override
        protected void finalize() throws Throwable {
            try {
                dispose();
            } finally {
                super.finalize();
            }
        }
    }

    private final Finalizable finalizable = new Finalizable();
    private String mText;
    private Spanned mSpanned;
    private byte[] mBreakRecord;
    private ArrayList<BidiParagraph> mBidiParagraphs;
    private ArrayList<IntrinsicRun> mIntrinsicRuns;

    /**
     * Constructs the typesetter object using given text, typeface and type size.
     *
     * @param text The text to typeset.
     * @param typeface The typeface to use.
     * @param typeSize The type size to apply.
     *
     * @throws NullPointerException if <code>text</code> is null, or <code>typeface</code> is null.
     * @throws IllegalArgumentException if <code>text</code> is empty.
     */
	public Typesetter(String text, Typeface typeface, float typeSize) {
        if (text == null) {
            throw new NullPointerException("Text is null");
        }
        if (typeface == null) {
            throw new NullPointerException("Typeface is null");
        }
        if (text.length() == 0) {
            throw new IllegalArgumentException("Text is empty");
        }

        SpannableString spanned = new SpannableString(text);
        spanned.setSpan(new TypefaceSpan(typeface), 0, text.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        spanned.setSpan(new TypeSizeSpan(typeSize), 0, text.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        init(text, spanned, null);
	}

    /**
     * Constructs the typesetter object using a spanned text.
     *
     * @param spanned The spanned text to typeset.
     *
     * @throws NullPointerException if <code>spanned</code> is null.
     * @throws IllegalArgumentException if <code>spanned</code> is empty.
     */
    public Typesetter(Spanned spanned) {
        this(spanned, null);
    }

    public Typesetter(Spanned spanned, List<Object> defaultSpans) {
        if (spanned == null) {
            throw new NullPointerException("Spanned text is null");
        }
        if (spanned.length() == 0) {
            throw new IllegalArgumentException("Spanned text is empty");
        }

        init(StringUtils.copyString(spanned), spanned, defaultSpans);
    }

    private void init(String text, Spanned spanned, List<Object> defaultSpans) {
        mText = text;
        mSpanned = spanned;
        mBreakRecord = new byte[text.length()];
        mBidiParagraphs = new ArrayList<>();
        mIntrinsicRuns = new ArrayList<>();

        if (defaultSpans == null) {
            defaultSpans = Collections.EMPTY_LIST;
        }

        BreakResolver.fillBreaks(mText, mBreakRecord);
        ShapeResolver.fillRuns(mText, mSpanned, defaultSpans, mBreakRecord,
                               mBidiParagraphs, mIntrinsicRuns);
    }

    /**
     * Returns the spanned source text for which this typesetter object was created.
     *
     * @return The spanned source text for which this typesetter object was created.
     */
    public Spanned getSpanned() {
        return mSpanned;
    }

    private String checkRange(int charStart, int charEnd) {
        if (charStart < 0) {
            return ("Char Start: " + charStart);
        }
        if (charEnd > mText.length()) {
            return ("Char End: " + charEnd + ", Text Length: " + mText.length());
        }
        if (charStart >= charEnd) {
            return ("Bad Range: [" + charStart + ".." + charEnd + ")");
        }

        return null;
    }

    /**
     * Suggests a forward break index based on the provided range and width. The measurement
     * proceeds from first character to last character. If there is still room after measuring all
     * characters, then last index is returned. Otherwise, break index is returned.
     *
     * @param charStart The index to the first character (inclusive) for break calculations.
     * @param charEnd The index to the last character (exclusive) for break calculations.
     * @param breakWidth The requested break width.
     * @param breakMode The requested break mode.
     * @return The index (exclusive) that would cause the break.
     *
     * @throws NullPointerException if <code>breakMode</code> is null.
     * @throws IllegalArgumentException if <code>charStart</code> is negative, or
     *         <code>charEnd</code> is greater than the length of source text, or
     *         <code>charStart</code> is greater than or equal to <code>charEnd</code>
     */
    public int suggestForwardBreak(int charStart, int charEnd, float breakWidth, BreakMode breakMode) {
        if (breakMode == null) {
            throw new NullPointerException("Break mode is null");
        }
        String rangeError = checkRange(charStart, charEnd);
        if (rangeError != null) {
            throw new IllegalArgumentException(rangeError);
        }

        switch (breakMode) {
        case CHARACTER:
            return BreakResolver.suggestForwardCharBreak(mText, mIntrinsicRuns, mBreakRecord,
                                                         charStart, charEnd, breakWidth);

        case LINE:
            return BreakResolver.suggestForwardLineBreak(mText, mIntrinsicRuns, mBreakRecord,
                                                         charStart, charEnd, breakWidth);
        }

        return -1;
    }

    /**
     * Suggests a backward break index based on the provided range and width. The measurement
     * proceeds from last character to first character. If there is still room after measuring all
     * characters, then first index is returned. Otherwise, break index is returned.
     *
     * @param charStart The index to the first character (inclusive) for break calculations.
     * @param charEnd The index to the last character (exclusive) for break calculations.
     * @param breakWidth The requested break width.
     * @param breakMode The requested break mode.
     * @return The index (inclusive) that would cause the break.
     *
     * @throws NullPointerException if <code>breakMode</code> is null.
     * @throws IllegalArgumentException if <code>charStart</code> is negative, or
     *         <code>charEnd</code> is greater than the length of source text, or
     *         <code>charStart</code> is greater than or equal to <code>charEnd</code>
     */
    public int suggestBackwardBreak(int charStart, int charEnd, float breakWidth, BreakMode breakMode) {
        if (breakMode == null) {
            throw new NullPointerException("Break mode is null");
        }
        String rangeError = checkRange(charStart, charEnd);
        if (rangeError != null) {
            throw new IllegalArgumentException(rangeError);
        }

        switch (breakMode) {
        case CHARACTER:
            return BreakResolver.suggestBackwardCharBreak(mText, mIntrinsicRuns, mBreakRecord,
                                                          charStart, charEnd, breakWidth);

        case LINE:
            return BreakResolver.suggestBackwardLineBreak(mText, mIntrinsicRuns, mBreakRecord,
                                                          charStart, charEnd, breakWidth);
        }

        return -1;
    }

    /**
     * Creates a simple line of specified string range.
     *
     * @param charStart The index to first character of the line in source text.
     * @param charEnd The index after the last character of the line in source text.
     * @return The new line object.
     *
     * @throws IllegalArgumentException if <code>charStart</code> is negative, or
     *         <code>charEnd</code> is greater than the length of source text, or
     *         <code>charStart</code> is greater than or equal to <code>charEnd</code>
     */
	public ComposedLine createSimpleLine(int charStart, int charEnd) {
        String rangeError = checkRange(charStart, charEnd);
        if (rangeError != null) {
            throw new IllegalArgumentException(rangeError);
        }

        ArrayList<GlyphRun> lineRuns = new ArrayList<>();
        addContinuousLineRuns(charStart, charEnd, lineRuns);

		return new ComposedLine(mText, charStart, charEnd, lineRuns,
                                Paragraphs.levelOfChar(mBidiParagraphs, charStart));
	}

    /**
     * Creates a line of specified string range, truncating it with ellipsis character (U+2026) or
     * three dots if it overflows the max width.
     *
     * @param charStart The index to first character of the line in source text.
     * @param charEnd The index after the last character of the line in source text.
     * @param maxWidth The width at which truncation will begin.
     * @param breakMode The truncation mode to be used on the line.
     * @param truncationPlace The place of truncation for the line.
     * @return The new line which is truncated if it overflows the <code>maxWidth</code>.
     *
     * @throws NullPointerException if <code>breakMode</code> is null, or
     *         <code>truncationPlace</code> is null.
     * @throws IllegalArgumentException if any of the following is true:
     *         <ul>
     *             <li><code>charStart</code> is negative</li>
     *             <li><code>charEnd</code> is greater than the length of source text</li>
     *             <li><code>charStart</code> is greater than or equal to <code>charEnd</code></li>
     *         </ul>
     */
    public ComposedLine createTruncatedLine(int charStart, int charEnd, float maxWidth,
                                            BreakMode breakMode, TruncationPlace truncationPlace) {
        if (breakMode == null) {
            throw new NullPointerException("Break mode is null");
        }
        if (truncationPlace == null) {
            throw new NullPointerException("Truncation place is null");
        }
        String rangeError = checkRange(charStart, charEnd);
        if (rangeError != null) {
            throw new IllegalArgumentException(rangeError);
        }

        return createCompactLine(charStart, charEnd, maxWidth, breakMode, truncationPlace,
                TokenResolver.createToken(mSpanned, charStart, charEnd, truncationPlace, null));
    }

    /**
     * Creates a line of specified string range, truncating it if it overflows the max width.
     *
     * @param charStart The index to first character of the line in source text.
     * @param charEnd The index after the last character of the line in source text.
     * @param maxWidth The width at which truncation will begin.
     * @param breakMode The truncation mode to be used on the line.
     * @param truncationPlace The place of truncation for the line.
     * @param truncationToken The token to indicate the line truncation.
     * @return The new line which is truncated if it overflows the <code>maxWidth</code>.
     *
     * @throws NullPointerException if <code>breakMode</code> is null, or
     *         <code>truncationPlace</code> is null, or <code>truncationToken</code> is null
     * @throws IllegalArgumentException if any of the following is true:
     *         <ul>
     *             <li><code>charStart</code> is negative</li>
     *             <li><code>charEnd</code> is greater than the length of source text</li>
     *             <li><code>charStart</code> is greater than or equal to <code>charEnd</code></li>
     *         </ul>
     */
    public ComposedLine createTruncatedLine(int charStart, int charEnd, float maxWidth,
                                            BreakMode breakMode, TruncationPlace truncationPlace,
                                            String truncationToken) {
        if (breakMode == null) {
            throw new NullPointerException("Break mode is null");
        }
        if (truncationPlace == null) {
            throw new NullPointerException("Truncation place is null");
        }
        if (truncationToken == null) {
            throw new NullPointerException("Truncation token is null");
        }
        String rangeError = checkRange(charStart, charEnd);
        if (rangeError != null) {
            throw new IllegalArgumentException(rangeError);
        }
        if (truncationToken.length() == 0) {
            throw new IllegalArgumentException("Truncation token is empty");
        }

        return createCompactLine(charStart, charEnd, maxWidth, breakMode, truncationPlace,
                TokenResolver.createToken(mSpanned, charStart, charEnd, truncationPlace, truncationToken));
    }

    /**
     * Creates a line of specified string range, truncating it if it overflows the max width.
     *
     * @param charStart The index to first character of the line in source text.
     * @param charEnd The index after the last character of the line in source text.
     * @param maxWidth The width at which truncation will begin.
     * @param breakMode The truncation mode to be used on the line.
     * @param truncationPlace The place of truncation for the line.
     * @param truncationToken The token to indicate the line truncation.
     * @return The new line which is truncated if it overflows the <code>maxWidth</code>.
     *
     * @throws NullPointerException if <code>breakMode</code> is null, or
     *         <code>truncationPlace</code> is null, or <code>truncationToken</code> is null
     * @throws IllegalArgumentException if any of the following is true:
     *         <ul>
     *             <li><code>charStart</code> is negative</li>
     *             <li><code>charEnd</code> is greater than the length of source text</li>
     *             <li><code>charStart</code> is greater than or equal to <code>charEnd</code></li>
     *         </ul>
     */
    public ComposedLine createTruncatedLine(int charStart, int charEnd, float maxWidth,
                                            BreakMode breakMode, TruncationPlace truncationPlace,
                                            ComposedLine truncationToken) {
        if (breakMode == null) {
            throw new NullPointerException("Break mode is null");
        }
        if (truncationPlace == null) {
            throw new NullPointerException("Truncation place is null");
        }
        if (truncationToken == null) {
            throw new NullPointerException("Truncation token is null");
        }
        String rangeError = checkRange(charStart, charEnd);
        if (rangeError != null) {
            throw new IllegalArgumentException(rangeError);
        }

        return createCompactLine(charStart, charEnd, maxWidth, breakMode, truncationPlace, truncationToken);
    }

    public ComposedLine createCompactLine(int charStart, int charEnd, float maxWidth,
                                          BreakMode breakMode, TruncationPlace truncationPlace,
                                          ComposedLine truncationToken) {
        float tokenlessWidth = maxWidth - truncationToken.getWidth();

        switch (truncationPlace) {
        case START:
            return createStartTruncatedLine(charStart, charEnd, tokenlessWidth,
                                            breakMode, truncationToken);

        case MIDDLE:
            return createMiddleTruncatedLine(charStart, charEnd, tokenlessWidth,
                                             breakMode, truncationToken);

        case END:
            return createEndTruncatedLine(charStart, charEnd, tokenlessWidth,
                                          breakMode, truncationToken);
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
                                                  BreakMode breakMode, ComposedLine truncationToken) {
        int truncatedStart = suggestBackwardBreak(charStart, charEnd, tokenlessWidth, breakMode);
        if (truncatedStart > charStart) {
            ArrayList<GlyphRun> runList = new ArrayList<>();
            int tokenInsertIndex = 0;

            if (truncatedStart < charEnd) {
                TruncationHandler truncationHandler = new TruncationHandler(charStart, charEnd, charStart, truncatedStart, runList);
                truncationHandler.addAllRuns();

                tokenInsertIndex = truncationHandler.trailingTokenIndex;
            }
            addTruncationTokenRuns(truncationToken, runList, tokenInsertIndex);

            return new ComposedLine(mText, truncatedStart, charEnd, runList,
                                    Paragraphs.levelOfChar(mBidiParagraphs, truncatedStart));
        }

        return createSimpleLine(truncatedStart, charEnd);
    }

    private ComposedLine createMiddleTruncatedLine(int charStart, int charEnd, float tokenlessWidth,
                                                   BreakMode breakMode, ComposedLine truncationToken) {
        float halfWidth = tokenlessWidth / 2.0f;
        int firstMidEnd = suggestForwardBreak(charStart, charEnd, halfWidth, breakMode);
        int secondMidStart = suggestBackwardBreak(charStart, charEnd, halfWidth, breakMode);

        if (firstMidEnd < secondMidStart) {
            // Exclude inner whitespaces as truncation token replaces them.
            firstMidEnd = StringUtils.getTrailingWhitespaceStart(mText, charStart, firstMidEnd);
            secondMidStart = StringUtils.getLeadingWhitespaceEnd(mText, secondMidStart, charEnd);

            ArrayList<GlyphRun> runList = new ArrayList<>();
            int tokenInsertIndex = 0;

            if (charStart < firstMidEnd || secondMidStart < charEnd) {
                TruncationHandler truncationHandler = new TruncationHandler(charStart, charEnd, firstMidEnd, secondMidStart, runList);
                truncationHandler.addAllRuns();

                tokenInsertIndex = truncationHandler.leadingTokenIndex;
            }
            addTruncationTokenRuns(truncationToken, runList, tokenInsertIndex);

            return new ComposedLine(mText, charStart, charEnd, runList,
                                    Paragraphs.levelOfChar(mBidiParagraphs, charStart));
        }

        return createSimpleLine(charStart, charEnd);
    }

    private ComposedLine createEndTruncatedLine(int charStart, int charEnd, float tokenlessWidth,
                                                BreakMode breakMode, ComposedLine truncationToken) {
        int truncatedEnd = suggestForwardBreak(charStart, charEnd, tokenlessWidth, breakMode);
        if (truncatedEnd < charEnd) {
            // Exclude trailing whitespaces as truncation token replaces them.
            truncatedEnd = StringUtils.getTrailingWhitespaceStart(mText, charStart, truncatedEnd);

            ArrayList<GlyphRun> runList = new ArrayList<>();
            int tokenInsertIndex = 0;

            if (charStart < truncatedEnd) {
                TruncationHandler truncationHandler = new TruncationHandler(charStart, charEnd, truncatedEnd, charEnd, runList);
                truncationHandler.addAllRuns();

                tokenInsertIndex = truncationHandler.leadingTokenIndex;
            }
            addTruncationTokenRuns(truncationToken, runList, tokenInsertIndex);

            return new ComposedLine(mText, charStart, truncatedEnd, runList,
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

    /**
     * Creates a frame full of lines in the rectangle provided by the <code>frameRect</code>
     * parameter. The typesetter will continue to fill the frame until it either runs out of text or
     * it finds that text no longer fits.
     *
     * @param charStart The index to first character of the frame in source text.
     * @param charEnd The index after the last character of the frame in source text.
     * @param frameRect The rectangle specifying the frame to fill.
     * @param textAlignment The horizontal text alignment of the lines in frame.
     * @return The new frame object.
     */
    public ComposedFrame createFrame(int charStart, int charEnd, RectF frameRect, TextAlignment textAlignment) {
        if (frameRect == null) {
            throw new NullPointerException("Frame rect is null");
        }
        if (textAlignment == null) {
            throw new NullPointerException("Text alignment is null");
        }
        String rangeError = checkRange(charStart, charEnd);
        if (rangeError != null) {
            throw new IllegalArgumentException(rangeError);
        }
        if (frameRect.isEmpty()) {
            throw new IllegalArgumentException("Frame rect is empty");
        }

        float flushFactor;
        switch (textAlignment) {
        case RIGHT:
            flushFactor = 1.0f;
            break;

        case CENTER:
            flushFactor = 0.5f;
            break;

        default:
            flushFactor = 0.0f;
            break;
        }

        float frameWidth = frameRect.width();
        float frameBottom = frameRect.bottom;

        ArrayList<ComposedLine> frameLines = new ArrayList<>();
        int lineStart = charStart;
        float lineY = frameRect.top;

        while (lineStart != charEnd) {
            int lineEnd = suggestForwardBreak(lineStart, charEnd, frameWidth, BreakMode.LINE);
            ComposedLine composedLine = createSimpleLine(lineStart, lineEnd);

            float lineX = composedLine.getFlushPenOffset(flushFactor, frameWidth);
            float lineAscent = composedLine.getAscent();
            float lineHeight = lineAscent + composedLine.getDescent();

            if ((lineY + lineHeight) > frameBottom) {
                break;
            }

            composedLine.setOriginX(frameRect.left + lineX);
            composedLine.setOriginY(lineY + lineAscent);

            frameLines.add(composedLine);

            lineStart = lineEnd;
            lineY += lineHeight;
        }

        return new ComposedFrame(charStart, lineStart, frameLines);
    }

    void dispose() {
        for (BidiParagraph paragraph : mBidiParagraphs) {
            paragraph.dispose();
        }
    }
}
