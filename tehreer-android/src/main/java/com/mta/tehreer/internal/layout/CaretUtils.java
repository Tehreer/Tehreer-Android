/*
 * Copyright (C) 2019 Muhammad Tayyab Akram
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

import com.mta.tehreer.collections.FloatList;

public class CaretUtils {
    public static float getLeftMargin(@NonNull FloatList caretEdges, boolean isRTL,
                                      int firstIndex, int lastIndex) {
        return caretEdges.get(isRTL ? lastIndex : firstIndex);
    }

    public static float getRangeDistance(@NonNull FloatList caretEdges, boolean isRTL,
                                         int firstIndex, int lastIndex) {
        final float firstEdge = caretEdges.get(firstIndex);
        final float lastEdge = caretEdges.get(lastIndex);

        return isRTL ? firstEdge - lastEdge : lastEdge - firstEdge;
    }

    public static int computeNearestIndex(@NonNull FloatList caretEdges, boolean isRTL,
                                          float distance) {
        final int firstIndex = 0;
        final int lastIndex = caretEdges.size() - 1;

        return computeNearestIndex(caretEdges, isRTL, firstIndex, lastIndex, distance);
    }

    public static int computeNearestIndex(@NonNull FloatList caretEdges, boolean isRTL,
                                          int firstIndex, int lastIndex, float distance) {
        final float leftMargin = getLeftMargin(caretEdges, isRTL, firstIndex, lastIndex);

        int leadingIndex = -1;
        int trailingIndex = -1;

        float leadingEdge = 0.0f;
        float trailingEdge = 0.0f;

        int index = (isRTL ? lastIndex : firstIndex);
        int next = (isRTL ? -1 : 1);

        while (index <= lastIndex && index >= firstIndex) {
            float caretEdge = caretEdges.get(index) - leftMargin;

            if (caretEdge <= distance) {
                leadingIndex = index;
                leadingEdge = caretEdge;
            } else {
                trailingIndex = index;
                trailingEdge = caretEdge;
                break;
            }

            index += next;
        }

        if (leadingIndex == -1) {
            // Nothing is covered by the input distance.
            return firstIndex;
        }

        if (trailingIndex == -1) {
            // Whole range is covered by the input distance.
            return lastIndex;
        }

        if (distance <= (leadingEdge + trailingEdge) / 2.0f) {
            // Input distance is closer to first edge.
            return leadingIndex;
        }

        // Input distance is closer to second edge.
        return trailingIndex;
    }

    private CaretUtils() {
    }
}
