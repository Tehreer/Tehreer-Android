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

import android.graphics.Canvas;
import android.graphics.RectF;
import android.text.style.ForegroundColorSpan;
import android.text.style.ReplacementSpan;
import android.text.style.ScaleXSpan;

import com.mta.tehreer.collections.FloatList;
import com.mta.tehreer.collections.IntList;
import com.mta.tehreer.collections.PointList;
import com.mta.tehreer.graphics.Renderer;
import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.internal.util.Clusters;
import com.mta.tehreer.sfnt.WritingDirection;

import java.util.List;

/**
 * A <code>GlyphRun</code> object is a collection of consecutive glyphs sharing the same attributes
 * and direction.
 */
public class GlyphRun {

    private final int charStart;
    private final int charEnd;
    private final List<Object> spans;
    private final boolean isBackward;
    private final byte bidiLevel;
    private final Typeface typeface;
    private final float typeSize;
    private final WritingDirection writingDirection;
    private final IntList glyphIds;
    private final PointList glyphOffsets;
    private final FloatList glyphAdvances;
    private final IntList clusterMap;
    private float originX;
    private float originY;

    private float mWidth = Float.NEGATIVE_INFINITY;

    GlyphRun(int charStart, int charEnd, List<Object> spans, boolean isBackward, byte bidiLevel,
             Typeface typeface, float typeSize, WritingDirection writingDirection,
             IntList glyphIds, PointList offsets, FloatList advances, IntList clusterMap) {
        this.charStart = charStart;
        this.charEnd = charEnd;
        this.spans = spans;
        this.isBackward = isBackward;
        this.bidiLevel = bidiLevel;
        this.typeface = typeface;
        this.typeSize = typeSize;
        this.writingDirection = writingDirection;
        this.glyphIds = glyphIds;
        this.glyphOffsets = offsets;
        this.glyphAdvances = advances;
        this.clusterMap = clusterMap;
    }

    GlyphRun(GlyphRun otherRun) {
        this.charStart = otherRun.charStart;
        this.charEnd = otherRun.charEnd;
        this.spans = otherRun.spans;
        this.isBackward = otherRun.isBackward;
        this.bidiLevel = otherRun.bidiLevel;
        this.typeface = otherRun.typeface;
        this.typeSize = otherRun.typeSize;
        this.writingDirection = otherRun.writingDirection;
        this.glyphIds = otherRun.glyphIds;
        this.glyphOffsets = otherRun.glyphOffsets;
        this.glyphAdvances = otherRun.glyphAdvances;
        this.clusterMap = otherRun.clusterMap;
        this.originX = otherRun.originX;
        this.originY = otherRun.originY;
    }

    private String checkCharIndex(int charIndex) {
        if (charIndex < charStart) {
            return ("Char Index: " + charIndex + ", Char Start: " + charStart);
        }
        if (charIndex >= charEnd) {
            return ("Char Index: " + charIndex + ", Char End: " + charEnd);
        }

        return null;
    }

    private String checkRange(int glyphStart, int glyphEnd) {
        if (glyphStart < 0) {
            return ("Glyph Start: " + glyphStart);
        }
        int glyphCount = glyphIds.size();
        if (glyphEnd > glyphCount) {
            return ("Glyph End: " + glyphEnd + ", Glyph Count: " + glyphCount);
        }
        if (glyphStart > glyphEnd) {
            return ("Glyph Start: " + glyphStart + ", Glyph End: " + glyphEnd);
        }

        return null;
    }

    List<Object> getSpans() {
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
     * Returns the bidirectional level of this run.
     *
     * @return The bidirectional level of this run.
     */
    public byte getBidiLevel() {
        return bidiLevel;
    }

    /**
     * Returns the typeface of this run.
     *
     * @return The typeface of this run.
     */
    public Typeface getTypeface() {
        return typeface;
    }

    /**
     * Returns the type size of this run.
     *
     * @return The type size of this run.
     */
    public float getTypeSize() {
        return typeSize;
    }

    /**
     * Returns the writing direction of this run.
     *
     * @return The writing direction of this run.
     */
    public WritingDirection getWritingDirection() {
        return writingDirection;
    }

    /**
     * Returns the number of glyphs in this run.
     *
     * @return The number of glyphs in this run.
     */
    public int getGlyphCount() {
        return glyphIds.size();
    }

    /**
     * Returns a list of glyph IDs in this run.
     *
     * @return A list of glyph IDs in this run.
     */
    public IntList getGlyphIds() {
        return glyphIds;
    }

    /**
     * Returns a list of glyph offsets in this run.
     *
     * @return A list of glyph offsets in this run.
     */
    public PointList getGlyphOffsets() {
        return glyphOffsets;
    }

    /**
     * Returns a list of glyph advances in this run.
     *
     * @return A list of glyph advances in this run.
     */
    public FloatList getGlyphAdvances() {
        return glyphAdvances;
    }

    /**
     * Returns a list of indexes, mapping each character in this run to corresponding glyph.
     *
     * @return A list of indexes, mapping each character in this run to corresponding glyph.
     */
    public IntList getClusterMap() {
        return clusterMap;
    }

    int getLeadingGlyphIndex(int charIndex) {
        String indexError = checkCharIndex(charIndex);
        if (indexError != null) {
            throw new IllegalArgumentException(indexError);
        }

        return Clusters.leadingGlyphIndex(clusterMap, charIndex - charStart);
    }

    int getTrailingGlyphIndex(int charIndex) {
        String indexError = checkCharIndex(charIndex);
        if (indexError != null) {
            throw new IllegalArgumentException(indexError);
        }

        return Clusters.trailingGlyphIndex(clusterMap, charIndex - charStart, isBackward, glyphIds.size());
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
        return typeface.getAscent() * (typeSize / typeface.getUnitsPerEm());
    }

    /**
     * Returns the descent of this run. The descent is the distance from the baseline to the bottom
     * of the <code>GlyphRun</code>. It is always either positive or zero.
     *
     * @return The descent of this run.
     */
    public float getDescent() {
        return typeface.getDescent() * (typeSize / typeface.getUnitsPerEm());
    }

    /**
     * Returns the leading of this run. The leading is the distance that should be placed between
     * two lines.
     *
     * @return The leading of this run.
     */
    public float getLeading() {
        return typeface.getLeading() * (typeSize / typeface.getUnitsPerEm());
    }

    /**
     * Returns the typographic width of this run.
     *
     * @return The typographic width of this run.
     */
    public float getWidth() {
        // Locking is not required for constant width.
        if (mWidth == Float.NEGATIVE_INFINITY) {
            mWidth = computeTypographicExtent(0, glyphIds.size());
        }

        return mWidth;
    }

    /**
     * Returns the typographic height of this run.
     *
     * @return The typographic height of this run.
     */
    public float getHeight() {
        return (getAscent() + getDescent() + getLeading());
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
        String indexError = checkCharIndex(charIndex);
        if (indexError != null) {
            throw new IllegalArgumentException(indexError);
        }

        return Clusters.actualClusterStart(clusterMap, charIndex - charStart) + charStart;
    }

    /**
     * Returns the index after the last character of specified cluster in source string. In most
     * cases, it would be one index after the specified one. But if the character occurs within a
     * cluster, then an even further index would be returned; whether the run logically flows
     * forward or backward.
     *
     * @param charIndex The index of a character in source string.
     * @return The index after the last character of specified cluster in source string.
     *
     * @throws IllegalArgumentException if <code>charIndex</code> is less than run start or greater
     *         than or equal to run end.
     */
    public int getActualClusterEnd(int charIndex) {
        String indexError = checkCharIndex(charIndex);
        if (indexError != null) {
            throw new IllegalArgumentException(indexError);
        }

        return Clusters.actualClusterEnd(clusterMap, charIndex - charStart) + charStart;
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
        IntList clusterMap = getClusterMap();
        FloatList advances = getGlyphAdvances();
        int charCount = clusterMap.size();

        WritingDirection writingDirection = getWritingDirection();
        if (writingDirection == WritingDirection.RIGHT_TO_LEFT) {
            // Reverse the distance in case of right to left direction.
            distance = getWidth() - distance;
        }

        int clusterStart = 0;
        int glyphStart = 0;
        int glyphCount = glyphIds.size();
        float computedAdvance = 0.0f;

        int leadingCharIndex = charStart;
        int trailingCharIndex = charEnd;
        float leadingEdgeAdvance = 0.0f;

        for (int i = 1; i <= charCount; i++) {
            int glyphIndex = (i < charCount ? clusterMap.get(i) : glyphCount);
            if (glyphIndex == glyphStart) {
                continue;
            }

            // Find the advance of current cluster.
            float clusterAdvance = 0.0f;
            for (int j = glyphStart; j < glyphIndex; j++) {
                clusterAdvance += advances.get(j);
            }

            // Divide the advance evenly between cluster length.
            int clusterLength = i - clusterStart;
            float charAdvance = clusterAdvance / clusterLength;

            boolean trailingCharFound = false;

            // Compare individual code points with input distance.
            for (int j = 0; j < clusterLength; j++) {
                // TODO: Iterate on code points rather than UTF-16 code units of java string.

                if (computedAdvance <= distance) {
                    leadingCharIndex = charStart + clusterStart + j;
                    leadingEdgeAdvance = computedAdvance;
                } else {
                    trailingCharIndex = charStart + clusterStart + j;
                    trailingCharFound = true;
                    break;
                }

                computedAdvance += charAdvance;
            }

            // Break the outer loop if trailing char has been found.
            if (trailingCharFound) {
                break;
            }

            clusterStart = i;
            glyphStart = glyphIndex;
        }

        if (leadingCharIndex == -1) {
            // No char is covered by the input distance.
            return charStart;
        }

        if (trailingCharIndex == -1) {
            // Whole run is covered by the input distance.
            return charEnd;
        }

        if (distance <= (leadingEdgeAdvance + computedAdvance) / 2.0) {
            // Input distance is closer to first edge.
            return leadingCharIndex;
        }

        // Input distance is closer to second edge.
        return trailingCharIndex;
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
        String rangeError = checkRange(glyphStart, glyphEnd);
        if (rangeError != null) {
            throw new IllegalArgumentException(rangeError);
        }

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
	public RectF computeBoundingBox(Renderer renderer, int glyphStart, int glyphEnd) {
        String rangeError = checkRange(glyphStart, glyphEnd);
        if (rangeError != null) {
            throw new IllegalArgumentException(rangeError);
        }

	    renderer.setTypeface(typeface);
	    renderer.setTypeSize(typeSize);
        renderer.setWritingDirection(writingDirection);

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
	public void draw(Renderer renderer, Canvas canvas) {
	    draw(renderer, canvas, 0, glyphIds.size());
	}

    /**
     * Draws a part of this run onto the given <code>canvas</code> using the given
     * <code>renderer</code>.
     *
     * @param renderer The renderer to use for drawing this run.
     * @param canvas The canvas onto which to draw this run.
     * @param glyphStart The index to the first glyph being drawn.
     * @param glyphEnd The index after the last glyph being drawn.
     *
     * @throws IllegalArgumentException if <code>glyphStart</code> is negative, or
     *         <code>glyphEnd</code> is greater than total number of glyphs in the run, or
     *         <code>glyphStart</code> is greater than <code>glyphEnd</code>.
     */
	public void draw(Renderer renderer, Canvas canvas, int glyphStart, int glyphEnd) {
        String rangeError = checkRange(glyphStart, glyphEnd);
        if (rangeError != null) {
            throw new IllegalArgumentException(rangeError);
        }

	    renderer.setTypeface(typeface);
        renderer.setTypeSize(typeSize);
        renderer.setScaleX(1.0f);
        renderer.setWritingDirection(writingDirection);

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
            renderer.drawGlyphs(canvas,
                                getGlyphIds().subList(glyphStart, glyphEnd),
                                getGlyphOffsets().subList(glyphStart, glyphEnd),
                                getGlyphAdvances().subList(glyphStart, glyphEnd));
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
        return "GlyphRun{charStart=" + getCharStart()
                + ", charEnd=" + getCharEnd()
                + ", bidiLevel=" + getBidiLevel()
                + ", writingDirection=" + getWritingDirection().toString()
                + ", glyphCount=" + getGlyphCount()
                + ", glyphIds=" + getGlyphIds().toString()
                + ", glyphOffsets=" + getGlyphOffsets().toString()
                + ", glyphAdvances=" + getGlyphAdvances().toString()
                + ", clusterMap=" + getClusterMap().toString()
                + ", originX=" + getOriginX()
                + ", originY=" + getOriginY()
                + ", ascent=" + getAscent()
                + ", descent=" + getDescent()
                + ", leading=" + getLeading()
                + ", width=" + getWidth()
                + ", height=" + getHeight()
                + "}";
    }
}
