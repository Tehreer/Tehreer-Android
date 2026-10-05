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

package com.mta.tehreer.widget

import android.graphics.RectF
import android.text.Spanned
import android.text.style.ReplacementSpan
import com.mta.tehreer.layout.ComposedFrame
import com.mta.tehreer.layout.ComposedLine
import com.mta.tehreer.layout.GlyphRun
import com.mta.tehreer.layout.style.ViewSpan
import kotlin.math.max
import kotlin.math.min

/**
 * Works out where a span is inside a composed frame, in the frame's own coordinates.
 *
 * A replacement span (an inline image, say) gets the box of the run that stands for it. Any other
 * span gets one rect per line it covers, following the convention of a text selection: a span that
 * wraps continues to the edge of the frame on the line where it breaks, and starts at the edge of
 * the frame on the line where it resumes. Runs are stored in visual order, so nothing here depends
 * on the writing direction.
 *
 * Returns an empty list if the span is not in [source], or is not (even partly) in the frame.
 */
internal fun computeSpanRects(frame: ComposedFrame, source: Spanned, span: Any): List<RectF> {
    val spanStart = source.getSpanStart(span)
    val spanEnd = source.getSpanEnd(span)
    if (spanStart < 0 || spanEnd < 0) {
        return emptyList()
    }

    if (span !is ReplacementSpan) {
        return computeSelectionRects(frame, spanStart, spanEnd)
    }

    val charStart = max(spanStart, frame.charStart)
    val charEnd = min(spanEnd, frame.charEnd)
    if (charStart >= charEnd) {
        return emptyList()
    }

    val firstIndex = frame.getLineIndexForChar(charStart)
    val lastIndex = frame.getLineIndexForChar(charEnd - 1)
    if (firstIndex < 0 || lastIndex < firstIndex) {
        return emptyList()
    }

    val rects = mutableListOf<RectF>()

    for (index in firstIndex..lastIndex) {
        val line = frame.lines[index]
        val baseline = frame.originY + line.originY

        for (run in line.runs) {
            if (run.charStart < charEnd && run.charEnd > charStart) {
                if (span is ViewSpan) {
                    rects.add(computeViewRect(frame, line, run, span))
                    continue
                }

                val left = frame.originX + line.originX + run.originX
                val top = baseline + run.originY - run.ascent
                val bottom = baseline + run.originY + run.descent

                rects.add(RectF(left, top, left + run.width, bottom))
            }
        }
    }

    return rects
}

/**
 * Works out the rects that the chars from [rangeStart] up to [rangeEnd] cover in [frame], in the
 * frame's own coordinates: one for each line, following the convention of a text selection.
 *
 * Returns an empty list if the range is not (even partly) in the frame.
 */
internal fun computeSelectionRects(frame: ComposedFrame, rangeStart: Int, rangeEnd: Int): List<RectF> {
    val charStart = max(rangeStart, frame.charStart)
    val charEnd = min(rangeEnd, frame.charEnd)
    if (charStart >= charEnd) {
        return emptyList()
    }

    val lines = frame.lines
    val firstIndex = frame.getLineIndexForChar(charStart)
    val lastIndex = frame.getLineIndexForChar(charEnd - 1)
    if (firstIndex < 0 || lastIndex < firstIndex) {
        return emptyList()
    }

    val frameWidth = frame.width
    val rects = mutableListOf<RectF>()

    for (index in firstIndex..lastIndex) {
        val line = lines[index]
        val baseline = frame.originY + line.originY
        val top = baseline - line.ascent
        val bottom = baseline + line.descent + line.leading
        val lineLeft = line.originX
        val lineRight = lineLeft + line.width
        val isRTL = (line.paragraphLevel.toInt() and 1) == 1

        var left = Float.POSITIVE_INFINITY
        var right = Float.NEGATIVE_INFINITY

        fun include(from: Float, to: Float) {
            left = min(left, max(from, 0.0f))
            right = max(right, min(to, frameWidth))
        }

        val segmentStart = max(charStart, line.charStart)
        val segmentEnd = min(charEnd, line.charEnd)

        if (segmentStart < segmentEnd) {
            val edges = line.computeVisualEdges(segmentStart, segmentEnd)
            for (i in edges.indices step 2) {
                include(edges[i] + lineLeft, edges[i + 1] + lineLeft)
            }
        }

        if (firstIndex != lastIndex) {
            when (index) {
                // The padding that follows the text of the first line...
                firstIndex -> if (isRTL) include(0.0f, lineLeft) else include(lineRight, frameWidth)
                // ...and the padding that leads to the text of the last one.
                lastIndex -> if (isRTL) include(lineRight, frameWidth) else include(0.0f, lineLeft)
                // Every line in between is covered entirely.
                else -> include(0.0f, frameWidth)
            }
        }

        if (left < right) {
            rects.add(RectF(frame.originX + left, top, frame.originX + right, bottom))
        }
    }

    return rects
}

/**
 * The rect of the view of [span], in the coordinates of the container: the box of its [run], which
 * is on the [line], without the margins of a block, and as wide as the frame.
 */
internal fun computeViewRect(frame: ComposedFrame, line: ComposedLine, run: GlyphRun, span: ViewSpan): RectF {
    val baseline = frame.originY + line.originY + run.originY
    val top = baseline - run.ascent
    val bottom = baseline + run.descent

    if (span.isBlock) {
        val margins = span.margins

        return RectF(
            frame.originX, top + margins.top.coerceAtLeast(0),
            frame.originX + frame.width, bottom - margins.bottom.coerceAtLeast(0)
        )
    }

    val left = frame.originX + line.originX + run.originX
    return RectF(left, top, left + run.width, bottom)
}
