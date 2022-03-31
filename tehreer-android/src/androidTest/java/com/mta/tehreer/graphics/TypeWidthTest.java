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
    public void testValuesArray() {
        TypeWidth[] array = new TypeWidth[] {
            TypeWidth.ULTRA_CONDENSED,
            TypeWidth.EXTRA_CONDENSED,
            TypeWidth.CONDENSED,
            TypeWidth.SEMI_CONDENSED,
            TypeWidth.NORMAL,
            TypeWidth.SEMI_EXPANDED,
            TypeWidth.EXPANDED,
            TypeWidth.EXTRA_EXPANDED,
            TypeWidth.ULTRA_EXPANDED
        };

        assertArrayEquals(TypeWidth.values(), array);
    }

    @Test
    public void testAssociatedValues() {
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

    @Test
    public void testValueOf() {
        assertEquals(TypeWidth.valueOf(-1), TypeWidth.ULTRA_CONDENSED);
        assertEquals(TypeWidth.valueOf(0), TypeWidth.ULTRA_CONDENSED);
        assertEquals(TypeWidth.valueOf(1), TypeWidth.ULTRA_CONDENSED);
        assertEquals(TypeWidth.valueOf(2), TypeWidth.EXTRA_CONDENSED);
        assertEquals(TypeWidth.valueOf(3), TypeWidth.CONDENSED);
        assertEquals(TypeWidth.valueOf(4), TypeWidth.SEMI_CONDENSED);
        assertEquals(TypeWidth.valueOf(5), TypeWidth.NORMAL);
        assertEquals(TypeWidth.valueOf(6), TypeWidth.SEMI_EXPANDED);
        assertEquals(TypeWidth.valueOf(7), TypeWidth.EXPANDED);
        assertEquals(TypeWidth.valueOf(8), TypeWidth.EXTRA_EXPANDED);
        assertEquals(TypeWidth.valueOf(9), TypeWidth.ULTRA_EXPANDED);
        assertEquals(TypeWidth.valueOf(10), TypeWidth.ULTRA_EXPANDED);
    }

    @Test
    public void testFromWdth() {
        assertEquals(TypeWidth.fromWdth(-1.0f), TypeWidth.ULTRA_CONDENSED);
        assertEquals(TypeWidth.fromWdth(0.0f), TypeWidth.ULTRA_CONDENSED);
        assertEquals(TypeWidth.fromWdth(25.0f), TypeWidth.ULTRA_CONDENSED);
        assertEquals(TypeWidth.fromWdth(50.0f), TypeWidth.ULTRA_CONDENSED);
        assertEquals(TypeWidth.fromWdth(62.4f), TypeWidth.ULTRA_CONDENSED);
        assertEquals(TypeWidth.fromWdth(62.5f), TypeWidth.EXTRA_CONDENSED);
        assertEquals(TypeWidth.fromWdth(70.0f), TypeWidth.EXTRA_CONDENSED);
        assertEquals(TypeWidth.fromWdth(74.9f), TypeWidth.EXTRA_CONDENSED);
        assertEquals(TypeWidth.fromWdth(75.0f), TypeWidth.CONDENSED);
        assertEquals(TypeWidth.fromWdth(80.0f), TypeWidth.CONDENSED);
        assertEquals(TypeWidth.fromWdth(87.4f), TypeWidth.CONDENSED);
        assertEquals(TypeWidth.fromWdth(87.5f), TypeWidth.SEMI_CONDENSED);
        assertEquals(TypeWidth.fromWdth(90.0f), TypeWidth.SEMI_CONDENSED);
        assertEquals(TypeWidth.fromWdth(99.9f), TypeWidth.SEMI_CONDENSED);
        assertEquals(TypeWidth.fromWdth(100.0f), TypeWidth.NORMAL);
        assertEquals(TypeWidth.fromWdth(110.0f), TypeWidth.NORMAL);
        assertEquals(TypeWidth.fromWdth(112.4f), TypeWidth.NORMAL);
        assertEquals(TypeWidth.fromWdth(112.5f), TypeWidth.SEMI_EXPANDED);
        assertEquals(TypeWidth.fromWdth(120.0f), TypeWidth.SEMI_EXPANDED);
        assertEquals(TypeWidth.fromWdth(124.9f), TypeWidth.SEMI_EXPANDED);
        assertEquals(TypeWidth.fromWdth(125.0f), TypeWidth.EXPANDED);
        assertEquals(TypeWidth.fromWdth(140.0f), TypeWidth.EXPANDED);
        assertEquals(TypeWidth.fromWdth(149.9f), TypeWidth.EXPANDED);
        assertEquals(TypeWidth.fromWdth(150.0f), TypeWidth.EXTRA_EXPANDED);
        assertEquals(TypeWidth.fromWdth(160.0f), TypeWidth.EXTRA_EXPANDED);
        assertEquals(TypeWidth.fromWdth(174.9f), TypeWidth.EXTRA_EXPANDED);
        assertEquals(TypeWidth.fromWdth(175.0f), TypeWidth.ULTRA_EXPANDED);
        assertEquals(TypeWidth.fromWdth(190.0f), TypeWidth.ULTRA_EXPANDED);
        assertEquals(TypeWidth.fromWdth(199.9f), TypeWidth.ULTRA_EXPANDED);
        assertEquals(TypeWidth.fromWdth(200.0f), TypeWidth.ULTRA_EXPANDED);
        assertEquals(TypeWidth.fromWdth(250.0f), TypeWidth.ULTRA_EXPANDED);
        assertEquals(TypeWidth.fromWdth(500.0f), TypeWidth.ULTRA_EXPANDED);
    }
}
