/*
 * Copyright (C) 2022-2023 Muhammad Tayyab Akram
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

import androidx.annotation.NonNull;

import com.mta.tehreer.collections.IntListTestSuite;
import com.mta.tehreer.internal.Memory;
import com.mta.tehreer.internal.Raw;
import com.mta.tehreer.sfnt.ShapingResult.ClusterMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

@Ignore
public class ClusterMapTest extends IntListTestSuite<ClusterMap> {
    private final List<Long> pointers = new ArrayList<>();

    public ClusterMapTest() {
        super(ClusterMap.class);
    }

    private @NonNull ClusterMap buildList(@NonNull int[] values) {
        int capacity = values.length * Raw.INT32_SIZE;
        long pointer = Memory.allocate(capacity);
        pointers.add(pointer);

        ByteBuffer byteBuffer = Memory.buffer(pointer, capacity).order(ByteOrder.nativeOrder());
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(values);

        return new ClusterMap(this, pointer, values.length);
    }

    @Override
    protected @NonNull ClusterMap buildIdentical(@NonNull ClusterMap list) {
        return buildList(list.toArray());
    }

    @Before
    public void setUp() {
        values = new int[] {
            0x00000000, 0x1C71AAAA, 0x38E35554, 0x5554FFFE, 0x71C6AAA8,
            0x8E385552, 0xAAA9FFFC, 0xC71BAAA6, 0xE38D5550, 0xFFFEFFFA
        };
        subject = buildList(values);
    }

    @After
    public void tearDown() {
        for (long pointer : pointers) {
            Memory.dispose(pointer);
        }
    }
}
