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
import com.mta.tehreer.internal.Description
import com.mta.tehreer.internal.sfnt.DataTable
import com.mta.tehreer.internal.sfnt.SfntTable
import com.mta.tehreer.sfnt.SfntTag
import java.nio.charset.Charset
import java.nio.charset.IllegalCharsetNameException
import java.nio.charset.UnsupportedCharsetException
import java.util.Locale

/**
 * Represents an OpenType `name' table.
 */
class NameTable private constructor(data: ByteArray) {
    private val records = readRecords(data)

    /**
     * Constructs an `NameTable` object from the specified typeface.
     *
     * @param typeface The typeface from which the `NameTable` object is constructed.
     *
     * @throws RuntimeException if `typeface` does not contain `name' table.
     */
    constructor(typeface: Typeface) : this(
        checkNotNull(typeface.getTableData(SfntTag.make("name"))) { "The typeface does not contain `name' table" }
    )

    /**
     * Returns the number of name records in this table.
     *
     * @return The number of name records in this table.
     */
    fun recordCount(): Int {
        return records.size
    }

    /**
     * Returns the name record at the specified index.
     *
     * @param index The index of the name record.
     * @return A record of OpenType `name' table at a given index.
     *
     * @throws IndexOutOfBoundsException if `index` is negative, or `index` is greater than or equal
     *         to [recordCount].
     */
    fun recordAt(index: Int): Record {
        if (index < 0 || index >= records.size) {
            throw IndexOutOfBoundsException("Index: $index")
        }

        // The record is a copy, as its fields can be changed.
        val record = records[index]

        return Record(
            record.nameId, record.platformId, record.languageId, record.encodingId,
            record.bytes?.clone()
        )
    }

    /**
     * Represents a single record of OpenType `name' table.
     */
    class Record @JvmOverloads constructor(
        /** The name id of this record. */
        @JvmField var nameId: Int = 0,
        /** The platform id of this record. */
        @JvmField var platformId: Int = 0,
        /** The language id of this record. */
        @JvmField var languageId: Int = 0,
        /** The encoding id of this record. */
        @JvmField var encodingId: Int = 0,
        /** The encoded bytes of this record. */
        @JvmField var bytes: ByteArray? = null
    ) {
        /**
         * Returns the locale of this record.
         */
        fun locale(): Locale {
            return Locale.Builder()
                .setLanguage(SfntTables.getNameLanguage(platformId, languageId))
                .setRegion(SfntTables.getNameRegion(platformId, languageId))
                .setScript(SfntTables.getNameScript(platformId, languageId))
                .setVariant(SfntTables.getNameVariant(platformId, languageId))
                .build()
        }

        /**
         * Returns the charset of the bytes of this record, or `null` if it is not known.
         */
        fun charset(): Charset? {
            return try {
                SfntTables.getNameCharset(platformId, encodingId)?.let { Charset.forName(it) }
            } catch (ignored: IllegalCharsetNameException) {
                null
            } catch (ignored: UnsupportedCharsetException) {
                null
            }
        }

        /**
         * Returns the string that the bytes of this record represent, or `null` if their charset is
         * not known.
         */
        fun string(): String? {
            val charset = charset()
            val bytes = bytes

            return if (charset != null && bytes != null) String(bytes, charset) else null
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other == null || javaClass != other.javaClass) {
                return false
            }

            other as Record

            return nameId == other.nameId
                && platformId == other.platformId
                && languageId == other.languageId
                && encodingId == other.encodingId
                && bytes.contentEquals(other.bytes)
        }

        override fun hashCode(): Int {
            var result = nameId
            result = 31 * result + platformId
            result = 31 * result + languageId
            result = 31 * result + encodingId
            result = 31 * result + bytes.contentHashCode()

            return result
        }

        override fun toString(): String {
            val charset = charset()

            return "NameTable.Record{nameId=$nameId" +
                ", platformId=$platformId" +
                ", languageId=$languageId" +
                ", encodingId=$encodingId" +
                ", bytes=${Description.forByteArray(bytes!!)}" +
                ", locale=${locale()}" +
                ", charset=${charset?.name()}" +
                ", string=${string()}" +
                "}"
        }
    }

    companion object {
        private const val HEADER_LENGTH = 6
        private const val RECORD_LENGTH = 12
        private const val LANG_TAG_RECORD_LENGTH = 4

        /**
         * Constructs a `NameTable` object from the specified typeface.
         *
         * @param typeface The typeface from which the `NameTable` object is constructed.
         * @return A new `NameTable` object, or `null` if `name' table does not exist in the
         *         specified typeface.
         */
        fun from(typeface: Typeface): NameTable? {
            return typeface.getTableData(SfntTag.make("name"))?.let { NameTable(it) }
        }

        /**
         * Reads the records that point to a string inside the table, and skips the empty ones, in
         * the order that they are stored.
         */
        private fun readRecords(data: ByteArray): List<Record> {
            val records = ArrayList<Record>()
            if (data.size < HEADER_LENGTH) {
                return records
            }

            val table: SfntTable = DataTable(data)
            val format = table.readUInt16(0)
            val count = table.readUInt16(2)
            val storageOffset = table.readUInt16(4)

            var storageStart = HEADER_LENGTH + RECORD_LENGTH.toLong() * count
            if (storageStart > data.size) {
                return records
            }

            // The language tag records of the format 1 come before the strings.
            if (format == 1) {
                if (storageStart + 2 > data.size) {
                    return records
                }

                storageStart += 2 + LANG_TAG_RECORD_LENGTH.toLong() * table.readUInt16(storageStart.toInt())
            }

            for (i in 0 until count) {
                val offset = HEADER_LENGTH + i * RECORD_LENGTH
                val length = table.readUInt16(offset + 8)
                val stringStart = storageOffset + table.readUInt16(offset + 10).toLong()

                if (length > 0 && stringStart >= storageStart && stringStart + length <= data.size) {
                    records.add(
                        Record(
                            table.readUInt16(offset + 6),
                            table.readUInt16(offset),
                            table.readUInt16(offset + 4),
                            table.readUInt16(offset + 2),
                            data.copyOfRange(stringStart.toInt(), stringStart.toInt() + length)
                        )
                    )
                }
            }

            return records
        }
    }
}
