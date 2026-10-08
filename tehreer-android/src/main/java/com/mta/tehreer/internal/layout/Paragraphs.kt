/*
 * Copyright (C) 2026 Muhammad Tayyab Akram
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

/**
 * Finds the paragraphs of a text. A paragraph ends with its separator, which is one of the
 * characters that end a paragraph in the Unicode bidirectional algorithm; a carriage return that
 * is followed by a line feed makes a single separator.
 */
internal object Paragraphs {
    private fun isSeparator(char: Char): Boolean {
        return when (char) {
            '\n', '\r', '\u001C', '\u001D', '\u001E', '\u0085', '\u2029' -> true
            else -> false
        }
    }

    /**
     * Returns the start and the end (exclusive) of each paragraph of a text, one after the other.
     * The end of a paragraph is the start of the next.
     */
    @JvmStatic
    fun boundsOf(text: CharSequence): IntArray {
        val bounds = ArrayList<Int>()
        var start = 0
        var index = 0

        while (index < text.length) {
            val char = text[index++]

            if (isSeparator(char)) {
                if (char == '\r' && index < text.length && text[index] == '\n') {
                    index++
                }

                bounds.add(start)
                bounds.add(index)
                start = index
            }
        }

        if (start < text.length) {
            bounds.add(start)
            bounds.add(text.length)
        }

        return bounds.toIntArray()
    }

    /** Returns the index of the paragraph that has a character, among those of [boundsOf]. */
    @JvmStatic
    fun indexOf(bounds: IntArray, charIndex: Int): Int {
        var low = 0
        var high = bounds.size / 2 - 1

        while (low <= high) {
            val mid = (low + high) ushr 1

            if (charIndex >= bounds[mid * 2 + 1]) {
                low = mid + 1
            } else if (charIndex < bounds[mid * 2]) {
                high = mid - 1
            } else {
                return mid
            }
        }

        return high.coerceIn(0, bounds.size / 2 - 1)
    }
}
