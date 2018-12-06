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

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Represents the general category of a character in Unicode specification. The constants correspond
 * to the values defined in
 * <a href="https://unicode.org/reports/tr44/#General_Category_Values">
 *     Unicode Standard Annex #44: General Category Values
 * </a>.
 */
public final class GeneralCategory {
    /** @hide */
    @IntDef({
        UPPERCASE_LETTER,
        LOWERCASE_LETTER,
        TITLECASE_LETTER,
        MODIFIER_LETTER,
        OTHER_LETTER,
        NONSPACING_MARK,
        SPACING_MARK,
        ENCLOSING_MARK,
        DECIMAL_NUMBER,
        LETTER_NUMBER,
        OTHER_NUMBER,
        CONNECTOR_PUNCTUATION,
        DASH_PUNCTUATION,
        OPEN_PUNCTUATION,
        CLOSE_PUNCTUATION,
        INITIAL_PUNCTUATION,
        FINAL_PUNCTUATION,
        OTHER_PUNCTUATION,
        MATH_SYMBOL,
        CURRENCY_SYMBOL,
        MODIFIER_SYMBOL,
        OTHER_SYMBOL,
        SPACE_SEPARATOR,
        LINE_SEPARATOR,
        PARAGRAPH_SEPARATOR,
        CONTROL,
        FORMAT,
        SURROGATE,
        PRIVATE_USE,
        UNASSIGNED,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Value { }

    /**
     * General Category "Uppercase_Letter".
     */
    public static final int UPPERCASE_LETTER = 0x01;

    /**
     * General Category "Lowercase_Letter".
     */
    public static final int LOWERCASE_LETTER = 0x02;

    /**
     * General Category "Titlecase_Letter".
     */
    public static final int TITLECASE_LETTER = 0x03;

    /**
     * General Category "Modifier_Letter".
     */
    public static final int MODIFIER_LETTER = 0x04;

    /**
     * General Category "Other_Letter".
     */
    public static final int OTHER_LETTER = 0x05;

    /**
     * General Category "Nonspacing_Mark".
     */
    public static final int NONSPACING_MARK = 0x06;

    /**
     * General Category "Spacing_Mark".
     */
    public static final int SPACING_MARK = 0x07;

    /**
     * General Category "Enclosing_Mark".
     */
    public static final int ENCLOSING_MARK = 0x08;

    /**
     * General Category "Decimal_Number".
     */
    public static final int DECIMAL_NUMBER = 0x09;

    /**
     * General Category "Letter_Number".
     */
    public static final int LETTER_NUMBER = 0x0A;

    /**
     * General Category "Other_Number".
     */
    public static final int OTHER_NUMBER = 0x0B;

    /**
     * General Category "Connector_Punctuation".
     */
    public static final int CONNECTOR_PUNCTUATION = 0x0C;

    /**
     * General Category "Dash_Punctuation".
     */
    public static final int DASH_PUNCTUATION = 0x0D;

    /**
     * General Category "Open_Punctuation".
     */
    public static final int OPEN_PUNCTUATION = 0x0E;

    /**
     * General Category "Close_Punctuation".
     */
    public static final int CLOSE_PUNCTUATION = 0x0F;

    /**
     * General Category "Initial_Punctuation".
     */
    public static final int INITIAL_PUNCTUATION = 0x10;

    /**
     * General Category "Final_Punctuation".
     */
    public static final int FINAL_PUNCTUATION = 0x11;

    /**
     * General Category "Other_Punctuation".
     */
    public static final int OTHER_PUNCTUATION = 0x12;

    /**
     * General Category "Math_Symbol".
     */
    public static final int MATH_SYMBOL = 0x13;

    /**
     * General Category "Currency_Symbol".
     */
    public static final int CURRENCY_SYMBOL = 0x14;

    /**
     * General Category "Modifier_Symbol".
     */
    public static final int MODIFIER_SYMBOL = 0x15;

    /**
     * General Category "Other_Symbol".
     */
    public static final int OTHER_SYMBOL = 0x16;

    /**
     * General Category "Space_Separator".
     */
    public static final int SPACE_SEPARATOR = 0x17;

    /**
     * General Category "Line_Separator".
     */
    public static final int LINE_SEPARATOR = 0x18;

    /**
     * General Category "Paragraph_Separator".
     */
    public static final int PARAGRAPH_SEPARATOR = 0x19;

    /**
     * General Category "Control".
     */
    public static final int CONTROL = 0x1A;

    /**
     * General Category "Format".
     */
    public static final int FORMAT = 0x1B;

    /**
     * General Category "Surrogate".
     */
    public static final int SURROGATE = 0x1C;

    /**
     * General Category "Private_Use".
     */
    public static final int PRIVATE_USE = 0x1D;

    /**
     * General Category "Unassigned".
     */
    public static final int UNASSIGNED = 0x1E;

    private GeneralCategory() {
    }
}
