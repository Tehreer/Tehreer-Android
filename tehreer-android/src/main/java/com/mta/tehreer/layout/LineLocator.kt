/*
 * Copyright (C) 2026 Muhammad Tayyab Akram
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

package com.mta.tehreer.layout

import android.graphics.RectF
import com.mta.tehreer.graphics.Renderer
import com.mta.tehreer.internal.JniBridge

/**
 * Locates the lines of a [ComposedFrame] that are visible in a rectangle, without looking at all of
 * them. A view that shows a long text uses it to find the lines to show.
 *
 * The locator keeps a box for each line, in the coordinates of the frame. The box of a line is the
 * union of the rectangle that spans the width of the frame from the top to the bottom of the line,
 * and the ink box of its glyphs, which can be taller than the line. The edges of the boxes are
 * rounded outward to integers, and the boxes of the lines can overlap.
 *
 * A locator is immutable and can be used from any thread.
 *
 * @constructor Creates a locator that finds the lines of a frame. The ink boxes of the lines are
 * measured with the given renderer, which is the one that draws the frame.
 *
 * @param frame The frame whose lines are located.
 * @param renderer The renderer that decides how the glyphs are measured.
 *
 * @throws IllegalStateException if the locator could not be created, for example when the lines
 *         are placed billions of units apart.
 */
class LineLocator(frame: ComposedFrame, renderer: Renderer) {
    private class Finalizable(private val nativeLocator: Long) {
        @Suppress("unused")
        protected fun finalize() {
            nDispose(nativeLocator)
        }
    }

    private val nativeLocator = nCreate(frame.nativeFrame, renderer.nativeHandle)
    private val finalizable: Finalizable

    init {
        check(nativeLocator != 0L) { "Could not create the line locator" }
        finalizable = Finalizable(nativeLocator)
    }

    /**
     * The number of lines of the frame that this locator was made from.
     */
    val lineCount: Int
        get() = nGetLineCount(nativeLocator)

    /**
     * Returns the box of a line, in the coordinates of the frame.
     *
     * @param lineIndex The index of the line.
     * @param outBox The rectangle that receives the box, which has integral edges. A new one is
     *        made if it is not given, so pass one in a loop to avoid the allocation.
     * @return `outBox`.
     *
     * @throws IndexOutOfBoundsException if `lineIndex` is negative or not less than [lineCount].
     */
    @JvmOverloads
    fun getLineBox(lineIndex: Int, outBox: RectF = RectF()): RectF {
        if (lineIndex < 0 || lineIndex >= lineCount) {
            throw IndexOutOfBoundsException("Line Index: $lineIndex, Line Count: $lineCount")
        }

        nGetLineBox(nativeLocator, lineIndex, outBox)

        return outBox
    }

    /**
     * Finds the lines whose boxes intersect a rectangle, without allocating. Two rectangles
     * intersect if they overlap by more than their edges, so a box that only touches the
     * rectangle is not included.
     *
     * The lines are in the order of the frame, unless the ink of a line reaches into a band above
     * the lines before it, in which case that line comes first.
     *
     * The indexes are written into `indexes`, which is meant to be kept and reused for the calls
     * that follow, as when scrolling. If it is too small, only the first lines fit in it and the
     * result is greater than its size: make a larger one and call again.
     *
     * ```
     * var count = locator.findLines(bounds, indexes)
     * if (count > indexes.size) {
     *     indexes = IntArray(count)
     *     count = locator.findLines(bounds, indexes)
     * }
     * ```
     *
     * @param bounds The rectangle in the coordinates of the frame. It can be bigger than the frame,
     *        or lie outside of it, in which case no line is found. Its edges can be infinite.
     * @param indexes The array that receives the indexes of the lines, each of them once.
     * @return The number of lines that intersect the rectangle, which can be greater than the size
     *         of `indexes`.
     *
     * @throws IllegalArgumentException if `bounds` is not a valid rectangle, which means that an
     *         edge is not a number or its right is less than its left, or its bottom is less than
     *         its top.
     */
    fun findLines(bounds: RectF, indexes: IntArray): Int {
        require(!bounds.left.isNaN() && !bounds.top.isNaN()
            && !bounds.right.isNaN() && !bounds.bottom.isNaN()
            && bounds.right >= bounds.left && bounds.bottom >= bounds.top) {
            "Bad Bounds: $bounds"
        }

        return nFindLines(nativeLocator, bounds.left, bounds.top, bounds.right, bounds.bottom, indexes)
    }

    /**
     * Returns the index of the first line whose box reaches below a vertical position, which means
     * that the bottom of its box is greater than the position.
     *
     * @param y The vertical position in the coordinates of the frame.
     * @return The index of the line, or `-1` if no box reaches below the position, or `y` is not a
     *         number.
     */
    fun getFirstLineIndexBelow(y: Float): Int {
        return nGetFirstLineIndexBelow(nativeLocator, y)
    }

    private external fun nGetLineCount(nativeLocator: Long): Int
    private external fun nGetLineBox(nativeLocator: Long, lineIndex: Int, outBox: RectF)
    private external fun nFindLines(
        nativeLocator: Long, left: Float, top: Float, right: Float, bottom: Float, indexes: IntArray
    ): Int
    private external fun nGetFirstLineIndexBelow(nativeLocator: Long, y: Float): Int

    private companion object {
        init {
            JniBridge.loadLibrary()
        }

        @JvmStatic external fun nCreate(nativeFrame: Long, nativeRenderer: Long): Long
        @JvmStatic external fun nDispose(nativeLocator: Long)
    }
}
