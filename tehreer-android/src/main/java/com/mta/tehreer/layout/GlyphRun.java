/*
 * Copyright (C) 2016-2018 Muhammad Tayyab Akram
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.style.ForegroundColorSpan;
import android.text.style.ReplacementSpan;
import android.text.style.ScaleXSpan;

import com.mta.tehreer.collections.FloatList;
import com.mta.tehreer.collections.IntList;
import com.mta.tehreer.collections.PointList;
import com.mta.tehreer.graphics.Renderer;
import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.internal.layout.CaretEdgeList;
import com.mta.tehreer.internal.layout.ClusterRange;
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
    private final int startExtraLength;
    private final int endExtraLength;
    private final @NonNull List<Object> spans;
    private final boolean isBackward;
    private final byte bidiLevel;
    private final @NonNull WritingDirection writingDirection;
    private final @NonNull Typeface typeface;
    private final float typeSize;
    private final float ascent;
    private final float descent;
    private final float leading;
    private final @NonNull IntList glyphIds;
    private final @NonNull PointList glyphOffsets;
    private final @NonNull FloatList glyphAdvances;
    private final @NonNull IntList clusterMap;
    private final @NonNull CaretEdgeList caretEdges;
    private float originX;
    private float originY;

    GlyphRun(int charStart, int charEnd, int startExtraLength, int endExtraLength,
             @NonNull List<Object> spans, boolean isBackward, byte bidiLevel,
             @NonNull WritingDirection writingDirection,
             @NonNull Typeface typeface, float typeSize,
             float ascent, float descent, float leading,
             @NonNull IntList glyphIds, @NonNull PointList offsets, @NonNull FloatList advances,
             @NonNull IntList clusterMap, @NonNull CaretEdgeList caretEdges) {
        this.charStart = charStart;
        this.charEnd = charEnd;
        this.startExtraLength = startExtraLength;
        this.endExtraLength = endExtraLength;
        this.spans = spans;
        this.isBackward = isBackward;
        this.writingDirection = writingDirection;
        this.bidiLevel = bidiLevel;
        this.typeface = typeface;
        this.typeSize = typeSize;
        this.ascent = ascent;
        this.descent = descent;
        this.leading = leading;
        this.glyphIds = glyphIds;
        this.glyphOffsets = offsets;
        this.glyphAdvances = advances;
        this.clusterMap = clusterMap;
        this.caretEdges = caretEdges;
    }

    GlyphRun(@NonNull GlyphRun otherRun) {
        this.charStart = otherRun.charStart;
        this.charEnd = otherRun.charEnd;
        this.startExtraLength = otherRun.startExtraLength;
        this.endExtraLength = otherRun.endExtraLength;
        this.spans = otherRun.spans;
        this.isBackward = otherRun.isBackward;
        this.bidiLevel = otherRun.bidiLevel;
        this.writingDirection = otherRun.writingDirection;
        this.typeface = otherRun.typeface;
        this.typeSize = otherRun.typeSize;
        this.ascent = otherRun.ascent;
        this.descent = otherRun.descent;
        this.leading = otherRun.leading;
        this.glyphIds = otherRun.glyphIds;
        this.glyphOffsets = otherRun.glyphOffsets;
        this.glyphAdvances = otherRun.glyphAdvances;
        this.clusterMap = otherRun.clusterMap;
        this.caretEdges = otherRun.caretEdges;
        this.originX = otherRun.originX;
        this.originY = otherRun.originY;
    }

    private String checkCharIndex(int charIndex) {
        if (charIndex < charStart || charIndex >= charEnd) {
            return ("Char Index: " + charIndex
                    + ", Run Range: [" + charStart + ".." + charEnd + ")");
        }

        return null;
    }

    private String checkGlyphRange(int glyphStart, int glyphEnd) {
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
        return startExtraLength;
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
        return endExtraLength;
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
    public @NonNull Typeface getTypeface() {
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
    public @NonNull WritingDirection getWritingDirection() {
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
    public @NonNull IntList getGlyphIds() {
        return glyphIds;
    }

    /**
     * Returns a list of glyph offsets in this run.
     *
     * @return A list of glyph offsets in this run.
     */
    public @NonNull PointList getGlyphOffsets() {
        return glyphOffsets;
    }

    /**
     * Returns a list of glyph advances in this run.
     *
     * @return A list of glyph advances in this run.
     */
    public @NonNull FloatList getGlyphAdvances() {
        return glyphAdvances;
    }

    /**
     * Returns a list of indexes, mapping each character in this run to corresponding glyph.
     *
     * @return A list of indexes, mapping each character in this run to corresponding glyph.
     */
    public @NonNull IntList getClusterMap() {
        return clusterMap;
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
        return ascent;
    }

    /**
     * Returns the descent of this run. The descent is the distance from the baseline to the bottom
     * of the <code>GlyphRun</code>. It is always either positive or zero.
     *
     * @return The descent of this run.
     */
    public float getDescent() {
        return descent;
    }

    /**
     * Returns the leading of this run. The leading is the distance that should be placed between
     * two lines.
     *
     * @return The leading of this run.
     */
    public float getLeading() {
        return leading;
    }

    /**
     * Returns the typographic width of this run.
     *
     * @return The typographic width of this run.
     */
    public float getWidth() {
        int visibleOffset = startExtraLength;
        int visibleLength = charEnd - charStart;

        return caretEdges.distance(visibleOffset, visibleOffset + visibleLength);
    }

    /**
     * Returns the typographic height of this run.
     *
     * @return The typographic height of this run.
     */
    public float getHeight() {
        return (ascent + descent + leading);
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

        int extraStart = charStart - startExtraLength;
        int arrayIndex = charIndex - extraStart;

        return Clusters.actualClusterStart(clusterMap, arrayIndex) + extraStart;
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
        String indexError = checkCharIndex(charIndex);
        if (indexError != null) {
            throw new IllegalArgumentException(indexError);
        }

        int extraStart = charStart - startExtraLength;
        int arrayIndex = charIndex - extraStart;

        return Clusters.actualClusterEnd(clusterMap, arrayIndex) + extraStart;
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
        String indexError = checkCharIndex(charIndex);
        if (indexError != null) {
            throw new IllegalArgumentException(indexError);
        }

        int extraStart = charStart - startExtraLength;
        int arrayIndex = charIndex - extraStart;

        return Clusters.leadingGlyphIndex(clusterMap, arrayIndex, isBackward, glyphIds.size());
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
        String indexError = checkCharIndex(charIndex);
        if (indexError != null) {
            throw new IllegalArgumentException(indexError);
        }

        int extraStart = charStart - startExtraLength;
        int arrayIndex = charIndex - extraStart;

        return Clusters.trailingGlyphIndex(clusterMap, arrayIndex, isBackward, glyphIds.size());
    }

    private float getCaretEdge(int charIndex) {
        int extraStart = charStart - startExtraLength;
        int arrayIndex = charIndex - extraStart;

        return caretEdges.get(arrayIndex);
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
        if (charIndex < charStart || charIndex > charEnd) {
            throw new IllegalArgumentException("Char Index: " + charIndex
                                               + ", Run Range: [" + charStart + ".." + charEnd + ")");
        }

        return getCaretEdge(charIndex);
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
        int extraStart = charStart - startExtraLength;
        boolean reversed = caretEdges.reversed();

        int leadingCharIndex = -1;
        int trailingCharIndex = -1;

        float leadingCaretEdge = 0.0f;
        float trailingCaretEdge = 0.0f;

        int index = (reversed ? charEnd : charStart);
        int next = (reversed ? -1 : 1);

        while (index <= charEnd && index >= charStart) {
            float caretEdge = caretEdges.get(index - extraStart);

            if (caretEdge <= distance) {
                leadingCharIndex = index;
                leadingCaretEdge = caretEdge;
            } else {
                trailingCharIndex = index;
                trailingCaretEdge = caretEdge;
                break;
            }

            index += next;
        }

        if (leadingCharIndex == -1) {
            // No char is covered by the input distance.
            return charStart;
        }

        if (trailingCharIndex == -1) {
            // Whole run is covered by the input distance.
            return charEnd;
        }

        if (distance <= (leadingCaretEdge + trailingCaretEdge) / 2.0f) {
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
        String rangeError = checkGlyphRange(glyphStart, glyphEnd);
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
	public @NonNull RectF computeBoundingBox(@NonNull Renderer renderer, int glyphStart, int glyphEnd) {
        String rangeError = checkGlyphRange(glyphStart, glyphEnd);
        if (rangeError != null) {
            throw new IllegalArgumentException(rangeError);
        }

	    renderer.setTypeface(typeface);
	    renderer.setTypeSize(typeSize);
        renderer.setWritingDirection(writingDirection);

        return renderer.computeBoundingBox(glyphIds.subList(glyphStart, glyphEnd),
                                           glyphOffsets.subList(glyphStart, glyphEnd),
                                           glyphAdvances.subList(glyphStart, glyphEnd));
	}

    private ClusterRange getClusterRange(int charIndex, @Nullable ClusterRange exclusion) {
	    int actualStart = getActualClusterStart(charIndex);
	    int actualEnd = getActualClusterEnd(charIndex);

	    int leadingIndex = getLeadingGlyphIndex(charIndex);
	    int trailingIndex = getTrailingGlyphIndex(charIndex);

        ClusterRange clusterRange = new ClusterRange();
        clusterRange.actualStart = actualStart;
        clusterRange.actualEnd = actualEnd;
        clusterRange.glyphStart = Math.min(leadingIndex, trailingIndex);
        clusterRange.glyphEnd = Math.max(leadingIndex, trailingIndex) + 1;

        if (exclusion != null) {
            clusterRange.glyphStart = Math.max(clusterRange.glyphStart, exclusion.glyphEnd);
        }
        if (clusterRange.glyphStart < clusterRange.glyphEnd) {
            return clusterRange;
        }

        return null;
    }

    private void drawEdgeCluster(@NonNull Renderer renderer, @NonNull Canvas canvas, @NonNull ClusterRange cluster) {
        float clipLeft = Float.NEGATIVE_INFINITY;
        float clipRight = Float.POSITIVE_INFINITY;

        if (!caretEdges.reversed()) {
            if (cluster.actualStart < charStart) {
                clipLeft = getCaretEdge(charStart);
            }
            if (cluster.actualEnd > charEnd) {
                clipRight = getCaretEdge(charEnd);
            }
        } else {
            if (cluster.actualStart < charStart) {
                clipRight = getCaretEdge(charStart);
            }
            if (cluster.actualEnd > charEnd) {
                clipLeft = getCaretEdge(charEnd);
            }
        }

        canvas.save();
        canvas.clipRect(clipLeft, Float.NEGATIVE_INFINITY, clipRight, Float.POSITIVE_INFINITY);
        canvas.translate(getCaretEdge(cluster.actualStart), 0.0f);

        renderer.drawGlyphs(canvas,
                            glyphIds.subList(cluster.glyphStart, cluster.glyphEnd),
                            glyphOffsets.subList(cluster.glyphStart, cluster.glyphEnd),
                            glyphAdvances.subList(cluster.glyphStart, cluster.glyphEnd));

        canvas.restore();
    }

    /**
     * Draws this run completely onto the given <code>canvas</code> using the given
     * <code>renderer</code>.
     *
     * @param renderer The renderer to use for drawing this run.
     * @param canvas The canvas onto which to draw this run.
     */
	public void draw(@NonNull Renderer renderer, @NonNull Canvas canvas) {
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
            ClusterRange firstCluster = null;
            ClusterRange lastCluster = null;

            if (startExtraLength > 0) {
                firstCluster = getClusterRange(charStart, null);
            }
            if (endExtraLength > 0) {
                lastCluster = getClusterRange(charEnd - 1, firstCluster);
            }

            int glyphStart = 0;
            int glyphEnd = glyphIds.size();

            int chunkStart = charStart;

            if (firstCluster != null) {
                drawEdgeCluster(renderer, canvas, firstCluster);
                chunkStart = firstCluster.actualEnd;
                glyphStart = firstCluster.glyphEnd;
            }
            if (lastCluster != null) {
                glyphEnd = lastCluster.glyphStart;
            }

            canvas.save();
            canvas.translate(getCaretEdge(chunkStart), 0.0f);

            renderer.drawGlyphs(canvas,
                                glyphIds.subList(glyphStart, glyphEnd),
                                glyphOffsets.subList(glyphStart, glyphEnd),
                                glyphAdvances.subList(glyphStart, glyphEnd));

            canvas.restore();

            if (lastCluster != null) {
                drawEdgeCluster(renderer, canvas, lastCluster);
            }
        } else {
            int top = (int) -(ascent + 0.5f);
            int bottom = (int) (descent + 0.5f);

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
                + ", bidiLevel=" + bidiLevel
                + ", writingDirection=" + writingDirection.toString()
                + ", glyphCount=" + getGlyphCount()
                + ", glyphIds=" + glyphIds.toString()
                + ", glyphOffsets=" + glyphOffsets.toString()
                + ", glyphAdvances=" + glyphAdvances.toString()
                + ", clusterMap=" + clusterMap.toString()
                + ", originX=" + originX
                + ", originY=" + originY
                + ", ascent=" + ascent
                + ", descent=" + descent
                + ", leading=" + leading
                + ", width=" + getWidth()
                + ", height=" + getHeight()
                + "}";
    }
}
