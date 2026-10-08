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
 * Represents an OpenType `head' table.
 */
class HeadTable private constructor(private val table: SfntTable) {
    /**
     * Constructs a `HeadTable` object from the specified typeface.
     *
     * @param typeface The typeface from which the `HeadTable` object is constructed.
     *
     * @throws RuntimeException if `typeface` does not contain `head' table.
     */
    constructor(typeface: Typeface) : this(
        checkNotNull(SfntTables.readTable(typeface, "head", TABLE_LENGTH)) { "The typeface does not contain `head' table" }
    )

    fun version(): Int = table.readInt32(VERSION)
    fun fontRevision(): Int = table.readInt32(FONT_REVISION)
    fun checkSumAdjustment(): Long = table.readUInt32(CHECK_SUM_ADJUSTMENT)
    fun magicNumber(): Long = table.readUInt32(MAGIC_NUMBER)
    fun flags(): Int = table.readUInt16(FLAGS)
    fun unitsPerEm(): Int = table.readUInt16(UNITS_PER_EM)
    fun created(): Long = table.readInt64(CREATED)
    fun modified(): Long = table.readInt64(MODIFIED)
    fun xMin(): Short = table.readInt16(X_MIN)
    fun yMin(): Short = table.readInt16(Y_MIN)
    fun xMax(): Short = table.readInt16(X_MAX)
    fun yMax(): Short = table.readInt16(Y_MAX)
    fun macStyle(): Int = table.readUInt16(MAC_STYLE)
    fun lowestRecPPEM(): Int = table.readUInt16(LOWEST_REC_PPEM)
    fun fontDirectionHint(): Short = table.readInt16(FONT_DIRECTION_HINT)
    fun indexToLocFormat(): Short = table.readInt16(INDEX_TO_LOC_FORMAT)
    fun glyphDataFormat(): Short = table.readInt16(GLYPH_DATA_FORMAT)

    companion object {
        private const val VERSION = 0
        private const val FONT_REVISION = 4
        private const val CHECK_SUM_ADJUSTMENT = 8
        private const val MAGIC_NUMBER = 12
        private const val FLAGS = 16
        private const val UNITS_PER_EM = 18
        private const val CREATED = 20
        private const val MODIFIED = 28
        private const val X_MIN = 36
        private const val Y_MIN = 38
        private const val X_MAX = 40
        private const val Y_MAX = 42
        private const val MAC_STYLE = 44
        private const val LOWEST_REC_PPEM = 46
        private const val FONT_DIRECTION_HINT = 48
        private const val INDEX_TO_LOC_FORMAT = 50
        private const val GLYPH_DATA_FORMAT = 52
        private const val TABLE_LENGTH = 54

        /**
         * Constructs a `HeadTable` object from the specified typeface.
         *
         * @param typeface The typeface from which the `HeadTable` object is constructed.
         * @return A new `HeadTable` object, or `null` if `head' table does not exist in the specified
         *         typeface.
         */
        fun from(typeface: Typeface): HeadTable? {
            return SfntTables.readTable(typeface, "head", TABLE_LENGTH)?.let { HeadTable(it) }
        }
    }
}
