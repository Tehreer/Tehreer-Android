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

public abstract class ByteList implements PrimitiveList {

    public abstract int size();
    public abstract byte get(int index);

    public abstract void copyTo(byte[] array, int atIndex);
    public abstract ByteList subList(int fromIndex, int toIndex);

    public byte[] toArray() {
        byte[] array = new byte[size()];
        copyTo(array, 0);

        return array;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof ByteList)) {
            return false;
        }

        ByteList other = (ByteList) obj;
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
        return Description.forByteList(this);
    }
}
