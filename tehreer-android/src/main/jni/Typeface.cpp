/*
 * Copyright (C) 2016-2019 Muhammad Tayyab Akram
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
#include FT_ADVANCES_H
#include FT_FREETYPE_H
#include FT_MULTIPLE_MASTERS_H
#include FT_SFNT_NAMES_H
#include FT_SIZES_H
#include FT_STROKER_H
#include FT_SYSTEM_H
#include FT_TRUETYPE_TABLES_H
#include FT_TYPES_H

#include <SFFont.h>
}

#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <cstdint>
#include <cstdlib>
#include <cstring>
#include <jni.h>
#include <mutex>
#include <string>

#include "FontFile.h"
#include "FreeType.h"
#include "JavaBridge.h"
#include "Miscellaneous.h"
#include "SfntTables.h"
#include "Typeface.h"

using namespace std;
using namespace Tehreer;
using namespace Tehreer::SFNT::head;
using namespace Tehreer::SFNT::name;
using namespace Tehreer::SFNT::OS2;

static inline FT_F26Dot6 toF26Dot6(float value)
{
    return static_cast<FT_F26Dot6>((value * 64) + 0.5);
}

static inline FT_Fixed toF16Dot16(float value)
{
    return static_cast<FT_Fixed>((value * 0x10000) + 0.5);
}

static inline float f16Dot16toFloat(FT_Fixed value)
{
    return value / static_cast<float>(0x10000);
}

static inline float f26Dot6PosToFloat(FT_Pos value)
{
    return static_cast<float>(value / 64.0);
}

/**
 * NOTE: The caller needs to lock the typeface before invoking this function.
 */
static int32_t searchEnglishNameRecordIndex(FT_Face face, uint16_t nameID)
{
    FT_UInt nameCount = FT_Get_Sfnt_Name_Count(face);
    FT_Int candidate = -1;

    for (FT_UInt i = 0; i < nameCount; i++) {
        FT_SfntName record;
        FT_Get_Sfnt_Name(face, i, &record);

        if (record.name_id != nameID) {
            continue;
        }

        Locale locale(record.platform_id, record.language_id);
        const string *language = locale.language();

        if (language && *language == "en") {
            const string *region = locale.region();

            if (record.platform_id == PlatformID::WINDOWS && region && *region == "US") {
                return i;
            }

            if (candidate == -1 || record.platform_id == PlatformID::MACINTOSH) {
                candidate = i;
            }
        }
    }

    return candidate;
}

static int32_t searchFamilyNameRecordIndex(FT_Face face, TT_OS2 *os2Table)
{
    int32_t familyName = -1;

    if (os2Table && (os2Table->fsSelection & FSSelection::WWS)) {
        familyName = searchEnglishNameRecordIndex(face, NameID::WWS_FAMILY);
    }
    if (familyName == -1) {
        familyName = searchEnglishNameRecordIndex(face, NameID::TYPOGRAPHIC_FAMILY);
    }
    if (familyName == -1) {
        familyName = searchEnglishNameRecordIndex(face, NameID::FONT_FAMILY);
    }

    return familyName;
}

static int32_t searchStyleNameRecordIndex(FT_Face face, TT_OS2 *os2Table)
{
    int32_t styleName = -1;

    if (os2Table && (os2Table->fsSelection & FSSelection::WWS)) {
        styleName = searchEnglishNameRecordIndex(face, NameID::WWS_SUBFAMILY);
    }
    if (styleName == -1) {
        styleName = searchEnglishNameRecordIndex(face, NameID::TYPOGRAPHIC_SUBFAMILY);
    }
    if (styleName == -1) {
        styleName = searchEnglishNameRecordIndex(face, NameID::FONT_SUBFAMILY);
    }

    return styleName;
}

static int32_t searchFullNameRecordIndex(FT_Face face)
{
    return searchEnglishNameRecordIndex(face, NameID::FULL);
}

static inline uint16_t variableWeightToStandard(FT_Fixed coordinate)
{
    float value = f16Dot16toFloat(coordinate);

    if (value < 1) {
        return 1;
    }
    if (value > 1000) {
        return 1000;
    }

    return static_cast<uint16_t>(value);
}

static inline uint16_t variableWidthToStandard(FT_Fixed coordinate)
{
    float value = f16Dot16toFloat(coordinate);

    if (value < 50) {
        return 1;
    }
    if (value < 125) {
        return static_cast<uint16_t>(((value - 50) / 12.5) + 1);
    }
    if (value < 200) {
        return static_cast<uint16_t>(((value - 125) / 25) + 7);
    }

    return 9;
}

static inline Typeface::Slope variableItalicToSlope(FT_Fixed coordinate)
{
    return coordinate >= 0x10000 ? Typeface::Slope::ITALIC : Typeface::Slope::PLAIN;
}

Typeface *Typeface::createFromFile(FontFile *fontFile, FT_Long faceIndex, FT_Long instanceIndex)
{
    if (fontFile) {
        FT_Face ftFace = fontFile->createFace(faceIndex, instanceIndex);
        if (ftFace) {
            return new Typeface(fontFile, ftFace);
        }
    }

    return nullptr;
}

Typeface::Typeface(FontFile *fontFile, FT_Face ftFace)
    : m_familyName(-1)
    , m_styleName(-1)
    , m_fullName(-1)
    , m_weight(Weight::REGULAR)
    , m_width(Width::NORMAL)
    , m_slope(Slope::PLAIN)
    , m_strikeoutPosition(0)
    , m_strikeoutThickness(0)
{
    SFFontProtocol protocol;
    protocol.finalize = nullptr;
    protocol.loadTable = [](void *object, SFTag tag, SFUInt8 *buffer, SFUInteger *length) {
        Typeface *typeface = reinterpret_cast<Typeface *>(object);
        FT_ULong tableSize = 0;

        typeface->loadSfntTable(tag, buffer, length ? &tableSize : nullptr);

        if (length) {
            *length = tableSize;
        }
    };
    protocol.getGlyphIDForCodepoint = [](void *object, SFCodepoint codepoint) {
        Typeface *typeface = reinterpret_cast<Typeface *>(object);
        FT_UInt glyphID = typeface->getGlyphID(codepoint);
        if (glyphID > 0xFFFF) {
            LOGW("Received invalid glyph id for code point: %u", codepoint);
            glyphID = 0;
        }

        return static_cast<SFGlyphID>(glyphID);
    };
    protocol.getAdvanceForGlyph = [](void *object, SFFontLayout fontLayout, SFGlyphID glyphID)
    {
        Typeface *typeface = reinterpret_cast<Typeface *>(object);
        FT_Fixed glyphAdvance = typeface->getGlyphAdvance(glyphID, fontLayout == SFFontLayoutVertical);

        return static_cast<SFInt32>(glyphAdvance);
    };

    m_fontFile = fontFile->retain();
    m_ftFace = ftFace;
    m_ftSize = nullptr;
    m_ftStroker = nullptr;
    m_sfFont = SFFontCreateWithProtocol(&protocol, this);

    FT_New_Size(m_ftFace, &m_ftSize);

    setupDescription();
    setupVariation();
}

void Typeface::setupDescription()
{
    TT_OS2 *os2Table = static_cast<TT_OS2 *>(FT_Get_Sfnt_Table(m_ftFace, FT_SFNT_OS2));
    TT_Header *headTable = static_cast<TT_Header *>(FT_Get_Sfnt_Table(m_ftFace, FT_SFNT_HEAD));

    m_familyName = searchFamilyNameRecordIndex(m_ftFace, os2Table);
    m_styleName = searchStyleNameRecordIndex(m_ftFace, os2Table);
    m_fullName = searchFullNameRecordIndex(m_ftFace);

    if (os2Table) {
        m_weight = os2Table->usWeightClass;
        m_width = os2Table->usWidthClass;

        if (os2Table->fsSelection & FSSelection::OBLIQUE) {
            m_slope = Slope::OBLIQUE;
        } else if (os2Table->fsSelection & FSSelection::ITALIC) {
            m_slope = Slope::ITALIC;
        }

        m_strikeoutPosition = os2Table->yStrikeoutPosition;
        m_strikeoutThickness = os2Table->yStrikeoutSize;
    } else if (headTable) {
        if (headTable->Mac_Style & MacStyle::BOLD) {
            m_weight = Weight::BOLD;
        }

        if (headTable->Mac_Style & MacStyle::CONDENSED) {
            m_width = Width::CONDENSED;
        } else if (headTable->Mac_Style & MacStyle::EXTENDED) {
            m_width = Width::EXPANDED;
        }

        if (headTable->Mac_Style & MacStyle::ITALIC) {
            m_slope = Slope::ITALIC;
        }
    }
}

void Typeface::setupVariation()
{
    FT_MM_Var *variation;
    FT_Error error = FT_Get_MM_Var(m_ftFace, &variation);

    if (error == FT_Err_Ok) {
        FT_UInt numCoords = variation->num_axis;
        FT_Fixed fixedCoords[numCoords];

        if (FT_Get_Var_Blend_Coordinates(m_ftFace, numCoords, fixedCoords) == FT_Err_Ok) {
            SFFontRef normalFont = m_sfFont;
            SFInt16 coordArray[numCoords];

            // Convert the FreeType's F16DOT16 coordinates to standard normalized F2DOT14 format.
            for (FT_UInt i = 0; i < numCoords; i++) {
                coordArray[i] = static_cast<SFInt16>(fixedCoords[i] >> 2);
            }

            // Derive the variable font of SheenFigure.
            m_sfFont = SFFontCreateWithVariationCoordinates(normalFont, m_ftFace, coordArray, numCoords);
            SFFontRelease(normalFont);
        }

        if (FT_Get_Var_Design_Coordinates(m_ftFace, numCoords, fixedCoords) == FT_Err_Ok) {
            // Get the style name of this instance.
            for (FT_UInt i = 0; i < variation->num_namedstyles; i++) {
                FT_Var_Named_Style *namedStyle = &variation->namedstyle[i];
                FT_Fixed *namedCoords = namedStyle->coords;

                int result = memcmp(namedCoords, fixedCoords, sizeof(FT_Fixed) * numCoords);
                if (result == 0) {
                    m_styleName = static_cast<uint16_t>(namedStyle->strid);
                    break;
                }
            }

            // Get the values of variation axes.
            for (FT_UInt i = 0; i < numCoords; i++) {
                FT_Var_Axis *axis = &variation->axis[i];

                switch (axis->tag) {
                case FT_MAKE_TAG('i', 't', 'a', 'l'):
                    m_slope = variableItalicToSlope(fixedCoords[i]);
                    break;

                case FT_MAKE_TAG('s', 'l', 'n', 't'):
                    // TODO: Evaluate slant coordinate as oblique style.
                    break;

                case FT_MAKE_TAG('w', 'd', 't', 'h'):
                    m_width = variableWidthToStandard(fixedCoords[i]);
                    break;

                case FT_MAKE_TAG('w', 'g', 'h', 't'):
                    m_weight = variableWeightToStandard(fixedCoords[i]);
                    break;
                }
            }
        }

        FT_Done_MM_Var(FreeType::library(), variation);
    }
}

Typeface::~Typeface()
{
    SFFontRelease(m_sfFont);

    if (m_ftStroker) {
        FT_Stroker_Done(m_ftStroker);
    }
    if (m_ftSize) {
        m_mutex.lock();

        FT_Done_Size(m_ftSize);

        m_mutex.unlock();
    }
    if (m_ftFace) {
        std::mutex &mutex = FreeType::mutex();
        mutex.lock();

        FT_Done_Face(m_ftFace);

        mutex.unlock();
    }

    m_fontFile->release();
}

FT_Stroker Typeface::ftStroker()
{
    /*
     * NOTE:
     *      The caller is responsible to lock the mutex.
     */

    if (!m_ftStroker) {
        /*
         * There is no need to lock 'library' as it is only taken to have access to FreeType's
         * memory handling functions.
         */
        FT_Stroker_New(FreeType::library(), &m_ftStroker);
    }

    return m_ftStroker;
}

void Typeface::loadSfntTable(FT_ULong tag, FT_Byte *buffer, FT_ULong *length)
{
    m_mutex.lock();

    FT_Load_Sfnt_Table(m_ftFace, tag, 0, buffer, length);

    m_mutex.unlock();
}

FT_UInt Typeface::getGlyphID(FT_ULong codePoint)
{
    m_mutex.lock();

    FT_UInt glyphID = FT_Get_Char_Index(m_ftFace, codePoint);

    m_mutex.unlock();

    return glyphID;
}

FT_Fixed Typeface::getGlyphAdvance(FT_UInt glyphID, bool vertical)
{
    FT_Int32 loadFlags = FT_LOAD_NO_SCALE;
    if (vertical) {
        loadFlags |= FT_LOAD_VERTICAL_LAYOUT;
    }

    m_mutex.lock();

    FT_Fixed advance;
    FT_Get_Advance(m_ftFace, glyphID, loadFlags, &advance);

    m_mutex.unlock();

    return advance;
}

FT_Fixed Typeface::getGlyphAdvance(FT_UInt glyphID, FT_F26Dot6 typeSize, bool vertical)
{
    FT_Int32 loadFlags = FT_LOAD_DEFAULT;
    if (vertical) {
        loadFlags |= FT_LOAD_VERTICAL_LAYOUT;
    }

    m_mutex.lock();

    FT_Activate_Size(m_ftSize);
    FT_Set_Char_Size(m_ftFace, 0, typeSize, 0, 0);
    FT_Set_Transform(m_ftFace, nullptr, nullptr);

    FT_Fixed advance;
    FT_Get_Advance(m_ftFace, glyphID, loadFlags, &advance);

    m_mutex.unlock();

    return advance;
}

jobject Typeface::getGlyphPathNoLock(JavaBridge bridge, FT_UInt glyphID)
{
    jobject glyphPath = nullptr;

    FT_Error error = FT_Load_Glyph(m_ftFace, glyphID, FT_LOAD_NO_BITMAP);
    if (error == FT_Err_Ok) {
        struct PathContext {
            JavaBridge bridge;
            jobject path;
        };

        FT_Outline_Funcs funcs;
        funcs.move_to = [](const FT_Vector *to, void *user) -> int
        {
            PathContext *context = reinterpret_cast<PathContext *>(user);
            context->bridge.Path_moveTo(context->path,
                                        f26Dot6PosToFloat(to->x), f26Dot6PosToFloat(to->y));
            return 0;
        };
        funcs.line_to = [](const FT_Vector *to, void *user) -> int
        {
            PathContext *context = reinterpret_cast<PathContext *>(user);
            context->bridge.Path_lineTo(context->path,
                                        f26Dot6PosToFloat(to->x), f26Dot6PosToFloat(to->y));
            return 0;
        };
        funcs.conic_to = [](const FT_Vector *control1, const FT_Vector *to, void *user) -> int
        {
            PathContext *context = reinterpret_cast<PathContext *>(user);
            context->bridge.Path_quadTo(context->path,
                                        f26Dot6PosToFloat(control1->x), f26Dot6PosToFloat(control1->y),
                                        f26Dot6PosToFloat(to->x), f26Dot6PosToFloat(to->y));
            return 0;
        };
        funcs.cubic_to = [](const FT_Vector *control1, const FT_Vector *control2, const FT_Vector *to, void *user) -> int
        {
            PathContext *context = reinterpret_cast<PathContext *>(user);
            context->bridge.Path_cubicTo(context->path,
                                         f26Dot6PosToFloat(control1->x), f26Dot6PosToFloat(control1->y),
                                         f26Dot6PosToFloat(control2->x), f26Dot6PosToFloat(control2->y),
                                         f26Dot6PosToFloat(to->x), f26Dot6PosToFloat(to->y));
            return 0;
        };
        funcs.shift = 0;
        funcs.delta = 0;

        PathContext context = { bridge, bridge.Path_construct() };

        FT_Outline *outline = &m_ftFace->glyph->outline;
        error = FT_Outline_Decompose(outline, &funcs, &context);
        if (error == FT_Err_Ok) {
            glyphPath = context.path;
        }
    }

    return glyphPath;
}

jobject Typeface::getGlyphPath(JavaBridge bridge, FT_UInt glyphID, FT_F26Dot6 typeSize, FT_Matrix *matrix, FT_Vector *delta)
{
    jobject glyphPath = nullptr;

    m_mutex.lock();

    FT_Activate_Size(m_ftSize);
    FT_Set_Char_Size(m_ftFace, 0, typeSize, 0, 0);
    FT_Set_Transform(m_ftFace, matrix, delta);

    glyphPath = getGlyphPathNoLock(bridge, glyphID);

    m_mutex.unlock();

    return glyphPath;
}

static jlong createWithAsset(JNIEnv *env, jobject obj, jobject assetManager, jstring path)
{
    if (path) {
        const char *utfChars = env->GetStringUTFChars(path, nullptr);
        AAssetManager *nativeAssetManager = AAssetManager_fromJava(env, assetManager);
        FontFile *fontFile = FontFile::createFromAsset(nativeAssetManager, utfChars);
        Typeface *typeface = Typeface::createFromFile(fontFile, 0, 0);

        env->ReleaseStringUTFChars(path, utfChars);

        return reinterpret_cast<jlong>(typeface);
    }

    return 0;
}

static jlong createWithFile(JNIEnv *env, jobject obj, jstring path)
{
    if (path) {
        const char *utfChars = env->GetStringUTFChars(path, nullptr);
        FontFile *fontFile = FontFile::createFromPath(utfChars);
        Typeface *typeface = Typeface::createFromFile(fontFile, 0, 0);

        env->ReleaseStringUTFChars(path, utfChars);

        return reinterpret_cast<jlong>(typeface);
    }

    return 0;
}

static jlong createFromStream(JNIEnv *env, jobject obj, jobject stream)
{
    if (stream) {
        FontFile *fontFile = FontFile::createFromStream(JavaBridge(env), stream);
        Typeface *typeface = Typeface::createFromFile(fontFile, 0, 0);

        return reinterpret_cast<jlong>(typeface);
    }

    return 0;
}

static void dispose(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    delete typeface;
}

static jbyteArray getTableData(JNIEnv *env, jobject obj, jlong typefaceHandle, jint tableTag)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    FT_ULong inputTag = static_cast<SFTag>(tableTag);
    FT_ULong length = 0;

    typeface->loadSfntTable(inputTag, nullptr, &length);

    if (length > 0) {
        jint dataLength = static_cast<jint>(length);
        jbyteArray dataArray = env->NewByteArray(dataLength);
        void *dataBuffer = env->GetPrimitiveArrayCritical(dataArray, nullptr);

        FT_Byte *dataBytes = static_cast<FT_Byte *>(dataBuffer);
        typeface->loadSfntTable(inputTag, dataBytes, nullptr);

        env->ReleasePrimitiveArrayCritical(dataArray, dataBuffer, 0);

        return dataArray;
    }

    return nullptr;
}

static void getNameRecordIndexes(JNIEnv *env, jobject obj, jlong typefaceHandle, jintArray indicesArray)
{
    const jint FAMILY_NAME = 0;
    const jint STYLE_NAME = 1;
    const jint FULL_NAME = 2;

    Typeface *typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    void *buffer = env->GetPrimitiveArrayCritical(indicesArray, nullptr);

    jint *values = static_cast<jint *>(buffer);
    values[FAMILY_NAME] = typeface->familyName();
    values[STYLE_NAME] = typeface->styleName();
    values[FULL_NAME] = typeface->fullName();

    env->ReleasePrimitiveArrayCritical(indicesArray, buffer, 0);
}

static jint getWeight(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    uint16_t weight = typeface->weight();

    return static_cast<jint>(weight);
}

static jint getWidth(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    uint16_t width = typeface->width();

    return static_cast<jint>(width);
}

static jint getSlope(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    uint16_t slope = typeface->slope();

    return static_cast<jint>(slope);
}

static jint getUnitsPerEm(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    uint16_t unitsPerEM = typeface->unitsPerEM();

    return static_cast<jint>(unitsPerEM);
}

static jint getAscent(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    int16_t ascent = typeface->ascent();

    return static_cast<jint>(ascent);
}

static jint getDescent(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    int16_t descent = typeface->descent();

    return static_cast<jint>(descent);
}

static jint getLeading(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    int16_t leading = typeface->leading();

    return static_cast<jint>(leading);
}

static jint getGlyphCount(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    int32_t glyphCount = typeface->glyphCount();

    return static_cast<jint>(glyphCount);
}

static jint getGlyphId(JNIEnv *env, jobject obj, jlong typefaceHandle, jint codePoint)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    FT_UInt glyphId = typeface->getGlyphID(static_cast<FT_ULong>(codePoint));

    return static_cast<jint>(glyphId);
}

static jfloat getGlyphAdvance(JNIEnv *env, jobject obj, jlong typefaceHandle, jint glyphId, jfloat typeSize, jboolean vertical)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    FT_UInt glyphIndex = static_cast<FT_UInt>(glyphId);
    FT_F26Dot6 fixedSize = toF26Dot6(typeSize);
    FT_Fixed advance = typeface->getGlyphAdvance(glyphIndex, fixedSize, vertical);

    return f16Dot16toFloat(advance);
}

static jobject getGlyphPath(JNIEnv *env, jobject obj, jlong typefaceHandle, jint glyphId, jfloat typeSize, jfloatArray matrixArray)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    FT_UInt glyphIndex = static_cast<FT_UInt>(glyphId);
    FT_F26Dot6 fixedSize = toF26Dot6(typeSize);
    FT_Matrix transform;
    FT_Vector delta;

    if (!matrixArray) {
        transform = { 0x10000, 0, 0, -0x10000 };
        delta = { 0, 0 };
    } else {
        jfloat *matrixValues = env->GetFloatArrayElements(matrixArray, nullptr);

        FT_Matrix actual = {
            toF16Dot16(matrixValues[0]), toF16Dot16(matrixValues[1]),
            toF16Dot16(matrixValues[3]), toF16Dot16(matrixValues[4]),
        };
        FT_Matrix flip = { 1, 0, 0, -1 };

        transform = {
            (actual.xx * flip.xx) + (actual.xy * flip.yx), (actual.xx * flip.xy) + (actual.xy * flip.yy),
            (actual.yx * flip.xx) + (actual.yy * flip.yx), (actual.yx * flip.xy) + (actual.yy * flip.yy)
        };
        delta = {
            toF26Dot6(matrixValues[2]), toF26Dot6(matrixValues[5]),
        };

        env->ReleaseFloatArrayElements(matrixArray, matrixValues, 0);
    }

    return typeface->getGlyphPath(JavaBridge(env), glyphIndex, fixedSize, &transform, &delta);
}

static void getBoundingBox(JNIEnv *env, jobject obj, jlong typefaceHandle, jobject rect)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    FT_Face baseFace = typeface->ftFace();
    FT_BBox bbox = baseFace->bbox;

    JavaBridge(env).Rect_set(rect,
                             static_cast<jint>(bbox.xMin), static_cast<jint>(bbox.yMin),
                             static_cast<jint>(bbox.xMax), static_cast<jint>(bbox.yMax));
}

static jint getUnderlinePosition(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    int16_t underlinePosition = typeface->underlinePosition();

    return static_cast<jint>(underlinePosition);
}

static jint getUnderlineThickness(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    int16_t underlineThickness = typeface->underlineThickness();

    return static_cast<jint>(underlineThickness);
}

static jint getStrikeoutPosition(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    int16_t strikeoutPosition = typeface->strikeoutPosition();

    return static_cast<jint>(strikeoutPosition);
}

static jint getStrikeoutThickness(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    int16_t strikeoutThickness = typeface->strikeoutThickness();

    return static_cast<jint>(strikeoutThickness);
}

static JNINativeMethod JNI_METHODS[] = {
    { "nCreateWithAsset", "(Landroid/content/res/AssetManager;Ljava/lang/String;)J", (void *)createWithAsset },
    { "nCreateWithFile", "(Ljava/lang/String;)J", (void *)createWithFile },
    { "nCreateFromStream", "(Ljava/io/InputStream;)J", (void *)createFromStream },
    { "nDispose", "(J)V", (void *)dispose },
    { "nGetTableData", "(JI)[B", (void *)getTableData },
    { "nGetNameRecordIndexes", "(J[I)V", (void *)getNameRecordIndexes },
    { "nGetWeight", "(J)I", (void *)getWeight },
    { "nGetWidth", "(J)I", (void *)getWidth },
    { "nGetSlope", "(J)I", (void *)getSlope },
    { "nGetUnitsPerEm", "(J)I", (void *)getUnitsPerEm },
    { "nGetAscent", "(J)I", (void *)getAscent },
    { "nGetDescent", "(J)I", (void *)getDescent },
    { "nGetLeading", "(J)I", (void *)getLeading },
    { "nGetGlyphCount", "(J)I", (void *)getGlyphCount },
    { "nGetGlyphId", "(JI)I", (void *)getGlyphId },
    { "nGetGlyphAdvance", "(JIFZ)F", (void *)getGlyphAdvance },
    { "nGetGlyphPath", "(JIF[F)Landroid/graphics/Path;", (void *)getGlyphPath },
    { "nGetBoundingBox", "(JLandroid/graphics/Rect;)V", (void *)getBoundingBox },
    { "nGetUnderlinePosition", "(J)I", (void *)getUnderlinePosition },
    { "nGetUnderlineThickness", "(J)I", (void *)getUnderlineThickness },
    { "nGetStrikeoutPosition", "(J)I", (void *)getStrikeoutPosition },
    { "nGetStrikeoutThickness", "(J)I", (void *)getStrikeoutThickness },
};

jint register_com_mta_tehreer_graphics_Typeface(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/graphics/Typeface", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
