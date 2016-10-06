/*
 * Copyright (C) 2016 Muhammad Tayyab Akram
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

    private void verifyCharIndex(int charIndex) {
        if (charIndex < mCharStart || charIndex >= mCharEnd) {
            throw new IndexOutOfBoundsException("Char Index: " + charIndex
                                                + ", Run Range: [" + mCharStart + ".." + mCharEnd + ")");
        }
    }

    private void verifyGlyphIndex(int glyphIndex) {
        if (glyphIndex < 0 || glyphIndex >= mGlyphCount) {
            throw new IndexOutOfBoundsException("Glyph Index: " + glyphIndex);
        }
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
     * Returns the number of glyphs in this run.
     *
     * @return The number of glyphs in this run.
     */
    public int getGlyphCount() {
        return mGlyphCount;
    }

    /**
     * Returns the glyph id at the specified index in this run.
     *
     * @param glyphIndex The index of the glyph record.
     * @return The glyph id at the specified index in this run.
     *
     * @throws IndexOutOfBoundsException if <code>glyphIndex</code> is negative, or
     *         <code>glyphIndex</code> is greater than or equal to {@link #getGlyphCount()}
     */
    public int getGlyphId(int glyphIndex) {
        verifyGlyphIndex(glyphIndex);
        return mGlyphRun.glyphIds[glyphIndex + mGlyphOffset];
    }

    /**
     * Returns the glyph's x- offset at the specified index in this run.
     *
     * @param glyphIndex The index of the glyph record.
     * @return The glyph's x- offset at the specified index in this run.
     *
     * @throws IndexOutOfBoundsException if <code>glyphIndex</code> is negative, or
     *         <code>glyphIndex</code> is greater than or equal to {@link #getGlyphCount()}
     */
    public float getGlyphXOffset(int glyphIndex) {
        verifyGlyphIndex(glyphIndex);
        return mGlyphRun.xOffsets[glyphIndex + mGlyphOffset];
    }

    /**
     * Returns the glyph's y- offset at the specified index in this run.
     *
     * @param glyphIndex The index of the glyph record.
     * @return The glyph's y- offset at the specified index in this run.
     *
     * @throws IndexOutOfBoundsException if <code>glyphIndex</code> is negative, or
     *         <code>glyphIndex</code> is greater than or equal to {@link #getGlyphCount()}
     */
    public float getGlyphYOffset(int glyphIndex) {
        verifyGlyphIndex(glyphIndex);
        return mGlyphRun.yOffsets[glyphIndex + mGlyphOffset];
    }

    /**
     * Returns the glyph's advance at the specified index in this run.
     *
     * @param glyphIndex The index of the glyph record.
     * @return The glyph's advance at the specified index in this run.
     *
     * @throws IndexOutOfBoundsException if <code>glyphIndex</code> is negative, or
     *         <code>glyphIndex</code> is greater than or equal to {@link #getGlyphCount()}
     */
    public float getGlyphAdvance(int glyphIndex) {
        verifyGlyphIndex(glyphIndex);
        return mGlyphRun.advances[glyphIndex + mGlyphOffset];
    }

    /**
     * Returns the index to the first glyph associated with the character at the specified index in
     * source text.
     *
     * @param charIndex The index of the character in source text.
     * @return The index of the first glyph associated with the character at the specified index in
     *         source text.
     *
     * @throws IllegalArgumentException if <code>charIndex</code> is less than
     *         {@link #getCharStart()}, or greater than or equal to {@link #getCharEnd()}
     */
    public int getCharGlyphStart(int charIndex) {
        verifyCharIndex(charIndex);
        return (mGlyphRun.charGlyphStart(charIndex) - mGlyphOffset);
    }

    /**
     * Returns the index after the last glyph associated with the character at the specified index
     * in source text.
     *
     * @param charIndex The index of the character in source text.
     * @return The index after the last glyph associated with the character at the specified index
     *         in source text.
     *
     * @throws IllegalArgumentException if <code>charIndex</code> is less than
     *         {@link #getCharStart()}, or greater than or equal to {@link #getCharEnd()}
     */
    public int getCharGlyphEnd(int charIndex) {
        verifyCharIndex(charIndex);
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
     * Calculates the advance width for the given glyph range in this run.
     *
     * @param glyphStart The index to first glyph being measured.
     * @param glyphEnd The index after the last glyph being measured.
     * @return The advance width for the given glyph range in this run.
     */
    public float computeWidth(int glyphStart, int glyphEnd) {
        verifyGlyphRange(glyphStart, glyphEnd);

        float[] advances = mGlyphRun.advances;
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
	    renderer.setTextSize(mGlyphRun.fontSize);

        return renderer.computeBoundingBox(mGlyphRun.glyphIds,
                                           mGlyphRun.xOffsets, mGlyphRun.yOffsets, mGlyphRun.advances,
                                           glyphStart + mGlyphOffset, glyphEnd + mGlyphOffset);
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
        renderer.setTextSize(mGlyphRun.fontSize);
        renderer.setTextDirection(mGlyphRun.textDirection());

	    renderer.drawGlyphs(canvas,
                            mGlyphRun.glyphIds,
                            mGlyphRun.xOffsets, mGlyphRun.yOffsets, mGlyphRun.advances,
                            glyphStart + mGlyphOffset, glyphEnd + mGlyphOffset);
	}

    @Override
    public String toString() {
        StringBuilder glyphIdsBuilder = new StringBuilder();
        StringBuilder xOffsetsBuilder = new StringBuilder();
        StringBuilder yOffsetsBuilder = new StringBuilder();
        StringBuilder advancesBuilder = new StringBuilder();

        glyphIdsBuilder.append("[");
        xOffsetsBuilder.append("[");
        yOffsetsBuilder.append("[");
        advancesBuilder.append("[");

        for (int i = 0; i < mGlyphCount; i++) {
            glyphIdsBuilder.append(mGlyphRun.glyphIds[i + mGlyphOffset]);
            xOffsetsBuilder.append(mGlyphRun.xOffsets[i + mGlyphOffset]);
            yOffsetsBuilder.append(mGlyphRun.yOffsets[i + mGlyphOffset]);
            advancesBuilder.append(mGlyphRun.advances[i + mGlyphOffset]);

            if (i < mGlyphCount - 1) {
                glyphIdsBuilder.append(", ");
                xOffsetsBuilder.append(", ");
                yOffsetsBuilder.append(", ");
                advancesBuilder.append(", ");
            }
        }

        glyphIdsBuilder.append("]");
        xOffsetsBuilder.append("]");
        yOffsetsBuilder.append("]");
        advancesBuilder.append("]");

        return "TextRun{charStart=" + mCharStart
                + ", charEnd=" + mCharEnd
                + ", glyphCount=" + mGlyphCount
                + ", glyphIds=" + glyphIdsBuilder.toString()
                + ", glyphXOffsets=" + xOffsetsBuilder.toString()
                + ", glyphYOffsets=" + yOffsetsBuilder.toString()
                + ", glyphAdvances=" + advancesBuilder.toString()
                + ", originX=" + mOriginX
                + ", originY=" + mOriginY
                + ", ascent=" + getAscent()
                + ", descent=" + getDescent()
                + "}";
    }
}
