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

    private static class SubListTestSuite extends IntListTestSuite {
        SubListTestSuite(IntList subList, int[] expected) {
            this.actual = subList;
            this.expected = expected;
        }
    }

    protected IntListTestSuite() {
    }

    @Test
    public void testSize() {
        assertEquals(actual.size(), expected.length);
    }

    @Test
    public void testElements() {
        int length = expected.length;
        for (int i = 0; i < length; i++) {
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

    private static void testSubList(IntList subList, int[] expected) {
        SubListTestSuite suite = new SubListTestSuite(subList, expected);
        suite.testSize();
        suite.testElements();
        suite.testCopyFull();
        suite.testCopyAtStart();
        suite.testCopyAtEnd();
        suite.testToArray();
        suite.testEquals();
    }

    @Test
    public void testSubListEmpty() {
        int fullLength = expected.length;
        int halfLength = fullLength / 2;

        testSubList(actual.subList(0, 0), new int[0]);
        testSubList(actual.subList(halfLength, halfLength), new int[0]);
        testSubList(actual.subList(fullLength, fullLength), new int[0]);
    }

    @Test
    public void testSubListFirstHalf() {
        int firstStart = 0;
        int firstEnd = expected.length / 2;
        int[] firstHalf = Arrays.copyOfRange(expected, firstStart, firstEnd);
        IntList subList = actual.subList(firstStart, firstEnd);

        testSubList(subList, firstHalf);
    }

    @Test
    public void testSubListSecondHalf() {
        int secondStart = expected.length / 2;
        int secondEnd = expected.length;
        int[] secondHalf = Arrays.copyOfRange(expected, secondStart, secondEnd);
        IntList subList = actual.subList(secondStart, secondEnd);

        testSubList(subList, secondHalf);
    }

    @Test
    public void testSubListMidHalf() {
        int halfLength = expected.length / 2;
        int midStart = halfLength / 2;
        int midEnd = midStart + halfLength;
        int[] midHalf = Arrays.copyOfRange(expected, midStart, midEnd);
        IntList subList = actual.subList(midStart, midEnd);

        testSubList(subList, midHalf);
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
