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


import com.mta.tehreer.test.HashableTestSuite
import com.mta.tehreer.util.DescriptionBuilder

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class BidiPairTest : HashableTestSuite<BidiPair>(BidiPair::class.java) {
    private var charIndex: Int = 0
    private var actualCodePoint: Int = '('.code
    private var pairingCodePoint: Int = ')'.code

    protected override fun buildIdentical(bidiPair: BidiPair): BidiPair {
        return BidiPair(bidiPair.charIndex, bidiPair.actualCodePoint, bidiPair.pairingCodePoint)
    }

    @Before
    fun setUp() {
        subject = BidiPair(charIndex, actualCodePoint, pairingCodePoint)
    }

    @Test
    fun testToString() {
        val description = DescriptionBuilder
                .of(BidiPair::class.java)
                .put("charIndex", charIndex)
                .put("actualCodePoint", actualCodePoint)
                .put("pairingCodePoint", pairingCodePoint)
                .build()

        // When
        val string = subject.toString()

        // Then
        assertEquals(string, description)
    }
}


