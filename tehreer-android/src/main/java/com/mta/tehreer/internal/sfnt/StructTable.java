/*
 * Copyright (C) 2017-2019 Muhammad Tayyab Akram
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mta.tehreer.internal.Raw;

public class StructTable implements SfntTable {
    /**
     * Represents the source from which the struct pointer was obtained. Keep the source in memory
     * so that it does not accidentally get disposed by the GC when in use.
     */
    private final @Nullable Object source;
    /**
     * Represents the pointer to a native struct modeling an open type table.
     */
    protected final long pointer;

    public StructTable(@Nullable Object source, long pointer) {
        this.source = source;
        this.pointer = pointer;
    }

    protected long pointerOf(int offset) {
        return pointer + offset;
    }

    @Override
    public @NonNull byte[] readBytes(int offset, int count) {
        byte[] array = new byte[count];
        Raw.copyInt8Buffer(pointerOf(offset), array, 0, count);

        return array;
    }

    @Override
    public short readInt16(int offset) {
        return Raw.getInt16Value(pointerOf(offset));
    }

    @Override
    public int readUInt16(int offset) {
        return Raw.getInt16Value(pointerOf(offset)) & 0xFFFF;
    }

    @Override
    public int readInt32(int offset) {
        return Raw.getInt32Value(pointerOf(offset));
    }

    @Override
    public long readUInt32(int offset) {
        return Raw.getInt32Value(pointerOf(offset)) & 0xFFFFFFFFL;
    }

    @Override
    public long readInt64(int offset) {
        return ((Raw.getInt32Value(pointerOf(offset)) & 0xFFFFFFFFL) << 32)
             | ((Raw.getInt32Value(pointerOf(offset)) & 0xFFFFFFFFL));
    }

    @Override
    public SfntTable subTable(int offset) {
        return null;
    }
}
