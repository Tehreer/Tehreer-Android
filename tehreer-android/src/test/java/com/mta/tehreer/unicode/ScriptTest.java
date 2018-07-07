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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ScriptTest {

    @Test
    public void testValues() {
        assertThat(Script.INHERITED, equalTo(0x01));
        assertThat(Script.COMMON, equalTo(0x02));
        assertThat(Script.UNKNOWN, equalTo(0x03));
        assertThat(Script.ARABIC, equalTo(0x04));
        assertThat(Script.ARMENIAN, equalTo(0x05));
        assertThat(Script.BENGALI, equalTo(0x06));
        assertThat(Script.BOPOMOFO, equalTo(0x07));
        assertThat(Script.CYRILLIC, equalTo(0x08));
        assertThat(Script.DEVANAGARI, equalTo(0x09));
        assertThat(Script.GEORGIAN, equalTo(0x0A));
        assertThat(Script.GREEK, equalTo(0x0B));
        assertThat(Script.GUJARATI, equalTo(0x0C));
        assertThat(Script.GURMUKHI, equalTo(0x0D));
        assertThat(Script.HANGUL, equalTo(0x0E));
        assertThat(Script.HAN, equalTo(0x0F));
        assertThat(Script.HEBREW, equalTo(0x10));
        assertThat(Script.HIRAGANA, equalTo(0x11));
        assertThat(Script.KATAKANA, equalTo(0x12));
        assertThat(Script.KANNADA, equalTo(0x13));
        assertThat(Script.LAO, equalTo(0x14));
        assertThat(Script.LATIN, equalTo(0x15));
        assertThat(Script.MALAYALAM, equalTo(0x16));
        assertThat(Script.ORIYA, equalTo(0x17));
        assertThat(Script.TAMIL, equalTo(0x18));
        assertThat(Script.TELUGU, equalTo(0x19));
        assertThat(Script.THAI, equalTo(0x1A));
        assertThat(Script.TIBETAN, equalTo(0x1B));
        assertThat(Script.BRAILLE, equalTo(0x1C));
        assertThat(Script.CANADIAN_ABORIGINAL, equalTo(0x1D));
        assertThat(Script.CHEROKEE, equalTo(0x1E));
        assertThat(Script.ETHIOPIC, equalTo(0x1F));
        assertThat(Script.KHMER, equalTo(0x20));
        assertThat(Script.MONGOLIAN, equalTo(0x21));
        assertThat(Script.MYANMAR, equalTo(0x22));
        assertThat(Script.OGHAM, equalTo(0x23));
        assertThat(Script.RUNIC, equalTo(0x24));
        assertThat(Script.SINHALA, equalTo(0x25));
        assertThat(Script.SYRIAC, equalTo(0x26));
        assertThat(Script.THAANA, equalTo(0x27));
        assertThat(Script.YI, equalTo(0x28));
        assertThat(Script.DESERET, equalTo(0x29));
        assertThat(Script.GOTHIC, equalTo(0x2A));
        assertThat(Script.OLD_ITALIC, equalTo(0x2B));
        assertThat(Script.BUHID, equalTo(0x2C));
        assertThat(Script.HANUNOO, equalTo(0x2D));
        assertThat(Script.TAGBANWA, equalTo(0x2E));
        assertThat(Script.TAGALOG, equalTo(0x2F));
        assertThat(Script.CYPRIOT, equalTo(0x30));
        assertThat(Script.LIMBU, equalTo(0x31));
        assertThat(Script.LINEAR_B, equalTo(0x32));
        assertThat(Script.OSMANYA, equalTo(0x33));
        assertThat(Script.SHAVIAN, equalTo(0x34));
        assertThat(Script.TAI_LE, equalTo(0x35));
        assertThat(Script.UGARITIC, equalTo(0x36));
        assertThat(Script.BUGINESE, equalTo(0x37));
        assertThat(Script.COPTIC, equalTo(0x38));
        assertThat(Script.GLAGOLITIC, equalTo(0x39));
        assertThat(Script.KHAROSHTHI, equalTo(0x3A));
        assertThat(Script.SYLOTI_NAGRI, equalTo(0x3B));
        assertThat(Script.NEW_TAI_LUE, equalTo(0x3C));
        assertThat(Script.TIFINAGH, equalTo(0x3D));
        assertThat(Script.OLD_PERSIAN, equalTo(0x3E));
        assertThat(Script.BALINESE, equalTo(0x3F));
        assertThat(Script.NKO, equalTo(0x40));
        assertThat(Script.PHAGS_PA, equalTo(0x41));
        assertThat(Script.PHOENICIAN, equalTo(0x42));
        assertThat(Script.CUNEIFORM, equalTo(0x43));
        assertThat(Script.CARIAN, equalTo(0x44));
        assertThat(Script.CHAM, equalTo(0x45));
        assertThat(Script.KAYAH_LI, equalTo(0x46));
        assertThat(Script.LEPCHA, equalTo(0x47));
        assertThat(Script.LYCIAN, equalTo(0x48));
        assertThat(Script.LYDIAN, equalTo(0x49));
        assertThat(Script.OL_CHIKI, equalTo(0x4A));
        assertThat(Script.REJANG, equalTo(0x4B));
        assertThat(Script.SAURASHTRA, equalTo(0x4C));
        assertThat(Script.SUNDANESE, equalTo(0x4D));
        assertThat(Script.VAI, equalTo(0x4E));
        assertThat(Script.IMPERIAL_ARAMAIC, equalTo(0x4F));
        assertThat(Script.AVESTAN, equalTo(0x50));
        assertThat(Script.BAMUM, equalTo(0x51));
        assertThat(Script.EGYPTIAN_HIEROGLYPHS, equalTo(0x52));
        assertThat(Script.JAVANESE, equalTo(0x53));
        assertThat(Script.KAITHI, equalTo(0x54));
        assertThat(Script.TAI_THAM, equalTo(0x55));
        assertThat(Script.LISU, equalTo(0x56));
        assertThat(Script.MEETEI_MAYEK, equalTo(0x57));
        assertThat(Script.OLD_TURKIC, equalTo(0x58));
        assertThat(Script.INSCRIPTIONAL_PAHLAVI, equalTo(0x59));
        assertThat(Script.INSCRIPTIONAL_PARTHIAN, equalTo(0x5A));
        assertThat(Script.SAMARITAN, equalTo(0x5B));
        assertThat(Script.OLD_SOUTH_ARABIAN, equalTo(0x5C));
        assertThat(Script.TAI_VIET, equalTo(0x5D));
        assertThat(Script.BATAK, equalTo(0x5E));
        assertThat(Script.BRAHMI, equalTo(0x5F));
        assertThat(Script.MANDAIC, equalTo(0x60));
        assertThat(Script.CHAKMA, equalTo(0x61));
        assertThat(Script.MEROITIC_CURSIVE, equalTo(0x62));
        assertThat(Script.MEROITIC_HIEROGLYPHS, equalTo(0x63));
        assertThat(Script.MIAO, equalTo(0x64));
        assertThat(Script.SHARADA, equalTo(0x65));
        assertThat(Script.SORA_SOMPENG, equalTo(0x66));
        assertThat(Script.TAKRI, equalTo(0x67));
        assertThat(Script.CAUCASIAN_ALBANIAN, equalTo(0x68));
        assertThat(Script.BASSA_VAH, equalTo(0x69));
        assertThat(Script.DUPLOYAN, equalTo(0x6A));
        assertThat(Script.ELBASAN, equalTo(0x6B));
        assertThat(Script.GRANTHA, equalTo(0x6C));
        assertThat(Script.PAHAWH_HMONG, equalTo(0x6D));
        assertThat(Script.KHOJKI, equalTo(0x6E));
        assertThat(Script.LINEAR_A, equalTo(0x6F));
        assertThat(Script.MAHAJANI, equalTo(0x70));
        assertThat(Script.MANICHAEAN, equalTo(0x71));
        assertThat(Script.MENDE_KIKAKUI, equalTo(0x72));
        assertThat(Script.MODI, equalTo(0x73));
        assertThat(Script.MRO, equalTo(0x74));
        assertThat(Script.OLD_NORTH_ARABIAN, equalTo(0x75));
        assertThat(Script.NABATAEAN, equalTo(0x76));
        assertThat(Script.PALMYRENE, equalTo(0x77));
        assertThat(Script.PAU_CIN_HAU, equalTo(0x78));
        assertThat(Script.OLD_PERMIC, equalTo(0x79));
        assertThat(Script.PSALTER_PAHLAVI, equalTo(0x7A));
        assertThat(Script.SIDDHAM, equalTo(0x7B));
        assertThat(Script.KHUDAWADI, equalTo(0x7C));
        assertThat(Script.TIRHUTA, equalTo(0x7D));
        assertThat(Script.WARANG_CITI, equalTo(0x7E));
        assertThat(Script.AHOM, equalTo(0x7F));
        assertThat(Script.HATRAN, equalTo(0x80));
        assertThat(Script.ANATOLIAN_HIEROGLYPHS, equalTo(0x81));
        assertThat(Script.OLD_HUNGARIAN, equalTo(0x82));
        assertThat(Script.MULTANI, equalTo(0x83));
        assertThat(Script.SIGNWRITING, equalTo(0x84));
        assertThat(Script.ADLAM, equalTo(0x85));
        assertThat(Script.BHAIKSUKI, equalTo(0x86));
        assertThat(Script.MARCHEN, equalTo(0x87));
        assertThat(Script.NEWA, equalTo(0x88));
        assertThat(Script.OSAGE, equalTo(0x89));
        assertThat(Script.TANGUT, equalTo(0x8A));
        assertThat(Script.MASARAM_GONDI, equalTo(0x8B));
        assertThat(Script.NUSHU, equalTo(0x8C));
        assertThat(Script.SOYOMBO, equalTo(0x8D));
        assertThat(Script.ZANABAZAR_SQUARE, equalTo(0x8E));
        assertThat(Script.DOGRA, equalTo(0x8F));
        assertThat(Script.GUNJALA_GONDI, equalTo(0x90));
        assertThat(Script.MAKASAR, equalTo(0x91));
        assertThat(Script.MEDEFAIDRIN, equalTo(0x92));
        assertThat(Script.HANIFI_ROHINGYA, equalTo(0x93));
        assertThat(Script.SOGDIAN, equalTo(0x94));
        assertThat(Script.OLD_SOGDIAN, equalTo(0x95));
    }
}
