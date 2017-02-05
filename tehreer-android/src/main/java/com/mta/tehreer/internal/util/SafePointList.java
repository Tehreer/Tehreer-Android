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

import com.mta.tehreer.util.PointList;

public class SafePointList implements PointList {

    private final float[] array;
    private final int offset;
    private final int size;

    public SafePointList(float[] array, int offset, int size) {
        this.array = array;
        this.offset = offset;
        this.size = size;
    }

    @Override
    public boolean equals(Object obj) {
        return Primitives.equals(this, obj);
    }

    @Override
    public int hashCode() {
        return Primitives.hashCode(this);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public float getX(int index) {
        if (index < 0 || index >= size) {
            throw new ArrayIndexOutOfBoundsException(index);
        }

        return array[index * 2 + 0];
    }

    @Override
    public float getY(int index) {
        if (index < 0 || index >= size) {
            throw new ArrayIndexOutOfBoundsException(index);
        }

        return array[index * 2 + 1];
    }

    @Override
    public void setX(int index, float value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setY(int index, float value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public float[] toArray(float scale) {
        return Primitives.copyScaled(array, scale);
    }

    @Override
    public String toString() {
        return Description.forPointList(this);
    }
}
