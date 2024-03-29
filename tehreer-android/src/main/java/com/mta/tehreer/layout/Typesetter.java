/*
 * Copyright (C) 2016-2023 Muhammad Tayyab Akram
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.internal.layout.BreakResolver;
import com.mta.tehreer.internal.layout.ParagraphCollection;
import com.mta.tehreer.internal.layout.RunCollection;
import com.mta.tehreer.internal.layout.ShapeResolver;
import com.mta.tehreer.internal.layout.TokenResolver;
import com.mta.tehreer.internal.util.StringUtils;
import com.mta.tehreer.layout.style.TypeSizeSpan;
import com.mta.tehreer.layout.style.TypefaceSpan;
import com.mta.tehreer.unicode.BreakClassifier;

import java.util.Collections;
import java.util.List;

import kotlin.Pair;

import static com.mta.tehreer.internal.util.Preconditions.checkArgument;
import static com.mta.tehreer.internal.util.Preconditions.checkNotNull;

/**
 * Represents a typesetter which performs text layout. It can be used to create lines, perform line
 * breaking, and do other contextual analysis based on the characters in the string.
 */
public class Typesetter {
    private String mText;
    private Spanned mSpanned;
    private ParagraphCollection mBidiParagraphs;
    private RunCollection mIntrinsicRuns;
    private LineResolver mLineResolver;
    private BreakResolver mBreakResolver;

    /**
     * Constructs the typesetter object using given text, typeface and type size.
     *
     * @param text The text to typeset.
     * @param typeface The typeface to use.
     * @param typeSize The type size to apply.
     *
     * @throws IllegalArgumentException if <code>text</code> is empty.
     */
	public Typesetter(@NonNull String text, @NonNull Typeface typeface, float typeSize) {
	    checkNotNull(text, "text");
	    checkNotNull(typeface, "typeface");
	    checkArgument(text.length() > 0, "Text is empty");

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
     * @throws IllegalArgumentException if <code>spanned</code> is empty.
     */
    public Typesetter(@NonNull Spanned spanned) {
        this(spanned, null);
    }

    public Typesetter(@NonNull Spanned spanned, @Nullable List<Object> defaultSpans) {
        checkNotNull(spanned, "spanned");
        checkArgument(spanned.length() > 0, "Text is empty");

        init(StringUtils.copyString(spanned), spanned, defaultSpans);
    }

    private void init(@NonNull String text, @NonNull Spanned spanned, @Nullable List<Object> defaultSpans) {
        mText = text;
        mSpanned = spanned;

        if (defaultSpans == null) {
            defaultSpans = Collections.emptyList();
        }

        ShapeResolver shapeResolver = new ShapeResolver(mText, mSpanned, defaultSpans);
        Pair<ParagraphCollection, RunCollection> shapeResult = shapeResolver.createParagraphsAndRuns();
        mBidiParagraphs = shapeResult.getFirst();
        mIntrinsicRuns = shapeResult.getSecond();

        mLineResolver = new LineResolver(spanned, mBidiParagraphs, mIntrinsicRuns);

        BreakClassifier breakClassifier = new BreakClassifier(text);
        mBreakResolver = new BreakResolver(mText, mBidiParagraphs, mIntrinsicRuns, breakClassifier);
    }

    /**
     * Returns the spanned source text for which this typesetter object was created.
     *
     * @return The spanned source text for which this typesetter object was created.
     */
    public Spanned getSpanned() {
        return mSpanned;
    }

    ParagraphCollection getParagraphs() {
        return mBidiParagraphs;
    }

    RunCollection getRuns() {
        return mIntrinsicRuns;
    }

    private void checkSubRange(int charStart, int charEnd) {
        checkArgument(charStart >= 0, "Char Start: " + charStart);
        checkArgument(charEnd <= mText.length(), "Char End: " + charEnd + ", Text Length: " + mText.length());
        checkArgument(charEnd > charStart, "Bad Range: [" + charStart + ", " + charEnd + ')');
    }

    /**
     * Suggests a forward break index based on the provided range and width. The measurement
     * proceeds from first character to last character. If there is still room after measuring all
     * characters, then last index is returned. Otherwise, break index is returned.
     *
     * @param charStart The index to the first character (inclusive) for break calculations.
     * @param charEnd The index to the last character (exclusive) for break calculations.
     * @param breakExtent The requested break extent.
     * @param breakMode The requested break mode.
     * @return The index (exclusive) that would cause the break.
     *
     * @throws IllegalArgumentException if <code>charStart</code> is negative, or
     *         <code>charEnd</code> is greater than the length of source text, or
     *         <code>charStart</code> is greater than or equal to <code>charEnd</code>
     */
    public int suggestForwardBreak(int charStart, int charEnd,
                                   float breakExtent, @NonNull BreakMode breakMode) {
        checkNotNull(breakMode, "breakMode");
        checkSubRange(charStart, charEnd);

        return mBreakResolver.suggestForwardBreak(charStart, charEnd, breakExtent, breakMode);
    }

    /**
     * Suggests a backward break index based on the provided range and width. The measurement
     * proceeds from last character to first character. If there is still room after measuring all
     * characters, then first index is returned. Otherwise, break index is returned.
     *
     * @param charStart The index to the first character (inclusive) for break calculations.
     * @param charEnd The index to the last character (exclusive) for break calculations.
     * @param breakExtent The requested break extent.
     * @param breakMode The requested break mode.
     * @return The index (inclusive) that would cause the break.
     *
     * @throws IllegalArgumentException if <code>charStart</code> is negative, or
     *         <code>charEnd</code> is greater than the length of source text, or
     *         <code>charStart</code> is greater than or equal to <code>charEnd</code>
     */
    public int suggestBackwardBreak(int charStart, int charEnd,
                                    float breakExtent, @NonNull BreakMode breakMode) {
        checkNotNull(breakMode, "breakMode");
        checkSubRange(charStart, charEnd);

        return mBreakResolver.suggestBackwardBreak(charStart, charEnd, breakExtent, breakMode);
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
	public @NonNull ComposedLine createSimpleLine(int charStart, int charEnd) {
        checkSubRange(charStart, charEnd);

        return mLineResolver.createSimpleLine(charStart, charEnd);
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
     * @throws IllegalArgumentException if any of the following is true:
     *         <ul>
     *             <li><code>charStart</code> is negative</li>
     *             <li><code>charEnd</code> is greater than the length of source text</li>
     *             <li><code>charStart</code> is greater than or equal to <code>charEnd</code></li>
     *         </ul>
     */
    public @NonNull ComposedLine createTruncatedLine(int charStart, int charEnd, float maxWidth,
                                                     @NonNull BreakMode breakMode,
                                                     @NonNull TruncationPlace truncationPlace) {
        checkNotNull(breakMode, "breakMode");
        checkNotNull(truncationPlace, "truncationPlace");
        checkSubRange(charStart, charEnd);

        return mLineResolver.createCompactLine(charStart, charEnd, maxWidth, mBreakResolver, breakMode, truncationPlace,
                TokenResolver.createToken(mIntrinsicRuns, charStart, charEnd, truncationPlace, null));
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
     * @throws IllegalArgumentException if any of the following is true:
     *         <ul>
     *             <li><code>charStart</code> is negative</li>
     *             <li><code>charEnd</code> is greater than the length of source text</li>
     *             <li><code>charStart</code> is greater than or equal to <code>charEnd</code></li>
     *         </ul>
     */
    public @NonNull ComposedLine createTruncatedLine(int charStart, int charEnd, float maxWidth,
                                                     @NonNull BreakMode breakMode,
                                                     @NonNull TruncationPlace truncationPlace,
                                                     @NonNull String truncationToken) {
        checkNotNull(breakMode, "breakMode");
        checkNotNull(truncationPlace, "truncationPlace");
        checkNotNull(truncationToken, "truncationToken");
        checkSubRange(charStart, charEnd);
        checkArgument(truncationToken.length() > 0, "Truncation token is empty");

        return mLineResolver.createCompactLine(charStart, charEnd, maxWidth, mBreakResolver, breakMode, truncationPlace,
                TokenResolver.createToken(mIntrinsicRuns, charStart, charEnd, truncationPlace, truncationToken));
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
     * @throws IllegalArgumentException if any of the following is true:
     *         <ul>
     *             <li><code>charStart</code> is negative</li>
     *             <li><code>charEnd</code> is greater than the length of source text</li>
     *             <li><code>charStart</code> is greater than or equal to <code>charEnd</code></li>
     *         </ul>
     */
    public @NonNull ComposedLine createTruncatedLine(int charStart, int charEnd, float maxWidth,
                                                     @NonNull BreakMode breakMode,
                                                     @NonNull TruncationPlace truncationPlace,
                                                     @NonNull ComposedLine truncationToken) {
        checkNotNull(breakMode, "breakMode");
        checkNotNull(truncationPlace, "truncationPlace");
        checkNotNull(truncationToken, "truncationToken");
        checkSubRange(charStart, charEnd);

        return mLineResolver.createCompactLine(charStart, charEnd, maxWidth, mBreakResolver, breakMode,
                                               truncationPlace, truncationToken);
    }

    /**
     * Creates a justified line of specified string range.
     *
     * @param charStart The index to first character of the line in source text.
     * @param charEnd The index after the last character of the line in source text.
     * @param justificationFactor The factor that specifies the full or partial justification. When
     *                            set to 1.0 or greater, full justification is performed. If this
     *                            parameter is set to less than 1.0, varying degrees of partial
     *                            justification are performed. If it is set to 0 or less, no
     *                            justification is performed.
     * @param justificationWidth The width at which the line should be justified. If it is less than
     *                           the actual width of the line, then negative justification is
     *                           performed (that is, words are squeezed together).
     * @return The new justified line.
     */
    public @NonNull ComposedLine createJustifiedLine(int charStart, int charEnd,
                                                     float justificationFactor,
                                                     float justificationWidth) {
        checkSubRange(charStart, charEnd);

        return mLineResolver.createJustifiedLine(charStart, charEnd, justificationFactor,
                                                 justificationWidth);
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
    public @NonNull ComposedFrame createFrame(int charStart, int charEnd,
                                              @NonNull RectF frameRect,
                                              @NonNull TextAlignment textAlignment) {
        checkNotNull(frameRect, "frameRect");
        checkNotNull(textAlignment, "textAlignment");
        checkSubRange(charStart, charEnd);
        checkArgument(!frameRect.isEmpty(), "Frame rect is empty");

        FrameResolver resolver = new FrameResolver();
        resolver.setTypesetter(this);
        resolver.setFrameBounds(frameRect);
        resolver.setTextAlignment(textAlignment);

        return resolver.createFrame(charStart, charEnd);
    }
}
