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

public class BidiClassTest {

    @Test
    public void testValueOf() {
        assertThat(BidiClass.valueOf((byte) 0x00), is(nullValue()));
        assertThat(BidiClass.valueOf((byte) 0x01), is(BidiClass.LEFT_TO_RIGHT));
        assertThat(BidiClass.valueOf((byte) 0x02), is(BidiClass.RIGHT_TO_LEFT));
        assertThat(BidiClass.valueOf((byte) 0x03), is(BidiClass.RIGHT_TO_LEFT_ARABIC));
        assertThat(BidiClass.valueOf((byte) 0x04), is(BidiClass.BOUNDARY_NEUTRAL));
        assertThat(BidiClass.valueOf((byte) 0x05), is(BidiClass.NON_SPACING_MARK));
        assertThat(BidiClass.valueOf((byte) 0x06), is(BidiClass.ARABIC_NUMBER));
        assertThat(BidiClass.valueOf((byte) 0x07), is(BidiClass.EUROPEAN_NUMBER));
        assertThat(BidiClass.valueOf((byte) 0x08), is(BidiClass.EUROPEAN_NUMBER_TERMINATOR));
        assertThat(BidiClass.valueOf((byte) 0x09), is(BidiClass.EUROPEAN_NUMBER_SEPARATOR));
        assertThat(BidiClass.valueOf((byte) 0x0A), is(BidiClass.COMMON_NUMBER_SEPARATOR));
        assertThat(BidiClass.valueOf((byte) 0x0B), is(BidiClass.WHITE_SPACE));
        assertThat(BidiClass.valueOf((byte) 0x0C), is(BidiClass.SEGMENT_SEPARATOR));
        assertThat(BidiClass.valueOf((byte) 0x0D), is(BidiClass.PARAGRAPH_SEPARATOR));
        assertThat(BidiClass.valueOf((byte) 0x0E), is(BidiClass.OTHER_NEUTRAL));
        assertThat(BidiClass.valueOf((byte) 0x0F), is(BidiClass.LEFT_TO_RIGHT_ISOLATE));
        assertThat(BidiClass.valueOf((byte) 0x10), is(BidiClass.RIGHT_TO_LEFT_ISOLATE));
        assertThat(BidiClass.valueOf((byte) 0x11), is(BidiClass.FIRST_STRONG_ISOLATE));
        assertThat(BidiClass.valueOf((byte) 0x12), is(BidiClass.POP_DIRECTIONAL_ISOLATE));
        assertThat(BidiClass.valueOf((byte) 0x13), is(BidiClass.LEFT_TO_RIGHT_EMBEDDING));
        assertThat(BidiClass.valueOf((byte) 0x14), is(BidiClass.RIGHT_TO_LEFT_EMBEDDING));
        assertThat(BidiClass.valueOf((byte) 0x15), is(BidiClass.LEFT_TO_RIGHT_OVERRIDE));
        assertThat(BidiClass.valueOf((byte) 0x16), is(BidiClass.RIGHT_TO_LEFT_OVERRIDE));
        assertThat(BidiClass.valueOf((byte) 0x17), is(BidiClass.POP_DIRECTIONAL_FORMATTING));
    }
}
