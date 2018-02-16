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

package com.mta.tehreer.internal.layout;

import java.util.ArrayList;

public class RunCollection extends ArrayList<IntrinsicRun> {

    public int binarySearch(int charIndex) {
        int low = 0;
        int high = size() - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            IntrinsicRun value = get(mid);

            if (charIndex >= value.charEnd) {
                low = mid + 1;
            } else if (charIndex < value.charStart) {
                high = mid - 1;
            } else {
                return mid;
            }
        }

        return -(low + 1);
    }

    public float measureChars(int charStart, int charEnd) {
        float extent = 0.0f;

        if (charEnd > charStart) {
            int runIndex = binarySearch(charStart);

            do {
                IntrinsicRun intrinsicRun = get(runIndex);
                int glyphStart = intrinsicRun.charGlyphStart(charStart);
                int glyphEnd;

                int segmentEnd = Math.min(charEnd, intrinsicRun.charEnd);
                glyphEnd = intrinsicRun.charGlyphEnd(segmentEnd - 1);

                extent += intrinsicRun.measureGlyphs(glyphStart, glyphEnd);

                charStart = segmentEnd;
                runIndex++;
            } while (charStart < charEnd);
        }

        return extent;
    }
}
