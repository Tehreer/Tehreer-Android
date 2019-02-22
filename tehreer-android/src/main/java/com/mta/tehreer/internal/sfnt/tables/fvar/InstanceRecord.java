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

package com.mta.tehreer.internal.sfnt.tables.fvar;

import androidx.annotation.NonNull;

import com.mta.tehreer.internal.sfnt.SfntTable;

public final class InstanceRecord {
    private static final int SUBFAMILY_NAME_ID = 0;
    private static final int FLAGS = 2;
    private static final int COORDINATES = 4;

    private final @NonNull SfntTable data;
    private final int axisCount;
    private final int postScriptNameIDOffset;

    InstanceRecord(@NonNull SfntTable data, int axisCount, int instanceSize) {
        int coordsEndOffset = COORDINATES + (axisCount * 4);

        this.data = data;
        this.axisCount = axisCount;
        this.postScriptNameIDOffset = ((instanceSize - coordsEndOffset) < 2) ? -1 : coordsEndOffset;
    }

    public int subfamilyNameID() {
        return data.readUInt16(SUBFAMILY_NAME_ID);
    }

    public int flags() {
        return data.readUInt16(FLAGS);
    }

    public @NonNull float[] coordinates() {
        float[] coordinates = new float[axisCount];

        for (int i = 0; i < axisCount; i++) {
            coordinates[i] = data.readFixed(COORDINATES + (i * 4));
        }

        return coordinates;
    }

    public int postScriptNameID() {
        if (postScriptNameIDOffset != -1) {
            return data.readInt32((COORDINATES + (axisCount * 4)));
        }

        return -1;
    }
}
