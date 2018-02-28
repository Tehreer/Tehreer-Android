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

import com.mta.tehreer.graphics.Renderer;
import com.mta.tehreer.internal.Description;

import java.util.List;

/**
 * Represents a line of text consisting of an array of <code>GlyphRun</code> objects in visual order.
 */
public class ComposedLine {

	private final int charStart;
	private final int charEnd;
    private final byte paragraphLevel;
    private final float extent;
    private final float trailingWhitespaceExtent;
	private final List<GlyphRun> runList;

    private Object[] mSpans;
    private boolean mFirst;

    private float mAscent;
    private float mDescent;
    private float mLeading;

    private float mOriginX;
    private float mOriginY;

	ComposedLine(int charStart, int charEnd, byte paragraphLevel,
                 float ascent, float descent, float leading, float extent,
                 float trailingWhitespaceExtent, List<GlyphRun> runList) {
	    this.charStart = charStart;
	    this.charEnd = charEnd;
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
		return charStart;
	}

    /**
     * Returns the index after the last character of this line in source text.
     *
     * @return The index after the last character of this line in source text.
     */
	public int getCharEnd() {
		return charEnd;
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
    public List<GlyphRun> getRuns() {
        return runList;
    }

    /**
     * Returns the index of character nearest to the specified distance.
     *
     * @param distance The distance for which to determine the character index. It should be offset
     *                 from zero origin.
     * @return The index of character nearest to the specified distance. It will be an absolute
     *         index in source string.
     */
    public int getCharIndexFromDistance(float distance) {
        GlyphRun glyphRun = null;

        for (int i = runList.size() - 1; i >= 0; i--) {
            glyphRun = runList.get(i);
            if (glyphRun.getOriginX() <= distance) {
                break;
            }
        }

        return glyphRun.getCharIndexFromDistance(distance - glyphRun.getOriginX());
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
    public void draw(Renderer renderer, Canvas canvas, float x, float y) {
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
