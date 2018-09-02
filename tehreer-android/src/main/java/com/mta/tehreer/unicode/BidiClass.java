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

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Represents the bidirectional class of a character in Unicode specification. The constants
 * correspond to the values defined in
 * <a href="https://unicode.org/reports/tr44/#Bidi_Class_Values">
 *     Unicode Standard Annex #44: Bidirectional Class Values
 * </a>.
 */
public final class BidiClass {
    @IntDef({
        LEFT_TO_RIGHT,
        RIGHT_TO_LEFT,
        ARABIC_LETTER,
        BOUNDARY_NEUTRAL,
        NONSPACING_MARK,
        ARABIC_NUMBER,
        EUROPEAN_NUMBER,
        EUROPEAN_TERMINATOR,
        EUROPEAN_SEPARATOR,
        COMMON_SEPARATOR,
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
        POP_DIRECTIONAL_FORMAT,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Value { }

    /**
     * Bidi Class: "Left_To_Right".
     */
    public static final int LEFT_TO_RIGHT = 0x01;

    /**
     * Bidi Class: "Right_To_Left".
     */
    public static final int RIGHT_TO_LEFT = 0x02;

    /**
     * Bidi Class: "Arabic_Letter".
     */
    public static final int ARABIC_LETTER = 0x03;

    /**
     * Bidi Class: "Boundary_Neutral".
     */
    public static final int BOUNDARY_NEUTRAL = 0x04;

    /**
     * Bidi Class: "Nonspacing_Mark".
     */
    public static final int NONSPACING_MARK = 0x05;

    /**
     * Bidi Class: "Arabic_Number".
     */
    public static final int ARABIC_NUMBER = 0x06;

    /**
     * Bidi Class: "European_Number".
     */
    public static final int EUROPEAN_NUMBER = 0x07;

    /**
     * Bidi Class: "European_Terminator".
     */
    public static final int EUROPEAN_TERMINATOR = 0x08;

    /**
     * Bidi Class: "European_Separator".
     */
    public static final int EUROPEAN_SEPARATOR = 0x09;

    /**
     * Bidi Class: "Common_Separator".
     */
    public static final int COMMON_SEPARATOR = 0x0A;

    /**
     * Bidi Class: "White_Space".
     */
    public static final int WHITE_SPACE = 0x0B;

    /**
     * Bidi Class: "Segment_Separator".
     */
    public static final int SEGMENT_SEPARATOR = 0x0C;

    /**
     * Bidi Class: "Paragraph_Separator".
     */
    public static final int PARAGRAPH_SEPARATOR = 0x0D;

    /**
     * Bidi Class: "Other_Neutral".
     */
    public static final int OTHER_NEUTRAL = 0x0E;

    /**
     * Bidi Class: "Left_To_Right_Isolate".
     */
    public static final int LEFT_TO_RIGHT_ISOLATE = 0x0F;

    /**
     * Bidi Class: "Right_To_Left_Isolate".
     */
    public static final int RIGHT_TO_LEFT_ISOLATE = 0x10;

    /**
     * Bidi Class: "First_Strong_Isolate".
     */
    public static final int FIRST_STRONG_ISOLATE = 0x11;

    /**
     * Bidi Class: "Pop_Directional_Isolate".
     */
    public static final int POP_DIRECTIONAL_ISOLATE = 0x12;

    /**
     * Bidi Class: "Left_To_Right_Embedding".
     */
    public static final int LEFT_TO_RIGHT_EMBEDDING = 0x13;

    /**
     * Bidi Class: "Right_To_Left_Embedding".
     */
    public static final int RIGHT_TO_LEFT_EMBEDDING = 0x14;

    /**
     * Bidi Class: "Left_To_Right_Override".
     */
    public static final int LEFT_TO_RIGHT_OVERRIDE = 0x15;

    /**
     * Bidi Class: "Right_To_Left_Override".
     */
    public static final int RIGHT_TO_LEFT_OVERRIDE = 0x16;

    /**
     * Bidi Class: "Pop_Directional_Format".
     */
    public static final int POP_DIRECTIONAL_FORMAT = 0x17;

    private BidiClass() {
    }
}
