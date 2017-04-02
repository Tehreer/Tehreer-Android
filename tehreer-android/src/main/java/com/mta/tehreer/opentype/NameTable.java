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

import android.os.Build;

import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.internal.Sustain;
import com.mta.tehreer.internal.util.Description;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Locale;

/**
 * Represents a collection of entries in SFNT 'name' table.
 */
public class NameTable {

    private static final int TAG_ID_LANGUAGE = 1;
    private static final int TAG_ID_REGION = 2;
    private static final int TAG_ID_SCRIPT = 3;
    private static final int TAG_ID_VARIANT = 4;

    private final Typeface typeface;

    /**
     * Returns an <code>NameTable</code> object for the specified typeface.
     *
     * @param typeface The typeface for which to create the <code>NameTable</code> object.
     * @return A new <code>NameTable</code> object for the specified typeface.
     *
     * @throws NullPointerException if <code>typeface</code> is <code>null</code>.
     */
    public static NameTable forTypeface(Typeface typeface) {
        if (typeface == null) {
            throw new NullPointerException("Typeface is null");
        }

        return new NameTable(typeface);
    }

    private NameTable(Typeface typeface) {
        this.typeface = typeface;
    }

    /**
     * Returns the number of name entries in SFNT 'name' table.
     *
     * @return The number of name entries in SFNT 'name' table.
     */
    public int recordCount() {
        return nativeGetRecordCount(typeface);
    }

    /**
     * Retrieves a record of SFNT 'name' table at a given index.
     *
     * @param index The index of the 'name' record.
     * @return A record of SFNT 'name' table at a given index.
     *
     * @throws IndexOutOfBoundsException if <code>index</code> is negative, or
     *         <code>index</code> is greater than or equal to {@link #recordCount()}
     */
    public Record recordAt(int index) {
        if (index < 0 || index >= recordCount()) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }

        return nativeGetRecordAt(typeface, index);
    }

    private static native String nativeGetLocaleTag(int tagId, int platformId, int languageId);
    private static native String nativeGetCharsetName(int platformId, int encodingId);

    private static native int nativeGetRecordCount(Typeface typeface);
    private static native Record nativeGetRecordAt(Typeface typeface, int index);

    /**
     * Represents a single entry of SFNT 'name' table.
     */
    public static class Record {

        /**
         * The name id of this entry.
         */
        public int nameId;
        /**
         * The platform id of this entry.
         */
        public int platformId;
        /**
         * The language id of this entry.
         */
        public int languageId;
        /**
         * The encoding id of this entry.
         */
        public int encodingId;
        /**
         * The encoded bytes of this entry.
         */
        public byte[] bytes;

        @Sustain
        public Record(int nameId, int platformId, int languageId, int encodingId, byte[] bytes) {
            this.nameId = nameId;
            this.platformId = platformId;
            this.languageId = languageId;
            this.encodingId = encodingId;
            this.bytes = bytes;
        }

        public Record() {
        }

        /**
         * Generates a relevant locale for this entry by interpreting {@link #platformId} and
         * {@link #languageId}.
         *
         * @return The relevant locale for this entry.
         */
        public Locale locale() {
            String language = nativeGetLocaleTag(TAG_ID_LANGUAGE, platformId, languageId);
            String region = nativeGetLocaleTag(TAG_ID_REGION, platformId, languageId);
            String script = nativeGetLocaleTag(TAG_ID_SCRIPT, platformId, languageId);
            String variant = nativeGetLocaleTag(TAG_ID_VARIANT, platformId, languageId);

            if (Build.VERSION.SDK_INT >= 21) {
                Locale.Builder builder = new Locale.Builder();
                builder.setLanguage(language);
                builder.setRegion(region);
                builder.setScript(script);
                builder.setVariant(variant);

                return builder.build();
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
         * Determines a suitable charset for this entry reflecting {@link #platformId} and
         * {@link #encodingId}. If a charset cannot be determined or is unsupported in the current
         * Java virtual machine, then <code>null</code> is returned.
         *
         * @return The suitable charset for this entry, or <code>null</code>.
         */
        public Charset charset() {
            Charset charset = null;

            try {
                String charsetName = nativeGetCharsetName(platformId, encodingId);
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
         * @return The decoded string for this entry, or <code>null</code>.
         */
        public String string() {
            Charset charset = charset();
            if (charset != null && bytes != null) {
                return new String(bytes, charset);
            }

            return null;
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
