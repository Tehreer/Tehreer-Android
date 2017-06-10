/*
 * Copyright (C) 2017 Muhammad Tayyab Akram
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
#include <SFArtist.h>
#include <SFBase.h>
#include <SFPattern.h>
#include <SFScheme.h>
}

#include <cstring>
#include <jni.h>
#include <map>

#include "JavaBridge.h"
#include "PatternCache.h"
#include "ShapingEngine.h"

using namespace Tehreer;

SFTextDirection ShapingEngine::getScriptDefaultDirection(SFTag scriptTag)
{
    return SFScriptGetDefaultDirection(scriptTag);
}

ShapingEngine::ShapingEngine()
    : m_sfArtist(SFArtistCreate())
    , m_sfScheme(SFSchemeCreate())
    , m_typeface(nullptr)
    , m_typeSize(16.0)
    , m_scriptTag(SFTagMake('D', 'F', 'L', 'T'))
    , m_languageTag(SFTagMake('d', 'f', 'l', 't'))
    , m_textMode(SFTextModeForward)
    , m_textDirection(SFTextDirectionLeftToRight)
{
}

ShapingEngine::~ShapingEngine()
{
    SFArtistRelease(m_sfArtist);
    SFSchemeRelease(m_sfScheme);
}

void ShapingEngine::setTextDirection(SFTextDirection textDirection)
{
    m_textDirection = textDirection;
    SFArtistSetTextDirection(m_sfArtist, textDirection);
}

void ShapingEngine::setTextMode(SFTextMode textMode)
{
    m_textMode = textMode;
    SFArtistSetTextMode(m_sfArtist, textMode);
}

void ShapingEngine::shapeText(ShapingResult &shapingResult, const jchar *charArray, jint charStart, jint charEnd)
{
    PatternCache &cache = m_typeface->patternCache();
    PatternKey key(m_scriptTag, m_languageTag);
    SFPatternRef pattern = cache.get(key);

    if (!pattern) {
        SFSchemeSetFont(m_sfScheme, m_typeface->sfFont());
        SFSchemeSetScriptTag(m_sfScheme, m_scriptTag);
        SFSchemeSetLanguageTag(m_sfScheme, m_languageTag);

        pattern = SFSchemeBuildPattern(m_sfScheme);
        cache.put(key, pattern);
        SFPatternRelease(pattern);
    }

    if (pattern) {
        jchar *stringOffset = const_cast<jchar *>(charArray + charStart);
        void *stringBuffer = reinterpret_cast<void *>(stringOffset);
        SFUInteger stringLength = static_cast<SFUInteger>(charEnd - charStart);

        SFArtistSetPattern(m_sfArtist, pattern);
        SFArtistSetString(m_sfArtist, SFStringEncodingUTF16, stringBuffer, stringLength);
        SFArtistFillAlbum(m_sfArtist, shapingResult.sfAlbum());

        shapingResult.sanitizeClusterMap();
    }

    jfloat sizeByEm = m_typeSize / m_typeface->ftFace()->units_per_EM;
    bool isBackward = m_textMode == SFTextModeBackward;

    shapingResult.setAdditionalInfo(sizeByEm, isBackward, charStart, charEnd);
}

static jint getScriptDefaultDirection(JNIEnv *env, jobject obj, jint scriptTag)
{
    SFTag inputTag = static_cast<SFTag>(scriptTag);
    SFTextDirection defaultDirection = ShapingEngine::getScriptDefaultDirection(inputTag);

    return static_cast<jint>(defaultDirection);
}

static jlong create(JNIEnv *env, jobject obj)
{
    ShapingEngine *shapingEngine = new ShapingEngine();
    return reinterpret_cast<jlong>(shapingEngine);
}

static void dispose(JNIEnv *env, jobject obj, jlong engineHandle)
{
    ShapingEngine *shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    delete shapingEngine;
}

static void setTypeface(JNIEnv *env, jobject obj, jlong engineHandle, jobject jtypeface)
{
    ShapingEngine *shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    Typeface *typeface = nullptr;

    if (jtypeface) {
        jlong typefaceHandle = JavaBridge(env).Typeface_getNativeTypeface(jtypeface);
        typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    }

    shapingEngine->setTypeface(typeface);
}

static jfloat getTypeSize(JNIEnv *env, jobject obj, jlong engineHandle)
{
    ShapingEngine *shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    return shapingEngine->typeSize();
}

static void setTypeSize(JNIEnv *env, jobject obj, jlong engineHandle, jfloat typeSize)
{
    ShapingEngine *shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    shapingEngine->setTypeSize(typeSize);
}

static jint getScriptTag(JNIEnv *env, jobject obj, jlong engineHandle)
{
    ShapingEngine *shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    SFTag scriptTag = shapingEngine->scriptTag();

    return static_cast<jint>(scriptTag);
}

static void setScriptTag(JNIEnv *env, jobject obj, jlong engineHandle, jint scriptTag)
{
    ShapingEngine *shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    SFTag inputTag = static_cast<SFTag>(scriptTag);

    shapingEngine->setScriptTag(inputTag);
}

static jint getLanguageTag(JNIEnv *env, jobject obj, jlong engineHandle)
{
    ShapingEngine *shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    SFTag languageTag = shapingEngine->languageTag();

    return static_cast<jint>(languageTag);
}

static void setLanguageTag(JNIEnv *env, jobject obj, jlong engineHandle, jint languageTag)
{
    ShapingEngine *shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    SFTag inputTag = static_cast<SFTag>(languageTag);

    shapingEngine->setLanguageTag(inputTag);
}

static jint getWritingDirection(JNIEnv *env, jobject obj, jlong engineHandle)
{
    ShapingEngine *shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    SFTextDirection textDirection = shapingEngine->textDirection();

    return static_cast<jint>(textDirection);
}

static void setWritingDirection(JNIEnv *env, jobject obj, jlong engineHandle, jint shapingDirection)
{
    ShapingEngine *shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    SFTextDirection textDirection = static_cast<SFTextDirection>(shapingDirection);

    shapingEngine->setTextDirection(textDirection);
}

static jint getShapingOrder(JNIEnv *env, jobject obj, jlong engineHandle)
{
    ShapingEngine *shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    SFTextMode textMode = shapingEngine->textMode();

    return static_cast<jint>(textMode);
}

static void setShapingOrder(JNIEnv *env, jobject obj, jlong engineHandle, jint shapingOrder)
{
    ShapingEngine *shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    SFTextMode textMode = static_cast<SFTextMode>(shapingOrder);

    shapingEngine->setTextMode(textMode);
}

static void shapeText(JNIEnv *env, jobject obj, jlong engineHandle, jlong albumHandle, jstring text, jint fromIndex, jint toIndex)
{
    ShapingEngine *shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    ShapingResult *opentypeAlbum = reinterpret_cast<ShapingResult *>(albumHandle);

    const jchar *charArray = env->GetStringChars(text, nullptr);

    shapingEngine->shapeText(*opentypeAlbum, charArray, fromIndex, toIndex);

    env->ReleaseStringChars(text, charArray);
}

static JNINativeMethod JNI_METHODS[] = {
    { "nativeCreate", "()J", (void *)create },
    { "nativeDispose", "(J)V", (void *)dispose },
    { "nativeGetScriptDefaultDirection", "(I)I", (void *)getScriptDefaultDirection },
    { "nativeSetTypeface", "(JLcom/mta/tehreer/graphics/Typeface;)V", (void *)setTypeface },
    { "nativeGetTypeSize", "(J)F", (void *)getTypeSize },
    { "nativeSetTypeSize", "(JF)V", (void *)setTypeSize },
    { "nativeGetScriptTag", "(J)I", (void *)getScriptTag },
    { "nativeSetScriptTag", "(JI)V", (void *)setScriptTag },
    { "nativeGetLanguageTag", "(J)I", (void *)getLanguageTag },
    { "nativeSetLanguageTag", "(JI)V", (void *)setLanguageTag },
    { "nativeGetWritingDirection", "(J)I", (void *)getWritingDirection },
    { "nativeSetWritingDirection", "(JI)V", (void *)setWritingDirection },
    { "nativeGetShapingOrder", "(J)I", (void *)getShapingOrder },
    { "nativeSetShapingOrder", "(JI)V", (void *)setShapingOrder },
    { "nativeShapeText", "(JJLjava/lang/String;II)V", (void *)shapeText },
};

jint register_com_mta_tehreer_opentype_ShapingEngine(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/opentype/ShapingEngine", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
