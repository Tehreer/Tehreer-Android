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

import com.mta.tehreer.unicode.BidiLine;
import com.mta.tehreer.unicode.BidiParagraph;
import com.mta.tehreer.unicode.BidiRun;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ParagraphCollection extends ArrayList<BidiParagraph> {

    public int binarySearch(final int charIndex) {
        return Collections.binarySearch(this, null, new Comparator<BidiParagraph>() {
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

    public byte charLevel(int charIndex) {
        int paragraphIndex = binarySearch(charIndex);
        BidiParagraph charParagraph = get(paragraphIndex);

        return charParagraph.getBaseLevel();
    }

    public interface RunConsumer {
        void accept(BidiRun bidiRun);
    }

    public void forEachLineRun(int lineStart, int lineEnd, RunConsumer runConsumer) {
        int paragraphIndex = binarySearch(lineStart);
        int feasibleStart;
        int feasibleEnd;

        do {
            BidiParagraph bidiParagraph = get(paragraphIndex);
            feasibleStart = Math.max(bidiParagraph.getCharStart(), lineStart);
            feasibleEnd = Math.min(bidiParagraph.getCharEnd(), lineEnd);

            BidiLine bidiLine = bidiParagraph.createLine(feasibleStart, feasibleEnd);
            for (BidiRun bidiRun : bidiLine.getVisualRuns()) {
                runConsumer.accept(bidiRun);
            }
            bidiLine.dispose();

            paragraphIndex++;
        } while (feasibleEnd != lineEnd);
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            int size = size();
            for (int i = 0; i < size; i++) {
                BidiParagraph paragraph = get(i);
                paragraph.dispose();
            }
        } finally {
            super.finalize();
        }
    }
}
