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
import android.support.annotation.NonNull;
import android.support.annotation.Size;

import com.mta.tehreer.graphics.Renderer;
import com.mta.tehreer.internal.Description;

import java.util.Arrays;
import java.util.List;

/**
 * Represents a line of text consisting of an array of <code>GlyphRun</code> objects in visual order.
 */
public class ComposedLine {
	private final int lineStart;
	private final int lineEnd;
    private final byte paragraphLevel;
    private final float extent;
    private final float trailingWhitespaceExtent;
	private final @NonNull List<GlyphRun> runList;

    private Object[] mSpans;
    private boolean mFirst;
    private float mIntrinsicMargin;
    private float mFlushFactor;

    private float mAscent;
    private float mDescent;
    private float mLeading;

    private float mOriginX;
    private float mOriginY;

	ComposedLine(int charStart, int charEnd, byte paragraphLevel,
                 float ascent, float descent, float leading, float extent,
                 float trailingWhitespaceExtent, @NonNull List<GlyphRun> runList) {
	    this.lineStart = charStart;
	    this.lineEnd = charEnd;
	    this.paragraphLevel = paragraphLevel;
	    this.extent = extent;
	    this.trailingWhitespaceExtent = trailingWhitespaceExtent;
	    this.runList = runList;

	    mAscent = ascent;
	    mDescent = descent;
	    mLeading = leading;
    }

    /**
     * Returns the index to the first character of this line in source text.
     *
     * @return The index to the first character of this line in source text.
     */
	public int getCharStart() {
		return lineStart;
	}

    /**
     * Returns the index after the last character of this line in source text.
     *
     * @return The index after the last character of this line in source text.
     */
	public int getCharEnd() {
		return lineEnd;
	}

	Object[] getSpans() {
	    return mSpans;
    }

    void setSpans(Object[] spans) {
	    mSpans = spans;
    }

    boolean isFirst() {
	    return mFirst;
    }

    void setFirst(boolean first) {
	    mFirst = first;
    }

    float getIntrinsicMargin() {
	    return mIntrinsicMargin;
    }

    void setIntrinsicMargin(float intrinsicMargin) {
	    mIntrinsicMargin = intrinsicMargin;
    }

    public float getFlushFactor() {
        return mFlushFactor;
    }

    public void setFlushFactor(float flushFactor) {
        mFlushFactor = flushFactor;
    }

    /**
     * Returns the paragraph level of this line.
     *
     * @return The paragraph level of this line.
     */
    public byte getParagraphLevel() {
        return paragraphLevel;
    }

    /**
     * Returns the x- origin of this line in parent frame.
     *
     * @return The x- origin of this line in parent frame.
     */
    public float getOriginX() {
        return mOriginX;
    }

    void setOriginX(float originX) {
        mOriginX = originX;
    }

    /**
     * Returns the y- origin of this line in parent frame.
     *
     * @return The y- origin of this line in parent frame.
     */
    public float getOriginY() {
        return mOriginY;
    }

    void setOriginY(float originY) {
        mOriginY = originY;
    }

    /**
     * Returns the ascent of this line which is the maximum ascent from the baseline of all runs.
     *
     * @return The ascent of this line.
     */
    public float getAscent() {
        return mAscent;
    }

    void setAscent(float ascent) {
        mAscent = ascent;
    }

    /**
     * Returns the descent of this line which is the maximum descent from the baseline of all runs.
     *
     * @return The descent of this line.
     */
    public float getDescent() {
        return mDescent;
    }

    void setDescent(float descent) {
        mDescent = descent;
    }

    /**
     * Returns the leading of this line which is the maximum leading of all runs.
     *
     * @return The leading of this line.
     */
    public float getLeading() {
        return mLeading;
    }

    void setLeading(float leading) {
        mLeading = leading;
    }

    /**
     * Returns the typographic width of this line.
     *
     * @return The typographic width of this line.
     */
    public float getWidth() {
        return extent;
    }

    /**
     * Returns the typographic height of this line.
     *
     * @return The typographic height of this line.
     */
    public float getHeight() {
        return (mAscent + mDescent + mLeading);
    }

    float getTop() {
        return mOriginY - mAscent;
    }

    float getBottom() {
        return mOriginY + mDescent + mLeading;
    }

    float getLeft() {
        return mOriginX;
    }

    float getRight() {
        return mOriginX + extent;
    }

    /**
     * Returns the advance sum of glyphs corresponding to the trailing whitespace characters in this
     * line.
     *
     * @return The typographic extent for the glyphs corresponding to the trailing whitespace
     *         characters in this line.
     */
    public float getTrailingWhitespaceExtent() {
        return trailingWhitespaceExtent;
    }

    /**
     * Returns an unmodifiable list that contains all the runs of this line.
     *
     * @return An unmodifiable list that contains all the runs of this line.
     */
    public @NonNull List<GlyphRun> getRuns() {
        return runList;
    }

    /**
     * Determines the distance of specified character from the start of the line assumed at zero.
     *
     * @param charIndex The index of character in source text.
     * @return The distance of specified character from the start of the line assumed at zero.
     *
     * @throws IllegalArgumentException if <code>charIndex</code> is less than line start or greater
     *         than line end.
     */
    public float computeCharDistance(int charIndex) {
        if (charIndex < lineStart || charIndex > lineEnd) {
            throw new IllegalArgumentException("Char Index: " + charIndex
                                               + ", Line Range: [" + lineStart + ".." + lineEnd + ")");
        }

        float distance = 0.0f;

        int runCount = runList.size();
        for (int i = 0; i < runCount; i++) {
            GlyphRun glyphRun = runList.get(i);
            if (charIndex >= glyphRun.getCharStart() && charIndex < glyphRun.getCharEnd()) {
                distance += glyphRun.computeCharDistance(charIndex);
                break;
            }

            distance += glyphRun.getWidth();
        }

        return distance;
    }

    /**
     * Returns an array of visual edges corresponding to the specified character range.
     * <p>
     * The resulting array will contain pairs of leading and trailing edges sorted from left to
     * right. There will be a separate pair for each glyph run occurred in the specified character
     * range. Each edge will be positioned relative to the start of the line assumed at zero.
     *
     * @param charStart The index to the first logical character in source text.
     * @param charEnd The index after the last logical character in source text.
     * @return An array of visual edges corresponding to the specified character range.
     *
     * @throws IllegalArgumentException if <code>charStart</code> is less than line start, or
     *         <code>charEnd</code> is greater than line end, or <code>charStart</code> is greater
     *         than <code>charEnd</code>.
     */
    public @NonNull @Size(multiple = 2) float[] computeVisualEdges(int charStart, int charEnd) {
        if (charStart < lineStart) {
            throw new IllegalArgumentException("Char Start: " + charStart
                                               + ", Line Range: [" + lineStart + ".." + lineEnd + ")");
        }
        if (charEnd > lineEnd) {
            throw new IllegalArgumentException("Char End: " + charEnd
                                               + ", Line Range: [" + lineStart + ".." + lineEnd + ")");
        }
        if (charStart > charEnd) {
            throw new IllegalArgumentException("Bad Range: [" + lineStart + ".." + lineEnd + ")");
        }

        int runCount = runList.size();

        float[] edgeList = new float[runCount * 2];
        int edgeIndex = 0;

        for (int i = 0; i < runCount; i++) {
            GlyphRun glyphRun = runList.get(i);
            int runStart = glyphRun.getCharStart();
            int runEnd = glyphRun.getCharEnd();

            if (runStart < charEnd && runEnd > charStart) {
                int selectionStart = Math.max(charStart, runStart);
                int selectionEnd = Math.min(charEnd, runEnd);

                float leadingEdge = glyphRun.computeCharDistance(selectionStart);
                float trailingEdge = glyphRun.computeCharDistance(selectionEnd);

                float relativeLeft = glyphRun.getOriginX();
                float selectionLeft = Math.min(leadingEdge, trailingEdge) + relativeLeft;
                float selectionRight = Math.max(leadingEdge, trailingEdge) + relativeLeft;

                edgeList[edgeIndex++] = selectionLeft;
                edgeList[edgeIndex++] = selectionRight;
            }
        }

        if (edgeIndex < edgeList.length) {
            edgeList = Arrays.copyOf(edgeList, edgeIndex);
        }

        return edgeList;
    }

    /**
     * Returns the index of character nearest to the specified distance.
     *
     * @param distance The distance for which to determine the character index. It should be offset
     *                 from zero origin.
     * @return The index of character nearest to the specified distance. It will be an absolute
     *         index in source string.
     */
    public int computeNearestCharIndex(float distance) {
        GlyphRun glyphRun = null;

        for (int i = runList.size() - 1; i >= 0; i--) {
            glyphRun = runList.get(i);
            if (glyphRun.getOriginX() <= distance) {
                break;
            }
        }

        return glyphRun.computeNearestCharIndex(distance - glyphRun.getOriginX());
    }

    /**
     * Returns the pen offset required to draw flush text.
     *
     * @param flushFactor Specifies the kind of flushness. A flush factor of 0 or less indicates
     *                    left flush. A flushFactor of 1.0 or more indicates right flush. Flush
     *                    factors between 0 and 1.0 indicate varying degrees of center flush, with a
     *                    value of 0.5 being totally center flush.
     * @param flushExtent Specifies the extent that the flushness operation should apply to.
     * @return A value which can be used to offset the current pen position for the flush operation.
     */
    public float getFlushPenOffset(float flushFactor, float flushExtent) {
        float penOffset = (flushExtent - (extent - trailingWhitespaceExtent)) * flushFactor;
        if ((paragraphLevel & 1) == 1) {
            penOffset -= trailingWhitespaceExtent;
        }

        return penOffset;
    }

    /**
     * Draws this line onto the given <code>canvas</code> using the given <code>renderer</code>.
     *
     * @param renderer The renderer to use for drawing this line.
     * @param canvas The canvas onto which to draw this line.
     * @param x The x- position at which to draw this line.
     * @param y The y- position at which to draw this line.
     */
    public void draw(@NonNull Renderer renderer, @NonNull Canvas canvas, float x, float y) {
        for (GlyphRun glyphRun : runList) {
            float translateX = x + glyphRun.getOriginX();
            float translateY = y + glyphRun.getOriginY();

            canvas.translate(translateX, translateY);
            glyphRun.draw(renderer, canvas);
            canvas.translate(-translateX, -translateY);
        }
    }

    @Override
    public String toString() {
        return "ComposedLine{charStart=" + getCharStart()
                + ", charEnd=" + getCharEnd()
                + ", originX=" + getOriginX()
                + ", originY=" + getOriginY()
                + ", ascent=" + getAscent()
                + ", descent=" + getDescent()
                + ", leading=" + getLeading()
                + ", width=" + getWidth()
                + ", height=" + getHeight()
                + ", trailingWhitespaceExtent=" + getTrailingWhitespaceExtent()
                + ", runs=" + Description.forIterable(runList)
                + "}";
    }
}
