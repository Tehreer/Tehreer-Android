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

import com.mta.tehreer.collections.FloatList;
import com.mta.tehreer.internal.Exceptions;
import com.mta.tehreer.internal.Raw;

public class RawInt32AsFloatList extends FloatList {

    private final long pointer;
    private final int size;
    private final float scale;

    public RawInt32AsFloatList(long pointer, int size, float scale) {
        this.pointer = pointer;
        this.size = size;
        this.scale = scale;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public float get(int index) {
        if (index < 0 || index >= size) {
            throw Exceptions.indexOutOfBounds(index, size);
        }

        return Raw.getInt32Value(pointer + (index * Raw.INT32_SIZE)) * scale;
    }

    @Override
    public void copyTo(float[] array, int atIndex) {
        if (array == null) {
            throw new NullPointerException();
        }
        int length = array.length;
        if (atIndex < 0 || (length - atIndex) < size) {
            throw new ArrayIndexOutOfBoundsException();
        }

        Raw.copyInt32Buffer(pointer, array, atIndex, size, scale);
    }

    @Override
    public FloatList subList(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex > size || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException();
        }

        return new RawInt32AsFloatList(pointer + (fromIndex * Raw.INT32_SIZE), toIndex - fromIndex, scale);
    }
}
