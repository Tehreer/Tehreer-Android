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

package com.mta.tehreer.graphics;

import com.mta.tehreer.opentype.FontHeaderTable;
import com.mta.tehreer.opentype.NameTable;
import com.mta.tehreer.opentype.Os2WinMetricsTable;

import java.util.Locale;

class TypefaceDescription {

    private static final int PLATFORM_MACINTOSH = 1;
    private static final int PLATFORM_WINDOWS = 3;

    private static final int NAME_FONT_FAMILY = 1;
    private static final int NAME_FONT_SUBFAMILY = 2;
    private static final int NAME_TYPOGRAPHIC_FAMILY = 16;
    private static final int NAME_TYPOGRAPHIC_SUBFAMILY = 17;
    private static final int NAME_WWS_FAMILY = 21;
    private static final int NAME_WWS_SUBFAMILY = 22;

    private static final int MAC_STYLE_BOLD = 1 << 0;
    private static final int MAC_STYLE_ITALIC = 1 << 1;
    private static final int MAC_STYLE_CONDENSED = 1 << 5;
    private static final int MAC_STYLE_EXTENDED = 1 << 6;

    private static final int FS_SELECTION_ITALIC = 1 << 0;
    private static final int FS_SELECTION_WWS = 1 << 8;
    private static final int FS_SELECTION_OBLIQUE = 1 << 9;

    private String mFamilyName;
    private String mFaceName;
    private TypeWeight mWeight;
    private TypeStretch mStretch;
    private TypeStyle mStyle;

    private static String getEnglishName(NameTable nameTable, int nameId) {
        int recordCount = nameTable.recordCount();
        NameTable.Record candidate = null;

        for (int i = 0; i < recordCount; i++) {
            NameTable.Record record = nameTable.recordAt(i);

            if (record.nameId == nameId) {
                Locale locale = record.locale();

                if (locale.getLanguage().equals("en")) {
                    if (record.platformId == PLATFORM_WINDOWS && locale.getCountry().equals("US")) {
                        return record.string();
                    }

                    if (candidate == null || record.platformId == PLATFORM_MACINTOSH) {
                        candidate = record;
                    }
                }
            }
        }

        if (candidate != null) {
            return candidate.string();
        }

        return null;
    }

    private static String getFamilyName(NameTable nameTable, Os2WinMetricsTable os2Table) {
        String familyName = null;

        if (os2Table != null && (os2Table.fsSelection() & FS_SELECTION_WWS) == 0) {
            familyName = getEnglishName(nameTable, NAME_WWS_FAMILY);
        }

        if (familyName == null) {
            familyName = getEnglishName(nameTable, NAME_TYPOGRAPHIC_FAMILY);
        }

        if (familyName == null) {
            familyName = getEnglishName(nameTable, NAME_FONT_FAMILY);
        }

        return familyName;
    }

    private static String getFaceName(NameTable nameTable, Os2WinMetricsTable os2Table) {
        String familyName = null;

        if (os2Table != null && (os2Table.fsSelection() & FS_SELECTION_WWS) == 0) {
            familyName = getEnglishName(nameTable, NAME_WWS_SUBFAMILY);
        }

        if (familyName == null) {
            familyName = getEnglishName(nameTable, NAME_TYPOGRAPHIC_SUBFAMILY);
        }

        if (familyName == null) {
            familyName = getEnglishName(nameTable, NAME_FONT_SUBFAMILY);
        }

        return familyName;
    }

    TypefaceDescription(Typeface typeface) {
        FontHeaderTable headTable = FontHeaderTable.forTypeface(typeface);
        Os2WinMetricsTable os2Table = Os2WinMetricsTable.forTypeface(typeface);
        NameTable nameTable = NameTable.forTypeface(typeface);

        mFamilyName = getFamilyName(nameTable, os2Table);
        mFaceName = getFaceName(nameTable, os2Table);

        if (os2Table != null) {
            int version = os2Table.version();
            int fsSelection = os2Table.fsSelection();
            int usWeightClass = os2Table.usWeightClass();
            int usWidthClass = os2Table.usWidthClass();

            mStretch = TypeStretch.valueOf(usWidthClass);
            mWeight = TypeWeight.valueOf(usWeightClass);

            if (version >= 4 && (fsSelection & FS_SELECTION_OBLIQUE) != 0) {
                mStyle = TypeStyle.OBLIQUE;
            } else if ((fsSelection & FS_SELECTION_ITALIC) != 0) {
                mStyle = TypeStyle.ITALIC;
            }
        } else if (headTable != null) {
            int macStyle = headTable.macStyle();

            if ((macStyle & MAC_STYLE_CONDENSED) != 0) {
                mStretch = TypeStretch.CONDENSED;
            } else if ((macStyle & MAC_STYLE_EXTENDED) != 0) {
                mStretch = TypeStretch.EXPANDED;
            }

            if ((macStyle & MAC_STYLE_BOLD) != 0) {
                mWeight = TypeWeight.BOLD;
            }

            if ((macStyle & MAC_STYLE_ITALIC) != 0) {
                mStyle = TypeStyle.ITALIC;
            }
        }

        if (mWeight == null) {
            mWeight = TypeWeight.NORMAL;
        }
        if (mStretch == null) {
            mStretch = TypeStretch.NORMAL;
        }
        if (mStyle == null) {
            mStyle = TypeStyle.NORMAL;
        }
    }

    String getFamilyName() {
        return mFamilyName;
    }

    String getFaceName() {
        return mFaceName;
    }

    TypeWeight getWeight() {
        return mWeight;
    }

    TypeStretch getStretch() {
        return mStretch;
    }

    TypeStyle getStyle() {
        return mStyle;
    }
}
