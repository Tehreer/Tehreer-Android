/*
 * Copyright (C) 2016-2021 Muhammad Tayyab Akram
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
#include FT_COLOR_H
#include FT_FREETYPE_H
#include FT_MULTIPLE_MASTERS_H
#include FT_SFNT_NAMES_H
#include FT_SIZES_H
#include FT_STROKER_H
#include FT_TRUETYPE_TABLES_H
#include FT_TYPES_H
}

#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <cstddef>
#include <cstdint>
#include <cstring>
#include <jni.h>
#include <mutex>

#include "Convert.h"
#include "FontFile.h"
#include "FreeType.h"
#include "JavaBridge.h"
#include "RenderableFace.h"
#include "SfntTables.h"
#include "ShapableFace.h"
#include "Typeface.h"

using namespace std;
using namespace Tehreer;
using namespace Tehreer::SFNT::head;
using namespace Tehreer::SFNT::name;
using namespace Tehreer::SFNT::OS2;

using FaceLock = lock_guard<RenderableFace>;

/**
 * NOTE: The caller needs to lock the typeface before invoking this function.
 */
static int32_t searchEnglishNameRecordIndex(FT_Face face, uint16_t nameID)
{
    FT_UInt nameCount = FT_Get_Sfnt_Name_Count(face);
    int32_t candidate = -1;

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
                return static_cast<int32_t>(i);
            }

            if (candidate == -1 || record.platform_id == PlatformID::MACINTOSH) {
                candidate = static_cast<int32_t>(i);
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

static inline uint16_t variableWeightToStandard(float value)
{
    if (value < 1) {
        return 1;
    }
    if (value > 1000) {
        return 1000;
    }

    return static_cast<uint16_t>(value);
}

static inline uint16_t variableWidthToStandard(float value)
{
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

static inline Typeface::Slope variableItalicToSlope(float value)
{
    return value >= 1.0 ? Typeface::Slope::ITALIC : Typeface::Slope::PLAIN;
}

static inline Typeface::Slope variableSlantToSlope(float value)
{
    return value != 0.0 ? Typeface::Slope::OBLIQUE : Typeface::Slope::PLAIN;
}

Typeface *Typeface::createFromFile(FontFile *fontFile, FT_Long faceIndex)
{
    if (!fontFile) {
        return nullptr;
    }

    RenderableFace *renderableFace = fontFile->createRenderableFace(faceIndex);
    if (!renderableFace) {
        return nullptr;
    }

    auto typeface = new Typeface(*renderableFace);

    renderableFace->release();

    return typeface;
}

Typeface::Typeface(RenderableFace &renderableFace)
    : m_renderableFace(renderableFace.retain())
    , m_ftSize(nullptr)
    , m_ftStroker(nullptr)
    , m_shapableFace(nullptr)
    , m_description(Description())
    , m_defaults(DefaultProperties())
    , m_strikeoutPosition(0)
    , m_strikeoutThickness(0)
    , m_palette({})
{
    setupSize();
    setupDescription();
    setupStrikeout();
    setupHarfBuzz();
}

Typeface::Typeface(const Typeface &parent, RenderableFace &renderableFace)
    : m_renderableFace(renderableFace.retain())
    , m_ftSize(nullptr)
    , m_ftStroker(nullptr)
    , m_shapableFace(nullptr)
    , m_description(parent.m_defaults.description)
    , m_defaults(parent.m_defaults)
    , m_strikeoutPosition(0)
    , m_strikeoutThickness(0)
    , m_palette(parent.m_palette)
{
    setupSize();
    setupHarfBuzz(parent.m_shapableFace);
}

Typeface::Typeface(const Typeface &parent, const FT_Color *colorArray, size_t colorCount)
    : m_renderableFace(parent.renderableFace().retain())
    , m_ftSize(nullptr)
    , m_ftStroker(nullptr)
    , m_shapableFace(&parent.m_shapableFace->retain())
    , m_description(parent.m_description)
    , m_defaults(parent.m_defaults)
    , m_strikeoutPosition(parent.m_strikeoutPosition)
    , m_strikeoutThickness(parent.m_strikeoutThickness)
    , m_palette({})
{
    setupSize();
    setupColors(colorArray, colorCount);
}

void Typeface::setupCoordinates(const float *coordArray, size_t coordCount)
{
    m_renderableFace.setupCoordinates(coordArray, coordCount);
}

void Typeface::setupSize()
{
    FT_New_Size(m_renderableFace.ftFace(), &m_ftSize);
}

void Typeface::setupDescription()
{
    FT_Face ftFace = m_renderableFace.ftFace();
    auto os2Table = static_cast<TT_OS2 *>(FT_Get_Sfnt_Table(ftFace, FT_SFNT_OS2));
    auto headTable = static_cast<TT_Header *>(FT_Get_Sfnt_Table(ftFace, FT_SFNT_HEAD));

    Description description;
    description.familyName = searchFamilyNameRecordIndex(ftFace, os2Table);
    description.styleName = searchStyleNameRecordIndex(ftFace, os2Table);
    description.fullName = searchFullNameRecordIndex(ftFace);

    if (os2Table) {
        description.weight = os2Table->usWeightClass;
        description.width = os2Table->usWidthClass;

        if (os2Table->fsSelection & FSSelection::OBLIQUE) {
            description.slope = Slope::OBLIQUE;
        } else if (os2Table->fsSelection & FSSelection::ITALIC) {
            description.slope = Slope::ITALIC;
        }
    } else if (headTable) {
        if (headTable->Mac_Style & MacStyle::BOLD) {
            description.weight = Weight::BOLD;
        }

        if (headTable->Mac_Style & MacStyle::CONDENSED) {
            description.width = Width::CONDENSED;
        } else if (headTable->Mac_Style & MacStyle::EXTENDED) {
            description.width = Width::EXPANDED;
        }

        if (headTable->Mac_Style & MacStyle::ITALIC) {
            description.slope = Slope::ITALIC;
        }
    }

    m_defaults.description = description;
    m_description = description;
}

void Typeface::setupStrikeout()
{
    FT_Face ftFace = m_renderableFace.ftFace();
    auto os2Table = static_cast<TT_OS2 *>(FT_Get_Sfnt_Table(ftFace, FT_SFNT_OS2));

    if (os2Table) {
        m_strikeoutPosition = os2Table->yStrikeoutPosition;
        m_strikeoutThickness = os2Table->yStrikeoutSize;
    }
}

void Typeface::setupHarfBuzz(ShapableFace *parent)
{
    if (parent) {
        m_shapableFace = &parent->deriveVariation(m_renderableFace);
    } else {
        m_shapableFace = &ShapableFace::create(m_renderableFace);
    }
}

void Typeface::setupVariation(float italValue, float slntValue, float wdthValue, float wghtValue)
{
    if (!isnan(italValue)) {
        m_description.slope = variableItalicToSlope(italValue);
    }
    if (!isnan(slntValue)) {
        m_description.slope = variableSlantToSlope(slntValue);
    }
    if (!isnan(wdthValue)) {
        m_description.width = variableWidthToStandard(wdthValue);
    }
    if (!isnan(wghtValue)) {
        m_description.weight = variableWeightToStandard(wghtValue);
    }
}

void Typeface::setupColors(const FT_Color *colorArray, size_t colorCount)
{
    m_palette = Palette(colorArray, colorArray + colorCount);
}

Typeface::~Typeface()
{
    m_shapableFace->release();

    if (m_ftStroker) {
        FT_Stroker_Done(m_ftStroker);
    }
    if (m_ftSize) {
        FaceLock lock(m_renderableFace);
        FT_Done_Size(m_ftSize);
    }

    m_renderableFace.release();
}

Typeface *Typeface::deriveVariation(const float *coordArray, size_t coordCount)
{
    RenderableFace *renderableFace = m_renderableFace.deriveVariation(coordArray, coordCount);
    if (!renderableFace) {
        return nullptr;
    }

    auto instance = new Typeface(*this, *renderableFace);

    renderableFace->release();

    return instance;
}

Typeface *Typeface::deriveColor(const uint32_t *colorArray, size_t colorCount)
{
    FT_Color colors[colorCount];
    Palette palette;

    for (size_t i = 0; i < colorCount; i++) {
        colors[i] = toFTColor(colorArray[i]);
    }

    return new Typeface(*this, colors, colorCount);
}

FT_Stroker Typeface::ftStroker()
{
    if (!m_ftStroker) {
        m_mutex.lock();

        if (!m_ftStroker) {
            /*
             * There is no need to lock 'library' as it is only taken to have access to FreeType's
             * memory handling functions.
             */
            FT_Stroker_New(FreeType::library(), &m_ftStroker);
        }

        m_mutex.unlock();
    }

    return m_ftStroker;
}

size_t Typeface::getTableLength(uint32_t tag)
{
    FaceLock lock(m_renderableFace);
    FT_Face ftFace = m_renderableFace.ftFace();

    FT_ULong length = 0;
    FT_Load_Sfnt_Table(ftFace, tag, 0, nullptr, &length);

    return length;
}

void Typeface::getTableData(uint32_t tag, void *buffer)
{
    FaceLock lock(m_renderableFace);
    FT_Face ftFace = m_renderableFace.ftFace();

    auto ftBuffer = reinterpret_cast<FT_Byte *>(buffer);
    FT_Load_Sfnt_Table(ftFace, tag, 0, ftBuffer, nullptr);
}

int32_t Typeface::searchNameIndex(uint16_t nameID)
{
    FaceLock lock(m_renderableFace);

    FT_Face ftFace = m_renderableFace.ftFace();
    int32_t recordIndex = searchEnglishNameRecordIndex(ftFace, nameID);

    return recordIndex;
}

jobject Typeface::getNameRecord(const JavaBridge &javaBridge, int32_t nameIndex)
{
    lock();

    FT_SfntName sfntName;
    FT_Get_Sfnt_Name(ftFace(), static_cast<FT_UInt>(nameIndex), &sfntName);

    unlock();

    auto buffer = reinterpret_cast<jbyte *>(sfntName.string);
    auto length = static_cast<jint>(sfntName.string_len);

    JNIEnv *env = javaBridge.env();
    jbyteArray bytes = env->NewByteArray(length);
    env->SetByteArrayRegion(bytes, 0, length, buffer);

    return javaBridge.NameTableRecord_construct(sfntName.name_id, sfntName.platform_id,
                                                sfntName.language_id, sfntName.encoding_id, bytes);
}

jstring Typeface::getNameString(const JavaBridge &javaBridge, int32_t nameIndex)
{
    jobject nameRecord = getNameRecord(javaBridge, nameIndex);
    jstring name = javaBridge.NameTableRecord_string(nameRecord);

    return name;
}

uint16_t Typeface::getGlyphID(uint32_t codePoint)
{
    FaceLock lock(m_renderableFace);

    FT_Face ftFace = m_renderableFace.ftFace();
    FT_UInt glyphID = FT_Get_Char_Index(ftFace, codePoint);

    return static_cast<uint16_t>(glyphID);
}

float Typeface::getGlyphAdvance(uint16_t glyphID, float typeSize, bool vertical)
{
    FT_Int32 loadFlags = FT_LOAD_DEFAULT;
    if (vertical) {
        loadFlags |= FT_LOAD_VERTICAL_LAYOUT;
    }

    FaceLock lock(m_renderableFace);
    FT_Face ftFace = m_renderableFace.ftFace();

    FT_Activate_Size(ftSize());
    FT_Set_Char_Size(ftFace, 0, toF26Dot6(typeSize), 0, 0);
    FT_Set_Transform(ftFace, nullptr, nullptr);

    FT_Fixed advance;
    FT_Get_Advance(ftFace, glyphID, loadFlags, &advance);

    return f16Dot16toFloat(advance);
}

jobject Typeface::unsafeGetGlyphPath(JavaBridge bridge, uint16_t glyphID)
{
    jobject glyphPath = nullptr;

    FT_Error error = FT_Load_Glyph(ftFace(), glyphID, FT_LOAD_NO_BITMAP);
    if (error == FT_Err_Ok) {
        struct PathContext {
            JavaBridge bridge;
            jobject path;
        };

        FT_Outline_Funcs funcs;
        funcs.move_to = [](const FT_Vector *to, void *user) -> int
        {
            auto context = reinterpret_cast<PathContext *>(user);
            context->bridge.Path_moveTo(context->path,
                                        f26Dot6PosToFloat(to->x), f26Dot6PosToFloat(to->y));
            return 0;
        };
        funcs.line_to = [](const FT_Vector *to, void *user) -> int
        {
            auto context = reinterpret_cast<PathContext *>(user);
            context->bridge.Path_lineTo(context->path,
                                        f26Dot6PosToFloat(to->x), f26Dot6PosToFloat(to->y));
            return 0;
        };
        funcs.conic_to = [](const FT_Vector *control1, const FT_Vector *to, void *user) -> int
        {
            auto context = reinterpret_cast<PathContext *>(user);
            context->bridge.Path_quadTo(context->path,
                                        f26Dot6PosToFloat(control1->x), f26Dot6PosToFloat(control1->y),
                                        f26Dot6PosToFloat(to->x), f26Dot6PosToFloat(to->y));
            return 0;
        };
        funcs.cubic_to = [](const FT_Vector *control1, const FT_Vector *control2, const FT_Vector *to, void *user) -> int
        {
            auto context = reinterpret_cast<PathContext *>(user);
            context->bridge.Path_cubicTo(context->path,
                                         f26Dot6PosToFloat(control1->x), f26Dot6PosToFloat(control1->y),
                                         f26Dot6PosToFloat(control2->x), f26Dot6PosToFloat(control2->y),
                                         f26Dot6PosToFloat(to->x), f26Dot6PosToFloat(to->y));
            return 0;
        };
        funcs.shift = 0;
        funcs.delta = 0;

        PathContext context = { bridge, bridge.Path_construct() };

        FT_Outline *outline = &ftFace()->glyph->outline;
        error = FT_Outline_Decompose(outline, &funcs, &context);
        if (error == FT_Err_Ok) {
            glyphPath = context.path;
        }
    }

    return glyphPath;
}

jobject Typeface::getGlyphPath(JavaBridge bridge, uint16_t glyphID, float typeSize, float *transform)
{
    FT_Matrix matrix;
    FT_Vector delta;

    if (!transform) {
        matrix = { 0x10000, 0, 0, -0x10000 };
        delta = { 0, 0 };
    } else {
        FT_Matrix actual = {
            toF16Dot16(transform[0]), toF16Dot16(transform[1]),
            toF16Dot16(transform[3]), toF16Dot16(transform[4]),
        };
        FT_Matrix flip = { 1, 0, 0, -1 };

        matrix = {
            (actual.xx * flip.xx) + (actual.xy * flip.yx), (actual.xx * flip.xy) + (actual.xy * flip.yy),
            (actual.yx * flip.xx) + (actual.yy * flip.yx), (actual.yx * flip.xy) + (actual.yy * flip.yy)
        };
        delta = {
                toF26Dot6(transform[2]), toF26Dot6(transform[5]),
        };
    }

    FaceLock lock(m_renderableFace);
    FT_Face ftFace = m_renderableFace.ftFace();

    FT_Activate_Size(ftSize());
    FT_Set_Char_Size(ftFace, 0, toF26Dot6(typeSize), 0, 0);
    FT_Set_Transform(ftFace, &matrix, &delta);

    return unsafeGetGlyphPath(bridge, glyphID);
}

static jlong createWithAsset(JNIEnv *env, jobject obj, jobject assetManager, jstring path)
{
    if (path) {
        const char *utfChars = env->GetStringUTFChars(path, nullptr);
        AAssetManager *nativeAssetManager = AAssetManager_fromJava(env, assetManager);
        FontFile *fontFile = FontFile::createFromAsset(nativeAssetManager, utfChars);
        Typeface *typeface = Typeface::createFromFile(fontFile, 0);

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
        Typeface *typeface = Typeface::createFromFile(fontFile, 0);

        env->ReleaseStringUTFChars(path, utfChars);

        return reinterpret_cast<jlong>(typeface);
    }

    return 0;
}

static jlong createFromStream(JNIEnv *env, jobject obj, jobject stream)
{
    if (stream) {
        FontFile *fontFile = FontFile::createFromStream(JavaBridge(env), stream);
        Typeface *typeface = Typeface::createFromFile(fontFile, 0);

        return reinterpret_cast<jlong>(typeface);
    }

    return 0;
}

void setupCoordinates(JNIEnv *env, jobject obj, jlong typefaceHandle, jfloatArray coordinates)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);

    jint coordLength = env->GetArrayLength(coordinates);
    void *coordBuffer = env->GetPrimitiveArrayCritical(coordinates, nullptr);

    auto *coordArray = static_cast<float *>(coordBuffer);
    auto coordCount = static_cast<size_t>(coordLength);

    typeface->setupCoordinates(coordArray, coordCount);

    env->ReleasePrimitiveArrayCritical(coordinates, coordBuffer, 0);
}

void setupStrikeout(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    typeface->setupStrikeout();
}

void setupVariation(JNIEnv *env, jobject obj, jlong typefaceHandle,
    jfloat italValue, jfloat slntValue, jfloat wdthValue, jfloat wghtValue)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    typeface->setupVariation(italValue, slntValue, wdthValue, wghtValue);
}

void setupColors(JNIEnv *env, jobject obj, jlong typefaceHandle, jintArray colors)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);

    jint numColors = env->GetArrayLength(colors);
    void *colorBuffer = env->GetPrimitiveArrayCritical(colors, nullptr);

    auto *intColors = static_cast<uint32_t *>(colorBuffer);
    auto colorCount = static_cast<size_t>(numColors);

    FT_Color colorArray[colorCount];

    for (size_t i = 0; i < colorCount; i++) {
        colorArray[i] = toFTColor(intColors[i]);
    }

    typeface->setupColors(colorArray, colorCount);

    env->ReleasePrimitiveArrayCritical(colors, colorBuffer, 0);
}

static void dispose(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    delete typeface;
}

static jlong getVariationInstance(JNIEnv *env, jobject obj, jlong typefaceHandle, jfloatArray coordinates)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);

    jint numCoords = env->GetArrayLength(coordinates);
    jfloat *coordValues = env->GetFloatArrayElements(coordinates, nullptr);

    auto coordCount = static_cast<size_t>(numCoords);
    Typeface *variationInstance = typeface->deriveVariation(coordValues, coordCount);

    env->ReleaseFloatArrayElements(coordinates, coordValues, 0);

    return reinterpret_cast<jlong>(variationInstance);
}

static void getVariationCoordinates(JNIEnv *env, jobject obj, jlong typefaceHandle, jfloatArray coordinates)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    const CoordArray *values = typeface->coordinates();

    void *coordBuffer = env->GetPrimitiveArrayCritical(coordinates, nullptr);
    auto coordValues = static_cast<jfloat *>(coordBuffer);

    for (size_t i = 0; i < values->size(); i++) {
        coordValues[i] = values->at(i);
    }

    env->ReleasePrimitiveArrayCritical(coordinates, coordBuffer, 0);
}

static jlong getColorInstance(JNIEnv *env, jobject obj, jlong typefaceHandle, jintArray colors)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);

    jint numColors = env->GetArrayLength(colors);
    void *colorBuffer = env->GetPrimitiveArrayCritical(colors, nullptr);

    auto colorValues = static_cast<uint32_t *>(colorBuffer);
    auto colorCount = static_cast<size_t>(numColors);

    Typeface *variationInstance = typeface->deriveColor(colorValues, colorCount);

    env->ReleasePrimitiveArrayCritical(colors, colorBuffer, 0);

    return reinterpret_cast<jlong>(variationInstance);
}

static void getAssociatedColors(JNIEnv *env, jobject obj, jlong typefaceHandle, jintArray colors)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    const Typeface::Palette &palette = *typeface->palette();

    void *colorBuffer = env->GetPrimitiveArrayCritical(colors, nullptr);
    auto colorValues = static_cast<jint *>(colorBuffer);

    for (size_t i = 0; i < palette.size(); i++) {
        uint32_t currentColor = toIntColor(palette[i]);
        colorValues[i] = static_cast<jint>(currentColor);
    }

    env->ReleasePrimitiveArrayCritical(colors, colorBuffer, 0);
}

static jbyteArray getTableData(JNIEnv *env, jobject obj, jlong typefaceHandle, jint tableTag)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    auto inputTag = static_cast<uint32_t>(tableTag);

    size_t tableLength = typeface->getTableLength(inputTag);
    if (tableLength == 0) {
        return nullptr;
    }

    jint dataLength = static_cast<jint>(tableLength);
    jbyteArray dataArray = env->NewByteArray(dataLength);
    void *dataBuffer = env->GetPrimitiveArrayCritical(dataArray, nullptr);

    typeface->getTableData(inputTag, dataBuffer);

    env->ReleasePrimitiveArrayCritical(dataArray, dataBuffer, 0);

    return dataArray;
}

static jstring searchNameString(JNIEnv *env, jobject obj, jlong typefaceHandle, jint nameID)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    auto inputID = static_cast<uint16_t>(nameID);
    int32_t nameIndex = typeface->searchNameIndex(inputID);

    return typeface->getNameString(JavaBridge(env), nameIndex);
}

static jstring getDefaultFamilyName(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    int32_t nameIndex = typeface->familyName();

    return typeface->getNameString(JavaBridge(env), nameIndex);
}

static jstring getDefaultStyleName(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    int32_t nameIndex = typeface->styleName();

    return typeface->getNameString(JavaBridge(env), nameIndex);
}

static jstring getDefaultFullName(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    int32_t nameIndex = typeface->fullName();

    return typeface->getNameString(JavaBridge(env), nameIndex);
}

static jint getWeight(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    uint16_t weight = typeface->weight();

    return static_cast<jint>(weight);
}

static jint getWidth(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    uint16_t width = typeface->width();

    return static_cast<jint>(width);
}

static jint getSlope(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    uint16_t slope = typeface->slope();

    return static_cast<jint>(slope);
}

static jint getUnitsPerEm(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    uint16_t unitsPerEM = typeface->unitsPerEM();

    return static_cast<jint>(unitsPerEM);
}

static jint getAscent(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    int16_t ascent = typeface->ascent();

    return static_cast<jint>(ascent);
}

static jint getDescent(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    int16_t descent = typeface->descent();

    return static_cast<jint>(descent);
}

static jint getLeading(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    int16_t leading = typeface->leading();

    return static_cast<jint>(leading);
}

static jint getGlyphCount(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    int32_t glyphCount = typeface->glyphCount();

    return static_cast<jint>(glyphCount);
}

static jint getGlyphId(JNIEnv *env, jobject obj, jlong typefaceHandle, jint codePoint)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    auto charCode = static_cast<uint32_t>(codePoint);
    uint16_t glyphId = typeface->getGlyphID(charCode);

    return static_cast<jint>(glyphId);
}

static jfloat getGlyphAdvance(JNIEnv *env, jobject obj, jlong typefaceHandle,
    jint glyphId, jfloat typeSize, jboolean vertical)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    auto glyphIndex = static_cast<uint16_t>(glyphId);

    return typeface->getGlyphAdvance(glyphIndex, typeSize, vertical);
}

static jobject getGlyphPath(JNIEnv *env, jobject obj, jlong typefaceHandle, jint glyphId, jfloat typeSize, jfloatArray matrixArray)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    auto glyphIndex = static_cast<uint16_t>(glyphId);

    jfloat *transform = env->GetFloatArrayElements(matrixArray, nullptr);
    jobject glyphPath = typeface->getGlyphPath(JavaBridge(env), glyphIndex, typeSize, transform);

    env->ReleaseFloatArrayElements(matrixArray, transform, 0);

    return glyphPath;
}

static void getBoundingBox(JNIEnv *env, jobject obj, jlong typefaceHandle, jobject rect)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    FT_Face baseFace = typeface->ftFace();
    FT_BBox bbox = baseFace->bbox;

    JavaBridge(env).Rect_set(rect,
                             static_cast<jint>(bbox.xMin), static_cast<jint>(bbox.yMin),
                             static_cast<jint>(bbox.xMax), static_cast<jint>(bbox.yMax));
}

static jint getUnderlinePosition(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    int16_t underlinePosition = typeface->underlinePosition();

    return static_cast<jint>(underlinePosition);
}

static jint getUnderlineThickness(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    int16_t underlineThickness = typeface->underlineThickness();

    return static_cast<jint>(underlineThickness);
}

static jint getStrikeoutPosition(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    int16_t strikeoutPosition = typeface->strikeoutPosition();

    return static_cast<jint>(strikeoutPosition);
}

static jint getStrikeoutThickness(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    int16_t strikeoutThickness = typeface->strikeoutThickness();

    return static_cast<jint>(strikeoutThickness);
}

static JNINativeMethod JNI_METHODS[] = {
    { "nCreateWithAsset", "(Landroid/content/res/AssetManager;Ljava/lang/String;)J", (void *)createWithAsset },
    { "nCreateWithFile", "(Ljava/lang/String;)J", (void *)createWithFile },
    { "nCreateFromStream", "(Ljava/io/InputStream;)J", (void *)createFromStream },
    { "nSetupCoordinates", "(J[F)V", (void *)setupCoordinates },
    { "nSetupStrikeout", "(J)V", (void *)setupStrikeout },
    { "nSetupVariation", "(JFFFF)V", (void *)setupVariation },
    { "nSetupColors", "(J[I)V", (void *)setupColors },
    { "nDispose", "(J)V", (void *)dispose },
    { "nGetVariationInstance", "(J[F)J", (void *)getVariationInstance },
    { "nGetVariationCoordinates", "(J[F)V", (void *)getVariationCoordinates },
    { "nGetColorInstance", "(J[I)J", (void *)getColorInstance },
    { "nGetAssociatedColors", "(J[I)V", (void *)getAssociatedColors },
    { "nGetTableData", "(JI)[B", (void *)getTableData },
    { "nSearchNameString", "(JI)Ljava/lang/String;", (void *)searchNameString },
    { "nGetDefaultFamilyName", "(J)Ljava/lang/String;", (void *)getDefaultFamilyName },
    { "nGetDefaultStyleName", "(J)Ljava/lang/String;", (void *)getDefaultStyleName },
    { "nGetDefaultFullName", "(J)Ljava/lang/String;", (void *)getDefaultFullName },
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
