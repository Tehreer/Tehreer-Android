/*
 * Copyright (C) 2020 Muhammad Tayyab Akram
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

package com.mta.tehreer.internal.sfnt.tables.cpal;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.mta.tehreer.internal.sfnt.SfntTable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class PaletteTypesArray {
    public static final int USABLE_WITH_LIGHT_BACKGROUND = 0x0001;
    public static final int USABLE_WITH_DARK_BACKGROUND = 0x0002;

    /** @hide */
    @IntDef(
        flag = true,
        value = {
            USABLE_WITH_LIGHT_BACKGROUND,
            USABLE_WITH_DARK_BACKGROUND
        })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Flags { }

    private final @NonNull SfntTable data;

    PaletteTypesArray(@NonNull SfntTable data) {
        this.data = data;
    }

    public @Flags int get(int index) {
        return data.readInt32(index * 4);
    }
}
