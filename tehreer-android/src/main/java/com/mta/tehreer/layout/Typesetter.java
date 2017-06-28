/*
 * Copyright (C) 2017 Muhammad Tayyab Akram
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

import com.mta.tehreer.bidi.BaseDirection;
import com.mta.tehreer.bidi.BidiAlgorithm;
import com.mta.tehreer.bidi.BidiLine;
import com.mta.tehreer.bidi.BidiParagraph;
import com.mta.tehreer.bidi.BidiRun;
import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.internal.text.StringUtils;
import com.mta.tehreer.internal.text.TopSpanIterator;
import com.mta.tehreer.layout.style.TypeSizeSpan;
import com.mta.tehreer.layout.style.TypefaceSpan;
import com.mta.tehreer.sfnt.SfntTag;
import com.mta.tehreer.sfnt.ShapingEngine;
import com.mta.tehreer.sfnt.ShapingResult;
import com.mta.tehreer.sfnt.WritingDirection;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Represents a typesetter which performs text layout. It can be used to create lines, perform line
 * breaking, and do other contextual analysis based on the characters in the string.
 */
public class Typesetter {

    private static final float DEFAULT_FONT_SIZE = 16.0f;

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

    private static final byte BREAK_TYPE_NONE = 0;
    private static final byte BREAK_TYPE_LINE = 1 << 0;
    private static final byte BREAK_TYPE_CHARACTER = 1 << 2;
    private static final byte BREAK_TYPE_PARAGRAPH = 1 << 4;

    private static byte specializeBreakType(byte breakType, boolean forward) {
        return (byte) (forward ? breakType : breakType << 1);
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
     * @throws IllegalArgumentException if <code>text</code> is null or empty, or typeface is null
     */
	public Typesetter(String text, Typeface typeface, float typeSize) {
        if (text == null || text.length() == 0) {
            throw new IllegalArgumentException("Text is null or empty");
        }

        SpannableString spanned = new SpannableString(text);
        spanned.setSpan(new TypefaceSpan(typeface), 0, text.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        spanned.setSpan(new TypeSizeSpan(typeSize), 0, text.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        init(text, spanned);
	}

    /**
     * Constructs the typesetter object using a spanned text.
     *
     * @param spanned The spanned text to typeset.
     *
     * @throws IllegalArgumentException if <code>spanned</code> is null or empty
     */
    public Typesetter(Spanned spanned) {
        if (spanned == null || spanned.length() == 0) {
            throw new IllegalArgumentException("Spanned text is null or empty");
        }

        init(StringUtils.copyString(spanned), spanned);
    }

    private void init(String text, Spanned spanned) {
        mText = text;
        mSpanned = spanned;
        mBreakRecord = new byte[text.length()];
        mBidiParagraphs = new ArrayList<>();
        mIntrinsicRuns = new ArrayList<>();

        resolveBreaks();
        resolveBidi();
    }

    public String getText() {
        return mText;
    }

    private void resolveBreaks() {
        resolveBreaks(BreakIterator.getLineInstance(), BREAK_TYPE_LINE);
        resolveBreaks(BreakIterator.getCharacterInstance(), BREAK_TYPE_CHARACTER);
    }

    private void resolveBreaks(BreakIterator breakIterator, byte breakType) {
        breakIterator.setText(mText);
        breakIterator.first();

        byte forwardType = specializeBreakType(breakType, true);
        int charNext;

        while ((charNext = breakIterator.next()) != BreakIterator.DONE) {
            mBreakRecord[charNext - 1] |= forwardType;
        }

        breakIterator.last();
        byte backwardType = specializeBreakType(breakType, false);
        int charIndex;

        while ((charIndex = breakIterator.previous()) != BreakIterator.DONE) {
            mBreakRecord[charIndex] |= backwardType;
        }
    }

    private void resolveBidi() {
        // TODO: Analyze script runs.

        BidiAlgorithm bidiAlgorithm = null;
        ShapingEngine shapingEngine = null;

        try {
            bidiAlgorithm = new BidiAlgorithm(mText);
            shapingEngine = new ShapingEngine();

            BaseDirection baseDirection = BaseDirection.DEFAULT_LEFT_TO_RIGHT;
            byte forwardType = specializeBreakType(BREAK_TYPE_PARAGRAPH, true);
            byte backwardType = specializeBreakType(BREAK_TYPE_PARAGRAPH, false);

            int paragraphStart = 0;
            int suggestedEnd = mText.length();

            while (paragraphStart != suggestedEnd) {
                BidiParagraph paragraph = bidiAlgorithm.createParagraph(paragraphStart, suggestedEnd, baseDirection);
                for (BidiRun bidiRun : paragraph.getLogicalRuns()) {
                    int scriptTag = SfntTag.make(bidiRun.isRightToLeft() ? "arab" : "latn");
                    WritingDirection writingDirection = ShapingEngine.getScriptDefaultDirection(scriptTag);

                    shapingEngine.setScriptTag(scriptTag);
                    shapingEngine.setWritingDirection(writingDirection);

                    resolveTypefaces(bidiRun.charStart, bidiRun.charEnd,
                                     bidiRun.embeddingLevel, shapingEngine);
                }
                mBidiParagraphs.add(paragraph);

                mBreakRecord[paragraph.getCharStart()] |= backwardType;
                mBreakRecord[paragraph.getCharEnd() - 1] |= forwardType;

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

    private void resolveTypefaces(int charStart, int charEnd, byte bidiLevel,
                                  ShapingEngine shapingEngine) {
        Spanned spanned = mSpanned;
        TopSpanIterator<TypefaceSpan> iterator = new TopSpanIterator<>(spanned, charStart, charEnd, TypefaceSpan.class);

        while (iterator.hasNext()) {
            TypefaceSpan spanObject = iterator.next();
            int spanStart = iterator.getSpanStart();
            int spanEnd = iterator.getSpanEnd();

            if (spanObject == null || spanObject.getTypeface() == null) {
                throw new IllegalArgumentException("No typeface is specified for range ["
                                                   + spanStart + ".." + spanEnd + ")");
            }

            resolveFonts(spanStart, spanEnd, bidiLevel, shapingEngine, spanObject.getTypeface());
        }
    }

    private void resolveFonts(int charStart, int charEnd, byte bidiLevel,
                              ShapingEngine shapingEngine, Typeface typeface) {
        Spanned spanned = mSpanned;
        TopSpanIterator<TypeSizeSpan> iterator = new TopSpanIterator<>(spanned, charStart, charEnd, TypeSizeSpan.class);

        while (iterator.hasNext()) {
            TypeSizeSpan spanObject = iterator.next();
            int spanStart = iterator.getSpanStart();
            int spanEnd = iterator.getSpanEnd();

            float typeSize;

            if (spanObject == null) {
                typeSize = DEFAULT_FONT_SIZE;
            } else {
                typeSize = spanObject.getSize();
                if (typeSize < 0.0f) {
                    typeSize = 0.0f;
                }
            }

            IntrinsicRun intrinsicRun = resolveGlyphs(spanStart, spanEnd, bidiLevel, shapingEngine, typeface, typeSize);
            mIntrinsicRuns.add(intrinsicRun);
        }
    }

    private IntrinsicRun resolveGlyphs(int charStart, int charEnd, byte bidiLevel,
                                       ShapingEngine shapingEngine, Typeface typeface, float typeSize) {
        shapingEngine.setTypeface(typeface);
        shapingEngine.setTypeSize(typeSize);

        ShapingResult shapingResult = null;
        IntrinsicRun intrinsicRun = null;

        try {
            shapingResult = shapingEngine.shapeText(mText, charStart, charEnd);
            intrinsicRun = new IntrinsicRun(shapingResult, typeface, typeSize, bidiLevel, shapingEngine.getWritingDirection());
        } finally {
            if (shapingResult != null) {
                shapingResult.dispose();
            }
        }

        return intrinsicRun;
    }

    private void verifyTextRange(int charStart, int charEnd) {
        if (charStart < 0) {
            throw new IllegalArgumentException("Char Start: " + charStart);
        }
        if (charEnd > mText.length()) {
            throw new IllegalArgumentException("Char End: " + charEnd
                                               + ", Text Length: " + mText.length());
        }
        if (charStart >= charEnd) {
            throw new IllegalArgumentException("Bad Range: [" + charStart + ".." + charEnd + ")");
        }
    }

    private int indexOfBidiParagraph(final int charIndex) {
        return Collections.binarySearch(mBidiParagraphs, null, new Comparator<BidiParagraph>() {
            @Override
            public int compare(BidiParagraph obj1, BidiParagraph obj2) {
                if (charIndex < obj1.getCharStart()) {
                    return 1;
                }

                if (charIndex >= obj1.getCharEnd()) {
                    return -1;
                }

                return 0;
            }
        });
    }

    private int indexOfGlyphRun(final int charIndex) {
        return Collections.binarySearch(mIntrinsicRuns, null, new Comparator<IntrinsicRun>() {
            @Override
            public int compare(IntrinsicRun obj1, IntrinsicRun obj2) {
                if (charIndex < obj1.charStart) {
                    return 1;
                }

                if (charIndex >= obj1.charEnd) {
                    return -1;
                }

                return 0;
            }
        });
    }

    private byte getCharParagraphLevel(int charIndex) {
        int paragraphIndex = indexOfBidiParagraph(charIndex);
        BidiParagraph charParagraph = mBidiParagraphs.get(paragraphIndex);
        return charParagraph.getBaseLevel();
    }

    private float measureChars(int charStart, int charEnd) {
        float measuredWidth = 0.0f;

        if (charEnd > charStart) {
            int runIndex = indexOfGlyphRun(charStart);

            do {
                IntrinsicRun intrinsicRun = mIntrinsicRuns.get(runIndex);
                int glyphStart = intrinsicRun.charGlyphStart(charStart);
                int glyphEnd;

                int segmentEnd = Math.min(charEnd, intrinsicRun.charEnd);
                glyphEnd = intrinsicRun.charGlyphEnd(segmentEnd - 1);

                measuredWidth += intrinsicRun.measureGlyphs(glyphStart, glyphEnd);

                charStart = segmentEnd;
                runIndex++;
            } while (charStart < charEnd);
        }

        return measuredWidth;
    }

    private int findForwardBreak(byte breakType, int charStart, int charEnd, float maxWidth) {
        int forwardBreak = charStart;
        int charIndex = charStart;
        float measuredWidth = 0.0f;

        byte mustType = specializeBreakType(BREAK_TYPE_PARAGRAPH, true);
        breakType = specializeBreakType(breakType, true);

        while (charIndex < charEnd) {
            byte charType = mBreakRecord[charIndex];

            // Handle necessary break.
            if ((charType & mustType) == mustType) {
                int segmentEnd = charIndex + 1;

                measuredWidth += measureChars(forwardBreak, segmentEnd);
                if (measuredWidth <= maxWidth) {
                    forwardBreak = segmentEnd;
                }
                break;
            }

            // Handle optional break.
            if ((charType & breakType) == breakType) {
                int segmentEnd = charIndex + 1;

                measuredWidth += measureChars(forwardBreak, segmentEnd);
                if (measuredWidth > maxWidth) {
                    int whitespaceStart = StringUtils.getTrailingWhitespaceStart(mText, forwardBreak, segmentEnd);
                    float whitespaceWidth = measureChars(whitespaceStart, segmentEnd);

                    // Break if excluding whitespaces width helps.
                    if ((measuredWidth - whitespaceWidth) <= maxWidth) {
                        forwardBreak = segmentEnd;
                    }
                    break;
                }

                forwardBreak = segmentEnd;
            }

            charIndex++;
        }

        return forwardBreak;
    }

    private int findBackwardBreak(byte breakType, int charStart, int charEnd, float maxWidth) {
        int backwardBreak = charEnd;
        int charIndex = charEnd - 1;
        float measuredWidth = 0.0f;

        byte mustType = specializeBreakType(BREAK_TYPE_PARAGRAPH, false);
        breakType = specializeBreakType(breakType, false);

        while (charIndex >= charStart) {
            byte charType = mBreakRecord[charIndex];

            // Handle necessary break.
            if ((charType & mustType) == mustType) {
                measuredWidth += measureChars(backwardBreak, charIndex);
                if (measuredWidth <= maxWidth) {
                    backwardBreak = charIndex;
                }
                break;
            }

            // Handle optional break.
            if ((charType & breakType) == breakType) {
                measuredWidth += measureChars(charIndex, backwardBreak);
                if (measuredWidth > maxWidth) {
                    int whitespaceStart = StringUtils.getTrailingWhitespaceStart(mText, charIndex, backwardBreak);
                    float whitespaceWidth = measureChars(whitespaceStart, backwardBreak);

                    // Break if excluding trailing whitespaces helps.
                    if ((measuredWidth - whitespaceWidth) <= maxWidth) {
                        backwardBreak = charIndex;
                    }
                    break;
                }

                backwardBreak = charIndex;
            }

            charIndex--;
        }

        return backwardBreak;
    }

    private int suggestForwardCharBreak(int charStart, int charEnd, float maxWidth) {
        int forwardBreak = findForwardBreak(BREAK_TYPE_CHARACTER, charStart, charEnd, maxWidth);

        // Take at least one character (grapheme) if max size is too small.
        if (forwardBreak == charStart) {
            for (int i = charStart; i < charEnd; i++) {
                if ((mBreakRecord[i] & BREAK_TYPE_CHARACTER) != 0) {
                    forwardBreak = i + 1;
                    break;
                }
            }

            // Character range does not cover even a single grapheme?
            if (forwardBreak == charStart) {
                forwardBreak = Math.min(charStart + 1, charEnd);
            }
        }

        return forwardBreak;
    }

    private int suggestBackwardCharBreak(int charStart, int charEnd, float maxWidth) {
        int backwardBreak = findBackwardBreak(BREAK_TYPE_CHARACTER, charStart, charEnd, maxWidth);

        // Take at least one character (grapheme) if max size is too small.
        if (backwardBreak == charEnd) {
            for (int i = charEnd - 1; i >= charStart; i++) {
                if ((mBreakRecord[i] & BREAK_TYPE_CHARACTER) != 0) {
                    backwardBreak = i;
                    break;
                }
            }

            // Character range does not cover even a single grapheme?
            if (backwardBreak == charEnd) {
                backwardBreak = Math.max(charEnd - 1, charStart);
            }
        }

        return backwardBreak;
    }

    private int suggestForwardLineBreak(int charStart, int charEnd, float maxWidth) {
        int forwardBreak = findForwardBreak(BREAK_TYPE_LINE, charStart, charEnd, maxWidth);

        // Fallback to character break if no line break occurs in max size.
        if (forwardBreak == charStart) {
            forwardBreak = suggestForwardCharBreak(charStart, charEnd, maxWidth);
        }

        return forwardBreak;
    }

    private int suggestBackwardLineBreak(int charStart, int charEnd, float maxWidth) {
        int backwardBreak = findBackwardBreak(BREAK_TYPE_LINE, charStart, charEnd, maxWidth);

        // Fallback to character break if no line break occurs in max size.
        if (backwardBreak == charEnd) {
            backwardBreak = suggestBackwardCharBreak(charStart, charEnd, maxWidth);
        }

        return backwardBreak;
    }

    private int suggestForwardTruncationBreak(int charStart, int charEnd, float maxWidth, WrapMode wrapMode) {
        switch (wrapMode) {
        case WORD:
            return suggestForwardLineBreak(charStart, charEnd, maxWidth);

        case CHARACTER:
            return suggestForwardCharBreak(charStart, charEnd, maxWidth);
        }

        return -1;
    }

    private int suggestBackwardTruncationBreak(int charStart, int charEnd, float maxWidth, WrapMode wrapMode) {
        switch (wrapMode) {
        case WORD:
            return suggestBackwardLineBreak(charStart, charEnd, maxWidth);

        case CHARACTER:
            return suggestBackwardCharBreak(charStart, charEnd, maxWidth);
        }

        return -1;
    }

    /**
     * Suggests a typographic character line break index based on the width provided. This can be
     * used by the caller to implement a different line breaking scheme, such as hyphenation.
     *
     * @param charStart The starting index for the typographic character break calculations.
     * @param maxWidth The requested typographic character break width.
     * @return The index (exclusive) that would cause the character break.
     *
     * @throws IllegalArgumentException if any of the following is true:
     *         <ul>
     *             <li><code>charStart</code> is negative</li>
     *             <li><code>charStart</code> is greater than or equal to the length of source
     *                 text</li>
     *             <li><code>maxWidth</code> is less than or equal to zero</li>
     *         </ul>
     */
    public int suggestCharBoundary(int charStart, float maxWidth) {
        if (charStart < 0 || charStart >= mText.length()) {
            throw new IndexOutOfBoundsException("Char Start: " + charStart);
        }
        if (maxWidth <= 0.0f) {
            throw new IllegalArgumentException("Max Width: " + maxWidth);
        }

        return suggestForwardCharBreak(charStart, mText.length(), maxWidth);
    }

    /**
     * Suggests a contextual line break index based on the width provided.
     *
     * @param charStart The starting index for the line break calculations.
     * @param maxWidth The requested line break width.
     * @return The index (exclusive) that would cause the line break.
     *
     * @throws IllegalArgumentException if any of the following is true:
     *         <ul>
     *             <li><code>charStart</code> is negative</li>
     *             <li><code>charStart</code> is greater than or equal to the length of source
     *                 text</li>
     *             <li><code>maxWidth</code> is less than or equal to zero</li>
     *         </ul>
     */
    public int suggestLineBoundary(int charStart, float maxWidth) {
        if (charStart < 0) {
            throw new IllegalArgumentException("Char Start: " + charStart);
        }
        if (charStart > mText.length()) {
            throw new IllegalArgumentException("Char Start: " + charStart
                                               + ", Text Length: " + mText.length());
        }
        if (maxWidth <= 0.0f) {
            throw new IllegalArgumentException("Max Width: " + maxWidth);
        }

        return suggestForwardLineBreak(charStart, mText.length(), maxWidth);
    }

    /**
     * Creates a line of specified string range.
     *
     * @param charStart The index to first character of the line in source text.
     * @param charEnd The index after the last character of the line in source text.
     * @return The new line object.
     *
     * @throws IllegalArgumentException if <code>charStart</code> is negative, or
     *         <code>charEnd</code> is greater than the length of source text, or
     *         <code>charStart</code> is greater than or equal to <code>charEnd</code>
     */
	public ComposedLine createLine(int charStart, int charEnd) {
        verifyTextRange(charStart, charEnd);

        ArrayList<GlyphRun> lineRuns = new ArrayList<>();
        addContinuousLineRuns(charStart, charEnd, lineRuns);

		return new ComposedLine(mText, charStart, charEnd, lineRuns, getCharParagraphLevel(charStart));
	}

    /**
     * Creates a line of specified string range, truncating it if it overflows the max width.
     *
     * @param charStart The index to first character of the line in source text.
     * @param charEnd The index after the last character of the line in source text.
     * @param maxWidth The width at which truncation will begin. The line will be truncated if its
     *                 width is greater than the width passed in this.
     * @param truncationType The type of truncation to perform if needed.
     * @param truncationToken This token will be added to the point where truncation took place to
     *                        indicate that the line was truncated. Usually, the truncation token is
     *                        the ellipsis character (U+2026). The line specified in
     *                        <code>truncationToken</code> should have a width less than the width
     *                        specified by the <code>maxWidth</code> parameter.
     * @return The new line which is truncated if it overflows the <code>maxWidth</code>.
     *
     * @throws NullPointerException if <code>truncationType</code> is null, or
     *         <code>truncationToken</code> is null
     * @throws IllegalArgumentException if any of the following is true:
     *         <ul>
     *             <li><code>charStart</code> is negative</li>
     *             <li><code>charEnd</code> is greater than the length of source text</li>
     *             <li><code>charStart</code> is greater than of equal to <code>charEnd</code></li>
     *             <li><code>maxWidth</code> is less than the width of line specified in
     *                 <code>truncationToken</code></li>
     *         </ul>
     */
    public ComposedLine createTruncatedLine(int charStart, int charEnd, float maxWidth,
                                            WrapMode wrapMode, TruncationType truncationType,
                                            ComposedLine truncationToken) {
        verifyTextRange(charStart, charEnd);
        if (wrapMode == null) {
            throw new NullPointerException("Wrap mode is null");
        }
        if (truncationType == null) {
            throw new NullPointerException("Truncation type is null");
        }
        if (truncationToken == null) {
            throw new NullPointerException("Truncation token is null");
        }
        float tokenWidth = truncationToken.getWidth();
        if (maxWidth < tokenWidth) {
            throw new IllegalArgumentException("Max Width: " + maxWidth + ", Token Width: " + tokenWidth);
        }

        float tokenlessWidth = maxWidth - tokenWidth;

        switch (truncationType) {
        case START:
            return createStartTruncatedLine(charStart, charEnd, tokenlessWidth,
                                            wrapMode, truncationToken);

        case MIDDLE:
            return createMiddleTruncatedLine(charStart, charEnd, tokenlessWidth,
                                             wrapMode, truncationToken);

        case END:
            return createEndTruncatedLine(charStart, charEnd, tokenlessWidth,
                                          wrapMode, truncationToken);
        }

        return null;
    }

    private interface BidiRunConsumer {
        void accept(BidiRun bidiRun);
    }

    private class StartTruncationHandler implements BidiRunConsumer {

        private final int charStart;
        private final int charEnd;
        private final List<GlyphRun> runList;

        private int firstRunIndex = -1;

        public StartTruncationHandler(int charStart, int charEnd, List<GlyphRun> runList) {
            this.charStart = charStart;
            this.charEnd = charEnd;
            this.runList = runList;
        }

        @Override
        public void accept(BidiRun bidiRun) {
            int visualStart = bidiRun.charStart;
            int visualEnd = bidiRun.charEnd;

            if (charStart == visualStart) {
                firstRunIndex = runList.size();
            }

            addVisualRuns(visualStart, visualEnd, runList);
        }

        public int addAllRuns() {
            addContinuousLineRuns(charStart, charEnd, this);

            int tokenInsertIndex = firstRunIndex;
            GlyphRun firstGlyphRun = runList.get(firstRunIndex);
            IntrinsicRun firstIntrinsicRun = firstGlyphRun.getGlyphRun();

            if (firstIntrinsicRun.charStart < charStart) {
                // If previous character belongs to the same glyph run, follow its direction.
                if ((firstIntrinsicRun.bidiLevel & 1) == 1) {
                    tokenInsertIndex += 1;
                }
            } else {
                // If previous character belongs to a different glyph run, follow paragraph direction.
                int paragraphIndex = indexOfBidiParagraph(charStart);
                BidiParagraph bidiParagraph = mBidiParagraphs.get(paragraphIndex);
                int paragraphLevel = bidiParagraph.getBaseLevel();

                if ((paragraphLevel & 1) == 1) {
                    tokenInsertIndex += 1;
                }
            }

            return tokenInsertIndex;
        }
    }

    private ComposedLine createStartTruncatedLine(int charStart, int charEnd, float tokenlessWidth,
                                                  WrapMode wrapMode, ComposedLine truncationToken) {
        int truncatedStart = suggestBackwardTruncationBreak(charStart, charEnd, tokenlessWidth, wrapMode);
        if (truncatedStart > charStart) {
            ArrayList<GlyphRun> runList = new ArrayList<>();
            int tokenInsertIndex = 0;

            if (truncatedStart < charEnd) {
                StartTruncationHandler truncationHandler = new StartTruncationHandler(truncatedStart, charEnd, runList);
                tokenInsertIndex = truncationHandler.addAllRuns();
            }
            addTruncationTokenRuns(truncationToken, runList, tokenInsertIndex);

            return new ComposedLine(mText, truncatedStart, charEnd, runList, getCharParagraphLevel(truncatedStart));
        }

        return createLine(truncatedStart, charEnd);
    }

    private ComposedLine createMiddleTruncatedLine(int charStart, int charEnd, float tokenlessWidth,
                                                   WrapMode wrapMode, ComposedLine truncationToken) {
        float halfWidth = tokenlessWidth / 2.0f;
        int firstMidEnd = suggestForwardTruncationBreak(charStart, charEnd, halfWidth, wrapMode);
        int secondMidStart = suggestBackwardTruncationBreak(charStart, charEnd, halfWidth, wrapMode);

        if (firstMidEnd < secondMidStart) {
            ArrayList<GlyphRun> runList = new ArrayList<>();

            // Exclude inner whitespaces as truncation token replaces them.
            firstMidEnd = StringUtils.getTrailingWhitespaceStart(mText, charStart, firstMidEnd);
            secondMidStart = StringUtils.getLeadingWhitespaceEnd(mText, secondMidStart, charEnd);

            if (charStart < firstMidEnd) {
                addContinuousLineRuns(charStart, firstMidEnd, runList);
            }
            addTruncationTokenRuns(truncationToken, runList, runList.size());
            if (secondMidStart < charEnd) {
                addContinuousLineRuns(secondMidStart, charEnd, runList);
            }

            return new ComposedLine(mText, charStart, charEnd, runList, getCharParagraphLevel(charStart));
        }

        return createLine(charStart, charEnd);
    }

    private class EndTruncationHandler implements BidiRunConsumer {

        private final int charStart;
        private final int charEnd;
        private final List<GlyphRun> runList;

        private int lastRunIndex = -1;

        public EndTruncationHandler(int charStart, int charEnd, List<GlyphRun> runList) {
            this.charStart = charStart;
            this.charEnd = charEnd;
            this.runList = runList;
        }

        @Override
        public void accept(BidiRun bidiRun) {
            int visualStart = bidiRun.charStart;
            int visualEnd = bidiRun.charEnd;

            if (visualEnd == charEnd) {
                lastRunIndex = runList.size();
            }

            addVisualRuns(visualStart, visualEnd, runList);
        }

        public int addAllRuns() {
            addContinuousLineRuns(charStart, charEnd, this);

            int tokenInsertIndex = lastRunIndex;
            GlyphRun lastGlyphRun = runList.get(lastRunIndex);
            IntrinsicRun lastIntrinsicRun = lastGlyphRun.getGlyphRun();

            if (lastIntrinsicRun.charEnd > charEnd) {
                // If next character belongs to the same glyph run, follow its direction.
                if ((lastIntrinsicRun.bidiLevel & 1) == 0) {
                    tokenInsertIndex += 1;
                }
            } else {
                // If next character belongs to a different glyph run, follow paragraph direction.
                int paragraphIndex = indexOfBidiParagraph(charStart);
                BidiParagraph bidiParagraph = mBidiParagraphs.get(paragraphIndex);
                int paragraphLevel = bidiParagraph.getBaseLevel();

                if ((paragraphLevel & 1) == 0) {
                    tokenInsertIndex += 1;
                }
            }

            return tokenInsertIndex;
        }
    }

    private ComposedLine createEndTruncatedLine(int charStart, int charEnd, float tokenlessWidth,
                                                WrapMode wrapMode, ComposedLine truncationToken) {
        int truncatedEnd = suggestForwardTruncationBreak(charStart, charEnd, tokenlessWidth, wrapMode);
        if (truncatedEnd < charEnd) {
            ArrayList<GlyphRun> runList = new ArrayList<>();
            int tokenInsertIndex = 0;

            // Exclude trailing whitespaces as truncation token replaces them.
            truncatedEnd = StringUtils.getTrailingWhitespaceStart(mText, charStart, truncatedEnd);

            if (charStart < truncatedEnd) {
                EndTruncationHandler truncationHandler = new EndTruncationHandler(charStart, truncatedEnd, runList);
                tokenInsertIndex = truncationHandler.addAllRuns();
            }
            addTruncationTokenRuns(truncationToken, runList, tokenInsertIndex);

            return new ComposedLine(mText, charStart, truncatedEnd, runList, getCharParagraphLevel(charStart));
        }

        return createLine(charStart, truncatedEnd);
    }

    private void addTruncationTokenRuns(ComposedLine truncationToken, ArrayList<GlyphRun> runList, int insertIndex) {
        for (GlyphRun truncationRun : truncationToken.getRuns()) {
            GlyphRun modifiedRun = new GlyphRun(truncationRun);
            runList.add(insertIndex, modifiedRun);

            insertIndex++;
        }
    }

    private void addContinuousLineRuns(int charStart, int charEnd, BidiRunConsumer runConsumer) {
        int paragraphIndex = indexOfBidiParagraph(charStart);
        int feasibleStart;
        int feasibleEnd;

        do {
            BidiParagraph bidiParagraph = mBidiParagraphs.get(paragraphIndex);
            feasibleStart = Math.max(bidiParagraph.getCharStart(), charStart);
            feasibleEnd = Math.min(bidiParagraph.getCharEnd(), charEnd);

            BidiLine bidiLine = bidiParagraph.createLine(feasibleStart, feasibleEnd);
            for (BidiRun bidiRun : bidiLine.getVisualRuns()) {
                runConsumer.accept(bidiRun);
            }
            bidiLine.dispose();

            paragraphIndex++;
        } while (feasibleEnd != charEnd);
    }

    private void addContinuousLineRuns(int charStart, int charEnd, final List<GlyphRun> runList) {
        addContinuousLineRuns(charStart, charEnd, new BidiRunConsumer() {
            @Override
            public void accept(BidiRun bidiRun) {
                int visualStart = bidiRun.charStart;
                int visualEnd = bidiRun.charEnd;

                addVisualRuns(visualStart, visualEnd, runList);
            }
        });
    }

    private void addVisualRuns(int visualStart, int visualEnd, List<GlyphRun> runList) {
        // ASSUMPTIONS:
        //      - The length of visual range is always greater than zero.
        //      - Visual range may fall in one or more glyph runs.
        //      - Consecutive glyphs runs may have same bidi level.

        int insertIndex = runList.size();
        IntrinsicRun previousRun = null;

        do {
            int runIndex = indexOfGlyphRun(visualStart);

            IntrinsicRun intrinsicRun = mIntrinsicRuns.get(runIndex);
            int feasibleStart = Math.max(intrinsicRun.charStart, visualStart);
            int feasibleEnd = Math.min(intrinsicRun.charEnd, visualEnd);

            GlyphRun glyphRun = new GlyphRun(intrinsicRun, feasibleStart, feasibleEnd);
            if (previousRun != null) {
                byte bidiLevel = intrinsicRun.bidiLevel;
                if (bidiLevel != previousRun.bidiLevel || (bidiLevel & 1) == 0) {
                    insertIndex = runList.size();
                }
            }
            runList.add(insertIndex, glyphRun);

            previousRun = intrinsicRun;
            visualStart = feasibleEnd;
        } while (visualStart != visualEnd);
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
        verifyTextRange(charStart, charEnd);
        if (frameRect.isEmpty()) {
            throw new IllegalArgumentException("Frame rect is empty");
        }
        if (textAlignment == null) {
            throw new NullPointerException("Text alignment is null");
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
        float frameHeight = frameRect.height();

        ArrayList<ComposedLine> frameLines = new ArrayList<>();
        int lineStart = charStart;
        float lineY = frameRect.top;

        while (lineStart != charEnd) {
            int lineEnd = suggestLineBoundary(lineStart, frameWidth);
            ComposedLine composedLine = createLine(lineStart, lineEnd);

            float lineX = composedLine.getFlushPenOffset(flushFactor, frameWidth);
            float lineAscent = composedLine.getAscent();
            float lineHeight = lineAscent + composedLine.getDescent();

            if ((lineY + lineHeight) > frameHeight) {
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
