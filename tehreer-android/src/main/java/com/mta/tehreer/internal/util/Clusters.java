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

    public static int getGlyphStart(IntList clusterMap, int arrayIndex) {
        return clusterMap.get(arrayIndex);
    }

    public static int getGlyphEnd(IntList clusterMap, int arrayIndex, boolean isBackward, int glyphCount) {
        int glyphEnd;

        if (!isBackward) {
            int charNext = arrayIndex + 1;

            glyphEnd = (charNext < clusterMap.size()
                        ? clusterMap.get(charNext)
                        : glyphCount);
        } else {
            int charPrevious = arrayIndex - 1;

            glyphEnd = (charPrevious > -1
                        ? clusterMap.get(charPrevious)
                        : glyphCount);
        }

        return glyphEnd;
    }
}
