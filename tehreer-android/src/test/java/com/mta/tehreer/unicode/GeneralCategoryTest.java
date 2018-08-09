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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GeneralCategoryTest {
    @Test
    public void testValues() {
        assertEquals(GeneralCategory.UPPERCASE_LETTER, 0x01);
        assertEquals(GeneralCategory.LOWERCASE_LETTER, 0x02);
        assertEquals(GeneralCategory.TITLECASE_LETTER, 0x03);
        assertEquals(GeneralCategory.MODIFIER_LETTER, 0x04);
        assertEquals(GeneralCategory.OTHER_LETTER, 0x05);
        assertEquals(GeneralCategory.NONSPACING_MARK, 0x06);
        assertEquals(GeneralCategory.SPACING_MARK, 0x07);
        assertEquals(GeneralCategory.ENCLOSING_MARK, 0x08);
        assertEquals(GeneralCategory.DECIMAL_NUMBER, 0x09);
        assertEquals(GeneralCategory.LETTER_NUMBER, 0x0A);
        assertEquals(GeneralCategory.OTHER_NUMBER, 0x0B);
        assertEquals(GeneralCategory.CONNECTOR_PUNCTUATION, 0x0C);
        assertEquals(GeneralCategory.DASH_PUNCTUATION, 0x0D);
        assertEquals(GeneralCategory.OPEN_PUNCTUATION, 0x0E);
        assertEquals(GeneralCategory.CLOSE_PUNCTUATION, 0x0F);
        assertEquals(GeneralCategory.INITIAL_PUNCTUATION, 0x10);
        assertEquals(GeneralCategory.FINAL_PUNCTUATION, 0x11);
        assertEquals(GeneralCategory.OTHER_PUNCTUATION, 0x12);
        assertEquals(GeneralCategory.MATH_SYMBOL, 0x13);
        assertEquals(GeneralCategory.CURRENCY_SYMBOL, 0x14);
        assertEquals(GeneralCategory.MODIFIER_SYMBOL, 0x15);
        assertEquals(GeneralCategory.OTHER_SYMBOL, 0x16);
        assertEquals(GeneralCategory.SPACE_SEPARATOR, 0x17);
        assertEquals(GeneralCategory.LINE_SEPARATOR, 0x18);
        assertEquals(GeneralCategory.PARAGRAPH_SEPARATOR, 0x19);
        assertEquals(GeneralCategory.CONTROL, 0x1A);
        assertEquals(GeneralCategory.FORMAT, 0x1B);
        assertEquals(GeneralCategory.SURROGATE, 0x1C);
        assertEquals(GeneralCategory.PRIVATE_USE, 0x1D);
        assertEquals(GeneralCategory.UNASSIGNED, 0x1E);
    }
}
