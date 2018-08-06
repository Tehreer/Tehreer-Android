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

public class JByteArrayIntListTest extends IntListTestSuite {
    public JByteArrayIntListTest() {
        byte[] sample = new byte[] { 0, 28, 56, 84, 112, -116, -88, -60, -32, -4 };
        this.expected = new int[] { 0, 28, 56, 84, 112, -116, -88, -60, -32, -4 };
        this.actual = new JByteArrayIntList(sample, 0, sample.length);
    }
}
