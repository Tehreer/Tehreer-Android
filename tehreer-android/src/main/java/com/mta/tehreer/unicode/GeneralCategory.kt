/*
 * Copyright (C) 2018-2026 Muhammad Tayyab Akram
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

package com.mta.tehreer.unicode

import androidx.annotation.IntDef

/**
 * Represents the general category of a character in Unicode specification. The constants correspond
 * to the values defined in
 * <a href="https://unicode.org/reports/tr44/#General_Category_Values">
 *     Unicode Standard Annex #44: General Category Values
 * </a>.
 */
object GeneralCategory {
    @IntDef(
        value = [
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
        ]
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class Value

    /**
     * General Category "Uppercase_Letter".
     */
    const val UPPERCASE_LETTER = 0x01

    /**
     * General Category "Lowercase_Letter".
     */
    const val LOWERCASE_LETTER = 0x02

    /**
     * General Category "Titlecase_Letter".
     */
    const val TITLECASE_LETTER = 0x03

    /**
     * General Category "Modifier_Letter".
     */
    const val MODIFIER_LETTER = 0x04

    /**
     * General Category "Other_Letter".
     */
    const val OTHER_LETTER = 0x05

    /**
     * General Category "Nonspacing_Mark".
     */
    const val NONSPACING_MARK = 0x06

    /**
     * General Category "Spacing_Mark".
     */
    const val SPACING_MARK = 0x07

    /**
     * General Category "Enclosing_Mark".
     */
    const val ENCLOSING_MARK = 0x08

    /**
     * General Category "Decimal_Number".
     */
    const val DECIMAL_NUMBER = 0x09

    /**
     * General Category "Letter_Number".
     */
    const val LETTER_NUMBER = 0x0A

    /**
     * General Category "Other_Number".
     */
    const val OTHER_NUMBER = 0x0B

    /**
     * General Category "Connector_Punctuation".
     */
    const val CONNECTOR_PUNCTUATION = 0x0C

    /**
     * General Category "Dash_Punctuation".
     */
    const val DASH_PUNCTUATION = 0x0D

    /**
     * General Category "Open_Punctuation".
     */
    const val OPEN_PUNCTUATION = 0x0E

    /**
     * General Category "Close_Punctuation".
     */
    const val CLOSE_PUNCTUATION = 0x0F

    /**
     * General Category "Initial_Punctuation".
     */
    const val INITIAL_PUNCTUATION = 0x10

    /**
     * General Category "Final_Punctuation".
     */
    const val FINAL_PUNCTUATION = 0x11

    /**
     * General Category "Other_Punctuation".
     */
    const val OTHER_PUNCTUATION = 0x12

    /**
     * General Category "Math_Symbol".
     */
    const val MATH_SYMBOL = 0x13

    /**
     * General Category "Currency_Symbol".
     */
    const val CURRENCY_SYMBOL = 0x14

    /**
     * General Category "Modifier_Symbol".
     */
    const val MODIFIER_SYMBOL = 0x15

    /**
     * General Category "Other_Symbol".
     */
    const val OTHER_SYMBOL = 0x16

    /**
     * General Category "Space_Separator".
     */
    const val SPACE_SEPARATOR = 0x17

    /**
     * General Category "Line_Separator".
     */
    const val LINE_SEPARATOR = 0x18

    /**
     * General Category "Paragraph_Separator".
     */
    const val PARAGRAPH_SEPARATOR = 0x19

    /**
     * General Category "Control".
     */
    const val CONTROL = 0x1A

    /**
     * General Category "Format".
     */
    const val FORMAT = 0x1B

    /**
     * General Category "Surrogate".
     */
    const val SURROGATE = 0x1C

    /**
     * General Category "Private_Use".
     */
    const val PRIVATE_USE = 0x1D

    /**
     * General Category "Unassigned".
     */
    const val UNASSIGNED = 0x1E
}
