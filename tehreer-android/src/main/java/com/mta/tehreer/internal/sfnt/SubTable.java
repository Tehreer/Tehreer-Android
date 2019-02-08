/*
 * Copyright (C) 2019 Muhammad Tayyab Akram
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

public class SubTable implements SfntTable {
    private final SfntTable table;
    private final int globalOffset;

    public SubTable(SfntTable table, int offset) {
        this.table = table;
        this.globalOffset = offset;
    }

    @Override
    public @NonNull byte[] readBytes(int offset, int count) {
        return table.readBytes(globalOffset + offset, count);
    }

    @Override
    public short readInt16(int offset) {
        return table.readInt16(globalOffset + offset);
    }

    @Override
    public int readInt32(int offset) {
        return table.readInt32(globalOffset + offset);
    }

    @Override
    public int readUInt16(int offset) {
        return table.readUInt16(globalOffset + offset);
    }

    @Override
    public long readUInt32(int offset) {
        return table.readUInt32(globalOffset + offset);
    }

    @Override
    public long readInt64(int offset) {
        return table.readInt64(globalOffset + offset);
    }

    @Override
    public SfntTable subTable(int offset) {
        return new SubTable(table, globalOffset + offset);
    }
}
