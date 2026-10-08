/*
 * Copyright (C) 2022-2026 Muhammad Tayyab Akram
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

package com.mta.tehreer.sfnt

import org.junit.Assert.assertEquals

import com.mta.tehreer.DisposableTestSuite
import com.mta.tehreer.sfnt.ShapingEngineTestSuite.ShapingEngineBuilder

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ShapingEngineStaticTest : DisposableTestSuite.StaticTestSuite<ShapingEngine>(ShapingEngineBuilder()) {
    @Test
    fun testGetScriptDirectionForArabic() {
        // Given
        val scriptTag = SfntTag.make("arab")

        // When
        val writingDirection = ShapingEngine.getScriptDirection(scriptTag)

        // Then
        assertEquals(writingDirection, WritingDirection.RIGHT_TO_LEFT)
    }

    @Test
    fun testGetScriptDirectionForLatin() {
        // Given
        val scriptTag = SfntTag.make("latn")

        // When
        val writingDirection = ShapingEngine.getScriptDirection(scriptTag)

        // Then
        assertEquals(writingDirection, WritingDirection.LEFT_TO_RIGHT)
    }
}


