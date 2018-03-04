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
import android.graphics.Paint;
import android.text.Layout;
import android.text.Spanned;
import android.text.style.LeadingMarginSpan;

import com.mta.tehreer.graphics.Renderer;
import com.mta.tehreer.internal.Description;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a frame containing multiple lines of text. The frame object is the output resulting
 * from text-framing process performed by a typesetter object.
 */
public class ComposedFrame {

    private final CharSequence source;
    private final int charStart;
    private final int charEnd;
    private final List<ComposedLine> lineList;

    private float mOriginX;
    private float mOriginY;
    private float mWidth;
    private float mHeight;

    private Paint paint;

    ComposedFrame(CharSequence source, int charStart, int charEnd, List<ComposedLine> lineList) {
        this.source = source;
        this.charStart = charStart;
        this.charEnd = charEnd;
        this.lineList = Collections.unmodifiableList(lineList);
    }

    void setContainerRect(float originX, float originY, float width, float height) {
        mOriginX = originX;
        mOriginY = originY;
        mWidth = width;
        mHeight = height;
    }

    private Paint lazyPaint() {
        if (paint == null) {
            paint = new Paint();
        }

        return paint;
    }

    /**
     * Returns the index to the first character of this frame in source text.
     *
     * @return The index to the first character of this frame in source text.
     */
    public int getCharStart() {
        return charStart;
    }

    /**
     * Returns the index after the last character of this frame in source text.
     *
     * @return The index after the last character of this frame in source text.
     */
    public int getCharEnd() {
        return charEnd;
    }

    /**
     * Returns the x- origin of this frame.
     *
     * @return The x- origin of this frame.
     */
    public float getOriginX() {
        return mOriginX;
    }

    /**
     * Returns the y- origin of this frame.
     *
     * @return The y- origin of this frame.
     */
    public float getOriginY() {
        return mOriginY;
    }

    /**
     * Returns the width of this frame.
     *
     * @return The width of this frame.
     */
    public float getWidth() {
        return mWidth;
    }

    /**
     * Returns the height of this frame.
     *
     * @return The height of this frame.
     */
    public float getHeight() {
        return mHeight;
    }

    /**
     * Returns an unmodifiable list that contains all the lines of this frame.
     *
     * @return An unmodifiable list that contains all the lines of this frame.
     */
    public List<ComposedLine> getLines() {
        return lineList;
    }

    /**
     * Returns the line containing the specified character.
     *
     * @param charIndex The index of character for which to return the line.
     * @return The line containing the specified character.
     *
     * @throws IllegalArgumentException if <code>charIndex</code> is less than frame start or
     *         greater than or equal to frame end.
     */
    public ComposedLine getLineForChar(int charIndex) {
        if (charIndex < charStart || charIndex >= charEnd) {
            throw new IllegalArgumentException("Char Index: " + charIndex
                                               + ", Frame Range: [" + charStart + ".." + charEnd + ")");
        }

        int low = 0;
        int high = lineList.size() - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            ComposedLine value = lineList.get(mid);

            if (charIndex >= value.getCharEnd()) {
                low = mid + 1;
            } else if (charIndex < value.getCharStart()) {
                high = mid - 1;
            } else {
                return value;
            }
        }

        return null;
    }

    /**
     * Returns a suitable line representing the specified position.
     *
     * @param x The x- coordinate of position.
     * @param y The y- coordinate of position.
     * @return A suitable line representing the specified position.
     */
    public ComposedLine getLineForPosition(float x, float y) {
        Iterator<ComposedLine> iterator = lineList.iterator();
        ComposedLine line = null;

        while (iterator.hasNext()) {
            line = iterator.next();

            float top = line.getTop();
            float bottom = top + line.getHeight();
            if (y >= top && y <= bottom) {
                break;
            }
        }

        return line;
    }

    /**
     * Draws this frame onto the given <code>canvas</code> using the given <code>renderer</code>.
     *
     * @param renderer The renderer to use for drawing this frame.
     * @param canvas The canvas onto which to draw this frame.
     * @param x The x- position at which to draw this frame.
     * @param y The y- position at which to draw this frame.
     */
    public void draw(Renderer renderer, Canvas canvas, float x, float y) {
        canvas.translate(x, y);

        for (ComposedLine composedLine : lineList) {
            Object[] spans = composedLine.getSpans();

            int left = 0;
            int right = (int) (getWidth() + 0.5f);

            for (Object style : spans) {
                if (style instanceof LeadingMarginSpan) {
                    LeadingMarginSpan span = (LeadingMarginSpan) style;

                    byte paragraphLevel = composedLine.getParagraphLevel();
                    boolean isLTR = (paragraphLevel & 1) == 0;

                    Paint paint = lazyPaint();
                    int margin = (isLTR ? left : right);
                    int dir = (isLTR ? Layout.DIR_LEFT_TO_RIGHT : Layout.DIR_RIGHT_TO_LEFT);
                    int top = (int) (composedLine.getTop() + 0.5f);
                    int baseline = (int) (composedLine.getOriginY() + 0.5f);
                    int bottom = (int) (composedLine.getTop() + composedLine.getHeight() + 0.5f);
                    Spanned text = (Spanned) source;
                    int start = text.getSpanStart(span);
                    int end = text.getSpanEnd(span);
                    boolean first = composedLine.isFirst();

                    span.drawLeadingMargin(canvas, paint, margin, dir, top, baseline, bottom,
                                           text, start, end, first, null);

                    if (isLTR) {
                        left += span.getLeadingMargin(first);
                    } else {
                        right -= span.getLeadingMargin(first);
                    }
                }
            }

            composedLine.draw(renderer, canvas, composedLine.getOriginX(), composedLine.getOriginY());
        }

        canvas.translate(-x, -y);
    }

    @Override
    public String toString() {
        return "ComposedFrame{charStart=" + charStart
                + ", charEnd=" + charEnd
                + ", originX=" + getOriginX()
                + ", originY=" + getOriginY()
                + ", width=" + getWidth()
                + ", height=" + getHeight()
                + ", lines=" + Description.forIterable(lineList)
                + "}";
    }
}
