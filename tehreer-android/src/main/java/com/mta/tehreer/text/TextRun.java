/*
 * Copyright (C) 2017 Muhammad Tayyab Akram
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

package com.mta.tehreer.text;

import android.graphics.Canvas;
import android.graphics.RectF;

import com.mta.tehreer.graphics.Renderer;
import com.mta.tehreer.internal.util.SafeFloatList;
import com.mta.tehreer.internal.util.SafeIntList;
import com.mta.tehreer.internal.util.SafePointList;
import com.mta.tehreer.opentype.WritingDirection;
import com.mta.tehreer.util.FloatList;
import com.mta.tehreer.util.IntList;
import com.mta.tehreer.util.PointList;

/**
 * A <code>TextRun</code> object is a collection of consecutive glyphs sharing the same attributes
 * and direction.
 */
public class TextRun {

    private GlyphRun mGlyphRun;
	private int mCharStart;
	private int mCharEnd;
    private int mGlyphOffset;
    private int mGlyphCount;
    private float mOriginX;
    private float mOriginY;
    private float mWidth = Float.NEGATIVE_INFINITY;

	TextRun(GlyphRun glyphRun, int charStart, int charEnd) {
        mGlyphRun = glyphRun;
        mCharStart = charStart;
        mCharEnd = charEnd;
        mGlyphOffset = glyphRun.charGlyphStart(charStart);
        mGlyphCount = glyphRun.charGlyphEnd(charEnd - 1) - mGlyphOffset;
	}

    TextRun(TextRun otherRun) {
        mGlyphRun = otherRun.mGlyphRun;
        mGlyphOffset = otherRun.mGlyphOffset;
        mGlyphCount = otherRun.mGlyphCount;
    }

    private void verifyGlyphRange(int glyphStart, int glyphEnd) {
        if (glyphStart < 0) {
            throw new IllegalArgumentException("Glyph Start: " + glyphStart);
        }
        if (glyphEnd > mGlyphCount) {
            throw new IllegalArgumentException("Glyph End: " + glyphEnd
                                               + "Glyph Count: " + mGlyphCount);
        }
        if (glyphStart > glyphEnd) {
            throw new IllegalArgumentException("Glyph Start: " + glyphStart
                                               + ", Glyph End: " + glyphEnd);
        }
    }

    GlyphRun getGlyphRun() {
        return mGlyphRun;
    }

    /**
     * Returns the index to the first character of this run in source text.
     *
     * @return The index to the first character of this run in source text.
     */
    public int getCharStart() {
        return mCharStart;
    }

    /**
     * Returns the index after the last character of this run in source text.
     *
     * @return The index after the last character of this run in source text.
     */
    public int getCharEnd() {
        return mCharEnd;
    }

    /**
     * Returns the bidirectional level of this run.
     *
     * @return The bidirectional level of this run.
     */
    public byte getBidiLevel() {
        return mGlyphRun.bidiLevel;
    }

    /**
     * Returns the writing direction of this run.
     *
     * @return The writing direction of this run.
     */
    public WritingDirection getWritingDirection() {
        return mGlyphRun.writingDirection();
    }

    /**
     * Returns the number of glyphs in this run.
     *
     * @return The number of glyphs in this run.
     */
    public int getGlyphCount() {
        return mGlyphCount;
    }

    /**
     * Returns a list of glyph IDs in this run.
     *
     * @return A list of glyph IDs in this run.
     */
    public IntList getGlyphIds() {
        return new SafeIntList(mGlyphRun.glyphIds, mGlyphOffset, mGlyphCount);
    }

    /**
     * Returns a list of glyph offsets in this run.
     *
     * @return A list of glyph offsets in this run.
     */
    public PointList getGlyphOffsets() {
        return new SafePointList(mGlyphRun.glyphOffsets, mGlyphOffset, mGlyphCount);
    }

    /**
     * Returns a list of glyph advances in this run.
     *
     * @return A list of glyph advances in this run.
     */
    public FloatList getGlyphAdvances() {
        return new SafeFloatList(mGlyphRun.glyphAdvances, mGlyphOffset, mGlyphCount);
    }

    /**
     * Returns a list of indexes, mapping each character in this run to corresponding glyph.
     *
     * @return A list of indexes, mapping each character in this run to corresponding glyph.
     */
    public IntList getCharToGlyphMap() {
        return new IndexList(mGlyphRun.charToGlyphMap,
                             mCharStart - mGlyphRun.charStart,
                             mCharEnd - mCharStart,
                             mGlyphOffset);
    }

    private static class IndexList extends SafeIntList {

        final int difference;

        public IndexList(int[] array, int offset, int size, int difference) {
            super(array, offset, size);
            this.difference = difference;
        }

        @Override
        public int get(int index) {
            return super.get(index) - difference;
        }
    }

    int getCharGlyphStart(int charIndex) {
        return (mGlyphRun.charGlyphStart(charIndex) - mGlyphOffset);
    }

    int getCharGlyphEnd(int charIndex) {
        return (mGlyphRun.charGlyphEnd(charIndex) - mGlyphOffset);
    }

    /**
     * Returns the x- origin of this run in parent line.
     *
     * @return The x- origin of this run in parent line.
     */
    public float getOriginX() {
        return mOriginX;
    }

    void setOriginX(float originX) {
        mOriginX = originX;
    }

    /**
     * Returns the y- origin of this run in parent line.
     *
     * @return The y- origin of this run in parent line.
     */
    public float getOriginY() {
        return mOriginY;
    }

    void setOriginY(float originY) {
        mOriginY = originY;
    }

    /**
     * Returns the ascent of this run. The ascent is the distance from the top of the
     * <code>TextRun</code> to the baseline. It is always either positive or zero.
     *
     * @return The ascent of this run.
     */
    public float getAscent() {
        return mGlyphRun.ascent();
    }

    /**
     * Returns the descent of this run. The descent is the distance from the baseline to the bottom
     * of the <code>TextRun</code>. It is always either positive or zero.
     *
     * @return The descent of this run.
     */
    public float getDescent() {
        return mGlyphRun.descent();
    }

    /**
     * Returns the leading of this run. The leading is the distance that should be placed between
     * two lines.
     *
     * @return The leading of this run.
     */
    public float getLeading() {
        return mGlyphRun.leading();
    }

    /**
     * Returns the typographic width of this run.
     *
     * @return The typographic width of this run.
     */
    public float getWidth() {
        // Locking is not required for constant width.
        if (mWidth == Float.NEGATIVE_INFINITY) {
            mWidth = computeTypographicExtent(0, mGlyphCount);
        }

        return mWidth;
    }

    /**
     * Returns the typographic height of this run.
     *
     * @return The typographic height of this run.
     */
    public float getHeight() {
        return (mGlyphRun.ascent() + mGlyphRun.descent() + mGlyphRun.leading());
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
        verifyGlyphRange(glyphStart, glyphEnd);

        float[] advances = mGlyphRun.glyphAdvances;
        float width = 0.0f;

        glyphStart += mGlyphOffset;
        glyphEnd += mGlyphOffset;

        for (int i = glyphStart; i < glyphEnd; i++) {
            width += advances[i];
        }

        return width;
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
	    verifyGlyphRange(glyphStart, glyphEnd);

	    renderer.setTypeface(mGlyphRun.typeface);
	    renderer.setTypeSize(mGlyphRun.typeSize);
        renderer.setWritingDirection(mGlyphRun.writingDirection());

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
	    draw(renderer, canvas, 0, mGlyphCount);
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
        verifyGlyphRange(glyphStart, glyphEnd);

	    renderer.setTypeface(mGlyphRun.typeface);
        renderer.setTypeSize(mGlyphRun.typeSize);
        renderer.setWritingDirection(mGlyphRun.writingDirection());

	    renderer.drawGlyphs(canvas,
                            getGlyphIds().subList(glyphStart, glyphEnd),
                            getGlyphOffsets().subList(glyphStart, glyphEnd),
                            getGlyphAdvances().subList(glyphStart, glyphEnd));
	}

    @Override
    public String toString() {
        return "TextRun{charStart=" + getCharStart()
                + ", charEnd=" + getCharEnd()
                + ", bidiLevel=" + getBidiLevel()
                + ", writingDirection=" + getWritingDirection().toString()
                + ", glyphCount=" + getGlyphCount()
                + ", glyphIds=" + getGlyphIds().toString()
                + ", glyphOffsets=" + getGlyphOffsets().toString()
                + ", glyphAdvances=" + getGlyphAdvances().toString()
                + ", charToGlyphMap=" + getCharToGlyphMap().toString()
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
