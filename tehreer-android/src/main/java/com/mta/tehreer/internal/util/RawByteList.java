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
import com.mta.tehreer.util.ByteList;

public class RawByteList implements ByteList {

    private final long pointer;
    private final int size;

    public RawByteList(long pointer, int size) {
        this.pointer = pointer;
        this.size = size;
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
        if (size != other.size()) {
            return false;
        }

        if (other.getClass() == RawByteList.class) {
            RawByteList raw = (RawByteList) other;
            if (pointer != raw.pointer && !Raw.isEqual(pointer, raw.pointer, size)) {
                return false;
            }
        } else {
            for (int i = 0; i < size; i++) {
                if (Raw.getByte(pointer, i) != other.get(i)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        return Raw.getHash(pointer, size);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public byte get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }

        return Raw.getByte(pointer, index);
    }

    @Override
    public void set(int index, byte value) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }

        Raw.putByte(pointer, index, value);
    }

    @Override
    public byte[] toArray() {
        return Raw.toByteArray(pointer, size);
    }

    @Override
    public String toString() {
        return Description.forByteList(this);
    }
}
