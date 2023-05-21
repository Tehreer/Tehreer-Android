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

package com.mta.tehreer.internal.layout

import android.graphics.Canvas
import com.mta.tehreer.sfnt.WritingDirection
import com.mta.tehreer.collections.IntList
import com.mta.tehreer.collections.PointList
import com.mta.tehreer.collections.FloatList
import android.graphics.RectF
import com.mta.tehreer.graphics.Renderer
import com.mta.tehreer.graphics.Typeface
import com.mta.tehreer.internal.graphics.DefaultTextRunDrawing
import com.mta.tehreer.internal.util.Preconditions

internal class IntrinsicRunSlice(
    private val intrinsicRun: IntrinsicRun,
    override val charStart: Int,
    override val charEnd: Int,
    override val spans: List<Any>
) : TextRun {
    private val glyphOffset: Int
    override val glyphCount: Int
    private val caretBoundary: Float

    init {
        val glyphRange = intrinsicRun.getGlyphRangeForChars(charStart, charEnd)
        glyphOffset = glyphRange.first
        glyphCount = glyphRange.last - glyphRange.first + 1
        caretBoundary = intrinsicRun.getCaretBoundary(charStart, charEnd)
    }

    override val isBackward: Boolean
        get() = intrinsicRun.isBackward

    override val bidiLevel: Byte
        get() = intrinsicRun.bidiLevel

    override val startExtraLength: Int
        get() = charStart - intrinsicRun.getClusterStart(charStart)

    override val endExtraLength: Int
        get() = intrinsicRun.getClusterEnd(charEnd - 1) - charEnd

    override val typeface: Typeface
        get() = intrinsicRun.typeface

    override val typeSize: Float
        get() = intrinsicRun.typeSize

    override val writingDirection: WritingDirection
        get() = intrinsicRun.writingDirection

    override val glyphIds: IntList
        get() = intrinsicRun.glyphIds.subList(glyphOffset, glyphOffset + glyphCount)

    override val glyphOffsets: PointList
        get() = intrinsicRun.glyphOffsets.subList(glyphOffset, glyphOffset + glyphCount)

    override val glyphAdvances: FloatList
        get() = intrinsicRun.glyphAdvances.subList(glyphOffset, glyphOffset + glyphCount)

    override val clusterMap: IntList
        get() {
            val actualStart = intrinsicRun.getClusterStart(charStart)
            val actualEnd = intrinsicRun.getClusterEnd(charEnd - 1)

            val offset = actualStart - intrinsicRun.charStart
            val size = actualEnd - actualStart

            return ClusterMap(intrinsicRun.clusterMapArray, offset, size, glyphOffset)
        }

    internal class CaretEdges(
        val parentEdges: FloatList,
        val offset: Int,
        val size: Int,
        val boundary: Float
    ) : FloatList() {
        override fun size(): Int {
            return size
        }

        override fun get(index: Int): Float {
            Preconditions.checkElementIndex(index, size)
            return parentEdges[index + offset] - boundary
        }

        override fun copyTo(array: FloatArray, atIndex: Int) {
            Preconditions.checkNotNull(array)

            for (i in 0 until size) {
                array[i + atIndex] = parentEdges[i + offset] - boundary
            }
        }

        override fun subList(fromIndex: Int, toIndex: Int): FloatList {
            Preconditions.checkIndexRange(fromIndex, toIndex, size)

            return CaretEdges(parentEdges, offset + fromIndex, toIndex - fromIndex, boundary)
        }
    }

    override val caretEdges: FloatList
        get() {
            val actualStart = intrinsicRun.getClusterStart(charStart)
            val actualEnd = intrinsicRun.getClusterEnd(charEnd - 1)

            val offset = actualStart - intrinsicRun.charStart
            val size = actualEnd - actualStart + 1

            return CaretEdges(intrinsicRun.caretEdges, offset, size, caretBoundary)
        }

    override val ascent: Float
        get() = intrinsicRun.ascent

    override val descent: Float
        get() = intrinsicRun.descent

    override val leading: Float
        get() = intrinsicRun.leading

    override val width: Float
        get() = intrinsicRun.getRangeDistance(charStart, charEnd)

    override val height: Float
        get() = intrinsicRun.ascent + intrinsicRun.descent + intrinsicRun.leading

    override fun getClusterStart(charIndex: Int): Int {
        return intrinsicRun.getClusterStart(charIndex)
    }

    override fun getClusterEnd(charIndex: Int): Int {
        return intrinsicRun.getClusterEnd(charIndex)
    }

    override fun getGlyphRangeForChars(fromIndex: Int, toIndex: Int): IntRange {
        val glyphRange = intrinsicRun.getGlyphRangeForChars(fromIndex, toIndex)
        return (glyphRange.first - glyphOffset)..(glyphRange.last - glyphOffset)
    }

    override fun getLeadingGlyphIndex(charIndex: Int): Int {
        return intrinsicRun.getLeadingGlyphIndex(charIndex) - glyphOffset
    }

    override fun getTrailingGlyphIndex(charIndex: Int): Int {
        return intrinsicRun.getTrailingGlyphIndex(charIndex) - glyphOffset
    }

    override fun getCaretBoundary(fromIndex: Int, toIndex: Int): Float {
        return intrinsicRun.getCaretBoundary(fromIndex, toIndex) - caretBoundary
    }

    override fun getCaretEdge(charIndex: Int): Float {
        return intrinsicRun.getCaretEdge(charIndex) - caretBoundary
    }

    override fun getRangeDistance(fromIndex: Int, toIndex: Int): Float {
        return intrinsicRun.getRangeDistance(fromIndex, toIndex)
    }

    override fun computeNearestCharIndex(distance: Float): Int {
        return intrinsicRun.computeNearestCharIndex(distance, charStart, charEnd)
    }

    override fun computeBoundingBox(renderer: Renderer, glyphStart: Int, glyphEnd: Int): RectF {
        val actualStart = glyphStart + glyphOffset
        val actualEnd = glyphEnd + glyphOffset

        return intrinsicRun.computeBoundingBox(renderer, actualStart, actualEnd)
    }

    override fun draw(renderer: Renderer, canvas: Canvas) {
        val drawing = DefaultTextRunDrawing(this)
        drawing.draw(renderer, canvas)
    }
}
