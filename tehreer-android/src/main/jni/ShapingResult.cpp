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

#include <jni.h>

#include <Tehreer/TRShapingResult.h>

#include "JavaBridge.h"
#include "ShapingResult.h"

using namespace std;
using namespace Tehreer;

ShapingResult::ShapingResult()
    : m_core(nullptr)
    , m_glyphIds(nullptr)
    , m_glyphOffsets(nullptr)
    , m_glyphAdvances(nullptr)
    , m_glyphCount(0)
    , m_clusterMap()
    , m_charStart(0)
    , m_charEnd(0)
{
}

ShapingResult::~ShapingResult()
{
    if (m_core) {
        TRShapingResultRelease(m_core);
    }
}

void ShapingResult::setup(TRShapingResultRef core, jint charStart, jint charEnd)
{
    if (m_core) {
        TRShapingResultRelease(m_core);
    }

    m_core = core;
    m_charStart = charStart;
    m_charEnd = charEnd;
    m_glyphIds = nullptr;
    m_glyphOffsets = nullptr;
    m_glyphAdvances = nullptr;
    m_glyphCount = 0;
    m_clusterMap.clear();

    if (core) {
        auto codeUnitCount = static_cast<size_t>(TRShapingResultGetCodeUnitCount(core));
        const TRUInteger *clusterMap = TRShapingResultGetClusterMapPtr(core);

        m_glyphIds = TRShapingResultGetGlyphIDsPtr(core);
        m_glyphOffsets = TRShapingResultGetGlyphOffsetsPtr(core);
        m_glyphAdvances = TRShapingResultGetGlyphAdvancesPtr(core);
        m_glyphCount = static_cast<unsigned int>(TRShapingResultGetGlyphCount(core));

        m_clusterMap.resize(codeUnitCount);
        for (size_t i = 0; i < codeUnitCount; i++) {
            m_clusterMap[i] = static_cast<jint>(clusterMap[i]);
        }
    }
}

void ShapingResult::copyGlyphIds(jint offset, jint length, jint *destination) const
{
    for (jint i = 0; i < length; i++) {
        destination[i] = m_glyphIds[offset + i];
    }
}

void ShapingResult::copyGlyphOffsets(jint offset, jint length, jfloat *destination) const
{
    jint index = 0;

    for (jint i = 0; i < length; i++) {
        destination[index++] = m_glyphOffsets[offset + i].x;
        destination[index++] = m_glyphOffsets[offset + i].y;
    }
}

void ShapingResult::copyGlyphAdvances(jint offset, jint length, jfloat *destination) const
{
    for (jint i = 0; i < length; i++) {
        destination[i] = m_glyphAdvances[offset + i];
    }
}

static jlong create(JNIEnv *env, jobject obj)
{
    auto shapingResult = new ShapingResult();
    return reinterpret_cast<jlong>(shapingResult);
}

static void dispose(JNIEnv *env, jobject obj, jlong resultHandle)
{
    auto shapingResult = reinterpret_cast<ShapingResult *>(resultHandle);
    delete shapingResult;
}

static jboolean isBackward(JNIEnv *env, jobject obj, jlong resultHandle)
{
    auto shapingResult = reinterpret_cast<ShapingResult *>(resultHandle);
    return shapingResult->isBackward();
}

static jboolean isRTL(JNIEnv *env, jobject obj, jlong resultHandle)
{
    auto shapingResult = reinterpret_cast<ShapingResult *>(resultHandle);
    return shapingResult->isRTL();
}

static jint getCharStart(JNIEnv *env, jobject obj, jlong resultHandle)
{
    auto shapingResult = reinterpret_cast<ShapingResult *>(resultHandle);
    jint charStart = shapingResult->charStart();

    return charStart;
}

static jint getCharEnd(JNIEnv *env, jobject obj, jlong resultHandle)
{
    auto shapingResult = reinterpret_cast<ShapingResult *>(resultHandle);
    jint charEnd = shapingResult->charEnd();

    return charEnd;
}

static jint getCharCount(JNIEnv *env, jobject obj, jlong resultHandle)
{
    auto shapingResult = reinterpret_cast<ShapingResult *>(resultHandle);
    jint charCount = shapingResult->charEnd() - shapingResult->charStart();

    return charCount;
}

static jint getGlyphCount(JNIEnv *env, jobject obj, jlong resultHandle)
{
    auto shapingResult = reinterpret_cast<ShapingResult *>(resultHandle);
    unsigned int glyphCount = shapingResult->glyphCount();

    return static_cast<jint>(glyphCount);
}

static jint getGlyphId(JNIEnv *env, jobject obj, jlong resultHandle, jint index)
{
    auto shapingResult = reinterpret_cast<ShapingResult *>(resultHandle);
    jint glyphId = shapingResult->glyphIdAt(index);

    return static_cast<jint>(glyphId);
}

static jfloat getGlyphXOffset(JNIEnv *env, jobject obj, jlong resultHandle, jint index)
{
    auto shapingResult = reinterpret_cast<ShapingResult *>(resultHandle);
    return shapingResult->glyphXOffsetAt(index);
}

static jfloat getGlyphYOffset(JNIEnv *env, jobject obj, jlong resultHandle, jint index)
{
    auto shapingResult = reinterpret_cast<ShapingResult *>(resultHandle);
    return shapingResult->glyphYOffsetAt(index);
}

static jfloat getGlyphAdvance(JNIEnv *env, jobject obj, jlong resultHandle, jint index)
{
    auto shapingResult = reinterpret_cast<ShapingResult *>(resultHandle);
    return shapingResult->glyphAdvanceAt(index);
}

static jlong getClusterMapPtr(JNIEnv *env, jobject obj, jlong resultHandle)
{
    auto shapingResult = reinterpret_cast<ShapingResult *>(resultHandle);
    const jint *clusterMapPtr = shapingResult->clusterMapPtr();

    return reinterpret_cast<jlong>(clusterMapPtr);
}

static void copyGlyphIds(JNIEnv *env, jobject obj, jlong resultHandle, jint offset, jint length,
    jintArray destination, jint index)
{
    auto shapingResult = reinterpret_cast<ShapingResult *>(resultHandle);
    void *raw = env->GetPrimitiveArrayCritical(destination, nullptr);
    auto values = static_cast<jint *>(raw) + index;

    shapingResult->copyGlyphIds(offset, length, values);

    env->ReleasePrimitiveArrayCritical(destination, raw, 0);
}

static void copyGlyphOffsets(JNIEnv *env, jobject obj, jlong resultHandle, jint offset, jint length,
    jfloatArray destination, jint index)
{
    auto shapingResult = reinterpret_cast<ShapingResult *>(resultHandle);
    void *raw = env->GetPrimitiveArrayCritical(destination, nullptr);
    auto values = static_cast<jfloat *>(raw) + index;

    shapingResult->copyGlyphOffsets(offset, length, values);

    env->ReleasePrimitiveArrayCritical(destination, raw, 0);
}

static void copyGlyphAdvances(JNIEnv *env, jobject obj, jlong resultHandle,
    jint offset, jint length, jfloatArray destination, jint index)
{
    auto shapingResult = reinterpret_cast<ShapingResult *>(resultHandle);
    void *raw = env->GetPrimitiveArrayCritical(destination, nullptr);
    auto values = static_cast<jfloat *>(raw) + index;

    shapingResult->copyGlyphAdvances(offset, length, values);

    env->ReleasePrimitiveArrayCritical(destination, raw, 0);
}

static JNINativeMethod JNI_METHODS[] = {
    { "nCreate", "()J", (void *)create },
    { "nDispose", "(J)V", (void *)dispose },
    { "nIsBackward", "(J)Z", (void *)isBackward },
    { "nIsRTL", "(J)Z", (void *)isRTL },
    { "nGetCharStart", "(J)I", (void *)getCharStart },
    { "nGetCharEnd", "(J)I", (void *)getCharEnd },
    { "nGetCharCount", "(J)I", (void *)getCharCount },
    { "nGetGlyphCount", "(J)I", (void *)getGlyphCount },
    { "nGetGlyphId", "(JI)I", (void *)getGlyphId },
    { "nGetGlyphXOffset", "(JI)F", (void *)getGlyphXOffset },
    { "nGetGlyphYOffset", "(JI)F", (void *)getGlyphYOffset },
    { "nGetGlyphAdvance", "(JI)F", (void *)getGlyphAdvance },
    { "nGetClusterMapPtr", "(J)J", (void *)getClusterMapPtr },
    { "nCopyGlyphIds", "(JII[II)V", (void *)copyGlyphIds },
    { "nCopyGlyphOffsets", "(JII[FI)V", (void *)copyGlyphOffsets },
    { "nCopyGlyphAdvances", "(JII[FI)V", (void *)copyGlyphAdvances },
};

jint register_com_mta_tehreer_sfnt_ShapingResult(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/sfnt/ShapingResult", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
