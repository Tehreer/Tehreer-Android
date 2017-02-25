/*
 * Copyright (C) 2016 Muhammad Tayyab Akram
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

package com.mta.tehreer.text;

import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.opentype.ShapingDirection;
import com.mta.tehreer.opentype.ShapingResult;

class GlyphRun {

    final Typeface typeface;
    final float fontSize;
    final float sizeByEm;
    final byte bidiLevel;
    final boolean isBackward;
    final int charStart;
    final int charEnd;
    final int[] glyphIds;
    final float[] glyphOffsets;
    final float[] glyphAdvances;
    final int[] charToGlyphMap;

    GlyphRun(ShapingResult album, Typeface typeface, float fontSize, byte bidiLevel) {
        float sizeByEm = fontSize / typeface.getUnitsPerEm();

        this.typeface = typeface;
        this.fontSize = fontSize;
        this.sizeByEm = sizeByEm;
        this.bidiLevel = bidiLevel;
        this.isBackward = album.isBackward();
        this.charStart = album.getCharStart();
        this.charEnd = album.getCharEnd();
        this.glyphIds = album.getGlyphIds().toArray();
        this.glyphOffsets = album.getGlyphOffsets().toArray();
        this.glyphAdvances = album.getGlyphAdvances().toArray();
        this.charToGlyphMap = album.getCharToGlyphMap().toArray();
    }

    ShapingDirection textDirection() {
        return (((bidiLevel & 1) ^ (isBackward ? 1 : 0)) == 0
                ? ShapingDirection.LEFT_TO_RIGHT
                : ShapingDirection.RIGHT_TO_LEFT);
    }

    int glyphCount() {
        return glyphIds.length;
    }

    int charGlyphStart(int charIndex) {
        return charToGlyphMap[charIndex - charStart];
    }

    int charGlyphEnd(int charIndex) {
        int glyphEnd;

        if (!isBackward) {
            int charNext = charIndex + 1;

            glyphEnd = (charNext < charEnd
                        ? charToGlyphMap[charNext - charStart]
                        : glyphCount());
        } else {
            int charPrevious = charIndex - 1;

            glyphEnd = (charPrevious > charStart
                        ? charToGlyphMap[charPrevious - charStart]
                        : glyphCount());
        }

        return glyphEnd;
    }

    float ascent() {
        return typeface.getAscent() * sizeByEm;
    }

    float descent() {
        return typeface.getDescent() * sizeByEm;
    }

    float leading() {
        return typeface.getLeading() * sizeByEm;
    }

    float measureGlyphs(int glyphStart, int glyphEnd) {
        float size = 0.0f;

        for (int i = glyphStart; i < glyphEnd; i++) {
            size += glyphAdvances[i];
        }

        return size;
    }
}
