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
    ): Float {
        return textRun.getCaretEdge(if (!textRun.isBackward) fromIndex else toIndex)
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

        return if (cluster.glyphStart < cluster.glyphEnd) {
            cluster
        } else {
            null
        }
    }

    private fun drawEdgeCluster(renderer: Renderer, canvas: Canvas, cluster: ClusterRange) {
        val runStart = textRun.startIndex
        val runEnd = textRun.endIndex

        val startClipped = (cluster.actualStart < runStart)
        val endClipped = (cluster.actualEnd > runEnd)

        val clipLeft: Float
        val clipRight: Float

        if (!isRTL) {
            clipLeft = if (startClipped) textRun.getCaretEdge(runStart) else -Float.MAX_VALUE
            clipRight = if (endClipped) textRun.getCaretEdge(runEnd) else Float.MAX_VALUE
        } else {
            clipRight = if (startClipped) textRun.getCaretEdge(runStart) else Float.MAX_VALUE
            clipLeft = if (endClipped) textRun.getCaretEdge(runEnd) else -Float.MAX_VALUE
        }

        canvas.save()
        canvas.clipRect(clipLeft, -Float.MAX_VALUE, clipRight, Float.MAX_VALUE)
        canvas.translate(getLeadingEdge(cluster.actualStart, cluster.actualEnd), 0.0f)

        renderer.drawGlyphs(
            canvas,
            textRun.glyphIds.subList(cluster.glyphStart, cluster.glyphEnd),
            textRun.glyphOffsets.subList(cluster.glyphStart, cluster.glyphEnd),
            textRun.glyphAdvances.subList(cluster.glyphStart, cluster.glyphEnd)
        )

        canvas.restore()
    }

    override fun draw(renderer: Renderer, canvas: Canvas) {
        renderer.typeface = textRun.typeface
        renderer.typeSize = textRun.typeSize
        renderer.scaleX = 1.0f
        renderer.writingDirection = textRun.writingDirection

        val defaultFillColor = renderer.fillColor

        for (span in textRun.spans) {
            if (span is ForegroundColorSpan) {
                renderer.fillColor = span.foregroundColor
            } else if (span is ScaleXSpan) {
                renderer.scaleX = span.scaleX
            }
        }

        val firstIndex = textRun.startIndex
        val lastIndex = textRun.endIndex - 1

        var firstCluster: ClusterRange? = null
        var lastCluster: ClusterRange? = null

        if (textRun.startExtraLength > 0) {
            firstCluster = getClusterRange(firstIndex, null)
        }
        if (textRun.endExtraLength > 0) {
            lastCluster = getClusterRange(lastIndex, firstCluster)
        }

        val isBackward = textRun.isBackward

        var glyphStart = 0
        var glyphEnd = textRun.glyphCount

        var chunkStart = firstIndex
        var chunkEnd = lastIndex + 1

        if (firstCluster != null) {
            drawEdgeCluster(renderer, canvas, firstCluster)

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
        canvas.translate(getLeadingEdge(chunkStart, chunkEnd), 0.0f)

        renderer.drawGlyphs(
            canvas,
            textRun.glyphIds.subList(glyphStart, glyphEnd),
            textRun.glyphOffsets.subList(glyphStart, glyphEnd),
            textRun.glyphAdvances.subList(glyphStart, glyphEnd)
        )

        canvas.restore()

        lastCluster?.let {
            drawEdgeCluster(renderer, canvas, it)
        }

        renderer.fillColor = defaultFillColor
    }
}
