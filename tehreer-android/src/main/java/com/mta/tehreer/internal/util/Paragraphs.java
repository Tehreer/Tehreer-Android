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

import com.mta.tehreer.unicode.BidiLine;
import com.mta.tehreer.unicode.BidiParagraph;
import com.mta.tehreer.unicode.BidiRun;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Paragraphs {

    public interface RunConsumer {
        void accept(BidiRun bidiRun);
    }

    public static int binarySearch(List<BidiParagraph> paragraphs, final int charIndex) {
        return Collections.binarySearch(paragraphs, null, new Comparator<BidiParagraph>() {
            @Override
            public int compare(BidiParagraph obj1, BidiParagraph obj2) {
                if (charIndex < obj1.getCharStart()) {
                    return 1;
                }

                if (charIndex >= obj1.getCharEnd()) {
                    return -1;
                }

                return 0;
            }
        });
    }

    public static byte levelOfChar(List<BidiParagraph> paragraphs, int charIndex) {
        int paragraphIndex = binarySearch(paragraphs, charIndex);
        BidiParagraph charParagraph = paragraphs.get(paragraphIndex);
        return charParagraph.getBaseLevel();
    }

    public static void iterateLineRuns(List<BidiParagraph> paragraphs,
                                       int charStart, int charEnd, RunConsumer runConsumer) {
        int paragraphIndex = binarySearch(paragraphs, charStart);
        int feasibleStart;
        int feasibleEnd;

        do {
            BidiParagraph bidiParagraph = paragraphs.get(paragraphIndex);
            feasibleStart = Math.max(bidiParagraph.getCharStart(), charStart);
            feasibleEnd = Math.min(bidiParagraph.getCharEnd(), charEnd);

            BidiLine bidiLine = bidiParagraph.createLine(feasibleStart, feasibleEnd);
            for (BidiRun bidiRun : bidiLine.getVisualRuns()) {
                runConsumer.accept(bidiRun);
            }
            bidiLine.dispose();

            paragraphIndex++;
        } while (feasibleEnd != charEnd);
    }
}
