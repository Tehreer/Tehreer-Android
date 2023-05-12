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

package com.mta.tehreer.internal.util

import android.text.GetChars

internal fun CharSequence.getLeadingWhitespaceEnd(charStart: Int, charEnd: Int): Int {
    for (i in charStart until charEnd) {
        if (!Character.isWhitespace(this[i])) {
            return i
        }
    }

    return charEnd
}

internal fun CharSequence.getTrailingWhitespaceStart(charStart: Int, charEnd: Int): Int {
    for (i in charEnd - 1 downTo charStart) {
        if (!Character.isWhitespace(this[i])) {
            return i + 1
        }
    }

    return charStart
}

internal fun CharSequence.getNextSpace(charStart: Int, charEnd: Int): Int {
    for (i in charStart until charEnd) {
        if (Character.isWhitespace(this[i])) {
            return i
        }
    }

    return charEnd
}

internal object StringUtils {
    @JvmStatic
    fun copyString(charSequence: CharSequence): String {
        val length = charSequence.length
        val chars = CharArray(length)

        if (charSequence is GetChars) {
            charSequence.getChars(0, length, chars, 0)
        } else {
            for (i in 0 until length) {
                chars[i] = charSequence[i]
            }
        }

        return String(chars)
    }
}
