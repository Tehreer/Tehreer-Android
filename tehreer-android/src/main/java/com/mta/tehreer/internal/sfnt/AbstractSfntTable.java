/*
 * Copyright (C) 2017-2020 Muhammad Tayyab Akram
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

public abstract class AbstractSfntTable implements SfntTable {
    protected AbstractSfntTable() { }

    public float readFixed(int offset) {
        return readInt32(offset) / 65536.0f;
    }

    public int readOffset32(int offset) {
        return ((int) readUInt32(offset)) & ~0x80000000;
    }

    public SfntTable subTable(int offset) {
        return new SubTable(this, offset);
    }
}
