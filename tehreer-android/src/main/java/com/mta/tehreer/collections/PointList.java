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

package com.mta.tehreer.collections;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;

import com.mta.tehreer.internal.Description;
import com.mta.tehreer.internal.collections.JFloatArrayPointList;

import static com.mta.tehreer.internal.util.Preconditions.checkNotNull;

/**
 * Represents a primitive list of points.
 */
public abstract class PointList implements Primitive {
    /**
     * Returns a point list whose elements are the specified array. Even numbered array entries will
     * become the x- coordinates while odd numbered array entries will become the y- coordinates.
     *
     * @param array The elements of the point list.
     * @return A new point list.
     */
    public static @NonNull PointList of(@NonNull @Size(multiple = 2) float[] array) {
        checkNotNull(array);

        return new JFloatArrayPointList(array, 0, array.length / 2);
    }

    /**
     * Returns the number of points in this list.
     *
     * @return The number of points in this list.
     */
    public abstract int size();

    /**
     * Returns the x- coordinate of the point at the specified index in this list.
     *
     * @param index Index of the point whose x- coordinate is returned.
     * @return The x- coordinate of the point at the specified index in this list.
     *
     * @throws IndexOutOfBoundsException if the index is out of range (<code>index &lt; 0 || index
     *         &gt;= size()</code>).
     */
    public abstract float getX(int index);

    /**
     * Returns the y- coordinate of the point at the specified index in this list.
     *
     * @param index Index of the point whose y- coordinate is returned.
     * @return The y- coordinate of the point at the specified index in this list.
     *
     * @throws IndexOutOfBoundsException if the index is out of range (<code>index &lt; 0 || index
     *         &gt;= size()</code>).
     */
    public abstract float getY(int index);

    /**
     * Copies all of the points in this list to an array, starting at the specified index of the
     * target array. Each x- coordinate will be followed by y- coordinate of the point in the target
     * array.
     *
     * @param array The array into which the points of this list are to be copied.
     * @param atIndex The index in the target array at which copying begins.
     *
     * @throws NullPointerException if <code>array</code> is null.
     * @throws IndexOutOfBoundsException for an illegal endpoint index value (<code>atIndex &lt; 0
     *         || (array.length - atIndex) &lt; size() * 2</code>).
     */
    public abstract void copyTo(@NonNull float[] array, int atIndex);

    /**
     * Returns a view of the portion of this list between the specified <code>fromIndex</code>,
     * inclusive, and <code>toIndex</code>, exclusive.
     *
     * @param fromIndex Low endpoint (inclusive) of the sub list.
     * @param toIndex High endpoint (exclusive) of the sub list.
     * @return A view of the specified range within this list.
     *
     * @throws IndexOutOfBoundsException for an illegal endpoint index value (<code>fromIndex &lt; 0
     *         || toIndex &gt; size() || fromIndex &gt; toIndex</code>).
     */
    public abstract @NonNull PointList subList(int fromIndex, int toIndex);

    /**
     * Returns a new array containing all of the elements in this list in proper sequence (from
     * first to last element). Even numbered array entries will be the x- coordinates while odd
     * numbered array entries will be the y- coordinates.
     *
     * @return A new array containing all of the elements in this list in proper sequence.
     */
    public @NonNull @Size(multiple = 2) float[] toArray() {
        float[] array = new float[size() * 2];
        copyTo(array, 0);

        return array;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
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
