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


extern "C" {
#include <ft2build.h>
#include FT_FREETYPE_H
#include FT_SFNT_NAMES_H
}

#include <cstdint>
#include <jni.h>
#include <map>
#include <string>
#include <vector>

#include "JavaBridge.h"
#include "Typeface.h"
#include "SfntNames.h"

using namespace std;
using namespace Tehreer;

class Locale {
private:
    const string *m_platform;
    const vector<string> *m_values;

public:
    Locale(uint16_t platformID, uint16_t languageID)
    {
        static const vector<string> AllPlatforms = {
            "ucd",  // Unicode
            "mac",  // Macintosh
            "iso",  // ISO
            "win",  // Windows
            "cst",  // Custom
        };
        static const string UnrecognizedPlatform = "unr";

        // Format: <ID, <Language, Region, Script, Variant>>
        static const map<uint16_t, vector<string>> MacLanguages = {
            {0, {"en"}},
            {1, {"fr"}},
            {2, {"de"}},
            {3, {"it"}},
            {4, {"nl"}},
            {5, {"sv"}},
            {6, {"es"}},
            {7, {"da"}},
            {8, {"pt"}},
            {9, {"no"}},
            {10, {"he"}},
            {11, {"ja"}},
            {12, {"ar"}},
            {13, {"fi"}},
            {14, {"el"}},
            {15, {"is"}},
            {16, {"mt"}},
            {17, {"tr"}},
            {18, {"hr"}},
            {19, {"zh", "", "Hant"}},
            {20, {"ur"}},
            {21, {"hi"}},
            {22, {"th"}},
            {23, {"ko"}},
            {24, {"lt"}},
            {25, {"pl"}},
            {26, {"hu"}},
            {27, {"es"}},
            {28, {"lv"}},
            {29, {"se"}},
            {30, {"fo"}},
            {31, {"fa"}},
            {32, {"ru"}},
            {33, {"zh"}},
            {34, {"nl", "BE"}},
            {35, {"ga"}},
            {36, {"sq"}},
            {37, {"ro"}},
            {38, {"cz"}},
            {39, {"sk"}},
            {40, {"si"}},
            {41, {"yi"}},
            {42, {"sr"}},
            {43, {"mk"}},
            {44, {"bg"}},
            {45, {"uk"}},
            {46, {"be"}},
            {47, {"uz"}},
            {48, {"kk"}},
            {49, {"az", "", "Cyrl"}},
            {50, {"az", "", "Arab"}},
            {51, {"hy"}},
            {52, {"ka"}},
            {53, {"mo"}},
            {54, {"ky"}},
            {55, {"tg"}},
            {56, {"tk"}},
            {57, {"mn", "CN"}},
            {58, {"mn"}},
            {59, {"ps"}},
            {60, {"ks"}},
            {61, {"ku"}},
            {62, {"sd"}},
            {63, {"bo"}},
            {64, {"ne"}},
            {65, {"sa"}},
            {66, {"mr"}},
            {67, {"bn"}},
            {68, {"as"}},
            {69, {"gu"}},
            {70, {"pa"}},
            {71, {"or"}},
            {72, {"ml"}},
            {73, {"kn"}},
            {74, {"ta"}},
            {75, {"te"}},
            {76, {"si"}},
            {77, {"my"}},
            {78, {"km"}},
            {79, {"lo"}},
            {80, {"vi"}},
            {81, {"id"}},
            {82, {"tl"}},
            {83, {"ms"}},
            {84, {"ms", "", "Arab"}},
            {85, {"am"}},
            {86, {"ti"}},
            {87, {"om"}},
            {88, {"so"}},
            {89, {"sw"}},
            {90, {"rw"}},
            {91, {"rn"}},
            {92, {"ny"}},
            {93, {"mg"}},
            {94, {"eo"}},
            {128, {"cy"}},
            {129, {"eu"}},
            {130, {"ca"}},
            {131, {"la"}},
            {132, {"qu"}},
            {133, {"gn"}},
            {134, {"ay"}},
            {135, {"tt"}},
            {136, {"ug"}},
            {137, {"dz"}},
            {138, {"jv"}},
            {139, {"su"}},
            {140, {"gl"}},
            {141, {"af"}},
            {142, {"br"}},
            {143, {"iu"}},
            {144, {"gd"}},
            {145, {"gv"}},
            {146, {"ga"}},
            {147, {"to"}},
            {148, {"el", "", "", "polyton"}},
            {149, {"kl"}},
            {150, {"az"}},
            {151, {"nn"}},
        };
        // Reference: https://msdn.microsoft.com/en-us/library/cc233982.aspx
        static const map<uint16_t, vector<string>> WindowsLanguages = {
            {0x0436, {"af", "ZA"}},
            {0x041C, {"sq", "AL"}},
            {0x0484, {"gsw", "FR"}},
            {0x045E, {"am", "ET"}},
            {0x1401, {"ar", "DZ"}},
            {0x3C01, {"ar", "BH"}},
            {0x0C01, {"ar", "EG"}},
            {0x0801, {"ar", "IQ"}},
            {0x2C01, {"ar", "JO"}},
            {0x3401, {"ar", "KW"}},
            {0x3001, {"ar", "LB"}},
            {0x1001, {"ar", "LY"}},
            {0x1801, {"ar", "MA"}},
            {0x2001, {"ar", "OM"}},
            {0x4001, {"ar", "QA"}},
            {0x0401, {"ar", "SA"}},
            {0x2801, {"ar", "SY"}},
            {0x1C01, {"ar", "TN"}},
            {0x3801, {"ar", "AE"}},
            {0x2401, {"ar", "YE"}},
            {0x042B, {"hy", "AM"}},
            {0x044D, {"as", "IN"}},
            {0x082C, {"az", "AZ", "Cyrl"}},
            {0x042C, {"az", "AZ", "Latn"}},
            {0x046D, {"ba", "RU"}},
            {0x042D, {"eu", "ES"}},
            {0x0423, {"be", "BY"}},
            {0x0845, {"bn", "BD"}},
            {0x0445, {"bn", "IN"}},
            {0x201A, {"bs", "BA", "Cyrl"}},
            {0x141A, {"bs", "BA", "Latn"}},
            {0x047E, {"br", "FR"}},
            {0x0402, {"bg", "BG"}},
            {0x0403, {"ca", "ES"}},
            {0x0C04, {"zh", "HK"}},
            {0x1404, {"zh", "MO"}},
            {0x0804, {"zh", "CN"}},
            {0x1004, {"zh", "SG"}},
            {0x0404, {"zh", "TW"}},
            {0x0483, {"co", "FR"}},
            {0x041A, {"hr", "HR"}},
            {0x101A, {"hr", "BA"}},
            {0x0405, {"cs", "CZ"}},
            {0x0406, {"da", "DK"}},
            {0x048C, {"prs", "AF"}},
            {0x0465, {"dv", "MV"}},
            {0x0813, {"nl", "BE"}},
            {0x0413, {"nl", "NL"}},
            {0x0C09, {"en", "AU"}},
            {0x2809, {"en", "BZ"}},
            {0x1009, {"en", "CA"}},
            {0x2409, {"en", "029"}},
            {0x4009, {"en", "IN"}},
            {0x1809, {"en", "IE"}},
            {0x2009, {"en", "JM"}},
            {0x4409, {"en", "MY"}},
            {0x1409, {"en", "NZ"}},
            {0x3409, {"en", "PH"}},
            {0x4809, {"en", "SG"}},
            {0x1C09, {"en", "ZA"}},
            {0x2C09, {"en", "TT"}},
            {0x0809, {"en", "GB"}},
            {0x0409, {"en", "US"}},
            {0x3009, {"en", "ZW"}},
            {0x0425, {"et", "EE"}},
            {0x0438, {"fo", "FO"}},
            {0x0464, {"fil", "PH"}},
            {0x040B, {"fi", "FI"}},
            {0x080C, {"fr", "BE"}},
            {0x0C0C, {"fr", "CA"}},
            {0x040C, {"fr", "FR"}},
            {0x140C, {"fr", "LU"}},
            {0x180C, {"fr", "MC"}},
            {0x100C, {"fr", "CH"}},
            {0x0462, {"fy", "NL"}},
            {0x0456, {"gl", "ES"}},
            {0x0437, {"ka", "GE"}},
            {0x0C07, {"de", "AT"}},
            {0x0407, {"de", "DE"}},
            {0x1407, {"de", "LI"}},
            {0x1007, {"de", "LU"}},
            {0x0807, {"de", "CH"}},
            {0x0408, {"el", "GR"}},
            {0x046F, {"kl", "GL"}},
            {0x0447, {"gu", "IN"}},
            {0x0468, {"ha", "NG", "Latn"}},
            {0x040D, {"he", "IL"}},
            {0x0439, {"hi", "IN"}},
            {0x040E, {"hu", "HU"}},
            {0x040F, {"is", "IS"}},
            {0x0470, {"ig", "NG"}},
            {0x0421, {"id", "ID"}},
            {0x045D, {"iu", "CA", "Cans"}},
            {0x085D, {"iu", "CA", "Latn"}},
            {0x083C, {"ga", "IE"}},
            {0x0434, {"xh", "ZA"}},
            {0x0435, {"zu", "ZA"}},
            {0x0410, {"it", "IT"}},
            {0x0810, {"it", "CH"}},
            {0x0411, {"ja", "JP"}},
            {0x044B, {"kn", "IN"}},
            {0x043F, {"kk", "KZ"}},
            {0x0453, {"km", "KH"}},
            {0x0486, {"quc", "GT", "Latn"}},
            {0x0487, {"rw", "RW"}},
            {0x0441, {"sw", "KE"}},
            {0x0457, {"kok", "IN"}},
            {0x0412, {"ko", "KR"}},
            {0x0440, {"ky", "KG"}},
            {0x0454, {"lo", "LA"}},
            {0x0426, {"lv", "LV"}},
            {0x0427, {"lt", "LT"}},
            {0x082E, {"dsb", "DE"}},
            {0x046E, {"lb", "LU"}},
            {0x042F, {"mk", "MK"}},
            {0x083E, {"ms", "BN"}},
            {0x043E, {"ms", "MY"}},
            {0x044C, {"ml", "IN"}},
            {0x043A, {"mt", "MT"}},
            {0x0481, {"mi", "NZ"}},
            {0x047A, {"arn", "CL"}},
            {0x044E, {"mr", "IN"}},
            {0x047C, {"moh", "CA"}},
            {0x0450, {"mn", "MN"}},
            {0x0850, {"mn", "CN", "Mong"}},
            {0x0461, {"ne", "NP"}},
            {0x0414, {"nb", "NO"}},
            {0x0814, {"nn", "NO"}},
            {0x0482, {"oc", "FR"}},
            {0x0448, {"or", "IN"}},
            {0x0463, {"ps", "AF"}},
            {0x0415, {"pl", "PL"}},
            {0x0416, {"pt", "BR"}},
            {0x0816, {"pt", "PT"}},
            {0x0446, {"pa", "IN"}},
            {0x046B, {"quz", "BO"}},
            {0x086B, {"quz", "EC"}},
            {0x0C6B, {"quz", "PE"}},
            {0x0418, {"ro", "RO"}},
            {0x0417, {"rm", "CH"}},
            {0x0419, {"ru", "RU"}},
            {0x243B, {"smn", "FI"}},
            {0x103B, {"smj", "NO"}},
            {0x143B, {"smj", "SE"}},
            {0x0C3B, {"se", "FI"}},
            {0x043B, {"se", "NO"}},
            {0x083B, {"se", "SE"}},
            {0x203B, {"sms", "FI"}},
            {0x183B, {"sma", "NO"}},
            {0x1C3B, {"sma", "SE"}},
            {0x044F, {"sa", "IN"}},
            {0x1C1A, {"sr", "BA", "Cyrl"}},
            {0x0C1A, {"sr", "CS", "Cyrl"}},
            {0x181A, {"sr", "BA", "Latn"}},
            {0x081A, {"sr", "CS", "Latn"}},
            {0x046C, {"nso", "ZA"}},
            {0x0432, {"tn", "ZA"}},
            {0x045B, {"si", "LK"}},
            {0x041B, {"sk", "SK"}},
            {0x0424, {"sl", "SI"}},
            {0x2C0A, {"es", "AR"}},
            {0x400A, {"es", "BO"}},
            {0x340A, {"es", "CL"}},
            {0x240A, {"es", "CO"}},
            {0x140A, {"es", "CR"}},
            {0x1C0A, {"es", "DO"}},
            {0x300A, {"es", "EC"}},
            {0x440A, {"es", "SV"}},
            {0x100A, {"es", "GT"}},
            {0x480A, {"es", "HN"}},
            {0x080A, {"es", "MX"}},
            {0x4C0A, {"es", "NI"}},
            {0x180A, {"es", "PA"}},
            {0x3C0A, {"es", "PY"}},
            {0x280A, {"es", "PE"}},
            {0x500A, {"es", "PR"}},
            {0x0C0A, {"es", "ES"}},
            {0x040A, {"es", "ES", "", "tradnl"}},
            {0x540A, {"es", "US"}},
            {0x380A, {"es", "UY"}},
            {0x200A, {"es", "VE"}},
            {0x081D, {"sv", "FI"}},
            {0x041D, {"sv", "SE"}},
            {0x045A, {"syr", "SY"}},
            {0x0428, {"tg", "TJ", "Cyrl"}},
            {0x085F, {"tzm", "DZ", "Latn"}},
            {0x0449, {"ta", "IN"}},
            {0x0444, {"tt", "RU"}},
            {0x044A, {"te", "IN"}},
            {0x041E, {"th", "TH"}},
            {0x0451, {"bo", "CN"}},
            {0x041F, {"tr", "TR"}},
            {0x0442, {"tk", "TM"}},
            {0x0480, {"ug", "CN"}},
            {0x0422, {"uk", "UA"}},
            {0x042E, {"hsb", "DE"}},
            {0x0420, {"ur", "PK"}},
            {0x0843, {"uz", "UZ", "Cyrl"}},
            {0x0443, {"uz", "UZ", "Latn"}},
            {0x042A, {"vi", "VN"}},
            {0x0452, {"cy", "GB"}},
            {0x0488, {"wo", "SN"}},
            {0x0485, {"sah", "RU"}},
            {0x0478, {"ii", "CN"}},
            {0x046A, {"yo", "NG"}},
        };
        static const vector<string> UndeterminedLanguage = {"und"};

        if (platformID < AllPlatforms.size()) {
            m_platform = &AllPlatforms[platformID];
        } else {
            m_platform = &UnrecognizedPlatform;
        }

        switch (platformID) {
        case 1: {
            auto pair = MacLanguages.find(languageID);
            if (pair != MacLanguages.end()) {
                m_values = &pair->second;
            }
            break;
        }

        case 3: {
            auto pair = WindowsLanguages.find(languageID);
            if (pair != WindowsLanguages.end()) {
                m_values = &pair->second;
            }
            break;
        }

        default:
            m_values = &UndeterminedLanguage;
            break;
        }
    }

    const char *platform() const { return (m_platform ? m_platform->c_str() : nullptr); }
    const char *language() const { return (m_values->size() > 0 ? m_values->at(0).c_str() : nullptr); }
    const char *region() const { return (m_values->size() > 1 ? m_values->at(1).c_str() : nullptr); }
    const char *script() const { return (m_values->size() > 2 ? m_values->at(2).c_str() : nullptr); }
    const char *variant() const { return (m_values->size() > 3 ? m_values->at(3).c_str() : nullptr); }
};

class Encoding {
private:
    string m_name;

public:
    Encoding(uint16_t platformID, uint16_t encodingID)
    {
        switch (platformID) {
        case 0:
            switch (encodingID) {
            case 0:
            case 1:
            case 2:
            case 3:
                m_name = "UTF-16";
                break;

            case 4:
            case 6:
                m_name = "UTF_32";
                break;

            default:
                // Unsupported encoding.
                break;
            }
            break;

        case 1:
            switch (encodingID) {
            case 0:
                m_name = "MacRoman";
                break;

            case 1:
                m_name = "SJIS";
                break;

            case 2:
                m_name = "Big5";
                break;

            case 3:
                m_name = "EUC_KR";
                break;

            case 4:
                m_name = "MacArabic";
                break;

            case 5:
                m_name = "MacHebrew";
                break;

            case 6:
                m_name = "MacGreek";
                break;

            case 7:
            case 29:
                m_name = "MacCyrillic";
                break;

            case 8:
                m_name = "MacSymbol";
                break;

            case 21:
                m_name = "MacThai";
                break;

            case 25:
                m_name = "EUC-CN";
                break;

            default:
                // Unsupported encoding.
                break;
            }
            break;

        case 2:
            switch (encodingID) {
            case 0:
                m_name = "ASCII";
                break;

            case 1:
                m_name = "UTF-16";
                break;

            case 2:
                m_name = "ISO8859_1";
                break;

            default:
                // Unsupported encoding.
                break;
            }
            break;

        case 3:
            switch (encodingID) {
            case 0:
            case 1:
                m_name = "UTF-16";
                break;

            case 2:
                m_name = "SJIS";
                break;

            case 3:
                m_name = "GBK";
                break;

            case 4:
                m_name = "MS950";
                break;

            case 5:
                m_name = "EUC_KR";
                break;

            case 6:
                m_name = "Johab";
                break;

            case 10:
                m_name = "UTF_32";
                break;

            default:
                // Unsupported encoding.
                break;
            }
            break;

        default:
            // Unsupported platform.
            break;
        }
    }

    const char *name() const { return (m_name.length() > 0 ? m_name.c_str() : nullptr); }
};

static jobject createLocale(JNIEnv *env, Locale &locale)
{
    jstring platform = env->NewStringUTF(locale.platform());
    jstring language = env->NewStringUTF(locale.language());
    jstring region = env->NewStringUTF(locale.region());
    jstring script = env->NewStringUTF(locale.script());
    jstring variant = env->NewStringUTF(locale.variant());
    jobject result = JavaBridge(env).SfntNames_createLocale(platform, language, region, script, variant);

    env->DeleteLocalRef(variant);
    env->DeleteLocalRef(script);
    env->DeleteLocalRef(region);
    env->DeleteLocalRef(language);
    env->DeleteLocalRef(platform);

    return result;
}

static jbyteArray createBytes(JNIEnv *env, FT_Byte *buffer, FT_UInt length)
{
    jbyteArray bytes = env->NewByteArray(length);
    env->SetByteArrayRegion(bytes, 0, length, reinterpret_cast<jbyte *>(buffer));

    return bytes;
}

static jstring decodeBytes(JNIEnv *env, Encoding &encoding, jbyteArray bytes)
{
    if (encoding.name()) {
        jstring charsetName = env->NewStringUTF(encoding.name());
        jstring decodedString = JavaBridge(env).SfntNames_decodeBytes(charsetName, bytes);

        env->DeleteLocalRef(charsetName);

        return decodedString;
    }

    return nullptr;
}

static jstring decodeBuffer(JNIEnv *env, Encoding &encoding, FT_Byte *buffer, FT_UInt length)
{
    jbyteArray encodedBytes = createBytes(env, buffer, length);
    jstring decodedString = decodeBytes(env, encoding, encodedBytes);

    env->DeleteLocalRef(encodedBytes);

    return decodedString;
}

jstring getLocaleTag(JNIEnv *env, jobject obj, jint tagId, jint platformId, jint languageId)
{
    Locale locale(static_cast<uint16_t>(platformId), static_cast<uint16_t>(languageId));
    jstring tag = nullptr;

    switch(tagId) {
    case 0:
        tag = env->NewStringUTF(locale.platform());
        break;

    case 1:
        tag = env->NewStringUTF(locale.language());
        break;

    case 2:
        tag = env->NewStringUTF(locale.region());
        break;

    case 3:
        tag = env->NewStringUTF(locale.script());
        break;

    case 4:
        tag = env->NewStringUTF(locale.variant());
        break;

    default:
        // Unknown tag id.
        break;
    }

    return tag;
}

jstring getCharsetName(JNIEnv *env, jobject obj, jint platformId, jint encodingId)
{
    Encoding encoding(static_cast<uint16_t>(platformId), static_cast<uint16_t>(encodingId));
    jstring name = env->NewStringUTF(encoding.name());

    return name;
}

void addStandardNames(JNIEnv *env, jobject obj, jobject jsfntNames, jobject jtypeface)
{
    jlong typefaceHandle = JavaBridge(env).Typeface_getNativeTypeface(jtypeface);
    Typeface *typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    FT_Face baseFace = typeface->ftFace();
    FT_UInt nameCount = FT_Get_Sfnt_Name_Count(baseFace);

    for (FT_UInt i = 0; i < nameCount; i++) {
        // Lock the typeface as FreeType loads the name on demand.
        typeface->lock();

        FT_SfntName sfntName;
        FT_Error error = FT_Get_Sfnt_Name(baseFace, i, &sfntName);

        typeface->unlock();

        if (error != FT_Err_Ok) {
            continue;
        }

        if (sfntName.name_id <= 25) {
            Locale locale(sfntName.platform_id, sfntName.language_id);
            Encoding encoding(sfntName.platform_id, sfntName.encoding_id);

            jobject relevantLocale = createLocale(env, locale);
            jstring decodedString = decodeBuffer(env, encoding, sfntName.string, sfntName.string_len);

            if (decodedString) {
                JavaBridge(env).SfntNames_addName(jsfntNames, sfntName.name_id, relevantLocale, decodedString);
            }

            env->DeleteLocalRef(decodedString);
            env->DeleteLocalRef(relevantLocale);
        }
    }
}

jint getNameCount(JNIEnv *env, jobject obj, jobject jtypeface)
{
    jlong typefaceHandle = JavaBridge(env).Typeface_getNativeTypeface(jtypeface);
    Typeface *typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    FT_Face baseFace = typeface->ftFace();
    FT_UInt nameCount = FT_Get_Sfnt_Name_Count(baseFace);

    return static_cast<jint>(nameCount);
}

jobject getNameAt(JNIEnv *env, jobject obj, jobject jtypeface, jint index)
{
    jlong typefaceHandle = JavaBridge(env).Typeface_getNativeTypeface(jtypeface);
    Typeface *typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    FT_Face baseFace = typeface->ftFace();

    // Lock the typeface as FreeType loads the name on demand.
    typeface->lock();

    FT_SfntName sfntName;
    FT_Get_Sfnt_Name(baseFace, static_cast<FT_UInt>(index), &sfntName);

    typeface->unlock();

    jbyteArray bytes = createBytes(env, sfntName.string, sfntName.string_len);

    return JavaBridge(env).SfntNamesEntry_construct(sfntName.name_id, sfntName.platform_id,
                                                    sfntName.language_id, sfntName.encoding_id,
                                                    bytes);
}

static JNINativeMethod JNI_METHODS[] = {
    { "nativeGetLocaleTag", "(III)Ljava/lang/String;", (void *)getLocaleTag },
    { "nativeGetCharsetName", "(II)Ljava/lang/String;", (void *)getCharsetName },
    { "nativeAddStandardNames", "(Lcom/mta/tehreer/opentype/SfntNames;Lcom/mta/tehreer/graphics/Typeface;)V", (void *)addStandardNames },
    { "nativeGetNameCount", "(Lcom/mta/tehreer/graphics/Typeface;)I", (void *)getNameCount },
    { "nativeGetNameAt", "(Lcom/mta/tehreer/graphics/Typeface;I)Lcom/mta/tehreer/opentype/SfntNames$Entry;", (void *)getNameAt },
};

jint register_com_mta_tehreer_opentype_SfntNames(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/opentype/SfntNames", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
