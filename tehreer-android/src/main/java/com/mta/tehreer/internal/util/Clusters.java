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

package com.mta.tehreer.internal.util;

import android.support.annotation.NonNull;

import com.mta.tehreer.collections.IntList;

public final class Clusters {
    private static void loadForwardExtents(@NonNull int[] clusterMap, @NonNull int[] glyphIds,
                                           @NonNull float[] glyphAdvances, boolean isRTL,
                                           @NonNull float[] charExtents) {
        int clusterStart = 0;
        int runLength = clusterMap.length;

        int glyphStart = clusterMap[0];
        float distance = 0.0f;

        for (int i = 0; i <= runLength; i++) {
            int glyphIndex = (i < runLength ? clusterMap[i] : glyphIds.length);
            if (glyphIndex == glyphStart) {
                continue;
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
                distance += (isRTL ? -charAdvance : charAdvance);
                charExtents[j] = distance;
            }

            clusterStart = i;
            glyphStart = glyphIndex;
        }
    }

    public static void loadGlyphRange(@NonNull int[] clusterMap, int startIndex, int endIndex,
                                      boolean isBackward, int glyphCount, @NonNull int[] glyphRange) {
        if (!isBackward) {
            glyphRange[0] = clusterMap[startIndex];
            glyphRange[1] = forwardGlyphIndex(clusterMap, endIndex - 1, glyphCount) + 1;
        } else {
            glyphRange[0] = clusterMap[endIndex - 1];
            glyphRange[1] = backwardGlyphIndex(clusterMap, startIndex, glyphCount) + 1;
        }
    }

    public static int leadingGlyphIndex(@NonNull IntList clusterMap, int arrayIndex, boolean isBackward, int glyphCount) {
        if (!isBackward) {
            return clusterMap.get(arrayIndex);
        }

        return backwardGlyphIndex(clusterMap, arrayIndex, glyphCount);
    }

    public static int trailingGlyphIndex(@NonNull IntList clusterMap, int arrayIndex, boolean isBackward, int glyphCount) {
        if (!isBackward) {
            return forwardGlyphIndex(clusterMap, arrayIndex, glyphCount);
        }

        return clusterMap.get(arrayIndex);
    }

    private static int forwardGlyphIndex(@NonNull int[] clusterMap, int arrayIndex, int glyphCount) {
        int common = clusterMap[arrayIndex];
        int length = clusterMap.length;

        for (int i = arrayIndex + 1; i < length; i++) {
            int mapping = clusterMap[i];
            if (mapping != common) {
                return mapping - 1;
            }
        }

        return glyphCount - 1;
    }

    private static int forwardGlyphIndex(@NonNull IntList clusterMap, int arrayIndex, int glyphCount) {
        int common = clusterMap.get(arrayIndex);
        int length = clusterMap.size();

        for (int i = arrayIndex + 1; i < length; i++) {
            int mapping = clusterMap.get(i);
            if (mapping != common) {
                return mapping - 1;
            }
        }

        return glyphCount - 1;
    }

    private static int backwardGlyphIndex(@NonNull int[] clusterMap, int arrayIndex, int glyphCount) {
        int common = clusterMap[arrayIndex];

        for (int i = arrayIndex - 1; i >= 0; i--) {
            int mapping = clusterMap[i];
            if (mapping != common) {
                return mapping - 1;
            }
        }

        return glyphCount - 1;
    }

    private static int backwardGlyphIndex(@NonNull IntList clusterMap, int arrayIndex, int glyphCount) {
        int common = clusterMap.get(arrayIndex);

        for (int i = arrayIndex - 1; i >= 0; i--) {
            int mapping = clusterMap.get(i);
            if (mapping != common) {
                return mapping - 1;
            }
        }

        return glyphCount - 1;
    }

    public static int actualClusterStart(@NonNull int[] clusterMap, int arrayIndex) {
        int common = clusterMap[arrayIndex];

        for (int i = arrayIndex - 1; i >= 0; i--) {
            int mapping = clusterMap[i];
            if (mapping != common) {
                return i + 1;
            }
        }

        return 0;
    }

    public static int actualClusterStart(@NonNull IntList clusterMap, int arrayIndex) {
        int common = clusterMap.get(arrayIndex);

        for (int i = arrayIndex - 1; i >= 0; i--) {
            int mapping = clusterMap.get(i);
            if (mapping != common) {
                return i + 1;
            }
        }

        return 0;
    }

    public static int actualClusterEnd(@NonNull int[] clusterMap, int arrayIndex) {
        int common = clusterMap[arrayIndex];
        int length = clusterMap.length;

        for (int i = arrayIndex + 1; i < length; i++) {
            int mapping = clusterMap[i];
            if (mapping != common) {
                return i;
            }
        }

        return length;
    }

    public static int actualClusterEnd(@NonNull IntList clusterMap, int arrayIndex) {
        int common = clusterMap.get(arrayIndex);
        int length = clusterMap.size();

        for (int i = arrayIndex + 1; i < length; i++) {
            int mapping = clusterMap.get(i);
            if (mapping != common) {
                return i;
            }
        }

        return length;
    }
}
