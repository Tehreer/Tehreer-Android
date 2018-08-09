/*
 * Copyright (C) 2018 Muhammad Tayyab Akram
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

package com.mta.tehreer.graphics;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TypeSlopeTest {
    @Test
    public void testOrdinals() {
        assertEquals(TypeSlope.PLAIN.ordinal(), 0);
        assertEquals(TypeSlope.ITALIC.ordinal(), 1);
        assertEquals(TypeSlope.OBLIQUE.ordinal(), 2);
    }
}
