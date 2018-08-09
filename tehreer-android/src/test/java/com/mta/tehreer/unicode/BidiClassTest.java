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

public class BidiClassTest {
    @Test
    public void testValues() {
        assertEquals(BidiClass.LEFT_TO_RIGHT, 0x01);
        assertEquals(BidiClass.RIGHT_TO_LEFT, 0x02);
        assertEquals(BidiClass.ARABIC_LETTER, 0x03);
        assertEquals(BidiClass.BOUNDARY_NEUTRAL, 0x04);
        assertEquals(BidiClass.NONSPACING_MARK, 0x05);
        assertEquals(BidiClass.ARABIC_NUMBER, 0x06);
        assertEquals(BidiClass.EUROPEAN_NUMBER, 0x07);
        assertEquals(BidiClass.EUROPEAN_TERMINATOR, 0x08);
        assertEquals(BidiClass.EUROPEAN_SEPARATOR, 0x09);
        assertEquals(BidiClass.COMMON_SEPARATOR, 0x0A);
        assertEquals(BidiClass.WHITE_SPACE, 0x0B);
        assertEquals(BidiClass.SEGMENT_SEPARATOR, 0x0C);
        assertEquals(BidiClass.PARAGRAPH_SEPARATOR, 0x0D);
        assertEquals(BidiClass.OTHER_NEUTRAL, 0x0E);
        assertEquals(BidiClass.LEFT_TO_RIGHT_ISOLATE, 0x0F);
        assertEquals(BidiClass.RIGHT_TO_LEFT_ISOLATE, 0x10);
        assertEquals(BidiClass.FIRST_STRONG_ISOLATE, 0x11);
        assertEquals(BidiClass.POP_DIRECTIONAL_ISOLATE, 0x12);
        assertEquals(BidiClass.LEFT_TO_RIGHT_EMBEDDING, 0x13);
        assertEquals(BidiClass.RIGHT_TO_LEFT_EMBEDDING, 0x14);
        assertEquals(BidiClass.LEFT_TO_RIGHT_OVERRIDE, 0x15);
        assertEquals(BidiClass.RIGHT_TO_LEFT_OVERRIDE, 0x16);
        assertEquals(BidiClass.POP_DIRECTIONAL_FORMAT, 0x17);
    }
}
