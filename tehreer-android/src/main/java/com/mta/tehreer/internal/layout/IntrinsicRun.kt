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
    override val startIndex: Int,
    override val endIndex: Int,
    override val isBackward: Boolean,
    override val bidiLevel: Byte,
    override val writingDirection: WritingDirection,
    override val typeface: Typeface,
    override val typeSize: Float,
    override val ascent: Float,
    override val descent: Float,
    override val leading: Float,
    override val glyphIds: IntList,
    override val glyphOffsets: PointList,
    override val glyphAdvances: FloatList,
    override val clusterMap: IntList,
    override val caretEdges: FloatList
) : AbstractTextRun() {
    override val spans: List<Any>
        get() = emptyList()

    override val startExtraLength: Int
        get() = 0

    override val endExtraLength: Int
        get() = 0

    override val glyphCount: Int
        get() = glyphIds.size()

    override fun getClusterStart(charIndex: Int): Int {
        val listIndex = charIndex - startIndex
        val common = clusterMap[listIndex]

        for (i in listIndex - 1 downTo 0) {
            if (clusterMap[i] != common) {
                return (i + 1) + startIndex
            }
        }

        return startIndex
    }

    override fun getClusterEnd(charIndex: Int): Int {
        val listIndex = charIndex - startIndex
        val common = clusterMap[listIndex]
        val size = clusterMap.size()

        for (i in listIndex + 1 until size) {
            if (clusterMap[i] != common) {
                return i + startIndex
            }
        }

        return size + startIndex
    }

    private fun forwardGlyphIndex(listIndex: Int): Int {
        val common = clusterMap[listIndex]
        val size = clusterMap.size()

        for (i in listIndex + 1 until size) {
            val mapping = clusterMap[i]
            if (mapping != common) {
                return mapping - 1
            }
        }

        return glyphIds.size() - 1
    }

    private fun backwardGlyphIndex(listIndex: Int): Int {
        val common = clusterMap[listIndex]

        for (i in listIndex - 1 downTo 0) {
            val mapping = clusterMap[i]
            if (mapping != common) {
                return mapping - 1
            }
        }

        return glyphIds.size() - 1
    }

    override fun getGlyphRangeForChars(fromIndex: Int, toIndex: Int): IntRange {
        val firstIndex = fromIndex - startIndex
        val lastIndex = toIndex - 1 - startIndex

        return if (isBackward) {
            clusterMap[lastIndex]..backwardGlyphIndex(firstIndex)
        } else {
            clusterMap[firstIndex]..forwardGlyphIndex(lastIndex)
        }
    }

    override fun getLeadingGlyphIndex(charIndex: Int): Int {
        val listIndex = charIndex - startIndex
        return if (isBackward) backwardGlyphIndex(listIndex) else clusterMap[listIndex]
    }

    override fun getTrailingGlyphIndex(charIndex: Int): Int {
        val listIndex = charIndex - startIndex
        return if (isBackward) clusterMap[listIndex] else forwardGlyphIndex(listIndex)
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
