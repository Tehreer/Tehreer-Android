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

import com.mta.tehreer.collections.IntList;

public class Clusters {

    public static int leadingGlyphIndex(IntList clusterMap, int arrayIndex) {
        return clusterMap.get(arrayIndex);
    }

    public static int trailingGlyphIndex(IntList clusterMap, int arrayIndex, boolean isBackward, int glyphCount) {
        if (!isBackward) {
            return forwardTrailing(clusterMap, arrayIndex, glyphCount);
        }

        return backwardTrailing(clusterMap, arrayIndex, glyphCount);
    }

    private static int forwardTrailing(IntList clusterMap, int arrayIndex, int glyphCount) {
        int leading = clusterMap.get(arrayIndex);
        int length = clusterMap.size();

        for (int i = arrayIndex + 1; i < length; i++) {
            int mapping = clusterMap.get(i);
            if (mapping != leading) {
                return mapping;
            }
        }

        return glyphCount;
    }

    private static int backwardTrailing(IntList clusterMap, int arrayIndex, int glyphCount) {
        int leading = clusterMap.get(arrayIndex);

        for (int i = arrayIndex - 1; i >= 0; i--) {
            int mapping = clusterMap.get(i);
            if (mapping != leading) {
                return mapping;
            }
        }

        return glyphCount;
    }
}
