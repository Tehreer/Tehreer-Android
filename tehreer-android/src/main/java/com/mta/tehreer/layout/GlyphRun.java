/*
 * Copyright (C) 2016-2019 Muhammad Tayyab Akram
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

import android.graphics.Canvas;
import android.graphics.RectF;
import android.text.style.ForegroundColorSpan;
import android.text.style.ReplacementSpan;
import android.text.style.ScaleXSpan;

import androidx.annotation.NonNull;

import com.mta.tehreer.collections.FloatList;
import com.mta.tehreer.collections.IntList;
import com.mta.tehreer.collections.PointList;
import com.mta.tehreer.graphics.Renderer;
import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.internal.layout.ClusterMap;
import com.mta.tehreer.internal.layout.IntrinsicRun;
import com.mta.tehreer.sfnt.WritingDirection;

import java.util.List;

import static com.mta.tehreer.internal.util.Preconditions.checkArgument;

/**
 * A <code>GlyphRun</code> object is a collection of consecutive glyphs sharing the same attributes
 * and direction.
 */
public class GlyphRun {
    private final IntrinsicRun intrinsicRun;
    private final int charStart;
    private final int charEnd;
    private final @NonNull List<Object> spans;
    private final int glyphOffset;
    private final int glyphCount;
    private float originX;
    private float originY;

    GlyphRun(IntrinsicRun intrinsicRun, int charStart, int charEnd, @NonNull List<Object> spans) {
        final int[] glyphRange = intrinsicRun.getGlyphRangeForChars(charStart, charEnd);

        this.intrinsicRun = intrinsicRun;
        this.charStart = charStart;
        this.charEnd = charEnd;
        this.spans = spans;
        this.glyphOffset = glyphRange[0];
        this.glyphCount = glyphRange[1] - glyphRange[0];
    }

    GlyphRun(@NonNull GlyphRun otherRun) {
        this.intrinsicRun = otherRun.intrinsicRun;
        this.charStart = otherRun.charStart;
        this.charEnd = otherRun.charEnd;
        this.spans = otherRun.spans;
        this.glyphOffset = otherRun.glyphOffset;
        this.glyphCount = otherRun.glyphCount;
        this.originX = otherRun.originX;
        this.originY = otherRun.originY;
    }

    private void checkCharIndex(int charIndex) {
        checkArgument(charIndex >= charStart && charIndex < charEnd,
                      "Char Index: " + charIndex + ", Run Range: [" + charStart + ", " + charEnd + ')');
    }

    private void checkGlyphRange(int glyphStart, int glyphEnd) {
        checkArgument(glyphStart >= 0, "Glyph Start: " + glyphStart);
        checkArgument(glyphEnd <= glyphCount, "Glyph End: " + glyphEnd + ", Glyph Count: " + glyphCount);
        checkArgument(glyphEnd >= glyphStart, "Bad Range: [" + glyphStart + ", " + glyphEnd + ')');
    }

    @NonNull List<Object> getSpans() {
	    return spans;
    }

    /**
     * Returns the index to the first character of this run in source text.
     *
     * @return The index to the first character of this run in source text.
     */
    public int getCharStart() {
        return charStart;
    }

    /**
     * Returns the index after the last character of this run in source text.
     *
     * @return The index after the last character of this run in source text.
     */
    public int getCharEnd() {
        return charEnd;
    }

    /**
     * Returns the extra excluded length at the start of the cluster map. If the first cluster of
     * this run begins within the extra range, then its rendering will be clipped from the start.
     * The amount of clipping would be equal to the perceived trailing caret position of last
     * excluded character.
     * <p>
     * For example, consider three characters `f`, `i` and another `i` form a cluster having a
     * single ligature, `fii` and the run starts from the second `i` with `f` and `i` being extra
     * characters. In this case, the ligature would be divided into three equal parts and the first
     * two parts would be clipped.
     *
     * @return The extra excluded length at the start of the cluster map.
     *
     * @see #getClusterMap()
     */
    public int getStartExtraLength() {
        return charStart - intrinsicRun.getClusterStart(charStart);
    }

    /**
     * Returns the extra excluded length at the end of the cluster map. If the last cluster of this
     * run finishes within the excluded range, then its rendering will be clipped from the end. The
     * amount of clipping would be equal to the perceived leading caret position of first excluded
     * character.
     * <p>
     * For example, consider three characters `f`, `i` and another `i` form a cluster having a
     * single ligature, `fii` and the run consists of just `f` with both `i` being extra characters.
     * In this case, the ligature would be divided into three equal parts and the last two parts
     * would be clipped.
     *
     * @return The extra excluded length at the end of the cluster map.
     *
     * @see #getClusterMap()
     */
    public int getEndExtraLength() {
        return intrinsicRun.getClusterEnd(charEnd - 1) - charEnd;
    }

    /**
     * Returns the bidirectional level of this run.
     *
     * @return The bidirectional level of this run.
     */
    public byte getBidiLevel() {
        return intrinsicRun.bidiLevel;
    }

    /**
     * Returns the typeface of this run.
     *
     * @return The typeface of this run.
     */
    public @NonNull Typeface getTypeface() {
        return intrinsicRun.typeface;
    }

    /**
     * Returns the type size of this run.
     *
     * @return The type size of this run.
     */
    public float getTypeSize() {
        return intrinsicRun.typeSize;
    }

    /**
     * Returns the writing direction of this run.
     *
     * @return The writing direction of this run.
     */
    public @NonNull WritingDirection getWritingDirection() {
        return intrinsicRun.writingDirection;
    }

    /**
     * Returns the number of glyphs in this run.
     *
     * @return The number of glyphs in this run.
     */
    public int getGlyphCount() {
        return glyphCount;
    }

    /**
     * Returns a list of glyph IDs in this run.
     *
     * @return A list of glyph IDs in this run.
     */
    public @NonNull IntList getGlyphIds() {
        return intrinsicRun.getGlyphIds().subList(glyphOffset, glyphCount);
    }

    /**
     * Returns a list of glyph offsets in this run.
     *
     * @return A list of glyph offsets in this run.
     */
    public @NonNull PointList getGlyphOffsets() {
        return intrinsicRun.getGlyphOffsets().subList(glyphOffset, glyphCount);
    }

    /**
     * Returns a list of glyph advances in this run.
     *
     * @return A list of glyph advances in this run.
     */
    public @NonNull FloatList getGlyphAdvances() {
        return intrinsicRun.getGlyphAdvances().subList(glyphOffset, glyphCount);
    }

    /**
     * Returns a list of indexes, mapping each character in this run to corresponding glyph.
     *
     * @return A list of indexes, mapping each character in this run to corresponding glyph.
     */
    public @NonNull IntList getClusterMap() {
        final int actualStart = intrinsicRun.getClusterStart(charStart);
        final int actualEnd = intrinsicRun.getClusterEnd(charEnd - 1);
        final int actualLength = actualEnd - actualStart;

        return new ClusterMap(intrinsicRun.clusterMap, actualStart, actualLength, glyphOffset);
    }

    /**
     * Returns the x- origin of this run in parent line.
     *
     * @return The x- origin of this run in parent line.
     */
    public float getOriginX() {
        return originX;
    }

    void setOriginX(float originX) {
        this.originX = originX;
    }

    /**
     * Returns the y- origin of this run in parent line.
     *
     * @return The y- origin of this run in parent line.
     */
    public float getOriginY() {
        return originY;
    }

    void setOriginY(float originY) {
        this.originY = originY;
    }

    /**
     * Returns the ascent of this run. The ascent is the distance from the top of the
     * <code>GlyphRun</code> to the baseline. It is always either positive or zero.
     *
     * @return The ascent of this run.
     */
    public float getAscent() {
        return intrinsicRun.ascent;
    }

    /**
     * Returns the descent of this run. The descent is the distance from the baseline to the bottom
     * of the <code>GlyphRun</code>. It is always either positive or zero.
     *
     * @return The descent of this run.
     */
    public float getDescent() {
        return intrinsicRun.descent;
    }

    /**
     * Returns the leading of this run. The leading is the distance that should be placed between
     * two lines.
     *
     * @return The leading of this run.
     */
    public float getLeading() {
        return intrinsicRun.leading;
    }

    /**
     * Returns the typographic width of this run.
     *
     * @return The typographic width of this run.
     */
    public float getWidth() {
        return intrinsicRun.measureChars(charStart, charEnd);
    }

    /**
     * Returns the typographic height of this run.
     *
     * @return The typographic height of this run.
     */
    public float getHeight() {
        return (intrinsicRun.ascent + intrinsicRun.descent + intrinsicRun.leading);
    }

    /**
     * Returns the index to the first character of specified cluster in source string. In most
     * cases, it would be the same index as the specified one. But if the character occurs within a
     * cluster, then a previous index would be returned; whether the run logically flows forward or
     * backward.
     *
     * @param charIndex The index of a character in source string.
     * @return The index to the first character of specified cluster in source string.
     *
     * @throws IllegalArgumentException if <code>charIndex</code> is less than run start or greater
     *         than or equal to run end.
     */
    public int getActualClusterStart(int charIndex) {
        checkCharIndex(charIndex);

        return intrinsicRun.getClusterStart(charIndex);
    }

    /**
     * Returns the index after the last character of specified cluster in source string. In most
     * cases, it would be an index after the specified one. But if the character occurs within a
     * cluster, then a farther index would be returned; whether the run logically flows forward or
     * backward.
     *
     * @param charIndex The index of a character in source string.
     * @return The index after the last character of specified cluster in source string.
     *
     * @throws IllegalArgumentException if <code>charIndex</code> is less than run start or greater
     *         than or equal to run end.
     */
    public int getActualClusterEnd(int charIndex) {
        checkCharIndex(charIndex);

        return intrinsicRun.getClusterEnd(charIndex);
    }

    /**
     * Returns the index of leading glyph related to the specified cluster. It will come after the
     * trailing glyph, if the characters of this run logically flow backward.
     *
     * @param charIndex The index of a character in source string.
     * @return The index of leading glyph related to the specified cluster.
     *
     * @throws IllegalArgumentException if <code>charIndex</code> is less than run start or greater
     *         than or equal to run end.
     *
     * @see #getTrailingGlyphIndex(int)
     * @see #getActualClusterStart(int)
     * @see #getActualClusterEnd(int)
     */
    public int getLeadingGlyphIndex(int charIndex) {
        checkCharIndex(charIndex);

        return intrinsicRun.getLeadingGlyphIndex(charIndex) - glyphOffset;
    }

    /**
     * Returns the index of trailing glyph related to the specified cluster. It will come before
     * the leading glyph, if the characters of this run logically flow backward.
     *
     * @param charIndex The index of a character in source string.
     * @return The index of trailing glyph related to the specified cluster.
     *
     * @throws IllegalArgumentException if <code>charIndex</code> is less than run start or greater
     *         than or equal to run end.
     *
     * @see #getLeadingGlyphIndex(int)
     * @see #getActualClusterStart(int)
     * @see #getActualClusterEnd(int)
     */
    public int getTrailingGlyphIndex(int charIndex) {
        checkCharIndex(charIndex);

        return intrinsicRun.getTrailingGlyphIndex(charIndex) - glyphOffset;
    }

    /**
     * Determines the distance of specified character from the start of the run assumed at zero.
     *
     * @param charIndex The index of a character in source string.
     * @return The distance of specified character from the start of the run assumed at zero.
     *
     * @throws IllegalArgumentException if <code>charIndex</code> is less than run start or greater
     *         than run end.
     */
    public float computeCharDistance(int charIndex) {
        checkArgument(charIndex >= charStart && charIndex <= charEnd,
                      "Char Index: " + charIndex + ", Run Range: [" + charStart + ", " + charEnd + ')');

        final float caretBoundary = intrinsicRun.getCaretBoundary(charStart, charEnd);
        final float caretEdge = intrinsicRun.getCaretEdge(charIndex, caretBoundary);

        return caretEdge - caretBoundary;
    }

    float computeRangeDistance(int fromIndex, int toIndex) {
        return intrinsicRun.measureChars(fromIndex, toIndex);
    }

    /**
     * Determines the index of character nearest to the specified distance.
     * <p>
     * The process involves iterating over the clusters of this glyph run. If a cluster consists of
     * multiple characters, its total advance is evenly distributed among the number of characters
     * it contains. The advance of each character is added to track the covered distance. This way
     * leading and trailing characters are determined close to the specified distance. Afterwards,
     * the index of nearer character is returned.
     * <p>
     * If <code>distance</code> is negative, then run's starting index is returned. If it is beyond
     * run's extent, then ending index is returned. The indices will be reversed in case of
     * right-to-left run.
     *
     * @param distance The distance for which to determine the character index. It should be offset
     *                 from zero origin.
     * @return The index of character nearest to the specified distance. It will be an absolute
     *         index in source string.
     *
     * @see #getCharStart()
     * @see #getCharEnd()
     * @see #computeTypographicExtent(int, int)
     */
    public int computeNearestCharIndex(float distance) {
        return intrinsicRun.computeNearestCharIndex(distance, charStart, charEnd);
    }

    /**
     * Calculates the typographic extent for the given glyph range in this run. The typographic
     * extent is equal to the sum of advances of glyphs.
     *
     * @param glyphStart The index to first glyph being measured.
     * @param glyphEnd The index after the last glyph being measured.
     * @return The typographic extent for the given glyph range in this run.
     */
    public float computeTypographicExtent(int glyphStart, int glyphEnd) {
        checkGlyphRange(glyphStart, glyphEnd);

        FloatList glyphAdvances = getGlyphAdvances();
        float extent = 0.0f;

        for (int i = glyphStart; i < glyphEnd; i++) {
            extent += glyphAdvances.get(i);
        }

        return extent;
    }

    /**
     * Calculates the bounding box for the given glyph range in this run. The bounding box is a
     * rectangle that encloses the paths of this run's glyphs in the given range, as tightly as
     * possible.
     *
     * @param renderer The renderer to use for calculating the bounding box. This is required
     *                 because the renderer could have settings in it that would cause changes in
     *                 the bounding box.
     * @param glyphStart The index to the first glyph being measured.
     * @param glyphEnd The index after the last glyph being measured.
     * @return A rectangle that tightly encloses the paths of this run's glyphs in the given range.
     *
     * @throws IllegalArgumentException if <code>glyphStart</code> is negative, or
     *         <code>glyphEnd</code> is greater than total number of glyphs in the run, or
     *         <code>glyphStart</code> is greater than <code>glyphEnd</code>.
     */
	public @NonNull RectF computeBoundingBox(@NonNull Renderer renderer, int glyphStart, int glyphEnd) {
        checkGlyphRange(glyphStart, glyphEnd);

	    renderer.setTypeface(getTypeface());
	    renderer.setTypeSize(getTypeSize());
        renderer.setWritingDirection(getWritingDirection());

        return renderer.computeBoundingBox(getGlyphIds().subList(glyphStart, glyphEnd),
                                           getGlyphOffsets().subList(glyphStart, glyphEnd),
                                           getGlyphAdvances().subList(glyphStart, glyphEnd));
	}

    /**
     * Draws this run completely onto the given <code>canvas</code> using the given
     * <code>renderer</code>.
     *
     * @param renderer The renderer to use for drawing this run.
     * @param canvas The canvas onto which to draw this run.
     */
	public void draw(@NonNull Renderer renderer, @NonNull Canvas canvas) {
        renderer.setScaleX(1.0f);

        int defaultFillColor = renderer.getFillColor();
        ReplacementSpan replacement = null;

        for (Object span : spans) {
            if (span instanceof ForegroundColorSpan) {
                renderer.setFillColor(((ForegroundColorSpan) span).getForegroundColor());
            } else if (span instanceof ReplacementSpan) {
                replacement = (ReplacementSpan) span;
            } else if (span instanceof ScaleXSpan) {
                renderer.setScaleX(((ScaleXSpan) span).getScaleX());
            }
        }

        if (replacement == null) {
            intrinsicRun.draw(renderer, canvas, charStart, charEnd);
        } else {
            int top = (int) -(getAscent() + 0.5f);
            int bottom = (int) (getDescent() + 0.5f);

            replacement.draw(canvas,
                             null, charStart, charEnd,
                             0, top, 0, bottom, null);
        }

        renderer.setFillColor(defaultFillColor);
	}

    @Override
    public String toString() {
        return "GlyphRun{charStart=" + charStart
                + ", charEnd=" + charEnd
                + ", bidiLevel=" + getBidiLevel()
                + ", writingDirection=" + getWritingDirection().toString()
                + ", glyphCount=" + getGlyphCount()
                + ", glyphIds=" + getGlyphIds().toString()
                + ", glyphOffsets=" + getGlyphOffsets().toString()
                + ", glyphAdvances=" + getGlyphAdvances().toString()
                + ", clusterMap=" + getClusterMap().toString()
                + ", originX=" + originX
                + ", originY=" + originY
                + ", ascent=" + getAscent()
                + ", descent=" + getDescent()
                + ", leading=" + getLeading()
                + ", width=" + getWidth()
                + ", height=" + getHeight()
                + "}";
    }
}
