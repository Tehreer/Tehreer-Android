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

import android.graphics.Paint;
import android.graphics.RectF;
import android.text.Layout;
import android.text.Spanned;
import android.text.style.AlignmentSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.LeadingMarginSpan.LeadingMarginSpan2;
import android.text.style.LineHeightSpan;
import android.text.style.ParagraphStyle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mta.tehreer.internal.layout.BreakResolver;
import com.mta.tehreer.internal.layout.ParagraphCollection;
import com.mta.tehreer.internal.layout.RunCollection;
import com.mta.tehreer.unicode.BidiParagraph;

import java.util.ArrayList;
import java.util.List;

import static com.mta.tehreer.internal.util.Preconditions.checkArgument;
import static com.mta.tehreer.internal.util.Preconditions.checkNotNull;

/**
 * This class resolves text frames by using a typesetter object.
 */
public class FrameResolver {
    private @NonNull LineResolver mLineResolver = new LineResolver();
    private Typesetter mTypesetter;
    private Spanned mSpanned;
    private ParagraphCollection mParagraphs;
    private RunCollection mRuns;
    private byte[] mBreaks;

    private @NonNull RectF mFrameBounds = new RectF(0, 0, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
    private boolean mFitsHorizontally = false;
    private boolean mFitsVertically = false;
    private @NonNull TextAlignment mTextAlignment = TextAlignment.INTRINSIC;
    private @NonNull VerticalAlignment mVerticalAlignment = VerticalAlignment.TOP;
    private @NonNull BreakMode mTruncationMode = BreakMode.LINE;
    private @Nullable TruncationPlace mTruncationPlace = null;
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
    public void setTypesetter(@NonNull Typesetter typesetter) {
        checkNotNull(typesetter, "typesetter");

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
    public @NonNull RectF getFrameBounds() {
        return new RectF(mFrameBounds);
    }

    /**
     * Sets the rectangle specifying the frame bounds. The default value is an infinite rectangle at
     * zero origin.
     *
     * @param frameBounds A rectangle specifying the frame bounds.
     */
    public void setFrameBounds(@NonNull RectF frameBounds) {
        checkNotNull(frameBounds);
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
    public @NonNull TextAlignment getTextAlignment() {
        return mTextAlignment;
    }

    /**
     * Sets the text alignment to apply on each line of a frame. The default value is
     * {@link TextAlignment#INTRINSIC}.
     *
     * @param textAlignment A value of {@link TextAlignment}.
     */
    public void setTextAlignment(@NonNull TextAlignment textAlignment) {
        checkNotNull(textAlignment);
        mTextAlignment = textAlignment;
    }

    /**
     * Returns the vertical alignment to apply on the contents of a frame. The default value is
     * {@link VerticalAlignment#TOP}.
     *
     * @return The current vertical alignment.
     */
    public @NonNull VerticalAlignment getVerticalAlignment() {
        return mVerticalAlignment;
    }

    /**
     * Sets the vertical alignment to apply on the contents of a frame. The default value is
     * {@link VerticalAlignment#TOP}.
     *
     * @param verticalAlignment A value of {@link VerticalAlignment}.
     */
    public void setVerticalAlignment(@NonNull VerticalAlignment verticalAlignment) {
        checkNotNull(verticalAlignment);
        mVerticalAlignment = verticalAlignment;
    }

    /**
     * Returns the truncation mode to apply on the last line of a frame in case of overflow. The
     * default value is {@link BreakMode#LINE}.
     *
     * @return The current truncation mode.
     */
    public @NonNull BreakMode getTruncationMode() {
        return mTruncationMode;
    }

    /**
     * Sets the truncation mode to apply on the last line of a frame in case of overflow. The
     * default value is {@link BreakMode#LINE}.
     *
     * @param truncationMode A value of {@link BreakMode}.
     */
    public void setTruncationMode(@NonNull BreakMode truncationMode) {
        checkNotNull(truncationMode);
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

    private float getFlushFactor(@Nullable Layout.Alignment layoutAlignment, byte paragraphLevel) {
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

    private void checkSubRange(int charStart, int charEnd) {
        checkArgument(charStart >= 0, "Char Start: " + charStart);
        checkArgument(charEnd <= mSpanned.length(), "Char End: " + charEnd + ", Text Length: " + mSpanned.length());
        checkArgument(charEnd > charStart, "Bad Range: [" + charStart + ", " + charEnd + ')');
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
    public @NonNull ComposedFrame createFrame(int charStart, int charEnd) {
        checkSubRange(charStart, charEnd);

        FrameContext context = new FrameContext();
        context.layoutWidth = mFrameBounds.width();
        context.layoutHeight = mFrameBounds.height();
        context.maxLines = (mMaxLines > 0 ? mMaxLines : Integer.MAX_VALUE);

        int paragraphIndex = mParagraphs.binarySearch(charStart);

        int segmentStart = charStart;
        int segmentEnd;

        // Iterate over all paragraphs in provided range.
        do {
            final BidiParagraph paragraph = mParagraphs.get(paragraphIndex);
            segmentEnd = Math.min(charEnd, paragraph.getCharEnd());

            // Setup the frame context and add the lines.
            context.startIndex = segmentStart;
            context.endIndex = segmentEnd;
            context.baseLevel = paragraph.getBaseLevel();

            addParagraphLines(context);

            if (context.isFilled) {
                break;
            }

            segmentStart = segmentEnd;
            paragraphIndex++;
        } while (segmentStart < charEnd);

        handleTruncation(context, charEnd);
        resolveAlignments(context);

        ComposedFrame frame = new ComposedFrame(mSpanned, charStart, context.frameEnd(), context.textLines);
        frame.setContainerRect(mFrameBounds.left, mFrameBounds.top, context.layoutWidth, context.layoutHeight);

        return frame;
    }

    private static class FrameContext {
        // region Layout Properties

        float layoutWidth = 0.0f;
        float layoutHeight = 0.0f;

        int maxLines = 0;

        final List<ComposedLine> textLines = new ArrayList<>();
        boolean isFilled = false;

        // endregion

        // region Paragraph Properties

        int startIndex = 0;
        int endIndex = 0;
        byte baseLevel = 0;

        ParagraphStyle[] paragraphSpans;
        LineHeightSpan[] pickHeightSpans;

        int[] pickHeightTops;

        int leadingLineCount = 1;
        Paint.FontMetricsInt fontMetrics;

        // endregion

        // region Line Properties

        float lineExtent = 0.0f;
        float leadingOffset = 0.0f;

        float lineTop = 0.0f;
        float lastFlushFactor = 0.0f;

        // endregion

        int frameEnd() {
            return textLines.get(textLines.size() - 1).getCharEnd();
        }
    }

    private int binarySearch(@NonNull FrameContext context, int charIndex) {
        final List<ComposedLine> textLines = context.textLines;

        int low = 0;
        int high = textLines.size() - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            ComposedLine value = textLines.get(mid);

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

    private float computeFlushFactor(@NonNull FrameContext context) {
        final ParagraphStyle[] paragraphSpans = context.paragraphSpans;
        Layout.Alignment alignment = null;

        // Get the top most alignment.
        for (int i = paragraphSpans.length - 1; i >= 0; i--) {
            if (paragraphSpans[i] instanceof AlignmentSpan) {
                alignment = ((AlignmentSpan) paragraphSpans[i]).getAlignment();
                break;
            }
        }

        return getFlushFactor(alignment, context.baseLevel);
    }

    private void resolveLeadingOffset(@NonNull FrameContext context) {
        if ((context.baseLevel & 1) == 0) {
            context.leadingOffset = context.layoutWidth - context.lineExtent;
        }
    }

    private void addParagraphLines(@NonNull FrameContext context) {
        float leadingLineExtent = context.layoutWidth;
        float trailingLineExtent = context.layoutWidth;

        // Extract all spans of this paragraph.
        context.paragraphSpans = mSpanned.getSpans(context.startIndex, context.endIndex, ParagraphStyle.class);

        // Compute margins for leading and trailing lines.
        for (ParagraphStyle style : context.paragraphSpans) {
            if (style instanceof LeadingMarginSpan) {
                final LeadingMarginSpan span = (LeadingMarginSpan) style;
                leadingLineExtent -= span.getLeadingMargin(true);
                trailingLineExtent -= span.getLeadingMargin(false);

                if (span instanceof LeadingMarginSpan2) {
                    final LeadingMarginSpan2 span2 = (LeadingMarginSpan2) span;
                    final int spanTotalLines = span2.getLeadingMarginLineCount();
                    if (spanTotalLines > context.leadingLineCount) {
                        context.leadingLineCount = spanTotalLines;
                    }
                }
            }
        }

        // Extract line height spans and create font metrics if necessary.
        context.pickHeightSpans = mSpanned.getSpans(context.startIndex, context.endIndex, LineHeightSpan.class);
        final int chooseHeightCount = context.pickHeightSpans.length;
        if (chooseHeightCount > 0 && context.fontMetrics == null) {
            context.fontMetrics = new Paint.FontMetricsInt();
        }

        // Setup array for caching top of first line related to each line height span.
        if (context.pickHeightTops == null || context.pickHeightTops.length < chooseHeightCount) {
            context.pickHeightTops = new int[chooseHeightCount];
        }

        // Compute top of first line related to each line height span.
        for (int i = 0; i < chooseHeightCount; i++) {
            final int spanStart = mSpanned.getSpanStart(context.pickHeightSpans[i]);
            int spanTop = (int) (context.lineTop + 0.5f);

            // Fix span top in case it starts in a previous paragraph.
            if (spanStart < context.startIndex) {
                final int lineIndex = binarySearch(context, spanStart);
                final ComposedLine spanLine = context.textLines.get(lineIndex);
                spanTop = (int) (spanLine.getTop() + 0.5f);
            }

            context.pickHeightTops[i] = spanTop;
        }

        final float flushFactor = computeFlushFactor(context);
        context.lineExtent = leadingLineExtent;
        resolveLeadingOffset(context);

        // Iterate over each line of this paragraph.
        int lineStart = context.startIndex;
        while (lineStart != context.endIndex) {
            final int lineEnd = BreakResolver.suggestForwardBreak(mSpanned, mRuns, mBreaks, lineStart, context.endIndex, context.lineExtent, BreakMode.LINE);
            final ComposedLine composedLine = mLineResolver.createSimpleLine(lineStart, lineEnd);
            prepareLine(context, composedLine, flushFactor);

            final float lineHeight = composedLine.getHeight();

            // Make sure that at least one line is added even if frame is smaller in height.
            if ((context.lineTop + lineHeight) > context.layoutHeight && context.textLines.size() > 0) {
                context.isFilled = true;
                return;
            }

            context.textLines.add(composedLine);
            context.lastFlushFactor = flushFactor;

            // Stop the filling process if maximum lines have been added.
            if (context.textLines.size() == context.maxLines) {
                context.isFilled = true;
                return;
            }

            // Find out extent of next line.
            if (--context.leadingLineCount <= 0) {
                context.lineExtent = trailingLineExtent;
                resolveLeadingOffset(context);
            }

            lineStart = lineEnd;
            context.lineTop += lineHeight;
        }
    }

    private void prepareLine(@NonNull FrameContext context, @NonNull ComposedLine composedLine, float flushFactor) {
        final LineHeightSpan[] pickHeightSpans = context.pickHeightSpans;
        final Paint.FontMetricsInt fontMetrics = context.fontMetrics;

        // Resolve line height spans.
        final int chooseHeightCount = pickHeightSpans.length;
        for (int i = 0; i < chooseHeightCount; i++) {
            fontMetrics.ascent = (int) -(composedLine.getAscent() + 0.5f);
            fontMetrics.descent = (int) (composedLine.getDescent() + 0.5f);
            fontMetrics.leading = (int) (composedLine.getLeading() + 0.5f);
            fontMetrics.top = fontMetrics.ascent;
            fontMetrics.bottom = fontMetrics.descent;

            final LineHeightSpan span = pickHeightSpans[i];
            final int lineStart = composedLine.getCharStart();
            final int lineEnd = composedLine.getCharEnd();
            final int lineTop = (int) (context.lineTop + 0.5f);
            final int spanTop = context.pickHeightTops[i];

            span.chooseHeight(mSpanned, lineStart, lineEnd, spanTop, lineTop, fontMetrics);

            // Override the line metrics.
            composedLine.setAscent(-fontMetrics.ascent);
            composedLine.setDescent(fontMetrics.descent);
            composedLine.setLeading(fontMetrics.leading);
        }

        // Resolve line height multiplier.
        if (mLineHeightMultiplier != 0.0f) {
            final float oldHeight = composedLine.getHeight();
            final float newHeight = oldHeight * mLineHeightMultiplier;
            final float midOffset = (newHeight - oldHeight) / 2.0f;

            // Adjust metrics in such a way that text remains in the middle of line.
            composedLine.setAscent(composedLine.getAscent() + midOffset);
            composedLine.setDescent(composedLine.getDescent() + midOffset);
        }

        // Resolve extra line spacing.
        if (mExtraLineSpacing != 0.0f) {
            composedLine.setLeading(composedLine.getLeading() + mExtraLineSpacing);
        }

        // Compute the origin of line.
        final float originX = context.leadingOffset + composedLine.getFlushPenOffset(flushFactor, context.lineExtent);
        final float originY = context.lineTop + composedLine.getAscent();

        // Set the origin of line.
        composedLine.setOriginX(originX);
        composedLine.setOriginY(originY);

        // Set supporting properties of line.
        composedLine.setSpans(context.paragraphSpans);
        composedLine.setFirst(context.leadingLineCount > 0);
        composedLine.setIntrinsicMargin(context.layoutWidth - context.lineExtent);
        composedLine.setFlushFactor(flushFactor);
    }

    private void handleTruncation(@NonNull FrameContext context, int frameEnd) {
        if (mTruncationPlace != null) {
            final List<ComposedLine> textLines = context.textLines;
            final int lastIndex = textLines.size() - 1;
            final ComposedLine lastLine = textLines.get(lastIndex);

            // No need to truncate if frame range is already covered.
            if (lastLine.getCharEnd() == frameEnd) {
                return;
            }

            // Move the y to last line's position.
            context.lineTop = lastLine.getTop();

            // Create the truncated line.
            ComposedLine truncatedLine = mTypesetter.createTruncatedLine(lastLine.getCharStart(), frameEnd, context.lineExtent, mTruncationMode, mTruncationPlace);
            prepareLine(context, truncatedLine, context.lastFlushFactor);

            // Replace the last line with truncated one.
            textLines.set(lastIndex, truncatedLine);
        }
    }

    private void resolveAlignments(@NonNull FrameContext context) {
        // Find out the occupied height.
        final List<ComposedLine> textLines = context.textLines;
        final int lineCount = textLines.size();
        final ComposedLine lastLine = textLines.get(lineCount - 1);
        final float occupiedHeight = lastLine.getTop() + lastLine.getHeight();

        if (mFitsVertically) {
            // Update the layout height to occupied height.
            context.layoutHeight = occupiedHeight;
        } else {
            // Find out the additional top for vertical alignment.
            final float verticalMultiplier = getVerticalMultiplier();
            final float remainingHeight = context.layoutHeight - occupiedHeight;
            final float additionalTop = remainingHeight * verticalMultiplier;

            // Readjust the vertical position of each line.
            for (int i = 0; i < lineCount; i++) {
                final ComposedLine composedLine = textLines.get(i);
                final float oldTop = composedLine.getOriginY();
                final float adjustedTop = oldTop + additionalTop;

                composedLine.setOriginY(adjustedTop);
            }
        }

        if (mFitsHorizontally) {
            float occupiedWidth = Float.NEGATIVE_INFINITY;

            // Find out the occupied width.
            for (int i = 0; i < lineCount; i++) {
                final ComposedLine composedLine = textLines.get(i);
                final float intrinsicMargin = composedLine.getIntrinsicMargin();
                final float contentWidth = composedLine.getWidth();
                final float marginalWidth = intrinsicMargin + contentWidth;

                if (marginalWidth > occupiedWidth) {
                    occupiedWidth = marginalWidth;
                }
            }

            // Readjust the horizontal position of each line.
            for (int i = 0; i < lineCount; i++) {
                final ComposedLine composedLine = textLines.get(i);
                final float intrinsicMargin = composedLine.getIntrinsicMargin();
                final float flushFactor = composedLine.getFlushFactor();
                final float availableWidth = occupiedWidth - intrinsicMargin;
                final float alignedLeft = composedLine.getFlushPenOffset(flushFactor, availableWidth);
                float marginalLeft = 0.0f;

                final byte paragraphLevel = composedLine.getParagraphLevel();
                if ((paragraphLevel & 1) == 0) {
                    marginalLeft = intrinsicMargin;
                }

                composedLine.setOriginX(marginalLeft + alignedLeft);
            }

            // Update the layout width to occupied width.
            context.layoutWidth = occupiedWidth;
        }
    }
}
