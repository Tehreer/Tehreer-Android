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

import java.util.ArrayList
import kotlin.math.min

internal class RunCollection : ArrayList<TextRun>() {
    fun binarySearch(charIndex: Int): Int {
        var low = 0
        var high = size - 1

        while (low <= high) {
            val mid = (low + high) ushr 1
            val value = this[mid]

            if (charIndex >= value.endIndex) {
                low = mid + 1
            } else if (charIndex < value.startIndex) {
                high = mid - 1
            } else {
                return mid
            }
        }

        return -(low + 1)
    }

    fun measureChars(charStart: Int, charEnd: Int): Float {
        var startIndex = charStart
        var extent = 0.0f

        if (charEnd > startIndex) {
            var runIndex = binarySearch(startIndex)

            do {
                val textRun = this[runIndex]
                val segmentEnd = min(charEnd, textRun.endIndex)
                extent += textRun.getRangeDistance(startIndex, segmentEnd)

                startIndex = segmentEnd
                runIndex++
            } while (startIndex < charEnd)
        }

        return extent
    }
}
