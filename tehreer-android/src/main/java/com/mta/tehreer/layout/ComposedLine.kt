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
import android.graphics.RectF
import androidx.annotation.Size
import com.mta.tehreer.graphics.Renderer
import com.mta.tehreer.graphics.Typeface
import com.mta.tehreer.internal.Description
import com.mta.tehreer.internal.JniBridge
import com.mta.tehreer.internal.util.FloatCollector
import java.util.NavigableMap

/**
 * Represents a line of text consisting of an array of `GlyphRun` objects in visual order.
 */
class ComposedLine internal constructor(
    internal val nativeLine: Long,
    typefaces: Map<Long, Typeface>,
    holders: NavigableMap<Int, ReplacementHolder>
) {
    private class Finalizable(private val nativeLine: Long) {
        @Suppress("unused")
        protected fun finalize() {
            nDispose(nativeLine)
        }
    }

    private val finalizable = Finalizable(nativeLine)

    /**
     * Returns an unmodifiable list that contains all the runs of this line.
     */
    val runs: List<GlyphRun> = List(nGetRunCount(nativeLine)) {
        GlyphRun(this, nGetRun(nativeLine, it), typefaces, holders)
    }

    /**
     * Returns the index to the first character of this line in source text.
     */
    val charStart: Int
        get() = nGetCharStart(nativeLine)

    /**
     * Returns the index after the last character of this line in source text.
     */
    val charEnd: Int
        get() = nGetCharEnd(nativeLine)

    val breakEnd: Int
        get() = charEnd

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
        get() = ascent + descent + leading

    internal val top: Float
        get() = originY - ascent

    /**
     * Returns the width of the whitespaces at the end of this line, which are not shown.
     */
    val trailingWhitespaceExtent: Float
        get() = nGetTrailingWhitespaceExtent(nativeLine)

    private fun checkCharIndex(charIndex: Int) {
        val charStart = charStart
        val charEnd = charEnd

        require(charIndex in charStart..charEnd) {
            "Char Index: $charIndex, Line Range: [$charStart, $charEnd)"
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
    fun computeCharDistance(charIndex: Int): Float {
        checkCharIndex(charIndex)

        return nGetDistance(nativeLine, charIndex)
    }

    /**
     * Returns an array of visual edges corresponding to the specified character range.
     *
     * The resulting array will contain pairs of leading and trailing edges sorted from left to
     * right. There will be a separate pair for each glyph run occurred in the specified character
     * range. Each edge will be positioned relative to the start of the line assumed at zero.
     *
     * @param charStart The index to the first logical character in source text.
     * @param charEnd The index after the last logical character in source text.
     * @return An array of visual edges corresponding to the specified character range.
     *
     * @throws IllegalArgumentException if `charStart` is less than line start, or `charEnd` is
     *         greater than line end, or `charStart` is greater than `charEnd`.
     */
    @Size(multiple = 2)
    fun computeVisualEdges(charStart: Int, charEnd: Int): FloatArray {
        val lineStart = this.charStart
        val lineEnd = this.charEnd

        require(charStart >= lineStart) { "Char Start: $charStart, Line Range: [$lineStart, $lineEnd)" }
        require(charEnd <= lineEnd) { "Char End: $charEnd, Line Range: [$lineStart, $lineEnd)" }
        require(charEnd >= charStart) { "Bad Range: [$charStart, $charEnd)" }

        val edges = FloatCollector()
        nEnumerateEdges(nativeLine, charStart, charEnd, edges)

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
    fun computeNearestCharIndex(distance: Float): Int {
        return nGetIndexOfCodeUnit(nativeLine, distance)
    }

    /**
     * Calculates the bounding box of this line. The bounding box is a rectangle that encloses the
     * paths of this line's glyphs, as tightly as possible.
     *
     * @param renderer The renderer to use for calculating the bounding box. This is required
     *                 because the renderer could have settings in it that would cause changes in
     *                 the bounding box.
     * @return A rectangle that tightly encloses the paths of this line's glyphs.
     */
    fun computeBoundingBox(renderer: Renderer): RectF {
        val box = nGetBoundingBox(nativeLine, renderer.nativeHandle)
        renderer.syncNative()

        return box
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
     * @param x The x- position at which to draw this line.
     * @param y The y- position at which to draw this line.
     */
    fun draw(renderer: Renderer, canvas: Canvas, x: Float, y: Float) {
        for (glyphRun in runs) {
            val translateX = x + glyphRun.originX
            val translateY = y + glyphRun.originY

            canvas.translate(translateX, translateY)
            glyphRun.draw(renderer, canvas)
            canvas.translate(-translateX, -translateY)
        }
    }

    override fun toString(): String {
        return "ComposedLine{charStart=$charStart" +
            ", charEnd=$charEnd" +
            ", originX=$originX" +
            ", originY=$originY" +
            ", ascent=$ascent" +
            ", descent=$descent" +
            ", leading=$leading" +
            ", width=$width" +
            ", height=$height" +
            ", trailingWhitespaceExtent=$trailingWhitespaceExtent" +
            ", runs=${Description.forIterable(runs)}" +
            "}"
    }

    private companion object {
        init {
            JniBridge.loadLibrary()
        }

        @JvmStatic external fun nDispose(nativeLine: Long)
        @JvmStatic external fun nGetCharStart(nativeLine: Long): Int
        @JvmStatic external fun nGetCharEnd(nativeLine: Long): Int
        @JvmStatic external fun nGetParagraphLevel(nativeLine: Long): Int
        @JvmStatic external fun nIsBlock(nativeLine: Long): Boolean
        @JvmStatic external fun nGetAscent(nativeLine: Long): Float
        @JvmStatic external fun nGetDescent(nativeLine: Long): Float
        @JvmStatic external fun nGetLeading(nativeLine: Long): Float
        @JvmStatic external fun nGetWidth(nativeLine: Long): Float
        @JvmStatic external fun nGetTrailingWhitespaceExtent(nativeLine: Long): Float
        @JvmStatic external fun nGetOriginX(nativeLine: Long): Float
        @JvmStatic external fun nGetOriginY(nativeLine: Long): Float
        @JvmStatic external fun nGetRunCount(nativeLine: Long): Int
        @JvmStatic external fun nGetRun(nativeLine: Long, index: Int): Long
        @JvmStatic external fun nGetDistance(nativeLine: Long, charIndex: Int): Float
        @JvmStatic external fun nEnumerateEdges(
            nativeLine: Long, charStart: Int, charEnd: Int, collector: FloatCollector
        )
        @JvmStatic external fun nGetIndexOfCodeUnit(nativeLine: Long, distance: Float): Int
        @JvmStatic external fun nGetPenOffset(
            nativeLine: Long, flushFactor: Float, flushExtent: Float
        ): Float
        @JvmStatic external fun nGetBoundingBox(nativeLine: Long, nativeRenderer: Long): RectF
    }
}
