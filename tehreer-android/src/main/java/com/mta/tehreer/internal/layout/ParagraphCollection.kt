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

import com.mta.tehreer.unicode.BidiParagraph
import com.mta.tehreer.unicode.BidiRun
import java.util.ArrayList
import kotlin.math.max
import kotlin.math.min

internal class ParagraphCollection : ArrayList<BidiParagraph>() {
    fun binarySearch(charIndex: Int): Int {
        var low = 0
        var high = size - 1

        while (low <= high) {
            val mid = (low + high) ushr 1
            val value = this[mid]

            if (charIndex >= value.charEnd) {
                low = mid + 1
            } else if (charIndex < value.charStart) {
                high = mid - 1
            } else {
                return mid
            }
        }

        return -(low + 1)
    }

    fun charLevel(charIndex: Int): Byte {
        val paragraphIndex = binarySearch(charIndex)
        return this[paragraphIndex].baseLevel
    }

    interface RunConsumer {
        fun accept(bidiRun: BidiRun)
    }

    fun forEachLineRun(lineStart: Int, lineEnd: Int, runConsumer: RunConsumer) {
        var paragraphIndex = binarySearch(lineStart)
        val directionalParagraph = this[paragraphIndex]
        val isRTL = (directionalParagraph.baseLevel.toInt() and 1) == 1

        if (isRTL) {
            val paragraphEnd = directionalParagraph.charEnd
            if (paragraphEnd < lineEnd) {
                paragraphIndex = binarySearch(lineEnd - 1)
            }
        }

        val next = if (isRTL) -1 else 1
        var feasibleStart: Int
        var feasibleEnd: Int

        do {
            val bidiParagraph = this[paragraphIndex]
            feasibleStart = max(bidiParagraph.charStart, lineStart)
            feasibleEnd = min(bidiParagraph.charEnd, lineEnd)

            val bidiLine = bidiParagraph.createLine(feasibleStart, feasibleEnd)
            val bidiRuns = bidiLine.visualRuns

            for (i in 0 until bidiRuns.size) {
                runConsumer.accept(bidiRuns[i])
            }

            bidiLine.dispose()
            paragraphIndex += next
        } while (if (isRTL) feasibleStart != lineStart else feasibleEnd != lineEnd)
    }

    protected fun finalize() {
        for (i in 0 until size) {
            this[i].dispose()
        }
    }
}
