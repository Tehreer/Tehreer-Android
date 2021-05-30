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
#include FT_COLOR_H
#include FT_FREETYPE_H
#include FT_MULTIPLE_MASTERS_H
#include FT_SIZES_H
#include FT_TYPES_H
}

#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <cstddef>
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

Typeface *Typeface::createFromFile(FontFile *fontFile, FT_Long faceIndex)
{
    if (!fontFile) {
        return nullptr;
    }

    RenderableFace *renderableFace = fontFile->createRenderableFace(faceIndex);
    if (!renderableFace) {
        return nullptr;
    }

    IntrinsicFace &instance = IntrinsicFace::create(*renderableFace);
    auto typeface = new Typeface(instance);

    instance.release();
    renderableFace->release();

    return typeface;
}

Typeface::Typeface(IntrinsicFace &instance)
    : m_instance(instance.retain())
{
}

Typeface::Typeface(const Typeface &typeface, IntrinsicFace &instance)
    : m_instance(instance.retain())
{
    m_palette = typeface.m_palette;
}

Typeface::Typeface(const Typeface &typeface, const FT_Color *colorArray, size_t colorCount)
    : m_instance(typeface.m_instance.retain())
{
    setupColors(colorArray, colorCount);
}

void Typeface::setupColors(const FT_Color *colorArray, size_t colorCount)
{
    m_palette = Palette(colorArray, colorArray + colorCount);
}

Typeface::~Typeface()
{
    m_instance.release();
}

Typeface *Typeface::deriveVariation(const float *coordArray, size_t coordCount)
{
    IntrinsicFace *instance = m_instance.deriveVariation(coordArray, coordCount);
    if (!instance) {
        return nullptr;
    }

    auto typeface = new Typeface(*this, *instance);

    instance->release();

    return typeface;
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

size_t Typeface::getTableLength(uint32_t tag)
{
    return m_instance.getTableLength(tag);
}

void Typeface::getTableData(uint32_t tag, void *buffer)
{
    return m_instance.getTableData(tag, buffer);
}

int32_t Typeface::searchNameRecordIndex(uint16_t nameID)
{
    return m_instance.searchNameRecordIndex(nameID);
}

uint16_t Typeface::getGlyphID(uint32_t codePoint)
{
    return m_instance.getGlyphID(codePoint);
}

float Typeface::getGlyphAdvance(uint16_t glyphID, float typeSize, bool vertical)
{
    return m_instance.getGlyphAdvance(glyphID, typeSize, vertical);
}

jobject Typeface::unsafeGetGlyphPath(JavaBridge bridge, uint16_t glyphID)
{
    return m_instance.unsafeGetGlyphPath(bridge, glyphID);
}

jobject Typeface::getGlyphPath(JavaBridge bridge, uint16_t glyphID, float typeSize, float *transform)
{
    return m_instance.getGlyphPath(bridge, glyphID, typeSize, transform);
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

void Tehreer::setupColors(JNIEnv *env, jobject obj, jlong typefaceHandle, jintArray colors)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);

    jint colorCount = env->GetArrayLength(colors);
    void *colorBuffer = env->GetPrimitiveArrayCritical(colors, nullptr);

    auto *intColors = static_cast<uint32_t *>(colorBuffer);
    FT_Color colorArray[colorCount];

    for (jint i = 0; i < colorCount; i++) {
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

    Typeface *variationInstance = typeface->deriveVariation(coordValues, numCoords);

    env->ReleaseFloatArrayElements(coordinates, coordValues, 0);

    return reinterpret_cast<jlong>(variationInstance);
}

static void getVariationCoordinates(JNIEnv *env, jobject obj, jlong typefaceHandle, jfloatArray coordinates)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    const CoordArray *values = typeface->coordinates();

    void *coordBuffer = env->GetPrimitiveArrayCritical(coordinates, nullptr);
    auto coordValues = static_cast<jfloat *>(coordBuffer);

    for (jint i = 0; i < values->size(); i++) {
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
        colorValues[i] = toIntColor(palette[i]);
    }

    env->ReleasePrimitiveArrayCritical(colors, colorBuffer, 0);
}

static jbyteArray getTableData(JNIEnv *env, jobject obj, jlong typefaceHandle, jint tableTag)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);

    size_t length = typeface->getTableLength(tableTag);
    if (length == 0) {
        return nullptr;
    }

    jbyteArray array = env->NewByteArray(length);
    void *buffer = env->GetPrimitiveArrayCritical(array, nullptr);
    typeface->getTableData(tableTag, buffer);

    env->ReleasePrimitiveArrayCritical(array, buffer, 0);

    return array;
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

static jfloat getGlyphAdvance(JNIEnv *env, jobject obj, jlong typefaceHandle,
    jint glyphId, jfloat typeSize, jboolean vertical)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);

    return typeface->getGlyphAdvance(glyphId, typeSize, vertical);
}

static jobject getGlyphPath(JNIEnv *env, jobject obj, jlong typefaceHandle, jint glyphId, jfloat typeSize, jfloatArray matrixArray)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    auto glyphIndex = static_cast<FT_UInt>(glyphId);

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
    { "nSetupColors", "(J[I)V", (void *)setupColors },
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
