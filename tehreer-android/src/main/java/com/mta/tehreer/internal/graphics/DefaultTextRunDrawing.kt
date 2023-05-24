/*
 * Copyright (C) 2023 Muhammad Tayyab Akram
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

package com.mta.tehreer.internal.graphics

import android.graphics.Canvas
import android.text.style.ForegroundColorSpan
import android.text.style.ScaleXSpan
import com.mta.tehreer.graphics.Renderer
import com.mta.tehreer.internal.layout.TextRun
import com.mta.tehreer.internal.util.isOdd
import kotlin.math.max
import kotlin.math.min

private class ClusterRange(
    var actualStart: Int = 0,
    var actualEnd: Int = 0,
    var glyphStart: Int = 0,
    var glyphEnd:Int = 0,
)

internal class DefaultTextRunDrawing(
    private val textRun: TextRun
) : TextRunDrawing {
    private val isRTL: Boolean
        get() = textRun.bidiLevel.isOdd()

    private fun getLeadingEdge(
        fromIndex: Int,
        toIndex: Int,
        isBackward: Boolean,
        caretBoundary: Float
    ): Float {
        return textRun.getCaretEdge(if (!isBackward) fromIndex else toIndex) - caretBoundary
    }

    private fun getClusterRange(charIndex: Int, exclusion: ClusterRange?): ClusterRange? {
        val actualStart = textRun.getClusterStart(charIndex)
        val actualEnd = textRun.getClusterEnd(charIndex)

        val leadingIndex = textRun.getLeadingGlyphIndex(charIndex)
        val trailingIndex = textRun.getTrailingGlyphIndex(charIndex)

        val cluster = ClusterRange(
            actualStart = actualStart,
            actualEnd = actualEnd,
            glyphStart = min(leadingIndex, trailingIndex),
            glyphEnd = max(leadingIndex, trailingIndex) + 1
        )

        if (exclusion != null) {
            val minStart = min(exclusion.glyphStart, cluster.glyphEnd)
            val maxEnd = max(cluster.glyphStart, exclusion.glyphEnd)
            val isBackward = textRun.isBackward

            cluster.glyphStart = if (!isBackward) maxEnd else cluster.glyphStart
            cluster.glyphEnd = if (isBackward) minStart else cluster.glyphEnd
        }

        return if (cluster.glyphStart < cluster.glyphEnd) cluster else null
    }

    private fun drawEdgeCluster(
        renderer: Renderer, canvas: Canvas,
        cluster: ClusterRange, caretBoundary: Float,
        fromIndex: Int, toIndex: Int
    ) {
        val startClipped = cluster.actualStart < fromIndex
        val endClipped = cluster.actualEnd > toIndex

        val clipLeft: Float
        val clipRight: Float

        val isBackward = textRun.isBackward

        if (!isRTL) {
            clipLeft =
                if (startClipped) textRun.getCaretEdge(fromIndex) - caretBoundary else -Float.MAX_VALUE
            clipRight =
                if (endClipped) textRun.getCaretEdge(toIndex) - caretBoundary else Float.MAX_VALUE
        } else {
            clipRight =
                if (startClipped) textRun.getCaretEdge(fromIndex) - caretBoundary else Float.MAX_VALUE
            clipLeft =
                if (endClipped) textRun.getCaretEdge(toIndex) - caretBoundary else -Float.MAX_VALUE
        }

        canvas.save()
        canvas.clipRect(clipLeft, -Float.MAX_VALUE, clipRight, Float.MAX_VALUE)
        canvas.translate(
            getLeadingEdge(cluster.actualStart, cluster.actualEnd, isBackward, caretBoundary),
            0.0f
        )

        renderer.drawGlyphs(
            canvas,
            textRun.glyphIds.subList(cluster.glyphStart, cluster.glyphEnd),
            textRun.glyphOffsets.subList(cluster.glyphStart, cluster.glyphEnd),
            textRun.glyphAdvances.subList(cluster.glyphStart, cluster.glyphEnd)
        )

        canvas.restore()
    }

    override fun draw(renderer: Renderer, canvas: Canvas) {
        renderer.scaleX = 1.0f

        val defaultFillColor = renderer.fillColor

        for (span in textRun.spans) {
            if (span is ForegroundColorSpan) {
                renderer.fillColor = span.foregroundColor
            } else if (span is ScaleXSpan) {
                renderer.scaleX = span.scaleX
            }
        }

        draw(renderer, canvas, textRun.startIndex, textRun.endIndex)

        renderer.fillColor = defaultFillColor
    }

    private fun draw(renderer: Renderer, canvas: Canvas, fromIndex: Int, toIndex: Int) {
        renderer.typeface = textRun.typeface
        renderer.typeSize = textRun.typeSize
        renderer.writingDirection = textRun.writingDirection

        val lastIndex = toIndex - 1
        val actualStart = textRun.getClusterStart(fromIndex)
        val actualEnd = textRun.getClusterEnd(lastIndex)

        var firstCluster: ClusterRange? = null
        var lastCluster: ClusterRange? = null

        if (actualStart < fromIndex) {
            firstCluster = getClusterRange(fromIndex, null)
        }
        if (actualEnd > toIndex) {
            lastCluster = getClusterRange(lastIndex, firstCluster)
        }

        val isBackward = textRun.isBackward
        val caretBoundary = textRun.getCaretBoundary(fromIndex, toIndex)
        val glyphRange = textRun.getGlyphRangeForChars(actualStart, actualEnd)

        var glyphStart = glyphRange.first
        var glyphEnd = glyphRange.last + 1

        var chunkStart = fromIndex
        var chunkEnd = toIndex

        if (firstCluster != null) {
            drawEdgeCluster(renderer, canvas, firstCluster, caretBoundary, fromIndex, toIndex)

            // Exclude first cluster characters.
            chunkStart = firstCluster.actualEnd
            // Exclude first cluster glyphs.
            glyphStart = if (!isBackward) firstCluster.glyphEnd else glyphStart
            glyphEnd = if (isBackward) firstCluster.glyphStart else glyphEnd
        }
        if (lastCluster != null) {
            // Exclude last cluster characters.
            chunkEnd = lastCluster.actualStart
            // Exclude last cluster glyphs.
            glyphEnd = if (!isBackward) lastCluster.glyphStart else glyphEnd
            glyphStart = if (isBackward) lastCluster.glyphEnd else glyphStart
        }

        canvas.save()
        canvas.translate(
            getLeadingEdge(chunkStart, chunkEnd, isBackward, caretBoundary),
            0.0f
        )

        renderer.drawGlyphs(
            canvas,
            textRun.glyphIds.subList(glyphStart, glyphEnd),
            textRun.glyphOffsets.subList(glyphStart, glyphEnd),
            textRun.glyphAdvances.subList(glyphStart, glyphEnd)
        )

        canvas.restore()

        lastCluster?.let {
            drawEdgeCluster(renderer, canvas, it, caretBoundary, fromIndex, toIndex)
        }
    }
}
