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
import com.mta.tehreer.internal.JniBridge
import com.mta.tehreer.internal.sfnt.DataTable
import com.mta.tehreer.internal.sfnt.SfntTable
import com.mta.tehreer.sfnt.SfntTag

internal object SfntTables {
    init {
        JniBridge.loadLibrary()
    }

    /** The parts of the language of a name record are `null` if the language does not have them. */
    @JvmStatic external fun getNameLanguage(platformId: Int, languageId: Int): String?
    @JvmStatic external fun getNameRegion(platformId: Int, languageId: Int): String?
    @JvmStatic external fun getNameScript(platformId: Int, languageId: Int): String?
    @JvmStatic external fun getNameVariant(platformId: Int, languageId: Int): String?

    @JvmStatic external fun getNameCharset(platformId: Int, encodingId: Int): String?

    @JvmStatic external fun getGlyphName(typefaceHandle: Long, glyphId: Int): String

    /**
     * Reads the data of a table of the typeface. The fields that an older version of the table
     * does not have read as zero, so the data is padded up to `minLength`.
     *
     * @return The table, or `null` if the typeface does not have it.
     */
    fun readTable(typeface: Typeface, tag: String, minLength: Int): SfntTable? {
        val data = typeface.getTableData(SfntTag.make(tag)) ?: return null

        return DataTable(if (data.size < minLength) data.copyOf(minLength) else data)
    }
}
