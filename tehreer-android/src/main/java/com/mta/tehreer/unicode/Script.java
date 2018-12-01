/*
 * Copyright (C) 2018 Muhammad Tayyab Akram
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

package com.mta.tehreer.unicode;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Represents the script of a character in Unicode specification. The constants correspond to the
 * script property values defined in
 * <a href="https://www.unicode.org/reports/tr24/#Data_File_PVA">
 *     Unicode Standard Annex #24: PropertyValueAliases.txt
 * </a>.
 */
public final class Script {
    @IntDef({
        INHERITED,
        COMMON,
        UNKNOWN,
        ARABIC,
        ARMENIAN,
        BENGALI,
        BOPOMOFO,
        CYRILLIC,
        DEVANAGARI,
        GEORGIAN,
        GREEK,
        GUJARATI,
        GURMUKHI,
        HANGUL,
        HAN,
        HEBREW,
        HIRAGANA,
        KATAKANA,
        KANNADA,
        LAO,
        LATIN,
        MALAYALAM,
        ORIYA,
        TAMIL,
        TELUGU,
        THAI,
        TIBETAN,
        BRAILLE,
        CANADIAN_ABORIGINAL,
        CHEROKEE,
        ETHIOPIC,
        KHMER,
        MONGOLIAN,
        MYANMAR,
        OGHAM,
        RUNIC,
        SINHALA,
        SYRIAC,
        THAANA,
        YI,
        DESERET,
        GOTHIC,
        OLD_ITALIC,
        BUHID,
        HANUNOO,
        TAGBANWA,
        TAGALOG,
        CYPRIOT,
        LIMBU,
        LINEAR_B,
        OSMANYA,
        SHAVIAN,
        TAI_LE,
        UGARITIC,
        BUGINESE,
        COPTIC,
        GLAGOLITIC,
        KHAROSHTHI,
        SYLOTI_NAGRI,
        NEW_TAI_LUE,
        TIFINAGH,
        OLD_PERSIAN,
        BALINESE,
        NKO,
        PHAGS_PA,
        PHOENICIAN,
        CUNEIFORM,
        CARIAN,
        CHAM,
        KAYAH_LI,
        LEPCHA,
        LYCIAN,
        LYDIAN,
        OL_CHIKI,
        REJANG,
        SAURASHTRA,
        SUNDANESE,
        VAI,
        IMPERIAL_ARAMAIC,
        AVESTAN,
        BAMUM,
        EGYPTIAN_HIEROGLYPHS,
        JAVANESE,
        KAITHI,
        TAI_THAM,
        LISU,
        MEETEI_MAYEK,
        OLD_TURKIC,
        INSCRIPTIONAL_PAHLAVI,
        INSCRIPTIONAL_PARTHIAN,
        SAMARITAN,
        OLD_SOUTH_ARABIAN,
        TAI_VIET,
        BATAK,
        BRAHMI,
        MANDAIC,
        CHAKMA,
        MEROITIC_CURSIVE,
        MEROITIC_HIEROGLYPHS,
        MIAO,
        SHARADA,
        SORA_SOMPENG,
        TAKRI,
        CAUCASIAN_ALBANIAN,
        BASSA_VAH,
        DUPLOYAN,
        ELBASAN,
        GRANTHA,
        PAHAWH_HMONG,
        KHOJKI,
        LINEAR_A,
        MAHAJANI,
        MANICHAEAN,
        MENDE_KIKAKUI,
        MODI,
        MRO,
        OLD_NORTH_ARABIAN,
        NABATAEAN,
        PALMYRENE,
        PAU_CIN_HAU,
        OLD_PERMIC,
        PSALTER_PAHLAVI,
        SIDDHAM,
        KHUDAWADI,
        TIRHUTA,
        WARANG_CITI,
        AHOM,
        HATRAN,
        ANATOLIAN_HIEROGLYPHS,
        OLD_HUNGARIAN,
        MULTANI,
        SIGNWRITING,
        ADLAM,
        BHAIKSUKI,
        MARCHEN,
        NEWA,
        OSAGE,
        TANGUT,
        MASARAM_GONDI,
        NUSHU,
        SOYOMBO,
        ZANABAZAR_SQUARE,
        DOGRA,
        GUNJALA_GONDI,
        MAKASAR,
        MEDEFAIDRIN,
        HANIFI_ROHINGYA,
        SOGDIAN,
        OLD_SOGDIAN,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Value { }

    /**
     * Script "Inherited".
     */
    public static final int INHERITED = 0x01;

    /**
     * Script "Common".
     */
    public static final int COMMON = 0x02;

    /**
     * Script "Unknown".
     */
    public static final int UNKNOWN = 0x03;

    /**
     * Script "Arabic".
     */
    public static final int ARABIC = 0x04;

    /**
     * Script "Armenian".
     */
    public static final int ARMENIAN = 0x05;

    /**
     * Script "Bengali".
     */
    public static final int BENGALI = 0x06;

    /**
     * Script "Bopomofo".
     */
    public static final int BOPOMOFO = 0x07;

    /**
     * Script "Cyrillic".
     */
    public static final int CYRILLIC = 0x08;

    /**
     * Script "Devanagari".
     */
    public static final int DEVANAGARI = 0x09;

    /**
     * Script "Georgian".
     */
    public static final int GEORGIAN = 0x0A;

    /**
     * Script "Greek".
     */
    public static final int GREEK = 0x0B;

    /**
     * Script "Gujarati".
     */
    public static final int GUJARATI = 0x0C;

    /**
     * Script "Gurmukhi".
     */
    public static final int GURMUKHI = 0x0D;

    /**
     * Script "Hangul".
     */
    public static final int HANGUL = 0x0E;

    /**
     * Script "Han".
     */
    public static final int HAN = 0x0F;

    /**
     * Script "Hebrew".
     */
    public static final int HEBREW = 0x10;

    /**
     * Script "Hiragana".
     */
    public static final int HIRAGANA = 0x11;

    /**
     * Script "Katakana".
     */
    public static final int KATAKANA = 0x12;

    /**
     * Script "Kannada".
     */
    public static final int KANNADA = 0x13;

    /**
     * Script "Lao".
     */
    public static final int LAO = 0x14;

    /**
     * Script "Latin".
     */
    public static final int LATIN = 0x15;

    /**
     * Script "Malayalam".
     */
    public static final int MALAYALAM = 0x16;

    /**
     * Script "Oriya".
     */
    public static final int ORIYA = 0x17;

    /**
     * Script "Tamil".
     */
    public static final int TAMIL = 0x18;

    /**
     * Script "Telugu".
     */
    public static final int TELUGU = 0x19;

    /**
     * Script "Thai".
     */
    public static final int THAI = 0x1A;

    /**
     * Script "Tibetan".
     */
    public static final int TIBETAN = 0x1B;

    /**
     * Script "Braille".
     */
    public static final int BRAILLE = 0x1C;

    /**
     * Script "Canadian_Aboriginal".
     */
    public static final int CANADIAN_ABORIGINAL = 0x1D;

    /**
     * Script "Cherokee".
     */
    public static final int CHEROKEE = 0x1E;

    /**
     * Script "Ethiopic".
     */
    public static final int ETHIOPIC = 0x1F;

    /**
     * Script "Khmer".
     */
    public static final int KHMER = 0x20;

    /**
     * Script "Mongolian".
     */
    public static final int MONGOLIAN = 0x21;

    /**
     * Script "Myanmar".
     */
    public static final int MYANMAR = 0x22;

    /**
     * Script "Ogham".
     */
    public static final int OGHAM = 0x23;

    /**
     * Script "Runic".
     */
    public static final int RUNIC = 0x24;

    /**
     * Script "Sinhala".
     */
    public static final int SINHALA = 0x25;

    /**
     * Script "Syriac".
     */
    public static final int SYRIAC = 0x26;

    /**
     * Script "Thaana".
     */
    public static final int THAANA = 0x27;

    /**
     * Script "Yi".
     */
    public static final int YI = 0x28;

    /**
     * Script "Deseret".
     */
    public static final int DESERET = 0x29;

    /**
     * Script "Gothic".
     */
    public static final int GOTHIC = 0x2A;

    /**
     * Script "Old_Italic".
     */
    public static final int OLD_ITALIC = 0x2B;

    /**
     * Script "Buhid".
     */
    public static final int BUHID = 0x2C;

    /**
     * Script "Hanunoo".
     */
    public static final int HANUNOO = 0x2D;

    /**
     * Script "Tagbanwa".
     */
    public static final int TAGBANWA = 0x2E;

    /**
     * Script "Tagalog".
     */
    public static final int TAGALOG = 0x2F;

    /**
     * Script "Cypriot".
     */
    public static final int CYPRIOT = 0x30;

    /**
     * Script "Limbu".
     */
    public static final int LIMBU = 0x31;

    /**
     * Script "Linear_B".
     */
    public static final int LINEAR_B = 0x32;

    /**
     * Script "Osmanya".
     */
    public static final int OSMANYA = 0x33;

    /**
     * Script "Shavian".
     */
    public static final int SHAVIAN = 0x34;

    /**
     * Script "Tai_Le".
     */
    public static final int TAI_LE = 0x35;

    /**
     * Script "Ugaritic".
     */
    public static final int UGARITIC = 0x36;

    /**
     * Script "Buginese".
     */
    public static final int BUGINESE = 0x37;

    /**
     * Script "Coptic".
     */
    public static final int COPTIC = 0x38;

    /**
     * Script "Glagolitic".
     */
    public static final int GLAGOLITIC = 0x39;

    /**
     * Script "Kharoshthi".
     */
    public static final int KHAROSHTHI = 0x3A;

    /**
     * Script "Syloti_Nagri".
     */
    public static final int SYLOTI_NAGRI = 0x3B;

    /**
     * Script "New_Tai_Lue".
     */
    public static final int NEW_TAI_LUE = 0x3C;

    /**
     * Script "Tifinagh".
     */
    public static final int TIFINAGH = 0x3D;

    /**
     * Script "Old_Persian".
     */
    public static final int OLD_PERSIAN = 0x3E;

    /**
     * Script "Balinese".
     */
    public static final int BALINESE = 0x3F;

    /**
     * Script "Nko".
     */
    public static final int NKO = 0x40;

    /**
     * Script "Phags_Pa".
     */
    public static final int PHAGS_PA = 0x41;

    /**
     * Script "Phoenician".
     */
    public static final int PHOENICIAN = 0x42;

    /**
     * Script "Cuneiform".
     */
    public static final int CUNEIFORM = 0x43;

    /**
     * Script "Carian".
     */
    public static final int CARIAN = 0x44;

    /**
     * Script "Cham".
     */
    public static final int CHAM = 0x45;

    /**
     * Script "Kayah_Li".
     */
    public static final int KAYAH_LI = 0x46;

    /**
     * Script "Lepcha".
     */
    public static final int LEPCHA = 0x47;

    /**
     * Script "Lycian".
     */
    public static final int LYCIAN = 0x48;

    /**
     * Script "Lydian".
     */
    public static final int LYDIAN = 0x49;

    /**
     * Script "Ol_Chiki".
     */
    public static final int OL_CHIKI = 0x4A;

    /**
     * Script "Rejang".
     */
    public static final int REJANG = 0x4B;

    /**
     * Script "Saurashtra".
     */
    public static final int SAURASHTRA = 0x4C;

    /**
     * Script "Sundanese".
     */
    public static final int SUNDANESE = 0x4D;

    /**
     * Script "Vai".
     */
    public static final int VAI = 0x4E;

    /**
     * Script "Imperial_Aramaic".
     */
    public static final int IMPERIAL_ARAMAIC = 0x4F;

    /**
     * Script "Avestan".
     */
    public static final int AVESTAN = 0x50;

    /**
     * Script "Bamum".
     */
    public static final int BAMUM = 0x51;

    /**
     * Script "Egyptian_Hieroglyphs".
     */
    public static final int EGYPTIAN_HIEROGLYPHS = 0x52;

    /**
     * Script "Javanese".
     */
    public static final int JAVANESE = 0x53;

    /**
     * Script "Kaithi".
     */
    public static final int KAITHI = 0x54;

    /**
     * Script "Tai_Tham".
     */
    public static final int TAI_THAM = 0x55;

    /**
     * Script "Lisu".
     */
    public static final int LISU = 0x56;

    /**
     * Script "Meetei_Mayek".
     */
    public static final int MEETEI_MAYEK = 0x57;

    /**
     * Script "Old_Turkic".
     */
    public static final int OLD_TURKIC = 0x58;

    /**
     * Script "Inscriptional_Pahlavi".
     */
    public static final int INSCRIPTIONAL_PAHLAVI = 0x59;

    /**
     * Script "Inscriptional_Parthian".
     */
    public static final int INSCRIPTIONAL_PARTHIAN = 0x5A;

    /**
     * Script "Samaritan".
     */
    public static final int SAMARITAN = 0x5B;

    /**
     * Script "Old_South_Arabian".
     */
    public static final int OLD_SOUTH_ARABIAN = 0x5C;

    /**
     * Script "Tai_Viet".
     */
    public static final int TAI_VIET = 0x5D;

    /**
     * Script "Batak".
     */
    public static final int BATAK = 0x5E;

    /**
     * Script "Brahmi".
     */
    public static final int BRAHMI = 0x5F;

    /**
     * Script "Mandaic".
     */
    public static final int MANDAIC = 0x60;

    /**
     * Script "Chakma".
     */
    public static final int CHAKMA = 0x61;

    /**
     * Script "Meroitic_Cursive".
     */
    public static final int MEROITIC_CURSIVE = 0x62;

    /**
     * Script "Meroitic_Hieroglyphs".
     */
    public static final int MEROITIC_HIEROGLYPHS = 0x63;

    /**
     * Script "Miao".
     */
    public static final int MIAO = 0x64;

    /**
     * Script "Sharada".
     */
    public static final int SHARADA = 0x65;

    /**
     * Script "Sora_Sompeng".
     */
    public static final int SORA_SOMPENG = 0x66;

    /**
     * Script "Takri".
     */
    public static final int TAKRI = 0x67;

    /**
     * Script "Caucasian_Albanian".
     */
    public static final int CAUCASIAN_ALBANIAN = 0x68;

    /**
     * Script "Bassa_Vah".
     */
    public static final int BASSA_VAH = 0x69;

    /**
     * Script "Duployan".
     */
    public static final int DUPLOYAN = 0x6A;

    /**
     * Script "Elbasan".
     */
    public static final int ELBASAN = 0x6B;

    /**
     * Script "Grantha".
     */
    public static final int GRANTHA = 0x6C;

    /**
     * Script "Pahawh_Hmong".
     */
    public static final int PAHAWH_HMONG = 0x6D;

    /**
     * Script "Khojki".
     */
    public static final int KHOJKI = 0x6E;

    /**
     * Script "Linear_A".
     */
    public static final int LINEAR_A = 0x6F;

    /**
     * Script "Mahajani".
     */
    public static final int MAHAJANI = 0x70;

    /**
     * Script "Manichaean".
     */
    public static final int MANICHAEAN = 0x71;

    /**
     * Script "Mende_Kikakui".
     */
    public static final int MENDE_KIKAKUI = 0x72;

    /**
     * Script "Modi".
     */
    public static final int MODI = 0x73;

    /**
     * Script "Mro".
     */
    public static final int MRO = 0x74;

    /**
     * Script "Old_North_Arabian".
     */
    public static final int OLD_NORTH_ARABIAN = 0x75;

    /**
     * Script "Nabataean".
     */
    public static final int NABATAEAN = 0x76;

    /**
     * Script "Palmyrene".
     */
    public static final int PALMYRENE = 0x77;

    /**
     * Script "Pau_Cin_Hau".
     */
    public static final int PAU_CIN_HAU = 0x78;

    /**
     * Script "Old_Permic".
     */
    public static final int OLD_PERMIC = 0x79;

    /**
     * Script "Psalter_Pahlavi".
     */
    public static final int PSALTER_PAHLAVI = 0x7A;

    /**
     * Script "Siddham".
     */
    public static final int SIDDHAM = 0x7B;

    /**
     * Script "Khudawadi".
     */
    public static final int KHUDAWADI = 0x7C;

    /**
     * Script "Tirhuta".
     */
    public static final int TIRHUTA = 0x7D;

    /**
     * Script "Warang_Citi".
     */
    public static final int WARANG_CITI = 0x7E;

    /**
     * Script "Ahom".
     */
    public static final int AHOM = 0x7F;

    /**
     * Script "Hatran".
     */
    public static final int HATRAN = 0x80;

    /**
     * Script "Anatolian_Hieroglyphs".
     */
    public static final int ANATOLIAN_HIEROGLYPHS = 0x81;

    /**
     * Script "Old_Hungarian".
     */
    public static final int OLD_HUNGARIAN = 0x82;

    /**
     * Script "Multani".
     */
    public static final int MULTANI = 0x83;

    /**
     * Script "SignWriting".
     */
    public static final int SIGNWRITING = 0x84;

    /**
     * Script "Adlam".
     */
    public static final int ADLAM = 0x85;

    /**
     * Script "Bhaiksuki".
     */
    public static final int BHAIKSUKI = 0x86;

    /**
     * Script "Marchen".
     */
    public static final int MARCHEN = 0x87;

    /**
     * Script "Newa".
     */
    public static final int NEWA = 0x88;

    /**
     * Script "Osage".
     */
    public static final int OSAGE = 0x89;

    /**
     * Script "Tangut".
     */
    public static final int TANGUT = 0x8A;

    /**
     * Script "Masaram_Gondi".
     */
    public static final int MASARAM_GONDI = 0x8B;

    /**
     * Script "Nushu".
     */
    public static final int NUSHU = 0x8C;

    /**
     * Script "Soyombo".
     */
    public static final int SOYOMBO = 0x8D;

    /**
     * Script "Zanabazar_Square".
     */
    public static final int ZANABAZAR_SQUARE = 0x8E;

    /**
     * Script "Dogra".
     */
    public static final int DOGRA = 0x8F;

    /**
     * Script "Gunjala_Gondi".
     */
    public static final int GUNJALA_GONDI = 0x90;

    /**
     * Script "Makasar".
     */
    public static final int MAKASAR = 0x91;

    /**
     * Script "Medefaidrin".
     */
    public static final int MEDEFAIDRIN = 0x92;

    /**
     * Script "Hanifi_Rohingya".
     */
    public static final int HANIFI_ROHINGYA = 0x93;

    /**
     * Script "Sogdian".
     */
    public static final int SOGDIAN = 0x94;

    /**
     * Script "Old_Sogdian".
     */
    public static final int OLD_SOGDIAN = 0x95;

    private Script() {
    }

    /**
     * Returns the OpenType tag of specified script as an integer in big endian byte order. The
     * association between Unicode Script property and OpenType script tags is taken from the
     * specification:
     * <a href="https://docs.microsoft.com/en-us/typography/opentype/spec/scripttags">
     *     https://docs.microsoft.com/en-us/typography/opentype/spec/scripttags
     * </a>.
     *
     * If more than one tag is associated with a script, then the latest one is returned. For
     * example, Devanagari script has two tags, `deva` and `dev2`. So in this case, `dev2` will be
     * returned.
     *
     * If no tag is associated with a script, then `DFLT` is returned.
     *
     * @param script
     *      The script whose OpenType tag is returned.
     * @return
     *      The OpenType tag of specified script as an integer in big endian byte order.
     */
    public static int getOpenTypeTag(@Value int script) {
        return Unicode.getScriptOpenTypeTag(script);
    }
}
