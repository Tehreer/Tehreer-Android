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

package com.mta.tehreer.unicode

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue


import com.mta.tehreer.test.HashableTestSuite
import com.mta.tehreer.util.DescriptionBuilder

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class BidiRunTest : HashableTestSuite<BidiRun>(BidiRun::class.java) {
    private var startIndex: Int = 0
    private var endIndex: Int = 4
    private var embeddingLevel: Byte = 1

    protected override fun buildIdentical(bidiRun: BidiRun): BidiRun {
        return BidiRun(bidiRun.charStart, bidiRun.charEnd, bidiRun.embeddingLevel)
    }

    @Before
    fun setUp() {
        subject = BidiRun(startIndex, endIndex, embeddingLevel)
    }

    @Test
    fun testIsRightToLeftForEvenLevel() {
        // Given
        subject.embeddingLevel = 0

        // When
        val isRightToLeft = subject.isRightToLeft

        // Then
        assertFalse(isRightToLeft)
    }

    @Test
    fun testIsRightToLeftForOddLevel() {
        // Given
        subject.embeddingLevel = 1

        // When
        val isRightToLeft = subject.isRightToLeft

        // Then
        assertTrue(isRightToLeft)
    }

    @Test
    fun testToString() {
        val description = DescriptionBuilder
                .of(BidiRun::class.java)
                .put("charStart", startIndex)
                .put("charEnd", endIndex)
                .put("embeddingLevel", embeddingLevel)
                .build()

        // When
        val string = subject.toString()

        // Then
        assertEquals(string, description)
    }
}


