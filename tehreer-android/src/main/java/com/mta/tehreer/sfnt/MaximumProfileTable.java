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

package com.mta.tehreer.sfnt;

import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.internal.sfnt.SfntTable;
import com.mta.tehreer.internal.sfnt.StructTable;

/**
 * Represents an OpenType `maxp' table.
 */
public class MaximumProfileTable {

    private static final int VERSION = 0;
    private static final int NUM_GLYPHS = 4;
    private static final int MAX_POINTS = 6;
    private static final int MAX_CONTOURS = 8;
    private static final int MAX_COMPOSITE_POINTS = 10;
    private static final int MAX_COMPOSITE_CONTOURS = 12;
    private static final int MAX_ZONES = 14;
    private static final int MAX_TWILIGHT_POINTS = 16;
    private static final int MAX_STORAGE = 18;
    private static final int MAX_FUNCTION_DEFS = 20;
    private static final int MAX_INSTRUCTION_DEFS = 22;
    private static final int MAX_STACK_ELEMENTS = 24;
    private static final int MAX_SIZE_OF_INSTRUCTIONS = 26;
    private static final int MAX_COMPONENT_ELEMENTS = 28;
    private static final int MAX_COMPONENT_DEPTH = 30;

    private final SfntTable table;

    /**
     * Constructs a <code>MaximumProfileTable</code> object from the specified typeface.
     *
     * @param typeface The typeface from which the <code>MaximumProfileTable</code> object is
     *                 constructed.
     *
     * @throws NullPointerException if <code>typeface</code> is <code>null</code>.
     * @throws RuntimeException if <code>typeface</code> does not contain `maxp' table.
     */
    public MaximumProfileTable(Typeface typeface) {
        if (typeface == null) {
            throw new NullPointerException("Typeface is null");
        }
        long pointer = SfntTables.getTablePointer(typeface, SfntTables.TABLE_MAXP);
        if (pointer == 0) {
            throw new RuntimeException("The typeface does not contain `maxp' table");
        }

        this.table = new StructTable(typeface, pointer);
    }

    public int version() {
        return table.readInt32(VERSION);
    }

    public int numGlyphs() {
        return table.readUInt16(NUM_GLYPHS);
    }

    public int maxPoints() {
        return table.readUInt16(MAX_POINTS);
    }

    public int maxContours() {
        return table.readUInt16(MAX_CONTOURS);
    }

    public int maxCompositePoints() {
        return table.readUInt16(MAX_COMPOSITE_POINTS);
    }

    public int maxCompositeContours() {
        return table.readUInt16(MAX_COMPOSITE_CONTOURS);
    }

    public int maxZones() {
        return table.readUInt16(MAX_ZONES);
    }

    public int maxTwilightPoints() {
        return table.readUInt16(MAX_TWILIGHT_POINTS);
    }

    public int maxStorage() {
        return table.readUInt16(MAX_STORAGE);
    }

    public int maxFunctionDefs() {
        return table.readUInt16(MAX_FUNCTION_DEFS);
    }

    public int maxInstructionDefs() {
        return table.readUInt16(MAX_INSTRUCTION_DEFS);
    }

    public int maxStackElements() {
        return table.readUInt16(MAX_STACK_ELEMENTS);
    }

    public int maxSizeOfInstructions() {
        return table.readUInt16(MAX_SIZE_OF_INSTRUCTIONS);
    }

    public int maxComponentElements() {
        return table.readUInt16(MAX_COMPONENT_ELEMENTS);
    }

    public int maxComponentDepth() {
        return table.readUInt16(MAX_COMPONENT_DEPTH);
    }
}
