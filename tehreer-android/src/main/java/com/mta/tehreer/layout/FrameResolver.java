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
    private boolean mJustificationEnabled = false;
    private float mJustificationLevel = 1.0f;
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
     * Returns whether or not to justify the lines in a frame. The default value is
     * <code>false</code>.
     */
    public boolean isJustificationEnabled() {
        return mJustificationEnabled;
    }

    /**
     * Sets whether or not to justify the lines in a frame. The default value is <code>false</code>.
     *
     * @param justificationEnabled A boolean value specifying the justification enabled state.
     */
    public void setJustificationEnabled(boolean justificationEnabled) {
        mJustificationEnabled = justificationEnabled;
    }

    public float getJustificationLevel() {
        return mJustificationLevel;
    }

    public void setJustificationLevel(float justificationLevel) {
        mJustificationLevel = justificationLevel;
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
        setupLayoutSize(context);
        setupMaxLines(context);
        setupJustificationMultiplier(context);

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

            resolveParagraphLines(context);

            if (context.isFilled) {
                break;
            }

            segmentStart = segmentEnd;
            paragraphIndex++;
        } while (segmentStart < charEnd);

        resolveTruncation(context, charEnd);
        resolveAlignments(context);
        resolveJustification(context);

        ComposedFrame frame = new ComposedFrame(mSpanned, charStart, context.frameEnd(), context.textLines);
        frame.setContainerRect(mFrameBounds.left, mFrameBounds.top, context.layoutWidth, context.layoutHeight);

        return frame;
    }

    private static class FrameContext {
        // region Layout Properties

        float layoutWidth = 0.0f;
        float layoutHeight = 0.0f;

        int maxLines = 0;
        float justificationMultiplier = 0.0f;
        float extraWidth = 0.0f;

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

        float leadingLineExtent = 0.0f;
        float trailingLineExtent = 0.0f;

        float flushFactor = 0.0f;

        // endregion

        // region Line Properties

        float lineExtent = 0.0f;
        float leadingOffset = 0.0f;
        float lineTop = 0.0f;

        // endregion

        int frameEnd() {
            return textLines.get(textLines.size() - 1).getCharEnd();
        }
    }

    private void setupLayoutSize(@NonNull FrameContext context) {
        context.layoutWidth = mFrameBounds.width();
        context.layoutHeight = mFrameBounds.height();
    }

    private void setupMaxLines(@NonNull FrameContext context) {
        context.maxLines = (mMaxLines > 0 ? mMaxLines : Integer.MAX_VALUE);
    }

    private void setupJustificationMultiplier(@NonNull FrameContext context) {
        if (mJustificationEnabled) {
            context.justificationMultiplier = 1.0f - Math.max(0.0f, Math.min(1.0f, mJustificationLevel));
        }
    }

    // region Paragraph Handling

    private void resolveParagraphLines(@NonNull FrameContext context) {
        setupParagraphSpans(context);
        setupLineHeightSpans(context);

        resolveLeadingMargins(context);
        resolveFlushFactor(context);
        resolveLineMargins(context, true);

        // Iterate over each line of this paragraph.
        int lineStart = context.startIndex;
        while (lineStart != context.endIndex) {
            final float breakExtent = context.lineExtent + context.extraWidth;
            final int lineEnd = BreakResolver.suggestForwardBreak(mSpanned, mRuns, mBreaks, lineStart, context.endIndex, breakExtent, BreakMode.LINE);
            final ComposedLine composedLine = mLineResolver.createSimpleLine(lineStart, lineEnd);
            resolveAttributes(context, composedLine);

            final float lineHeight = composedLine.getHeight();

            // Make sure that at least one line is added even if frame is smaller in height.
            if ((context.lineTop + lineHeight) > context.layoutHeight && context.textLines.size() > 0) {
                context.isFilled = true;
                return;
            }

            context.textLines.add(composedLine);

            // Stop the filling process if maximum lines have been added.
            if (context.textLines.size() == context.maxLines) {
                context.isFilled = true;
                return;
            }

            resolveLineMargins(context, false);

            lineStart = lineEnd;
            context.lineTop += lineHeight;
        }
    }

    private void setupParagraphSpans(@NonNull FrameContext context) {
        // Extract all spans of this paragraph.
        context.paragraphSpans = mSpanned.getSpans(context.startIndex, context.endIndex, ParagraphStyle.class);
    }

    private void setupLineHeightSpans(@NonNull FrameContext context) {
        // Extract line height spans and create font metrics if necessary.
        context.pickHeightSpans = mSpanned.getSpans(context.startIndex, context.endIndex, LineHeightSpan.class);
        final int spanCount = context.pickHeightSpans.length;
        if (spanCount > 0 && context.fontMetrics == null) {
            context.fontMetrics = new Paint.FontMetricsInt();
        }

        // Setup array for caching top of first line related to each line height span.
        if (context.pickHeightTops == null || context.pickHeightTops.length < spanCount) {
            context.pickHeightTops = new int[spanCount];
        }

        // Compute top of first line related to each line height span.
        for (int i = 0; i < spanCount; i++) {
            final int spanStart = mSpanned.getSpanStart(context.pickHeightSpans[i]);
            int spanTop = (int) (context.lineTop + 0.5f);

            // Fix span top in case it starts in a previous paragraph.
            if (spanStart < context.startIndex) {
                final int lineIndex = searchLineIndex(context, spanStart);
                final ComposedLine spanLine = context.textLines.get(lineIndex);
                spanTop = (int) (spanLine.getTop() + 0.5f);
            }

            context.pickHeightTops[i] = spanTop;
        }
    }

    private int searchLineIndex(@NonNull FrameContext context, int charIndex) {
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

    private void resolveLeadingMargins(@NonNull FrameContext context) {
        context.leadingLineExtent = context.layoutWidth;
        context.trailingLineExtent = context.layoutWidth;

        // Compute margins for leading and trailing lines.
        for (ParagraphStyle style : context.paragraphSpans) {
            if (style instanceof LeadingMarginSpan) {
                final LeadingMarginSpan span = (LeadingMarginSpan) style;
                context.leadingLineExtent -= span.getLeadingMargin(true);
                context.trailingLineExtent -= span.getLeadingMargin(false);

                if (span instanceof LeadingMarginSpan2) {
                    final LeadingMarginSpan2 span2 = (LeadingMarginSpan2) span;
                    final int lineCount = span2.getLeadingMarginLineCount();
                    if (lineCount > context.leadingLineCount) {
                        context.leadingLineCount = lineCount;
                    }
                }
            }
        }
    }

    private void resolveFlushFactor(@NonNull FrameContext context) {
        final ParagraphStyle[] paragraphSpans = context.paragraphSpans;
        Layout.Alignment alignment = null;

        // Get the top most alignment.
        for (int i = paragraphSpans.length - 1; i >= 0; i--) {
            if (paragraphSpans[i] instanceof AlignmentSpan) {
                alignment = ((AlignmentSpan) paragraphSpans[i]).getAlignment();
                break;
            }
        }

        context.flushFactor = getFlushFactor(alignment, context.baseLevel);
    }

    private void resolveLineMargins(@NonNull FrameContext context, boolean isInitial) {
        if (isInitial) {
            context.lineExtent = context.leadingLineExtent;
            resolveExtraWidth(context);
            resolveLeadingOffset(context);
        } else {
            if (--context.leadingLineCount <= 0) {
                context.lineExtent = context.trailingLineExtent;
                resolveExtraWidth(context);
                resolveLeadingOffset(context);
            }
        }
    }

    private void resolveExtraWidth(@NonNull FrameContext context) {
        float adjustableWidth = context.lineExtent / 4.0f;
        context.extraWidth = adjustableWidth * context.justificationMultiplier;
    }

    private void resolveLeadingOffset(@NonNull FrameContext context) {
        if ((context.baseLevel & 1) == 0) {
            context.leadingOffset = context.layoutWidth - context.lineExtent;
        }
    }

    // endregion

    // region Line Handling

    private void resolveAttributes(@NonNull FrameContext context, @NonNull ComposedLine textLine) {
        resolveCustomHeight(context, textLine);
        resolveLineHeightMultiplier(context, textLine);
        resolveExtraLineSpacing(context, textLine);

        // Compute the origin of line.
        textLine.setOriginX(context.leadingOffset + textLine.getFlushPenOffset(context.flushFactor, context.lineExtent));
        textLine.setOriginY(context.lineTop + textLine.getAscent());

        // Set supporting properties of line.
        textLine.setSpans(context.paragraphSpans);
        textLine.setFirst(context.leadingLineCount > 0);
        textLine.setIntrinsicMargin(context.layoutWidth - context.lineExtent);
        textLine.setFlushFactor(context.flushFactor);
    }

    private void resolveCustomHeight(@NonNull FrameContext context, @NonNull ComposedLine textLine) {
        final LineHeightSpan[] pickHeightSpans = context.pickHeightSpans;
        final Paint.FontMetricsInt fontMetrics = context.fontMetrics;

        // Resolve line height spans.
        final int spanCount = pickHeightSpans.length;
        for (int i = 0; i < spanCount; i++) {
            fontMetrics.ascent = (int) -(textLine.getAscent() + 0.5f);
            fontMetrics.descent = (int) (textLine.getDescent() + 0.5f);
            fontMetrics.leading = (int) (textLine.getLeading() + 0.5f);
            fontMetrics.top = fontMetrics.ascent;
            fontMetrics.bottom = fontMetrics.descent;

            final LineHeightSpan span = pickHeightSpans[i];
            final int lineStart = textLine.getCharStart();
            final int lineEnd = textLine.getCharEnd();
            final int lineTop = (int) (context.lineTop + 0.5f);
            final int spanTop = context.pickHeightTops[i];

            span.chooseHeight(mSpanned, lineStart, lineEnd, spanTop, lineTop, fontMetrics);

            // Override the line metrics.
            textLine.setAscent(-fontMetrics.ascent);
            textLine.setDescent(fontMetrics.descent);
            textLine.setLeading(fontMetrics.leading);
        }
    }

    private void resolveLineHeightMultiplier(@NonNull FrameContext context, @NonNull ComposedLine textLine) {
        // Resolve line height multiplier.
        if (mLineHeightMultiplier != 0.0f) {
            final float oldHeight = textLine.getHeight();
            final float newHeight = oldHeight * mLineHeightMultiplier;
            final float midOffset = (newHeight - oldHeight) / 2.0f;

            // Adjust metrics in such a way that text remains in the middle of line.
            textLine.setAscent(textLine.getAscent() + midOffset);
            textLine.setDescent(textLine.getDescent() + midOffset);
        }
    }

    private void resolveExtraLineSpacing(@NonNull FrameContext context, @NonNull ComposedLine textLine) {
        // Resolve extra line spacing.
        if (mExtraLineSpacing != 0.0f) {
            textLine.setLeading(textLine.getLeading() + mExtraLineSpacing);
        }
    }

    // endregion

    // region Layout Handling

    private void resolveTruncation(@NonNull FrameContext context, int frameEnd) {
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
            final float breakExtent = context.lineExtent + context.extraWidth;
            ComposedLine truncatedLine = mTypesetter.createTruncatedLine(lastLine.getCharStart(), frameEnd, breakExtent, mTruncationMode, mTruncationPlace);
            resolveAttributes(context, truncatedLine);

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

    private void resolveJustification(@NonNull FrameContext context) {
        if (mJustificationEnabled) {
            final List<ComposedLine> textLines = context.textLines;
            final int lineCount = textLines.size();

            for (int i = 0; i < lineCount; i++) {
                final ComposedLine textLine = textLines.get(i);
                final int charStart = textLine.getCharStart();
                final int charEnd = textLine.getCharEnd();

                // Skip the last line of paragraph.
                if (charEnd == mSpanned.length() || mSpanned.charAt(charEnd - 1) == '\n') {
                    continue;
                }

                ComposedLine justifiedLine = mTypesetter.createJustifiedLine(charStart, charEnd, 1.0f, context.layoutWidth);

                final float intrinsicMargin = textLine.getIntrinsicMargin();
                final float flushFactor = textLine.getFlushFactor();
                final float availableWidth = context.layoutWidth - intrinsicMargin;
                final float alignedLeft = justifiedLine.getFlushPenOffset(flushFactor, availableWidth);
                float marginalLeft = 0.0f;

                final byte paragraphLevel = justifiedLine.getParagraphLevel();
                if ((paragraphLevel & 1) == 0) {
                    marginalLeft = intrinsicMargin;
                }

                justifiedLine.setOriginX(marginalLeft + alignedLeft);
                justifiedLine.setOriginY(textLine.getOriginY());
                justifiedLine.setSpans(textLine.getSpans());
                justifiedLine.setFirst(textLine.isFirst());
                justifiedLine.setIntrinsicMargin(textLine.getIntrinsicMargin());
                justifiedLine.setFlushFactor(textLine.getFlushFactor());

                // Setup the line metrics.
                justifiedLine.setAscent(textLine.getAscent());
                justifiedLine.setDescent(textLine.getDescent());
                justifiedLine.setLeading(textLine.getLeading());

                textLines.set(i, justifiedLine);
            }
        }
    }

    // endregion
}
