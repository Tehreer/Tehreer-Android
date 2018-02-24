/*
 * Copyright (C) 2018 Muhammad Tayyab Akram
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

package com.mta.tehreer.internal.layout;

import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.layout.ComposedLine;
import com.mta.tehreer.layout.TruncationPlace;
import com.mta.tehreer.layout.Typesetter;

public class TokenResolver {

    public static ComposedLine createToken(RunCollection runs, int charStart, int charEnd,
                                           TruncationPlace truncationPlace, String tokenStr) {
        int truncationIndex = 0;

        switch (truncationPlace) {
        case START:
            truncationIndex = charStart;
            break;

        case MIDDLE:
            truncationIndex = (charStart + charEnd) / 2;
            break;

        case END:
            truncationIndex = charEnd - 1;
            break;
        }

        int runIndex = runs.binarySearch(truncationIndex);
        IntrinsicRun suitableRun = runs.get(runIndex);
        Typeface tokenTypeface = suitableRun.typeface;
        float tokenTypeSize = suitableRun.typeSize;

        if (tokenStr == null || tokenStr.length() == 0) {
            // Token string is not given. Use ellipsis character if available; fallback to three
            // dot characters.

            int ellipsisGlyphId = tokenTypeface.getGlyphId(0x2026);
            if (ellipsisGlyphId == 0) {
                tokenStr = "...";
            } else {
                tokenStr = "\u2026";
            }
        }

        Typesetter typesetter = new Typesetter(tokenStr, tokenTypeface, tokenTypeSize);
        return typesetter.createSimpleLine(0, tokenStr.length());
    }
}
