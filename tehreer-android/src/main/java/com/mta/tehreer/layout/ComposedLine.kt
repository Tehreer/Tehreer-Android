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

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.annotation.Size
import com.mta.tehreer.graphics.Renderer
import com.mta.tehreer.graphics.Typeface
import com.mta.tehreer.internal.Description
import com.mta.tehreer.internal.JniBridge
import com.mta.tehreer.internal.util.FloatCollector

/**
 * Represents a line of text consisting of an array of `GlyphRun` objects in visual order.
 */
class ComposedLine internal constructor(
    internal val nativeLine: Long,
    typefaces: Map<Long, Typeface>,
    holders: Map<Long, ReplacementHolder>
) {
    private class Finalizable(private val nativeLine: Long) {
        @Suppress("unused")
        protected fun finalize() {
            nDispose(nativeLine)
        }
    }

    private val finalizable = Finalizable(nativeLine)

    private val replacementDrawer = ReplacementDrawer(holders)

    /**
     * Returns an unmodifiable list that contains all the runs of this line.
     */
    val runs: List<GlyphRun> = List(nGetRunCount(nativeLine)) {
        GlyphRun(this, nGetRun(nativeLine, it), typefaces, holders)
    }

    /**
     * Returns the index to the first character of this line in source text.
     */
    val startIndex: Int
        get() = nGetStartIndex(nativeLine)

    /**
     * Returns the index after the last character of this line in source text.
     */
    val endIndex: Int
        get() = nGetEndIndex(nativeLine)

    /** Whether this line is the line of a view that has a line of its own. */
    internal val isBlock: Boolean
        get() = nIsBlock(nativeLine)

    /**
     * Returns the paragraph level of this line.
     */
    val paragraphLevel: Byte
        get() = nGetParagraphLevel(nativeLine).toByte()

    /**
     * Returns the x- origin of this line in parent frame.
     */
    val originX: Float
        get() = nGetOriginX(nativeLine)

    /**
     * Returns the y- origin of this line in parent frame.
     */
    val originY: Float
        get() = nGetOriginY(nativeLine)

    /**
     * Returns the ascent of this line which is the maximum ascent from the baseline of all runs.
     */
    val ascent: Float
        get() = nGetAscent(nativeLine)

    /**
     * Returns the descent of this line which is the maximum descent from the baseline of all runs.
     */
    val descent: Float
        get() = nGetDescent(nativeLine)

    /**
     * Returns the leading of this line which is the maximum leading of all runs.
     */
    val leading: Float
        get() = nGetLeading(nativeLine)

    /**
     * Returns the typographic width of this line.
     */
    val width: Float
        get() = nGetWidth(nativeLine)

    /**
     * Returns the typographic height of this line.
     */
    val height: Float
        get() = nGetHeight(nativeLine)

    /**
     * Whether this line shows a truncation token in place of some of its text. Such a line covers
     * the text that was cut out too.
     */
    val isTruncated: Boolean
        get() = nIsTruncated(nativeLine)

    /**
     * Returns the width of the whitespaces at the end of this line, which are not shown.
     */
    val trailingWhitespaceExtent: Float
        get() = nGetTrailingWhitespaceExtent(nativeLine)

    private fun checkCharIndex(charIndex: Int) {
        val startIndex = this.startIndex
        val endIndex = this.endIndex

        require(charIndex in startIndex..endIndex) {
            "Char Index: $charIndex, Line Range: [$startIndex, $endIndex)"
        }
    }

    /**
     * Determines the distance of specified character from the start of the line assumed at zero.
     *
     * @param charIndex The index of character in source text.
     * @return The distance of specified character from the start of the line assumed at zero.
     *
     * @throws IllegalArgumentException if `charIndex` is less than line start or greater than line
     *         end.
     */
    fun getCharDistance(charIndex: Int): Float {
        checkCharIndex(charIndex)

        return nGetCharDistance(nativeLine, charIndex)
    }

    /**
     * Returns an array of visual edges corresponding to the specified character range.
     *
     * The resulting array will contain pairs of leading and trailing edges sorted from left to
     * right. There will be a separate pair for each glyph run occurred in the specified character
     * range. Each edge will be positioned relative to the start of the line assumed at zero.
     *
     * @param fromIndex The index to the first logical character in source text.
     * @param toIndex The index after the last logical character in source text.
     * @return An array of visual edges corresponding to the specified character range.
     *
     * @throws IllegalArgumentException if `fromIndex` is less than line start, or `toIndex` is
     *         greater than line end, or `fromIndex` is greater than `toIndex`.
     */
    @Size(multiple = 2)
    fun getVisualEdges(fromIndex: Int, toIndex: Int): FloatArray {
        val lineStart = this.startIndex
        val lineEnd = this.endIndex

        require(fromIndex >= lineStart) { "From Index: $fromIndex, Line Range: [$lineStart, $lineEnd)" }
        require(toIndex <= lineEnd) { "To Index: $toIndex, Line Range: [$lineStart, $lineEnd)" }
        require(toIndex >= fromIndex) { "Bad Range: [$fromIndex, $toIndex)" }

        val edges = FloatCollector()
        nEnumerateVisualEdges(nativeLine, fromIndex, toIndex, edges)

        return edges.toArray()
    }

    /**
     * Returns the index of character nearest to the specified distance.
     *
     * @param distance The distance for which to determine the character index. It should be offset
     *                 from zero origin.
     * @return The index of character nearest to the specified distance. It will be an absolute
     *         index in source string.
     */
    fun getCharIndexAtDistance(distance: Float): Int {
        return nGetCharIndexAtDistance(nativeLine, distance)
    }

    /**
     * Calculates the ink box of this line. The ink box is a rectangle that encloses the
     * paths of this line's glyphs, as tightly as possible.
     *
     * @param renderer The renderer to use for calculating the ink box. This is required
     *                 because the renderer could have settings in it that would cause changes in
     *                 the ink box.
     * @return A rectangle that tightly encloses the paths of this line's glyphs.
     */
    fun computeInkBox(renderer: Renderer): RectF {
        return nComputeInkBox(nativeLine, renderer.nativeHandle)
    }

    /**
     * Returns the offset that a pen has to move to flush the line in a given extent.
     *
     * @param flushFactor The factor that decides how much of the extra extent is left before the
     *                    line; zero for the start and one for the end.
     * @param flushExtent The extent in which the line is flushed.
     */
    fun getFlushPenOffset(flushFactor: Float, flushExtent: Float): Float {
        return nGetPenOffset(nativeLine, flushFactor, flushExtent)
    }

    /**
     * Draws this line onto the given `canvas` using the given `renderer`.
     *
     * @param renderer The renderer to use for drawing this line.
     * @param canvas The canvas onto which to draw this line.
     * @param x The x- position of the origin of the container of this line. The origin of the line
     *          is added to it.
     * @param y The y- position of the origin of the container of this line. The origin of the line
     *          is added to it.
     */
    fun draw(renderer: Renderer, canvas: Canvas, x: Float, y: Float) {
        nDraw(
            nativeLine, renderer.nativeHandle, canvas, renderer.preparePaint(canvas),
            replacementDrawer, x, y
        )
    }

    override fun toString(): String {
        return "ComposedLine{startIndex=$startIndex" +
            ", endIndex=$endIndex" +
            ", originX=$originX" +
            ", originY=$originY" +
            ", ascent=$ascent" +
            ", descent=$descent" +
            ", leading=$leading" +
            ", width=$width" +
            ", height=$height" +
            ", isTruncated=$isTruncated" +
            ", trailingWhitespaceExtent=$trailingWhitespaceExtent" +
            ", runs=${Description.forIterable(runs)}" +
            "}"
    }

    private external fun nDraw(
        nativeLine: Long, nativeRenderer: Long, canvas: Canvas, paint: Paint, drawer: Any,
        x: Float, y: Float
    )
    private external fun nGetStartIndex(nativeLine: Long): Int
    private external fun nGetEndIndex(nativeLine: Long): Int
    private external fun nGetParagraphLevel(nativeLine: Long): Int
    private external fun nIsBlock(nativeLine: Long): Boolean
    private external fun nGetAscent(nativeLine: Long): Float
    private external fun nGetDescent(nativeLine: Long): Float
    private external fun nGetLeading(nativeLine: Long): Float
    private external fun nGetWidth(nativeLine: Long): Float
    private external fun nGetHeight(nativeLine: Long): Float
    private external fun nIsTruncated(nativeLine: Long): Boolean
    private external fun nGetTrailingWhitespaceExtent(nativeLine: Long): Float
    private external fun nGetOriginX(nativeLine: Long): Float
    private external fun nGetOriginY(nativeLine: Long): Float
    private external fun nGetRunCount(nativeLine: Long): Int
    private external fun nGetRun(nativeLine: Long, index: Int): Long
    private external fun nGetCharDistance(nativeLine: Long, charIndex: Int): Float
    private external fun nEnumerateVisualEdges(
        nativeLine: Long, fromIndex: Int, toIndex: Int, collector: FloatCollector
    )
    private external fun nGetCharIndexAtDistance(nativeLine: Long, distance: Float): Int
    private external fun nGetPenOffset(
        nativeLine: Long, flushFactor: Float, flushExtent: Float
    ): Float
    private external fun nComputeInkBox(nativeLine: Long, nativeRenderer: Long): RectF
    private companion object {
        init {
            JniBridge.loadLibrary()
        }

        @JvmStatic external fun nDispose(nativeLine: Long)
    }
}
