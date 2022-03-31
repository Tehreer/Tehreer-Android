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

public class TypeWeightTest {
    @Test
    public void testOrdinals() {
        assertEquals(TypeWeight.THIN.ordinal(), 0);
        assertEquals(TypeWeight.EXTRA_LIGHT.ordinal(), 1);
        assertEquals(TypeWeight.LIGHT.ordinal(), 2);
        assertEquals(TypeWeight.REGULAR.ordinal(), 3);
        assertEquals(TypeWeight.MEDIUM.ordinal(), 4);
        assertEquals(TypeWeight.SEMI_BOLD.ordinal(), 5);
        assertEquals(TypeWeight.BOLD.ordinal(), 6);
        assertEquals(TypeWeight.EXTRA_BOLD.ordinal(), 7);
        assertEquals(TypeWeight.HEAVY.ordinal(), 8);
    }

    @Test
    public void testValuesArray() {
        TypeWeight[] array = new TypeWeight[] {
            TypeWeight.THIN,
            TypeWeight.EXTRA_LIGHT,
            TypeWeight.LIGHT,
            TypeWeight.REGULAR,
            TypeWeight.MEDIUM,
            TypeWeight.SEMI_BOLD,
            TypeWeight.BOLD,
            TypeWeight.EXTRA_BOLD,
            TypeWeight.HEAVY
        };

        assertArrayEquals(TypeWeight.values(), array);
    }

    @Test
    public void testAssociatedValues() {
        assertEquals(TypeWeight.THIN.value, 100);
        assertEquals(TypeWeight.EXTRA_LIGHT.value, 200);
        assertEquals(TypeWeight.LIGHT.value, 300);
        assertEquals(TypeWeight.REGULAR.value, 400);
        assertEquals(TypeWeight.MEDIUM.value, 500);
        assertEquals(TypeWeight.SEMI_BOLD.value, 600);
        assertEquals(TypeWeight.BOLD.value, 700);
        assertEquals(TypeWeight.EXTRA_BOLD.value, 800);
        assertEquals(TypeWeight.HEAVY.value, 900);
    }

    @Test
    public void testValueOf() {
        assertEquals(TypeWeight.valueOf(-1), TypeWeight.THIN);
        assertEquals(TypeWeight.valueOf(0), TypeWeight.THIN);
        assertEquals(TypeWeight.valueOf(1), TypeWeight.THIN);
        assertEquals(TypeWeight.valueOf(50), TypeWeight.THIN);
        assertEquals(TypeWeight.valueOf(100), TypeWeight.THIN);
        assertEquals(TypeWeight.valueOf(149), TypeWeight.THIN);
        assertEquals(TypeWeight.valueOf(150), TypeWeight.EXTRA_LIGHT);
        assertEquals(TypeWeight.valueOf(200), TypeWeight.EXTRA_LIGHT);
        assertEquals(TypeWeight.valueOf(249), TypeWeight.EXTRA_LIGHT);
        assertEquals(TypeWeight.valueOf(250), TypeWeight.LIGHT);
        assertEquals(TypeWeight.valueOf(300), TypeWeight.LIGHT);
        assertEquals(TypeWeight.valueOf(349), TypeWeight.LIGHT);
        assertEquals(TypeWeight.valueOf(350), TypeWeight.REGULAR);
        assertEquals(TypeWeight.valueOf(400), TypeWeight.REGULAR);
        assertEquals(TypeWeight.valueOf(449), TypeWeight.REGULAR);
        assertEquals(TypeWeight.valueOf(450), TypeWeight.MEDIUM);
        assertEquals(TypeWeight.valueOf(500), TypeWeight.MEDIUM);
        assertEquals(TypeWeight.valueOf(549), TypeWeight.MEDIUM);
        assertEquals(TypeWeight.valueOf(550), TypeWeight.SEMI_BOLD);
        assertEquals(TypeWeight.valueOf(600), TypeWeight.SEMI_BOLD);
        assertEquals(TypeWeight.valueOf(649), TypeWeight.SEMI_BOLD);
        assertEquals(TypeWeight.valueOf(650), TypeWeight.BOLD);
        assertEquals(TypeWeight.valueOf(700), TypeWeight.BOLD);
        assertEquals(TypeWeight.valueOf(749), TypeWeight.BOLD);
        assertEquals(TypeWeight.valueOf(750), TypeWeight.EXTRA_BOLD);
        assertEquals(TypeWeight.valueOf(800), TypeWeight.EXTRA_BOLD);
        assertEquals(TypeWeight.valueOf(849), TypeWeight.EXTRA_BOLD);
        assertEquals(TypeWeight.valueOf(850), TypeWeight.HEAVY);
        assertEquals(TypeWeight.valueOf(900), TypeWeight.HEAVY);
        assertEquals(TypeWeight.valueOf(1000), TypeWeight.HEAVY);
        assertEquals(TypeWeight.valueOf(1001), TypeWeight.HEAVY);
        assertEquals(TypeWeight.valueOf(1050), TypeWeight.HEAVY);
    }

    @Test
    public void testFromWght() {
        assertEquals(TypeWeight.fromWght(-1.0f), TypeWeight.THIN);
        assertEquals(TypeWeight.fromWght(0.0f), TypeWeight.THIN);
        assertEquals(TypeWeight.fromWght(1.0f), TypeWeight.THIN);
        assertEquals(TypeWeight.fromWght(50.0f), TypeWeight.THIN);
        assertEquals(TypeWeight.fromWght(100.0f), TypeWeight.THIN);
        assertEquals(TypeWeight.fromWght(149.0f), TypeWeight.THIN);
        assertEquals(TypeWeight.fromWght(150.0f), TypeWeight.EXTRA_LIGHT);
        assertEquals(TypeWeight.fromWght(200.0f), TypeWeight.EXTRA_LIGHT);
        assertEquals(TypeWeight.fromWght(249.0f), TypeWeight.EXTRA_LIGHT);
        assertEquals(TypeWeight.fromWght(250.0f), TypeWeight.LIGHT);
        assertEquals(TypeWeight.fromWght(300.0f), TypeWeight.LIGHT);
        assertEquals(TypeWeight.fromWght(349.0f), TypeWeight.LIGHT);
        assertEquals(TypeWeight.fromWght(350.0f), TypeWeight.REGULAR);
        assertEquals(TypeWeight.fromWght(400.0f), TypeWeight.REGULAR);
        assertEquals(TypeWeight.fromWght(449.0f), TypeWeight.REGULAR);
        assertEquals(TypeWeight.fromWght(450.0f), TypeWeight.MEDIUM);
        assertEquals(TypeWeight.fromWght(500.0f), TypeWeight.MEDIUM);
        assertEquals(TypeWeight.fromWght(549.0f), TypeWeight.MEDIUM);
        assertEquals(TypeWeight.fromWght(550.0f), TypeWeight.SEMI_BOLD);
        assertEquals(TypeWeight.fromWght(600.0f), TypeWeight.SEMI_BOLD);
        assertEquals(TypeWeight.fromWght(649.0f), TypeWeight.SEMI_BOLD);
        assertEquals(TypeWeight.fromWght(650.0f), TypeWeight.BOLD);
        assertEquals(TypeWeight.fromWght(700.0f), TypeWeight.BOLD);
        assertEquals(TypeWeight.fromWght(749.0f), TypeWeight.BOLD);
        assertEquals(TypeWeight.fromWght(750.0f), TypeWeight.EXTRA_BOLD);
        assertEquals(TypeWeight.fromWght(800.0f), TypeWeight.EXTRA_BOLD);
        assertEquals(TypeWeight.fromWght(849.0f), TypeWeight.EXTRA_BOLD);
        assertEquals(TypeWeight.fromWght(850.0f), TypeWeight.HEAVY);
        assertEquals(TypeWeight.fromWght(900.0f), TypeWeight.HEAVY);
        assertEquals(TypeWeight.fromWght(1000.0f), TypeWeight.HEAVY);
        assertEquals(TypeWeight.fromWght(1001.0f), TypeWeight.HEAVY);
        assertEquals(TypeWeight.fromWght(1050.0f), TypeWeight.HEAVY);
    }
}
