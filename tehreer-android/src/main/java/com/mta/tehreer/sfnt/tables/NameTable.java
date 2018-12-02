/*
 * Copyright (C) 2016-2018 Muhammad Tayyab Akram
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

package com.mta.tehreer.sfnt.tables;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.internal.Description;
import com.mta.tehreer.internal.Sustain;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.Locale;

/**
 * Represents an OpenType `name' table.
 */
public class NameTable {
    private final @NonNull Typeface typeface;

    /**
     * Constructs an <code>NameTable</code> object from the specified typeface.
     *
     * @param typeface The typeface from which the <code>NameTable</code> object is
     *                 constructed.
     *
     * @throws NullPointerException if <code>typeface</code> is <code>null</code>.
     * @throws RuntimeException if <code>typeface</code> does not contain `name' table.
     */
    public NameTable(@NonNull Typeface typeface) {
        if (typeface == null) {
            throw new NullPointerException("Typeface is null");
        }

        this.typeface = typeface;
    }

    /**
     * Returns the number of name records in this table.
     *
     * @return The number of name records in this table.
     */
    public int recordCount() {
        return SfntTables.getNameCount(typeface);
    }

    /**
     * Retrieves a record of this table at a given index.
     *
     * @param index The index of the name record.
     * @return A record of OpenType `name' table at a given index.
     *
     * @throws IndexOutOfBoundsException if <code>index</code> is negative, or <code>index</code> is
     *         is greater than or equal to {@link #recordCount()}.
     */
    public @NonNull Record recordAt(int index) {
        if (index < 0 || index >= recordCount()) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }

        return SfntTables.getNameRecord(typeface, index);
    }

    /**
     * Represents a single record of OpenType `name' table.
     */
    public static class Record {
        /**
         * The name id of this record.
         */
        public int nameId;
        /**
         * The platform id of this record.
         */
        public int platformId;
        /**
         * The language id of this record.
         */
        public int languageId;
        /**
         * The encoding id of this record.
         */
        public int encodingId;
        /**
         * The encoded bytes of this record.
         */
        public byte[] bytes;

        /**
         * Constructs a <code>NameTable.Record</code> object.
         *
         * @param nameId The name id of record.
         * @param platformId The platform id of record.
         * @param languageId The language id of record.
         * @param encodingId The encoding id of record.
         * @param bytes The encoded bytes of record.
         */
        @Sustain
        public Record(int nameId, int platformId, int languageId, int encodingId, byte[] bytes) {
            this.nameId = nameId;
            this.platformId = platformId;
            this.languageId = languageId;
            this.encodingId = encodingId;
            this.bytes = bytes;
        }

        /**
         * Constructs a <code>NameTable.Record</code> object.
         */
        public Record() {
        }

        /**
         * Generates a relevant locale for this record by interpreting {@link #platformId} and
         * {@link #languageId}.
         *
         * @return The relevant locale for this record.
         */
        public @NonNull Locale locale() {
            String[] values = SfntTables.getNameLocale(platformId, languageId);
            String language = values[0];
            String region = values[1];
            String script = values[2];
            String variant = values[3];

            if (Build.VERSION.SDK_INT >= 21) {
                Locale.Builder builder = new Locale.Builder();
                builder.setLanguage(language);
                builder.setRegion(region);
                builder.setScript(script);
                builder.setVariant(variant);

                return builder.build();
            }

            if (region == null) {
                region = "";
            }

            String secret = "";
            if (script != null) {
                secret += script;
            }
            if (variant != null) {
                if (secret.length() > 0) {
                    secret += '-';
                }
                secret += variant;
            }

            return new Locale(language, region, secret);
        }

        /**
         * Determines a suitable charset for this record reflecting {@link #platformId} and
         * {@link #encodingId}. If a charset cannot be determined or is unsupported in the current
         * Java virtual machine, then <code>null</code> is returned.
         *
         * @return The suitable charset for this record, or <code>null</code>.
         */
        public @Nullable Charset charset() {
            Charset charset = null;

            try {
                String charsetName = SfntTables.getNameCharset(platformId, encodingId);
                if (charsetName != null) {
                    charset = Charset.forName(charsetName);
                }
            } catch (IllegalCharsetNameException | UnsupportedCharsetException ignored) {
            }

            return charset;
        }

        /**
         * Decodes the {@link #bytes} array into a string using a suitable charset. If no suitable
         * charset is available, then <code>null</code> is returned.
         *
         * @return The decoded string for this record, or <code>null</code>.
         */
        public @Nullable String string() {
            Charset charset = charset();
            if (charset != null && bytes != null) {
                return new String(bytes, charset);
            }

            return null;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }

            Record other = (Record) obj;
            return nameId == other.nameId
                    && platformId == other.platformId
                    && languageId == other.languageId
                    && encodingId == other.encodingId
                    && Arrays.equals(bytes, other.bytes);
        }

        @Override
        public int hashCode() {
            int result = nameId;
            result = 31 * result + platformId;
            result = 31 * result + languageId;
            result = 31 * result + encodingId;
            result = 31 * result + Arrays.hashCode(bytes);

            return result;
        }

        @Override
        public String toString() {
            Charset charset = charset();

            return "NameTable.Record{nameId=" + nameId
                    + ", platformId=" + platformId
                    + ", languageId=" + languageId
                    + ", encodingId=" + encodingId
                    + ", bytes=" + Description.forByteArray(bytes)
                    + ", locale=" + locale().toString()
                    + ", charset=" + (charset != null ? charset.name() : null)
                    + ", string=" + string()
                    + "}";
        }
    }
}
