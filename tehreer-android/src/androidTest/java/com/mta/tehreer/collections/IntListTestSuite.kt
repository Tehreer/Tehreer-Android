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

abstract class IntListTestSuite<T : IntList>(clazz: Class<T>) : HashableTestSuite<T>(clazz) {
    protected lateinit var values: IntArray

    private class SubListTestSuite(subList: IntList, values: IntArray) : IntListTestSuite<IntList>(IntList::class.java) {
        init {
            this.values = values
            this.subject = subList
        }

        protected override fun buildIdentical(obj: IntList): IntList {
            return IntList.of(*obj.toArray())
        }
    }

    @Test
    fun testSize() {
        // When
        var size: Int = subject.size()

        // Then
        assertEquals(size, values.size)
    }

    @Test
    fun testGetForNegativeIndex() {
        // Given
        var index: Int = -1

        // Then
        assertThrows(IndexOutOfBoundsException::class.java,
                     { subject.get(index) })
    }

    @Test()
    fun testGetForLimitIndex() {
        // Given
        var index: Int = values.size

        // Then
        assertThrows(IndexOutOfBoundsException::class.java,
                     { subject.get(index) })
    }

    @Test
    fun testGetForAllIndexes() {
        var length: Int = values.size
        for (i in 0 until length) {
            assertEquals(subject.get(i), values[i])
        }
    }

    @Test
    fun testCopyToForInvalidIndexes() {
        var array: IntArray = IntArray(values.size)

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
        var array: IntArray = IntArray(values.size / 2)

        // When
        assertThrows(ArrayIndexOutOfBoundsException::class.java,
                     { subject.copyTo(array, 0) })
    }

    @Test
    fun testCopyToOnMatchingArray() {
        var array: IntArray = IntArray(values.size)

        // When
        subject.copyTo(array, 0)

        // Then
        assertArrayEquals(array, values)
    }

    @Test
    fun testCopyToOnLargeArrayAtStart() {
        var actualLength: Int = values.size
        var extraLength: Int = actualLength / 2
        var finalLength: Int = actualLength + extraLength

        var array: IntArray = IntArray(finalLength)
        Arrays.fill(array, -1)

        var extraChunk: IntArray = Arrays.copyOfRange(array, actualLength, finalLength)

        // When
        subject.copyTo(array, 0)

        var copiedChunk: IntArray = Arrays.copyOfRange(array, 0, actualLength)
        var remainingChunk: IntArray = Arrays.copyOfRange(array, actualLength, finalLength)

        // Then
        assertArrayEquals(copiedChunk, values)
        assertArrayEquals(remainingChunk, extraChunk)
    }

    @Test
    fun testCopyToOnLargeArrayAtEnd() {
        var actualLength: Int = values.size
        var extraLength: Int = actualLength / 2
        var finalLength: Int = actualLength + extraLength

        var array: IntArray = IntArray(finalLength)
        Arrays.fill(array, -1)

        var extraChunk: IntArray = Arrays.copyOfRange(array, actualLength, finalLength)

        // When
        subject.copyTo(array, extraLength)

        var firstChunk: IntArray = Arrays.copyOfRange(array, 0, extraLength)
        var copiedChunk: IntArray = Arrays.copyOfRange(array, extraLength, finalLength)

        // Then
        assertArrayEquals(firstChunk, extraChunk)
        assertArrayEquals(copiedChunk, values)
    }

    private fun testSubList(subList: IntList, values: IntArray) {
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
                     { subject.subList(-1, values.size) })

        // Invalid End
        assertThrows(IndexOutOfBoundsException::class.java,
                     { subject.subList(0, values.size + 1) })

        // Bad Range
        assertThrows(IndexOutOfBoundsException::class.java,
                     { subject.subList(values.size, -1) })
    }

    @Test
    fun testSubListForEmptyRanges() {
        var fullLength: Int = values.size
        var halfLength: Int = fullLength / 2

        testSubList(subject.subList(0, 0), IntArray(0))
        testSubList(subject.subList(halfLength, halfLength), IntArray(0))
        testSubList(subject.subList(fullLength, fullLength), IntArray(0))
    }

    @Test
    fun testSubListForFirstHalf() {
        var startIndex: Int = 0
        var endIndex: Int = values.size / 2
        var firstHalf: IntArray = Arrays.copyOfRange(values, startIndex, endIndex)

        // When
        var subList: IntList = subject.subList(startIndex, endIndex)

        // Then
        testSubList(subList, firstHalf)
    }

    @Test
    fun testSubListForSecondHalf() {
        var startIndex: Int = values.size / 2
        var endIndex: Int = values.size
        var secondHalf: IntArray = Arrays.copyOfRange(values, startIndex, endIndex)

        // When
        var subList: IntList = subject.subList(startIndex, endIndex)

        // Then
        testSubList(subList, secondHalf)
    }

    @Test
    fun testSubListForMidHalf() {
        var halfLength: Int = values.size / 2
        var startIndex: Int = halfLength / 2
        var endIndex: Int = startIndex + halfLength
        var midHalf: IntArray = Arrays.copyOfRange(values, startIndex, endIndex)

        // When
        var subList: IntList = subject.subList(startIndex, endIndex)

        // Then
        testSubList(subList, midHalf)
    }

    @Test
    fun testToArray() {
        // When
        var array: IntArray = subject.toArray()

        // Then
        assertArrayEquals(array, values)
    }
}

