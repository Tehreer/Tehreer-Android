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
#include FT_SIZES_H
#include FT_TYPES_H
}

#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <cstdint>
#include <cstring>
#include <jni.h>

#include "Convert.h"
#include "FontFile.h"
#include "JavaBridge.h"
#include "RenderableFace.h"
#include "Typeface.h"

using namespace std;
using namespace Tehreer;

Typeface *Typeface::createFromFile(FontFile *fontFile, FT_Long faceIndex, FT_Long instanceIndex)
{
    if (fontFile) {
        FT_Face ftFace = fontFile->createFace(faceIndex, instanceIndex);
        if (ftFace) {
            auto renderableFace = RenderableFace::create(ftFace);
            auto instance = IntrinsicFace::create(fontFile, renderableFace);
            auto typeface = new Typeface(instance);

            instance->release();
            renderableFace->release();

            return typeface;
        }
    }

    return nullptr;
}

Typeface::Typeface(IntrinsicFace *instance)
{
    m_instance = instance->retain();
}

Typeface::Typeface(const Typeface &typeface, IntrinsicFace *instance)
{
    m_instance = instance->retain();
    m_palette = typeface.m_palette;
}

Typeface::Typeface(const Typeface &typeface, const FT_Color *colorArray, size_t colorCount)
{
    m_instance = typeface.m_instance->retain();
    m_palette = Palette(colorArray, colorArray + colorCount);
}

Typeface::~Typeface()
{
    m_instance->release();
}

Typeface *Typeface::deriveVariation(FT_Fixed *coordArray, FT_UInt coordCount)
{
    IntrinsicFace *instance = m_instance->deriveVariation(coordArray, coordCount);
    auto typeface = new Typeface(*this, instance);

    instance->release();

    return typeface;
}

Typeface *Typeface::deriveColor(const uint32_t *colorArray, size_t colorCount)
{
    FT_Color colors[colorCount];
    Palette palette;

    for (size_t i = 0; i < colorCount; i++) {
        colors[i].blue = colorArray[i] & 0xFF;
        colors[i].green = (colorArray[i] >> 8) & 0xFF;
        colors[i].red = (colorArray[i] >> 16) & 0xFF;
        colors[i].alpha = colorArray[i] >> 24;
    }

    return new Typeface(*this, colors, colorCount);
}

void Typeface::loadSfntTable(FT_ULong tag, FT_Byte *buffer, FT_ULong *length)
{
    m_instance->loadSfntTable(tag, buffer, length);
}

int32_t Typeface::searchNameRecordIndex(uint16_t nameID)
{
    return m_instance->searchNameRecordIndex(nameID);
}

FT_UInt Typeface::getGlyphID(FT_ULong codePoint)
{
    return m_instance->getGlyphID(codePoint);
}

FT_Fixed Typeface::getGlyphAdvance(FT_UInt glyphID, FT_F26Dot6 typeSize, bool vertical)
{
    FT_Int32 loadFlags = FT_LOAD_DEFAULT;
    if (vertical) {
        loadFlags |= FT_LOAD_VERTICAL_LAYOUT;
    }

    lock();

    FT_Activate_Size(ftSize());
    FT_Set_Char_Size(ftFace(), 0, typeSize, 0, 0);
    FT_Set_Transform(ftFace(), nullptr, nullptr);

    FT_Fixed advance;
    FT_Get_Advance(ftFace(), glyphID, loadFlags, &advance);

    unlock();

    return advance;
}

jobject Typeface::getGlyphPathNoLock(JavaBridge bridge, FT_UInt glyphID)
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

jobject Typeface::getGlyphPath(JavaBridge bridge, FT_UInt glyphID, FT_F26Dot6 typeSize, FT_Matrix *matrix, FT_Vector *delta)
{
    jobject glyphPath = nullptr;

    lock();

    FT_Activate_Size(ftSize());
    FT_Set_Char_Size(ftFace(), 0, typeSize, 0, 0);
    FT_Set_Transform(ftFace(), matrix, delta);

    glyphPath = getGlyphPathNoLock(bridge, glyphID);

    unlock();

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
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    delete typeface;
}

static jlong getVariationInstance(JNIEnv *env, jobject obj, jlong typefaceHandle, jfloatArray coordinates)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    jint numCoords = env->GetArrayLength(coordinates);

    void *coordBuffer = env->GetPrimitiveArrayCritical(coordinates, nullptr);
    auto coordValues = static_cast<jfloat *>(coordBuffer);
    FT_Fixed fixedCoords[numCoords];

    for (jint i = 0; i < numCoords; i++) {
        fixedCoords[i] = toF16Dot16(coordValues[i]);
    }

    env->ReleasePrimitiveArrayCritical(coordinates, coordBuffer, 0);

    Typeface *variationInstance = typeface->deriveVariation(fixedCoords, numCoords);

    return reinterpret_cast<jlong>(variationInstance);
}

static void getVariationCoordinates(JNIEnv *env, jobject obj, jlong typefaceHandle, jfloatArray coordinates)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    FT_Face ftFace = typeface->ftFace();

    jint numCoords = env->GetArrayLength(coordinates);
    FT_Fixed fixedCoords[numCoords];

    if (FT_Get_Var_Design_Coordinates(ftFace, numCoords, fixedCoords) == FT_Err_Ok) {
        void *coordBuffer = env->GetPrimitiveArrayCritical(coordinates, nullptr);
        auto coordValues = static_cast<jfloat *>(coordBuffer);

        for (jint i = 0; i < numCoords; i++) {
            coordValues[i] = f16Dot16toFloat(fixedCoords[i]);
        }

        env->ReleasePrimitiveArrayCritical(coordinates, coordBuffer, 0);
    }
}

static jlong getColorInstance(JNIEnv *env, jobject obj, jlong typefaceHandle, jintArray colors)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    jint numColors = env->GetArrayLength(colors);

    void *colorBuffer = env->GetPrimitiveArrayCritical(colors, nullptr);
    auto colorValues = static_cast<uint32_t *>(colorBuffer);
    Typeface *variationInstance = typeface->deriveColor(colorValues, numColors);

    env->ReleasePrimitiveArrayCritical(colors, colorBuffer, 0);

    return reinterpret_cast<jlong>(variationInstance);
}

static void getAssociatedColors(JNIEnv *env, jobject obj, jlong typefaceHandle, jintArray colors)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    const Typeface::Palette &palette = *typeface->palette();

    void *colorBuffer = env->GetPrimitiveArrayCritical(colors, nullptr);
    auto colorValues = static_cast<jint *>(colorBuffer);

    for (jint i = 0; i < palette.size(); i++) {
        colorValues[i] = (palette[i].alpha << 24)
                       | (palette[i].red << 16)
                       | (palette[i].green << 8)
                       | palette[i].blue;
    }

    env->ReleasePrimitiveArrayCritical(colors, colorBuffer, 0);
}

static jbyteArray getTableData(JNIEnv *env, jobject obj, jlong typefaceHandle, jint tableTag)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    FT_ULong inputTag = static_cast<uint32_t>(tableTag);
    FT_ULong length = 0;

    typeface->loadSfntTable(inputTag, nullptr, &length);

    if (length > 0) {
        jint dataLength = static_cast<jint>(length);
        jbyteArray dataArray = env->NewByteArray(dataLength);
        void *dataBuffer = env->GetPrimitiveArrayCritical(dataArray, nullptr);

        auto dataBytes = static_cast<FT_Byte *>(dataBuffer);
        typeface->loadSfntTable(inputTag, dataBytes, nullptr);

        env->ReleasePrimitiveArrayCritical(dataArray, dataBuffer, 0);

        return dataArray;
    }

    return nullptr;
}

static jint searchNameRecordIndex(JNIEnv *env, jobject obj, jlong typefaceHandle, jint nameID)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    int32_t recordIndex = typeface->searchNameRecordIndex(nameID);

    return static_cast<jint>(recordIndex);
}

static void getNameRecordIndexes(JNIEnv *env, jobject obj, jlong typefaceHandle, jintArray indicesArray)
{
    const jint FAMILY_NAME = 0;
    const jint STYLE_NAME = 1;
    const jint FULL_NAME = 2;

    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    void *buffer = env->GetPrimitiveArrayCritical(indicesArray, nullptr);

    jint *values = static_cast<jint *>(buffer);
    values[FAMILY_NAME] = typeface->familyName();
    values[STYLE_NAME] = typeface->styleName();
    values[FULL_NAME] = typeface->fullName();

    env->ReleasePrimitiveArrayCritical(indicesArray, buffer, 0);
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
    FT_UInt glyphId = typeface->getGlyphID(static_cast<FT_ULong>(codePoint));

    return static_cast<jint>(glyphId);
}

static jfloat getGlyphAdvance(JNIEnv *env, jobject obj, jlong typefaceHandle, jint glyphId, jfloat typeSize, jboolean vertical)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    auto glyphIndex = static_cast<FT_UInt>(glyphId);
    FT_F26Dot6 fixedSize = toF26Dot6(typeSize);
    FT_Fixed advance = typeface->getGlyphAdvance(glyphIndex, fixedSize, vertical);

    return f16Dot16toFloat(advance);
}

static jobject getGlyphPath(JNIEnv *env, jobject obj, jlong typefaceHandle, jint glyphId, jfloat typeSize, jfloatArray matrixArray)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    auto glyphIndex = static_cast<FT_UInt>(glyphId);
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
    { "nDispose", "(J)V", (void *)dispose },
    { "nGetVariationInstance", "(J[F)J", (void *)getVariationInstance },
    { "nGetVariationCoordinates", "(J[F)V", (void *)getVariationCoordinates },
    { "nGetColorInstance", "(J[I)J", (void *)getColorInstance },
    { "nGetAssociatedColors", "(J[I)V", (void *)getAssociatedColors },
    { "nGetTableData", "(JI)[B", (void *)getTableData },
    { "nSearchNameRecordIndex", "(JI)I", (void *)searchNameRecordIndex },
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
