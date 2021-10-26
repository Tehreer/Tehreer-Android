/*
 * Copyright (C) 2021 Muhammad Tayyab Akram
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
import androidx.annotation.Nullable;

import com.mta.tehreer.collections.FloatList;
import com.mta.tehreer.collections.IntList;

public final class CaretEdgesBuilder {
    private boolean isBackward;
    private boolean isRTL;
    private FloatList glyphAdvances;
    private IntList clusterMap;
    private @Nullable boolean[] caretStops;

    public @NonNull CaretEdgesBuilder setBackward(boolean backward) {
        this.isBackward = backward;
        return this;
    }

    public @NonNull CaretEdgesBuilder setRTL(boolean RTL) {
        this.isRTL = RTL;
        return this;
    }

    public @NonNull CaretEdgesBuilder setGlyphAdvances(FloatList glyphAdvances) {
        this.glyphAdvances = glyphAdvances;
        return this;
    }

    public @NonNull CaretEdgesBuilder setClusterMap(IntList clusterMap) {
        this.clusterMap = clusterMap;
        return this;
    }

    public @NonNull CaretEdgesBuilder setCaretStops(@Nullable boolean[] caretStops) {
        this.caretStops = caretStops;
        return this;
    }

    private @NonNull float[] buildCaretAdvances() {
        final int codeUnitCount = clusterMap.size();
        float[] caretAdvances = new float[codeUnitCount + 1];

        int glyphIndex = clusterMap.get(0) + 1;
        int refIndex = glyphIndex;
        int totalStops = 0;
        int clusterStart = 0;

        for (int codeUnitIndex = 1; codeUnitIndex <= codeUnitCount; codeUnitIndex++) {
            int oldIndex = glyphIndex;

            if (codeUnitIndex != codeUnitCount) {
                glyphIndex = clusterMap.get(codeUnitIndex) + 1;

                if (caretStops != null && !caretStops[codeUnitIndex - 1]) {
                    continue;
                }

                totalStops += 1;
            } else {
                totalStops += 1;
                glyphIndex = (isBackward ? 0 : glyphAdvances.size() + 1);
            }

            if (glyphIndex != oldIndex) {
                float clusterAdvance = 0;
                float distance = 0;
                int counter = 1;

                // Find the advance of current cluster.
                if (isBackward) {
                    while (refIndex > glyphIndex) {
                        clusterAdvance += glyphAdvances.get(refIndex - 1);
                        refIndex -= 1;
                    }
                } else {
                    while (refIndex < glyphIndex) {
                        clusterAdvance += glyphAdvances.get(refIndex - 1);
                        refIndex += 1;
                    }
                }

                // Divide the advance evenly between cluster length.
                while (clusterStart < codeUnitIndex) {
                    float advance = 0;

                    if (caretStops == null || caretStops[clusterStart] || clusterStart == codeUnitCount - 1) {
                        float previous = distance;

                        distance = (clusterAdvance * counter) / totalStops;
                        advance = distance - previous;
                        counter += 1;
                    }

                    caretAdvances[clusterStart] = advance;
                    clusterStart += 1;
                }

                totalStops = 0;
            }
        }

        return caretAdvances;
    }

    public @NonNull FloatList build() {
        final int codeUnitCount = clusterMap.size();
        float[] caretEdges = buildCaretAdvances();
        float distance = 0;

        if (isRTL) {
            // Last edge should be zero.
            caretEdges[codeUnitCount] = 0;

            // Iterate in reverse direction.
            for (int i = codeUnitCount - 1; i >= 0; i--) {
                distance += caretEdges[i];
                caretEdges[i] = distance;
            }
        } else {
            float advance = caretEdges[0];

            // First edge should be zero.
            caretEdges[0] = 0;

            for (int i = 1; i <= codeUnitCount; i++) {
                distance += advance;
                advance = caretEdges[i];
                caretEdges[i] = distance;
            }
        }

        return FloatList.of(caretEdges);
    }
}
