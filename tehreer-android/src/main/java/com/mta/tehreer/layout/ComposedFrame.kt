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

/**
 * Represents a frame containing multiple lines of text. The frame object is the output resulting
 * from text-framing process performed by a typesetter object.
 */
class ComposedFrame internal constructor(
    internal val nativeFrame: Long,
    private val source: Spanned,
    typefaces: Map<Long, Typeface>,
    holders: Map<Long, ReplacementHolder>
) {
    private class Finalizable(private val nativeFrame: Long) {
        @Suppress("unused")
        protected fun finalize() {
            nDispose(nativeFrame)
        }
    }

    private val finalizable = Finalizable(nativeFrame)

    private val replacementDrawer = ReplacementDrawer(holders)

    private val paint by lazy(LazyThreadSafetyMode.NONE) { Paint() }
    private var lineSpans: Array<Array<ParagraphStyle>>? = null
    private var firstLines: BooleanArray? = null

    /**
     * Returns an unmodifiable list that contains all the lines of this frame.
     */
    val lines: List<ComposedLine> = List(nGetLineCount(nativeFrame)) {
        ComposedLine(nGetLine(nativeFrame, it), typefaces, holders)
    }

    /**
     * Returns the index to the first character of this frame in source text.
     */
    val startIndex: Int
        get() = nGetStartIndex(nativeFrame)

    /**
     * Returns the index after the last character of this frame in source text.
     */
    val endIndex: Int
        get() = nGetEndIndex(nativeFrame)

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
     * Returns the index of line containing the specified character. The end index of the frame,
     * which is the insertion point after its last character, belongs to the last line. The result
     * is `-1` if the frame has no lines.
     *
     * @param charIndex The index of character for which to return the line index.
     * @return The index of line containing the specified character.
     *
     * @throws IllegalArgumentException if `charIndex` is less than frame start or greater than
     *         frame end.
     */
    fun getLineIndexForChar(charIndex: Int): Int {
        val startIndex = this.startIndex
        val endIndex = this.endIndex

        require(charIndex in startIndex..endIndex) {
            "Char Index: $charIndex, Frame Range: [$startIndex..$endIndex)"
        }

        return nGetLineIndexForCodeUnit(nativeFrame, charIndex)
    }

    /**
     * Returns the index of a suitable line representing the specified position.
     *
     * @param x The x- coordinate of position.
     * @param y The y- coordinate of position.
     * @return The index of a suitable line representing the specified position.
     */
    fun getLineIndexAtPosition(x: Float, y: Float): Int {
        return nGetLineIndexAtPosition(nativeFrame, x, y)
    }

    /**
     * Generates a path that contains a set of rectangles covering the specified selection range.
     *
     * @param fromIndex The index to the first character of selection in source text.
     * @param toIndex The index after the first character of selection in source text.
     * @return A path that contains a set of rectangles covering the specified selection range.
     *
     * @throws IllegalArgumentException if `fromIndex` is less than frame start, or `toIndex` is
     *         greater than frame end, or `fromIndex` is greater than `toIndex`.
     */
    fun generateSelectionPath(fromIndex: Int, toIndex: Int): Path {
        val frameStart = this.startIndex
        val frameEnd = this.endIndex

        require(fromIndex >= frameStart) { "From Index: $fromIndex, Frame Range: [$frameStart, $frameEnd)" }
        require(toIndex <= frameEnd) { "To Index: $toIndex, Frame Range: [$frameStart, $frameEnd)" }
        require(toIndex >= fromIndex) { "Bad Range: [$fromIndex, $toIndex)" }

        val selectionPath = Path()

        // The lines of a truncated frame do not cover the text that the token replaces, and Core
        // selects what the lines cover.
        val selectionStart = lines.firstOrNull { it.endIndex > fromIndex }
            ?.let { maxOf(fromIndex, it.startIndex) } ?: fromIndex
        val selectionEnd = lines.lastOrNull { it.startIndex < toIndex }
            ?.let { minOf(toIndex, it.endIndex) } ?: toIndex

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

        val frameStart = startIndex
        val frameEnd = endIndex
        val bounds = Paragraphs.boundsOf(source)
        val firstLines = BooleanArray(lines.size)

        var paragraphIndex = -1
        var paragraphSpans = emptyArray<ParagraphStyle>()
        var firstLineCount = 1
        var lineInParagraph = 0

        lineSpans = Array(lines.size) { i ->
            val index = Paragraphs.indexOf(bounds, lines[i].startIndex)

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
                    val lineStart = composedLine.startIndex
                    val lineEnd = composedLine.endIndex

                    if (lineStart >= source.getSpanEnd(style) || lineEnd <= source.getSpanStart(style)) {
                        continue
                    }

                    val lineTop = (composedLine.originY - composedLine.ascent + 0.5f).toInt()
                    val lineBaseline = (composedLine.originY + 0.5f).toInt()
                    val lineBottom = (composedLine.originY + composedLine.descent + composedLine.leading + 0.5f).toInt()

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

                    val lineTop = (composedLine.originY - composedLine.ascent + 0.5f).toInt()
                    val lineBaseline = (composedLine.originY + 0.5f).toInt()
                    val lineBottom = (composedLine.originY + composedLine.descent + composedLine.leading + 0.5f).toInt()

                    style.drawLeadingMargin(
                        canvas, paint,
                        if (isLTR) lineLeft else lineRight,
                        if (isLTR) Layout.DIR_LEFT_TO_RIGHT else Layout.DIR_RIGHT_TO_LEFT,
                        lineTop, lineBaseline, lineBottom,
                        source, composedLine.startIndex, composedLine.endIndex, isFirst, null
                    )

                    if (isLTR) {
                        lineLeft += style.getLeadingMargin(isFirst)
                    } else {
                        lineRight -= style.getLeadingMargin(isFirst)
                    }
                }
            }

        }

        canvas.translate(-x, -y)

        nDraw(
            nativeFrame, renderer.nativeHandle, canvas, renderer.preparePaint(canvas),
            replacementDrawer, x, y
        )
    }

    override fun toString(): String {
        return "ComposedFrame{startIndex=$startIndex" +
            ", endIndex=$endIndex" +
            ", width=$width" +
            ", height=$height" +
            ", lines=${Description.forIterable(lines)}" +
            "}"
    }

    private external fun nDraw(
        nativeFrame: Long, nativeRenderer: Long, canvas: Canvas, paint: Paint, drawer: Any,
        x: Float, y: Float
    )
    private external fun nGetStartIndex(nativeFrame: Long): Int
    private external fun nGetEndIndex(nativeFrame: Long): Int
    private external fun nGetWidth(nativeFrame: Long): Float
    private external fun nGetHeight(nativeFrame: Long): Float
    private external fun nGetLineCount(nativeFrame: Long): Int
    private external fun nGetLine(nativeFrame: Long, index: Int): Long
    private external fun nGetLineIndexForCodeUnit(nativeFrame: Long, charIndex: Int): Int
    private external fun nGetLineIndexAtPosition(nativeFrame: Long, x: Float, y: Float): Int
    private external fun nEnumerateSelection(
        nativeFrame: Long, fromIndex: Int, toIndex: Int, collector: FloatCollector
    )
    private companion object {
        init {
            JniBridge.loadLibrary()
        }

        @JvmStatic external fun nDispose(nativeFrame: Long)
    }
}
