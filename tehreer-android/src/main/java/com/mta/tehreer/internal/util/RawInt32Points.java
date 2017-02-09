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

package com.mta.tehreer.internal.util;

import com.mta.tehreer.internal.Raw;
import com.mta.tehreer.util.PointList;

public class RawInt32Points extends PointList {

    private final long pointer;
    private final int size;

    public RawInt32Points(long pointer, int size) {
        this.pointer = pointer;
        this.size = size;
    }

    @Override
    public void copyTo(float[] array, int at, int from, int count, float scale) {
        if (array == null) {
            throw new NullPointerException();
        }
        int length = array.length;
        if (at < 0 || from < 0 || count < 0 || size - from < count || length - at < count) {
            throw new ArrayIndexOutOfBoundsException();
        }

        Raw.copyInt32Floats(pointer + (from * 4), array, at, count, scale);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public float getX(int index) {
        return Raw.getInt32Value(pointer, index * 2 + 0);
    }

    @Override
    public float getY(int index) {
        return Raw.getInt32Value(pointer, index * 2 + 1);
    }

    @Override
    public void setX(int index, float value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setY(int index, float value) {
        throw new UnsupportedOperationException();
    }
}
