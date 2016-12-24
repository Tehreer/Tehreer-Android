/*
 * Copyright (C) 2016 Muhammad Tayyab Akram
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

import java.util.Locale;

public class NameEntry {

    private final int nameId;
    private final int platformId;
    private final int languageId;
    private final int encodingId;
    private final byte[] encodedBytes;
    private final Locale relevantLocale;
    private final String decodedString;

    NameEntry(int nameId, int platformId, int languageId, int encodingId, byte[] encodedBytes,
              Locale relevantLocale, String decodedString) {
        this.nameId = nameId;
        this.platformId = platformId;
        this.languageId = languageId;
        this.encodingId = encodingId;
        this.encodedBytes = encodedBytes;
        this.relevantLocale = relevantLocale;
        this.decodedString = decodedString;
    }

    public int getNameId() {
        return nameId;
    }

    public int getPlatformId() {
        return platformId;
    }

    public int getLanguageId() {
        return languageId;
    }

    public int getEncodingId() {
        return encodingId;
    }

    public byte[] getEncodedBytes() {
        return encodedBytes;
    }

    public Locale getRelevantLocale() {
        return relevantLocale;
    }

    public String getDecodedString() {
        return decodedString;
    }
}
