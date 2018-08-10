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

package com.mta.tehreer.graphics;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TypeWidthTest {
    @Test
    public void testOrdinals() {
        assertEquals(TypeWidth.ULTRA_CONDENSED.ordinal(), 0);
        assertEquals(TypeWidth.EXTRA_CONDENSED.ordinal(), 1);
        assertEquals(TypeWidth.CONDENSED.ordinal(), 2);
        assertEquals(TypeWidth.SEMI_CONDENSED.ordinal(), 3);
        assertEquals(TypeWidth.NORMAL.ordinal(), 4);
        assertEquals(TypeWidth.SEMI_EXPANDED.ordinal(), 5);
        assertEquals(TypeWidth.EXPANDED.ordinal(), 6);
        assertEquals(TypeWidth.EXTRA_EXPANDED.ordinal(), 7);
        assertEquals(TypeWidth.ULTRA_EXPANDED.ordinal(), 8);
    }

    @Test
    public void testValues() {
        assertEquals(TypeWidth.ULTRA_CONDENSED.value, 1);
        assertEquals(TypeWidth.EXTRA_CONDENSED.value, 2);
        assertEquals(TypeWidth.CONDENSED.value, 3);
        assertEquals(TypeWidth.SEMI_CONDENSED.value, 4);
        assertEquals(TypeWidth.NORMAL.value, 5);
        assertEquals(TypeWidth.SEMI_EXPANDED.value, 6);
        assertEquals(TypeWidth.EXPANDED.value, 7);
        assertEquals(TypeWidth.EXTRA_EXPANDED.value, 8);
        assertEquals(TypeWidth.ULTRA_EXPANDED.value, 9);
    }
}
