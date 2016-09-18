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
import com.mta.tehreer.opentype.OpenTypeAlbum;

class GlyphRun {

    final Typeface typeface;
    final float fontSize;
    final float sizeByEm;
    final byte bidiLevel;
    final boolean isBackward;
    final int charStart;
    final int charEnd;
    final int[] glyphIds;
    final float[] xOffsets;
    final float[] yOffsets;
    final float[] advances;
    final int[] charToGlyphMap;

    GlyphRun(OpenTypeAlbum album, Typeface typeface, float fontSize, byte bidiLevel) {
        float sizeByEm = fontSize / typeface.getUnitsPerEm();
        int glyphCount = album.getGlyphCount();

        this.typeface = typeface;
        this.fontSize = fontSize;
        this.sizeByEm = sizeByEm;
        this.bidiLevel = bidiLevel;
        this.isBackward = album.isBackward();
        this.charStart = album.getCharStart();
        this.charEnd = album.getCharEnd();
        this.glyphIds = new int[glyphCount];
        this.xOffsets = new float[glyphCount];
        this.yOffsets = new float[glyphCount];
        this.advances = new float[glyphCount];
        this.charToGlyphMap = new int[charEnd - charStart];

        album.copyGlyphInfos(0, glyphCount, sizeByEm, glyphIds, xOffsets, yOffsets, advances);
        album.copyCharGlyphIndexes(charStart, charEnd, charToGlyphMap);
    }

    TextDirection textDirection() {
        return (((bidiLevel & 1) ^ (isBackward ? 1 : 0)) == 0
                ? TextDirection.LEFT_TO_RIGHT
                : TextDirection.RIGHT_TO_LEFT);
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

    float measureGlyphs(int glyphStart, int glyphEnd) {
        float size = 0.0f;

        for (int i = glyphStart; i < glyphEnd; i++) {
            size += advances[i];
        }

        return size;
    }
}
