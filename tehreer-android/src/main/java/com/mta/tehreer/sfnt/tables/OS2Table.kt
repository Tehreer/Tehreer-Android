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
 * Represents an OpenType `OS/2' table.
 */
class OS2Table private constructor(private val table: SfntTable) {
    /**
     * Constructs a `OS2Table` object from the specified typeface.
     *
     * @param typeface The typeface from which the `OS2Table` object is constructed.
     *
     * @throws RuntimeException if `typeface` does not contain `OS/2' table.
     */
    constructor(typeface: Typeface) : this(
        SfntTables.readTable(typeface, "OS/2", TABLE_LENGTH)
            ?: throw RuntimeException("The typeface does not contain `OS/2' table")
    )

    fun version(): Int = table.readUInt16(VERSION)
    fun xAvgCharWidth(): Short = table.readInt16(X_AVG_CHAR_WIDTH)
    fun usWeightClass(): Int = table.readUInt16(US_WEIGHT_CLASS)
    fun usWidthClass(): Int = table.readUInt16(US_WIDTH_CLASS)
    fun fsType(): Int = table.readUInt16(FS_TYPE)
    fun ySubscriptXSize(): Short = table.readInt16(Y_SUBSCRIPT_X_SIZE)
    fun ySubscriptYSize(): Short = table.readInt16(Y_SUBSCRIPT_Y_SIZE)
    fun ySubscriptXOffset(): Short = table.readInt16(Y_SUBSCRIPT_X_OFFSET)
    fun ySubscriptYOffset(): Short = table.readInt16(Y_SUBSCRIPT_Y_OFFSET)
    fun ySuperscriptXSize(): Short = table.readInt16(Y_SUPERSCRIPT_X_SIZE)
    fun ySuperscriptYSize(): Short = table.readInt16(Y_SUPERSCRIPT_Y_SIZE)
    fun ySuperscriptXOffset(): Short = table.readInt16(Y_SUPERSCRIPT_X_OFFSET)
    fun ySuperscriptYOffset(): Short = table.readInt16(Y_SUPERSCRIPT_Y_OFFSET)
    fun yStrikeoutSize(): Short = table.readInt16(Y_STRIKEOUT_SIZE)
    fun yStrikeoutPosition(): Short = table.readInt16(Y_STRIKEOUT_POSITION)
    fun sFamilyClass(): Short = table.readInt16(S_FAMILY_CLASS)
    fun panose(): ByteArray = table.readBytes(PANOSE, PANOSE_LENGTH)
    fun ulUnicodeRange1(): Long = table.readUInt32(UL_UNICODE_RANGE_1)
    fun ulUnicodeRange2(): Long = table.readUInt32(UL_UNICODE_RANGE_2)
    fun ulUnicodeRange3(): Long = table.readUInt32(UL_UNICODE_RANGE_3)
    fun ulUnicodeRange4(): Long = table.readUInt32(UL_UNICODE_RANGE_4)
    fun achVendID(): Int = table.readInt32(ACH_VEND_ID)
    fun fsSelection(): Int = table.readUInt16(FS_SELECTION)
    fun usFirstCharIndex(): Int = table.readUInt16(US_FIRST_CHAR_INDEX)
    fun usLastCharIndex(): Int = table.readUInt16(US_LAST_CHAR_INDEX)
    fun sTypoAscender(): Short = table.readInt16(S_TYPO_ASCENDER)
    fun sTypoDescender(): Short = table.readInt16(S_TYPO_DESCENDER)
    fun sTypoLineGap(): Short = table.readInt16(S_TYPO_LINE_GAP)
    fun usWinAscent(): Int = table.readUInt16(US_WIN_ASCENT)
    fun usWinDescent(): Int = table.readUInt16(US_WIN_DESCENT)
    fun ulCodePageRange1(): Long = table.readUInt32(UL_CODE_PAGE_RANGE_1)
    fun ulCodePageRange2(): Long = table.readUInt32(UL_CODE_PAGE_RANGE_2)
    fun sxHeight(): Short = table.readInt16(SX_HEIGHT)
    fun sCapHeight(): Short = table.readInt16(S_CAP_HEIGHT)
    fun usDefaultChar(): Int = table.readUInt16(US_DEFAULT_CHAR)
    fun usBreakChar(): Int = table.readUInt16(US_BREAK_CHAR)
    fun usMaxContext(): Int = table.readUInt16(US_MAX_CONTEXT)
    fun usLowerOpticalPointSize(): Int = table.readUInt16(US_LOWER_OPTICAL_POINT_SIZE)
    fun usUpperOpticalPointSize(): Int = table.readUInt16(US_UPPER_OPTICAL_POINT_SIZE)

    companion object {
        private const val VERSION = 0
        private const val X_AVG_CHAR_WIDTH = 2
        private const val US_WEIGHT_CLASS = 4
        private const val US_WIDTH_CLASS = 6
        private const val FS_TYPE = 8
        private const val Y_SUBSCRIPT_X_SIZE = 10
        private const val Y_SUBSCRIPT_Y_SIZE = 12
        private const val Y_SUBSCRIPT_X_OFFSET = 14
        private const val Y_SUBSCRIPT_Y_OFFSET = 16
        private const val Y_SUPERSCRIPT_X_SIZE = 18
        private const val Y_SUPERSCRIPT_Y_SIZE = 20
        private const val Y_SUPERSCRIPT_X_OFFSET = 22
        private const val Y_SUPERSCRIPT_Y_OFFSET = 24
        private const val Y_STRIKEOUT_SIZE = 26
        private const val Y_STRIKEOUT_POSITION = 28
        private const val S_FAMILY_CLASS = 30
        private const val PANOSE = 32
        private const val PANOSE_LENGTH = 10
        private const val UL_UNICODE_RANGE_1 = 42
        private const val UL_UNICODE_RANGE_2 = 46
        private const val UL_UNICODE_RANGE_3 = 50
        private const val UL_UNICODE_RANGE_4 = 54
        private const val ACH_VEND_ID = 58
        private const val FS_SELECTION = 62
        private const val US_FIRST_CHAR_INDEX = 64
        private const val US_LAST_CHAR_INDEX = 66
        private const val S_TYPO_ASCENDER = 68
        private const val S_TYPO_DESCENDER = 70
        private const val S_TYPO_LINE_GAP = 72
        private const val US_WIN_ASCENT = 74
        private const val US_WIN_DESCENT = 76
        private const val UL_CODE_PAGE_RANGE_1 = 78
        private const val UL_CODE_PAGE_RANGE_2 = 82
        private const val SX_HEIGHT = 86
        private const val S_CAP_HEIGHT = 88
        private const val US_DEFAULT_CHAR = 90
        private const val US_BREAK_CHAR = 92
        private const val US_MAX_CONTEXT = 94
        private const val US_LOWER_OPTICAL_POINT_SIZE = 96
        private const val US_UPPER_OPTICAL_POINT_SIZE = 98
        private const val TABLE_LENGTH = 100

        /**
         * Constructs a `OS2Table` object from the specified typeface.
         *
         * @param typeface The typeface from which the `OS2Table` object is constructed.
         * @return A new `OS2Table` object, or `null` if `OS/2' table does not exist in the specified
         *         typeface.
         */
        @JvmStatic
        fun from(typeface: Typeface): OS2Table? {
            return SfntTables.readTable(typeface, "OS/2", TABLE_LENGTH)?.let { OS2Table(it) }
        }
    }
}
