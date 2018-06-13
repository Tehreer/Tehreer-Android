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

package com.mta.tehreer.unicode;

public enum BidiClass {
    LEFT_TO_RIGHT,
    RIGHT_TO_LEFT,
    RIGHT_TO_LEFT_ARABIC,
    BOUNDARY_NEUTRAL,
    NON_SPACING_MARK,
    ARABIC_NUMBER,
    EUROPEAN_NUMBER,
    EUROPEAN_NUMBER_TERMINATOR,
    EUROPEAN_NUMBER_SEPARATOR,
    COMMON_NUMBER_SEPARATOR,
    WHITE_SPACE,
    SEGMENT_SEPARATOR,
    PARAGRAPH_SEPARATOR,
    OTHER_NEUTRAL,
    LEFT_TO_RIGHT_ISOLATE,
    RIGHT_TO_LEFT_ISOLATE,
    FIRST_STRONG_ISOLATE,
    POP_DIRECTIONAL_ISOLATE,
    LEFT_TO_RIGHT_EMBEDDING,
    RIGHT_TO_LEFT_EMBEDDING,
    LEFT_TO_RIGHT_OVERRIDE,
    RIGHT_TO_LEFT_OVERRIDE,
    POP_DIRECTIONAL_FORMATTING;

    private static final BidiClass[] all = BidiClass.values();

    static BidiClass valueOf(byte nValue) {
        int index = (nValue - 1) & 0xFF;
        if (index < all.length) {
            return all[index];
        }

        return null;
    }
}
