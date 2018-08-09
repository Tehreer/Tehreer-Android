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

package com.mta.tehreer.internal.layout;

import com.mta.tehreer.collections.IntListTestSuite;

import org.junit.Before;

public class ClusterMapTest extends IntListTestSuite {
    @Before
    public void setUp() {
        int[] sample = new int[] {
            0x0000FFFF, 0x1C72AAA9, 0x38E45553, 0x5555FFFD, 0x71C7AAA7,
            0x8E395551, 0xAAAAFFFB, 0xC71CAAA5, 0xE38E554F, 0xFFFFFFF9
        };
        int difference = 0xFFFF;

        this.expected = new int[] {
            0x00000000, 0x1C71AAAA, 0x38E35554, 0x5554FFFE, 0x71C6AAA8,
            0x8E385552, 0xAAA9FFFC, 0xC71BAAA6, 0xE38D5550, 0xFFFEFFFA
        };
        this.actual = new ClusterMap(sample, 0, sample.length, difference);
    }
}
