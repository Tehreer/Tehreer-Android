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
#include <Tehreer/TRText.h>

#include "JavaBridge.h"
#include "TText.h"

using namespace std;
using namespace Tehreer;

/* Every text of the wrapper has UTF-16 code units, which is the encoding of Java strings. */

static jlong create(JNIEnv *env, jclass clazz, jstring string)
{
    jsize length = env->GetStringLength(string);
    const jchar *chars = env->GetStringChars(string, nullptr);

    TRTextRef text = TRTextCreate(chars, static_cast<TRUInteger>(length), TRStringEncodingUTF16);
    env->ReleaseStringChars(string, chars);

    return reinterpret_cast<jlong>(text);
}

static jlong createMutable(JNIEnv *env, jclass clazz)
{
    return reinterpret_cast<jlong>(TRTextCreateMutable(TRStringEncodingUTF16));
}

static jlong createCopy(JNIEnv *env, jclass clazz, jlong handle)
{
    return reinterpret_cast<jlong>(TRTextCreateCopy(toText(handle)));
}

static jlong createMutableCopy(JNIEnv *env, jclass clazz, jlong handle)
{
    return reinterpret_cast<jlong>(TRTextCreateMutableCopy(toText(handle)));
}

/* Gives the caller its own reference to the text. */
static jlong retain(JNIEnv *env, jclass clazz, jlong handle)
{
    return reinterpret_cast<jlong>(TRTextRetain(toText(handle)));
}

static jint getLength(JNIEnv *env, jobject obj, jlong handle)
{
    return static_cast<jint>(TRTextGetLength(toText(handle)));
}

static jstring getString(JNIEnv *env, jobject obj, jlong handle, jint start, jint end)
{
    vector<jchar> chars(static_cast<size_t>(end - start));
    jstring string = nullptr;

    if (TRTextGetCodeUnits(toText(handle), static_cast<TRUInteger>(start),
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
    TRAttributeListRef list = TRTextCopyAttributes(toText(handle), static_cast<TRUInteger>(index),
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

static void dispose(JNIEnv *env, jclass clazz, jlong handle)
{
    TRTextRelease(toText(handle));
}

static JNINativeMethod TEXT_METHODS[] = {
    { "nCreate", "(Ljava/lang/String;)J", (void *)create },
    { "nCreateMutable", "()J", (void *)createMutable },
    { "nCreateCopy", "(J)J", (void *)createCopy },
    { "nCreateMutableCopy", "(J)J", (void *)createMutableCopy },
    { "nRetain", "(J)J", (void *)retain },
    { "nGetLength", "(J)I", (void *)getLength },
    { "nGetString", "(JII)Ljava/lang/String;", (void *)getString },
    { "nGetAttributes", "(JI[I)[J", (void *)getAttributes },
    { "nGetFontFeatures", "(J)[I", (void *)getFontFeatures },
    { "nDispose", "(J)V", (void *)dispose },
};

jint register_com_mta_tehreer_layout_TText(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/layout/TText", TEXT_METHODS,
                                     sizeof(TEXT_METHODS) / sizeof(TEXT_METHODS[0]));
}
