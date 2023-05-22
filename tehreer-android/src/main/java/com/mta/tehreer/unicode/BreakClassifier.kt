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

package com.mta.tehreer.unicode

import java.text.BreakIterator
import kotlin.experimental.and
import kotlin.experimental.or

private const val BREAK_TYPE_CHARACTER = (1 shl 0).toByte()
private const val BREAK_TYPE_LINE = (1 shl 2).toByte()

internal class BreakClassifier(
    val text: String
) {
    private val breakData = ByteArray(text.length)

    init {
        fillBreaks(BreakIterator.getCharacterInstance(), BREAK_TYPE_CHARACTER)
        fillBreaks(BreakIterator.getLineInstance(), BREAK_TYPE_LINE)
    }

    private fun fillBreaks(iterator: BreakIterator, type: Byte) {
        iterator.setText(text)
        iterator.first()

        var charNext: Int

        while (iterator.next().also { charNext = it } != BreakIterator.DONE) {
            breakData[charNext - 1] = breakData[charNext - 1] or type
        }
    }

    fun getForwardGraphemeBreaks(fromIndex: Int, toIndex: Int) =
        ForwardGraphemeBreakIterator(breakData, fromIndex, toIndex)

    fun getBackwardGraphemeBreaks(fromIndex: Int, toIndex: Int) =
        BackwardGraphemeBreakIterator(breakData, fromIndex, toIndex)

    fun getForwardLineBreaks(fromIndex: Int, toIndex: Int) =
        ForwardLineBreakIterator(breakData, fromIndex, toIndex)

    fun getBackwardLineBreaks(fromIndex: Int, toIndex: Int) =
        BackwardLineBreakIterator(breakData, fromIndex, toIndex)

    class ForwardGraphemeBreakIterator(
        private val breakData: ByteArray,
        startIndex: Int,
        private val endIndex: Int
    ): IntIterator() {
        private var currentIndex = startIndex

        override fun hasNext(): Boolean = currentIndex != endIndex

        override fun nextInt(): Int {
            while (currentIndex < endIndex) {
                val breakType = breakData[currentIndex]
                currentIndex += 1

                if (breakType and BREAK_TYPE_CHARACTER == BREAK_TYPE_CHARACTER) {
                    break
                }
            }

            return currentIndex
        }
    }

    class BackwardGraphemeBreakIterator(
        private val breakData: ByteArray,
        private val startIndex: Int,
        val endIndex: Int
    ): IntIterator() {
        private var currentIndex = endIndex

        override fun hasNext(): Boolean = currentIndex != startIndex

        override fun nextInt(): Int {
            currentIndex -= 1

            while (currentIndex > startIndex) {
                val breakType = breakData[currentIndex - 1]
                if (breakType and BREAK_TYPE_CHARACTER == BREAK_TYPE_CHARACTER) {
                    break
                }

                currentIndex -= 1
            }

            return currentIndex
        }
    }

    class ForwardLineBreakIterator(
        private val breakData: ByteArray,
        startIndex: Int,
        private val endIndex: Int
    ): IntIterator() {
        private var currentIndex = startIndex

        override fun hasNext(): Boolean = currentIndex != endIndex

        override fun nextInt(): Int {
            while (currentIndex < endIndex) {
                val breakType = breakData[currentIndex]
                currentIndex += 1

                if (breakType and BREAK_TYPE_LINE == BREAK_TYPE_LINE) {
                    break
                }
            }

            return currentIndex
        }
    }

    class BackwardLineBreakIterator(
        private val breakData: ByteArray,
        private val startIndex: Int,
        val endIndex: Int
    ): IntIterator() {
        private var currentIndex = endIndex

        override fun hasNext(): Boolean = currentIndex != startIndex

        override fun nextInt(): Int {
            currentIndex -= 1

            while (currentIndex > startIndex) {
                val breakType = breakData[currentIndex - 1]
                if (breakType and BREAK_TYPE_LINE == BREAK_TYPE_LINE) {
                    break
                }

                currentIndex -= 1
            }

            return currentIndex
        }
    }
}
