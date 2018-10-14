/*
 * Copyright (C) 2016-2018 Muhammad Tayyab Akram
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

#include <cstdint>
#include <jni.h>
#include <vector>

#include "JavaBridge.h"
#include "PatternCache.h"
#include "ShapingEngine.h"

using namespace std;
using namespace Tehreer;

WritingDirection ShapingEngine::getScriptDefaultDirection(uint32_t scriptTag)
{
    SFTextDirection defaultDirection = SFScriptGetDefaultDirection(scriptTag);
    WritingDirection writingDirection = static_cast<WritingDirection>(defaultDirection);

    return writingDirection;
}

ShapingEngine::ShapingEngine()
    : m_sfArtist(SFArtistCreate())
    , m_sfScheme(SFSchemeCreate())
    , m_typeface(nullptr)
    , m_typeSize(16.0)
    , m_scriptTag(SFTagMake('D', 'F', 'L', 'T'))
    , m_languageTag(SFTagMake('d', 'f', 'l', 't'))
    , m_shapingOrder(ShapingOrder::FORWARD)
    , m_writingDirection(WritingDirection::LEFT_TO_RIGHT)
{
}

ShapingEngine::~ShapingEngine()
{
    SFArtistRelease(m_sfArtist);
    SFSchemeRelease(m_sfScheme);
}

void ShapingEngine::setOpenTypeFeatures(const vector<uint32_t> &featureTags, const vector<uint16_t> &featureValues)
{
    m_featureTags = featureTags;
    m_featureValues = featureValues;
}

void ShapingEngine::setShapingOrder(ShapingOrder shapingOrder)
{
    m_shapingOrder = shapingOrder;
    SFArtistSetTextMode(m_sfArtist, shapingOrder);
}

void ShapingEngine::setWritingDirection(WritingDirection writingDirection)
{
    m_writingDirection = writingDirection;
    SFArtistSetTextDirection(m_sfArtist, writingDirection);
}

void ShapingEngine::shapeText(ShapingResult &shapingResult, const jchar *charArray, jint charStart, jint charEnd)
{
    PatternCache &cache = m_typeface->patternCache();
    PatternKey key(m_scriptTag, m_languageTag, m_featureTags, m_featureValues);
    SFPatternRef pattern = cache.get(key);

    if (!pattern) {
        SFSchemeSetFont(m_sfScheme, m_typeface->sfFont());
        SFSchemeSetScriptTag(m_sfScheme, m_scriptTag);
        SFSchemeSetLanguageTag(m_sfScheme, m_languageTag);
        SFSchemeSetFeatureValues(m_sfScheme, m_featureTags.data(), m_featureValues.data(), m_featureTags.size());

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
    }

    jfloat sizeByEm = m_typeSize / m_typeface->ftFace()->units_per_EM;
    bool isBackward = m_shapingOrder == ShapingOrder::BACKWARD;

    shapingResult.setAdditionalInfo(sizeByEm, isBackward, charStart, charEnd);
}

static jint getScriptDefaultDirection(JNIEnv *env, jobject obj, jint scriptTag)
{
    uint32_t inputTag = static_cast<uint32_t>(scriptTag);
    WritingDirection defaultDirection = ShapingEngine::getScriptDefaultDirection(inputTag);

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
    uint32_t scriptTag = shapingEngine->scriptTag();

    return static_cast<jint>(scriptTag);
}

static void setScriptTag(JNIEnv *env, jobject obj, jlong engineHandle, jint scriptTag)
{
    ShapingEngine *shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    uint32_t inputTag = static_cast<uint32_t>(scriptTag);

    shapingEngine->setScriptTag(inputTag);
}

static jint getLanguageTag(JNIEnv *env, jobject obj, jlong engineHandle)
{
    ShapingEngine *shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    uint32_t languageTag = shapingEngine->languageTag();

    return static_cast<jint>(languageTag);
}

static void setLanguageTag(JNIEnv *env, jobject obj, jlong engineHandle, jint languageTag)
{
    ShapingEngine *shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    uint32_t inputTag = static_cast<uint32_t>(languageTag);

    shapingEngine->setLanguageTag(inputTag);
}

static void setOpenTypeFeatures(JNIEnv *env, jobject obj, jlong engineHandle, jintArray tagsArray, jshortArray valuesArray)
{
    ShapingEngine *shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);

    void *rawTags = env->GetPrimitiveArrayCritical(tagsArray, nullptr);
    void *rawValues = env->GetPrimitiveArrayCritical(valuesArray, nullptr);

    uint32_t *actualTags = static_cast<uint32_t *>(rawTags);
    uint16_t *actualValues = static_cast<uint16_t *>(rawValues);
    jint featureCount = env->GetArrayLength(tagsArray);

    const vector<uint32_t> featureTags(actualTags, actualTags + featureCount);
    const vector<uint16_t> featureValues(actualValues, actualValues + featureCount);

    shapingEngine->setOpenTypeFeatures(featureTags, featureValues);

    env->ReleasePrimitiveArrayCritical(tagsArray, rawTags, 0);
    env->ReleasePrimitiveArrayCritical(valuesArray, rawValues, 0);
}

static jint getWritingDirection(JNIEnv *env, jobject obj, jlong engineHandle)
{
    ShapingEngine *shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    WritingDirection writingDirection = shapingEngine->writingDirection();

    return static_cast<jint>(writingDirection);
}

static void setWritingDirection(JNIEnv *env, jobject obj, jlong engineHandle, jint writingDirection)
{
    ShapingEngine *shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    WritingDirection layoutDirection = static_cast<WritingDirection>(writingDirection);

    shapingEngine->setWritingDirection(layoutDirection);
}

static jint getShapingOrder(JNIEnv *env, jobject obj, jlong engineHandle)
{
    ShapingEngine *shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    ShapingOrder shapingOrder = shapingEngine->shapingOrder();

    return static_cast<jint>(shapingOrder);
}

static void setShapingOrder(JNIEnv *env, jobject obj, jlong engineHandle, jint shapingOrder)
{
    ShapingEngine *shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    ShapingOrder memoryOrder = static_cast<ShapingOrder>(shapingOrder);

    shapingEngine->setShapingOrder(memoryOrder);
}

static void shapeText(JNIEnv *env, jobject obj, jlong engineHandle, jlong resultHandle, jstring text, jint fromIndex, jint toIndex)
{
    ShapingEngine *shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    ShapingResult *shapingResult = reinterpret_cast<ShapingResult *>(resultHandle);

    const jchar *charArray = env->GetStringChars(text, nullptr);

    shapingEngine->shapeText(*shapingResult, charArray, fromIndex, toIndex);

    env->ReleaseStringChars(text, charArray);
}

static JNINativeMethod JNI_METHODS[] = {
    { "nCreate", "()J", (void *)create },
    { "nDispose", "(J)V", (void *)dispose },
    { "nGetScriptDefaultDirection", "(I)I", (void *)getScriptDefaultDirection },
    { "nSetTypeface", "(JLcom/mta/tehreer/graphics/Typeface;)V", (void *)setTypeface },
    { "nGetTypeSize", "(J)F", (void *)getTypeSize },
    { "nSetTypeSize", "(JF)V", (void *)setTypeSize },
    { "nGetScriptTag", "(J)I", (void *)getScriptTag },
    { "nSetScriptTag", "(JI)V", (void *)setScriptTag },
    { "nGetLanguageTag", "(J)I", (void *)getLanguageTag },
    { "nSetLanguageTag", "(JI)V", (void *)setLanguageTag },
    { "nSetOpenTypeFeatures", "(J[I[S)V", (void *)setOpenTypeFeatures },
    { "nGetWritingDirection", "(J)I", (void *)getWritingDirection },
    { "nSetWritingDirection", "(JI)V", (void *)setWritingDirection },
    { "nGetShapingOrder", "(J)I", (void *)getShapingOrder },
    { "nSetShapingOrder", "(JI)V", (void *)setShapingOrder },
    { "nShapeText", "(JJLjava/lang/String;II)V", (void *)shapeText },
};

jint register_com_mta_tehreer_sfnt_ShapingEngine(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/sfnt/ShapingEngine", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
