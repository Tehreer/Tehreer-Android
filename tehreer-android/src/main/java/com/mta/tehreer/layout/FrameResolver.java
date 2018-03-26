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

import android.graphics.Paint;
import android.graphics.RectF;
import android.text.Layout;
import android.text.Spanned;
import android.text.style.AlignmentSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.LeadingMarginSpan.LeadingMarginSpan2;
import android.text.style.LineHeightSpan;
import android.text.style.ParagraphStyle;

import com.mta.tehreer.internal.layout.BreakResolver;
import com.mta.tehreer.internal.layout.ParagraphCollection;
import com.mta.tehreer.internal.layout.RunCollection;
import com.mta.tehreer.unicode.BidiParagraph;

import java.util.ArrayList;
import java.util.List;

/**
 * This class resolves text frames by using a typesetter object.
 */
public class FrameResolver {

    private LineResolver mLineResolver = new LineResolver();
    private Typesetter mTypesetter;
    private Spanned mSpanned;
    private ParagraphCollection mParagraphs;
    private RunCollection mRuns;
    private byte[] mBreaks;

    private RectF mFrameBounds = new RectF(0, 0, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
    private boolean mFitsHorizontally = false;
    private boolean mFitsVertically = false;
    private TextAlignment mTextAlignment = TextAlignment.INTRINSIC;
    private VerticalAlignment mVerticalAlignment = VerticalAlignment.TOP;
    private BreakMode mTruncationMode = BreakMode.LINE;
    private TruncationPlace mTruncationPlace = TruncationPlace.END;
    private int mMaxLines = 0;
    private float mExtraLineSpacing = 0.0f;
    private float mLineHeightMultiplier = 0.0f;

    /**
     * Constructs a frame resolver object.
     */
    public FrameResolver() {
    }

    /**
     * Returns the typesetter to use for resolving frames.
     *
     * @return The current typesetter object.
     */
    public Typesetter getTypesetter() {
        return mTypesetter;
    }

    /**
     * Sets the typesetter to use for resolving frames.
     *
     * @param typesetter A typesetter object.
     */
    public void setTypesetter(Typesetter typesetter) {
        if (typesetter == null) {
            throw new NullPointerException("Typesetter is null");
        }

        mTypesetter = typesetter;
        mSpanned = typesetter.getSpanned();
        mParagraphs = typesetter.getParagraphs();
        mRuns = typesetter.getRuns();
        mBreaks = typesetter.getBreaks();
        mLineResolver.reset(mSpanned, mParagraphs, mRuns);
    }

    /**
     * Returns the rectangle specifying the frame bounds. The default value is an infinite rectangle
     * at zero origin.
     *
     * @return The current frame rectangle.
     */
    public RectF getFrameBounds() {
        return new RectF(mFrameBounds);
    }

    /**
     * Sets the rectangle specifying the frame bounds. The default value is an infinite rectangle at
     * zero origin.
     *
     * @param frameBounds A rectangle specifying the frame bounds.
     *
     * @throws NullPointerException if <code>frameBounds</code> is null.
     */
    public void setFrameBounds(RectF frameBounds) {
        if (frameBounds == null) {
            throw new NullPointerException("Frame bounds rectangle is null");
        }

        mFrameBounds.set(frameBounds);
    }

    /**
     * Returns whether or not to tightly fit the lines horizontally in a frame. The default value is
     * <code>false</code>.
     *
     * @return <code>true</code> if horizontal fitting is enabled; <code>false</code> otherwise.
     */
    public boolean getFitsHorizontally() {
        return mFitsHorizontally;
    }

    /**
     * Sets whether or not to tightly fit the lines horizontally in a frame. If enabled, the
     * resulting frame will have a minimum width that tightly encloses all the lines of specified
     * text. The default value is <code>false</code>.
     *
     * @param fitsHorizontally A boolean value specifying the horizontal fitting state.
     */
    public void setFitsHorizontally(boolean fitsHorizontally) {
        mFitsHorizontally = fitsHorizontally;
    }

    /**
     * Returns whether or not to tightly fit the lines vertically in a frame. The default value is
     * <code>false</code>.
     *
     * @return <code>true</code> if vertical fitting is enabled; <code>false</code> otherwise.
     */
    public boolean getFitsVertically() {
        return mFitsVertically;
    }

    /**
     * Sets whether or not to tightly fit the lines vertically in a frame. If enabled, the resulting
     * frame will have a minimum height that tightly encloses all the lines of specified text. The
     * default value is <code>false</code>.
     *
     * @param fitsVertically A boolean value specifying the vertical fitting state.
     */
    public void setFitsVertically(boolean fitsVertically) {
        mFitsVertically = fitsVertically;
    }

    /**
     * Returns the text alignment to apply on each line of a frame. The default value is
     * {@link TextAlignment#INTRINSIC}.
     *
     * @return The current text alignment.
     */
    public TextAlignment getTextAlignment() {
        return mTextAlignment;
    }

    /**
     * Sets the text alignment to apply on each line of a frame. The default value is
     * {@link TextAlignment#INTRINSIC}.
     *
     * @param textAlignment A value of {@link TextAlignment}.
     */
    public void setTextAlignment(TextAlignment textAlignment) {
        if (textAlignment == null) {
            throw new NullPointerException("Text alignment is null");
        }

        mTextAlignment = textAlignment;
    }

    /**
     * Returns the vertical alignment to apply on the contents of a frame. The default value is
     * {@link VerticalAlignment#TOP}.
     *
     * @return The current vertical alignment.
     */
    public VerticalAlignment getVerticalAlignment() {
        return mVerticalAlignment;
    }

    /**
     * Sets the vertical alignment to apply on the contents of a frame. The default value is
     * {@link VerticalAlignment#TOP}.
     *
     * @param verticalAlignment A value of {@link VerticalAlignment}.
     *
     * @throws NullPointerException if <code>verticalAlignment</code> is null.
     */
    public void setVerticalAlignment(VerticalAlignment verticalAlignment) {
        if (verticalAlignment == null) {
            throw new NullPointerException("Vertical alignment is null");
        }

        mVerticalAlignment = verticalAlignment;
    }

    /**
     * Returns the truncation mode to apply on the last line of a frame in case of overflow. The
     * default value is {@link BreakMode#LINE}.
     *
     * @return The current truncation mode.
     */
    public BreakMode getTruncationMode() {
        return mTruncationMode;
    }

    /**
     * Sets the truncation mode to apply on the last line of a frame in case of overflow. The
     * default value is {@link BreakMode#LINE}.
     *
     * @param truncationMode A value of {@link BreakMode}.
     *
     * @throws NullPointerException if <code>truncationMode</code> is null.
     */
    public void setTruncationMode(BreakMode truncationMode) {
        if (truncationMode == null) {
            throw new NullPointerException("Truncation mode is null");
        }

        mTruncationMode = truncationMode;
    }

    /**
     * Returns the truncation place for the last line of a frame.
     *
     * @return The current truncation place.
     */
    public TruncationPlace getTruncationPlace() {
        return mTruncationPlace;
    }

    /**
     * Sets the truncation place for the last line of a frame.
     * <p>
     * The truncation is disabled if the value of <code>truncationPlace</code> is <code>null</code>.
     *
     * @param truncationPlace A value of {@link TruncationPlace}.
     */
    public void setTruncationPlace(TruncationPlace truncationPlace) {
        mTruncationPlace = truncationPlace;
    }

    /**
     * Returns the maximum number of lines that a frame should consist of.
     *
     * @return The current max lines.
     */
    public int getMaxLines() {
        return mMaxLines;
    }

    /**
     * Sets the maximum number of lines that a frame should consist of.
     *
     * @param maxLines Maximum number of lines.
     */
    public void setMaxLines(int maxLines) {
        mMaxLines = maxLines;
    }

    /**
     * Returns the extra spacing to add after each line of a frame. It is resolved before line
     * height multiplier. The default value is zero.
     *
     * @return The current extra line spacing.
     *
     * @see #getLineHeightMultiplier()
     */
    public float getExtraLineSpacing() {
        return mExtraLineSpacing;
    }

    /**
     * Sets the extra spacing to add after each line of a frame. It is resolved before line height
     * multiplier. The default value is zero.
     *
     * <p>
     * The extra spacing is added in the leading of each line soon after it is composed.
     *
     * @param extraLineSpacing The extra line spacing in pixels.
     *
     * @see #setLineHeightMultiplier(float)
     */
    public void setExtraLineSpacing(float extraLineSpacing) {
        mExtraLineSpacing = extraLineSpacing;
    }

    /**
     * Returns the height multiplier to apply on each line of a frame. It is resolved after extra
     * line spacing. The default value is one.
     *
     * @return The current line height multiplier.
     *
     * @see #getExtraLineSpacing()
     */
    public float getLineHeightMultiplier() {
        return mLineHeightMultiplier;
    }

    /**
     * Sets the height multiplier to apply on each line of a frame. It is resolved after extra line
     * spacing. The default value is one.
     *
     * <p>
     * The additional spacing is adjusted in such a way that text remains in the middle of the line.
     *
     * @param lineHeightMultiplier The multiplication factor.
     *
     * @see #setExtraLineSpacing(float)
     */
    public void setLineHeightMultiplier(float lineHeightMultiplier) {
        mLineHeightMultiplier = lineHeightMultiplier;
    }

    private float getVerticalMultiplier() {
        switch (mVerticalAlignment) {
        case BOTTOM:
            return 1.0f;

        case MIDDLE:
            return 0.5f;

        default:
            return 0.0f;
        }
    }

    private float getFlushFactor(Layout.Alignment layoutAlignment, byte paragraphLevel) {
        boolean isLTR = ((paragraphLevel & 1) == 0);

        if (layoutAlignment != null) {
            switch (layoutAlignment) {
            case ALIGN_NORMAL:
                return (isLTR ? 0.0f : 1.0f);

            case ALIGN_CENTER:
                return 0.5f;

            case ALIGN_OPPOSITE:
                return (isLTR ? 1.0f : 0.0f);
            }
        } else if (mTextAlignment != null) {
            switch (mTextAlignment) {
            case LEFT:
                return 0.0f;

            case RIGHT:
                return 1.0f;

            case CENTER:
                return 0.5f;

            case INTRINSIC:
                return (isLTR ? 0.0f : 1.0f);

            case EXTRINSIC:
                return (isLTR ? 1.0f : 0.0f);
            }
        }

        return 0.0f;
    }

    /**
     * Creates a frame representing specified string range in source text.
     * <p>
     * The resolver keeps on filling the frame until it either runs out of text or it finds that
     * text no longer fits in frame bounds. The resulting frame consists of at least one line even
     * if frame bounds are smaller.
     *
     * @param charStart The index to first character of the frame in source text.
     * @param charEnd The index after the last character of the line in source text.
     * @return A new composed frame.
     *
     * @throws IllegalArgumentException if <code>charStart</code> is negative, or
     *         <code>charEnd</code> is greater than the length of source text, or
     *         <code>charStart</code> is greater than or equal to <code>charEnd</code>.
     */
    public ComposedFrame createFrame(int charStart, int charEnd) {
        if (charStart < 0) {
            throw new IllegalArgumentException("Char Start: " + charStart);
        }
        if (charEnd > mSpanned.length()) {
            throw new IllegalArgumentException("Char End: " + charEnd + ", Text Length: " + mSpanned.length());
        }
        if (charStart >= charEnd) {
            throw new IllegalArgumentException("Bad Range: [" + charStart + ".." + charEnd + ")");
        }

        FrameFiller frameFiller = new FrameFiller();
        int paragraphIndex = mParagraphs.binarySearch(charStart);

        int segmentStart = charStart;
        int segmentEnd;

        // Iterate over all paragraphs in provided range.
        do {
            BidiParagraph paragraph = mParagraphs.get(paragraphIndex);
            segmentEnd = Math.min(charEnd, paragraph.getCharEnd());

            // Setup the frame filler and add the lines.
            frameFiller.charStart = segmentStart;
            frameFiller.charEnd = segmentEnd;
            frameFiller.baseLevel = paragraph.getBaseLevel();
            frameFiller.addParagraphLines();

            if (frameFiller.filled) {
                break;
            }

            segmentStart = segmentEnd;
            paragraphIndex++;
        } while (segmentStart < charEnd);

        frameFiller.handleTruncation(charEnd);
        frameFiller.resolveAlignments();

        ComposedFrame frame = new ComposedFrame(mSpanned, charStart, frameFiller.frameEnd(), frameFiller.frameLines);
        frame.setContainerRect(mFrameBounds.left, mFrameBounds.top, frameFiller.layoutWidth, frameFiller.layoutHeight);

        return frame;
    }

    private class FrameFiller {

        final List<ComposedLine> frameLines = new ArrayList<>();
        float layoutWidth;
        float layoutHeight;
        int maxLines;

        int charStart;
        int charEnd;
        byte baseLevel;

        ParagraphStyle[] paragraphSpans;
        LineHeightSpan[] pickHeightSpans;
        int[] pickHeightTops;

        int leadingLineCount = 1;
        Paint.FontMetricsInt fontMetrics;

        float lineExtent = 0.0f;
        float leadingOffset = 0.0f;

        float lineTop = 0.0f;
        boolean filled = false;

        float lastFlushFactor = 0.0f;

        FrameFiller() {
            layoutWidth = mFrameBounds.width();
            layoutHeight = mFrameBounds.height();

            maxLines = (mMaxLines > 0 ? mMaxLines : Integer.MAX_VALUE);
        }

        int frameEnd() {
            return frameLines.get(frameLines.size() - 1).getCharEnd();
        }

        int binarySearch(int charIndex) {
            int low = 0;
            int high = frameLines.size() - 1;

            while (low <= high) {
                int mid = (low + high) >>> 1;
                ComposedLine value = frameLines.get(mid);

                if (charIndex >= value.getCharEnd()) {
                    low = mid + 1;
                } else if (charIndex < value.getCharStart()) {
                    high = mid - 1;
                } else {
                    return mid;
                }
            }

            return -1;
        }

        float computeFlushFactor() {
            Layout.Alignment alignment = null;

            // Get the top most alignment.
            for (int i = paragraphSpans.length - 1; i >= 0; i--) {
                if (paragraphSpans[i] instanceof AlignmentSpan) {
                    alignment = ((AlignmentSpan) paragraphSpans[i]).getAlignment();
                    break;
                }
            }

            return getFlushFactor(alignment, baseLevel);
        }

        void resolveLeadingOffset() {
            if ((baseLevel & 1) == 0) {
                leadingOffset = layoutWidth - lineExtent;
            }
        }

        void addParagraphLines() {
            float leadingLineExtent = layoutWidth;
            float trailingLineExtent = layoutWidth;

            // Extract all spans of this paragraph.
            paragraphSpans = mSpanned.getSpans(charStart, charEnd, ParagraphStyle.class);

            // Compute margins for leading and trailing lines.
            for (ParagraphStyle style : paragraphSpans) {
                if (style instanceof LeadingMarginSpan) {
                    LeadingMarginSpan span = (LeadingMarginSpan) style;
                    leadingLineExtent -= span.getLeadingMargin(true);
                    trailingLineExtent -= span.getLeadingMargin(false);

                    if (span instanceof LeadingMarginSpan2) {
                        LeadingMarginSpan2 span2 = (LeadingMarginSpan2) span;
                        int spanTotalLines = span2.getLeadingMarginLineCount();
                        if (spanTotalLines > leadingLineCount) {
                            leadingLineCount = spanTotalLines;
                        }
                    }
                }
            }

            // Extract line height spans and create font metrics if necessary.
            pickHeightSpans = mSpanned.getSpans(charStart, charEnd, LineHeightSpan.class);
            int chooseHeightCount = pickHeightSpans.length;
            if (chooseHeightCount > 0 && fontMetrics == null) {
                fontMetrics = new Paint.FontMetricsInt();
            }

            // Setup array for caching top of first line related to each line height span.
            if (pickHeightTops == null || pickHeightTops.length < chooseHeightCount) {
                pickHeightTops = new int[chooseHeightCount];
            }

            // Compute top of first line related to each line height span.
            for (int i = 0; i < chooseHeightCount; i++) {
                int spanStart = mSpanned.getSpanStart(pickHeightSpans[i]);
                int spanTop = (int) (lineTop + 0.5f);

                // Fix span top in case it starts in a previous paragraph.
                if (spanStart < charStart) {
                    int lineIndex = binarySearch(spanStart);
                    ComposedLine spanLine = frameLines.get(lineIndex);
                    spanTop = (int) (spanLine.getTop() + 0.5f);
                }

                pickHeightTops[i] = spanTop;
            }

            float flushFactor = computeFlushFactor();
            lineExtent = leadingLineExtent;
            resolveLeadingOffset();

            // Iterate over each line of this paragraph.
            int lineStart = charStart;
            while (lineStart != charEnd) {
                int lineEnd = BreakResolver.suggestForwardBreak(mSpanned, mRuns, mBreaks, lineStart, charEnd, lineExtent, BreakMode.LINE);
                ComposedLine composedLine = mLineResolver.createSimpleLine(lineStart, lineEnd);
                prepareLine(composedLine, flushFactor);

                float lineHeight = composedLine.getHeight();

                // Make sure that at least one line is added even if frame is smaller in height.
                if ((lineTop + lineHeight) > layoutHeight && frameLines.size() > 0) {
                    filled = true;
                    return;
                }

                frameLines.add(composedLine);
                lastFlushFactor = flushFactor;

                // Stop the filling process if maximum lines have been added.
                if (frameLines.size() == maxLines) {
                    filled = true;
                    return;
                }

                // Find out extent of next line.
                if (--leadingLineCount <= 0) {
                    lineExtent = trailingLineExtent;
                    resolveLeadingOffset();
                }

                lineStart = lineEnd;
                lineTop += lineHeight;
            }
        }

        void prepareLine(ComposedLine composedLine, float flushFactor) {
            // Resolve line height spans.
            int chooseHeightCount = pickHeightSpans.length;
            for (int i = 0; i < chooseHeightCount; i++) {
                fontMetrics.ascent = (int) -(composedLine.getAscent() + 0.5f);
                fontMetrics.descent = (int) (composedLine.getDescent() + 0.5f);
                fontMetrics.leading = (int) (composedLine.getLeading() + 0.5f);
                fontMetrics.top = fontMetrics.ascent;
                fontMetrics.bottom = fontMetrics.descent;

                LineHeightSpan span = pickHeightSpans[i];
                int lineStart = composedLine.getCharStart();
                int lineEnd = composedLine.getCharEnd();
                int lineTop = (int) (this.lineTop + 0.5f);
                int spanTop = pickHeightTops[i];

                span.chooseHeight(mSpanned, lineStart, lineEnd, spanTop, lineTop, fontMetrics);

                // Override the line metrics.
                composedLine.setAscent(-fontMetrics.ascent);
                composedLine.setDescent(fontMetrics.descent);
                composedLine.setLeading(fontMetrics.leading);
            }

            // Resolve line height multiplier.
            if (mLineHeightMultiplier != 0.0f) {
                float oldHeight = composedLine.getHeight();
                float newHeight = oldHeight * mLineHeightMultiplier;
                float midOffset = (newHeight - oldHeight) / 2.0f;

                // Adjust metrics in such a way that text remains in the middle of line.
                composedLine.setAscent(composedLine.getAscent() + midOffset);
                composedLine.setDescent(composedLine.getDescent() + midOffset);
            }

            // Resolve extra line spacing.
            if (mExtraLineSpacing != 0.0f) {
                composedLine.setLeading(composedLine.getLeading() + mExtraLineSpacing);
            }

            // Compute the origin of line.
            float originX = leadingOffset + composedLine.getFlushPenOffset(flushFactor, lineExtent);
            float originY = lineTop + composedLine.getAscent();

            // Set the origin of line.
            composedLine.setOriginX(originX);
            composedLine.setOriginY(originY);

            // Set supporting properties of line.
            composedLine.setSpans(paragraphSpans);
            composedLine.setFirst(leadingLineCount > 0);
            composedLine.setIntrinsicMargin(layoutWidth - lineExtent);
            composedLine.setFlushFactor(flushFactor);
        }

        void handleTruncation(int frameEnd) {
            if (mTruncationPlace != null) {
                int lastIndex = frameLines.size() - 1;
                ComposedLine lastLine = frameLines.get(lastIndex);

                // No need to truncate if frame range is already covered.
                if (lastLine.getCharEnd() == frameEnd) {
                    return;
                }

                // Move the y to last line's position.
                lineTop = lastLine.getTop();

                // Create the truncated line.
                ComposedLine truncatedLine = mTypesetter.createTruncatedLine(lastLine.getCharStart(), frameEnd, lineExtent, mTruncationMode, mTruncationPlace);
                prepareLine(truncatedLine, lastFlushFactor);

                // Replace the last line with truncated one.
                frameLines.set(lastIndex, truncatedLine);
            }
        }

        void resolveAlignments() {
            // Find out the occupied height.
            int lineCount = frameLines.size();
            ComposedLine lastLine = frameLines.get(lineCount - 1);
            float occupiedHeight = lastLine.getTop() + lastLine.getHeight();

            if (mFitsVertically) {
                // Update the layout height to occupied height.
                layoutHeight = occupiedHeight;
            } else {
                // Find out the additional top for vertical alignment.
                float verticalMultiplier = getVerticalMultiplier();
                float remainingHeight = layoutHeight - occupiedHeight;
                float additionalTop = remainingHeight * verticalMultiplier;

                // Readjust the vertical position of each line.
                for (int i = 0; i < lineCount; i++) {
                    ComposedLine composedLine = frameLines.get(i);
                    float oldTop = composedLine.getOriginY();
                    float adjustedTop = oldTop + additionalTop;

                    composedLine.setOriginY(adjustedTop);
                }
            }

            if (mFitsHorizontally) {
                float occupiedWidth = Float.NEGATIVE_INFINITY;

                // Find out the occupied width.
                for (int i = 0; i < lineCount; i++) {
                    ComposedLine composedLine = frameLines.get(i);
                    float intrinsicMargin = composedLine.getIntrinsicMargin();
                    float contentWidth = composedLine.getWidth();
                    float marginalWidth = intrinsicMargin + contentWidth;

                    if (marginalWidth > occupiedWidth) {
                        occupiedWidth = marginalWidth;
                    }
                }

                // Readjust the horizontal position of each line.
                for (int i = 0; i < lineCount; i++) {
                    ComposedLine composedLine = frameLines.get(i);
                    float intrinsicMargin = composedLine.getIntrinsicMargin();
                    float flushFactor = composedLine.getFlushFactor();
                    float availableWidth = occupiedWidth - intrinsicMargin;
                    float alignedLeft = composedLine.getFlushPenOffset(flushFactor, availableWidth);
                    float marginalLeft = 0.0f;

                    byte paragraphLevel = composedLine.getParagraphLevel();
                    if ((paragraphLevel & 1) == 0) {
                        marginalLeft = intrinsicMargin;
                    }

                    composedLine.setOriginX(marginalLeft + alignedLeft);
                }

                // Update the layout width to occupied width.
                layoutWidth = occupiedWidth;
            }
        }
    }
}
