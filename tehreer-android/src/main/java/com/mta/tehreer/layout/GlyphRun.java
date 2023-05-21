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

import android.graphics.Canvas;
import android.graphics.RectF;

import androidx.annotation.NonNull;

import com.mta.tehreer.collections.FloatList;
import com.mta.tehreer.collections.IntList;
import com.mta.tehreer.collections.PointList;
import com.mta.tehreer.graphics.Renderer;
import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.internal.layout.TextRun;
import com.mta.tehreer.sfnt.WritingDirection;

import java.util.List;

import static com.mta.tehreer.internal.util.Preconditions.checkArgument;

/**
 * A <code>GlyphRun</code> object is a collection of consecutive glyphs sharing the same attributes
 * and direction.
 */
public class GlyphRun {
    private TextRun textRun;
    private float originX;
    private float originY;

    GlyphRun(TextRun textRun) {
        this.textRun = textRun;
    }

    GlyphRun(@NonNull GlyphRun otherRun) {
        this.textRun = otherRun.textRun;
        this.originX = otherRun.originX;
        this.originY = otherRun.originY;
    }

    TextRun getTextRun() {
        return textRun;
    }

    void setTextRun(TextRun textRun) {
        this.textRun = textRun;
    }

    private void checkCharIndex(int charIndex) {
        final int charStart = textRun.getCharStart();
        final int charEnd = textRun.getCharEnd();

        checkArgument(charIndex >= charStart && charIndex < charEnd,
                      "Char Index: " + charIndex + ", Run Range: [" + charStart + ", " + charEnd + ')');
    }

    private void checkCaretIndex(int charIndex) {
        final int charStart = textRun.getCharStart();
        final int charEnd = textRun.getCharEnd();

        checkArgument(charIndex >= charStart && charIndex <= charEnd,
                      "Char Index: " + charIndex + ", Run Range: [" + charStart + ", " + charEnd + ')');
    }

    private void checkGlyphRange(int glyphStart, int glyphEnd) {
        final int glyphCount = textRun.getGlyphCount();

        checkArgument(glyphStart >= 0, "Glyph Start: " + glyphStart);
        checkArgument(glyphEnd <= glyphCount, "Glyph End: " + glyphEnd + ", Glyph Count: " + glyphCount);
        checkArgument(glyphEnd >= glyphStart, "Bad Range: [" + glyphStart + ", " + glyphEnd + ')');
    }

    @NonNull List<Object> getSpans() {
	    return textRun.getSpans();
    }

    /**
     * Returns the index to the first character of this run in source text.
     *
     * @return The index to the first character of this run in source text.
     */
    public int getCharStart() {
        return textRun.getCharStart();
    }

    /**
     * Returns the index after the last character of this run in source text.
     *
     * @return The index after the last character of this run in source text.
     */
    public int getCharEnd() {
        return textRun.getCharEnd();
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
        return textRun.getStartExtraLength();
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
        return textRun.getEndExtraLength();
    }

    /**
     * Returns the bidirectional level of this run.
     *
     * @return The bidirectional level of this run.
     */
    public byte getBidiLevel() {
        return textRun.getBidiLevel();
    }

    /**
     * Returns the typeface of this run.
     *
     * @return The typeface of this run.
     */
    public @NonNull Typeface getTypeface() {
        return textRun.getTypeface();
    }

    /**
     * Returns the type size of this run.
     *
     * @return The type size of this run.
     */
    public float getTypeSize() {
        return textRun.getTypeSize();
    }

    /**
     * Returns the writing direction of this run.
     *
     * @return The writing direction of this run.
     */
    public @NonNull WritingDirection getWritingDirection() {
        return textRun.getWritingDirection();
    }

    /**
     * Returns the number of glyphs in this run.
     *
     * @return The number of glyphs in this run.
     */
    public int getGlyphCount() {
        return textRun.getGlyphCount();
    }

    /**
     * Returns a list of glyph IDs in this run.
     *
     * @return A list of glyph IDs in this run.
     */
    public @NonNull IntList getGlyphIds() {
        return textRun.getGlyphIds();
    }

    /**
     * Returns a list of glyph offsets in this run.
     *
     * @return A list of glyph offsets in this run.
     */
    public @NonNull PointList getGlyphOffsets() {
        return textRun.getGlyphOffsets();
    }

    /**
     * Returns a list of glyph advances in this run.
     *
     * @return A list of glyph advances in this run.
     */
    public @NonNull FloatList getGlyphAdvances() {
        return textRun.getGlyphAdvances();
    }

    /**
     * Returns a list of indexes, mapping each character in this run to corresponding glyph.
     *
     * @return A list of indexes, mapping each character in this run to corresponding glyph.
     */
    public @NonNull IntList getClusterMap() {
        return textRun.getClusterMap();
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
        return textRun.getAscent();
    }

    /**
     * Returns the descent of this run. The descent is the distance from the baseline to the bottom
     * of the <code>GlyphRun</code>. It is always either positive or zero.
     *
     * @return The descent of this run.
     */
    public float getDescent() {
        return textRun.getDescent();
    }

    /**
     * Returns the leading of this run. The leading is the distance that should be placed between
     * two lines.
     *
     * @return The leading of this run.
     */
    public float getLeading() {
        return textRun.getLeading();
    }

    /**
     * Returns the typographic width of this run.
     *
     * @return The typographic width of this run.
     */
    public float getWidth() {
        return textRun.getWidth();
    }

    /**
     * Returns the typographic height of this run.
     *
     * @return The typographic height of this run.
     */
    public float getHeight() {
        return textRun.getHeight();
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

        return textRun.getClusterStart(charIndex);
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

        return textRun.getClusterEnd(charIndex);
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

        return textRun.getLeadingGlyphIndex(charIndex);
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

        return textRun.getTrailingGlyphIndex(charIndex);
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
        checkCaretIndex(charIndex);

        return textRun.getCaretEdge(charIndex);
    }

    float computeRangeDistance(int fromIndex, int toIndex) {
        return textRun.getRangeDistance(fromIndex, toIndex);
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
     */
    public int computeNearestCharIndex(float distance) {
        return textRun.computeNearestCharIndex(distance);
    }

    @NonNull RectF computeBoundingBox(@NonNull Renderer renderer) {
        return computeBoundingBox(renderer, 0, getGlyphCount());
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
	    return textRun.computeBoundingBox(renderer, glyphStart, glyphEnd);
	}

    /**
     * Draws this run completely onto the given <code>canvas</code> using the given
     * <code>renderer</code>.
     *
     * @param renderer The renderer to use for drawing this run.
     * @param canvas The canvas onto which to draw this run.
     */
	public void draw(@NonNull Renderer renderer, @NonNull Canvas canvas) {
        textRun.draw(renderer, canvas);
	}

    @Override
    public String toString() {
        return "GlyphRun{charStart=" + getCharStart()
                + ", charEnd=" + getCharEnd()
                + ", bidiLevel=" + getBidiLevel()
                + ", writingDirection=" + getWritingDirection()
                + ", glyphCount=" + getGlyphCount()
                + ", glyphIds=" + getGlyphIds()
                + ", glyphOffsets=" + getGlyphOffsets()
                + ", glyphAdvances=" + getGlyphAdvances()
                + ", clusterMap=" + getClusterMap()
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
