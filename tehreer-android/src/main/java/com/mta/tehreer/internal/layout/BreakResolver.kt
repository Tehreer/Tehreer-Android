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

import com.mta.tehreer.internal.util.getTrailingWhitespaceStart
import com.mta.tehreer.layout.BreakMode
import com.mta.tehreer.unicode.BreakClassifier
import kotlin.math.max
import kotlin.math.min

internal class BreakResolver(
    private val text: CharSequence,
    private val paragraphs: ParagraphCollection,
    private val runs: RunCollection,
    private val breaks: BreakClassifier
) {
    private fun findForwardBreak(iterator: IntIterator, startIndex: Int, breakExtent: Float): Int {
        var forwardIndex = startIndex
        var measurement = 0.0f

        for (breakIndex in iterator) {
            measurement += runs.measureChars(forwardIndex, breakIndex)
            if (measurement > breakExtent) {
                val wsStart = text.getTrailingWhitespaceStart(forwardIndex, breakIndex)
                val wsExtent = runs.measureChars(wsStart, breakIndex)

                // Break if excluding whitespace extent helps.
                if ((measurement - wsExtent) <= breakExtent) {
                    forwardIndex = breakIndex
                }
                break
            }

            forwardIndex = breakIndex
        }

        return forwardIndex
    }

    private fun findBackwardBreak(iterator: IntIterator, endIndex: Int, breakExtent: Float): Int {
        var backwardIndex = endIndex
        var measurement = 0.0f

        for (breakIndex in iterator) {
            measurement += runs.measureChars(breakIndex, backwardIndex)
            if (measurement > breakExtent) {
                val wsStart = text.getTrailingWhitespaceStart(breakIndex, backwardIndex)
                val wsExtent = runs.measureChars(wsStart, breakIndex)

                // Break if excluding whitespace extent helps.
                if ((measurement - wsExtent) <= breakExtent) {
                    backwardIndex = breakIndex
                }
                break
            }

            backwardIndex = breakIndex
        }

        return backwardIndex
    }

    fun findForwardBreak(
        startIndex: Int, endIndex: Int, breakExtent: Float, breakMode: BreakMode
    ): Int {
        val paragraph = paragraphs.getParagraph(startIndex)
        val maxIndex = min(endIndex, paragraph.charEnd)

        val iterator = when (breakMode) {
            BreakMode.CHARACTER -> breaks.getForwardGraphemeBreaks(startIndex, maxIndex)
            BreakMode.LINE -> breaks.getForwardLineBreaks(startIndex, maxIndex)
        }

        return findForwardBreak(iterator, startIndex, breakExtent)
    }

    fun findBackwardBreak(
        startIndex: Int, endIndex: Int, breakExtent: Float, breakMode: BreakMode
    ): Int {
        val paragraph = paragraphs.getParagraph(endIndex - 1)
        val minIndex = min(startIndex, paragraph.charStart)

        val iterator = when (breakMode) {
            BreakMode.CHARACTER -> breaks.getBackwardGraphemeBreaks(minIndex, endIndex)
            BreakMode.LINE -> breaks.getBackwardLineBreaks(minIndex, endIndex)
        }

        return findBackwardBreak(iterator, endIndex, breakExtent)
    }

    private fun suggestForwardCharacterBreak(
        startIndex: Int, endIndex: Int, breakExtent: Float
    ): Int {
        val breakIndex = findForwardBreak(startIndex, endIndex, breakExtent, BreakMode.CHARACTER)

        // Take at least one character (grapheme) if extent is too small.
        if (breakIndex == startIndex) {
            return min(endIndex, breakIndex + 1)
        }

        return breakIndex
    }

    private fun suggestBackwardCharacterBreak(
        startIndex: Int, endIndex: Int, breakExtent: Float
    ): Int {
        val breakIndex = findBackwardBreak(startIndex, endIndex, breakExtent, BreakMode.CHARACTER)

        // Take at least one character (grapheme) if extent is too small.
        if (breakIndex == endIndex) {
            return max(startIndex, breakIndex - 1)
        }

        return breakIndex
    }

    private fun suggestForwardLineBreak(startIndex: Int, endIndex: Int, breakExtent: Float): Int {
        val breakIndex = findForwardBreak(startIndex, endIndex, breakExtent, BreakMode.LINE)

        // Fallback to character break if no line break occurs in desired extent.
        if (breakIndex == startIndex) {
            return suggestForwardCharacterBreak(startIndex, endIndex, breakExtent)
        }

        return breakIndex
    }

    private fun suggestBackwardLineBreak(startIndex: Int, endIndex: Int, breakExtent: Float): Int {
        val breakIndex = findBackwardBreak(startIndex, endIndex, breakExtent, BreakMode.LINE)

        // Fallback to character break if no line break occurs in desired extent.
        if (breakIndex == endIndex) {
            return suggestBackwardCharacterBreak(startIndex, endIndex, breakExtent)
        }

        return breakIndex
    }

    fun suggestForwardBreak(
        startIndex: Int, endIndex: Int, breakExtent: Float, breakMode: BreakMode
    ): Int {
        return when (breakMode) {
            BreakMode.CHARACTER -> suggestForwardCharacterBreak(startIndex, endIndex, breakExtent)
            BreakMode.LINE -> suggestForwardLineBreak(startIndex, endIndex, breakExtent)
        }
    }

    fun suggestBackwardBreak(
        startIndex: Int, endIndex: Int, breakExtent: Float, breakMode: BreakMode
    ): Int {
        return when (breakMode) {
            BreakMode.CHARACTER -> suggestBackwardCharacterBreak(startIndex, endIndex, breakExtent)
            BreakMode.LINE -> suggestBackwardLineBreak(startIndex, endIndex, breakExtent)
        }
    }
}
