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
 * Represents an OpenType `post' table.
 */
class PostTable private constructor(
    private val typeface: Typeface,
    private val table: SfntTable
) {
    /**
     * Constructs a `PostTable` object from the specified typeface.
     *
     * @param typeface The typeface from which the `PostTable` object is constructed.
     *
     * @throws RuntimeException if `typeface` does not contain `post' table.
     */
    constructor(typeface: Typeface) : this(
        typeface,
        checkNotNull(SfntTables.readTable(typeface, "post", TABLE_LENGTH)) { "The typeface does not contain `post' table" }
    )

    fun version(): Int = table.readInt32(VERSION)
    fun italicAngle(): Int = table.readInt32(ITALIC_ANGLE)
    fun underlinePosition(): Short = table.readInt16(UNDERLINE_POSITION)
    fun underlineThickness(): Short = table.readInt16(UNDERLINE_THICKNESS)
    fun isFixedPitch(): Long = table.readUInt32(IS_FIXED_PITCH)
    fun minMemType42(): Long = table.readUInt32(MIN_MEM_TYPE_42)
    fun maxMemType42(): Long = table.readUInt32(MAX_MEM_TYPE_42)
    fun minMemType1(): Long = table.readUInt32(MIN_MEM_TYPE_1)
    fun maxMemType1(): Long = table.readUInt32(MAX_MEM_TYPE_1)

    fun numberOfGlyphs(): Int = typeface.glyphCount

    fun glyphNameAt(index: Int): String {
        if (index < 0 || index >= numberOfGlyphs()) {
            throw IndexOutOfBoundsException("Index: $index")
        }

        return SfntTables.getGlyphName(typeface.nativeTypeface, index, typeface)
    }

    companion object {
        private const val VERSION = 0
        private const val ITALIC_ANGLE = 4
        private const val UNDERLINE_POSITION = 8
        private const val UNDERLINE_THICKNESS = 10
        private const val IS_FIXED_PITCH = 12
        private const val MIN_MEM_TYPE_42 = 16
        private const val MAX_MEM_TYPE_42 = 20
        private const val MIN_MEM_TYPE_1 = 24
        private const val MAX_MEM_TYPE_1 = 28
        private const val TABLE_LENGTH = 32

        /**
         * Constructs a `PostTable` object from the specified typeface.
         *
         * @param typeface The typeface from which the `PostTable` object is constructed.
         * @return A new `PostTable` object, or `null` if `post' table does not exist in the specified
         *         typeface.
         */
        fun from(typeface: Typeface): PostTable? {
            return SfntTables.readTable(typeface, "post", TABLE_LENGTH)?.let { PostTable(typeface, it) }
        }
    }
}
