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
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class WritingDirectionTest {
    private static final int LEFT_TO_RIGHT = 0;
    private static final int RIGHT_TO_LEFT = 1;
    private static final int LIMIT = 2;

    @Test
    public void testValues() {
        assertEquals(WritingDirection.LEFT_TO_RIGHT.value, LEFT_TO_RIGHT);
        assertEquals(WritingDirection.RIGHT_TO_LEFT.value, RIGHT_TO_LEFT);
    }

    @Test
    public void testValueOf() {
        assertEquals(WritingDirection.valueOf(LEFT_TO_RIGHT), WritingDirection.LEFT_TO_RIGHT);
        assertEquals(WritingDirection.valueOf(RIGHT_TO_LEFT), WritingDirection.RIGHT_TO_LEFT);
        assertNull(WritingDirection.valueOf(LIMIT));
    }
}
