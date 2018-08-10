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

import static org.junit.Assert.assertEquals;

public class ScriptTest {
    @Test
    public void testValues() {
        assertEquals(Script.INHERITED, 0x01);
        assertEquals(Script.COMMON, 0x02);
        assertEquals(Script.UNKNOWN, 0x03);
        assertEquals(Script.ARABIC, 0x04);
        assertEquals(Script.ARMENIAN, 0x05);
        assertEquals(Script.BENGALI, 0x06);
        assertEquals(Script.BOPOMOFO, 0x07);
        assertEquals(Script.CYRILLIC, 0x08);
        assertEquals(Script.DEVANAGARI, 0x09);
        assertEquals(Script.GEORGIAN, 0x0A);
        assertEquals(Script.GREEK, 0x0B);
        assertEquals(Script.GUJARATI, 0x0C);
        assertEquals(Script.GURMUKHI, 0x0D);
        assertEquals(Script.HANGUL, 0x0E);
        assertEquals(Script.HAN, 0x0F);
        assertEquals(Script.HEBREW, 0x10);
        assertEquals(Script.HIRAGANA, 0x11);
        assertEquals(Script.KATAKANA, 0x12);
        assertEquals(Script.KANNADA, 0x13);
        assertEquals(Script.LAO, 0x14);
        assertEquals(Script.LATIN, 0x15);
        assertEquals(Script.MALAYALAM, 0x16);
        assertEquals(Script.ORIYA, 0x17);
        assertEquals(Script.TAMIL, 0x18);
        assertEquals(Script.TELUGU, 0x19);
        assertEquals(Script.THAI, 0x1A);
        assertEquals(Script.TIBETAN, 0x1B);
        assertEquals(Script.BRAILLE, 0x1C);
        assertEquals(Script.CANADIAN_ABORIGINAL, 0x1D);
        assertEquals(Script.CHEROKEE, 0x1E);
        assertEquals(Script.ETHIOPIC, 0x1F);
        assertEquals(Script.KHMER, 0x20);
        assertEquals(Script.MONGOLIAN, 0x21);
        assertEquals(Script.MYANMAR, 0x22);
        assertEquals(Script.OGHAM, 0x23);
        assertEquals(Script.RUNIC, 0x24);
        assertEquals(Script.SINHALA, 0x25);
        assertEquals(Script.SYRIAC, 0x26);
        assertEquals(Script.THAANA, 0x27);
        assertEquals(Script.YI, 0x28);
        assertEquals(Script.DESERET, 0x29);
        assertEquals(Script.GOTHIC, 0x2A);
        assertEquals(Script.OLD_ITALIC, 0x2B);
        assertEquals(Script.BUHID, 0x2C);
        assertEquals(Script.HANUNOO, 0x2D);
        assertEquals(Script.TAGBANWA, 0x2E);
        assertEquals(Script.TAGALOG, 0x2F);
        assertEquals(Script.CYPRIOT, 0x30);
        assertEquals(Script.LIMBU, 0x31);
        assertEquals(Script.LINEAR_B, 0x32);
        assertEquals(Script.OSMANYA, 0x33);
        assertEquals(Script.SHAVIAN, 0x34);
        assertEquals(Script.TAI_LE, 0x35);
        assertEquals(Script.UGARITIC, 0x36);
        assertEquals(Script.BUGINESE, 0x37);
        assertEquals(Script.COPTIC, 0x38);
        assertEquals(Script.GLAGOLITIC, 0x39);
        assertEquals(Script.KHAROSHTHI, 0x3A);
        assertEquals(Script.SYLOTI_NAGRI, 0x3B);
        assertEquals(Script.NEW_TAI_LUE, 0x3C);
        assertEquals(Script.TIFINAGH, 0x3D);
        assertEquals(Script.OLD_PERSIAN, 0x3E);
        assertEquals(Script.BALINESE, 0x3F);
        assertEquals(Script.NKO, 0x40);
        assertEquals(Script.PHAGS_PA, 0x41);
        assertEquals(Script.PHOENICIAN, 0x42);
        assertEquals(Script.CUNEIFORM, 0x43);
        assertEquals(Script.CARIAN, 0x44);
        assertEquals(Script.CHAM, 0x45);
        assertEquals(Script.KAYAH_LI, 0x46);
        assertEquals(Script.LEPCHA, 0x47);
        assertEquals(Script.LYCIAN, 0x48);
        assertEquals(Script.LYDIAN, 0x49);
        assertEquals(Script.OL_CHIKI, 0x4A);
        assertEquals(Script.REJANG, 0x4B);
        assertEquals(Script.SAURASHTRA, 0x4C);
        assertEquals(Script.SUNDANESE, 0x4D);
        assertEquals(Script.VAI, 0x4E);
        assertEquals(Script.IMPERIAL_ARAMAIC, 0x4F);
        assertEquals(Script.AVESTAN, 0x50);
        assertEquals(Script.BAMUM, 0x51);
        assertEquals(Script.EGYPTIAN_HIEROGLYPHS, 0x52);
        assertEquals(Script.JAVANESE, 0x53);
        assertEquals(Script.KAITHI, 0x54);
        assertEquals(Script.TAI_THAM, 0x55);
        assertEquals(Script.LISU, 0x56);
        assertEquals(Script.MEETEI_MAYEK, 0x57);
        assertEquals(Script.OLD_TURKIC, 0x58);
        assertEquals(Script.INSCRIPTIONAL_PAHLAVI, 0x59);
        assertEquals(Script.INSCRIPTIONAL_PARTHIAN, 0x5A);
        assertEquals(Script.SAMARITAN, 0x5B);
        assertEquals(Script.OLD_SOUTH_ARABIAN, 0x5C);
        assertEquals(Script.TAI_VIET, 0x5D);
        assertEquals(Script.BATAK, 0x5E);
        assertEquals(Script.BRAHMI, 0x5F);
        assertEquals(Script.MANDAIC, 0x60);
        assertEquals(Script.CHAKMA, 0x61);
        assertEquals(Script.MEROITIC_CURSIVE, 0x62);
        assertEquals(Script.MEROITIC_HIEROGLYPHS, 0x63);
        assertEquals(Script.MIAO, 0x64);
        assertEquals(Script.SHARADA, 0x65);
        assertEquals(Script.SORA_SOMPENG, 0x66);
        assertEquals(Script.TAKRI, 0x67);
        assertEquals(Script.CAUCASIAN_ALBANIAN, 0x68);
        assertEquals(Script.BASSA_VAH, 0x69);
        assertEquals(Script.DUPLOYAN, 0x6A);
        assertEquals(Script.ELBASAN, 0x6B);
        assertEquals(Script.GRANTHA, 0x6C);
        assertEquals(Script.PAHAWH_HMONG, 0x6D);
        assertEquals(Script.KHOJKI, 0x6E);
        assertEquals(Script.LINEAR_A, 0x6F);
        assertEquals(Script.MAHAJANI, 0x70);
        assertEquals(Script.MANICHAEAN, 0x71);
        assertEquals(Script.MENDE_KIKAKUI, 0x72);
        assertEquals(Script.MODI, 0x73);
        assertEquals(Script.MRO, 0x74);
        assertEquals(Script.OLD_NORTH_ARABIAN, 0x75);
        assertEquals(Script.NABATAEAN, 0x76);
        assertEquals(Script.PALMYRENE, 0x77);
        assertEquals(Script.PAU_CIN_HAU, 0x78);
        assertEquals(Script.OLD_PERMIC, 0x79);
        assertEquals(Script.PSALTER_PAHLAVI, 0x7A);
        assertEquals(Script.SIDDHAM, 0x7B);
        assertEquals(Script.KHUDAWADI, 0x7C);
        assertEquals(Script.TIRHUTA, 0x7D);
        assertEquals(Script.WARANG_CITI, 0x7E);
        assertEquals(Script.AHOM, 0x7F);
        assertEquals(Script.HATRAN, 0x80);
        assertEquals(Script.ANATOLIAN_HIEROGLYPHS, 0x81);
        assertEquals(Script.OLD_HUNGARIAN, 0x82);
        assertEquals(Script.MULTANI, 0x83);
        assertEquals(Script.SIGNWRITING, 0x84);
        assertEquals(Script.ADLAM, 0x85);
        assertEquals(Script.BHAIKSUKI, 0x86);
        assertEquals(Script.MARCHEN, 0x87);
        assertEquals(Script.NEWA, 0x88);
        assertEquals(Script.OSAGE, 0x89);
        assertEquals(Script.TANGUT, 0x8A);
        assertEquals(Script.MASARAM_GONDI, 0x8B);
        assertEquals(Script.NUSHU, 0x8C);
        assertEquals(Script.SOYOMBO, 0x8D);
        assertEquals(Script.ZANABAZAR_SQUARE, 0x8E);
        assertEquals(Script.DOGRA, 0x8F);
        assertEquals(Script.GUNJALA_GONDI, 0x90);
        assertEquals(Script.MAKASAR, 0x91);
        assertEquals(Script.MEDEFAIDRIN, 0x92);
        assertEquals(Script.HANIFI_ROHINGYA, 0x93);
        assertEquals(Script.SOGDIAN, 0x94);
        assertEquals(Script.OLD_SOGDIAN, 0x95);
    }
}
