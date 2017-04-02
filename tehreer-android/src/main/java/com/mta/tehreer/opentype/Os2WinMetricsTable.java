/*
 * Copyright (C) 2017 Muhammad Tayyab Akram
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

package com.mta.tehreer.opentype;

import com.mta.tehreer.graphics.Typeface;

public class Os2WinMetricsTable {

    private static final int VERSION = 0;
    private static final int X_AVG_CHAR_WIDTH = 2;
    private static final int US_WEIGHT_CLASS = 4;
    private static final int US_WIDTH_CLASS = 6;
    private static final int FS_TYPE = 8;
    private static final int Y_SUBSCRIPT_X_SIZE = 10;
    private static final int Y_SUBSCRIPT_Y_SIZE = 12;
    private static final int Y_SUBSCRIPT_X_OFFSET = 14;
    private static final int Y_SUBSCRIPT_Y_OFFSET = 16;
    private static final int Y_SUPERSCRIPT_X_SIZE = 18;
    private static final int Y_SUPERSCRIPT_Y_SIZE = 20;
    private static final int Y_SUPERSCRIPT_X_OFFSET = 22;
    private static final int Y_SUPERSCRIPT_Y_OFFSET = 24;
    private static final int Y_STRIKEOUT_SIZE = 26;
    private static final int Y_STRIKEOUT_POSITION = 28;
    private static final int S_FAMILY_CLASS = 30;
    private static final int PANOSE = 32;
    private static final int PANOSE_LENGTH = 10;
    private static final int UL_UNICODE_RANGE_1 = 42;
    private static final int UL_UNICODE_RANGE_2 = 46;
    private static final int UL_UNICODE_RANGE_3 = 50;
    private static final int UL_UNICODE_RANGE_4 = 54;
    private static final int ACH_VEND_ID = 58;
    private static final int FS_SELECTION = 62;
    private static final int US_FIRST_CHAR_INDEX = 64;
    private static final int US_LAST_CHAR_INDEX = 66;
    private static final int S_TYPO_ASCENDER = 68;
    private static final int S_TYPO_DESCENDER = 70;
    private static final int S_TYPO_LINE_GAP = 72;
    private static final int US_WIN_ASCENT = 74;
    private static final int US_WIN_DESCENT = 76;
    private static final int UL_CODE_PAGE_RANGE_1 = 78;
    private static final int UL_CODE_PAGE_RANGE_2 = 82;
    private static final int SX_HEIGHT = 86;
    private static final int S_CAP_HEIGHT = 88;
    private static final int US_DEFAULT_CHAR = 90;
    private static final int US_BREAK_CHAR = 92;
    private static final int US_MAX_CONTEXT = 94;
    private static final int US_LOWER_OPTICAL_POINT_SIZE = 96;
    private static final int US_UPPER_OPTICAL_POINT_SIZE = 98;

    private final SfntTable table;

    public static Os2WinMetricsTable forTypeface(Typeface typeface) {
        long pointer = OpenType.getTablePointer(typeface, OpenType.TABLE_OS_2);
        if (pointer != 0) {
            return new Os2WinMetricsTable(new StructTable(typeface, pointer));
        }

        return null;
    }

    private Os2WinMetricsTable(SfntTable table) {
        this.table = table;
    }

    public int version() {
        return table.readUInt16(VERSION);
    }

    public short xAvgCharWidth() {
        return table.readInt16(X_AVG_CHAR_WIDTH);
    }

    public int usWeightClass() {
        return table.readUInt16(US_WEIGHT_CLASS);
    }

    public int usWidthClass() {
        return table.readUInt16(US_WIDTH_CLASS);
    }

    public int fsType() {
        return table.readUInt16(FS_TYPE);
    }

    public short ySubscriptXSize() {
        return table.readInt16(Y_SUBSCRIPT_X_SIZE);
    }

    public short ySubscriptYSize() {
        return table.readInt16(Y_SUBSCRIPT_Y_SIZE);
    }

    public short ySubscriptXOffset() {
        return table.readInt16(Y_SUBSCRIPT_X_OFFSET);
    }

    public short ySubscriptYOffset() {
        return table.readInt16(Y_SUBSCRIPT_Y_OFFSET);
    }

    public short ySuperscriptXSize() {
        return table.readInt16(Y_SUPERSCRIPT_X_SIZE);
    }

    public short ySuperscriptYSize() {
        return table.readInt16(Y_SUPERSCRIPT_Y_SIZE);
    }

    public short ySuperscriptXOffset() {
        return table.readInt16(Y_SUPERSCRIPT_X_OFFSET);
    }

    public short ySuperscriptYOffset() {
        return table.readInt16(Y_SUPERSCRIPT_Y_OFFSET);
    }

    public short yStrikeoutSize() {
        return table.readInt16(Y_STRIKEOUT_SIZE);
    }

    public short yStrikeoutPosition() {
        return table.readInt16(Y_STRIKEOUT_POSITION);
    }

    public short sFamilyClass() {
        return table.readInt16(S_FAMILY_CLASS);
    }

    public byte[] panose() {
        return table.readBytes(PANOSE, PANOSE_LENGTH);
    }

    public long ulUnicodeRange1() {
        return table.readUInt32(UL_UNICODE_RANGE_1);
    }

    public long ulUnicodeRange2() {
        return table.readUInt32(UL_UNICODE_RANGE_2);
    }

    public long ulUnicodeRange3() {
        return table.readUInt32(UL_UNICODE_RANGE_3);
    }

    public long ulUnicodeRange4() {
        return table.readUInt32(UL_UNICODE_RANGE_4);
    }

    public int achVendID() {
        return table.readInt32(ACH_VEND_ID);
    }

    public int fsSelection() {
        return table.readUInt16(FS_SELECTION);
    }

    public int usFirstCharIndex() {
        return table.readUInt16(US_FIRST_CHAR_INDEX);
    }

    public int usLastCharIndex() {
        return table.readUInt16(US_LAST_CHAR_INDEX);
    }

    public short sTypoAscender() {
        return table.readInt16(S_TYPO_ASCENDER);
    }

    public short sTypoDescender() {
        return table.readInt16(S_TYPO_DESCENDER);
    }

    public short sTypoLineGap() {
        return table.readInt16(S_TYPO_LINE_GAP);
    }

    public int usWinAscent() {
        return table.readUInt16(US_WIN_ASCENT);
    }

    public int usWinDescent() {
        return table.readUInt16(US_WIN_DESCENT);
    }

    public long ulCodePageRange1() {
        return table.readUInt32(UL_CODE_PAGE_RANGE_1);
    }

    public long ulCodePageRange2() {
        return table.readUInt32(UL_CODE_PAGE_RANGE_2);
    }

    public short sxHeight() {
        return table.readInt16(SX_HEIGHT);
    }

    public short sCapHeight() {
        return table.readInt16(S_CAP_HEIGHT);
    }

    public int usDefaultChar() {
        return table.readUInt16(US_DEFAULT_CHAR);
    }

    public int usBreakChar() {
        return table.readUInt16(US_BREAK_CHAR);
    }

    public int usMaxContext() {
        return table.readUInt16(US_MAX_CONTEXT);
    }

    public int usLowerOpticalPointSize() {
        return table.readUInt16(US_LOWER_OPTICAL_POINT_SIZE);
    }

    public int usUpperOpticalPointSize() {
        return table.readUInt16(US_UPPER_OPTICAL_POINT_SIZE);
    }
}
