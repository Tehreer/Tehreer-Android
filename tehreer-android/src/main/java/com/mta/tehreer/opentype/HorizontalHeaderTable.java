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
 * Represents an OpenType `hhea' table.
 */
public class HorizontalHeaderTable {

    private static final int VERSION = 0;
    private static final int ASCENDER = 4;
    private static final int DESCENDER = 6;
    private static final int LINE_GAP = 8;
    private static final int ADVANCE_WIDTH_MAX = 10;
    private static final int MIN_LEFT_SIDE_BEARING = 12;
    private static final int MIN_RIGHT_SIDE_BEARING = 14;
    private static final int X_MAX_EXTENT = 16;
    private static final int CARET_SLOPE_RISE = 18;
    private static final int CARET_SLOPE_RUN = 20;
    private static final int CARET_OFFSET = 22;
    private static final int METRIC_DATA_FORMAT = 32;
    private static final int NUMBER_OF_H_METRICS = 34;

    private final SfntTable table;

    /**
     * Constructs a <code>HorizontalHeaderTable</code> object from the specified typeface.
     *
     * @param typeface The typeface from which the <code>HorizontalHeaderTable</code> object is
     *        constructed.
     *
     * @throws NullPointerException if <code>typeface</code> is <code>null</code>.
     * @throws RuntimeException if <code>typeface</code> does not contain `hhea' table.
     */
    public HorizontalHeaderTable(Typeface typeface) {
        if (typeface == null) {
            throw new NullPointerException("Typeface is null");
        }
        long pointer = OpenType.getTablePointer(typeface, OpenType.TABLE_HHEA);
        if (pointer == 0) {
            throw new RuntimeException("The typeface does not contain `hhea' table");
        }

        this.table = new StructTable(typeface, pointer);
    }

    public long version() {
        return table.readUInt32(VERSION);
    }

    public short ascender() {
        return table.readInt16(ASCENDER);
    }

    public short descender() {
        return table.readInt16(DESCENDER);
    }

    public short lineGap() {
        return table.readInt16(LINE_GAP);
    }

    public int advanceWidthMax() {
        return table.readUInt16(ADVANCE_WIDTH_MAX);
    }

    public short minLeftSideBearing() {
        return table.readInt16(MIN_LEFT_SIDE_BEARING);
    }

    public short minRightSideBearing() {
        return table.readInt16(MIN_RIGHT_SIDE_BEARING);
    }

    public short xMaxExtent() {
        return table.readInt16(X_MAX_EXTENT);
    }

    public short caretSlopeRise() {
        return table.readInt16(CARET_SLOPE_RISE);
    }

    public short caretSlopeRun() {
        return table.readInt16(CARET_SLOPE_RUN);
    }

    public short caretOffset() {
        return table.readInt16(CARET_OFFSET);
    }

    public short metricDataFormat() {
        return table.readInt16(METRIC_DATA_FORMAT);
    }

    public int numberOfHMetrics() {
        return table.readUInt16(NUMBER_OF_H_METRICS);
    }
}
