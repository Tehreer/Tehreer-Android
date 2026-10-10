/*
 * Copyright (C) 2016-2026 Muhammad Tayyab Akram
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

#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <cstddef>
#include <cstdint>
#include <jni.h>
#include <string>

#include <Tehreer/TRTypeface.h>

#include "FontFile.h"
#include "JavaBridge.h"
#include "PathBuilder.h"
#include "Typeface.h"

using namespace std;
using namespace Tehreer;

/* Creates the first typeface of a font file, which the typeface keeps alive afterwards. */
static jlong createFirstTypeface(TRFontFileRef fontFile)
{
    TRTypefaceRef typeface = nullptr;

    if (fontFile) {
        typeface = TRFontFileGetTypeface(fontFile, 0);
        if (typeface) {
            TRTypefaceRetain(typeface);
        }

        TRFontFileRelease(fontFile);
    }

    return reinterpret_cast<jlong>(typeface);
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

// MARK: Creation

static jlong createWithAsset(JNIEnv *env, jclass clazz, jobject assetManager, jstring path)
{
    return createFirstTypeface(createFontFileFromAsset(env, assetManager, path));
}

static jlong createWithFile(JNIEnv *env, jclass clazz, jstring path)
{
    return createFirstTypeface(createFontFileFromPath(env, path));
}

static jlong createFromStream(JNIEnv *env, jclass clazz, jobject stream)
{
    return createFirstTypeface(createFontFileFromStream(env, stream));
}

static void dispose(JNIEnv *env, jclass clazz, jlong typefaceHandle)
{
    TRTypefaceRelease(toTypeface(typefaceHandle));
}

// MARK: Names and Design

static jstring getFamilyName(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    return toJavaString(env, TRTypefaceGetFamilyName(toTypeface(typefaceHandle)));
}

static jstring getStyleName(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    return toJavaString(env, TRTypefaceGetSubfamilyName(toTypeface(typefaceHandle)));
}

static jstring getFullName(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    return toJavaString(env, TRTypefaceGetFullName(toTypeface(typefaceHandle)));
}

static jint getWeight(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    return static_cast<jint>(TRTypefaceGetWeight(toTypeface(typefaceHandle)));
}

static jint getWidth(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    return static_cast<jint>(TRTypefaceGetWidth(toTypeface(typefaceHandle)));
}

static jint getSlope(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    return static_cast<jint>(TRTypefaceGetSlope(toTypeface(typefaceHandle)));
}

// MARK: Variations

static jint getVariationAxisCount(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    return static_cast<jint>(TRTypefaceGetVariationAxisCount(toTypeface(typefaceHandle)));
}

static const TRVariationAxis &variationAxisAt(jlong typefaceHandle, jint index)
{
    return TRTypefaceGetVariationAxesPtr(toTypeface(typefaceHandle))[index];
}

static jint getVariationAxisTag(JNIEnv *env, jobject obj, jlong typefaceHandle, jint index)
{
    return static_cast<jint>(variationAxisAt(typefaceHandle, index).tag);
}

static jint getVariationAxisFlags(JNIEnv *env, jobject obj, jlong typefaceHandle, jint index)
{
    return static_cast<jint>(variationAxisAt(typefaceHandle, index).flags);
}

static jstring getVariationAxisName(JNIEnv *env, jobject obj, jlong typefaceHandle, jint index)
{
    return toJavaString(env, variationAxisAt(typefaceHandle, index).name);
}

static jfloat getVariationAxisMinValue(JNIEnv *env, jobject obj, jlong typefaceHandle, jint index)
{
    return variationAxisAt(typefaceHandle, index).minValue;
}

static jfloat getVariationAxisDefaultValue(JNIEnv *env, jobject obj, jlong typefaceHandle, jint index)
{
    return variationAxisAt(typefaceHandle, index).defaultValue;
}

static jfloat getVariationAxisMaxValue(JNIEnv *env, jobject obj, jlong typefaceHandle, jint index)
{
    return variationAxisAt(typefaceHandle, index).maxValue;
}

static jint getNamedStyleCount(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    return static_cast<jint>(TRTypefaceGetNamedStyleCount(toTypeface(typefaceHandle)));
}

static const TRNamedStyle &namedStyleAt(jlong typefaceHandle, jint index)
{
    return TRTypefaceGetNamedStylesPtr(toTypeface(typefaceHandle))[index];
}

static jstring getNamedStyleName(JNIEnv *env, jobject obj, jlong typefaceHandle, jint index)
{
    return toJavaString(env, namedStyleAt(typefaceHandle, index).subfamilyName);
}

/* A style without a post script name gives null. */
static jstring getNamedStylePostScriptName(JNIEnv *env, jobject obj, jlong typefaceHandle, jint index)
{
    const TRStringView *name = namedStyleAt(typefaceHandle, index).postScriptName;

    return name ? toJavaString(env, name) : nullptr;
}

/* The coordinates are as many as the variation axes, and belong to the typeface. */
static jlong getNamedStyleCoordinatesPtr(JNIEnv *env, jobject obj, jlong typefaceHandle, jint index)
{
    return reinterpret_cast<jlong>(namedStyleAt(typefaceHandle, index).coordinatesPtr);
}

static jlong getVariationInstance(JNIEnv *env, jobject obj, jlong typefaceHandle, jfloatArray coordinates)
{
    jint count = env->GetArrayLength(coordinates);
    jfloat *values = env->GetFloatArrayElements(coordinates, nullptr);

    TRTypefaceRef instance = TRTypefaceCreateWithVariation(toTypeface(typefaceHandle), values,
                                                           static_cast<TRUInteger>(count));

    env->ReleaseFloatArrayElements(coordinates, values, JNI_ABORT);

    return reinterpret_cast<jlong>(instance);
}

static jlong getVariationCoordinatesPtr(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    return reinterpret_cast<jlong>(TRTypefaceGetVariationCoordinatesPtr(toTypeface(typefaceHandle)));
}

// MARK: Palettes

static jint getPaletteEntryCount(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    return static_cast<jint>(TRTypefaceGetPaletteEntryCount(toTypeface(typefaceHandle)));
}

static jstring getPaletteEntryName(JNIEnv *env, jobject obj, jlong typefaceHandle, jint index)
{
    const TRPaletteEntry &entry = TRTypefaceGetPaletteEntriesPtr(toTypeface(typefaceHandle))[index];

    return toJavaString(env, entry.name);
}

static jint getPredefinedPaletteCount(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    return static_cast<jint>(TRTypefaceGetPredefinedPaletteCount(toTypeface(typefaceHandle)));
}

static const TRPredefinedPalette &predefinedPaletteAt(jlong typefaceHandle, jint index)
{
    return TRTypefaceGetPredefinedPalettesPtr(toTypeface(typefaceHandle))[index];
}

static jstring getPredefinedPaletteName(JNIEnv *env, jobject obj, jlong typefaceHandle, jint index)
{
    return toJavaString(env, predefinedPaletteAt(typefaceHandle, index).name);
}

static jint getPredefinedPaletteFlags(JNIEnv *env, jobject obj, jlong typefaceHandle, jint index)
{
    return static_cast<jint>(predefinedPaletteAt(typefaceHandle, index).flags);
}

/* The colors are as many as the palette entries, and belong to the typeface. */
static jlong getPredefinedPaletteColorsPtr(JNIEnv *env, jobject obj, jlong typefaceHandle, jint index)
{
    return reinterpret_cast<jlong>(predefinedPaletteAt(typefaceHandle, index).colorsPtr);
}

static jlong getColorInstance(JNIEnv *env, jobject obj, jlong typefaceHandle, jintArray colors)
{
    jint count = env->GetArrayLength(colors);
    jint *values = env->GetIntArrayElements(colors, nullptr);

    TRTypefaceRef instance = TRTypefaceCreateWithColors(toTypeface(typefaceHandle),
        reinterpret_cast<const TRColor *>(values), static_cast<TRUInteger>(count));

    env->ReleaseIntArrayElements(colors, values, JNI_ABORT);

    return reinterpret_cast<jlong>(instance);
}

static jlong getAssociatedColorsPtr(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    return reinterpret_cast<jlong>(TRTypefaceGetAssociatedColorsPtr(toTypeface(typefaceHandle)));
}

// MARK: Tables

static jint getTableSize(JNIEnv *env, jobject obj, jlong typefaceHandle, jint tableTag)
{
    return static_cast<jint>(TRTypefaceGetTableSize(toTypeface(typefaceHandle),
                                                    static_cast<TRTag>(tableTag)));
}

static jboolean isScalable(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    return TRTypefaceIsScalable(toTypeface(typefaceHandle)) ? JNI_TRUE : JNI_FALSE;
}

/* Hands the pixel width and the pixel height of each bitmap strike, one after the other. */
static jfloatArray getBitmapStrikes(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    TRTypefaceRef typeface = toTypeface(typefaceHandle);
    TRUInteger count = TRTypefaceGetBitmapStrikeCount(typeface);
    const TRBitmapStrike *strikes = TRTypefaceGetBitmapStrikesPtr(typeface);

    jfloatArray array = env->NewFloatArray(static_cast<jsize>(count * 2));
    for (TRUInteger i = 0; i < count; i++) {
        jfloat values[2] = { strikes[i].pixelWidth, strikes[i].pixelHeight };
        env->SetFloatArrayRegion(array, static_cast<jsize>(i * 2), 2, values);
    }

    return array;
}

static jstring getGlyphName(JNIEnv *env, jobject obj, jlong typefaceHandle, jint glyphId)
{
    char buffer[256];
    TRUInteger length = TRTypefaceGetGlyphName(toTypeface(typefaceHandle),
                                               static_cast<TRGlyphID>(glyphId),
                                               buffer, sizeof(buffer));

    return length > 0 ? env->NewStringUTF(buffer) : nullptr;
}

static jbyteArray getTableData(JNIEnv *env, jobject obj, jlong typefaceHandle, jint tableTag,
    jint offset, jint length)
{
    TRTypefaceRef typeface = toTypeface(typefaceHandle);
    auto inputTag = static_cast<TRTag>(tableTag);

    TRUInteger tableSize = TRTypefaceGetTableSize(typeface, inputTag);
    TRUInteger tableOffset = static_cast<TRUInteger>(offset);
    if (offset < 0 || tableOffset >= tableSize) {
        return nullptr;
    }

    TRUInteger tableLength = tableSize - tableOffset;
    if (length >= 0 && static_cast<TRUInteger>(length) < tableLength) {
        tableLength = static_cast<TRUInteger>(length);
    }
    if (tableLength == 0) {
        return nullptr;
    }

    jbyteArray dataArray = env->NewByteArray(static_cast<jint>(tableLength));
    void *dataBuffer = env->GetPrimitiveArrayCritical(dataArray, nullptr);

    TRTypefaceGetTableData(typeface, inputTag, tableOffset, dataBuffer, tableLength);

    env->ReleasePrimitiveArrayCritical(dataArray, dataBuffer, 0);

    return dataArray;
}

// MARK: Metrics

static jint getUnitsPerEm(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    return static_cast<jint>(TRTypefaceGetUnitsPerEM(toTypeface(typefaceHandle)));
}

static jint getAscent(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    return static_cast<jint>(TRTypefaceGetAscent(toTypeface(typefaceHandle)));
}

static jint getDescent(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    return static_cast<jint>(TRTypefaceGetDescent(toTypeface(typefaceHandle)));
}

static jint getLeading(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    return static_cast<jint>(TRTypefaceGetLeading(toTypeface(typefaceHandle)));
}

static jint getGlyphCount(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    return static_cast<jint>(TRTypefaceGetGlyphCount(toTypeface(typefaceHandle)));
}

static jobject getBoundingBox(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    TRRect box = TRTypefaceGetBoundingBox(toTypeface(typefaceHandle));

    return JavaBridge(env).Rect_construct(static_cast<jint>(box.origin.x),
                                          static_cast<jint>(box.origin.y),
                                          static_cast<jint>(box.origin.x + box.size.width),
                                          static_cast<jint>(box.origin.y + box.size.height));
}

static jint getUnderlinePosition(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    return static_cast<jint>(TRTypefaceGetUnderlinePosition(toTypeface(typefaceHandle)));
}

static jint getUnderlineThickness(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    return static_cast<jint>(TRTypefaceGetUnderlineThickness(toTypeface(typefaceHandle)));
}

static jint getStrikeoutPosition(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    return static_cast<jint>(TRTypefaceGetStrikeoutPosition(toTypeface(typefaceHandle)));
}

static jint getStrikeoutThickness(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    return static_cast<jint>(TRTypefaceGetStrikeoutThickness(toTypeface(typefaceHandle)));
}

// MARK: Glyphs

static jint getGlyphId(JNIEnv *env, jobject obj, jlong typefaceHandle, jint codePoint)
{
    return static_cast<jint>(TRTypefaceGetGlyphID(toTypeface(typefaceHandle),
                                                  static_cast<TRUInt32>(codePoint)));
}

static jfloat getGlyphAdvance(JNIEnv *env, jobject obj, jlong typefaceHandle,
    jint glyphId, jfloat typeSize, jboolean vertical)
{
    return TRTypefaceGetGlyphAdvance(toTypeface(typefaceHandle), static_cast<TRGlyphID>(glyphId),
                                     typeSize, vertical ? TRTrue : TRFalse);
}

/* The transform is the one of a matrix of Android, which is given by its six values. */
static jobject getGlyphPath(JNIEnv *env, jobject obj, jlong typefaceHandle, jint glyphId,
    jfloat typeSize, jfloat scaleX, jfloat skewX, jfloat translateX, jfloat skewY, jfloat scaleY,
    jfloat translateY)
{
    TRPathRef corePath = TRTypefaceCreateGlyphPath(toTypeface(typefaceHandle),
                                                   static_cast<TRGlyphID>(glyphId), typeSize);
    if (!corePath) {
        return nullptr;
    }

    /* The path of Core points downward, which is how the transform expects it. */
    TRAffineTransform matrix = { scaleX, skewY, skewX, scaleY, translateX, translateY };

    PathBuilder builder(env);
    TRPathCallbacks callbacks = PathBuilder::callbacks();
    TRPathEnumerate(corePath, &matrix, &callbacks, &builder);

    TRPathRelease(corePath);

    return builder.path;
}

static JNINativeMethod JNI_METHODS[] = {
    { "nCreateWithAsset", "(Landroid/content/res/AssetManager;Ljava/lang/String;)J", (void *)createWithAsset },
    { "nCreateWithFile", "(Ljava/lang/String;)J", (void *)createWithFile },
    { "nCreateFromStream", "(Ljava/io/InputStream;)J", (void *)createFromStream },
    { "nDispose", "(J)V", (void *)dispose },
    { "nGetFamilyName", "(J)Ljava/lang/String;", (void *)getFamilyName },
    { "nGetStyleName", "(J)Ljava/lang/String;", (void *)getStyleName },
    { "nGetFullName", "(J)Ljava/lang/String;", (void *)getFullName },
    { "nGetWeight", "(J)I", (void *)getWeight },
    { "nGetWidth", "(J)I", (void *)getWidth },
    { "nGetSlope", "(J)I", (void *)getSlope },
    { "nGetVariationAxisCount", "(J)I", (void *)getVariationAxisCount },
    { "nGetVariationAxisTag", "(JI)I", (void *)getVariationAxisTag },
    { "nGetVariationAxisFlags", "(JI)I", (void *)getVariationAxisFlags },
    { "nGetVariationAxisName", "(JI)Ljava/lang/String;", (void *)getVariationAxisName },
    { "nGetVariationAxisMinValue", "(JI)F", (void *)getVariationAxisMinValue },
    { "nGetVariationAxisDefaultValue", "(JI)F", (void *)getVariationAxisDefaultValue },
    { "nGetVariationAxisMaxValue", "(JI)F", (void *)getVariationAxisMaxValue },
    { "nGetNamedStyleCount", "(J)I", (void *)getNamedStyleCount },
    { "nGetNamedStyleName", "(JI)Ljava/lang/String;", (void *)getNamedStyleName },
    { "nGetNamedStylePostScriptName", "(JI)Ljava/lang/String;", (void *)getNamedStylePostScriptName },
    { "nGetNamedStyleCoordinatesPtr", "(JI)J", (void *)getNamedStyleCoordinatesPtr },
    { "nGetVariationInstance", "(J[F)J", (void *)getVariationInstance },
    { "nGetVariationCoordinatesPtr", "(J)J", (void *)getVariationCoordinatesPtr },
    { "nGetPaletteEntryCount", "(J)I", (void *)getPaletteEntryCount },
    { "nGetPaletteEntryName", "(JI)Ljava/lang/String;", (void *)getPaletteEntryName },
    { "nGetPredefinedPaletteCount", "(J)I", (void *)getPredefinedPaletteCount },
    { "nGetPredefinedPaletteName", "(JI)Ljava/lang/String;", (void *)getPredefinedPaletteName },
    { "nGetPredefinedPaletteFlags", "(JI)I", (void *)getPredefinedPaletteFlags },
    { "nGetPredefinedPaletteColorsPtr", "(JI)J", (void *)getPredefinedPaletteColorsPtr },
    { "nGetColorInstance", "(J[I)J", (void *)getColorInstance },
    { "nGetAssociatedColorsPtr", "(J)J", (void *)getAssociatedColorsPtr },
        { "nGetTableSize", "(JI)I", (void *)getTableSize },
    { "nGetTableData", "(JIII)[B", (void *)getTableData },
    { "nIsScalable", "(J)Z", (void *)isScalable },
    { "nGetBitmapStrikes", "(J)[F", (void *)getBitmapStrikes },
    { "nGetGlyphName", "(JI)Ljava/lang/String;", (void *)getGlyphName },
    { "nGetUnitsPerEm", "(J)I", (void *)getUnitsPerEm },
    { "nGetAscent", "(J)I", (void *)getAscent },
    { "nGetDescent", "(J)I", (void *)getDescent },
    { "nGetLeading", "(J)I", (void *)getLeading },
    { "nGetGlyphCount", "(J)I", (void *)getGlyphCount },
    { "nGetBoundingBox", "(J)Landroid/graphics/Rect;", (void *)getBoundingBox },
    { "nGetUnderlinePosition", "(J)I", (void *)getUnderlinePosition },
    { "nGetUnderlineThickness", "(J)I", (void *)getUnderlineThickness },
    { "nGetStrikeoutPosition", "(J)I", (void *)getStrikeoutPosition },
    { "nGetStrikeoutThickness", "(J)I", (void *)getStrikeoutThickness },
    { "nGetGlyphId", "(JI)I", (void *)getGlyphId },
    { "nGetGlyphAdvance", "(JIFZ)F", (void *)getGlyphAdvance },
    { "nGetGlyphPath", "(JIFFFFFFF)Landroid/graphics/Path;", (void *)getGlyphPath },
};

jint register_com_mta_tehreer_graphics_Typeface(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/graphics/Typeface", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
