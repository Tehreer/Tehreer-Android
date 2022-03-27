/*
 * Copyright (C) 2018-2022 Muhammad Tayyab Akram
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

import androidx.annotation.NonNull;

import com.mta.tehreer.collections.IntListTestSuite;
import com.mta.tehreer.internal.Memory;

import org.junit.After;
import org.junit.Before;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class UInt8BufferIntListTest extends IntListTestSuite<UInt8BufferIntList> {
    private final List<Long> pointers = new ArrayList<>();

    public UInt8BufferIntListTest() {
        super(UInt8BufferIntList.class);
    }

    private @NonNull byte[] toByteArray(@NonNull int[] values) {
        int length = values.length;
        byte[] array = new byte[length];

        for (int i = 0; i < length; i++) {
            array[i] = (byte) values[i];
        }

        return array;
    }

    private @NonNull UInt8BufferIntList buildList(@NonNull int[] values) {
        long pointer = Memory.allocate(values.length);
        pointers.add(pointer);

        ByteBuffer buffer = Memory.buffer(pointer, values.length);
        buffer.put(toByteArray(values));

        return new UInt8BufferIntList(this, pointer, values.length);
    }

    @Override
    protected @NonNull UInt8BufferIntList buildIdentical(@NonNull UInt8BufferIntList list) {
        return buildList(list.toArray());
    }

    @Before
    public void setUp() {
        values = new int[] {
            0x00, 0x1C, 0x38, 0x54, 0x70, 0x8C, 0xA8, 0xC4, 0xE0, 0xFC
        };
        sut = buildList(values);
    }

    @After
    public void tearDown() {
        for (long pointer : pointers) {
            Memory.dispose(pointer);
        }
    }
}
