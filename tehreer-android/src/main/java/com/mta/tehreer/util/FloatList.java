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

package com.mta.tehreer.util;

import com.mta.tehreer.internal.util.Description;

public abstract class FloatList implements PrimitiveList {

    public abstract void copyTo(float[] array, int atIndex);
    public abstract int size();

    public abstract float get(int index);
    public abstract void set(int index, float value);

    public abstract FloatList subList(int fromIndex, int toIndex);

    public float[] toArray() {
        int length = size();
        float[] array = new float[length];
        copyTo(array, 0);

        return array;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof FloatList)) {
            return false;
        }

        FloatList other = (FloatList) obj;
        int size = other.size();

        if (size() != size) {
            return false;
        }

        for (int i = 0; i < size; i++) {
            if (Float.floatToIntBits(get(i)) != Float.floatToIntBits(other.get(i))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int size = size();
        int result = 1;

        for (int i = 0; i < size; i++) {
            result = 31 * result + Float.floatToIntBits(get(i));
        }

        return result;
    }

    @Override
    public String toString() {
        return Description.forFloatList(this);
    }
}
