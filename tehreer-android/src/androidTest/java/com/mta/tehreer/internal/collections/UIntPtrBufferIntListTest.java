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
import java.nio.IntBuffer;
import java.nio.LongBuffer;

public class UIntPtrBufferIntListTest extends IntListTestSuite {
    private long pointer;

    private void setUpInts() {
        int[] sample = new int[] {
            0x00000000, 0x0E38E38E, 0x1C71C71C, 0x2AAAAAAA, 0x38E38E38,
            0x471C71C6, 0x55555554, 0x638E38E2, 0x71C71C70, 0x7FFFFFFE
        };
        int capacity = sample.length * 4;

        this.pointer = Memory.allocate(capacity);
        this.expected = sample;
        this.actual = new UIntPtrBufferIntList(null, pointer, sample.length);

        ByteBuffer byteBuffer = Memory.buffer(pointer, capacity).order(ByteOrder.nativeOrder());
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(sample);
    }

    private void setUpLongs() {
        long[] sample = new long[] {
            0x00000000, 0x0E38E38E, 0x1C71C71C, 0x2AAAAAAA, 0x38E38E38,
            0x471C71C6, 0x55555554, 0x638E38E2, 0x71C71C70, 0x7FFFFFFE
        };
        int capacity = sample.length * 8;

        this.pointer = Memory.allocate(capacity);
        this.expected = new int[] { 0x00000000, 0x0E38E38E, 0x1C71C71C, 0x2AAAAAAA, 0x38E38E38,
                                    0x471C71C6, 0x55555554, 0x638E38E2, 0x71C71C70, 0x7FFFFFFE };
        this.actual = new UIntPtrBufferIntList(null, pointer, sample.length);

        ByteBuffer byteBuffer = Memory.buffer(pointer, capacity).order(ByteOrder.nativeOrder());
        LongBuffer longBuffer = byteBuffer.asLongBuffer();
        longBuffer.put(sample);
    }

    @Before
    public void setUp() {
        switch (Memory.pointerSize()) {
        case 4:
            setUpInts();
            break;

        case 8:
            setUpLongs();
            break;

        default:
            throw new UnsupportedOperationException();
        }
    }

    @After
    public void tearDown() {
        Memory.dispose(pointer);
    }
}
