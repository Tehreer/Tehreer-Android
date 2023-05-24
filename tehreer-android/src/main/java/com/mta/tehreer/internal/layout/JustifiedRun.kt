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
import android.graphics.RectF
import com.mta.tehreer.graphics.Renderer
import com.mta.tehreer.graphics.Typeface
import com.mta.tehreer.internal.graphics.DefaultTextRunDrawing
import com.mta.tehreer.internal.util.isOdd
import com.mta.tehreer.internal.util.toList

internal class JustifiedRun(
    private val textRun: TextRun,
    justifiedAdvances: FloatList
) : AbstractTextRun() {
    override val glyphAdvances: FloatList
    override val caretEdges: FloatList
    private var caretBoundary = 0.0f

    init {
        val isRTL = textRun.bidiLevel.isOdd()

        glyphAdvances = justifiedAdvances
        caretEdges = CaretEdgesBuilder()
            .setBackward(textRun.isBackward)
            .setRTL(isRTL)
            .setGlyphAdvances(justifiedAdvances)
            .setClusterMap(textRun.clusterMap)
            .setCaretStops(null)
            .build()
            .toList()

        if (isRTL) {
            if (textRun.startExtraLength > 0) {
                caretBoundary = getCaretBoundary(textRun.startIndex, textRun.endIndex)
            }
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

    override fun getCaretBoundary(fromIndex: Int, toIndex: Int): Float {
        return super.getCaretBoundary(fromIndex, toIndex) - caretBoundary
    }

    override fun getCaretEdge(charIndex: Int): Float {
        return super.getCaretEdge(charIndex)
    }

    override fun getRangeDistance(fromIndex: Int, toIndex: Int): Float {
        return super.getRangeDistance(fromIndex, toIndex)
    }

    override fun computeNearestCharIndex(distance: Float): Int {
        return super.computeNearestCharIndex(distance)
    }

    override fun computeBoundingBox(renderer: Renderer, glyphStart: Int, glyphEnd: Int): RectF {
        return super.computeBoundingBox(renderer, glyphStart, glyphEnd)
    }

    override fun draw(renderer: Renderer, canvas: Canvas) {
        val drawing = DefaultTextRunDrawing(this)
        drawing.draw(renderer, canvas)
    }
}
