/*
 * Copyright (C) 2022 Muhammad Tayyab Akram
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

package com.mta.tehreer.sfnt;

import static com.mta.tehreer.util.Assert.assertThrows;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SfntTagTest {
    private static final int ARAB = 0x61726162;
    private static final int TRIR = 0x74726972;
    private static final int URDU = 0x55524420;

    @Test(expected = IllegalArgumentException.class)
    public void testMakeForLessCharacters() {
        SfntTag.make("URD");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMakeForMoreCharacters() {
        SfntTag.make("ISLAM");
    }

    @Test
    public void testMakeForInvalidCharacters() {
        assertThrows(IllegalArgumentException.class,
                     () -> SfntTag.make("\nrab"));
        assertThrows(IllegalArgumentException.class,
                     () -> SfntTag.make("a\rab"));
        assertThrows(IllegalArgumentException.class,
                     () -> SfntTag.make("ar\tb"));
        assertThrows(IllegalArgumentException.class,
                     () -> SfntTag.make("ara\0"));
    }

    @Test
    public void testMakeForValidTags() {
        assertEquals(SfntTag.make("arab"), ARAB);
        assertEquals(SfntTag.make("trir"), TRIR);
        assertEquals(SfntTag.make("URD "), URDU);
    }

    @Test
    public void testToString() {
        assertEquals(SfntTag.toString(ARAB), "arab");
        assertEquals(SfntTag.toString(TRIR), "trir");
        assertEquals(SfntTag.toString(URDU), "URD ");
    }
}
