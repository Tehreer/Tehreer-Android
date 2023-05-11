/*
 * Copyright (C) 2023 Muhammad Tayyab Akram
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
package com.mta.tehreer.internal.sfnt.tables.cpal

import com.mta.tehreer.graphics.Typeface
import com.mta.tehreer.internal.sfnt.DataTable
import com.mta.tehreer.internal.sfnt.SfntTable
import com.mta.tehreer.sfnt.SfntTag

private const val VERSION = 0
private const val NUM_PALETTE_ENTRIES = 2
private const val NUM_PALETTES = 4
private const val NUM_COLOR_RECORDS = 6
private const val COLOR_RECORDS_ARRAY_OFFSET = 8
private const val COLOR_RECORD_INDICES = 12
private const val PALETTE_TYPES_ARRAY_OFFSET = 0
private const val PALETTE_LABELS_ARRAY_OFFSET = 4
private const val PALETTE_ENTRY_LABELS_ARRAY_OFFSET = 8

internal class ColorPaletteTable(
    private val data: SfntTable
) {
    fun version() = data.readUInt16(VERSION)

    fun numPaletteEntries() = data.readUInt16(NUM_PALETTE_ENTRIES)

    fun numPalettes() = data.readUInt16(NUM_PALETTES)

    fun numColorRecords() = data.readUInt16(NUM_COLOR_RECORDS)

    fun colorRecords() =
        ColorRecordsArray(data.subTable(data.readOffset32(COLOR_RECORDS_ARRAY_OFFSET)))

    fun colorRecordIndexAt(paletteIndex: Int) =
        data.readUInt16(COLOR_RECORD_INDICES + paletteIndex * 2)

    fun paletteTypes(): PaletteTypesArray? {
        if (version() < 1) {
            return null
        }

        val numColorRecords = numColorRecords()
        val colorIndicesEndOffset = COLOR_RECORD_INDICES + numColorRecords * 2
        val typesArrayOffset = data.readOffset32(colorIndicesEndOffset + PALETTE_TYPES_ARRAY_OFFSET)

        return if (typesArrayOffset == 0) {
            null
        } else PaletteTypesArray(data.subTable(typesArrayOffset))
    }

    fun paletteLabels(): PaletteLabelsArray? {
        if (version() < 1) {
            return null
        }

        val numColorRecords = numColorRecords()
        val colorIndicesEndOffset = COLOR_RECORD_INDICES + numColorRecords * 2
        val labelsArrayOffset = data.readOffset32(colorIndicesEndOffset + PALETTE_LABELS_ARRAY_OFFSET)

        return if (labelsArrayOffset == 0) {
            null
        } else PaletteLabelsArray(data.subTable(labelsArrayOffset))
    }

    fun paletteEntryLabels(): PaletteLabelsArray? {
        if (version() < 1) {
            return null
        }

        val numColorRecords = numColorRecords()
        val colorIndicesEndOffset = COLOR_RECORD_INDICES + numColorRecords * 2
        val labelsArrayOffset = data.readOffset32(colorIndicesEndOffset + PALETTE_ENTRY_LABELS_ARRAY_OFFSET)

        return if (labelsArrayOffset == 0) {
            null
        } else PaletteLabelsArray(data.subTable(labelsArrayOffset))
    }

    companion object {
        @JvmStatic
        fun from(typeface: Typeface) = with(typeface) {
            getTableData(SfntTag.make("CPAL"))?.let {
                ColorPaletteTable(DataTable(it))
            }
        }
    }
}
