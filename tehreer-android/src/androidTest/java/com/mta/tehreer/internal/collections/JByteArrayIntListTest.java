/*
 * Copyright (C) 2018-2023 Muhammad Tayyab Akram
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

import org.junit.Before;

public class JByteArrayIntListTest extends IntListTestSuite<JByteArrayIntList> {
    public JByteArrayIntListTest() {
        super(JByteArrayIntList.class);
    }

    private @NonNull byte[] toByteArray(@NonNull int[] values) {
        int length = values.length;
        byte[] array = new byte[length];

        for (int i = 0; i < length; i++) {
            array[i] = (byte) values[i];
        }

        return array;
    }

    private @NonNull JByteArrayIntList buildList(@NonNull int[] values) {
        return new JByteArrayIntList(toByteArray(values), 0, values.length);
    }

    @Override
    protected @NonNull JByteArrayIntList buildIdentical(@NonNull JByteArrayIntList list) {
        return buildList(list.toArray());
    }

    @Before
    public void setUp() {
        values = new int[] {
            0, 28, 56, 84, 112, -116, -88, -60, -32, -4
        };
        subject = buildList(values);
    }
}
