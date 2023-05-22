/*
 * Copyright (C) 2022-2023 Muhammad Tayyab Akram
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

package com.mta.tehreer.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import androidx.annotation.NonNull;

import org.junit.Test;

public abstract class HashableTestSuite<T> {
    protected final Class<T> clazz;

    protected T subject;

    protected HashableTestSuite(Class<T> clazz) {
        this.clazz = clazz;
    }

    protected abstract @NonNull T buildIdentical(T object);

    @Test
    public void testEqualsWithSelf() {
        // When
        boolean isEqual = subject.equals(subject);

        // Then
        assertTrue(isEqual);
    }

    @Test
    public void testEqualsWithNull() {
        // When
        boolean isEqual = subject.equals(null);

        // Then
        assertFalse(isEqual);
    }

    @Test
    public void testEqualsWithIdenticalObject() {
        // Given
        T other = buildIdentical(subject);

        // When
        boolean isEqual = subject.equals(other);

        // Then
        assertTrue(isEqual);
    }

    @Test
    public void testEqualsWithMockObject() {
        // Given
        T mock = mock(clazz);

        // When
        boolean isEqual = subject.equals(mock);

        // Then
        assertFalse(isEqual);
    }

    @Test
    public void testHashCodeByMatchingWithIdenticalObject() {
        // Given
        T other = buildIdentical(subject);

        // When
        int hashCode = subject.hashCode();

        // Then
        assertEquals(hashCode, other.hashCode());
    }

    @Test
    public void testHashCodeByGeneratingItFiveTimes() {
        // Given
        int hashCode = subject.hashCode();

        // Then
        assertEquals(hashCode, subject.hashCode());
        assertEquals(hashCode, subject.hashCode());
        assertEquals(hashCode, subject.hashCode());
        assertEquals(hashCode, subject.hashCode());
    }
}
