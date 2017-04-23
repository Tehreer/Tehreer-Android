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

/**
 * Represents an OpenType `head' table.
 */
public class FontHeaderTable {

    private static final int VERSION = 0;
    private static final int FONT_REVISION = 4;
    private static final int CHECK_SUM_ADJUSTMENT = 8;
    private static final int MAGIC_NUMBER = 12;
    private static final int FLAGS = 16;
    private static final int UNITS_PER_EM = 18;
    private static final int CREATED = 20;
    private static final int MODIFIED = 28;
    private static final int X_MIN = 36;
    private static final int Y_MIN = 38;
    private static final int X_MAX = 40;
    private static final int Y_MAX = 42;
    private static final int MAC_STYLE = 44;
    private static final int LOWEST_REC_PPEM = 46;
    private static final int FONT_DIRECTION_HINT = 48;
    private static final int INDEX_TO_LOC_FORMAT = 50;
    private static final int GLYPH_DATA_FORMAT = 52;

    private final SfntTable table;

    /**
     * Constructs a <code>FontHeaderTable</code> object from the specified typeface.
     *
     * @param typeface The typeface from which the <code>FontHeaderTable</code> object is
     *                 constructed.
     *
     * @throws NullPointerException if <code>typeface</code> is <code>null</code>.
     * @throws RuntimeException if <code>typeface</code> does not contain `head' table.
     */
    public FontHeaderTable(Typeface typeface) {
        if (typeface == null) {
            throw new NullPointerException("Typeface is null");
        }
        long pointer = OpenType.getTablePointer(typeface, OpenType.TABLE_HEAD);
        if (pointer == 0) {
            throw new RuntimeException("The typeface does not contain `head' table");
        }

        this.table = new StructTable(typeface, pointer);
    }

    public long version() {
        return table.readUInt32(VERSION);
    }

    public long fontRevision() {
        return table.readUInt32(FONT_REVISION);
    }

    public long checkSumAdjustment() {
        return table.readUInt32(CHECK_SUM_ADJUSTMENT);
    }

    public long magicNumber() {
        return table.readUInt32(MAGIC_NUMBER);
    }

    public int flags() {
        return table.readUInt16(FLAGS);
    }

    public int unitsPerEm() {
        return table.readUInt16(UNITS_PER_EM);
    }

    public long created() {
        return table.readInt64(CREATED);
    }

    public long modified() {
        return table.readInt64(MODIFIED);
    }

    public short xMin() {
        return table.readInt16(X_MIN);
    }

    public short yMin() {
        return table.readInt16(Y_MIN);
    }

    public short xMax() {
        return table.readInt16(X_MAX);
    }

    public short yMax() {
        return table.readInt16(Y_MAX);
    }

    public int macStyle() {
        return table.readUInt16(MAC_STYLE);
    }

    public int lowestRecPPEM() {
        return table.readUInt16(LOWEST_REC_PPEM);
    }

    public short fontDirectionHint() {
        return table.readInt16(FONT_DIRECTION_HINT);
    }

    public short indexToLocFormat() {
        return table.readInt16(INDEX_TO_LOC_FORMAT);
    }

    public short glyphDataFormat() {
        return table.readInt16(GLYPH_DATA_FORMAT);
    }
}
