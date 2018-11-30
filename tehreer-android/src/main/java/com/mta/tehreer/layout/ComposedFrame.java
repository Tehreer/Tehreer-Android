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
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.Spanned;
import android.text.style.LeadingMarginSpan;
import android.text.style.LineBackgroundSpan;

import com.mta.tehreer.graphics.Renderer;
import com.mta.tehreer.internal.Description;

import java.util.Collections;
import java.util.List;

import static com.mta.tehreer.internal.util.Preconditions.checkArgument;

/**
 * Represents a frame containing multiple lines of text. The frame object is the output resulting
 * from text-framing process performed by a typesetter object.
 */
public class ComposedFrame {
    private final CharSequence source;
    private final int frameStart;
    private final int frameEnd;
    private final @NonNull List<ComposedLine> lineList;

    private float mOriginX;
    private float mOriginY;
    private float mWidth;
    private float mHeight;

    private @Nullable Paint paint;

    ComposedFrame(CharSequence source, int charStart, int charEnd,
                  @NonNull List<ComposedLine> lineList) {
        this.source = source;
        this.frameStart = charStart;
        this.frameEnd = charEnd;
        this.lineList = Collections.unmodifiableList(lineList);
    }

    void setContainerRect(float originX, float originY, float width, float height) {
        mOriginX = originX;
        mOriginY = originY;
        mWidth = width;
        mHeight = height;
    }

    private @NonNull Paint lazyPaint() {
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
        return frameStart;
    }

    /**
     * Returns the index after the last character of this frame in source text.
     *
     * @return The index after the last character of this frame in source text.
     */
    public int getCharEnd() {
        return frameEnd;
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
    public @NonNull List<ComposedLine> getLines() {
        return lineList;
    }

    /**
     * Returns the index of line containing the specified character.
     *
     * @param charIndex The index of character for which to return the line index.
     * @return The index of line containing the specified character.
     *
     * @throws IllegalArgumentException if <code>charIndex</code> is less than frame start or
     *         greater than frame end.
     */
    public int getLineIndexForChar(int charIndex) {
        if (charIndex < frameStart || charIndex > frameEnd) {
            throw new IllegalArgumentException("Char Index: " + charIndex
                                               + ", Frame Range: [" + frameStart + ".." + frameEnd + ")");
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
                return mid;
            }
        }

        return high;
    }

    /**
     * Returns the index of a suitable line representing the specified position.
     *
     * @param x The x- coordinate of position.
     * @param y The y- coordinate of position.
     * @return The index of a suitable line representing the specified position.
     */
    public int getLineIndexForPosition(float x, float y) {
        int lineCount = lineList.size();

        for (int i = 0; i < lineCount; i++) {
            ComposedLine line = lineList.get(i);
            float top = line.getTop();
            float bottom = top + line.getHeight();
            if (y >= top && y <= bottom) {
                return i;
            }
        }

        return lineCount - 1;
    }

    private void addSelectionParts(@NonNull ComposedLine line, int charStart, int charEnd,
                                   float selectionTop, float selectionBottom, @NonNull Path selectionPath) {
        float[] visualEdges = line.computeVisualEdges(charStart, charEnd);
        float lineLeft = line.getLeft();

        int edgeCount = visualEdges.length;
        int edgeIndex = 0;

        while (edgeIndex < edgeCount) {
            float selectionLeft = visualEdges[edgeIndex++] + lineLeft;
            float selectionRight = visualEdges[edgeIndex++] + lineLeft;

            selectionPath.addRect(selectionLeft, selectionTop,
                                  selectionRight, selectionBottom, Path.Direction.CW);
        }
    }

    private void checkSubRange(int charStart, int charEnd) {
        checkArgument(charStart >= frameStart, "Char Start: " + charStart + ", Frame Range: [" + frameStart + ", " + frameEnd + ')');
        checkArgument(charEnd <= frameEnd, "Char End: " + charEnd + ", Frame Range: [" + frameStart + ", " + frameEnd + ')');
        checkArgument(charEnd >= charStart, "Bad Range: [" + charStart + ", " + charEnd + ')');
    }

    /**
     * Generates a path that contains a set of rectangles covering the specified selection range.
     *
     * @param charStart The index to the first character of selection in source text.
     * @param charEnd The index after the first character of selection in source text.
     * @return A path that contains a set of rectangles covering the specified selection range.
     *
     * @throws IllegalArgumentException if <code>charStart</code> is less than frame start, or
     *         <code>charEnd</code> is greater than frame end, or <code>charStart</code> is greater
     *         than <code>charEnd</code>.
     */
    public @NonNull Path generateSelectionPath(int charStart, int charEnd) {
        checkSubRange(charStart, charEnd);

        Path selectionPath = new Path();

        int firstIndex = getLineIndexForChar(charStart);
        int lastIndex = getLineIndexForChar(charEnd);

        ComposedLine firstLine = lineList.get(firstIndex);
        ComposedLine lastLine = lineList.get(lastIndex);

        float firstTop = firstLine.getTop();
        float lastBottom = lastLine.getBottom();

        if (firstLine == lastLine) {
            addSelectionParts(firstLine, charStart, charEnd, firstTop, lastBottom, selectionPath);
        } else {
            float frameLeft = 0.0f;
            float frameRight = mWidth;

            float firstBottom = firstLine.getBottom();
            float lastTop = lastLine.getTop();

            // Select each intersecting part of first line.
            addSelectionParts(firstLine, charStart, firstLine.getCharEnd(),
                              firstTop, firstBottom, selectionPath);

            // Select trailing padding of first line.
            if ((lastLine.getParagraphLevel() & 1) == 1) {
                selectionPath.addRect(frameLeft, firstTop,
                                      firstLine.getLeft(), firstBottom, Path.Direction.CW);
            } else {
                selectionPath.addRect(firstLine.getRight(), firstTop,
                                      frameRight, firstBottom, Path.Direction.CW);
            }

            // Select whole part of each mid line.
            for (int i = firstIndex + 1; i < lastIndex; i++) {
                ComposedLine midLine = lineList.get(i);
                float midTop = midLine.getTop();
                float midBottom = midLine.getBottom();

                selectionPath.addRect(frameLeft, midTop,
                                      frameRight, midBottom, Path.Direction.CW);
            }

            // Select leading padding of last line.
            if ((lastLine.getParagraphLevel() & 1) == 1) {
                selectionPath.addRect(lastLine.getRight(), lastTop,
                                      frameRight, lastBottom, Path.Direction.CW);
            } else {
                selectionPath.addRect(frameLeft, lastTop,
                                      lastLine.getLeft(), lastBottom, Path.Direction.CW);
            }

            // Select each intersecting part of last line.
            addSelectionParts(lastLine, lastLine.getCharStart(), charEnd,
                              lastTop, lastBottom, selectionPath);
        }

        return selectionPath;
    }

    private void drawBackground(@NonNull Canvas canvas) {
        int frameLeft = 0;
        int frameRight = (int) (mWidth + 0.5f);

        int lineCount = lineList.size();
        for (int i = 0; i < lineCount; i++) {
            ComposedLine composedLine = lineList.get(i);
            Object[] lineSpans = composedLine.getSpans();

            for (Object style : lineSpans) {
                if (style instanceof LineBackgroundSpan) {
                    LineBackgroundSpan span = (LineBackgroundSpan) style;

                    Spanned sourceText = (Spanned) source;
                    int spanStart = sourceText.getSpanStart(span);
                    int spanEnd = sourceText.getSpanEnd(span);

                    int lineStart = composedLine.getCharStart();
                    int lineEnd = composedLine.getCharEnd();
                    if (lineStart >= spanEnd || lineEnd <= spanStart) {
                        continue;
                    }

                    Paint paint = lazyPaint();
                    int lineTop = (int) (composedLine.getTop() + 0.5f);
                    int lineBaseline = (int) (composedLine.getOriginY() + 0.5f);
                    int lineBottom = (int) (composedLine.getTop() + composedLine.getHeight() + 0.5f);

                    span.drawBackground(canvas, paint, frameLeft, frameRight,
                                        lineTop, lineBaseline, lineBottom,
                                        sourceText, lineStart, lineEnd, i);
                }
            }
        }
    }

    /**
     * Draws this frame onto the given <code>canvas</code> using the given <code>renderer</code>.
     *
     * @param renderer The renderer to use for drawing this frame.
     * @param canvas The canvas onto which to draw this frame.
     * @param x The x- position at which to draw this frame.
     * @param y The y- position at which to draw this frame.
     */
    public void draw(@NonNull Renderer renderer, @NonNull Canvas canvas, float x, float y) {
        canvas.translate(x, y);

        drawBackground(canvas);

        int lineCount = lineList.size();
        for (int i = 0; i < lineCount; i++) {
            ComposedLine composedLine = lineList.get(i);
            Object[] lineSpans = composedLine.getSpans();

            int lineLeft = 0;
            int lineRight = (int) (mWidth + 0.5f);

            // Draw leading margins of this line.
            for (Object style : lineSpans) {
                if (style instanceof LeadingMarginSpan) {
                    LeadingMarginSpan span = (LeadingMarginSpan) style;

                    byte paragraphLevel = composedLine.getParagraphLevel();
                    boolean isLTR = (paragraphLevel & 1) == 0;

                    Paint paint = lazyPaint();
                    int margin = (isLTR ? lineLeft : lineRight);
                    int direction = (isLTR ? Layout.DIR_LEFT_TO_RIGHT : Layout.DIR_RIGHT_TO_LEFT);
                    int lineTop = (int) (composedLine.getTop() + 0.5f);
                    int lineBaseline = (int) (composedLine.getOriginY() + 0.5f);
                    int lineBottom = (int) (composedLine.getTop() + composedLine.getHeight() + 0.5f);
                    Spanned sourceText = (Spanned) source;
                    int lineStart = composedLine.getCharStart();
                    int lineEnd = composedLine.getCharEnd();
                    boolean isFirst = composedLine.isFirst();

                    span.drawLeadingMargin(canvas, paint, margin, direction,
                                           lineTop, lineBaseline, lineBottom,
                                           sourceText, lineStart, lineEnd, isFirst, null);

                    if (isLTR) {
                        lineLeft += span.getLeadingMargin(isFirst);
                    } else {
                        lineRight -= span.getLeadingMargin(isFirst);
                    }
                }
            }

            composedLine.draw(renderer, canvas, composedLine.getOriginX(), composedLine.getOriginY());
        }

        canvas.translate(-x, -y);
    }

    @Override
    public String toString() {
        return "ComposedFrame{charStart=" + frameStart
                + ", charEnd=" + frameEnd
                + ", originX=" + mOriginX
                + ", originY=" + mOriginY
                + ", width=" + mWidth
                + ", height=" + mHeight
                + ", lines=" + Description.forIterable(lineList)
                + '}';
    }
}
