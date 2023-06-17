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
import com.mta.tehreer.collections.FloatList
import com.mta.tehreer.sfnt.WritingDirection
import com.mta.tehreer.collections.IntList
import com.mta.tehreer.collections.PointList
import com.mta.tehreer.graphics.Renderer
import com.mta.tehreer.graphics.Typeface
import com.mta.tehreer.internal.graphics.DefaultTextRunDrawing
import com.mta.tehreer.internal.util.Preconditions.checkElementIndex
import com.mta.tehreer.internal.util.Preconditions.checkIndexRange
import com.mta.tehreer.internal.util.Preconditions.checkNotNull
import com.mta.tehreer.internal.util.isOdd
import com.mta.tehreer.internal.util.toFloatList

internal class JustifiedRun(
    private val textRun: TextRun,
    justifiedAdvances: FloatList
) : AbstractTextRun() {
    override val glyphAdvances: FloatList
    override val caretEdges: FloatList

    init {
        val isRTL = textRun.bidiLevel.isOdd()

        val caretEdgesArray = CaretEdgesBuilder()
            .setBackward(textRun.isBackward)
            .setRTL(isRTL)
            .setGlyphAdvances(justifiedAdvances)
            .setClusterMap(textRun.clusterMap)
            .setCaretStops(null)
            .build()

        val runLength = textRun.endIndex - textRun.startIndex
        val firstIndex = textRun.startExtraLength
        val lastIndex = firstIndex + runLength

        val caretBoundary = caretEdgesArray[if (isRTL) lastIndex else firstIndex]

        glyphAdvances = justifiedAdvances
        caretEdges = CaretEdges(caretEdgesArray, caretBoundary)
    }

    internal class CaretEdges(
        val base: FloatArray,
        val offset: Int,
        val size: Int,
        val boundary: Float
    ) : FloatList() {
        constructor(
            base: FloatArray,
            boundary: Float
        ): this(base, 0, base.size, boundary)

        override fun size(): Int {
            return size
        }

        override fun get(index: Int): Float {
            checkElementIndex(index, size)
            return base[index + offset] - boundary
        }

        override fun copyTo(array: FloatArray, atIndex: Int) {
            checkNotNull(array)

            for (i in 0 until size) {
                array[i + atIndex] = base[i + offset] - boundary
            }
        }

        override fun subList(fromIndex: Int, toIndex: Int): FloatList {
            checkIndexRange(fromIndex, toIndex, size)
            return CaretEdges(base, offset + fromIndex, toIndex - fromIndex, boundary)
        }
    }

    override val startIndex: Int
        get() = textRun.startIndex

    override val endIndex: Int
        get() = textRun.endIndex

    override val isBackward: Boolean
        get() = textRun.isBackward

    override val bidiLevel: Byte
        get() = textRun.bidiLevel

    override val spans: List<Any?>
        get() = textRun.spans

    override val startExtraLength: Int
        get() = textRun.startExtraLength

    override val endExtraLength: Int
        get() = textRun.endExtraLength

    override val typeface: Typeface
        get() = textRun.typeface

    override val typeSize: Float
        get() = textRun.typeSize

    override val writingDirection: WritingDirection
        get() = textRun.writingDirection

    override val glyphCount: Int
        get() = textRun.glyphCount

    override val glyphIds: IntList
        get() = textRun.glyphIds

    override val glyphOffsets: PointList
        get() = textRun.glyphOffsets

    override val clusterMap: IntList
        get() = textRun.clusterMap

    override val ascent: Float
        get() = textRun.ascent

    override val descent: Float
        get() = textRun.descent

    override val leading: Float
        get() = textRun.leading

    override val width: Float
        get() = super.width

    override val height: Float
        get() = textRun.height

    override fun getClusterStart(charIndex: Int): Int {
        return textRun.getClusterStart(charIndex)
    }

    override fun getClusterEnd(charIndex: Int): Int {
        return textRun.getClusterEnd(charIndex)
    }

    override fun getGlyphRangeForChars(fromIndex: Int, toIndex: Int): IntRange {
        return textRun.getGlyphRangeForChars(fromIndex, toIndex)
    }

    override fun getLeadingGlyphIndex(charIndex: Int): Int {
        return textRun.getLeadingGlyphIndex(charIndex)
    }

    override fun getTrailingGlyphIndex(charIndex: Int): Int {
        return textRun.getTrailingGlyphIndex(charIndex)
    }

    override fun draw(renderer: Renderer, canvas: Canvas) {
        val drawing = DefaultTextRunDrawing(this)
        drawing.draw(renderer, canvas)
    }
}
