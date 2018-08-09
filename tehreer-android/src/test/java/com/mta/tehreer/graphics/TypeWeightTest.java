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
    public void testValues() {
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
}
