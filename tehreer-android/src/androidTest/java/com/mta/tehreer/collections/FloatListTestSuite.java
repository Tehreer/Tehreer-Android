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

package com.mta.tehreer.collections;

import static com.mta.tehreer.util.Assert.assertThrows;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import androidx.annotation.NonNull;

import com.mta.tehreer.test.HashableTestSuite;

import org.junit.Test;

import java.util.Arrays;

public abstract class FloatListTestSuite<T extends FloatList> extends HashableTestSuite<T> {
    protected float[] values;

    private static class SubListTestSuite extends FloatListTestSuite<FloatList> {
        SubListTestSuite(FloatList subList, float[] values) {
            super(FloatList.class);

            this.values = values;
            this.sut = subList;
        }

        @Override
        protected @NonNull FloatList buildIdentical(@NonNull FloatList object) {
            return FloatList.of(object.toArray());
        }
    }

    protected FloatListTestSuite(Class<T> clazz) {
        super(clazz);
    }

    @Test
    public void testSize() {
        // When
        int size = sut.size();

        // Then
        assertEquals(size, values.length);
    }

    @Test
    public void testGetForNegativeIndex() {
        // Given
        int index = -1;

        // Then
        assertThrows(IndexOutOfBoundsException.class,
                     () -> sut.get(index));
    }

    @Test()
    public void testGetForLimitIndex() {
        // Given
        int index = values.length;

        // Then
        assertThrows(IndexOutOfBoundsException.class,
                     () -> sut.get(index));
    }

    @Test
    public void testGetForAllIndexes() {
        int length = values.length;
        for (int i = 0; i < length; i++) {
            assertEquals(sut.get(i), values[i], 0.0f);
        }
    }

    @Test
    public void testCopyToForInvalidIndexes() {
        float[] array = new float[values.length];

        // Invalid Start
        assertThrows(ArrayIndexOutOfBoundsException.class,
                     () -> sut.copyTo(array, -1));

        // Exceeding Length
        assertThrows(ArrayIndexOutOfBoundsException.class,
                     () -> sut.copyTo(array, 1));
    }

    @Test
    public void testCopyToOnNullArray() {
        // Given
        float[] array = null;

        // Then
        assertThrows(NullPointerException.class,
                     () -> sut.copyTo(array, 0));
    }

    @Test
    public void testCopyToOnSmallArray() {
        if (values.length == 0) {
            return;
        }

        // Given
        float[] array = new float[values.length / 2];

        // When
        assertThrows(ArrayIndexOutOfBoundsException.class,
                     () -> sut.copyTo(array, 0));
    }

    @Test
    public void testCopyToOnMatchingArray() {
        float[] array = new float[values.length];

        // When
        sut.copyTo(array, 0);

        // Then
        assertArrayEquals(array, values, 0.0f);
    }

    @Test
    public void testCopyToOnLargeArrayAtStart() {
        int actualLength = values.length;
        int extraLength = actualLength / 2;
        int finalLength = actualLength + extraLength;

        float[] array = new float[finalLength];
        Arrays.fill(array, -1.0f);

        float[] extraChunk = Arrays.copyOfRange(array, actualLength, finalLength);

        // When
        sut.copyTo(array, 0);

        float[] copiedChunk = Arrays.copyOfRange(array, 0, actualLength);
        float[] remainingChunk = Arrays.copyOfRange(array, actualLength, finalLength);

        // Then
        assertArrayEquals(copiedChunk, values, 0.0f);
        assertArrayEquals(remainingChunk, extraChunk, 0.0f);
    }

    @Test
    public void testCopyToOnLargeArrayAtEnd() {
        int actualLength = values.length;
        int extraLength = actualLength / 2;
        int finalLength = actualLength + extraLength;

        float[] array = new float[finalLength];
        Arrays.fill(array, -1.0f);

        float[] extraChunk = Arrays.copyOfRange(array, actualLength, finalLength);

        // When
        sut.copyTo(array, extraLength);

        float[] firstChunk = Arrays.copyOfRange(array, 0, extraLength);
        float[] copiedChunk = Arrays.copyOfRange(array, extraLength, finalLength);

        // Then
        assertArrayEquals(firstChunk, extraChunk, 0.0f);
        assertArrayEquals(copiedChunk, values, 0.0f);
    }


    private static void testSubList(FloatList subList, float[] values) {
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
                     () -> sut.subList(-1, values.length));

        // Invalid End
        assertThrows(IndexOutOfBoundsException.class,
                     () -> sut.subList(0, values.length + 1));

        // Bad Range
        assertThrows(IndexOutOfBoundsException.class,
                     () -> sut.subList(values.length, -1));
    }

    @Test
    public void testSubListForEmptyRanges() {
        int fullLength = values.length;
        int halfLength = fullLength / 2;

        testSubList(sut.subList(0, 0), new float[0]);
        testSubList(sut.subList(halfLength, halfLength), new float[0]);
        testSubList(sut.subList(fullLength, fullLength), new float[0]);
    }

    @Test
    public void testSubListForFirstHalf() {
        int startIndex = 0;
        int endIndex = values.length / 2;
        float[] firstHalf = Arrays.copyOfRange(values, startIndex, endIndex);

        // When
        FloatList subList = sut.subList(startIndex, endIndex);

        // Then
        testSubList(subList, firstHalf);
    }

    @Test
    public void testSubListForSecondHalf() {
        int startIndex = values.length / 2;
        int endIndex = values.length;
        float[] secondHalf = Arrays.copyOfRange(values, startIndex, endIndex);

        // When
        FloatList subList = sut.subList(startIndex, endIndex);

        // Then
        testSubList(subList, secondHalf);
    }

    @Test
    public void testSubListForMidHalf() {
        int halfLength = values.length / 2;
        int startIndex = halfLength / 2;
        int endIndex = startIndex + halfLength;
        float[] midHalf = Arrays.copyOfRange(values, startIndex, endIndex);

        // When
        FloatList subList = sut.subList(startIndex, endIndex);

        // Then
        testSubList(subList, midHalf);
    }

    @Test
    public void testToArray() {
        // When
        float[] array = sut.toArray();

        // Then
        assertArrayEquals(array, values, 0.0f);
    }
}
