/*
 * Copyright (C) 2017 Muhammad Tayyab Akram
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

package com.mta.tehreer.layout;

import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.sfnt.WritingDirection;
import com.mta.tehreer.sfnt.ShapingResult;

class IntrinsicRun {

    final Typeface typeface;
    final float typeSize;
    final float sizeByEm;
    final byte bidiLevel;
    final WritingDirection writingDirection;
    final boolean isBackward;
    final int charStart;
    final int charEnd;
    final int[] glyphIds;
    final float[] glyphOffsets;
    final float[] glyphAdvances;
    final int[] clusterMap;

    IntrinsicRun(ShapingResult shapingResult, Typeface typeface, float typeSize,
                 byte bidiLevel, WritingDirection writingDirection) {
        float sizeByEm = typeSize / typeface.getUnitsPerEm();

        this.typeface = typeface;
        this.typeSize = typeSize;
        this.sizeByEm = sizeByEm;
        this.bidiLevel = bidiLevel;
        this.writingDirection = writingDirection;
        this.isBackward = shapingResult.isBackward();
        this.charStart = shapingResult.getCharStart();
        this.charEnd = shapingResult.getCharEnd();
        this.glyphIds = shapingResult.getGlyphIds().toArray();
        this.glyphOffsets = shapingResult.getGlyphOffsets().toArray();
        this.glyphAdvances = shapingResult.getGlyphAdvances().toArray();
        this.clusterMap = shapingResult.getClusterMap().toArray();
    }

    WritingDirection writingDirection() {
        return writingDirection;
    }

    int glyphCount() {
        return glyphIds.length;
    }

    int charGlyphStart(int charIndex) {
        return clusterMap[charIndex - charStart];
    }

    int charGlyphEnd(int charIndex) {
        int glyphEnd;

        if (!isBackward) {
            int charNext = charIndex + 1;

            glyphEnd = (charNext < charEnd
                        ? clusterMap[charNext - charStart]
                        : glyphCount());
        } else {
            int charPrevious = charIndex - 1;

            glyphEnd = (charPrevious > charStart
                        ? clusterMap[charPrevious - charStart]
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
