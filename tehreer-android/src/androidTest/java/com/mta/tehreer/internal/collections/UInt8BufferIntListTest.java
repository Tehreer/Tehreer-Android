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
import com.mta.tehreer.internal.Memory;

import org.junit.After;
import org.junit.Before;

import java.nio.ByteBuffer;

public class UInt8BufferIntListTest extends IntListTestSuite {
    private long pointer;

    @Before
    public void setUp() {
        byte[] sample = new byte[] {
            (byte) 0x00, (byte) 0x1C, (byte) 0x38, (byte) 0x54, (byte) 0x70,
            (byte) 0x8C, (byte) 0xA8, (byte) 0xC4, (byte) 0xE0, (byte) 0xFC
        };

        this.pointer = Memory.allocate(sample.length);
        this.expected = new int[] { 0x00, 0x1C, 0x38, 0x54, 0x70, 0x8C, 0xA8, 0xC4, 0xE0, 0xFC };
        this.actual = new UInt8BufferIntList(null, pointer, sample.length);

        ByteBuffer buffer = Memory.buffer(pointer, sample.length);
        buffer.put(sample);
    }

    @After
    public void tearDown() {
        Memory.dispose(pointer);
    }
}
