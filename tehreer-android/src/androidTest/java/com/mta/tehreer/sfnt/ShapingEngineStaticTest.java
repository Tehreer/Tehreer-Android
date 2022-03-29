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

package com.mta.tehreer.sfnt;

import static org.junit.Assert.assertEquals;

import com.mta.tehreer.DisposableTestSuite;
import com.mta.tehreer.sfnt.ShapingEngineTestSuite.ShapingEngineBuilder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ShapingEngineStaticTest extends DisposableTestSuite.StaticTestSuite<ShapingEngine, ShapingEngine.Finalizable> {
    public ShapingEngineStaticTest() {
        super(new ShapingEngineBuilder());
    }

    @Test
    public void testGetScriptDirectionForArabic() {
        // Given
        int scriptTag = SfntTag.make("arab");

        // When
        WritingDirection writingDirection = ShapingEngine.getScriptDirection(scriptTag);

        // Then
        assertEquals(writingDirection, WritingDirection.RIGHT_TO_LEFT);
    }

    @Test
    public void testGetScriptDirectionForLatin() {
        // Given
        int scriptTag = SfntTag.make("latn");

        // When
        WritingDirection writingDirection = ShapingEngine.getScriptDirection(scriptTag);

        // Then
        assertEquals(writingDirection, WritingDirection.LEFT_TO_RIGHT);
    }
}
