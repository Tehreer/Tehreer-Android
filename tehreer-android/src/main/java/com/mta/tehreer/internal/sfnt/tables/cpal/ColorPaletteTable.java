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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.internal.sfnt.DataTable;
import com.mta.tehreer.internal.sfnt.SfntTable;
import com.mta.tehreer.sfnt.SfntTag;

import static com.mta.tehreer.internal.util.Preconditions.checkNotNull;

public final class ColorPaletteTable {
    private static final int VERSION = 0;
    private static final int NUM_PALETTE_ENTRIES = 2;
    private static final int NUM_PALETTES = 4;
    private static final int NUM_COLOR_RECORDS = 6;
    private static final int COLOR_RECORDS_ARRAY_OFFSET = 8;
    private static final int COLOR_RECORD_INDICES = 12;

    private static final int PALETTE_TYPES_ARRAY_OFFSET = 0;
    private static final int PALETTE_LABELS_ARRAY_OFFSET = 4;
    private static final int PALETTE_ENTRY_LABELS_ARRAY_OFFSET = 8;

    private final @NonNull SfntTable data;

    public static @Nullable ColorPaletteTable from(@NonNull Typeface typeface) {
        checkNotNull(typeface);

        final byte[] cpalData = typeface.getTableData(SfntTag.make("CPAL"));
        if (cpalData != null) {
            return new ColorPaletteTable(new DataTable(cpalData));
        }

        return null;
    }

    ColorPaletteTable(@NonNull SfntTable data) {
        this.data = data;
    }

    public int version() {
        return data.readUInt16(VERSION);
    }

    public int numPaletteEntries() {
        return data.readUInt16(NUM_PALETTE_ENTRIES);
    }

    public int numPalettes() {
        return data.readUInt16(NUM_PALETTES);
    }

    public int numColorRecords() {
        return data.readUInt16(NUM_COLOR_RECORDS);
    }

    public @NonNull ColorRecordsArray colorRecords() {
        return new ColorRecordsArray(data.subTable(data.readOffset32(COLOR_RECORDS_ARRAY_OFFSET)));
    }

    public int colorRecordIndexAt(int paletteIndex) {
        return data.readUInt16(COLOR_RECORD_INDICES + (paletteIndex * 2));
    }

    public @Nullable PaletteTypesArray paletteTypes() {
        if (version() < 1) {
            return null;
        }

        final int numColorRecords = numColorRecords();
        final int colorIndicesEndOffset = COLOR_RECORD_INDICES + (numColorRecords * 2);
        final int typesArrayOffset = data.readOffset32(colorIndicesEndOffset + PALETTE_TYPES_ARRAY_OFFSET);
        if (typesArrayOffset == 0) {
            return null;
        }

        return new PaletteTypesArray(data.subTable(typesArrayOffset));
    }

    public @Nullable PaletteLabelsArray paletteLabels() {
        if (version() < 1) {
            return null;
        }

        final int numColorRecords = numColorRecords();
        final int colorIndicesEndOffset = COLOR_RECORD_INDICES + (numColorRecords * 2);
        final int labelsArrayOffset = data.readOffset32(colorIndicesEndOffset + PALETTE_LABELS_ARRAY_OFFSET);
        if (labelsArrayOffset == 0) {
            return null;
        }

        return new PaletteLabelsArray(data.subTable(labelsArrayOffset));
    }

    public @Nullable PaletteLabelsArray paletteEntryLabels() {
        if (version() < 1) {
            return null;
        }

        final int numColorRecords = numColorRecords();
        final int colorIndicesEndOffset = COLOR_RECORD_INDICES + (numColorRecords * 2);
        final int labelsArrayOffset = data.readOffset32(colorIndicesEndOffset + PALETTE_ENTRY_LABELS_ARRAY_OFFSET);
        if (labelsArrayOffset == 0) {
            return null;
        }

        return new PaletteLabelsArray(data.subTable(labelsArrayOffset));
    }
}
