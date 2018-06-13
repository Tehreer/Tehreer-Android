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

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

public class GeneralCategoryTest {

    @Test
    public void testValueOf() {
        assertThat(GeneralCategory.valueOf((byte) 0x00), is(nullValue()));
        assertThat(GeneralCategory.valueOf((byte) 0x01), is(GeneralCategory.UPPERCASE_LETTER));
        assertThat(GeneralCategory.valueOf((byte) 0x02), is(GeneralCategory.LOWERCASE_LETTER));
        assertThat(GeneralCategory.valueOf((byte) 0x03), is(GeneralCategory.TITLECASE_LETTER));
        assertThat(GeneralCategory.valueOf((byte) 0x04), is(GeneralCategory.MODIFIER_LETTER));
        assertThat(GeneralCategory.valueOf((byte) 0x05), is(GeneralCategory.OTHER_LETTER));
        assertThat(GeneralCategory.valueOf((byte) 0x06), is(GeneralCategory.NONSPACING_MARK));
        assertThat(GeneralCategory.valueOf((byte) 0x07), is(GeneralCategory.SPACING_MARK));
        assertThat(GeneralCategory.valueOf((byte) 0x08), is(GeneralCategory.ENCLOSING_MARK));
        assertThat(GeneralCategory.valueOf((byte) 0x09), is(GeneralCategory.DECIMAL_NUMBER));
        assertThat(GeneralCategory.valueOf((byte) 0x0A), is(GeneralCategory.LETTER_NUMBER));
        assertThat(GeneralCategory.valueOf((byte) 0x0B), is(GeneralCategory.OTHER_NUMBER));
        assertThat(GeneralCategory.valueOf((byte) 0x0C), is(GeneralCategory.CONNECTOR_PUNCTUATION));
        assertThat(GeneralCategory.valueOf((byte) 0x0D), is(GeneralCategory.DASH_PUNCTUATION));
        assertThat(GeneralCategory.valueOf((byte) 0x0E), is(GeneralCategory.OPEN_PUNCTUATION));
        assertThat(GeneralCategory.valueOf((byte) 0x0F), is(GeneralCategory.CLOSE_PUNCTUATION));
        assertThat(GeneralCategory.valueOf((byte) 0x10), is(GeneralCategory.INITIAL_PUNCTUATION));
        assertThat(GeneralCategory.valueOf((byte) 0x11), is(GeneralCategory.FINAL_PUNCTUATION));
        assertThat(GeneralCategory.valueOf((byte) 0x12), is(GeneralCategory.OTHER_PUNCTUATION));
        assertThat(GeneralCategory.valueOf((byte) 0x13), is(GeneralCategory.MATH_SYMBOL));
        assertThat(GeneralCategory.valueOf((byte) 0x14), is(GeneralCategory.CURRENCY_SYMBOL));
        assertThat(GeneralCategory.valueOf((byte) 0x15), is(GeneralCategory.MODIFIER_SYMBOL));
        assertThat(GeneralCategory.valueOf((byte) 0x16), is(GeneralCategory.OTHER_SYMBOL));
        assertThat(GeneralCategory.valueOf((byte) 0x17), is(GeneralCategory.SPACE_SEPARATOR));
        assertThat(GeneralCategory.valueOf((byte) 0x18), is(GeneralCategory.LINE_SEPARATOR));
        assertThat(GeneralCategory.valueOf((byte) 0x19), is(GeneralCategory.PARAGRAPH_SEPARATOR));
        assertThat(GeneralCategory.valueOf((byte) 0x1A), is(GeneralCategory.CONTROL));
        assertThat(GeneralCategory.valueOf((byte) 0x1B), is(GeneralCategory.FORMAT));
        assertThat(GeneralCategory.valueOf((byte) 0x1C), is(GeneralCategory.SURROGATE));
        assertThat(GeneralCategory.valueOf((byte) 0x1D), is(GeneralCategory.PRIVATE_USE));
        assertThat(GeneralCategory.valueOf((byte) 0x1E), is(GeneralCategory.UNASSIGNED));
    }
}
