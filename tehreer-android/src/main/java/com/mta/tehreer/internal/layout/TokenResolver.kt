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

import com.mta.tehreer.layout.TruncationPlace
import com.mta.tehreer.layout.ComposedLine
import com.mta.tehreer.layout.Typesetter

internal object TokenResolver {
    @JvmStatic
    fun createToken(
        runs: RunCollection,
        charStart: Int, charEnd: Int,
        truncationPlace: TruncationPlace,
        tokenStr: String?
    ): ComposedLine {
        val truncationIndex = when (truncationPlace) {
            TruncationPlace.START -> charStart
            TruncationPlace.MIDDLE -> (charStart + charEnd) / 2
            TruncationPlace.END -> charEnd - 1
        }

        val runIndex = runs.binarySearch(truncationIndex)
        val suitableRun = runs[runIndex]
        val tokenTypeface = suitableRun.typeface
        val tokenTypeSize = suitableRun.typeSize
        var ellipsisStr = tokenStr.orEmpty()

        if (ellipsisStr.isEmpty()) {
            // Token string is not given. Use ellipsis character if available; fallback to three
            // dot characters.

            val ellipsisGlyphId = tokenTypeface.getGlyphId(0x2026)
            ellipsisStr = if (ellipsisGlyphId == 0) "..." else "\u2026"
        }

        val typesetter = Typesetter(ellipsisStr, tokenTypeface, tokenTypeSize)
        return typesetter.createSimpleLine(0, ellipsisStr.length)
    }
}
