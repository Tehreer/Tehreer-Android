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

import android.text.Spanned;

import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.layout.ComposedLine;
import com.mta.tehreer.layout.TruncationPlace;
import com.mta.tehreer.layout.Typesetter;
import com.mta.tehreer.layout.style.TypeSizeSpan;
import com.mta.tehreer.layout.style.TypefaceSpan;

public class TokenResolver {

    public static ComposedLine createToken(Spanned spanned, int charStart, int charEnd,
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

        Object[] charSpans = spanned.getSpans(truncationIndex, truncationIndex + 1, Object.class);
        TypefaceSpan typefaceSpan = null;
        TypeSizeSpan typeSizeSpan = null;

        final int typefaceBit = 1;
        final int typeSizeBit = 1 << 1;
        final int requiredBits = typefaceBit | typeSizeBit;
        int foundBits = 0;

        for (Object span : charSpans) {
            if (span instanceof TypefaceSpan) {
                if (typefaceSpan == null) {
                    typefaceSpan = (TypefaceSpan) span;
                    foundBits |= typefaceBit;
                }
            } else if (span instanceof TypeSizeSpan) {
                if (typeSizeSpan == null) {
                    typeSizeSpan = (TypeSizeSpan) span;
                    foundBits |= typeSizeBit;
                }
            }

            if (foundBits == requiredBits) {
                Typeface tokenTypeface = typefaceSpan.getTypeface();
                float tokenTypeSize = typeSizeSpan.getSize();

                if (tokenStr == null || tokenStr.length() == 0) {
                    // Token string is not given. Use ellipsis character if available; fallback to
                    // three dots.

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

        return null;
    }
}
