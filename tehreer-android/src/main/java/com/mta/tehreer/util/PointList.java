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
import com.mta.tehreer.internal.util.SafePointList;

public abstract class PointList implements PrimitiveList {

    public static PointList forArray(float[] array) {
        return new SafePointList(array, 0, array.length / 2);
    }

    public abstract void copyTo(float[] array, int atIndex);
    public abstract int size();

    public abstract float getX(int index);
    public abstract float getY(int index);

    public abstract void setX(int index, float value);
    public abstract void setY(int index, float value);

    public abstract PointList subList(int fromIndex, int toIndex);

    public float[] toArray() {
        float[] array = new float[size() * 2];
        copyTo(array, 0);

        return array;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof PointList)) {
            return false;
        }

        PointList other = (PointList) obj;
        int size = other.size();

        if (size() != size) {
            return false;
        }

        for (int i = 0; i < size; i++) {
            if (getX(i) != other.getX(i) || getY(i) != other.getY(i)) {
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
            result = 31 * result + Float.floatToIntBits(getX(i));
            result = 31 * result + Float.floatToIntBits(getY(i));
        }

        return result;
    }

    @Override
    public String toString() {
        return Description.forPointList(this);
    }
}
