/*
 * Copyright (C) 2016 Muhammad Tayyab Akram
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

package com.mta.tehreer.text;

public enum TextTruncation {
    WORD_START,
    WORD_MIDDLE,
    WORD_END,
    CHARACTER_START,
    CHARACTER_MIDDLE,
    CHARACTER_END;

    private static final int MASK_PLACE = 0x0F;
    private static final int MASK_MODE = 0xF0;

    static final int PLACE_START = 1;
    static final int PLACE_MIDDLE = 2;
    static final int PLACE_END = 3;

    static final int MODE_WORD = 1 << 4;
    static final int MODE_CHARACTER = 1 << 5;

    private int options;

    static {
        WORD_START.options = PLACE_START | MODE_WORD;
        WORD_MIDDLE.options = PLACE_MIDDLE | MODE_WORD;
        WORD_END.options = PLACE_END | MODE_WORD;
        CHARACTER_START.options = PLACE_START | MODE_CHARACTER;
        CHARACTER_MIDDLE.options = PLACE_MIDDLE | MODE_CHARACTER;
        CHARACTER_END.options = PLACE_END | MODE_CHARACTER;
    }

    int getPlace() {
        return (options & MASK_PLACE);
    }

    int getMode() {
        return (options & MASK_MODE);
    }
}
