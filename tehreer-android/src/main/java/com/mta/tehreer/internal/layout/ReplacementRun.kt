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
import android.graphics.Paint
import com.mta.tehreer.internal.layout.CaretUtils.getRangeDistance
import com.mta.tehreer.internal.layout.CaretUtils.computeNearestIndex
import android.text.style.ReplacementSpan
import com.mta.tehreer.collections.FloatList
import com.mta.tehreer.sfnt.WritingDirection
import com.mta.tehreer.collections.IntList
import com.mta.tehreer.collections.PointList
import android.graphics.RectF
import com.mta.tehreer.graphics.Renderer
import com.mta.tehreer.graphics.Typeface
import com.mta.tehreer.internal.util.isOdd

internal class ReplacementRun(
    val charSequence: CharSequence,
    override val charStart: Int,
    override val charEnd: Int,
    override val bidiLevel: Byte,
    private val replacementSpan: ReplacementSpan,
    val paint: Paint,
    override val typeface: Typeface,
    override val typeSize: Float,
    private val replacementAscent: Int,
    private val replacementDescent: Int,
    private val replacementLeading: Int,
    private val replacementExtent: Int,
    override val caretEdges: FloatList
) : TextRun {
    override val isBackward: Boolean
        get() = false

    private val isRTL: Boolean
        get() = bidiLevel.isOdd()

    override val spans: List<Any>
        get() = listOf(replacementSpan as Any)

    override val startExtraLength: Int
        get() = 0

    override val endExtraLength: Int
        get() = 0

    override val writingDirection: WritingDirection
        get() = WritingDirection.LEFT_TO_RIGHT

    override val glyphCount: Int
        get() = 1

    private val spaceGlyphId: Int
        get() = typeface.getGlyphId(' '.code)

    override val glyphIds: IntList
        get() = IntList.of(spaceGlyphId)

    override val glyphOffsets: PointList
        get() = PointList.of(0f, 0f)

    override val glyphAdvances: FloatList
        get() = FloatList.of(width)

    override val clusterMap: IntList
        get() = IntList.of(*IntArray(charEnd - charStart))

    override val ascent: Float
        get() = replacementAscent.toFloat()

    override val descent: Float
        get() = replacementDescent.toFloat()

    override val leading: Float
        get() = replacementLeading.toFloat()

    override val width: Float
        get() = replacementExtent.toFloat()

    override val height: Float
        get() = (replacementAscent + replacementDescent + replacementLeading).toFloat()

    override fun getClusterStart(charIndex: Int): Int {
        return charStart
    }

    override fun getClusterEnd(charIndex: Int): Int {
        return charEnd
    }

    override fun getGlyphRangeForChars(fromIndex: Int, toIndex: Int): IntRange {
        return 0..0
    }

    override fun getLeadingGlyphIndex(charIndex: Int): Int {
        return 0
    }

    override fun getTrailingGlyphIndex(charIndex: Int): Int {
        return 1
    }

    override fun getCaretBoundary(fromIndex: Int, toIndex: Int): Float {
        return 0.0f
    }

    override fun getCaretEdge(charIndex: Int): Float {
        return caretEdges[charIndex - charStart]
    }

    override fun getRangeDistance(fromIndex: Int, toIndex: Int): Float {
        val firstIndex = fromIndex - charStart
        val lastIndex = toIndex - charStart

        return getRangeDistance(caretEdges, isRTL, firstIndex, lastIndex)
    }

    override fun computeNearestCharIndex(distance: Float): Int {
        return computeNearestIndex(caretEdges, isRTL, distance) + charStart
    }

    override fun computeTypographicExtent(glyphStart: Int, glyphEnd: Int): Float {
        return width
    }

    override fun computeBoundingBox(renderer: Renderer, glyphStart: Int, glyphEnd: Int): RectF {
        return RectF(0.0f, 0.0f, width, height)
    }

    override fun draw(renderer: Renderer, canvas: Canvas) {
        replacementSpan.draw(
            canvas,
            charSequence, charStart, charEnd,
            0f, -replacementAscent,
            0, replacementDescent,
            paint
        )
    }
}
