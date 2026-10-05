/*
 * Copyright (C) 2023-2026 Muhammad Tayyab Akram
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
    /**
     * A block run (a view that has a line of its own) that turns up while measuring for a break
     * candidate settles the break right away, from its own position, instead of the extent
     * accumulated so far: extent comparisons never get a say over a block.
     *
     * [startIndex] and [endIndex] are the whole range this break search was asked for (not the
     * current candidate step) - the same bounds [suggestForwardBreak] would have handed to a
     * separate `keepBlocksAlone` pass, were one still taken.
     */
    private fun findForwardBreak(
        iterator: IntIterator, startIndex: Int, endIndex: Int, breakExtent: Float
    ): Int {
        var forwardIndex = startIndex
        var measurement = 0.0f

        for (breakIndex in iterator) {
            val block = runs.findBlockForward(forwardIndex, breakIndex)

            if (block != null) {
                // A block that is not the very first thing on the line ends the line right
                // before it, whatever text has been accepted so far; the block gets a line of
                // its own later. A block that IS the first thing IS the line, its own line
                // stretching through the whitespace that follows it.
                return if (block.startIndex > startIndex) {
                    block.startIndex
                } else {
                    keepBlockLine(block, endIndex)
                }
            }

            measurement += runs.measureChars(forwardIndex, breakIndex)
            if (measurement > breakExtent) {
                val wsStart = text.getTrailingWhitespaceStart(forwardIndex, breakIndex)
                // Whitespace never holds a block, so the plain extent is enough here.
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

    /**
     * The mirror of [findForwardBreak]'s block handling: a block found while measuring settles the
     * break from its own position, without falling through to the extent comparison. Unlike the
     * forward search - which only ever treats the very first thing on the whole line specially -
     * the backward search re-checks adjacency on every step, because the boundary it is testing
     * against (`backwardIndex`) moves leftward as candidates are accepted, one at a time.
     */
    private fun findBackwardBreak(
        iterator: IntIterator, startIndex: Int, endIndex: Int, breakExtent: Float
    ): Int {
        var backwardIndex = endIndex
        var measurement = 0.0f

        for (breakIndex in iterator) {
            val block = runs.findBlockBackward(breakIndex, backwardIndex)

            if (block != null) {
                // A block that is not the nearest thing to the boundary already reached leaves
                // everything from it onward for this line, and nothing from it or before it; a
                // block that IS the nearest thing IS (part of) the line, together with the
                // whitespace/separator that precedes it.
                return if (block.endIndex < backwardIndex) {
                    block.endIndex
                } else {
                    keepBlockLineBackward(block, startIndex)
                }
            }

            measurement += runs.measureChars(breakIndex, backwardIndex)
            if (measurement > breakExtent) {
                val wsStart = text.getTrailingWhitespaceStart(breakIndex, backwardIndex)
                // Whitespace never holds a block, so the plain extent is enough here.
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

    /**
     * A block that opens its line (nothing comes before it) keeps that line through the
     * whitespace that follows it, up to and including a newline that ends it - bounded by
     * [endIndex], the end of the whole range the break was asked for.
     */
    private fun keepBlockLine(block: ReplacementRun, endIndex: Int): Int {
        var lineEnd = block.endIndex

        while (lineEnd < endIndex && text[lineEnd].isWhitespace()) {
            lineEnd += 1

            if (text[lineEnd - 1] == '\n') {
                break
            }
        }

        return lineEnd
    }

    /**
     * The mirror of [keepBlockLine]: a block that closes its line (nothing comes after it, on
     * this line) keeps that line through the whitespace/separator that precedes it, up to and
     * including a newline that starts it - bounded by [startIndex], the start of the whole range
     * the break was asked for.
     */
    private fun keepBlockLineBackward(block: ReplacementRun, startIndex: Int): Int {
        var lineStart = block.startIndex

        while (lineStart > startIndex && text[lineStart - 1].isWhitespace()) {
            lineStart -= 1

            if (text[lineStart] == '\n') {
                break
            }
        }

        return lineStart
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

        return findForwardBreak(iterator, startIndex, endIndex, breakExtent)
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

        return findBackwardBreak(iterator, startIndex, endIndex, breakExtent)
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

    /**
     * A view that has a line of its own is the only thing on it, whatever the width of the line:
     * the line ends before the view, and the line of the view goes on to the end of the whitespace
     * that follows, which is normally the newline that ends its paragraph. This is now handled as
     * part of the forward measurement scan itself - see [findForwardBreak] - rather than as a
     * separate pass over the runs once a naive, extent-only break has been found.
     */
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
