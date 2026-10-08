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
import android.graphics.Path
import android.text.Layout
import android.text.Spanned
import android.text.style.LeadingMarginSpan
import android.text.style.LeadingMarginSpan.LeadingMarginSpan2
import android.text.style.LineBackgroundSpan
import android.text.style.ParagraphStyle
import com.mta.tehreer.graphics.Renderer
import com.mta.tehreer.graphics.Typeface
import com.mta.tehreer.internal.Description
import com.mta.tehreer.internal.JniBridge
import com.mta.tehreer.internal.layout.Paragraphs
import com.mta.tehreer.internal.util.FloatCollector
import java.util.NavigableMap

/**
 * Represents a frame containing multiple lines of text. The frame object is the output resulting
 * from text-framing process performed by a typesetter object.
 */
class ComposedFrame internal constructor(
    private val nativeFrame: Long,
    originX: Float,
    originY: Float,
    private val source: Spanned,
    typefaces: Map<Long, Typeface>,
    holders: NavigableMap<Int, ReplacementHolder>
) {
    private class Finalizable(private val nativeFrame: Long) {
        @Suppress("unused")
        protected fun finalize() {
            nDispose(nativeFrame)
        }
    }

    private val finalizable = Finalizable(nativeFrame)

    private val paint by lazy(LazyThreadSafetyMode.NONE) { Paint() }
    private var lineSpans: Array<Array<ParagraphStyle>>? = null
    private var firstLines: BooleanArray? = null

    /**
     * Returns the x- origin of this frame.
     */
    val originX = originX

    /**
     * Returns the y- origin of this frame.
     */
    val originY = originY

    /**
     * Returns an unmodifiable list that contains all the lines of this frame.
     */
    val lines: List<ComposedLine> = List(nGetLineCount(nativeFrame)) {
        ComposedLine(nGetLine(nativeFrame, it), typefaces, holders)
    }

    /**
     * Returns the index to the first character of this frame in source text.
     */
    val charStart: Int
        get() = nGetCharStart(nativeFrame)

    /**
     * Returns the index after the last character of this frame in source text.
     */
    val charEnd: Int
        get() = nGetCharEnd(nativeFrame)

    /**
     * Returns the width of this frame.
     */
    val width: Float
        get() = nGetWidth(nativeFrame)

    /**
     * Returns the height of this frame.
     */
    val height: Float
        get() = nGetHeight(nativeFrame)

    /**
     * Returns the index of line containing the specified character.
     *
     * @param charIndex The index of character for which to return the line index.
     * @return The index of line containing the specified character.
     *
     * @throws IllegalArgumentException if `charIndex` is less than frame start or greater than
     *         frame end.
     */
    fun getLineIndexForChar(charIndex: Int): Int {
        val charStart = charStart
        val charEnd = charEnd

        require(charIndex in charStart..charEnd) {
            "Char Index: $charIndex, Frame Range: [$charStart..$charEnd)"
        }

        val lineIndex = nGetLineIndexForCodeUnit(nativeFrame, charIndex)

        // The end of the frame is not in any line, and belongs to the last one.
        return if (lineIndex >= 0) lineIndex else lines.size - 1
    }

    /**
     * Returns the index of a suitable line representing the specified position.
     *
     * @param x The x- coordinate of position.
     * @param y The y- coordinate of position.
     * @return The index of a suitable line representing the specified position.
     */
    fun getLineIndexForPosition(x: Float, y: Float): Int {
        return nGetLineIndexAtPosition(nativeFrame, x, y)
    }

    /**
     * Generates a path that contains a set of rectangles covering the specified selection range.
     *
     * @param charStart The index to the first character of selection in source text.
     * @param charEnd The index after the first character of selection in source text.
     * @return A path that contains a set of rectangles covering the specified selection range.
     *
     * @throws IllegalArgumentException if `charStart` is less than frame start, or `charEnd` is
     *         greater than frame end, or `charStart` is greater than `charEnd`.
     */
    fun generateSelectionPath(charStart: Int, charEnd: Int): Path {
        val frameStart = this.charStart
        val frameEnd = this.charEnd

        require(charStart >= frameStart) { "Char Start: $charStart, Frame Range: [$frameStart, $frameEnd)" }
        require(charEnd <= frameEnd) { "Char End: $charEnd, Frame Range: [$frameStart, $frameEnd)" }
        require(charEnd >= charStart) { "Bad Range: [$charStart, $charEnd)" }

        val selectionPath = Path()

        // The lines of a truncated frame do not cover the text that the token replaces, and Core
        // selects what the lines cover.
        val selectionStart = lines.firstOrNull { it.charEnd > charStart }
            ?.let { maxOf(charStart, it.charStart) } ?: charStart
        val selectionEnd = lines.lastOrNull { it.charStart < charEnd }
            ?.let { minOf(charEnd, it.charEnd) } ?: charEnd

        if (selectionEnd > selectionStart) {
            val rects = FloatCollector()
            nEnumerateSelection(nativeFrame, selectionStart, selectionEnd, rects)

            val frameWidth = width
            for (i in 0 until rects.size step 4) {
                // A selection never goes beyond the frame, even if a line does with its spacing.
                val left = maxOf(rects[i], 0.0f)
                val right = minOf(rects[i + 2], frameWidth)

                if (left < right) {
                    selectionPath.addRect(left, rects[i + 1], right, rects[i + 3], Path.Direction.CW)
                }
            }
        }

        return selectionPath
    }

    /**
     * Finds the paragraph styles that apply to each line, and whether the line is among the first
     * lines of its paragraph, which use the first leading margin.
     */
    private fun loadLineStyles() {
        if (lineSpans != null) {
            return
        }

        val frameStart = charStart
        val frameEnd = charEnd
        val bounds = Paragraphs.boundsOf(source)
        val firstLines = BooleanArray(lines.size)

        var paragraphIndex = -1
        var paragraphSpans = emptyArray<ParagraphStyle>()
        var firstLineCount = 1
        var lineInParagraph = 0

        lineSpans = Array(lines.size) { i ->
            val index = Paragraphs.indexOf(bounds, lines[i].charStart)

            if (index != paragraphIndex) {
                val start = maxOf(bounds[index * 2], frameStart)
                val end = minOf(bounds[index * 2 + 1], frameEnd)

                paragraphIndex = index
                paragraphSpans = source.getSpans(start, end, ParagraphStyle::class.java)
                firstLineCount = paragraphSpans
                    .filterIsInstance<LeadingMarginSpan2>()
                    .fold(1) { count, span -> maxOf(count, span.leadingMarginLineCount) }
                lineInParagraph = 0
            }

            firstLines[i] = lineInParagraph++ < firstLineCount
            paragraphSpans
        }
        this.firstLines = firstLines
    }

    private fun drawBackground(canvas: Canvas, lineSpans: Array<Array<ParagraphStyle>>) {
        val frameLeft = 0
        val frameRight = (width + 0.5f).toInt()

        for ((i, composedLine) in lines.withIndex()) {
            for (style in lineSpans[i]) {
                if (style is LineBackgroundSpan) {
                    val lineStart = composedLine.charStart
                    val lineEnd = composedLine.charEnd

                    if (lineStart >= source.getSpanEnd(style) || lineEnd <= source.getSpanStart(style)) {
                        continue
                    }

                    val lineTop = (composedLine.top + 0.5f).toInt()
                    val lineBaseline = (composedLine.originY + 0.5f).toInt()
                    val lineBottom = (composedLine.top + composedLine.height + 0.5f).toInt()

                    style.drawBackground(
                        canvas, paint, frameLeft, frameRight, lineTop, lineBaseline, lineBottom,
                        source, lineStart, lineEnd, i
                    )
                }
            }
        }
    }

    /**
     * Draws this frame onto the given `canvas` using the given `renderer`.
     *
     * @param renderer The renderer to use for drawing this frame.
     * @param canvas The canvas onto which to draw this frame.
     * @param x The x- position at which to draw this frame.
     * @param y The y- position at which to draw this frame.
     */
    fun draw(renderer: Renderer, canvas: Canvas, x: Float, y: Float) {
        loadLineStyles()

        val lineSpans = lineSpans!!
        val firstLines = firstLines!!

        canvas.translate(x, y)

        drawBackground(canvas, lineSpans)

        for ((i, composedLine) in lines.withIndex()) {
            var lineLeft = 0
            var lineRight = (width + 0.5f).toInt()

            // Draw leading margins of this line.
            for (style in lineSpans[i]) {
                if (style is LeadingMarginSpan) {
                    val isLTR = (composedLine.paragraphLevel.toInt() and 1) == 0
                    val isFirst = firstLines[i]

                    val lineTop = (composedLine.top + 0.5f).toInt()
                    val lineBaseline = (composedLine.originY + 0.5f).toInt()
                    val lineBottom = (composedLine.top + composedLine.height + 0.5f).toInt()

                    style.drawLeadingMargin(
                        canvas, paint,
                        if (isLTR) lineLeft else lineRight,
                        if (isLTR) Layout.DIR_LEFT_TO_RIGHT else Layout.DIR_RIGHT_TO_LEFT,
                        lineTop, lineBaseline, lineBottom,
                        source, composedLine.charStart, composedLine.charEnd, isFirst, null
                    )

                    if (isLTR) {
                        lineLeft += style.getLeadingMargin(isFirst)
                    } else {
                        lineRight -= style.getLeadingMargin(isFirst)
                    }
                }
            }

            composedLine.draw(renderer, canvas, composedLine.originX, composedLine.originY)
        }

        canvas.translate(-x, -y)
    }

    override fun toString(): String {
        return "ComposedFrame{charStart=$charStart" +
            ", charEnd=$charEnd" +
            ", originX=$originX" +
            ", originY=$originY" +
            ", width=$width" +
            ", height=$height" +
            ", lines=${Description.forIterable(lines)}" +
            "}"
    }

    private companion object {
        init {
            JniBridge.loadLibrary()
        }

        @JvmStatic external fun nDispose(nativeFrame: Long)
        @JvmStatic external fun nGetCharStart(nativeFrame: Long): Int
        @JvmStatic external fun nGetCharEnd(nativeFrame: Long): Int
        @JvmStatic external fun nGetWidth(nativeFrame: Long): Float
        @JvmStatic external fun nGetHeight(nativeFrame: Long): Float
        @JvmStatic external fun nGetLineCount(nativeFrame: Long): Int
        @JvmStatic external fun nGetLine(nativeFrame: Long, index: Int): Long
        @JvmStatic external fun nGetLineIndexForCodeUnit(nativeFrame: Long, charIndex: Int): Int
        @JvmStatic external fun nGetLineIndexAtPosition(nativeFrame: Long, x: Float, y: Float): Int
        @JvmStatic external fun nEnumerateSelection(
            nativeFrame: Long, charStart: Int, charEnd: Int, collector: FloatCollector
        )
    }
}
