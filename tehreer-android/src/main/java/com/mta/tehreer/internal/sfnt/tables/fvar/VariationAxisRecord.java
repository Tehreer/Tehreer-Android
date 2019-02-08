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

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.mta.tehreer.internal.sfnt.SfntTable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class VariationAxisRecord {
    private static final int AXIS_TAG = 0;
    private static final int MIN_VALUE = 4;
    private static final int DEFAULT_VALUE = 8;
    private static final int MAX_VALUE = 12;
    private static final int FLAGS = 16;
    private static final int AXIS_NAME_ID = 18;

    public static final int FLAG_HIDDEN_AXIS = 0x0001;

    @IntDef(
        flag = true,
        value = {
            FLAG_HIDDEN_AXIS
        })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Flags { }

    private final @NonNull SfntTable data;

    VariationAxisRecord(@NonNull SfntTable data) {
        this.data = data;
    }

    public int axisTag() {
        return data.readInt32(AXIS_TAG);
    }

    public float minValue() {
        return data.readFixed(MIN_VALUE);
    }

    public float defaultValue() {
        return data.readFixed(DEFAULT_VALUE);
    }

    public float maxValue() {
        return data.readFixed(MAX_VALUE);
    }

    public @Flags int flags() {
        return data.readUInt16(FLAGS);
    }

    public int axisNameId() {
        return data.readUInt16(AXIS_NAME_ID);
    }
}
