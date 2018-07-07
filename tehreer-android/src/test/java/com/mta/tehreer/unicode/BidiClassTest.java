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

public class BidiClassTest {

    @Test
    public void testValues() {
        assertThat(BidiClass.LEFT_TO_RIGHT, equalTo(0x01));
        assertThat(BidiClass.RIGHT_TO_LEFT, equalTo(0x02));
        assertThat(BidiClass.ARABIC_LETTER, equalTo(0x03));
        assertThat(BidiClass.BOUNDARY_NEUTRAL, equalTo(0x04));
        assertThat(BidiClass.NONSPACING_MARK, equalTo(0x05));
        assertThat(BidiClass.ARABIC_NUMBER, equalTo(0x06));
        assertThat(BidiClass.EUROPEAN_NUMBER, equalTo(0x07));
        assertThat(BidiClass.EUROPEAN_TERMINATOR, equalTo(0x08));
        assertThat(BidiClass.EUROPEAN_SEPARATOR, equalTo(0x09));
        assertThat(BidiClass.COMMON_SEPARATOR, equalTo(0x0A));
        assertThat(BidiClass.WHITE_SPACE, equalTo(0x0B));
        assertThat(BidiClass.SEGMENT_SEPARATOR, equalTo(0x0C));
        assertThat(BidiClass.PARAGRAPH_SEPARATOR, equalTo(0x0D));
        assertThat(BidiClass.OTHER_NEUTRAL, equalTo(0x0E));
        assertThat(BidiClass.LEFT_TO_RIGHT_ISOLATE, equalTo(0x0F));
        assertThat(BidiClass.RIGHT_TO_LEFT_ISOLATE, equalTo(0x10));
        assertThat(BidiClass.FIRST_STRONG_ISOLATE, equalTo(0x11));
        assertThat(BidiClass.POP_DIRECTIONAL_ISOLATE, equalTo(0x12));
        assertThat(BidiClass.LEFT_TO_RIGHT_EMBEDDING, equalTo(0x13));
        assertThat(BidiClass.RIGHT_TO_LEFT_EMBEDDING, equalTo(0x14));
        assertThat(BidiClass.LEFT_TO_RIGHT_OVERRIDE, equalTo(0x15));
        assertThat(BidiClass.RIGHT_TO_LEFT_OVERRIDE, equalTo(0x16));
        assertThat(BidiClass.POP_DIRECTIONAL_FORMAT, equalTo(0x17));
    }
}
