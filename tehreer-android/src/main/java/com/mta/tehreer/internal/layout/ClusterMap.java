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

package com.mta.tehreer.internal.layout;

import androidx.annotation.NonNull;

import com.mta.tehreer.collections.IntList;

import static com.mta.tehreer.internal.util.Preconditions.checkArrayBounds;
import static com.mta.tehreer.internal.util.Preconditions.checkElementIndex;
import static com.mta.tehreer.internal.util.Preconditions.checkIndexRange;
import static com.mta.tehreer.internal.util.Preconditions.checkNotNull;

public class ClusterMap extends IntList {
    private final @NonNull int[] array;
    private final int offset;
    private final int size;
    private final int difference;

    public ClusterMap(@NonNull int[] array, int offset, int size, int difference) {
        this.array = array;
        this.offset = offset;
        this.size = size;
        this.difference = difference;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int get(int index) {
        checkElementIndex(index, size);

        return array[index + offset] - difference;
    }

    @Override
    public void copyTo(@NonNull int[] array, int atIndex) {
        checkNotNull(array);
        checkArrayBounds(array, atIndex, size);

        for (int i = 0; i < size; i++) {
            array[i + atIndex] = this.array[i + offset] - difference;
        }
    }

    @Override
    public @NonNull IntList subList(int fromIndex, int toIndex) {
        checkIndexRange(fromIndex, toIndex, size);

        return new ClusterMap(array, offset + fromIndex, toIndex - fromIndex, difference);
    }
}
