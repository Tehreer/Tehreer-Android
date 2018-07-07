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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class GeneralCategoryTest {

    @Test
    public void testValues() {
        assertThat(GeneralCategory.UPPERCASE_LETTER, equalTo(0x01));
        assertThat(GeneralCategory.LOWERCASE_LETTER, equalTo(0x02));
        assertThat(GeneralCategory.TITLECASE_LETTER, equalTo(0x03));
        assertThat(GeneralCategory.MODIFIER_LETTER, equalTo(0x04));
        assertThat(GeneralCategory.OTHER_LETTER, equalTo(0x05));
        assertThat(GeneralCategory.NONSPACING_MARK, equalTo(0x06));
        assertThat(GeneralCategory.SPACING_MARK, equalTo(0x07));
        assertThat(GeneralCategory.ENCLOSING_MARK, equalTo(0x08));
        assertThat(GeneralCategory.DECIMAL_NUMBER, equalTo(0x09));
        assertThat(GeneralCategory.LETTER_NUMBER, equalTo(0x0A));
        assertThat(GeneralCategory.OTHER_NUMBER, equalTo(0x0B));
        assertThat(GeneralCategory.CONNECTOR_PUNCTUATION, equalTo(0x0C));
        assertThat(GeneralCategory.DASH_PUNCTUATION, equalTo(0x0D));
        assertThat(GeneralCategory.OPEN_PUNCTUATION, equalTo(0x0E));
        assertThat(GeneralCategory.CLOSE_PUNCTUATION, equalTo(0x0F));
        assertThat(GeneralCategory.INITIAL_PUNCTUATION, equalTo(0x10));
        assertThat(GeneralCategory.FINAL_PUNCTUATION, equalTo(0x11));
        assertThat(GeneralCategory.OTHER_PUNCTUATION, equalTo(0x12));
        assertThat(GeneralCategory.MATH_SYMBOL, equalTo(0x13));
        assertThat(GeneralCategory.CURRENCY_SYMBOL, equalTo(0x14));
        assertThat(GeneralCategory.MODIFIER_SYMBOL, equalTo(0x15));
        assertThat(GeneralCategory.OTHER_SYMBOL, equalTo(0x16));
        assertThat(GeneralCategory.SPACE_SEPARATOR, equalTo(0x17));
        assertThat(GeneralCategory.LINE_SEPARATOR, equalTo(0x18));
        assertThat(GeneralCategory.PARAGRAPH_SEPARATOR, equalTo(0x19));
        assertThat(GeneralCategory.CONTROL, equalTo(0x1A));
        assertThat(GeneralCategory.FORMAT, equalTo(0x1B));
        assertThat(GeneralCategory.SURROGATE, equalTo(0x1C));
        assertThat(GeneralCategory.PRIVATE_USE, equalTo(0x1D));
        assertThat(GeneralCategory.UNASSIGNED, equalTo(0x1E));
    }
}
