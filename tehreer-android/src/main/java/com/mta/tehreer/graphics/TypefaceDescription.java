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

import com.mta.tehreer.sfnt.tables.HeadTable;
import com.mta.tehreer.sfnt.tables.NameTable;
import com.mta.tehreer.sfnt.tables.OS2Table;

import java.util.Locale;

class TypefaceDescription {

    private static final int PLATFORM_MACINTOSH = 1;
    private static final int PLATFORM_WINDOWS = 3;

    private static final int NAME_FONT_FAMILY = 1;
    private static final int NAME_FONT_SUBFAMILY = 2;
    private static final int NAME_FULL = 4;
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

    final String familyName;
    final String styleName;
    final String fullName;
    final TypeWeight weight;
    final TypeWidth width;
    final TypeSlope slope;

    private static String getEnglishName(NameTable nameTable, int nameId) {
        if (nameTable != null) {
            int recordCount = nameTable.recordCount();
            NameTable.Record candidate = null;

            for (int i = 0; i < recordCount; i++) {
                NameTable.Record record = nameTable.recordAt(i);
                if (record.nameId != nameId) {
                    continue;
                }

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

            if (candidate != null) {
                return candidate.string();
            }
        }

        return null;
    }

    private static String getFamilyName(NameTable nameTable, OS2Table os2Table) {
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

    private static String getStyleName(NameTable nameTable, OS2Table os2Table) {
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

    private static String getFullName(NameTable nameTable) {
        return getEnglishName(nameTable, NAME_FULL);
    }

    static TypefaceDescription deduce(Typeface typeface) {
        HeadTable headTable = null;
        OS2Table os2Table = null;
        NameTable nameTable = null;

        try {
            headTable = new HeadTable(typeface);
        } catch (RuntimeException ignored) {
        }
        try {
            os2Table = new OS2Table(typeface);
        } catch (RuntimeException ignored) {
        }
        try {
            nameTable = new NameTable(typeface);
        } catch (RuntimeException ignored) {
        }

        String familyName = getFamilyName(nameTable, os2Table);
        String styleName = getStyleName(nameTable, os2Table);
        String fullName = getFullName(nameTable);
        TypeWeight weight = null;
        TypeWidth width = null;
        TypeSlope slope = null;

        if (os2Table != null) {
            int version = os2Table.version();
            int fsSelection = os2Table.fsSelection();
            int usWeightClass = os2Table.usWeightClass();
            int usWidthClass = os2Table.usWidthClass();

            weight = TypeWeight.valueOf(usWeightClass);
            width = TypeWidth.valueOf(usWidthClass);

            if (version >= 4 && (fsSelection & FS_SELECTION_OBLIQUE) != 0) {
                slope = TypeSlope.OBLIQUE;
            } else if ((fsSelection & FS_SELECTION_ITALIC) != 0) {
                slope = TypeSlope.ITALIC;
            }
        } else if (headTable != null) {
            int macStyle = headTable.macStyle();

            if ((macStyle & MAC_STYLE_BOLD) != 0) {
                weight = TypeWeight.BOLD;
            }

            if ((macStyle & MAC_STYLE_CONDENSED) != 0) {
                width = TypeWidth.CONDENSED;
            } else if ((macStyle & MAC_STYLE_EXTENDED) != 0) {
                width = TypeWidth.EXPANDED;
            }

            if ((macStyle & MAC_STYLE_ITALIC) != 0) {
                slope = TypeSlope.ITALIC;
            }
        }

        return new TypefaceDescription(familyName, styleName, fullName,
                                       weight, width, slope);
    }

    private TypefaceDescription(String familyName, String styleName, String fullName,
                        TypeWeight weight, TypeWidth width, TypeSlope slope) {
        this.familyName = familyName;
        this.styleName = styleName;
        this.fullName = fullName;
        this.weight = (weight != null ? weight : TypeWeight.REGULAR);
        this.width = (width != null ? width : TypeWidth.NORMAL);
        this.slope = (slope != null ? slope : TypeSlope.PLAIN);
    }
}
