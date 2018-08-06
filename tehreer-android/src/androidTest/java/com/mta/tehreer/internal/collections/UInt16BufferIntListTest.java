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
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

public class UInt16BufferIntListTest extends IntListTestSuite {
    private long pointer;

    @Before
    public void setUp() {
        short[] sample = new short[] {
            (short) 0x0000, (short) 0x1C71, (short) 0x38E2, (short) 0x5553, (short) 0x71C4,
            (short) 0x8E35, (short) 0xAAA6, (short) 0xC717, (short) 0xE388, (short) 0xFFF9
        };
        int capacity = sample.length * 2;

        this.pointer = Memory.allocate(capacity);
        this.expected = new int[] { 0x0000, 0x1C71, 0x38E2, 0x5553, 0x71C4,
                                    0x8E35, 0xAAA6, 0xC717, 0xE388, 0xFFF9 };
        this.actual = new UInt16BufferIntList(null, pointer, sample.length);

        ByteBuffer byteBuffer = Memory.buffer(pointer, capacity);
        ShortBuffer shortBuffer = byteBuffer.order(ByteOrder.nativeOrder()).asShortBuffer();
        shortBuffer.put(sample);
    }

    @After
    public void tearDown() {
        Memory.dispose(pointer);
    }
}
