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

package com.mta.tehreer.unicode

import androidx.annotation.IntDef

/**
 * Represents the script of a character in Unicode specification. The constants correspond to the
 * script property values defined in
 * <a href="https://www.unicode.org/reports/tr24/#Data_File_PVA">
 *     Unicode Standard Annex #24: PropertyValueAliases.txt
 * </a>.
 */
object Script {
    @IntDef(
        value = [
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
            ELYMAIC,
            NYIAKENG_PUACHUE_HMONG,
            NANDINAGARI,
            WANCHO,
            CHORASMIAN,
            DIVES_AKURU,
            KHITAN_SMALL_SCRIPT,
            YEZIDI,
            CYPRO_MINOAN,
            OLD_UYGHUR,
            TANGSA,
            TOTO,
            VITHKUQI,
        ]
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class Value

    /**
     * Script "Inherited".
     */
    const val INHERITED = 0x01

    /**
     * Script "Common".
     */
    const val COMMON = 0x02

    /**
     * Script "Unknown".
     */
    const val UNKNOWN = 0x03

    /**
     * Script "Arabic".
     */
    const val ARABIC = 0x04

    /**
     * Script "Armenian".
     */
    const val ARMENIAN = 0x05

    /**
     * Script "Bengali".
     */
    const val BENGALI = 0x06

    /**
     * Script "Bopomofo".
     */
    const val BOPOMOFO = 0x07

    /**
     * Script "Cyrillic".
     */
    const val CYRILLIC = 0x08

    /**
     * Script "Devanagari".
     */
    const val DEVANAGARI = 0x09

    /**
     * Script "Georgian".
     */
    const val GEORGIAN = 0x0A

    /**
     * Script "Greek".
     */
    const val GREEK = 0x0B

    /**
     * Script "Gujarati".
     */
    const val GUJARATI = 0x0C

    /**
     * Script "Gurmukhi".
     */
    const val GURMUKHI = 0x0D

    /**
     * Script "Hangul".
     */
    const val HANGUL = 0x0E

    /**
     * Script "Han".
     */
    const val HAN = 0x0F

    /**
     * Script "Hebrew".
     */
    const val HEBREW = 0x10

    /**
     * Script "Hiragana".
     */
    const val HIRAGANA = 0x11

    /**
     * Script "Katakana".
     */
    const val KATAKANA = 0x12

    /**
     * Script "Kannada".
     */
    const val KANNADA = 0x13

    /**
     * Script "Lao".
     */
    const val LAO = 0x14

    /**
     * Script "Latin".
     */
    const val LATIN = 0x15

    /**
     * Script "Malayalam".
     */
    const val MALAYALAM = 0x16

    /**
     * Script "Oriya".
     */
    const val ORIYA = 0x17

    /**
     * Script "Tamil".
     */
    const val TAMIL = 0x18

    /**
     * Script "Telugu".
     */
    const val TELUGU = 0x19

    /**
     * Script "Thai".
     */
    const val THAI = 0x1A

    /**
     * Script "Tibetan".
     */
    const val TIBETAN = 0x1B

    /**
     * Script "Braille".
     */
    const val BRAILLE = 0x1C

    /**
     * Script "Canadian_Aboriginal".
     */
    const val CANADIAN_ABORIGINAL = 0x1D

    /**
     * Script "Cherokee".
     */
    const val CHEROKEE = 0x1E

    /**
     * Script "Ethiopic".
     */
    const val ETHIOPIC = 0x1F

    /**
     * Script "Khmer".
     */
    const val KHMER = 0x20

    /**
     * Script "Mongolian".
     */
    const val MONGOLIAN = 0x21

    /**
     * Script "Myanmar".
     */
    const val MYANMAR = 0x22

    /**
     * Script "Ogham".
     */
    const val OGHAM = 0x23

    /**
     * Script "Runic".
     */
    const val RUNIC = 0x24

    /**
     * Script "Sinhala".
     */
    const val SINHALA = 0x25

    /**
     * Script "Syriac".
     */
    const val SYRIAC = 0x26

    /**
     * Script "Thaana".
     */
    const val THAANA = 0x27

    /**
     * Script "Yi".
     */
    const val YI = 0x28

    /**
     * Script "Deseret".
     */
    const val DESERET = 0x29

    /**
     * Script "Gothic".
     */
    const val GOTHIC = 0x2A

    /**
     * Script "Old_Italic".
     */
    const val OLD_ITALIC = 0x2B

    /**
     * Script "Buhid".
     */
    const val BUHID = 0x2C

    /**
     * Script "Hanunoo".
     */
    const val HANUNOO = 0x2D

    /**
     * Script "Tagbanwa".
     */
    const val TAGBANWA = 0x2E

    /**
     * Script "Tagalog".
     */
    const val TAGALOG = 0x2F

    /**
     * Script "Cypriot".
     */
    const val CYPRIOT = 0x30

    /**
     * Script "Limbu".
     */
    const val LIMBU = 0x31

    /**
     * Script "Linear_B".
     */
    const val LINEAR_B = 0x32

    /**
     * Script "Osmanya".
     */
    const val OSMANYA = 0x33

    /**
     * Script "Shavian".
     */
    const val SHAVIAN = 0x34

    /**
     * Script "Tai_Le".
     */
    const val TAI_LE = 0x35

    /**
     * Script "Ugaritic".
     */
    const val UGARITIC = 0x36

    /**
     * Script "Buginese".
     */
    const val BUGINESE = 0x37

    /**
     * Script "Coptic".
     */
    const val COPTIC = 0x38

    /**
     * Script "Glagolitic".
     */
    const val GLAGOLITIC = 0x39

    /**
     * Script "Kharoshthi".
     */
    const val KHAROSHTHI = 0x3A

    /**
     * Script "Syloti_Nagri".
     */
    const val SYLOTI_NAGRI = 0x3B

    /**
     * Script "New_Tai_Lue".
     */
    const val NEW_TAI_LUE = 0x3C

    /**
     * Script "Tifinagh".
     */
    const val TIFINAGH = 0x3D

    /**
     * Script "Old_Persian".
     */
    const val OLD_PERSIAN = 0x3E

    /**
     * Script "Balinese".
     */
    const val BALINESE = 0x3F

    /**
     * Script "Nko".
     */
    const val NKO = 0x40

    /**
     * Script "Phags_Pa".
     */
    const val PHAGS_PA = 0x41

    /**
     * Script "Phoenician".
     */
    const val PHOENICIAN = 0x42

    /**
     * Script "Cuneiform".
     */
    const val CUNEIFORM = 0x43

    /**
     * Script "Carian".
     */
    const val CARIAN = 0x44

    /**
     * Script "Cham".
     */
    const val CHAM = 0x45

    /**
     * Script "Kayah_Li".
     */
    const val KAYAH_LI = 0x46

    /**
     * Script "Lepcha".
     */
    const val LEPCHA = 0x47

    /**
     * Script "Lycian".
     */
    const val LYCIAN = 0x48

    /**
     * Script "Lydian".
     */
    const val LYDIAN = 0x49

    /**
     * Script "Ol_Chiki".
     */
    const val OL_CHIKI = 0x4A

    /**
     * Script "Rejang".
     */
    const val REJANG = 0x4B

    /**
     * Script "Saurashtra".
     */
    const val SAURASHTRA = 0x4C

    /**
     * Script "Sundanese".
     */
    const val SUNDANESE = 0x4D

    /**
     * Script "Vai".
     */
    const val VAI = 0x4E

    /**
     * Script "Imperial_Aramaic".
     */
    const val IMPERIAL_ARAMAIC = 0x4F

    /**
     * Script "Avestan".
     */
    const val AVESTAN = 0x50

    /**
     * Script "Bamum".
     */
    const val BAMUM = 0x51

    /**
     * Script "Egyptian_Hieroglyphs".
     */
    const val EGYPTIAN_HIEROGLYPHS = 0x52

    /**
     * Script "Javanese".
     */
    const val JAVANESE = 0x53

    /**
     * Script "Kaithi".
     */
    const val KAITHI = 0x54

    /**
     * Script "Tai_Tham".
     */
    const val TAI_THAM = 0x55

    /**
     * Script "Lisu".
     */
    const val LISU = 0x56

    /**
     * Script "Meetei_Mayek".
     */
    const val MEETEI_MAYEK = 0x57

    /**
     * Script "Old_Turkic".
     */
    const val OLD_TURKIC = 0x58

    /**
     * Script "Inscriptional_Pahlavi".
     */
    const val INSCRIPTIONAL_PAHLAVI = 0x59

    /**
     * Script "Inscriptional_Parthian".
     */
    const val INSCRIPTIONAL_PARTHIAN = 0x5A

    /**
     * Script "Samaritan".
     */
    const val SAMARITAN = 0x5B

    /**
     * Script "Old_South_Arabian".
     */
    const val OLD_SOUTH_ARABIAN = 0x5C

    /**
     * Script "Tai_Viet".
     */
    const val TAI_VIET = 0x5D

    /**
     * Script "Batak".
     */
    const val BATAK = 0x5E

    /**
     * Script "Brahmi".
     */
    const val BRAHMI = 0x5F

    /**
     * Script "Mandaic".
     */
    const val MANDAIC = 0x60

    /**
     * Script "Chakma".
     */
    const val CHAKMA = 0x61

    /**
     * Script "Meroitic_Cursive".
     */
    const val MEROITIC_CURSIVE = 0x62

    /**
     * Script "Meroitic_Hieroglyphs".
     */
    const val MEROITIC_HIEROGLYPHS = 0x63

    /**
     * Script "Miao".
     */
    const val MIAO = 0x64

    /**
     * Script "Sharada".
     */
    const val SHARADA = 0x65

    /**
     * Script "Sora_Sompeng".
     */
    const val SORA_SOMPENG = 0x66

    /**
     * Script "Takri".
     */
    const val TAKRI = 0x67

    /**
     * Script "Caucasian_Albanian".
     */
    const val CAUCASIAN_ALBANIAN = 0x68

    /**
     * Script "Bassa_Vah".
     */
    const val BASSA_VAH = 0x69

    /**
     * Script "Duployan".
     */
    const val DUPLOYAN = 0x6A

    /**
     * Script "Elbasan".
     */
    const val ELBASAN = 0x6B

    /**
     * Script "Grantha".
     */
    const val GRANTHA = 0x6C

    /**
     * Script "Pahawh_Hmong".
     */
    const val PAHAWH_HMONG = 0x6D

    /**
     * Script "Khojki".
     */
    const val KHOJKI = 0x6E

    /**
     * Script "Linear_A".
     */
    const val LINEAR_A = 0x6F

    /**
     * Script "Mahajani".
     */
    const val MAHAJANI = 0x70

    /**
     * Script "Manichaean".
     */
    const val MANICHAEAN = 0x71

    /**
     * Script "Mende_Kikakui".
     */
    const val MENDE_KIKAKUI = 0x72

    /**
     * Script "Modi".
     */
    const val MODI = 0x73

    /**
     * Script "Mro".
     */
    const val MRO = 0x74

    /**
     * Script "Old_North_Arabian".
     */
    const val OLD_NORTH_ARABIAN = 0x75

    /**
     * Script "Nabataean".
     */
    const val NABATAEAN = 0x76

    /**
     * Script "Palmyrene".
     */
    const val PALMYRENE = 0x77

    /**
     * Script "Pau_Cin_Hau".
     */
    const val PAU_CIN_HAU = 0x78

    /**
     * Script "Old_Permic".
     */
    const val OLD_PERMIC = 0x79

    /**
     * Script "Psalter_Pahlavi".
     */
    const val PSALTER_PAHLAVI = 0x7A

    /**
     * Script "Siddham".
     */
    const val SIDDHAM = 0x7B

    /**
     * Script "Khudawadi".
     */
    const val KHUDAWADI = 0x7C

    /**
     * Script "Tirhuta".
     */
    const val TIRHUTA = 0x7D

    /**
     * Script "Warang_Citi".
     */
    const val WARANG_CITI = 0x7E

    /**
     * Script "Ahom".
     */
    const val AHOM = 0x7F

    /**
     * Script "Hatran".
     */
    const val HATRAN = 0x80

    /**
     * Script "Anatolian_Hieroglyphs".
     */
    const val ANATOLIAN_HIEROGLYPHS = 0x81

    /**
     * Script "Old_Hungarian".
     */
    const val OLD_HUNGARIAN = 0x82

    /**
     * Script "Multani".
     */
    const val MULTANI = 0x83

    /**
     * Script "SignWriting".
     */
    const val SIGNWRITING = 0x84

    /**
     * Script "Adlam".
     */
    const val ADLAM = 0x85

    /**
     * Script "Bhaiksuki".
     */
    const val BHAIKSUKI = 0x86

    /**
     * Script "Marchen".
     */
    const val MARCHEN = 0x87

    /**
     * Script "Newa".
     */
    const val NEWA = 0x88

    /**
     * Script "Osage".
     */
    const val OSAGE = 0x89

    /**
     * Script "Tangut".
     */
    const val TANGUT = 0x8A

    /**
     * Script "Masaram_Gondi".
     */
    const val MASARAM_GONDI = 0x8B

    /**
     * Script "Nushu".
     */
    const val NUSHU = 0x8C

    /**
     * Script "Soyombo".
     */
    const val SOYOMBO = 0x8D

    /**
     * Script "Zanabazar_Square".
     */
    const val ZANABAZAR_SQUARE = 0x8E

    /**
     * Script "Dogra".
     */
    const val DOGRA = 0x8F

    /**
     * Script "Gunjala_Gondi".
     */
    const val GUNJALA_GONDI = 0x90

    /**
     * Script "Makasar".
     */
    const val MAKASAR = 0x91

    /**
     * Script "Medefaidrin".
     */
    const val MEDEFAIDRIN = 0x92

    /**
     * Script "Hanifi_Rohingya".
     */
    const val HANIFI_ROHINGYA = 0x93

    /**
     * Script "Sogdian".
     */
    const val SOGDIAN = 0x94

    /**
     * Script "Old_Sogdian".
     */
    const val OLD_SOGDIAN = 0x95

    /**
     * Script "Elymaic".
     */
    const val ELYMAIC = 0x96

    /**
     * Script "Nyiakeng_Puachue_Hmong".
     */
    const val NYIAKENG_PUACHUE_HMONG = 0x97

    /**
     * Script "Nandinagari".
     */
    const val NANDINAGARI = 0x98

    /**
     * Script "Wancho".
     */
    const val WANCHO = 0x99

    /**
     * Script "Chorasmian".
     */
    const val CHORASMIAN = 0x9A

    /**
     * Script "Dives_Akuru".
     */
    const val DIVES_AKURU = 0x9B

    /**
     * Script "Khitan_Small_Script".
     */
    const val KHITAN_SMALL_SCRIPT = 0x9C

    /**
     * Script "Yezidi".
     */
    const val YEZIDI = 0x9D

    /**
     * Script "Cypro_Minoan".
     */
    const val CYPRO_MINOAN = 0x9E

    /**
     * Script "Old_Uyghur".
     */
    const val OLD_UYGHUR = 0x9F

    /**
     * Script "Tangsa".
     */
    const val TANGSA = 0xA0

    /**
     * Script "Toto".
     */
    const val TOTO = 0xA1

    /**
     * Script "Vithkuqi".
     */
    const val VITHKUQI = 0xA2

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
    fun getOpenTypeTag(@Value script: Int): Int {
        return Unicode.getScriptOpenTypeTag(script)
    }
}
