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

package com.mta.tehreer.internal.layout;

import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.sfnt.WritingDirection;

public class IntrinsicRun {

    public final int charStart;
    public final int charEnd;
    public final boolean isBackward;
    public final byte bidiLevel;
    public final WritingDirection writingDirection;
    public final Typeface typeface;
    public final float typeSize;
    public final float sizeByEm;
    public final int[] glyphIds;
    public final float[] glyphOffsets;
    public final float[] glyphAdvances;
    public final int[] clusterMap;

    public IntrinsicRun(int charStart, int charEnd, boolean isBackward, byte bidiLevel,
                        Typeface typeface, float typeSize, WritingDirection writingDirection,
                        int[] glyphIds, float[] offsets, float[] advances, int[] clusterMap) {
        this.charStart = charStart;
        this.charEnd = charEnd;
        this.isBackward = isBackward;
        this.bidiLevel = bidiLevel;
        this.typeface = typeface;
        this.typeSize = typeSize;
        this.sizeByEm = typeSize / typeface.getUnitsPerEm();
        this.writingDirection = writingDirection;
        this.glyphIds = glyphIds;
        this.glyphOffsets = offsets;
        this.glyphAdvances = advances;
        this.clusterMap = clusterMap;
    }

    public int glyphCount() {
        return glyphIds.length;
    }

    public int charGlyphStart(int charIndex) {
        return clusterMap[charIndex - charStart];
    }

    public int charGlyphEnd(int charIndex) {
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

    public float ascent() {
        return typeface.getAscent() * sizeByEm;
    }

    public float descent() {
        return typeface.getDescent() * sizeByEm;
    }

    public float leading() {
        return typeface.getLeading() * sizeByEm;
    }

    public float measureGlyphs(int glyphStart, int glyphEnd) {
        float size = 0.0f;

        for (int i = glyphStart; i < glyphEnd; i++) {
            size += glyphAdvances[i];
        }

        return size;
    }
}
