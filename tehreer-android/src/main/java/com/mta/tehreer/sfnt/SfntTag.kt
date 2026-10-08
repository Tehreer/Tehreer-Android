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

package com.mta.tehreer.sfnt

import androidx.annotation.Size

/**
 * Provides static utility methods related to SFNT tags.
 */
object SfntTag {
    private fun verifyChar(char: Char, message: String) {
        require(char in ' '..'~') { message }
    }

    /**
     * Makes a four-byte integer, representing the passed-in tag as a string.
     *
     * @param tagStr The tag string to represent as an integer.
     * @return Integer representation of the tag.
     *
     * @throws IllegalArgumentException if `tagStr` is not four characters long, or any character is
     *         not a printing character represented by ASCII values 32-126.
     */
    fun make(@Size(4) tagStr: String): Int {
        require(tagStr.length == 4) { "The length of tag string is not equal to four" }

        for (i in 0 until 4) {
            verifyChar(tagStr[i], "Index: $i")
        }

        return (tagStr[0].code shl 24) or (tagStr[1].code shl 16) or (tagStr[2].code shl 8) or tagStr[3].code
    }

    /**
     * Returns the string representation of a tag.
     *
     * @param tag The tag.
     * @return The string representation of specified tag.
     */
    fun toString(tag: Int): String {
        return charArrayOf(
            (tag shr 24).toChar(),
            ((tag shr 16) and 0xFF).toChar(),
            ((tag shr 8) and 0xFF).toChar(),
            (tag and 0xFF).toChar()
        ).concatToString()
    }
}
