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

package com.mta.tehreer.layout

import android.text.Spanned
import com.mta.tehreer.collections.FloatList
import com.mta.tehreer.internal.layout.*
import com.mta.tehreer.internal.layout.ParagraphCollection.RunConsumer
import com.mta.tehreer.internal.util.getLeadingWhitespaceEnd
import com.mta.tehreer.internal.util.getNextSpace
import com.mta.tehreer.internal.util.getTrailingWhitespaceStart
import com.mta.tehreer.internal.util.isEven
import com.mta.tehreer.unicode.BidiRun
import java.util.*
import kotlin.math.max
import kotlin.math.min

private fun createGlyphRun(
    textRun: TextRun, spanStart: Int, spanEnd: Int,
    spans: Array<Any>
): GlyphRun {
    var innerRun = textRun
    if (innerRun is IntrinsicRun) {
        innerRun = IntrinsicRunSlice(innerRun, spanStart, spanEnd, listOf(*spans))
    }

    return GlyphRun(innerRun)
}

private fun createComposedLine(
    text: CharSequence, charStart: Int, charEnd: Int, breakEnd: Int,
    runList: List<GlyphRun>, paragraphLevel: Byte
): ComposedLine {
    var lineAscent = 0.0f
    var lineDescent = 0.0f
    var lineLeading = 0.0f
    var lineExtent = 0.0f

    val trailingWhitespaceStart = text.getTrailingWhitespaceStart(charStart, charEnd)
    var trailingWhitespaceExtent = 0.0f

    for (glyphRun in runList) {
        glyphRun.originX = lineExtent

        val wsStart = max(glyphRun.charStart, trailingWhitespaceStart)
        val wsEnd = min(glyphRun.charEnd, charEnd)
        if (wsStart < wsEnd) {
            trailingWhitespaceExtent += glyphRun.computeRangeDistance(wsStart, wsEnd)
        }

        lineAscent = max(lineAscent, glyphRun.ascent)
        lineDescent = max(lineDescent, glyphRun.descent)
        lineLeading = max(lineLeading, glyphRun.leading)
        lineExtent += glyphRun.width
    }

    return ComposedLine(
        charStart, charEnd, breakEnd, paragraphLevel,
        lineAscent, lineDescent, lineLeading, lineExtent,
        trailingWhitespaceExtent, Collections.unmodifiableList(runList)
    )
}

internal class LineResolver(
    private val spanned: Spanned,
    private val bidiParagraphs: ParagraphCollection,
    private val intrinsicRuns: RunCollection
) {
    fun createSimpleLine(start: Int, end: Int): ComposedLine {
        val runList = mutableListOf<GlyphRun>()

        bidiParagraphs.forEachLineRun(start, end, object : RunConsumer {
            override fun accept(bidiRun: BidiRun) {
                val visualStart = bidiRun.charStart
                val visualEnd = bidiRun.charEnd

                addVisualRuns(visualStart, visualEnd, runList)
            }
        })

        return createComposedLine(
            spanned, start, end, end, runList,
            bidiParagraphs.getBaseLevel(start)
        )
    }

    fun createCompactLine(
        start: Int, end: Int, extent: Float,
        breakResolver: BreakResolver,
        mode: BreakMode,
        place: TruncationPlace,
        token: ComposedLine
    ): ComposedLine {
        val tokenlessWidth = extent - token.width

        return when (place) {
            TruncationPlace.START -> {
                createStartTruncatedLine(start, end, tokenlessWidth, breakResolver, mode, token)
            }
            TruncationPlace.MIDDLE -> {
                createMiddleTruncatedLine(start, end, tokenlessWidth, breakResolver, mode, token)
            }
            TruncationPlace.END -> {
                createEndTruncatedLine(start, end, tokenlessWidth, breakResolver, mode, token)
            }
        }
    }

    private inner class TruncationHandler(
        val charStart: Int,
        val charEnd: Int,
        val skipStart: Int,
        val skipEnd: Int,
        val runList: MutableList<GlyphRun>
    ) : RunConsumer {
        var leadingTokenIndex = -1
        var trailingTokenIndex = -1

        override fun accept(bidiRun: BidiRun) {
            val visualStart = bidiRun.charStart
            val visualEnd = bidiRun.charEnd

            if (bidiRun.isRightToLeft) {
                // Handle second part of characters.
                if (visualEnd >= skipEnd) {
                    addVisualRuns(max(visualStart, skipEnd), visualEnd, runList)

                    if (visualStart < skipEnd) {
                        trailingTokenIndex = runList.size
                    }
                }

                // Handle first part of characters.
                if (visualStart <= skipStart) {
                    if (visualEnd > skipStart) {
                        leadingTokenIndex = runList.size
                    }
                    addVisualRuns(visualStart, min(visualEnd, skipStart), runList)
                }
            } else {
                // Handle first part of characters.
                if (visualStart <= skipStart) {
                    addVisualRuns(visualStart, min(visualEnd, skipStart), runList)

                    if (visualEnd > skipStart) {
                        leadingTokenIndex = runList.size
                    }
                }

                // Handle second part of characters.
                if (visualEnd >= skipEnd) {
                    if (visualStart < skipEnd) {
                        trailingTokenIndex = runList.size
                    }

                    addVisualRuns(max(visualStart, skipEnd), visualEnd, runList)
                }
            }
        }

        fun addAllRuns() {
            bidiParagraphs.forEachLineRun(charStart, charEnd, this)
        }
    }

    private fun createStartTruncatedLine(
        start: Int, end: Int, tokenlessWidth: Float,
        breakResolver: BreakResolver,
        mode: BreakMode,
        token: ComposedLine
    ): ComposedLine {
        val truncatedStart = breakResolver.suggestBackwardBreak(start, end, tokenlessWidth, mode)
        if (truncatedStart > start) {
            val runList = ArrayList<GlyphRun>()
            var tokenInsertIndex = 0

            if (truncatedStart < end) {
                val truncationHandler = TruncationHandler(start, end, start, truncatedStart, runList)
                truncationHandler.addAllRuns()

                tokenInsertIndex = truncationHandler.trailingTokenIndex
            }
            addTokenRuns(token, runList, tokenInsertIndex)

            return createComposedLine(
                spanned, truncatedStart, end, end, runList,
                bidiParagraphs.getBaseLevel(truncatedStart)
            )
        }

        return createSimpleLine(truncatedStart, end)
    }

    private fun createMiddleTruncatedLine(
        start: Int, end: Int, tokenlessWidth: Float,
        breakResolver: BreakResolver,
        mode: BreakMode,
        token: ComposedLine
    ): ComposedLine {
        val halfWidth = tokenlessWidth / 2.0f
        var firstMidEnd = breakResolver.suggestForwardBreak(start, end, halfWidth, mode)
        var secondMidStart = breakResolver.suggestBackwardBreak(start, end, halfWidth, mode)

        if (firstMidEnd < secondMidStart) {
            // Exclude inner whitespaces as truncation token replaces them.
            firstMidEnd = spanned.getTrailingWhitespaceStart(start, firstMidEnd)
            secondMidStart = spanned.getLeadingWhitespaceEnd(secondMidStart, end)

            val runList = mutableListOf<GlyphRun>()
            var tokenInsertIndex = 0

            if (start < firstMidEnd || secondMidStart < end) {
                val truncationHandler = TruncationHandler(start, end, firstMidEnd, secondMidStart, runList)
                truncationHandler.addAllRuns()

                tokenInsertIndex = truncationHandler.leadingTokenIndex
            }
            addTokenRuns(token, runList, tokenInsertIndex)

            return createComposedLine(
                spanned, start, end, end, runList,
                bidiParagraphs.getBaseLevel(start)
            )
        }

        return createSimpleLine(start, end)
    }

    private fun createEndTruncatedLine(
        start: Int, end: Int, tokenlessWidth: Float,
        breakResolver: BreakResolver,
        mode: BreakMode,
        token: ComposedLine
    ): ComposedLine {
        val breakEnd = breakResolver.suggestForwardBreak(start, end, tokenlessWidth, mode)
        if (breakEnd < end) {
            // Exclude trailing whitespaces as truncation token replaces them.
            val truncatedEnd = spanned.getTrailingWhitespaceStart(start, breakEnd)

            val runList = mutableListOf<GlyphRun>()
            var tokenInsertIndex = 0

            if (start < truncatedEnd) {
                val truncationHandler = TruncationHandler(start, end, truncatedEnd, end, runList)
                truncationHandler.addAllRuns()

                tokenInsertIndex = truncationHandler.leadingTokenIndex
            }
            addTokenRuns(token, runList, tokenInsertIndex)

            return createComposedLine(
                spanned, start, truncatedEnd, breakEnd, runList,
                bidiParagraphs.getBaseLevel(start)
            )
        }

        return createSimpleLine(start, breakEnd)
    }

    private fun addTokenRuns(token: ComposedLine, runList: MutableList<GlyphRun>, index: Int) {
        var insertIndex = index

        for (truncationRun in token.runs) {
            val modifiedRun = GlyphRun(truncationRun, true)
            runList.add(insertIndex, modifiedRun)

            insertIndex++
        }
    }

    private fun addVisualRuns(fromIndex: Int, toIndex: Int, runList: MutableList<GlyphRun>) {
        var visualStart = fromIndex

        if (visualStart < toIndex) {
            // ASSUMPTIONS:
            //      - Visual range may fall in one or more glyph runs.
            //      - Consecutive intrinsic runs may have same bidi level.
            var insertIndex = runList.size
            var previousRun: TextRun? = null

            do {
                val runIndex = intrinsicRuns.binarySearch(visualStart)

                val textRun = intrinsicRuns[runIndex]
                val feasibleStart = max(textRun.startIndex, visualStart)
                val feasibleEnd = min(textRun.endIndex, toIndex)

                val bidiLevel = textRun.bidiLevel
                val isForwardRun = bidiLevel.isEven()

                if (previousRun != null) {
                    if (bidiLevel != previousRun.bidiLevel || isForwardRun) {
                        insertIndex = runList.size
                    }
                }

                var spanStart = feasibleStart
                while (spanStart < feasibleEnd) {
                    val spanEnd = spanned.nextSpanTransition(spanStart, feasibleEnd, Any::class.java)
                    val spans = spanned.getSpans(spanStart, spanEnd, Any::class.java)

                    val glyphRun = createGlyphRun(textRun, spanStart, spanEnd, spans)
                    runList.add(insertIndex, glyphRun)

                    if (isForwardRun) {
                        insertIndex++
                    }

                    spanStart = spanEnd
                }

                previousRun = textRun
                visualStart = feasibleEnd
            } while (visualStart != toIndex)
        }
    }

    fun createJustifiedLine(
        line: ComposedLine,
        justificationFactor: Float,
        justificationWidth: Float
    ): ComposedLine {
        val lineStart = line.charStart
        val lineEnd = line.charEnd
        val breakEnd = line.breakEnd;

        val wordStart = spanned.getLeadingWhitespaceEnd(lineStart, lineEnd)
        val wordEnd = spanned.getTrailingWhitespaceStart(lineStart, lineEnd)

        val runList = line.runs
        val runCount = runList.size

        val actualWidth = line.width - line.trailingWhitespaceExtent
        var innerSpaceCount = 0

        for (i in 0 until runCount) {
            val glyphRun = runList[i]
            val textRun = glyphRun.textRun

            if (glyphRun.isTruncated || textRun is ReplacementRun) {
                continue
            }

            val runStart = max(glyphRun.charStart, wordStart)
            val runEnd = min(glyphRun.charEnd, wordEnd)

            innerSpaceCount += computeSpaceCount(runStart, runEnd)
        }

        val extraWidth = justificationWidth - actualWidth
        val availableWidth = extraWidth * justificationFactor
        val spaceAddition = availableWidth / innerSpaceCount

        for (i in 0 until runCount) {
            val glyphRun = runList[i]
            val textRun = glyphRun.textRun

            if (glyphRun.isTruncated || textRun is ReplacementRun) {
                continue
            }

            val glyphAdvances = glyphRun.glyphAdvances.toArray()

            val runStart = max(wordStart, glyphRun.charStart)
            val runEnd = min(wordEnd, glyphRun.charEnd)

            var j = runStart
            while (j < runEnd) {
                val spaceStart = spanned.getNextSpace(j, runEnd)
                val spaceEnd = spanned.getLeadingWhitespaceEnd(spaceStart, runEnd)

                j = spaceEnd

                if (spaceStart == spaceEnd) {
                    continue
                }

                val glyphRange = textRun.getGlyphRangeForChars(spaceStart, spaceEnd)
                val glyphCount = glyphRange.last - glyphRange.first + 1

                val spaceCount = spaceEnd - spaceStart

                val distribution = spaceCount.toFloat() / glyphCount
                val advanceAddition = spaceAddition * distribution

                for (k in glyphRange) {
                    glyphAdvances[k] += advanceAddition
                }
            }

            val justifiedAdvances = FloatList.of(*glyphAdvances)
            val justifiedRun = JustifiedRun(textRun, justifiedAdvances)

            glyphRun.textRun = justifiedRun
        }

        return createComposedLine(
            spanned, lineStart, lineEnd, breakEnd, runList,
            line.paragraphLevel
        )
    }

    fun createJustifiedLine(
        charStart: Int, charEnd: Int,
        justificationFactor: Float,
        justificationWidth: Float
    ): ComposedLine {
        return createJustifiedLine(
            createSimpleLine(charStart, charEnd),
            justificationFactor, justificationWidth
        )
    }

    private fun computeSpaceCount(startIndex: Int, endIndex: Int): Int {
        var spaceCount = 0

        // FIXME: Exclude replacement runs.

        var i = startIndex
        while (i < endIndex) {
            val spaceStart = spanned.getNextSpace(i, endIndex)
            val spaceEnd = spanned.getLeadingWhitespaceEnd(spaceStart, endIndex)

            spaceCount += spaceEnd - spaceStart
            i = spaceEnd + 1
        }

        return spaceCount
    }
}
