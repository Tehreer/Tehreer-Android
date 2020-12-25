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

import android.graphics.Color;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.mta.tehreer.internal.sfnt.SfntTable;

public final class ColorRecordsArray {
    private final @NonNull SfntTable data;

    ColorRecordsArray(@NonNull SfntTable data) {
        this.data = data;
    }

    public @ColorInt int get(int index) {
        final int offset = index * 4;
        final short blue = data.readUInt8(offset);
        final short green = data.readUInt8(offset + 1);
        final short red = data.readUInt8(offset + 2);
        final short alpha = data.readUInt8(offset + 3);

        return Color.argb(alpha, red, green, blue);
    }
}
