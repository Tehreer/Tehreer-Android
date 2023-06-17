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

import android.graphics.RectF
import com.mta.tehreer.graphics.Renderer
import com.mta.tehreer.internal.util.isOdd

internal abstract class AbstractTextRun : TextRun {
    val isRTL: Boolean
        get() = bidiLevel.isOdd()

    override val width: Float
        get() = getRangeDistance(startIndex, endIndex)

    override val height: Float
        get() = ascent + descent + leading

    override fun getCaretEdge(charIndex: Int): Float {
        val actualStart = startIndex - startExtraLength
        return caretEdges[charIndex - actualStart]
    }

    override fun getRangeDistance(fromIndex: Int, toIndex: Int): Float {
        val actualStart = startIndex - startExtraLength
        val firstIndex = fromIndex - actualStart
        val lastIndex = toIndex - actualStart

        return CaretUtils.getRangeDistance(caretEdges, isRTL, firstIndex, lastIndex)
    }

    override fun computeNearestCharIndex(distance: Float): Int {
        return computeNearestCharIndex(distance, startIndex, endIndex)
    }

    protected open fun computeNearestCharIndex(distance: Float, fromIndex: Int, toIndex: Int): Int {
        val firstIndex = fromIndex - startIndex
        val lastIndex = toIndex - startIndex

        val nearestIndex = CaretUtils.computeNearestIndex(
            caretEdges, isRTL,
            firstIndex, lastIndex, distance
        )

        return nearestIndex + startIndex
    }

    override fun computeBoundingBox(renderer: Renderer, glyphStart: Int, glyphEnd: Int): RectF {
        renderer.typeface = typeface
        renderer.typeSize = typeSize
        renderer.writingDirection = writingDirection

        return renderer.computeBoundingBox(
            glyphIds.subList(glyphStart, glyphEnd),
            glyphOffsets.subList(glyphStart, glyphEnd),
            glyphAdvances.subList(glyphStart, glyphEnd)
        )
    }
}
