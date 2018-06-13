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

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

public class ScriptTest {

    @Test
    public void testValueOf() {
        assertThat(Script.valueOf((byte) 0x00), is(nullValue()));
        assertThat(Script.valueOf((byte) 0x01), is(Script.INHERITED));
        assertThat(Script.valueOf((byte) 0x02), is(Script.COMMON));
        assertThat(Script.valueOf((byte) 0x03), is(Script.UNKNOWN));
        assertThat(Script.valueOf((byte) 0x04), is(Script.ARABIC));
        assertThat(Script.valueOf((byte) 0x05), is(Script.ARMENIAN));
        assertThat(Script.valueOf((byte) 0x06), is(Script.BENGALI));
        assertThat(Script.valueOf((byte) 0x07), is(Script.BOPOMOFO));
        assertThat(Script.valueOf((byte) 0x08), is(Script.CYRILLIC));
        assertThat(Script.valueOf((byte) 0x09), is(Script.DEVANAGARI));
        assertThat(Script.valueOf((byte) 0x0A), is(Script.GEORGIAN));
        assertThat(Script.valueOf((byte) 0x0B), is(Script.GREEK));
        assertThat(Script.valueOf((byte) 0x0C), is(Script.GUJARATI));
        assertThat(Script.valueOf((byte) 0x0D), is(Script.GURMUKHI));
        assertThat(Script.valueOf((byte) 0x0E), is(Script.HANGUL));
        assertThat(Script.valueOf((byte) 0x0F), is(Script.HAN));
        assertThat(Script.valueOf((byte) 0x10), is(Script.HEBREW));
        assertThat(Script.valueOf((byte) 0x11), is(Script.HIRAGANA));
        assertThat(Script.valueOf((byte) 0x12), is(Script.KATAKANA));
        assertThat(Script.valueOf((byte) 0x13), is(Script.KANNADA));
        assertThat(Script.valueOf((byte) 0x14), is(Script.LAO));
        assertThat(Script.valueOf((byte) 0x15), is(Script.LATIN));
        assertThat(Script.valueOf((byte) 0x16), is(Script.MALAYALAM));
        assertThat(Script.valueOf((byte) 0x17), is(Script.ORIYA));
        assertThat(Script.valueOf((byte) 0x18), is(Script.TAMIL));
        assertThat(Script.valueOf((byte) 0x19), is(Script.TELUGU));
        assertThat(Script.valueOf((byte) 0x1A), is(Script.THAI));
        assertThat(Script.valueOf((byte) 0x1B), is(Script.TIBETAN));
        assertThat(Script.valueOf((byte) 0x1C), is(Script.BRAILLE));
        assertThat(Script.valueOf((byte) 0x1D), is(Script.CANADIAN_ABORIGINAL));
        assertThat(Script.valueOf((byte) 0x1E), is(Script.CHEROKEE));
        assertThat(Script.valueOf((byte) 0x1F), is(Script.ETHIOPIC));
        assertThat(Script.valueOf((byte) 0x20), is(Script.KHMER));
        assertThat(Script.valueOf((byte) 0x21), is(Script.MONGOLIAN));
        assertThat(Script.valueOf((byte) 0x22), is(Script.MYANMAR));
        assertThat(Script.valueOf((byte) 0x23), is(Script.OGHAM));
        assertThat(Script.valueOf((byte) 0x24), is(Script.RUNIC));
        assertThat(Script.valueOf((byte) 0x25), is(Script.SINHALA));
        assertThat(Script.valueOf((byte) 0x26), is(Script.SYRIAC));
        assertThat(Script.valueOf((byte) 0x27), is(Script.THAANA));
        assertThat(Script.valueOf((byte) 0x28), is(Script.YI));
        assertThat(Script.valueOf((byte) 0x29), is(Script.DESERET));
        assertThat(Script.valueOf((byte) 0x2A), is(Script.GOTHIC));
        assertThat(Script.valueOf((byte) 0x2B), is(Script.OLD_ITALIC));
        assertThat(Script.valueOf((byte) 0x2C), is(Script.BUHID));
        assertThat(Script.valueOf((byte) 0x2D), is(Script.HANUNOO));
        assertThat(Script.valueOf((byte) 0x2E), is(Script.TAGBANWA));
        assertThat(Script.valueOf((byte) 0x2F), is(Script.TAGALOG));
        assertThat(Script.valueOf((byte) 0x30), is(Script.CYPRIOT));
        assertThat(Script.valueOf((byte) 0x31), is(Script.LIMBU));
        assertThat(Script.valueOf((byte) 0x32), is(Script.LINEAR_B));
        assertThat(Script.valueOf((byte) 0x33), is(Script.OSMANYA));
        assertThat(Script.valueOf((byte) 0x34), is(Script.SHAVIAN));
        assertThat(Script.valueOf((byte) 0x35), is(Script.TAI_LE));
        assertThat(Script.valueOf((byte) 0x36), is(Script.UGARITIC));
        assertThat(Script.valueOf((byte) 0x37), is(Script.BUGINESE));
        assertThat(Script.valueOf((byte) 0x38), is(Script.COPTIC));
        assertThat(Script.valueOf((byte) 0x39), is(Script.GLAGOLITIC));
        assertThat(Script.valueOf((byte) 0x3A), is(Script.KHAROSHTHI));
        assertThat(Script.valueOf((byte) 0x3B), is(Script.SYLOTI_NAGRI));
        assertThat(Script.valueOf((byte) 0x3C), is(Script.NEW_TAI_LUE));
        assertThat(Script.valueOf((byte) 0x3D), is(Script.TIFINAGH));
        assertThat(Script.valueOf((byte) 0x3E), is(Script.OLD_PERSIAN));
        assertThat(Script.valueOf((byte) 0x3F), is(Script.BALINESE));
        assertThat(Script.valueOf((byte) 0x40), is(Script.NKO));
        assertThat(Script.valueOf((byte) 0x41), is(Script.PHAGS_PA));
        assertThat(Script.valueOf((byte) 0x42), is(Script.PHOENICIAN));
        assertThat(Script.valueOf((byte) 0x43), is(Script.CUNEIFORM));
        assertThat(Script.valueOf((byte) 0x44), is(Script.CARIAN));
        assertThat(Script.valueOf((byte) 0x45), is(Script.CHAM));
        assertThat(Script.valueOf((byte) 0x46), is(Script.KAYAH_LI));
        assertThat(Script.valueOf((byte) 0x47), is(Script.LEPCHA));
        assertThat(Script.valueOf((byte) 0x48), is(Script.LYCIAN));
        assertThat(Script.valueOf((byte) 0x49), is(Script.LYDIAN));
        assertThat(Script.valueOf((byte) 0x4A), is(Script.OL_CHIKI));
        assertThat(Script.valueOf((byte) 0x4B), is(Script.REJANG));
        assertThat(Script.valueOf((byte) 0x4C), is(Script.SAURASHTRA));
        assertThat(Script.valueOf((byte) 0x4D), is(Script.SUNDANESE));
        assertThat(Script.valueOf((byte) 0x4E), is(Script.VAI));
        assertThat(Script.valueOf((byte) 0x4F), is(Script.IMPERIAL_ARAMAIC));
        assertThat(Script.valueOf((byte) 0x50), is(Script.AVESTAN));
        assertThat(Script.valueOf((byte) 0x51), is(Script.BAMUM));
        assertThat(Script.valueOf((byte) 0x52), is(Script.EGYPTIAN_HIEROGLYPHS));
        assertThat(Script.valueOf((byte) 0x53), is(Script.JAVANESE));
        assertThat(Script.valueOf((byte) 0x54), is(Script.KAITHI));
        assertThat(Script.valueOf((byte) 0x55), is(Script.TAI_THAM));
        assertThat(Script.valueOf((byte) 0x56), is(Script.LISU));
        assertThat(Script.valueOf((byte) 0x57), is(Script.MEETEI_MAYEK));
        assertThat(Script.valueOf((byte) 0x58), is(Script.OLD_TURKIC));
        assertThat(Script.valueOf((byte) 0x59), is(Script.INSCRIPTIONAL_PAHLAVI));
        assertThat(Script.valueOf((byte) 0x5A), is(Script.INSCRIPTIONAL_PARTHIAN));
        assertThat(Script.valueOf((byte) 0x5B), is(Script.SAMARITAN));
        assertThat(Script.valueOf((byte) 0x5C), is(Script.OLD_SOUTH_ARABIAN));
        assertThat(Script.valueOf((byte) 0x5D), is(Script.TAI_VIET));
        assertThat(Script.valueOf((byte) 0x5E), is(Script.BATAK));
        assertThat(Script.valueOf((byte) 0x5F), is(Script.BRAHMI));
        assertThat(Script.valueOf((byte) 0x60), is(Script.MANDAIC));
        assertThat(Script.valueOf((byte) 0x61), is(Script.CHAKMA));
        assertThat(Script.valueOf((byte) 0x62), is(Script.MEROITIC_CURSIVE));
        assertThat(Script.valueOf((byte) 0x63), is(Script.MEROITIC_HIEROGLYPHS));
        assertThat(Script.valueOf((byte) 0x64), is(Script.MIAO));
        assertThat(Script.valueOf((byte) 0x65), is(Script.SHARADA));
        assertThat(Script.valueOf((byte) 0x66), is(Script.SORA_SOMPENG));
        assertThat(Script.valueOf((byte) 0x67), is(Script.TAKRI));
        assertThat(Script.valueOf((byte) 0x68), is(Script.CAUCASIAN_ALBANIAN));
        assertThat(Script.valueOf((byte) 0x69), is(Script.BASSA_VAH));
        assertThat(Script.valueOf((byte) 0x6A), is(Script.DUPLOYAN));
        assertThat(Script.valueOf((byte) 0x6B), is(Script.ELBASAN));
        assertThat(Script.valueOf((byte) 0x6C), is(Script.GRANTHA));
        assertThat(Script.valueOf((byte) 0x6D), is(Script.PAHAWH_HMONG));
        assertThat(Script.valueOf((byte) 0x6E), is(Script.KHOJKI));
        assertThat(Script.valueOf((byte) 0x6F), is(Script.LINEAR_A));
        assertThat(Script.valueOf((byte) 0x70), is(Script.MAHAJANI));
        assertThat(Script.valueOf((byte) 0x71), is(Script.MANICHAEAN));
        assertThat(Script.valueOf((byte) 0x72), is(Script.MENDE_KIKAKUI));
        assertThat(Script.valueOf((byte) 0x73), is(Script.MODI));
        assertThat(Script.valueOf((byte) 0x74), is(Script.MRO));
        assertThat(Script.valueOf((byte) 0x75), is(Script.OLD_NORTH_ARABIAN));
        assertThat(Script.valueOf((byte) 0x76), is(Script.NABATAEAN));
        assertThat(Script.valueOf((byte) 0x77), is(Script.PALMYRENE));
        assertThat(Script.valueOf((byte) 0x78), is(Script.PAU_CIN_HAU));
        assertThat(Script.valueOf((byte) 0x79), is(Script.OLD_PERMIC));
        assertThat(Script.valueOf((byte) 0x7A), is(Script.PSALTER_PAHLAVI));
        assertThat(Script.valueOf((byte) 0x7B), is(Script.SIDDHAM));
        assertThat(Script.valueOf((byte) 0x7C), is(Script.KHUDAWADI));
        assertThat(Script.valueOf((byte) 0x7D), is(Script.TIRHUTA));
        assertThat(Script.valueOf((byte) 0x7E), is(Script.WARANG_CITI));
        assertThat(Script.valueOf((byte) 0x7F), is(Script.AHOM));
        assertThat(Script.valueOf((byte) 0x80), is(Script.HATRAN));
        assertThat(Script.valueOf((byte) 0x81), is(Script.ANATOLIAN_HIEROGLYPHS));
        assertThat(Script.valueOf((byte) 0x82), is(Script.OLD_HUNGARIAN));
        assertThat(Script.valueOf((byte) 0x83), is(Script.MULTANI));
        assertThat(Script.valueOf((byte) 0x84), is(Script.SIGNWRITING));
        assertThat(Script.valueOf((byte) 0x85), is(Script.ADLAM));
        assertThat(Script.valueOf((byte) 0x86), is(Script.BHAIKSUKI));
        assertThat(Script.valueOf((byte) 0x87), is(Script.MARCHEN));
        assertThat(Script.valueOf((byte) 0x88), is(Script.NEWA));
        assertThat(Script.valueOf((byte) 0x89), is(Script.OSAGE));
        assertThat(Script.valueOf((byte) 0x8A), is(Script.TANGUT));
        assertThat(Script.valueOf((byte) 0x8B), is(Script.MASARAM_GONDI));
        assertThat(Script.valueOf((byte) 0x8C), is(Script.NUSHU));
        assertThat(Script.valueOf((byte) 0x8D), is(Script.SOYOMBO));
        assertThat(Script.valueOf((byte) 0x8E), is(Script.ZANABAZAR_SQUARE));
    }
}
