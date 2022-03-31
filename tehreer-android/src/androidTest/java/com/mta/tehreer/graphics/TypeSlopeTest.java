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

public class TypeSlopeTest {
    @Test
    public void testOrdinals() {
        assertEquals(TypeSlope.PLAIN.ordinal(), 0);
        assertEquals(TypeSlope.ITALIC.ordinal(), 1);
        assertEquals(TypeSlope.OBLIQUE.ordinal(), 2);
    }

    @Test
    public void testValuesArray() {
        TypeSlope[] array = new TypeSlope[] {
            TypeSlope.PLAIN,
            TypeSlope.ITALIC,
            TypeSlope.OBLIQUE
        };

        assertArrayEquals(TypeSlope.values(), array);
    }

    @Test
    public void testValueOf() {
        assertEquals(TypeSlope.valueOf(0), TypeSlope.PLAIN);
        assertEquals(TypeSlope.valueOf(1), TypeSlope.ITALIC);
        assertEquals(TypeSlope.valueOf(2), TypeSlope.OBLIQUE);
    }

    @Test
    public void testFromItal() {
        assertEquals(TypeSlope.fromItal(-0.1f), TypeSlope.PLAIN);
        assertEquals(TypeSlope.fromItal(0.0f), TypeSlope.PLAIN);
        assertEquals(TypeSlope.fromItal(0.5f), TypeSlope.PLAIN);
        assertEquals(TypeSlope.fromItal(0.9f), TypeSlope.PLAIN);
        assertEquals(TypeSlope.fromItal(1.0f), TypeSlope.ITALIC);
        assertEquals(TypeSlope.fromItal(1.1f), TypeSlope.ITALIC);
        assertEquals(TypeSlope.fromItal(1.5f), TypeSlope.ITALIC);
        assertEquals(TypeSlope.fromItal(1.9f), TypeSlope.ITALIC);
    }

    @Test
    public void testFromSlnt() {
        assertEquals(TypeSlope.fromSlnt(-1.0f), TypeSlope.OBLIQUE);
        assertEquals(TypeSlope.fromSlnt(-0.9f), TypeSlope.OBLIQUE);
        assertEquals(TypeSlope.fromSlnt(-0.5f), TypeSlope.OBLIQUE);
        assertEquals(TypeSlope.fromSlnt(-0.1f), TypeSlope.OBLIQUE);
        assertEquals(TypeSlope.fromSlnt(0.0f), TypeSlope.PLAIN);
        assertEquals(TypeSlope.fromSlnt(0.1f), TypeSlope.OBLIQUE);
        assertEquals(TypeSlope.fromSlnt(0.5f), TypeSlope.OBLIQUE);
        assertEquals(TypeSlope.fromSlnt(0.9f), TypeSlope.OBLIQUE);
        assertEquals(TypeSlope.fromSlnt(1.0f), TypeSlope.OBLIQUE);
    }
}
