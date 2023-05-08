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

import com.mta.tehreer.internal.util.StringUtils
import com.mta.tehreer.layout.BreakMode
import java.text.BreakIterator
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.math.max
import kotlin.math.min

internal object BreakResolver {
    private const val ZERO: Byte = 0

    const val LINE = (1 shl 0).toByte()
    const val CHARACTER = (1 shl 2).toByte()
    const val PARAGRAPH = (1 shl 4).toByte()

    fun typeMode(type: Byte, forward: Boolean): Byte {
        return (if (forward) type else type.toInt() shl 1).toByte()
    }

    private fun fillBreaks(text: String, breaks: ByteArray, type: Byte) {
        val iterator = when (type) {
            CHARACTER -> BreakIterator.getCharacterInstance()
            else -> BreakIterator.getLineInstance()
        }

        iterator.setText(text)
        iterator.first()

        val forwardType = typeMode(type, true)
        var charNext: Int

        while (iterator.next().also { charNext = it } != BreakIterator.DONE) {
            breaks[charNext - 1] = breaks[charNext - 1] or forwardType
        }

        iterator.last()
        val backwardType = typeMode(type, false)
        var charIndex: Int

        while (iterator.previous().also { charIndex = it } != BreakIterator.DONE) {
            breaks[charIndex] = breaks[charIndex] or backwardType
        }
    }

    @JvmStatic
    fun fillBreaks(text: String, breaks: ByteArray) {
        fillBreaks(text, breaks, LINE)
        fillBreaks(text, breaks, CHARACTER)
    }

    private fun findForwardBreak(
        text: CharSequence, runs: RunCollection,
        breaks: ByteArray, type: Byte,
        start: Int, end: Int, extent: Float
    ): Int {
        var type = type
        var forwardBreak = start
        var charIndex = start
        var measurement = 0.0f

        val mustType = typeMode(PARAGRAPH, true)
        type = typeMode(type, true)

        while (charIndex < end) {
            val charType = breaks[charIndex]

            // Handle necessary break.
            if (charType and mustType == mustType) {
                val segmentEnd = charIndex + 1

                measurement += runs.measureChars(forwardBreak, segmentEnd)
                if (measurement <= extent) {
                    forwardBreak = segmentEnd
                }
                break
            }

            // Handle optional break.
            if (charType and type == type) {
                val segmentEnd = charIndex + 1

                measurement += runs.measureChars(forwardBreak, segmentEnd)
                if (measurement > extent) {
                    val whitespaceStart =
                        StringUtils.getTrailingWhitespaceStart(text, forwardBreak, segmentEnd)
                    val whitespaceWidth = runs.measureChars(whitespaceStart, segmentEnd)

                    // Break if excluding whitespaces width helps.
                    if (measurement - whitespaceWidth <= extent) {
                        forwardBreak = segmentEnd
                    }
                    break
                }

                forwardBreak = segmentEnd
            }

            charIndex++
        }

        return forwardBreak
    }

    private fun findBackwardBreak(
        text: CharSequence, runs: RunCollection,
        breaks: ByteArray, type: Byte,
        start: Int, end: Int, extent: Float
    ): Int {
        var type = type
        var backwardBreak = end
        var charIndex = end - 1
        var measurement = 0.0f

        val mustType = typeMode(PARAGRAPH, false)
        type = typeMode(type, false)

        while (charIndex >= start) {
            val charType = breaks[charIndex]

            // Handle necessary break.
            if (charType and mustType == mustType) {
                measurement += runs.measureChars(backwardBreak, charIndex)
                if (measurement <= extent) {
                    backwardBreak = charIndex
                }
                break
            }

            // Handle optional break.
            if (charType and type == type) {
                measurement += runs.measureChars(charIndex, backwardBreak)
                if (measurement > extent) {
                    val whitespaceStart =
                        StringUtils.getTrailingWhitespaceStart(text, charIndex, backwardBreak)
                    val whitespaceWidth = runs.measureChars(whitespaceStart, backwardBreak)

                    // Break if excluding trailing whitespaces helps.
                    if (measurement - whitespaceWidth <= extent) {
                        backwardBreak = charIndex
                    }
                    break
                }
                backwardBreak = charIndex
            }

            charIndex--
        }

        return backwardBreak
    }

    @JvmStatic
    fun suggestForwardCharBreak(
        text: CharSequence,
        runs: RunCollection, breaks: ByteArray,
        charStart: Int, charEnd: Int, extent: Float
    ): Int {
        var forwardBreak = findForwardBreak(
            text, runs, breaks, CHARACTER, charStart, charEnd, extent
        )

        // Take at least one character (grapheme) if extent is too small.
        if (forwardBreak == charStart) {
            for (i in charStart until charEnd) {
                if (breaks[i] and CHARACTER != ZERO) {
                    forwardBreak = i + 1
                    break
                }
            }

            // Character range does not cover even a single grapheme?
            if (forwardBreak == charStart) {
                forwardBreak = min(charStart + 1, charEnd)
            }
        }

        return forwardBreak
    }

    @JvmStatic
    fun suggestBackwardCharBreak(
        text: CharSequence,
        runs: RunCollection, breaks: ByteArray,
        start: Int, end: Int, extent: Float
    ): Int {
        var backwardBreak = findBackwardBreak(
            text, runs, breaks, CHARACTER, start, end, extent
        )

        // Take at least one character (grapheme) if extent is too small.
        if (backwardBreak == end) {
            for (i in end - 1 downTo start) {
                if (breaks[i] and CHARACTER != ZERO) {
                    backwardBreak = i
                    break
                }
            }

            // Character range does not cover even a single grapheme?
            if (backwardBreak == end) {
                backwardBreak = max(end - 1, start)
            }
        }

        return backwardBreak
    }

    @JvmStatic
    fun suggestForwardLineBreak(
        text: CharSequence,
        runs: RunCollection, breaks: ByteArray,
        start: Int, end: Int, extent: Float
    ): Int {
        var forwardBreak = findForwardBreak(text, runs, breaks, LINE, start, end, extent)

        // Fallback to character break if no line break occurs in desired extent.
        if (forwardBreak == start) {
            forwardBreak = suggestForwardCharBreak(text, runs, breaks, start, end, extent)
        }

        return forwardBreak
    }

    @JvmStatic
    fun suggestBackwardLineBreak(
        text: CharSequence,
        runs: RunCollection, breaks: ByteArray,
        start: Int, end: Int, extent: Float
    ): Int {
        var backwardBreak = findBackwardBreak(text, runs, breaks, LINE, start, end, extent)

        // Fallback to character break if no line break occurs in desired extent.
        if (backwardBreak == end) {
            backwardBreak = suggestBackwardCharBreak(text, runs, breaks, start, end, extent)
        }

        return backwardBreak
    }

    @JvmStatic
    fun suggestForwardBreak(
        text: CharSequence,
        runs: RunCollection, breaks: ByteArray,
        start: Int, end: Int, extent: Float, mode: BreakMode
    ): Int {
        return when (mode) {
            BreakMode.CHARACTER -> suggestForwardCharBreak(
                text, runs, breaks, start, end, extent
            )
            BreakMode.LINE -> suggestForwardLineBreak(
                text, runs, breaks, start, end, extent
            )
        }
    }

    @JvmStatic
    fun suggestBackwardBreak(
        text: CharSequence,
        runs: RunCollection, breaks: ByteArray,
        start: Int, end: Int, extent: Float, mode: BreakMode
    ): Int {
        return when (mode) {
            BreakMode.CHARACTER -> suggestBackwardCharBreak(
                text, runs, breaks, start, end, extent
            )
            BreakMode.LINE -> suggestBackwardLineBreak(
                text, runs, breaks, start, end, extent
            )
        }
    }
}
