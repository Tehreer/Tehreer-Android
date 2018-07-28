/*
 * Copyright (C) 2016-2018 Muhammad Tayyab Akram
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
    public final float ascent;
    public final float descent;
    public final float leading;
    public final int[] glyphIds;
    public final float[] glyphOffsets;
    public final float[] glyphAdvances;
    public final int[] clusterMap;
    public final float[] charExtents;

    public IntrinsicRun(int charStart, int charEnd, boolean isBackward, byte bidiLevel,
                        WritingDirection writingDirection, Typeface typeface, float typeSize,
                        float ascent, float descent, float leading,
                        int[] glyphIds, float[] offsets, float[] advances, int[] clusterMap) {
        this.charStart = charStart;
        this.charEnd = charEnd;
        this.isBackward = isBackward;
        this.bidiLevel = bidiLevel;
        this.writingDirection = writingDirection;
        this.typeface = typeface;
        this.typeSize = typeSize;
        this.ascent = ascent;
        this.descent = descent;
        this.leading = leading;
        this.glyphIds = glyphIds;
        this.glyphOffsets = offsets;
        this.glyphAdvances = advances;
        this.clusterMap = clusterMap;
        this.charExtents = buildCharExtents();
    }

    private float[] buildCharExtents() {
        int length = charEnd - charStart;
        float[] array = new float[length];
        float distance = 0.0f;

        int clusterStart = 0;
        int glyphStart = clusterMap[0];

        for (int i = 0; i <= length; i++) {
            int glyphIndex = (i < length ? clusterMap[i] : !isBackward ? glyphIds.length : 0);
            if (glyphIndex == glyphStart) {
                continue;
            }

            if (glyphStart > glyphIndex) {
                int tempIndex = glyphIndex;
                glyphIndex = glyphStart;
                glyphStart = tempIndex;
            }

            // Find the advance of current cluster.
            float clusterAdvance = 0.0f;
            for (int j = glyphStart; j < glyphIndex; j++) {
                clusterAdvance += glyphAdvances[j];
            }

            // Divide the advance evenly between cluster length.
            int clusterLength = i - clusterStart;
            float charAdvance = clusterAdvance / clusterLength;

            for (int j = clusterStart; j < i; j++) {
                distance += charAdvance;
                array[j] = distance;
            }

            clusterStart = i;
            glyphStart = glyphIndex;
        }

        return array;
    }

    public boolean isOpposite() {
        return (!isBackward && writingDirection == WritingDirection.RIGHT_TO_LEFT)
             | (isBackward && writingDirection == WritingDirection.LEFT_TO_RIGHT);
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

    public float measureGlyphs(int glyphStart, int glyphEnd) {
        float size = 0.0f;

        for (int i = glyphStart; i < glyphEnd; i++) {
            size += glyphAdvances[i];
        }

        return size;
    }
}
