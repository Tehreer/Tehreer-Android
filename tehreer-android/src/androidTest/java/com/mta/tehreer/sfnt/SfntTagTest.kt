/*
 * Copyright (C) 2026 Muhammad Tayyab Akram
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

package com.mta.tehreer.sfnt

import com.mta.tehreer.util.Assert.assertThrows
import org.junit.Assert.assertEquals

import org.junit.Test

class SfntTagTest {
    private val ARAB: Int = 0x61726162
    private val TRIR: Int = 0x74726972
    private val URDU: Int = 0x55524420

    @Test(expected = IllegalArgumentException::class)
    fun testMakeForLessCharacters() {
        SfntTag.make("URD")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testMakeForMoreCharacters() {
        SfntTag.make("ISLAM")
    }

    @Test
    fun testMakeForInvalidCharacters() {
        assertThrows(IllegalArgumentException::class.java,
                     { SfntTag.make("\nrab") })
        assertThrows(IllegalArgumentException::class.java,
                     { SfntTag.make("a\rab") })
        assertThrows(IllegalArgumentException::class.java,
                     { SfntTag.make("ar\tb") })
        assertThrows(IllegalArgumentException::class.java,
                     { SfntTag.make("ara\u0000") })
    }

    @Test
    fun testMakeForValidTags() {
        assertEquals(SfntTag.make("arab"), ARAB)
        assertEquals(SfntTag.make("trir"), TRIR)
        assertEquals(SfntTag.make("URD "), URDU)
    }

    @Test
    fun testToString() {
        assertEquals(SfntTag.toString(ARAB), "arab")
        assertEquals(SfntTag.toString(TRIR), "trir")
        assertEquals(SfntTag.toString(URDU), "URD ")
    }
}


