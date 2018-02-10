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
import java.util.Collections;
import java.util.Comparator;

public class RunCollection extends ArrayList<IntrinsicRun> {

    public int binarySearch(final int charIndex) {
        return Collections.binarySearch(this, null, new Comparator<IntrinsicRun>() {
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
