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

import androidx.annotation.NonNull;
import androidx.annotation.Size;

import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.internal.util.Clusters;
import com.mta.tehreer.sfnt.WritingDirection;

public class IntrinsicRun {
    public final int charStart;
    public final int charEnd;
    public final boolean isBackward;
    public final byte bidiLevel;
    public final @NonNull WritingDirection writingDirection;
    public final @NonNull Typeface typeface;
    public final float typeSize;
    public final float ascent;
    public final float descent;
    public final float leading;
    public final @NonNull int[] glyphIds;
    public final @NonNull float[] glyphOffsets;
    public final @NonNull float[] glyphAdvances;
    public final @NonNull int[] clusterMap;
    public final @NonNull float[] charExtents;

    public IntrinsicRun(int charStart, int charEnd, boolean isBackward, byte bidiLevel,
                        @NonNull WritingDirection writingDirection,
                        @NonNull Typeface typeface, float typeSize,
                        float ascent, float descent, float leading,
                        @NonNull int[] glyphIds,
                        @NonNull float[] offsets, @NonNull float[] advances,
                        @NonNull int[] clusterMap) {
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
        this.charExtents = new float[clusterMap.length];
        Clusters.loadCharExtents(clusterMap, isBackward, isVisuallyRTL(), glyphIds, advances, charExtents);
    }

    public boolean isVisuallyRTL() {
        return (bidiLevel & 1) == 1;
    }

    public int glyphCount() {
        return glyphIds.length;
    }

    public void loadGlyphRange(int startIndex, int endIndex, @NonNull @Size(2) int[] glyphRange) {
        Clusters.loadGlyphRange(clusterMap, startIndex - charStart, endIndex - charStart,
                                isBackward, glyphIds.length, glyphRange);
    }

    public int clusterStart(int charIndex) {
        return Clusters.actualClusterStart(clusterMap, charIndex - charStart) + charStart;
    }

    public int clusterEnd(int charIndex) {
        return Clusters.actualClusterEnd(clusterMap, charIndex - charStart) + charStart;
    }

    public float measureChars(int fromIndex, int toIndex) {
        CaretEdgeList caretEdges = new CaretEdgeList(charExtents, isBackward);
        return caretEdges.distance(fromIndex - charStart, toIndex - charStart, isVisuallyRTL());
    }
}
