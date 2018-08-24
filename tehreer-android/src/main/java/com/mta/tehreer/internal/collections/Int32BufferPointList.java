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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mta.tehreer.collections.PointList;
import com.mta.tehreer.internal.Exceptions;
import com.mta.tehreer.internal.Raw;

public class Int32BufferPointList extends PointList {
    private static final int STRUCT_SIZE = 8;
    private static final int X_OFFSET = 0;
    private static final int Y_OFFSET = 4;

    private final @Nullable Object owner;
    private final long pointer;
    private final int size;
    private final float scale;

    public Int32BufferPointList(@Nullable Object owner, long pointer, int size, float scale) {
        this.owner = owner;
        this.pointer = pointer;
        this.size = size;
        this.scale = scale;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public float getX(int index) {
        if (index < 0 || index >= size) {
            throw Exceptions.indexOutOfBounds(index, size);
        }

        return Raw.getInt32Value(pointer + (index * STRUCT_SIZE) + X_OFFSET) * scale;
    }

    @Override
    public float getY(int index) {
        if (index < 0 || index >= size) {
            throw Exceptions.indexOutOfBounds(index, size);
        }

        return Raw.getInt32Value(pointer + (index * STRUCT_SIZE) + Y_OFFSET) * scale;
    }

    @Override
    public void copyTo(@NonNull float[] array, int atIndex) {
        if (array == null) {
            throw new NullPointerException();
        }
        int length = array.length;
        if (atIndex < 0 || (length - atIndex) < (size * 2)) {
            throw new ArrayIndexOutOfBoundsException();
        }

        Raw.copyInt32Buffer(pointer, array, atIndex, size * 2, scale);
    }

    @Override
    public @NonNull PointList subList(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex > size || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException();
        }

        return new Int32BufferPointList(owner, pointer + (fromIndex * STRUCT_SIZE), toIndex - fromIndex, scale);
    }
}
