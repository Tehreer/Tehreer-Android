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

package com.mta.tehreer.graphics;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StrokeJoinTest {
    @Test
    public void testOrdinals() {
        assertEquals(StrokeJoin.BEVEL.ordinal(), 0);
        assertEquals(StrokeJoin.MITER.ordinal(), 1);
        assertEquals(StrokeJoin.ROUND.ordinal(), 2);
    }

    @Test
    public void testValuesArray() {
        StrokeJoin[] array = new StrokeJoin[] {
            StrokeJoin.BEVEL,
            StrokeJoin.MITER,
            StrokeJoin.ROUND
        };

        assertArrayEquals(StrokeJoin.values(), array);
    }

    @Test
    public void testAssociatedValues() {
        assertEquals(StrokeJoin.ROUND.value, 0);
        assertEquals(StrokeJoin.BEVEL.value, 1);
        assertEquals(StrokeJoin.MITER.value, 2);
    }
}
