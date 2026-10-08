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
 * Represents an OpenType `maxp' table.
 */
class MaxpTable private constructor(private val table: SfntTable) {
    /**
     * Constructs a `MaxpTable` object from the specified typeface.
     *
     * @param typeface The typeface from which the `MaxpTable` object is constructed.
     *
     * @throws RuntimeException if `typeface` does not contain `maxp' table.
     */
    constructor(typeface: Typeface) : this(
        SfntTables.readTable(typeface, "maxp", TABLE_LENGTH)
            ?: throw RuntimeException("The typeface does not contain `maxp' table")
    )

    fun version(): Int = table.readInt32(VERSION)
    fun numGlyphs(): Int = table.readUInt16(NUM_GLYPHS)
    fun maxPoints(): Int = table.readUInt16(MAX_POINTS)
    fun maxContours(): Int = table.readUInt16(MAX_CONTOURS)
    fun maxCompositePoints(): Int = table.readUInt16(MAX_COMPOSITE_POINTS)
    fun maxCompositeContours(): Int = table.readUInt16(MAX_COMPOSITE_CONTOURS)
    fun maxZones(): Int = table.readUInt16(MAX_ZONES)
    fun maxTwilightPoints(): Int = table.readUInt16(MAX_TWILIGHT_POINTS)
    fun maxStorage(): Int = table.readUInt16(MAX_STORAGE)
    fun maxFunctionDefs(): Int = table.readUInt16(MAX_FUNCTION_DEFS)
    fun maxInstructionDefs(): Int = table.readUInt16(MAX_INSTRUCTION_DEFS)
    fun maxStackElements(): Int = table.readUInt16(MAX_STACK_ELEMENTS)
    fun maxSizeOfInstructions(): Int = table.readUInt16(MAX_SIZE_OF_INSTRUCTIONS)
    fun maxComponentElements(): Int = table.readUInt16(MAX_COMPONENT_ELEMENTS)
    fun maxComponentDepth(): Int = table.readUInt16(MAX_COMPONENT_DEPTH)

    companion object {
        private const val VERSION = 0
        private const val NUM_GLYPHS = 4
        private const val MAX_POINTS = 6
        private const val MAX_CONTOURS = 8
        private const val MAX_COMPOSITE_POINTS = 10
        private const val MAX_COMPOSITE_CONTOURS = 12
        private const val MAX_ZONES = 14
        private const val MAX_TWILIGHT_POINTS = 16
        private const val MAX_STORAGE = 18
        private const val MAX_FUNCTION_DEFS = 20
        private const val MAX_INSTRUCTION_DEFS = 22
        private const val MAX_STACK_ELEMENTS = 24
        private const val MAX_SIZE_OF_INSTRUCTIONS = 26
        private const val MAX_COMPONENT_ELEMENTS = 28
        private const val MAX_COMPONENT_DEPTH = 30
        private const val TABLE_LENGTH = 32

        /**
         * Constructs a `MaxpTable` object from the specified typeface.
         *
         * @param typeface The typeface from which the `MaxpTable` object is constructed.
         * @return A new `MaxpTable` object, or `null` if `maxp' table does not exist in the specified
         *         typeface.
         */
        @JvmStatic
        fun from(typeface: Typeface): MaxpTable? {
            return SfntTables.readTable(typeface, "maxp", TABLE_LENGTH)?.let { MaxpTable(it) }
        }
    }
}
