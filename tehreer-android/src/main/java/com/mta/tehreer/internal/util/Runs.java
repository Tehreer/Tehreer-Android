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

import com.mta.tehreer.internal.layout.IntrinsicRun;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Runs {

    public static int binarySearch(List<IntrinsicRun> runs, final int charIndex) {
        return Collections.binarySearch(runs, null, new Comparator<IntrinsicRun>() {
            @Override
            public int compare(IntrinsicRun obj1, IntrinsicRun obj2) {
                if (charIndex < obj1.charStart) {
                    return 1;
                }

                if (charIndex >= obj1.charEnd) {
                    return -1;
                }

                return 0;
            }
        });
    }

    public static float measureChars(List<IntrinsicRun> runs, int charStart, int charEnd) {
        float measuredWidth = 0.0f;

        if (charEnd > charStart) {
            int runIndex = binarySearch(runs, charStart);

            do {
                IntrinsicRun intrinsicRun = runs.get(runIndex);
                int glyphStart = intrinsicRun.charGlyphStart(charStart);
                int glyphEnd;

                int segmentEnd = Math.min(charEnd, intrinsicRun.charEnd);
                glyphEnd = intrinsicRun.charGlyphEnd(segmentEnd - 1);

                measuredWidth += intrinsicRun.measureGlyphs(glyphStart, glyphEnd);

                charStart = segmentEnd;
                runIndex++;
            } while (charStart < charEnd);
        }

        return measuredWidth;
    }
}
