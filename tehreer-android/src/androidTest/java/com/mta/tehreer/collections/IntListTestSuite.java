/*
 * Copyright (C) 2018-2023 Muhammad Tayyab Akram
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

import static com.mta.tehreer.util.Assert.assertThrows;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import androidx.annotation.NonNull;

import com.mta.tehreer.test.HashableTestSuite;

import org.junit.Test;

import java.util.Arrays;

public abstract class IntListTestSuite<T extends IntList> extends HashableTestSuite<T> {
    protected int[] values;

    private static class SubListTestSuite extends IntListTestSuite<IntList> {
        SubListTestSuite(IntList subList, int[] values) {
            super(IntList.class);

            this.values = values;
            this.subject = subList;
        }

        @Override
        protected @NonNull
        IntList buildIdentical(@NonNull IntList object) {
            return IntList.of(object.toArray());
        }
    }

    protected IntListTestSuite(Class<T> clazz) {
        super(clazz);
    }

    @Test
    public void testSize() {
        // When
        int size = subject.size();

        // Then
        assertEquals(size, values.length);
    }

    @Test
    public void testGetForNegativeIndex() {
        // Given
        int index = -1;

        // Then
        assertThrows(IndexOutOfBoundsException.class,
                     () -> subject.get(index));
    }

    @Test()
    public void testGetForLimitIndex() {
        // Given
        int index = values.length;

        // Then
        assertThrows(IndexOutOfBoundsException.class,
                     () -> subject.get(index));
    }

    @Test
    public void testGetForAllIndexes() {
        int length = values.length;
        for (int i = 0; i < length; i++) {
            assertEquals(subject.get(i), values[i], 0.0f);
        }
    }

    @Test
    public void testCopyToForInvalidIndexes() {
        int[] array = new int[values.length];

        // Invalid Start
        assertThrows(ArrayIndexOutOfBoundsException.class,
                     () -> subject.copyTo(array, -1));

        // Exceeding Length
        assertThrows(ArrayIndexOutOfBoundsException.class,
                     () -> subject.copyTo(array, 1));
    }

    @Test
    public void testCopyToOnNullArray() {
        // Given
        int[] array = null;

        // Then
        assertThrows(NullPointerException.class,
                     () -> subject.copyTo(array, 0));
    }

    @Test
    public void testCopyToOnSmallArray() {
        if (values.length == 0) {
            return;
        }

        // Given
        int[] array = new int[values.length / 2];

        // When
        assertThrows(ArrayIndexOutOfBoundsException.class,
                     () -> subject.copyTo(array, 0));
    }

    @Test
    public void testCopyToOnMatchingArray() {
        int[] array = new int[values.length];

        // When
        subject.copyTo(array, 0);

        // Then
        assertArrayEquals(array, values);
    }

    @Test
    public void testCopyToOnLargeArrayAtStart() {
        int actualLength = values.length;
        int extraLength = actualLength / 2;
        int finalLength = actualLength + extraLength;

        int[] array = new int[finalLength];
        Arrays.fill(array, -1);

        int[] extraChunk = Arrays.copyOfRange(array, actualLength, finalLength);

        // When
        subject.copyTo(array, 0);

        int[] copiedChunk = Arrays.copyOfRange(array, 0, actualLength);
        int[] remainingChunk = Arrays.copyOfRange(array, actualLength, finalLength);

        // Then
        assertArrayEquals(copiedChunk, values);
        assertArrayEquals(remainingChunk, extraChunk);
    }

    @Test
    public void testCopyToOnLargeArrayAtEnd() {
        int actualLength = values.length;
        int extraLength = actualLength / 2;
        int finalLength = actualLength + extraLength;

        int[] array = new int[finalLength];
        Arrays.fill(array, -1);

        int[] extraChunk = Arrays.copyOfRange(array, actualLength, finalLength);

        // When
        subject.copyTo(array, extraLength);

        int[] firstChunk = Arrays.copyOfRange(array, 0, extraLength);
        int[] copiedChunk = Arrays.copyOfRange(array, extraLength, finalLength);

        // Then
        assertArrayEquals(firstChunk, extraChunk);
        assertArrayEquals(copiedChunk, values);
    }


    private static void testSubList(IntList subList, int[] values) {
        SubListTestSuite suite = new SubListTestSuite(subList, values);

        // equals()
        suite.testEqualsWithSelf();
        suite.testEqualsWithNull();
        suite.testEqualsWithIdenticalObject();

        // hashCode()
        suite.testHashCodeByMatchingWithIdenticalObject();
        suite.testHashCodeByGeneratingItFiveTimes();

        // size()
        suite.testSize();

        // get()
        suite.testGetForNegativeIndex();
        suite.testGetForLimitIndex();
        suite.testGetForAllIndexes();

        // copyTo()
        suite.testCopyToForInvalidIndexes();
        suite.testCopyToOnNullArray();
        suite.testCopyToOnSmallArray();
        suite.testCopyToOnMatchingArray();
        suite.testCopyToOnLargeArrayAtStart();
        suite.testCopyToOnLargeArrayAtEnd();

        // toArray()
        suite.testToArray();
    }

    @Test
    public void testSubListForInvalidRanges() {
        // Invalid Start
        assertThrows(IndexOutOfBoundsException.class,
                     () -> subject.subList(-1, values.length));

        // Invalid End
        assertThrows(IndexOutOfBoundsException.class,
                     () -> subject.subList(0, values.length + 1));

        // Bad Range
        assertThrows(IndexOutOfBoundsException.class,
                     () -> subject.subList(values.length, -1));
    }

    @Test
    public void testSubListForEmptyRanges() {
        int fullLength = values.length;
        int halfLength = fullLength / 2;

        testSubList(subject.subList(0, 0), new int[0]);
        testSubList(subject.subList(halfLength, halfLength), new int[0]);
        testSubList(subject.subList(fullLength, fullLength), new int[0]);
    }

    @Test
    public void testSubListForFirstHalf() {
        int startIndex = 0;
        int endIndex = values.length / 2;
        int[] firstHalf = Arrays.copyOfRange(values, startIndex, endIndex);

        // When
        IntList subList = subject.subList(startIndex, endIndex);

        // Then
        testSubList(subList, firstHalf);
    }

    @Test
    public void testSubListForSecondHalf() {
        int startIndex = values.length / 2;
        int endIndex = values.length;
        int[] secondHalf = Arrays.copyOfRange(values, startIndex, endIndex);

        // When
        IntList subList = subject.subList(startIndex, endIndex);

        // Then
        testSubList(subList, secondHalf);
    }

    @Test
    public void testSubListForMidHalf() {
        int halfLength = values.length / 2;
        int startIndex = halfLength / 2;
        int endIndex = startIndex + halfLength;
        int[] midHalf = Arrays.copyOfRange(values, startIndex, endIndex);

        // When
        IntList subList = subject.subList(startIndex, endIndex);

        // Then
        testSubList(subList, midHalf);
    }

    @Test
    public void testToArray() {
        // When
        int[] array = subject.toArray();

        // Then
        assertArrayEquals(array, values);
    }
}
