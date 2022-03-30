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

public class ShapingOrderTest {
    private static final int FORWARD = 0;
    private static final int BACKWARD = 1;
    private static final int LIMIT = 2;

    @Test
    public void testValues() {
        assertEquals(ShapingOrder.FORWARD.value, FORWARD);
        assertEquals(ShapingOrder.BACKWARD.value, BACKWARD);
    }

    @Test
    public void testValueOf() {
        assertEquals(ShapingOrder.valueOf(FORWARD), ShapingOrder.FORWARD);
        assertEquals(ShapingOrder.valueOf(BACKWARD), ShapingOrder.BACKWARD);
        assertNull(ShapingOrder.valueOf(LIMIT));
    }
}
