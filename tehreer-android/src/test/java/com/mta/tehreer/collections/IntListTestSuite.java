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

package com.mta.tehreer.collections;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public abstract class IntListTestSuite {
    protected IntList actual;
    protected int[] expected;

    protected IntListTestSuite() {
    }

    @Test
    public void testSize() {
        assertEquals(actual.size(), expected.length);
    }

    @Test
    public void testElements() {
        for (int i = 0; i < expected.length; i++) {
            assertEquals(actual.get(i), expected[i]);
        }
    }

    @Test
    public void testCopyFull() {
        int[] array = new int[expected.length];
        actual.copyTo(array, 0);

        assertArrayEquals(array, expected);
    }

    @Test
    public void testCopyAtStart() {
        int actualLength = expected.length;
        int extraLength = actualLength / 2;

        int[] extra = new int[extraLength];
        int[] array = new int[actualLength + extraLength];
        actual.copyTo(array, 0);

        int[] copiedChunk = Arrays.copyOfRange(array, 0, actualLength);
        int[] lastChunk = Arrays.copyOfRange(array, actualLength, array.length);

        assertArrayEquals(copiedChunk, expected);
        assertArrayEquals(lastChunk, extra);
    }

    @Test
    public void testCopyAtEnd() {
        int actualLength = expected.length;
        int extraLength = actualLength / 2;

        int[] extra = new int[extraLength];
        int[] array = new int[actualLength + extraLength];
        actual.copyTo(array, extraLength);

        int[] firstChunk = Arrays.copyOfRange(array, 0, extraLength);
        int[] copiedChunk = Arrays.copyOfRange(array, extraLength, array.length);

        assertArrayEquals(firstChunk, extra);
        assertArrayEquals(copiedChunk, expected);
    }

    @Test
    public void testToArray() {
        assertArrayEquals(actual.toArray(), expected);
    }

    @Test
    public void testEquals() {
        assertEquals(actual, IntList.of(expected));
    }
}
