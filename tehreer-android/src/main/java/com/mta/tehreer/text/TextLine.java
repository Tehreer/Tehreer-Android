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

import com.mta.tehreer.graphics.Renderer;
import com.mta.tehreer.internal.util.Description;
import com.mta.tehreer.text.internal.util.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * Represents a line of text consisting of an array of <code>TextRun</code> objects in visual order.
 */
public class TextLine {

	private int mCharStart;
	private int mCharEnd;
    private byte mParagraphLevel;
    private float mOriginX;
    private float mOriginY;
    private float mAscent;
    private float mDescent;
    private float mWidth;
    private float mTrailingWhitespaceWidth;
	private List<TextRun> mRunList;

	TextLine(String text, int charStart, int charEnd, List<TextRun> runList, byte paragraphLevel) {
		mCharStart = charStart;
		mCharEnd = charEnd;
        mParagraphLevel = paragraphLevel;
        mRunList = Collections.unmodifiableList(runList);

        int trailingWhitespaceStart = StringUtils.getTrailingWhitespaceStart(text, charStart, charEnd);

        for (TextRun textRun : runList) {
            textRun.setOriginX(mWidth);

            float runAscent = textRun.getAscent();
            float runDescent = textRun.getDescent();

            int runCharStart = textRun.getCharStart();
            int runCharEnd = textRun.getCharEnd();
            int runGlyphCount = textRun.getGlyphCount();
            float runWidth = textRun.computeWidth(0, runGlyphCount);

            if (trailingWhitespaceStart >= runCharStart && trailingWhitespaceStart < runCharEnd) {
                int whitespaceGlyphStart = textRun.getCharGlyphStart(trailingWhitespaceStart);
                int whitespaceGlyphEnd = textRun.getCharGlyphEnd(runCharEnd - 1);
                float whitespaceWidth = textRun.computeWidth(whitespaceGlyphStart, whitespaceGlyphEnd);

                mTrailingWhitespaceWidth += whitespaceWidth;
            }

            mAscent = Math.max(mAscent, runAscent);
            mDescent = Math.max(mDescent, runDescent);
            mWidth += runWidth;
        }
	}

    /**
     * Returns the index to the first character of this line in source text.
     *
     * @return The index to the first character of this line in source text.
     */
	public int getCharStart() {
		return mCharStart;
	}

    /**
     * Returns the index after the last character of this line in source text.
     *
     * @return The index after the last character of this line in source text.
     */
	public int getCharEnd() {
		return mCharEnd;
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
     * Returns the ascent of this line which is the maximum ascent from the baseline of all the runs
     * in the <code>TextLine</code>.
     *
     * @return The ascent of this line.
     */
    public float getAscent() {
        return mAscent;
    }

    /**
     * Returns the descent of this line which is the maximum descent from the baseline of all the
     * runs in the <code>TextLine</code>.
     *
     * @return The descent of this line.
     */
    public float getDescent() {
        return mDescent;
    }

    /**
     * Returns the advance width of all glyphs in this line.
     *
     * @return The advance width of all glyphs in this line.
     */
    public float getWidth() {
        return mWidth;
    }

    /**
     * Returns the advance width of the glyphs corresponding to trailing whitespace characters of
     * this line in source text.
     *
     * @return The advance width of the glyphs corresponding to trailing whitespace characters of
     *         this line in source text.
     */
    public float getTrailingWhitespaceWidth() {
        return mTrailingWhitespaceWidth;
    }

    /**
     * Returns a readonly list that contains all the runs of this line.
     *
     * @return A readonly list that contains all the runs of this line.
     */
    public List<TextRun> getRuns() {
        return mRunList;
    }

    /**
     * Returns the pen offset required to draw flush text.
     *
     * @param flushFactor Specifies the kind of flushness. A flush factor of 0 or less indicates
     *                    left flush. A flushFactor of 1.0 or more indicates right flush. Flush
     *                    factors between 0 and 1.0 indicate varying degrees of center flush, with a
     *                    value of 0.5 being totally center flush.
     * @param flushWidth Specifies the width that the flushness operation should apply to.
     * @return A value which can be used to offset the current pen position for the flush operation.
     */
    public float getFlushPenOffset(float flushFactor, float flushWidth) {
        float penOffset = (flushWidth - (mWidth - mTrailingWhitespaceWidth)) * flushFactor;
        if ((mParagraphLevel & 1) == 1) {
            penOffset -= mTrailingWhitespaceWidth;
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
        for (TextRun textRun : mRunList) {
            float translateX = x + (textRun.getOriginX() * renderer.getScaleX());
            float translateY = y + (textRun.getOriginY() * renderer.getScaleY());

            canvas.translate(translateX, translateY);
            textRun.draw(renderer, canvas);
            canvas.translate(-translateX, -translateY);
        }
    }

    @Override
    public String toString() {
        return "TextLine{charStart=" + mCharStart
                + ", charEnd=" + mCharEnd
                + ", originX=" + mOriginX
                + ", originY=" + mOriginY
                + ", ascent=" + mAscent
                + ", descent=" + mDescent
                + ", width=" + mWidth
                + ", trailingWhitespaceWidth=" + mTrailingWhitespaceWidth
                + ", runs=" + Description.forIterable(mRunList)
                + "}";
    }
}
