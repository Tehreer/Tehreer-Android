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
package com.mta.tehreer.internal.collections;

import com.mta.tehreer.collections.IntListTestSuite;

import org.junit.Before;

public class JIntArrayListTest extends IntListTestSuite {
    @Before
    public void setUp() {
        int[] sample = new int[] {
            0x00000000, 0x1C71C71C, 0x38E38E38, 0x55555554, 0x71C71C70,
            0x8E38E38C, 0xAAAAAAA8, 0xC71C71C4, 0xE38E38E0, 0xFFFFFFFC
        };

        this.expected = sample;
        this.actual = new JIntArrayList(sample, 0, sample.length);
    }
}
