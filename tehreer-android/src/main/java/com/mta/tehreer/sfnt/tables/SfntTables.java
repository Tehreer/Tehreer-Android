/*
 * Copyright (C) 2017-2018 Muhammad Tayyab Akram
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

import androidx.annotation.NonNull;

import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.internal.JniBridge;

class SfntTables {
    static {
        JniBridge.loadLibrary();
    }

    static final int TABLE_HEAD = 0;
    static final int TABLE_MAXP = 1;
    static final int TABLE_OS_2 = 2;
    static final int TABLE_HHEA = 3;
    static final int TABLE_POST = 5;

    static native String[] getNameLocale(int platformId, int languageId);
    static native String getNameCharset(int platformId, int encodingId);

    static native int getNameCount(@NonNull Typeface typeface);
    static native NameTable.Record getNameRecord(@NonNull Typeface typeface, int index);

    static native String getGlyphName(@NonNull Typeface typeface, int index);

    static native long getTablePointer(@NonNull Typeface typeface, int table);
}
