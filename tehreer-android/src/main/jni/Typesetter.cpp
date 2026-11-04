/*
 * Copyright (C) 2026 Muhammad Tayyab Akram
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

#include <cstdint>
#include <cstring>
#include <jni.h>
#include <vector>

#include <Tehreer/TRAttribute.h>
#include <Tehreer/TRAttributeList.h>
#include <Tehreer/TRFontFeatures.h>
#include <Tehreer/TRTypesetter.h>

#include "JavaBridge.h"
#include "LayoutHandles.h"
#include "Typesetter.h"

using namespace std;
using namespace Tehreer;

/* Every typesetter of the wrapper has UTF-16 code units, which is the encoding of Java strings. */

static jlong create(JNIEnv *env, jclass clazz, jstring string)
{
    jsize length = env->GetStringLength(string);
    const jchar *chars = env->GetStringChars(string, nullptr);

    TRTypesetterRef typesetter = TRTypesetterCreate(chars, static_cast<TRUInteger>(length),
                                                    TRStringEncodingUTF16, nullptr, 0);
    env->ReleaseStringChars(string, chars);

    return reinterpret_cast<jlong>(typesetter);
}

static jlong createCopy(JNIEnv *env, jclass clazz, jlong handle)
{
    return reinterpret_cast<jlong>(TRTypesetterCreateCopy(toTypesetter(handle)));
}

static jlong createMutableCopy(JNIEnv *env, jclass clazz, jlong handle)
{
    return reinterpret_cast<jlong>(TRTypesetterCreateMutableCopy(toTypesetter(handle)));
}

static jint getLength(JNIEnv *env, jobject obj, jlong handle)
{
    return static_cast<jint>(TRTypesetterGetLength(toTypesetter(handle)));
}

static jstring getString(JNIEnv *env, jobject obj, jlong handle, jint start, jint end)
{
    vector<jchar> chars(static_cast<size_t>(end - start));
    jstring string = nullptr;

    if (TRTypesetterGetCodeUnits(toTypesetter(handle), static_cast<TRUInteger>(start),
                           static_cast<TRUInteger>(end - start), chars.data())) {
        string = env->NewString(chars.data(), static_cast<jsize>(chars.size()));
    }

    return string;
}

/* Puts the value of an attribute in 64 bits, as the bits of a float, a number or a pointer. */
static jlong toBits(const TRAttribute &attribute)
{
    const TRAttributeValue &value = attribute.value;
    TRFloat number = 0.0f;
    bool isNumber = true;

    switch (attribute.type) {
    case TRAttributeTypeSize:
        number = value.typeSize;
        break;
    case TRAttributeScaleX:
        number = value.scaleX;
        break;
    case TRAttributeScaleY:
        number = value.scaleY;
        break;
    case TRAttributeBaselineOffset:
        number = value.baselineOffset;
        break;
    case TRAttributeObliqueness:
        number = value.obliqueness;
        break;
    case TRAttributeFirstLineHeadIndent:
        number = value.firstLineHeadIndent;
        break;
    case TRAttributeHeadIndent:
        number = value.headIndent;
        break;
    default:
        isNumber = false;
        break;
    }

    if (isNumber) {
        uint32_t bits = 0;
        memcpy(&bits, &number, sizeof(bits));

        return static_cast<jlong>(bits);
    }

    switch (attribute.type) {
    case TRAttributeTypeface:
        return reinterpret_cast<jlong>(value.typeface);
    case TRAttributeReplacement:
        return reinterpret_cast<jlong>(value.replacement);
    case TRAttributeFontFeatures:
        return reinterpret_cast<jlong>(value.fontFeatures);
    case TRAttributeForegroundColor:
        return static_cast<jlong>(value.foregroundColor);
    case TRAttributeBackgroundColor:
        return static_cast<jlong>(value.backgroundColor);
    case TRAttributeDecorationColor:
        return static_cast<jlong>(value.decorationColor);
    case TRAttributeUnderline:
        return value.underline ? 1 : 0;
    case TRAttributeStrikethrough:
        return value.strikethrough ? 1 : 0;
    case TRAttributeLanguage:
        return static_cast<jlong>(value.language);
    case TRAttributeTextAlignment:
        return static_cast<jlong>(value.textAlignment);
    case TRAttributeFirstIndentLineCount:
        return static_cast<jlong>(value.firstIndentLineCount);
    default:
        return 0;
    }
}

/* Hands the type and the bits of each attribute that applies at an index, one after the other. */
static jlongArray getAttributes(JNIEnv *env, jobject obj, jlong handle, jint index,
    jintArray runLength)
{
    TRUInteger length = 0;
    TRAttributeListRef list = TRTypesetterCopyAttributes(toTypesetter(handle), static_cast<TRUInteger>(index),
                                                   &length);
    jlongArray result = nullptr;

    if (list) {
        TRUInteger count = TRAttributeListGetCount(list);
        vector<jlong> items(count * 2);

        for (TRUInteger i = 0; i < count; i++) {
            const TRAttribute *attribute = TRAttributeListGetItem(list, i);
            items[i * 2] = static_cast<jlong>(attribute->type);
            items[i * 2 + 1] = toBits(*attribute);
        }

        jint runValue = static_cast<jint>(length);
        env->SetIntArrayRegion(runLength, 0, 1, &runValue);

        result = env->NewLongArray(static_cast<jsize>(items.size()));
        env->SetLongArrayRegion(result, 0, static_cast<jsize>(items.size()), items.data());

        TRAttributeListRelease(list);
    }

    return result;
}

/* Hands the tag and the value of each feature, one after the other. */
static jintArray getFontFeatures(JNIEnv *env, jclass clazz, jlong featuresHandle)
{
    auto features = reinterpret_cast<TRFontFeaturesRef>(featuresHandle);
    TRUInteger count = TRFontFeaturesGetCount(features);
    const TROpenTypeFeature *items = TRFontFeaturesGetItemsPtr(features);
    vector<jint> values(count * 2);

    for (TRUInteger i = 0; i < count; i++) {
        values[i * 2] = static_cast<jint>(items[i].tag);
        values[i * 2 + 1] = static_cast<jint>(items[i].value);
    }

    jintArray result = env->NewIntArray(static_cast<jsize>(values.size()));
    env->SetIntArrayRegion(result, 0, static_cast<jsize>(values.size()), values.data());

    return result;
}

static void disposeTypesetter(JNIEnv *env, jclass clazz, jlong handle)
{
    TRTypesetterRelease(toTypesetter(handle));
}

/* Gives the boundary index, or -1 if there is none. */
static jint findBoundary(JNIEnv *env, jobject obj, jlong handle, jint index, jint type,
    jint direction)
{
    TRUInteger boundary = TRTypesetterFindBoundary(toTypesetter(handle),
        static_cast<TRUInteger>(index), static_cast<TRBoundaryType>(type),
        static_cast<TRBoundaryDirection>(direction));

    return boundary != TRInvalidIndex ? static_cast<jint>(boundary) : -1;
}

/* Packs the start and the end of the range in 64 bits, or gives -1 if there is no range. */
static jlong getBoundaryRange(JNIEnv *env, jobject obj, jlong handle, jint index, jint type)
{
    TRRange range = TRTypesetterGetBoundaryRange(toTypesetter(handle),
        static_cast<TRUInteger>(index), static_cast<TRBoundaryType>(type));
    if (range.index == TRInvalidIndex) {
        return -1;
    }

    jlong start = static_cast<jlong>(range.index);
    jlong end = static_cast<jlong>(range.index + range.length);

    return (start << 32) | end;
}

static jint suggestForwardBreak(JNIEnv *env, jobject obj, jlong handle, jint start, jint end,
    jfloat extent, jint breakMode)
{
    return static_cast<jint>(TRTypesetterSuggestForwardBreak(toTypesetter(handle),
        toIndex(start), toLength(start, end), extent, static_cast<TRBreakMode>(breakMode)));
}

static jint suggestBackwardBreak(JNIEnv *env, jobject obj, jlong handle, jint start, jint end,
    jfloat extent, jint breakMode)
{
    return static_cast<jint>(TRTypesetterSuggestBackwardBreak(toTypesetter(handle),
        toIndex(start), toLength(start, end), extent, static_cast<TRBreakMode>(breakMode)));
}

static jlong createSimpleLine(JNIEnv *env, jobject obj, jlong handle, jint start, jint end,
    jfloat layoutWidth)
{
    return reinterpret_cast<jlong>(TRTypesetterCreateSimpleLine(toTypesetter(handle), toIndex(start),
                                                                toLength(start, end), layoutWidth));
}

static jlong createTruncationToken(JNIEnv *env, jobject obj, jlong handle, jint start, jint end,
    jint place, jstring token)
{
    const jchar *chars = nullptr;
    jsize length = 0;

    if (token) {
        length = env->GetStringLength(token);
        chars = env->GetStringChars(token, nullptr);
    }

    TRComposedLineRef line = TRTypesetterCreateTruncationToken(toTypesetter(handle),
        toIndex(start), toLength(start, end), static_cast<TRTruncationPlace>(place), chars,
        static_cast<TRUInteger>(length), TRStringEncodingUTF16);

    if (chars) {
        env->ReleaseStringChars(token, chars);
    }

    return reinterpret_cast<jlong>(line);
}

static jlong createTruncatedLine(JNIEnv *env, jobject obj, jlong handle, jint start, jint end,
    jfloat extent, jint breakMode, jint place, jlong tokenHandle, jobject token)
{
    return reinterpret_cast<jlong>(TRTypesetterCreateTruncatedLine(toTypesetter(handle),
        toIndex(start), toLength(start, end), extent, static_cast<TRBreakMode>(breakMode),
        static_cast<TRTruncationPlace>(place), toLine(tokenHandle)));
}

static jlong createJustifiedLine(JNIEnv *env, jobject obj, jlong handle, jint start, jint end,
    jfloat factor, jfloat extent)
{
    return reinterpret_cast<jlong>(TRTypesetterCreateJustifiedLine(toTypesetter(handle),
        toIndex(start), toLength(start, end), factor, extent));
}

static JNINativeMethod TYPESETTER_METHODS[] = {
    { "nCreate", "(Ljava/lang/String;)J", (void *)create },
    { "nCreateCopy", "(J)J", (void *)createCopy },
    { "nCreateMutableCopy", "(J)J", (void *)createMutableCopy },
    { "nGetFontFeatures", "(J)[I", (void *)getFontFeatures },
    { "nDispose", "(J)V", (void *)disposeTypesetter },
    { "nGetLength", "(J)I", (void *)getLength },
    { "nGetString", "(JII)Ljava/lang/String;", (void *)getString },
    { "nGetAttributes", "(JI[I)[J", (void *)getAttributes },
    { "nFindBoundary", "(JIII)I", (void *)findBoundary },
    { "nGetBoundaryRange", "(JII)J", (void *)getBoundaryRange },
    { "nSuggestForwardBreak", "(JIIFI)I", (void *)suggestForwardBreak },
    { "nSuggestBackwardBreak", "(JIIFI)I", (void *)suggestBackwardBreak },
    { "nCreateSimpleLine", "(JIIF)J", (void *)createSimpleLine },
    { "nCreateTruncationToken", "(JIIILjava/lang/String;)J", (void *)createTruncationToken },
    { "nCreateTruncatedLine", "(JIIFIIJLjava/lang/Object;)J", (void *)createTruncatedLine },
    { "nCreateJustifiedLine", "(JIIFF)J", (void *)createJustifiedLine },
};

jint register_com_mta_tehreer_layout_Typesetter(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/layout/Typesetter", TYPESETTER_METHODS,
                                     sizeof(TYPESETTER_METHODS) / sizeof(TYPESETTER_METHODS[0]));
}
