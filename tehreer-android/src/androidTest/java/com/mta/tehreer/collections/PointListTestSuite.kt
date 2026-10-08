/*
 * Copyright (C) 2026 Muhammad Tayyab Akram
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

package com.mta.tehreer.collections

import com.mta.tehreer.util.Assert.assertThrows
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals

import com.mta.tehreer.test.HashableTestSuite

import org.junit.Test

import java.util.Arrays

abstract class PointListTestSuite<T : PointList>(clazz: Class<T>) : HashableTestSuite<T>(clazz) {
    protected lateinit var values: FloatArray

    private class SubListTestSuite(subList: PointList, values: FloatArray) : PointListTestSuite<PointList>(PointList::class.java) {
        init {
            this.values = values
            this.subject = subList
        }

        protected override fun buildIdentical(obj: PointList): PointList {
            return PointList.of(*obj.toArray())
        }
    }

    private fun numberOfPoints(): Int {
        return values.size / 2
    }

    private fun copyPoints(startIndex: Int, endIndex: Int): FloatArray {
        return Arrays.copyOfRange(values, startIndex * 2, endIndex * 2)
    }

    @Test
    fun testSize() {
        // When
        var size: Int = subject.size()

        // Then
        assertEquals(size, numberOfPoints())
    }

    @Test
    fun testGetForNegativeIndex() {
        // Given
        var index: Int = -1

        // Then
        assertThrows(IndexOutOfBoundsException::class.java,
                     { subject.getX(index) })
        assertThrows(IndexOutOfBoundsException::class.java,
                     { subject.getY(index) })
    }

    @Test()
    fun testGetForLimitIndex() {
        // Given
        var index: Int = numberOfPoints()

        // Then
        assertThrows(IndexOutOfBoundsException::class.java,
                     { subject.getX(index) })
        assertThrows(IndexOutOfBoundsException::class.java,
                     { subject.getY(index) })
    }

    @Test
    fun testGetForAllIndexes() {
        var length: Int = numberOfPoints()
        for (i in 0 until length) {
            val firstIndex = i * 2
            val secondIndex = firstIndex + 1

            assertEquals(subject.getX(i), values[firstIndex], 0.0f)
            assertEquals(subject.getY(i), values[secondIndex], 0.0f)
        }
    }

    @Test
    fun testCopyToForInvalidIndexes() {
        var array: FloatArray = FloatArray(values.size)

        // Invalid Start
        assertThrows(ArrayIndexOutOfBoundsException::class.java,
                     { subject.copyTo(array, -1) })

        // Exceeding Length
        assertThrows(ArrayIndexOutOfBoundsException::class.java,
                     { subject.copyTo(array, 1) })
    }

    @Test
    fun testCopyToOnSmallArray() {
        if (values.size == 0) {
            return
        }

        // Given
        var array: FloatArray = FloatArray(values.size / 2)

        // When
        assertThrows(ArrayIndexOutOfBoundsException::class.java,
                     { subject.copyTo(array, 0) })
    }

    @Test
    fun testCopyToOnMatchingArray() {
        var array: FloatArray = FloatArray(values.size)

        // When
        subject.copyTo(array, 0)

        // Then
        assertArrayEquals(array, values, 0.0f)
    }

    @Test
    fun testCopyToOnLargeArrayAtStart() {
        var actualLength: Int = values.size
        var extraLength: Int = actualLength / 2
        var finalLength: Int = actualLength + extraLength

        var array: FloatArray = FloatArray(finalLength)
        Arrays.fill(array, -1.0f)

        var extraChunk: FloatArray = Arrays.copyOfRange(array, actualLength, finalLength)

        // When
        subject.copyTo(array, 0)

        var copiedChunk: FloatArray = Arrays.copyOfRange(array, 0, actualLength)
        var remainingChunk: FloatArray = Arrays.copyOfRange(array, actualLength, finalLength)

        // Then
        assertArrayEquals(copiedChunk, values, 0.0f)
        assertArrayEquals(remainingChunk, extraChunk, 0.0f)
    }

    @Test
    fun testCopyToOnLargeArrayAtEnd() {
        var actualLength: Int = values.size
        var extraLength: Int = actualLength / 2
        var finalLength: Int = actualLength + extraLength

        var array: FloatArray = FloatArray(finalLength)
        Arrays.fill(array, -1.0f)

        var extraChunk: FloatArray = Arrays.copyOfRange(array, actualLength, finalLength)

        // When
        subject.copyTo(array, extraLength)

        var firstChunk: FloatArray = Arrays.copyOfRange(array, 0, extraLength)
        var copiedChunk: FloatArray = Arrays.copyOfRange(array, extraLength, finalLength)

        // Then
        assertArrayEquals(firstChunk, extraChunk, 0.0f)
        assertArrayEquals(copiedChunk, values, 0.0f)
    }

    private fun testSubList(subList: PointList, values: FloatArray) {
        var suite: SubListTestSuite = SubListTestSuite(subList, values)

        // equals()
        suite.testEqualsWithSelf()
        suite.testEqualsWithNull()
        suite.testEqualsWithIdenticalObject()

        // hashCode()
        suite.testHashCodeByMatchingWithIdenticalObject()
        suite.testHashCodeByGeneratingItFiveTimes()

        // size()
        suite.testSize()

        // get()
        suite.testGetForNegativeIndex()
        suite.testGetForLimitIndex()
        suite.testGetForAllIndexes()

        // copyTo()
        suite.testCopyToForInvalidIndexes()
        suite.testCopyToOnSmallArray()
        suite.testCopyToOnMatchingArray()
        suite.testCopyToOnLargeArrayAtStart()
        suite.testCopyToOnLargeArrayAtEnd()

        // toArray()
        suite.testToArray()
    }

    @Test
    fun testSubListForInvalidRanges() {
        // Invalid Start
        assertThrows(IndexOutOfBoundsException::class.java,
                     { subject.subList(-1, numberOfPoints()) })

        // Invalid End
        assertThrows(IndexOutOfBoundsException::class.java,
                     { subject.subList(0, numberOfPoints() + 1) })

        // Bad Range
        assertThrows(IndexOutOfBoundsException::class.java,
                     { subject.subList(numberOfPoints(), -1) })
    }

    @Test
    fun testSubListForEmptyRanges() {
        var fullLength: Int = numberOfPoints()
        var halfLength: Int = fullLength / 2

        testSubList(subject.subList(0, 0), FloatArray(0))
        testSubList(subject.subList(halfLength, halfLength), FloatArray(0))
        testSubList(subject.subList(fullLength, fullLength), FloatArray(0))
    }

    @Test
    fun testSubListForFirstHalf() {
        var startIndex: Int = 0
        var endIndex: Int = numberOfPoints() / 2
        var firstHalf: FloatArray = copyPoints(startIndex, endIndex)

        // When
        var subList: PointList = subject.subList(startIndex, endIndex)

        // Then
        testSubList(subList, firstHalf)
    }

    @Test
    fun testSubListForSecondHalf() {
        var startIndex: Int = numberOfPoints() / 2
        var endIndex: Int = numberOfPoints()
        var secondHalf: FloatArray = copyPoints(startIndex, endIndex)

        // When
        var subList: PointList = subject.subList(startIndex, endIndex)

        // Then
        testSubList(subList, secondHalf)
    }

    @Test
    fun testSubListForMidHalf() {
        var halfLength: Int = numberOfPoints() / 2
        var startIndex: Int = halfLength / 2
        var endIndex: Int = startIndex + halfLength
        var midHalf: FloatArray = copyPoints(startIndex, endIndex)

        // When
        var subList: PointList = subject.subList(startIndex, endIndex)

        // Then
        testSubList(subList, midHalf)
    }

    @Test
    fun testToArray() {
        // When
        var array: FloatArray = subject.toArray()

        // Then
        assertArrayEquals(array, values, 0.0f)
    }
}

