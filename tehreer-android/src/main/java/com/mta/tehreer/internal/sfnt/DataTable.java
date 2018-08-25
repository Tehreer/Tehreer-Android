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

package com.mta.tehreer.internal.sfnt;

import android.support.annotation.NonNull;

public class DataTable implements SfntTable {
    private final @NonNull byte[] data;

    public DataTable(@NonNull byte[] data) {
        this.data = data;
    }

    @Override
    public @NonNull byte[] readBytes(int offset, int count) {
        byte[] array = new byte[count];
        System.arraycopy(data, offset, array, 0, count);

        return array;
    }

    @Override
    public short readInt16(int offset) {
        return (short) (((data[offset + 0] & 0xFF) << 8)
                       | (data[offset + 1] & 0xFF) << 0);
    }

    @Override
    public int readUInt16(int offset) {
        return (((data[offset + 0] & 0xFF) << 8)
               | (data[offset + 1] & 0xFF) << 0) & 0xFFFF;
    }

    @Override
    public int readInt32(int offset) {
        return ((data[offset + 0] & 0xFF) << 24)
             | ((data[offset + 1] & 0xFF) << 16)
             | ((data[offset + 2] & 0xFF) << 8)
             | ((data[offset + 3] & 0xFF) << 0);
    }

    @Override
    public long readUInt32(int offset) {
        return (((data[offset + 0] & 0xFF) << 24)
              | ((data[offset + 1] & 0xFF) << 16)
              | ((data[offset + 2] & 0xFF) << 8)
              | ((data[offset + 3] & 0xFF) << 0)) & 0xFFFFFFFFL;
    }

    @Override
    public long readInt64(int offset) {
        return (readUInt32(offset + 0) << 32)
             | (readUInt32(offset + 4) << 0);
    }
}
