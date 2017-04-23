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
 * Represents an OpenType `post' table.
 */
public class PostScriptTable {

    private static final int VERSION = 0;
    private static final int ITALIC_ANGLE = 4;
    private static final int UNDERLINE_POSITION = 8;
    private static final int UNDERLINE_THICKNESS = 10;
    private static final int IS_FIXED_PITCH = 12;
    private static final int MIN_MEM_TYPE_42 = 16;
    private static final int MAX_MEM_TYPE_42 = 20;
    private static final int MIN_MEM_TYPE_1 = 24;
    private static final int MAX_MEM_TYPE_1 = 28;

    private final Typeface typeface;
    private final SfntTable table;

    /**
     * Constructs a <code>PostScriptTable</code> object from the specified typeface.
     *
     * @param typeface The typeface from which the <code>PostScriptTable</code> object is
     *                 constructed.
     *
     * @throws NullPointerException if <code>typeface</code> is <code>null</code>.
     * @throws RuntimeException if <code>typeface</code> does not contain `post' table.
     */
    public PostScriptTable(Typeface typeface) {
        if (typeface == null) {
            throw new NullPointerException("Typeface is null");
        }
        long pointer = OpenType.getTablePointer(typeface, OpenType.TABLE_POST);
        if (pointer == 0) {
            throw new RuntimeException("The typeface does not contain `post' table");
        }

        this.typeface = typeface;
        this.table = new StructTable(typeface, pointer);
    }

    public long version() {
        return table.readUInt32(VERSION);
    }

    public int italicAngle() {
        return table.readInt32(ITALIC_ANGLE);
    }

    public short underlinePosition() {
        return table.readInt16(UNDERLINE_POSITION);
    }

    public short underlineThickness() {
        return table.readInt16(UNDERLINE_THICKNESS);
    }

    public long isFixedPitch() {
        return table.readUInt32(IS_FIXED_PITCH);
    }

    public long minMemType42() {
        return table.readUInt32(MIN_MEM_TYPE_42);
    }

    public long maxMemType42() {
        return table.readUInt32(MAX_MEM_TYPE_42);
    }

    public long minMemType1() {
        return table.readUInt32(MIN_MEM_TYPE_1);
    }

    public long maxMemType1() {
        return table.readUInt32(MAX_MEM_TYPE_1);
    }

    public int numberOfGlyphs() {
        return typeface.getGlyphCount();
    }

    public String glyphNameAt(int index) {
        if (index < 0 || index >= numberOfGlyphs()) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }

        return OpenType.getGlyphName(typeface, index);
    }
}
