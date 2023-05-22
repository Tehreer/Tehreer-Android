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

package com.mta.tehreer.unicode;

import static com.mta.tehreer.util.Assert.assertThrows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BidiAlgorithmDisposableTest extends BidiAlgorithmTestSuite {
    public BidiAlgorithmDisposableTest() {
        super(DefaultMode.DISPOSABLE);
    }

    @Test
    public void testConstructorForNullText() {
        // Given
        text = null;

        // Then
        assertThrows(NullPointerException.class, "text",
                     () -> buildSubject((subject) -> { }));
    }

    @Test
    public void testConstructorForEmptyText() {
        // Given
        text = "";

        // Then
        assertThrows(IllegalArgumentException.class, "Text is empty",
                     () -> buildSubject((subject) -> { }));
    }
}
