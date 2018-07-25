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
    private final int startAddedLength;
    private final int endAddedLength;
    private final List<Object> spans;
    private final boolean isBackward;
    private final byte bidiLevel;
    private final WritingDirection writingDirection;
    private final Typeface typeface;
    private final float typeSize;
    private final float ascent;
    private final float descent;
    private final float leading;
    private final IntList glyphIds;
    private final PointList glyphOffsets;
    private final FloatList glyphAdvances;
    private final IntList clusterMap;
    private final CaretEdgeList caretEdges;
    private float originX;
    private float originY;

    GlyphRun(int charStart, int charEnd, int startAddedLength, int endAddedLength,
             List<Object> spans, boolean isBackward, byte bidiLevel,
             WritingDirection writingDirection, Typeface typeface, float typeSize,
             float ascent, float descent, float leading,
             IntList glyphIds, PointList offsets, FloatList advances, IntList clusterMap,
             CaretEdgeList caretEdges) {
        this.charStart = charStart;
        this.charEnd = charEnd;
        this.startAddedLength = startAddedLength;
        this.endAddedLength = endAddedLength;
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

    GlyphRun(GlyphRun otherRun) {
        this.charStart = otherRun.charStart;
        this.charEnd = otherRun.charEnd;
        this.startAddedLength = otherRun.startAddedLength;
        this.endAddedLength = otherRun.endAddedLength;
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

    public int getStartAddedLength() {
        return startAddedLength;
    }

    public int getEndAddedLength() {
        return endAddedLength;
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
        int visibleOffset = startAddedLength;
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

        int addedStart = charStart - startAddedLength;
        int arrayIndex = charIndex - addedStart;

        return Clusters.actualClusterStart(clusterMap, arrayIndex) + addedStart;
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

        int addedStart = charStart - startAddedLength;
        int arrayIndex = charIndex - addedStart;

        return Clusters.actualClusterEnd(clusterMap, arrayIndex) + addedStart;
    }

    /**
     * Returns the index of leading glyph related to specified cluster. The trailing glyph will
     * always come after the leading glyph of a cluster, even if the run logically flows backward.
     *
     * @param charIndex The index of a character in source string.
     * @return The index of leading glyph related to specified cluster.
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

        int addedStart = charStart - startAddedLength;
        int arrayIndex = charIndex - addedStart;

        return Clusters.leadingGlyphIndex(clusterMap, arrayIndex, isBackward, glyphIds.size());
    }

    /**
     * Returns the index of trailing glyph related to specified cluster. The leading glyph will
     * always come before the trailing glyph of a cluster, even if the run logically flows backward.
     *
     * @param charIndex The index of a character in source string.
     * @return The index of trailing glyph related to specified cluster.
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

        int addedStart = charStart - startAddedLength;
        int arrayIndex = charIndex - addedStart;

        return Clusters.trailingGlyphIndex(clusterMap, arrayIndex, isBackward, glyphIds.size());
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
        if (writingDirection == WritingDirection.RIGHT_TO_LEFT) {
            // Reverse the distance in case of right to left direction.
            distance = getWidth() - distance;
        }

        int clusterStart = 0;
        int charCount = clusterMap.size();

        int glyphStart = 0;
        int glyphCount = glyphIds.size();

        int leadingCharIndex = charStart;
        int trailingCharIndex = charEnd;

        float computedAdvance = 0.0f;
        float leadingEdgeAdvance = 0.0f;

        for (int i = 1; i <= charCount; i++) {
            int glyphIndex = (i < charCount ? clusterMap.get(i) : glyphCount);
            if (glyphIndex == glyphStart) {
                continue;
            }

            // Find the advance of current cluster.
            float clusterAdvance = 0.0f;
            for (int j = glyphStart; j < glyphIndex; j++) {
                clusterAdvance += glyphAdvances.get(j);
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
	public RectF computeBoundingBox(Renderer renderer, int glyphStart, int glyphEnd) {
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

    private ClusterRange getClusterRange(int charIndex, ClusterRange exclusion) {
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

    private float getCaretEdge(int charIndex) {
        int addedStart = charStart - startAddedLength;
        int arrayIndex = charIndex - addedStart;
        float caretEdge = caretEdges.get(arrayIndex);

        if (!caretEdges.reversed()) {
            if (startAddedLength > 0) {
                caretEdge -= caretEdges.distance(0, startAddedLength);
            }
        } else {
            if (endAddedLength > 0) {
                int fromIndex = charEnd - addedStart;
                int toIndex = fromIndex + endAddedLength;

                caretEdge -= caretEdges.distance(fromIndex, toIndex);
            }
        }

        return caretEdge;
    }

    private void drawEdgeCluster(Renderer renderer, Canvas canvas, ClusterRange cluster) {
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
	public void draw(Renderer renderer, Canvas canvas) {
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

            if (startAddedLength > 0) {
                firstCluster = getClusterRange(charStart, null);
            }
            if (endAddedLength > 0) {
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
