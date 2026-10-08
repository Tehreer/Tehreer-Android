/*
 * Copyright (C) 2026 Muhammad Tayyab Akram
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

package com.mta.tehreer.sfnt.tables

import com.mta.tehreer.graphics.Typeface
import com.mta.tehreer.internal.sfnt.SfntTable

/**
 * Represents an OpenType `hhea' table.
 */
class HheaTable private constructor(private val table: SfntTable) {
    /**
     * Constructs a `HheaTable` object from the specified typeface.
     *
     * @param typeface The typeface from which the `HheaTable` object is constructed.
     *
     * @throws RuntimeException if `typeface` does not contain `hhea' table.
     */
    constructor(typeface: Typeface) : this(
        SfntTables.readTable(typeface, "hhea", TABLE_LENGTH)
            ?: throw RuntimeException("The typeface does not contain `hhea' table")
    )

    fun version(): Int = table.readInt32(VERSION)
    fun ascender(): Short = table.readInt16(ASCENDER)
    fun descender(): Short = table.readInt16(DESCENDER)
    fun lineGap(): Short = table.readInt16(LINE_GAP)
    fun advanceWidthMax(): Int = table.readUInt16(ADVANCE_WIDTH_MAX)
    fun minLeftSideBearing(): Short = table.readInt16(MIN_LEFT_SIDE_BEARING)
    fun minRightSideBearing(): Short = table.readInt16(MIN_RIGHT_SIDE_BEARING)
    fun xMaxExtent(): Short = table.readInt16(X_MAX_EXTENT)
    fun caretSlopeRise(): Short = table.readInt16(CARET_SLOPE_RISE)
    fun caretSlopeRun(): Short = table.readInt16(CARET_SLOPE_RUN)
    fun caretOffset(): Short = table.readInt16(CARET_OFFSET)
    fun metricDataFormat(): Short = table.readInt16(METRIC_DATA_FORMAT)
    fun numberOfHMetrics(): Int = table.readUInt16(NUMBER_OF_H_METRICS)

    companion object {
        private const val VERSION = 0
        private const val ASCENDER = 4
        private const val DESCENDER = 6
        private const val LINE_GAP = 8
        private const val ADVANCE_WIDTH_MAX = 10
        private const val MIN_LEFT_SIDE_BEARING = 12
        private const val MIN_RIGHT_SIDE_BEARING = 14
        private const val X_MAX_EXTENT = 16
        private const val CARET_SLOPE_RISE = 18
        private const val CARET_SLOPE_RUN = 20
        private const val CARET_OFFSET = 22
        private const val METRIC_DATA_FORMAT = 32
        private const val NUMBER_OF_H_METRICS = 34
        private const val TABLE_LENGTH = 36

        /**
         * Constructs a `HheaTable` object from the specified typeface.
         *
         * @param typeface The typeface from which the `HheaTable` object is constructed.
         * @return A new `HheaTable` object, or `null` if `hhea' table does not exist in the specified
         *         typeface.
         */
        @JvmStatic
        fun from(typeface: Typeface): HheaTable? {
            return SfntTables.readTable(typeface, "hhea", TABLE_LENGTH)?.let { HheaTable(it) }
        }
    }
}
