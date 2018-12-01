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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mta.tehreer.internal.Description;
import com.mta.tehreer.internal.collections.JIntArrayList;

import static com.mta.tehreer.internal.util.Preconditions.checkNotNull;

/**
 * Represents a primitive list of integers.
 */
public abstract class IntList implements Primitive {
    /**
     * Returns an integers list whose elements are the specified values.
     *
     * @param values The elements of the integer list.
     * @return A new integer list.
     */
    public static @NonNull IntList of(int... values) {
        checkNotNull(values);

        return new JIntArrayList(values, 0, values.length);
    }

    /**
     * Returns the number of integers in this list.
     *
     * @return The number of integers in this list.
     */
    public abstract int size();

    /**
     * Returns the integer at the specified index in this list.
     *
     * @param index Index of the integer to return.
     * @return The integer at the specified index in this list.
     *
     * @throws IndexOutOfBoundsException if the index is out of range (<code>index &lt; 0 || index
     *         &gt;= size()</code>).
     */
    public abstract int get(int index);

    /**
     * Copies all of the integers in this list to an array, starting at the specified index of the
     * target array.
     *
     * @param array The array into which the integers of this list are to be copied.
     * @param atIndex The index in the target array at which copying begins.
     *
     * @throws NullPointerException if <code>array</code> is null.
     * @throws IndexOutOfBoundsException for an illegal endpoint index value (<code>atIndex &lt; 0
     *         || (array.length - atIndex) &lt; size()</code>).
     */
    public abstract void copyTo(@NonNull int[] array, int atIndex);

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
    public abstract @NonNull IntList subList(int fromIndex, int toIndex);

    /**
     * Returns a new array containing all of the integers in this list in proper sequence (from
     * first to last element).
     *
     * @return A new array containing all of the integers in this list in proper sequence.
     */
    public @NonNull int[] toArray() {
        int[] array = new int[size()];
        copyTo(array, 0);

        return array;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof IntList)) {
            return false;
        }

        IntList other = (IntList) obj;
        int size = other.size();
        if (size() != size) {
            return false;
        }

        for (int i = 0; i < size; i++) {
            if (get(i) != other.get(i)) {
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
            result = 31 * result + get(i);
        }

        return result;
    }

    @Override
    public String toString() {
        return Description.forIntList(this);
    }
}
