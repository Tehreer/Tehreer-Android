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

import com.mta.tehreer.util.ByteList;

import java.util.Arrays;

public class SafeByteList implements ByteList {

    private final byte[] array;

    public SafeByteList(byte[] array) {
        this.array = array;
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
        if (array.length != other.size()) {
            return false;
        }

        if (other.getClass() == SafeByteList.class) {
            SafeByteList safe = (SafeByteList) other;
            if (array != safe.array && !Arrays.equals(array, safe.array)) {
                return false;
            }
        } else {
            for (int i = 0; i < array.length; i++) {
                if (array[i] != other.get(i)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(array);
    }

    @Override
    public int size() {
        return array.length;
    }

    @Override
    public byte get(int index) {
        return array[index];
    }

    @Override
    public void set(int index, byte value) {
        array[index] = value;
    }

    @Override
    public byte[] toArray() {
        return Arrays.copyOf(array, array.length);
    }
}
