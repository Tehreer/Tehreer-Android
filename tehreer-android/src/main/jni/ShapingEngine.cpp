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

#include <cstdint>
#include <jni.h>
#include <vector>

#include <Tehreer/TRShapingEngine.h>

#include "JavaBridge.h"
#include "Typeface.h"
#include "ShapingEngine.h"

using namespace std;
using namespace Tehreer;

TRWritingDirection ShapingEngine::getScriptDefaultDirection(uint32_t scriptTag)
{
    return TRShapingEngineGetScriptDefaultDirection(scriptTag);
}

ShapingEngine::ShapingEngine()
    : m_core(TRShapingEngineCreate())
    , m_typeSize(16.0)
    , m_scriptTag(TRTagMake('D', 'F', 'L', 'T'))
    , m_languageTag(TRTagMake('d', 'f', 'l', 't'))
    , m_shapingOrder(TRShapingOrderForward)
    , m_writingDirection(TRWritingDirectionLeftToRight)
{
}

ShapingEngine::~ShapingEngine()
{
    TRShapingEngineRelease(m_core);
}

void ShapingEngine::setTypeface(TRTypefaceRef typeface)
{
    TRShapingEngineSetTypeface(m_core, typeface);
}

void ShapingEngine::setTypeSize(jfloat typeSize)
{
    m_typeSize = typeSize;
    TRShapingEngineSetTypeSize(m_core, typeSize);
}

void ShapingEngine::setScriptTag(uint32_t scriptTag)
{
    m_scriptTag = scriptTag;
    TRShapingEngineSetScriptTag(m_core, scriptTag);
}

void ShapingEngine::setLanguageTag(uint32_t languageTag)
{
    m_languageTag = languageTag;
    TRShapingEngineSetLanguageTag(m_core, languageTag);
}

void ShapingEngine::addOpenTypeFeature(uint32_t tag, uint16_t value)
{
    TROpenTypeFeature feature;
    feature.tag = tag;
    feature.value = value;

    m_pendingFeatures.push_back(feature);
}

void ShapingEngine::applyOpenTypeFeatures()
{
    TRShapingEngineSetOpenTypeFeatures(m_core, m_pendingFeatures.data(), m_pendingFeatures.size());
    m_pendingFeatures.clear();
}

void ShapingEngine::setShapingOrder(TRShapingOrder shapingOrder)
{
    m_shapingOrder = shapingOrder;
    TRShapingEngineSetShapingOrder(m_core, shapingOrder);
}

void ShapingEngine::setWritingDirection(TRWritingDirection writingDirection)
{
    m_writingDirection = writingDirection;
    TRShapingEngineSetWritingDirection(m_core, writingDirection);
}

void ShapingEngine::shapeText(ShapingResult &shapingResult, const jchar *charArray, jint charStart, jint charEnd)
{
    TRShapingResultRef core = TRShapingEngineShape(m_core, charArray + charStart,
        static_cast<TRUInteger>(charEnd - charStart), TRStringEncodingUTF16);

    shapingResult.setup(core, charStart, charEnd);
}

static jint getScriptDefaultDirection(JNIEnv *env, jclass clazz, jint scriptTag)
{
    auto inputTag = static_cast<uint32_t>(scriptTag);
    TRWritingDirection defaultDirection = ShapingEngine::getScriptDefaultDirection(inputTag);

    return static_cast<jint>(defaultDirection);
}

static jlong create(JNIEnv *env, jclass clazz)
{
    auto shapingEngine = new ShapingEngine();
    return reinterpret_cast<jlong>(shapingEngine);
}

static void dispose(JNIEnv *env, jclass clazz, jlong engineHandle)
{
    auto shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    delete shapingEngine;
}

static void setTypeface(JNIEnv *env, jobject obj, jlong engineHandle, jlong typefaceHandle)
{
    auto shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);

    shapingEngine->setTypeface(toTypeface(typefaceHandle));
}

static jfloat getTypeSize(JNIEnv *env, jobject obj, jlong engineHandle)
{
    auto shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    return shapingEngine->typeSize();
}

static void setTypeSize(JNIEnv *env, jobject obj, jlong engineHandle, jfloat typeSize)
{
    auto shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    shapingEngine->setTypeSize(typeSize);
}

static jint getScriptTag(JNIEnv *env, jobject obj, jlong engineHandle)
{
    auto shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    uint32_t scriptTag = shapingEngine->scriptTag();

    return static_cast<jint>(scriptTag);
}

static void setScriptTag(JNIEnv *env, jobject obj, jlong engineHandle, jint scriptTag)
{
    auto shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    auto inputTag = static_cast<uint32_t>(scriptTag);

    shapingEngine->setScriptTag(inputTag);
}

static jint getLanguageTag(JNIEnv *env, jobject obj, jlong engineHandle)
{
    auto shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    uint32_t languageTag = shapingEngine->languageTag();

    return static_cast<jint>(languageTag);
}

static void setLanguageTag(JNIEnv *env, jobject obj, jlong engineHandle, jint languageTag)
{
    auto shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    auto inputTag = static_cast<uint32_t>(languageTag);

    shapingEngine->setLanguageTag(inputTag);
}

static void addOpenTypeFeature(JNIEnv *env, jobject obj, jlong engineHandle, jint tag, jshort value)
{
    auto shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);

    shapingEngine->addOpenTypeFeature(static_cast<uint32_t>(tag), static_cast<uint16_t>(value));
}

static void applyOpenTypeFeatures(JNIEnv *env, jobject obj, jlong engineHandle)
{
    auto shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);

    shapingEngine->applyOpenTypeFeatures();
}

static jint getWritingDirection(JNIEnv *env, jobject obj, jlong engineHandle)
{
    auto shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    TRWritingDirection writingDirection = shapingEngine->writingDirection();

    return static_cast<jint>(writingDirection);
}

static void setWritingDirection(JNIEnv *env, jobject obj, jlong engineHandle, jint writingDirection)
{
    auto shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    auto layoutDirection = static_cast<TRWritingDirection>(writingDirection);

    shapingEngine->setWritingDirection(layoutDirection);
}

static jint getShapingOrder(JNIEnv *env, jobject obj, jlong engineHandle)
{
    auto shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    TRShapingOrder shapingOrder = shapingEngine->shapingOrder();

    return static_cast<jint>(shapingOrder);
}

static void setShapingOrder(JNIEnv *env, jobject obj, jlong engineHandle, jint shapingOrder)
{
    auto shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    auto memoryOrder = static_cast<TRShapingOrder>(shapingOrder);

    shapingEngine->setShapingOrder(memoryOrder);
}

static void shapeText(JNIEnv *env, jobject obj, jlong engineHandle, jlong resultHandle, jstring text, jint fromIndex, jint toIndex)
{
    auto shapingEngine = reinterpret_cast<ShapingEngine *>(engineHandle);
    auto shapingResult = reinterpret_cast<ShapingResult *>(resultHandle);

    const jchar *charArray = env->GetStringChars(text, nullptr);

    shapingEngine->shapeText(*shapingResult, charArray, fromIndex, toIndex);

    env->ReleaseStringChars(text, charArray);
}

static JNINativeMethod JNI_METHODS[] = {
    { "nCreate", "()J", (void *)create },
    { "nDispose", "(J)V", (void *)dispose },
    { "nGetScriptDefaultDirection", "(I)I", (void *)getScriptDefaultDirection },
    { "nSetTypeface", "(JJ)V", (void *)setTypeface },
    { "nGetTypeSize", "(J)F", (void *)getTypeSize },
    { "nSetTypeSize", "(JF)V", (void *)setTypeSize },
    { "nGetScriptTag", "(J)I", (void *)getScriptTag },
    { "nSetScriptTag", "(JI)V", (void *)setScriptTag },
    { "nGetLanguageTag", "(J)I", (void *)getLanguageTag },
    { "nSetLanguageTag", "(JI)V", (void *)setLanguageTag },
    { "nAddOpenTypeFeature", "(JIS)V", (void *)addOpenTypeFeature },
    { "nApplyOpenTypeFeatures", "(J)V", (void *)applyOpenTypeFeatures },
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
