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

import com.mta.tehreer.collections.PointList;

import static com.mta.tehreer.internal.util.Preconditions.checkElementIndex;
import static com.mta.tehreer.internal.util.Preconditions.checkIndexRange;

public class JFloatArrayPointList extends PointList {
    private static final int FIELD_COUNT = 2;
    private static final int X_OFFSET = 0;
    private static final int Y_OFFSET = 1;

    private final @NonNull float[] array;
    private final int offset;
    private final int size;

    public JFloatArrayPointList(@NonNull float[] array, int offset, int size) {
        this.array = array;
        this.offset = offset;
        this.size = size;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public float getX(int index) {
        checkElementIndex(index, size);

        return array[((index + offset) * FIELD_COUNT) + X_OFFSET];
    }

    @Override
    public float getY(int index) {
        checkElementIndex(index, size);

        return array[((index + offset) * FIELD_COUNT) + Y_OFFSET];
    }

    @Override
    public void copyTo(@NonNull float[] array, int atIndex) {
        System.arraycopy(this.array, offset, array, atIndex, size * FIELD_COUNT);
    }

    @Override
    public @NonNull PointList subList(int fromIndex, int toIndex) {
        checkIndexRange(fromIndex, toIndex, size);

        return new JFloatArrayPointList(array, offset + fromIndex, toIndex - fromIndex);
    }
}
