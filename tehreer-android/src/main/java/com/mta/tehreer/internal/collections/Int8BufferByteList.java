/*
 * Copyright (C) 2017-2018 Muhammad Tayyab Akram
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
import androidx.annotation.Nullable;

import com.mta.tehreer.collections.ByteList;
import com.mta.tehreer.internal.Raw;

import static com.mta.tehreer.internal.util.Preconditions.checkArrayBounds;
import static com.mta.tehreer.internal.util.Preconditions.checkElementIndex;
import static com.mta.tehreer.internal.util.Preconditions.checkIndexRange;
import static com.mta.tehreer.internal.util.Preconditions.checkNotNull;

public class Int8BufferByteList extends ByteList {
    private final @Nullable Object owner;
    private final long pointer;
    private final int size;

    public Int8BufferByteList(@Nullable Object owner, long pointer, int size) {
        this.owner = owner;
        this.pointer = pointer;
        this.size = size;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public byte get(int index) {
        checkElementIndex(index, size);

        return Raw.getInt8Value(pointer + (index * Raw.INT8_SIZE));
    }

    @Override
    public void copyTo(@NonNull byte[] array, int atIndex) {
        checkNotNull(array);
        checkArrayBounds(array, atIndex, size);

        Raw.copyInt8Buffer(pointer, array, atIndex, size);
    }

    @Override
    public @NonNull ByteList subList(int fromIndex, int toIndex) {
        checkIndexRange(fromIndex, toIndex, size);

        return new Int8BufferByteList(owner, pointer + (fromIndex * Raw.INT8_SIZE), toIndex - fromIndex);
    }
}
