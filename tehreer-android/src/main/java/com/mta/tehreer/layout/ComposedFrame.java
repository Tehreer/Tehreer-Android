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

package com.mta.tehreer.layout;

import android.graphics.Canvas;

import com.mta.tehreer.graphics.Renderer;
import com.mta.tehreer.internal.util.Description;

import java.util.Collections;
import java.util.List;

/**
 * Represents a frame containing multiple lines of text. The frame object is the output resulting
 * from text-framing process performed by a typesetter object.
 */
public class ComposedFrame {

    private int mCharStart;
    private int mCharEnd;
    private List<ComposedLine> mLineList;

    ComposedFrame(int charStart, int charEnd, List<ComposedLine> lineList) {
        mCharStart = charStart;
        mCharEnd = charEnd;
        mLineList = Collections.unmodifiableList(lineList);
    }

    /**
     * Returns the index to the first character of this frame in source text.
     *
     * @return The index to the first character of this frame in source text.
     */
    public int getCharStart() {
        return mCharStart;
    }

    /**
     * Returns the index after the last character of this frame in source text.
     *
     * @return The index after the last character of this frame in source text.
     */
    public int getCharEnd() {
        return mCharEnd;
    }

    /**
     * Returns an unmodifiable list that contains all the lines of this frame.
     *
     * @return An unmodifiable list that contains all the lines of this frame.
     */
    public List<ComposedLine> getLines() {
        return mLineList;
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
        for (ComposedLine composedLine : mLineList) {
            canvas.translate(x, y);
            composedLine.draw(renderer, canvas, composedLine.getOriginX(), composedLine.getOriginY());
            canvas.translate(-x, -y);
        }
    }

    @Override
    public String toString() {
        return "ComposedFrame{charStart=" + mCharStart
                + ", charEnd=" + mCharEnd
                + ", lines=" + Description.forIterable(mLineList)
                + "}";
    }
}
