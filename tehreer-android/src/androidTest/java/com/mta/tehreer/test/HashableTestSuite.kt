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

package com.mta.tehreer.test

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.mockito.Mockito.mock
import org.junit.Test

abstract class HashableTestSuite<T : Any>(protected val clazz: Class<T>) {
    protected lateinit var subject: T

    protected abstract fun buildIdentical(obj: T): T

    @Test
    open fun testEqualsWithSelf() {
        // When
        val isEqual = subject.equals(subject)

        // Then
        assertTrue(isEqual)
    }

    @Test
    open fun testEqualsWithNull() {
        // When
        val isEqual = subject.equals(null)

        // Then
        assertFalse(isEqual)
    }

    @Test
    open fun testEqualsWithIdenticalObject() {
        // Given
        val other = buildIdentical(subject)

        // When
        val isEqual = subject.equals(other)

        // Then
        assertTrue(isEqual)
    }

    @Test
    open fun testEqualsWithMockObject() {
        // Given
        val mock = mock(clazz)

        // When
        val isEqual = subject.equals(mock)

        // Then
        assertFalse(isEqual)
    }

    @Test
    open fun testHashCodeByMatchingWithIdenticalObject() {
        // Given
        val other = buildIdentical(subject)

        // When
        val hashCode = subject.hashCode()

        // Then
        assertEquals(hashCode, other.hashCode())
    }

    @Test
    open fun testHashCodeByGeneratingItFiveTimes() {
        // Given
        val hashCode = subject.hashCode()

        // Then
        assertEquals(hashCode, subject.hashCode())
        assertEquals(hashCode, subject.hashCode())
        assertEquals(hashCode, subject.hashCode())
        assertEquals(hashCode, subject.hashCode())
    }
}

