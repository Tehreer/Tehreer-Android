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

import com.mta.tehreer.collections.FloatList

internal object CaretUtils {
    fun getLeftMargin(
        caretEdges: FloatList, isRTL: Boolean,
        firstIndex: Int, lastIndex: Int
    ): Float {
        return caretEdges[if (isRTL) lastIndex else firstIndex]
    }

    @JvmStatic
    fun getRangeDistance(
        caretEdges: FloatList, isRTL: Boolean,
        firstIndex: Int, lastIndex: Int
    ): Float {
        val firstEdge = caretEdges[firstIndex]
        val lastEdge = caretEdges[lastIndex]

        return if (isRTL) firstEdge - lastEdge else lastEdge - firstEdge
    }

    @JvmStatic
    fun computeNearestIndex(caretEdges: FloatList, isRTL: Boolean, distance: Float): Int {
        val firstIndex = 0
        val lastIndex = caretEdges.size() - 1

        return computeNearestIndex(caretEdges, isRTL, firstIndex, lastIndex, distance)
    }

    fun computeNearestIndex(
        caretEdges: FloatList, isRTL: Boolean,
        firstIndex: Int, lastIndex: Int, distance: Float
    ): Int {
        val leftMargin = getLeftMargin(caretEdges, isRTL, firstIndex, lastIndex)

        var leadingIndex = -1
        var trailingIndex = -1

        var leadingEdge = 0.0f
        var trailingEdge = 0.0f

        var index = if (isRTL) lastIndex else firstIndex
        val next = if (isRTL) -1 else 1

        while (index <= lastIndex && index >= firstIndex) {
            val caretEdge = caretEdges[index] - leftMargin

            if (caretEdge <= distance) {
                leadingIndex = index
                leadingEdge = caretEdge
            } else {
                trailingIndex = index
                trailingEdge = caretEdge
                break
            }

            index += next
        }

        if (leadingIndex == -1) {
            // Nothing is covered by the input distance.
            return firstIndex
        }

        if (trailingIndex == -1) {
            // Whole range is covered by the input distance.
            return lastIndex
        }

        return if (distance <= (leadingEdge + trailingEdge) / 2.0f) {
            // Input distance is closer to first edge.
            leadingIndex
        } else {
            // Input distance is closer to second edge.
            trailingIndex
        }
    }
}
