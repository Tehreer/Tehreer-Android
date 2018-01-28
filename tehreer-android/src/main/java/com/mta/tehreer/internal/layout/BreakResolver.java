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

import com.mta.tehreer.internal.util.Runs;
import com.mta.tehreer.internal.util.StringUtils;

import java.text.BreakIterator;
import java.util.List;

public class BreakResolver {

    public static final byte LINE = 1 << 0;
    public static final byte CHARACTER = 1 << 2;
    public static final byte PARAGRAPH = 1 << 4;

    public static byte typeMode(byte type, boolean forward) {
        return (byte) (forward ? type : type << 1);
    }

    private static void fillBreaks(String text, byte[] breaks, byte type) {
        BreakIterator iterator;

        switch (type) {
        case CHARACTER:
            iterator = BreakIterator.getCharacterInstance();
            break;

        default:
            iterator = BreakIterator.getLineInstance();
            break;
        }

        iterator.setText(text);
        iterator.first();

        byte forwardType = typeMode(type, true);
        int charNext;

        while ((charNext = iterator.next()) != BreakIterator.DONE) {
            breaks[charNext - 1] |= forwardType;
        }

        iterator.last();
        byte backwardType = typeMode(type, false);
        int charIndex;

        while ((charIndex = iterator.previous()) != BreakIterator.DONE) {
            breaks[charIndex] |= backwardType;
        }
    }

    public static void fillBreaks(String text, byte[] breaks) {
        BreakResolver.fillBreaks(text, breaks, BreakResolver.LINE);
        BreakResolver.fillBreaks(text, breaks, BreakResolver.CHARACTER);
    }

    private static int findForwardBreak(String text, List<IntrinsicRun> runs, byte[] breaks,
                                        byte type, int start, int end, float extent) {
        int forwardBreak = start;
        int charIndex = start;
        float measurement = 0.0f;

        byte mustType = typeMode(PARAGRAPH, true);
        type = typeMode(type, true);

        while (charIndex < end) {
            byte charType = breaks[charIndex];

            // Handle necessary break.
            if ((charType & mustType) == mustType) {
                int segmentEnd = charIndex + 1;

                measurement += Runs.measureChars(runs, forwardBreak, segmentEnd);
                if (measurement <= extent) {
                    forwardBreak = segmentEnd;
                }
                break;
            }

            // Handle optional break.
            if ((charType & type) == type) {
                int segmentEnd = charIndex + 1;

                measurement += Runs.measureChars(runs, forwardBreak, segmentEnd);
                if (measurement > extent) {
                    int whitespaceStart = StringUtils.getTrailingWhitespaceStart(text, forwardBreak, segmentEnd);
                    float whitespaceWidth = Runs.measureChars(runs, whitespaceStart, segmentEnd);

                    // Break if excluding whitespaces width helps.
                    if ((measurement - whitespaceWidth) <= extent) {
                        forwardBreak = segmentEnd;
                    }
                    break;
                }

                forwardBreak = segmentEnd;
            }

            charIndex++;
        }

        return forwardBreak;
    }

    private static int findBackwardBreak(String text, List<IntrinsicRun> runs, byte[] breaks,
                                         byte type, int start, int end, float extent) {
        int backwardBreak = end;
        int charIndex = end - 1;
        float measurement = 0.0f;

        byte mustType = typeMode(PARAGRAPH, false);
        type = typeMode(type, false);

        while (charIndex >= start) {
            byte charType = breaks[charIndex];

            // Handle necessary break.
            if ((charType & mustType) == mustType) {
                measurement += Runs.measureChars(runs, backwardBreak, charIndex);
                if (measurement <= extent) {
                    backwardBreak = charIndex;
                }
                break;
            }

            // Handle optional break.
            if ((charType & type) == type) {
                measurement += Runs.measureChars(runs, charIndex, backwardBreak);
                if (measurement > extent) {
                    int whitespaceStart = StringUtils.getTrailingWhitespaceStart(text, charIndex, backwardBreak);
                    float whitespaceWidth = Runs.measureChars(runs, whitespaceStart, backwardBreak);

                    // Break if excluding trailing whitespaces helps.
                    if ((measurement - whitespaceWidth) <= extent) {
                        backwardBreak = charIndex;
                    }
                    break;
                }

                backwardBreak = charIndex;
            }

            charIndex--;
        }

        return backwardBreak;
    }

    public static int suggestForwardCharBreak(String text, List<IntrinsicRun> runs, byte[] breaks,
                                              int charStart, int charEnd, float extent) {
        int forwardBreak = findForwardBreak(text, runs, breaks, CHARACTER, charStart, charEnd, extent);

        // Take at least one character (grapheme) if extent is too small.
        if (forwardBreak == charStart) {
            for (int i = charStart; i < charEnd; i++) {
                if ((breaks[i] & CHARACTER) != 0) {
                    forwardBreak = i + 1;
                    break;
                }
            }

            // Character range does not cover even a single grapheme?
            if (forwardBreak == charStart) {
                forwardBreak = Math.min(charStart + 1, charEnd);
            }
        }

        return forwardBreak;
    }

    public static int suggestBackwardCharBreak(String text, List<IntrinsicRun> runs, byte[] breaks,
                                               int start, int end, float extent) {
        int backwardBreak = findBackwardBreak(text, runs, breaks, CHARACTER, start, end, extent);

        // Take at least one character (grapheme) if extent is too small.
        if (backwardBreak == end) {
            for (int i = end - 1; i >= start; i++) {
                if ((breaks[i] & CHARACTER) != 0) {
                    backwardBreak = i;
                    break;
                }
            }

            // Character range does not cover even a single grapheme?
            if (backwardBreak == end) {
                backwardBreak = Math.max(end - 1, start);
            }
        }

        return backwardBreak;
    }

    public static int suggestForwardLineBreak(String text, List<IntrinsicRun> runs, byte[] breaks,
                                              int start, int end, float extent) {
        int forwardBreak = findForwardBreak(text, runs, breaks, LINE, start, end, extent);

        // Fallback to character break if no line break occurs in desired extent.
        if (forwardBreak == start) {
            forwardBreak = suggestForwardCharBreak(text, runs, breaks, start, end, extent);
        }

        return forwardBreak;
    }

    public static int suggestBackwardLineBreak(String text, List<IntrinsicRun> runs, byte[] breaks,
                                               int start, int end, float extent) {
        int backwardBreak = findBackwardBreak(text, runs, breaks, LINE, start, end, extent);

        // Fallback to character break if no line break occurs in desired extent.
        if (backwardBreak == end) {
            backwardBreak = suggestBackwardCharBreak(text, runs, breaks, start, end, extent);
        }

        return backwardBreak;
    }
}
