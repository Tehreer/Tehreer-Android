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

public class JIntArrayListTest extends IntListTestSuite<JIntArrayList> {
    public JIntArrayListTest() {
        super(JIntArrayList.class);
    }

    private @NonNull JIntArrayList buildList(@NonNull int[] values) {
        return new JIntArrayList(values, 0, values.length);
    }

    @Override
    protected @NonNull JIntArrayList buildIdentical(@NonNull JIntArrayList list) {
        return buildList(list.toArray());
    }

    @Before
    public void setUp() {
        values = new int[] {
            0x00000000, 0x1C71C71C, 0x38E38E38, 0x55555554, 0x71C71C70,
            0x8E38E38C, 0xAAAAAAA8, 0xC71C71C4, 0xE38E38E0, 0xFFFFFFFC
        };
        subject = buildList(values);
    }
}
