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
import com.mta.tehreer.collections.FloatList
import com.mta.tehreer.collections.IntList
import com.mta.tehreer.collections.PointList
import com.mta.tehreer.graphics.Renderer
import com.mta.tehreer.graphics.Typeface
import java.lang.UnsupportedOperationException

internal class IntrinsicRun(
    override val charStart: Int,
    override val charEnd: Int,
    override val isBackward: Boolean,
    override val bidiLevel: Byte,
    override val writingDirection: WritingDirection,
    override val typeface: Typeface,
    override val typeSize: Float,
    override val ascent: Float,
    override val descent: Float,
    override val leading: Float,
    private val glyphIdArray: IntArray,
    private val glyphOffsetArray: FloatArray,
    private val glyphAdvanceArray: FloatArray,
    val clusterMapArray: IntArray,
    override val caretEdges: FloatList
) : AbstractTextRun() {
    override val spans: List<Any>
        get() = emptyList()

    override val startExtraLength: Int
        get() = 0

    override val endExtraLength: Int
        get() = 0

    override val glyphCount: Int
        get() = glyphIdArray.size

    override val glyphIds = IntList.of(*glyphIdArray)
    override val glyphOffsets = PointList.of(*glyphOffsetArray)
    override val glyphAdvances = FloatList.of(*glyphAdvanceArray)

    override val clusterMap = IntList.of(*clusterMapArray)

    override fun getClusterStart(charIndex: Int): Int {
        val arrayIndex = charIndex - charStart
        val common = clusterMapArray[arrayIndex]

        for (i in arrayIndex - 1 downTo 0) {
            if (clusterMapArray[i] != common) {
                return (i + 1) + charStart
            }
        }

        return charStart
    }

    override fun getClusterEnd(charIndex: Int): Int {
        val arrayIndex = charIndex - charStart
        val common = clusterMapArray[arrayIndex]
        val length = clusterMapArray.size

        for (i in arrayIndex + 1 until length) {
            if (clusterMapArray[i] != common) {
                return i + charStart
            }
        }

        return length + charStart
    }

    private fun forwardGlyphIndex(arrayIndex: Int): Int {
        val common = clusterMapArray[arrayIndex]
        val length = clusterMapArray.size

        for (i in arrayIndex + 1 until length) {
            val mapping = clusterMapArray[i]
            if (mapping != common) {
                return mapping - 1
            }
        }

        return glyphIdArray.size - 1
    }

    private fun backwardGlyphIndex(arrayIndex: Int): Int {
        val common = clusterMapArray[arrayIndex]

        for (i in arrayIndex - 1 downTo 0) {
            val mapping = clusterMapArray[i]
            if (mapping != common) {
                return mapping - 1
            }
        }

        return glyphIdArray.size - 1
    }

    override fun getGlyphRangeForChars(fromIndex: Int, toIndex: Int): IntArray {
        val firstIndex = fromIndex - charStart
        val lastIndex = toIndex - 1 - charStart

        val glyphRange = IntArray(2)

        if (isBackward) {
            glyphRange[0] = clusterMapArray[lastIndex]
            glyphRange[1] = backwardGlyphIndex(firstIndex) + 1
        } else {
            glyphRange[0] = clusterMapArray[firstIndex]
            glyphRange[1] = forwardGlyphIndex(lastIndex) + 1
        }

        return glyphRange
    }

    override fun getLeadingGlyphIndex(charIndex: Int): Int {
        val arrayIndex = charIndex - charStart
        return if (isBackward) backwardGlyphIndex(arrayIndex) else clusterMapArray[arrayIndex]
    }

    override fun getTrailingGlyphIndex(charIndex: Int): Int {
        val arrayIndex = charIndex - charStart
        return if (isBackward) clusterMapArray[arrayIndex] else forwardGlyphIndex(arrayIndex)
    }

    override fun getCaretBoundary(fromIndex: Int, toIndex: Int): Float {
        return super.getCaretBoundary(fromIndex, toIndex)
    }

    override fun getCaretEdge(charIndex: Int): Float {
        return super.getCaretEdge(charIndex)
    }

    public override fun getCaretEdge(charIndex: Int, caretBoundary: Float): Float {
        return super.getCaretEdge(charIndex, caretBoundary)
    }

    override fun getRangeDistance(fromIndex: Int, toIndex: Int): Float {
        return super.getRangeDistance(fromIndex, toIndex)
    }

    override fun computeNearestCharIndex(distance: Float): Int {
        return super.computeNearestCharIndex(distance)
    }

    public override fun computeNearestCharIndex(
        distance: Float,
        fromIndex: Int,
        toIndex: Int
    ): Int {
        return super.computeNearestCharIndex(distance, fromIndex, toIndex)
    }

    override fun draw(renderer: Renderer, canvas: Canvas) {
        throw UnsupportedOperationException()
    }
}