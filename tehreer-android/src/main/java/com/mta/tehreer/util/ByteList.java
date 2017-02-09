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
import com.mta.tehreer.internal.util.Primitives;

public abstract class ByteList implements PrimitiveList {

    public abstract void copyTo(byte[] array, int at, int from, int count);
    public abstract int size();

    public abstract byte get(int index);
    public abstract void set(int index, byte value);

    public byte[] toArray() {
        int length = size();
        byte[] array = new byte[length];
        copyTo(array, 0, 0, length);

        return array;
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
    public String toString() {
        return Description.forByteList(this);
    }
}
