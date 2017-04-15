/*
 * Copyright (C) 2017 Muhammad Tayyab Akram
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

package com.mta.tehreer.internal.collection;

import com.mta.tehreer.internal.Raw;
import com.mta.tehreer.internal.util.Exceptions;
import com.mta.tehreer.util.PointList;

public class RawInt32Points extends PointList {

    private final long pointer;
    private final int size;
    private final float scale;

    public RawInt32Points(long pointer, int size, float scale) {
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

        return Raw.getInt32FromArray(pointer, index * 2 + 0) * scale;
    }

    @Override
    public float getY(int index) {
        if (index < 0 || index >= size) {
            throw Exceptions.indexOutOfBounds(index, size);
        }

        return Raw.getInt32FromArray(pointer, index * 2 + 1) * scale;
    }

    @Override
    public void copyTo(float[] array, int atIndex) {
        if (array == null) {
            throw new NullPointerException();
        }
        int length = array.length;
        if (atIndex < 0 || (length - atIndex) < (size * 2)) {
            throw new ArrayIndexOutOfBoundsException();
        }

        Raw.copyInt32FloatArray(pointer, array, atIndex, size * 2, scale);
    }

    @Override
    public PointList subList(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex > size || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException();
        }

        return new RawInt32Points(pointer + (fromIndex * 8), toIndex - fromIndex, scale);
    }
}
