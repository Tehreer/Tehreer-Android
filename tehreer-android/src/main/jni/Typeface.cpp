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
#include <string>
#include <vector>

#include <Tehreer/TRTypeface.h>

#include "Convert.h"
#include "FontFile.h"
#include "FreeType.h"
#include "JavaBridge.h"
#include "RenderableFace.h"
#include "SfntTables.h"
#include "Typeface.h"

using namespace std;
using namespace Tehreer;
using namespace Tehreer::SFNT::head;
using namespace Tehreer::SFNT::name;
using namespace Tehreer::SFNT::OS2;

using FaceLock = lock_guard<RenderableFace>;

Typeface *Typeface::createFromFile(FontFile *fontFile, FT_Long faceIndex)
{
    if (!fontFile) {
        return nullptr;
    }

    TRTypefaceRef core = TRTypefaceCreate(fontFile->core(), static_cast<TRUInteger>(faceIndex));
    if (!core) {
        return nullptr;
    }

    RenderableFace *renderableFace = fontFile->createRenderableFace(faceIndex);
    if (!renderableFace) {
        TRTypefaceRelease(core);
        return nullptr;
    }

    auto typeface = new Typeface(core, *renderableFace);

    renderableFace->release();

    return typeface;
}

Typeface::Typeface(TRTypefaceRef core, RenderableFace &renderableFace)
    : m_core(core)
    , m_renderableFace(renderableFace.retain())
{
}

Typeface::Typeface(const Typeface &parent, TRTypefaceRef core)
    : m_core(core)
    , m_renderableFace(parent.renderableFace().retain())
{
}

Typeface::~Typeface()
{
    m_renderableFace.release();

    TRTypefaceRelease(m_core);
}

Typeface *Typeface::deriveVariation(const float *coordArray, size_t coordCount)
{
    TRTypefaceRef core = TRTypefaceCreateWithVariation(m_core, coordArray, coordCount);
    if (!core) {
        return nullptr;
    }

    return new Typeface(*this, core);
}

Typeface *Typeface::deriveColor(const uint32_t *colorArray, size_t colorCount)
{
    TRTypefaceRef core = TRTypefaceCreateWithColors(m_core, colorArray, colorCount);
    if (!core) {
        return nullptr;
    }

    return new Typeface(*this, core);
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
    return TRTypefaceGetGlyphID(m_core, codePoint);
}

float Typeface::getGlyphAdvance(uint16_t glyphID, float typeSize, bool vertical)
{
    return TRTypefaceGetGlyphAdvance(m_core, glyphID, typeSize, vertical ? TRTrue : TRFalse);
}

jobject Typeface::getGlyphPath(JavaBridge bridge, uint16_t glyphID, float typeSize, float *transform)
{
    TRPathRef corePath = TRTypefaceCreateGlyphPath(m_core, glyphID, typeSize);
    if (!corePath) {
        return nullptr;
    }

    /* The path of Core points downward, which is how the transform expects it. */
    TRAffineTransform matrix = { 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f };
    if (transform) {
        matrix.a = transform[0];
        matrix.c = transform[1];
        matrix.tx = transform[2];
        matrix.b = transform[3];
        matrix.d = transform[4];
        matrix.ty = transform[5];
    }

    struct PathContext {
        JavaBridge bridge;
        jobject path;
    };

    TRPathCallbacks callbacks = {};
    callbacks.moveTo = [](void *user, TRFloat x, TRFloat y) {
        auto context = reinterpret_cast<PathContext *>(user);
        context->bridge.Path_moveTo(context->path, x, y);
    };
    callbacks.lineTo = [](void *user, TRFloat x, TRFloat y) {
        auto context = reinterpret_cast<PathContext *>(user);
        context->bridge.Path_lineTo(context->path, x, y);
    };
    callbacks.quadTo = [](void *user, TRFloat controlX, TRFloat controlY, TRFloat x, TRFloat y) {
        auto context = reinterpret_cast<PathContext *>(user);
        context->bridge.Path_quadTo(context->path, controlX, controlY, x, y);
    };
    callbacks.cubicTo = [](void *user, TRFloat control1X, TRFloat control1Y,
                           TRFloat control2X, TRFloat control2Y, TRFloat x, TRFloat y) {
        auto context = reinterpret_cast<PathContext *>(user);
        context->bridge.Path_cubicTo(context->path, control1X, control1Y,
                                     control2X, control2Y, x, y);
    };

    PathContext context = { bridge, bridge.Path_construct() };
    TRPathEnumerate(corePath, &matrix, &callbacks, &context);

    TRPathRelease(corePath);

    return context.path;
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

static void dispose(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    delete typeface;
}

static jstring toJavaString(JNIEnv *env, const TRStringView *view)
{
    if (!view || !view->buffer) {
        return env->NewStringUTF("");
    }

    switch (view->encoding) {
    case TRStringEncodingUTF16:
        return env->NewString(static_cast<const jchar *>(view->buffer),
                              static_cast<jsize>(view->length));

    case TRStringEncodingUTF8: {
        /* The string is not null-terminated, so it has to be copied. */
        std::string utf8(static_cast<const char *>(view->buffer), view->length);
        return env->NewStringUTF(utf8.c_str());
    }

    default:
        return env->NewStringUTF("");
    }
}

static jlong getCoreHandle(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    return reinterpret_cast<jlong>(typeface->core());
}

static jstring getFamilyName(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    return toJavaString(env, TRTypefaceGetFamilyName(typeface->core()));
}

static jstring getStyleName(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    return toJavaString(env, TRTypefaceGetSubfamilyName(typeface->core()));
}

static jstring getFullName(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    return toJavaString(env, TRTypefaceGetFullName(typeface->core()));
}

static jint getWeight(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    return static_cast<jint>(TRTypefaceGetWeight(typeface->core()));
}

static jint getWidth(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    return static_cast<jint>(TRTypefaceGetWidth(typeface->core()));
}

static jint getSlope(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    return static_cast<jint>(TRTypefaceGetSlope(typeface->core()));
}

static jint getVariationAxisCount(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    return static_cast<jint>(TRTypefaceGetVariationAxisCount(typeface->core()));
}

/* Fills the tags and flags in pairs, the minimum, default and maximum values in triples. */
static void getVariationAxes(JNIEnv *env, jobject obj, jlong typefaceHandle,
    jintArray tagsAndFlags, jfloatArray values, jobjectArray names)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    const TRVariationAxis *axes = TRTypefaceGetVariationAxesPtr(typeface->core());
    TRUInteger count = TRTypefaceGetVariationAxisCount(typeface->core());

    for (TRUInteger i = 0; i < count; i++) {
        jint pair[2] = { static_cast<jint>(axes[i].tag), static_cast<jint>(axes[i].flags) };
        jfloat triple[3] = { axes[i].minValue, axes[i].defaultValue, axes[i].maxValue };

        env->SetIntArrayRegion(tagsAndFlags, static_cast<jsize>(i * 2), 2, pair);
        env->SetFloatArrayRegion(values, static_cast<jsize>(i * 3), 3, triple);

        jstring name = toJavaString(env, axes[i].name);
        env->SetObjectArrayElement(names, static_cast<jsize>(i), name);
        env->DeleteLocalRef(name);
    }
}

static jint getNamedStyleCount(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    return static_cast<jint>(TRTypefaceGetNamedStyleCount(typeface->core()));
}

/* Fills the coordinates of each style one after the other. A missing post script name is null. */
static void getNamedStyles(JNIEnv *env, jobject obj, jlong typefaceHandle,
    jobjectArray styleNames, jobjectArray postScriptNames, jfloatArray coordinates)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    const TRNamedStyle *styles = TRTypefaceGetNamedStylesPtr(typeface->core());
    TRUInteger count = TRTypefaceGetNamedStyleCount(typeface->core());
    jsize offset = 0;

    for (TRUInteger i = 0; i < count; i++) {
        jstring styleName = toJavaString(env, styles[i].subfamilyName);
        env->SetObjectArrayElement(styleNames, static_cast<jsize>(i), styleName);
        env->DeleteLocalRef(styleName);

        if (styles[i].postScriptName) {
            jstring postScriptName = toJavaString(env, styles[i].postScriptName);
            env->SetObjectArrayElement(postScriptNames, static_cast<jsize>(i), postScriptName);
            env->DeleteLocalRef(postScriptName);
        }

        auto coordCount = static_cast<jsize>(styles[i].coordinateCount);
        env->SetFloatArrayRegion(coordinates, offset, coordCount, styles[i].coordinatesPtr);
        offset += coordCount;
    }
}

static jint getPaletteEntryCount(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    return static_cast<jint>(TRTypefaceGetPaletteEntryCount(typeface->core()));
}

static jint getPredefinedPaletteCount(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    return static_cast<jint>(TRTypefaceGetPredefinedPaletteCount(typeface->core()));
}

static void getPaletteEntryNames(JNIEnv *env, jobject obj, jlong typefaceHandle, jobjectArray names)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    const TRPaletteEntry *entries = TRTypefaceGetPaletteEntriesPtr(typeface->core());
    TRUInteger count = TRTypefaceGetPaletteEntryCount(typeface->core());

    for (TRUInteger i = 0; i < count; i++) {
        jstring name = toJavaString(env, entries[i].name);
        env->SetObjectArrayElement(names, static_cast<jsize>(i), name);
        env->DeleteLocalRef(name);
    }
}

/* Fills the colors of each palette one after the other. */
static void getPredefinedPalettes(JNIEnv *env, jobject obj, jlong typefaceHandle,
    jobjectArray names, jintArray flags, jintArray colors)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    const TRPredefinedPalette *palettes = TRTypefaceGetPredefinedPalettesPtr(typeface->core());
    TRUInteger count = TRTypefaceGetPredefinedPaletteCount(typeface->core());
    jsize offset = 0;

    for (TRUInteger i = 0; i < count; i++) {
        jstring name = toJavaString(env, palettes[i].name);
        env->SetObjectArrayElement(names, static_cast<jsize>(i), name);
        env->DeleteLocalRef(name);

        auto paletteFlags = static_cast<jint>(palettes[i].flags);
        env->SetIntArrayRegion(flags, static_cast<jsize>(i), 1, &paletteFlags);

        auto colorCount = static_cast<jsize>(palettes[i].colorCount);
        env->SetIntArrayRegion(colors, offset,
                               colorCount, reinterpret_cast<const jint *>(palettes[i].colorsPtr));
        offset += colorCount;
    }
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
    const TRFloat *values = TRTypefaceGetVariationCoordinatesPtr(typeface->core());
    jint count = env->GetArrayLength(coordinates);

    env->SetFloatArrayRegion(coordinates, 0, count, values);
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
    const TRColor *values = TRTypefaceGetAssociatedColorsPtr(typeface->core());
    jint count = env->GetArrayLength(colors);

    env->SetIntArrayRegion(colors, 0, count, reinterpret_cast<const jint *>(values));
}

static jbyteArray getTableData(JNIEnv *env, jobject obj, jlong typefaceHandle, jint tableTag)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    auto inputTag = static_cast<TRTag>(tableTag);

    TRUInteger tableLength = TRTypefaceGetTableData(typeface->core(), inputTag, nullptr, 0);
    if (tableLength == 0) {
        return nullptr;
    }

    jbyteArray dataArray = env->NewByteArray(static_cast<jint>(tableLength));
    void *dataBuffer = env->GetPrimitiveArrayCritical(dataArray, nullptr);

    TRTypefaceGetTableData(typeface->core(), inputTag, dataBuffer, tableLength);

    env->ReleasePrimitiveArrayCritical(dataArray, dataBuffer, 0);

    return dataArray;
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
    TRRect box = TRTypefaceGetBoundingBox(typeface->core());

    JavaBridge(env).Rect_set(rect,
                             static_cast<jint>(box.origin.x),
                             static_cast<jint>(box.origin.y),
                             static_cast<jint>(box.origin.x + box.size.width),
                             static_cast<jint>(box.origin.y + box.size.height));
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
    { "nGetCoreHandle", "(J)J", (void *)getCoreHandle },
    { "nGetFamilyName", "(J)Ljava/lang/String;", (void *)getFamilyName },
    { "nGetStyleName", "(J)Ljava/lang/String;", (void *)getStyleName },
    { "nGetFullName", "(J)Ljava/lang/String;", (void *)getFullName },
    { "nGetWeight", "(J)I", (void *)getWeight },
    { "nGetWidth", "(J)I", (void *)getWidth },
    { "nGetSlope", "(J)I", (void *)getSlope },
    { "nGetVariationAxisCount", "(J)I", (void *)getVariationAxisCount },
    { "nGetVariationAxes", "(J[I[F[Ljava/lang/String;)V", (void *)getVariationAxes },
    { "nGetNamedStyleCount", "(J)I", (void *)getNamedStyleCount },
    { "nGetNamedStyles", "(J[Ljava/lang/String;[Ljava/lang/String;[F)V", (void *)getNamedStyles },
    { "nGetPaletteEntryCount", "(J)I", (void *)getPaletteEntryCount },
    { "nGetPaletteEntryNames", "(J[Ljava/lang/String;)V", (void *)getPaletteEntryNames },
    { "nGetPredefinedPaletteCount", "(J)I", (void *)getPredefinedPaletteCount },
    { "nGetPredefinedPalettes", "(J[Ljava/lang/String;[I[I)V", (void *)getPredefinedPalettes },
    { "nGetVariationInstance", "(J[F)J", (void *)getVariationInstance },
    { "nGetVariationCoordinates", "(J[F)V", (void *)getVariationCoordinates },
    { "nGetColorInstance", "(J[I)J", (void *)getColorInstance },
    { "nGetAssociatedColors", "(J[I)V", (void *)getAssociatedColors },
    { "nGetTableData", "(JI)[B", (void *)getTableData },
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
