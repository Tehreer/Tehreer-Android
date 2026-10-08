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

#include <jni.h>
#include <vector>

#include <Tehreer/TRShapingResult.h>

#include "JavaBridge.h"
#include "ShapingResult.h"

using namespace std;
using namespace Tehreer;

ShapingResult::ShapingResult()
    : m_core(nullptr)
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
}

static ShapingResult *toResult(jlong handle)
{
    return reinterpret_cast<ShapingResult *>(handle);
}

static jlong create(JNIEnv *env, jclass clazz)
{
    return reinterpret_cast<jlong>(new ShapingResult());
}

static void dispose(JNIEnv *env, jclass clazz, jlong handle)
{
    delete toResult(handle);
}

static jboolean isBackward(JNIEnv *env, jclass clazz, jlong handle)
{
    TRShapingResultRef core = toResult(handle)->core();

    return (core && TRShapingResultIsBackward(core)) ? JNI_TRUE : JNI_FALSE;
}

static jboolean isRTL(JNIEnv *env, jclass clazz, jlong handle)
{
    TRShapingResultRef core = toResult(handle)->core();

    return (core && TRShapingResultIsRTL(core)) ? JNI_TRUE : JNI_FALSE;
}

static jint getCharStart(JNIEnv *env, jclass clazz, jlong handle)
{
    return toResult(handle)->charStart();
}

static jint getCharEnd(JNIEnv *env, jclass clazz, jlong handle)
{
    return toResult(handle)->charEnd();
}

static jint getGlyphCount(JNIEnv *env, jclass clazz, jlong handle)
{
    TRShapingResultRef core = toResult(handle)->core();

    return core ? static_cast<jint>(TRShapingResultGetGlyphCount(core)) : 0;
}

/* The memory belongs to the result of Core, and lives until it is replaced or disposed. */
static jlong getGlyphIdsPtr(JNIEnv *env, jclass clazz, jlong handle)
{
    TRShapingResultRef core = toResult(handle)->core();

    return reinterpret_cast<jlong>(core ? TRShapingResultGetGlyphIDsPtr(core) : nullptr);
}

static jlong getGlyphOffsetsPtr(JNIEnv *env, jclass clazz, jlong handle)
{
    TRShapingResultRef core = toResult(handle)->core();

    return reinterpret_cast<jlong>(core ? TRShapingResultGetGlyphOffsetsPtr(core) : nullptr);
}

static jlong getGlyphAdvancesPtr(JNIEnv *env, jclass clazz, jlong handle)
{
    TRShapingResultRef core = toResult(handle)->core();

    return reinterpret_cast<jlong>(core ? TRShapingResultGetGlyphAdvancesPtr(core) : nullptr);
}

static jlong getClusterMapPtr(JNIEnv *env, jclass clazz, jlong handle)
{
    TRShapingResultRef core = toResult(handle)->core();

    return reinterpret_cast<jlong>(core ? TRShapingResultGetClusterMapPtr(core) : nullptr);
}

/* Returns the caret edges of the shaped characters, one more than their count. */
static jfloatArray getCaretEdges(JNIEnv *env, jclass clazz, jlong handle, jbooleanArray caretStops)
{
    ShapingResult *result = toResult(handle);
    jint length = result->charEnd() - result->charStart();
    vector<TRBoolean> stops;

    if (caretStops) {
        vector<jboolean> values(length);
        env->GetBooleanArrayRegion(caretStops, 0, length, values.data());

        stops.resize(length);
        for (jint i = 0; i < length; i++) {
            stops[i] = (values[i] ? TRTrue : TRFalse);
        }
    }

    vector<jfloat> edges(length + 1);
    TRShapingResultGetCaretEdges(result->core(), caretStops ? stops.data() : nullptr, edges.data());

    jfloatArray array = env->NewFloatArray(length + 1);
    env->SetFloatArrayRegion(array, 0, length + 1, edges.data());

    return array;
}

static JNINativeMethod JNI_METHODS[] = {
    { "nCreate", "()J", (void *)create },
    { "nDispose", "(J)V", (void *)dispose },
    { "nIsBackward", "(J)Z", (void *)isBackward },
    { "nIsRTL", "(J)Z", (void *)isRTL },
    { "nGetCharStart", "(J)I", (void *)getCharStart },
    { "nGetCharEnd", "(J)I", (void *)getCharEnd },
    { "nGetGlyphCount", "(J)I", (void *)getGlyphCount },
    { "nGetGlyphIdsPtr", "(J)J", (void *)getGlyphIdsPtr },
    { "nGetGlyphOffsetsPtr", "(J)J", (void *)getGlyphOffsetsPtr },
    { "nGetGlyphAdvancesPtr", "(J)J", (void *)getGlyphAdvancesPtr },
    { "nGetClusterMapPtr", "(J)J", (void *)getClusterMapPtr },
    { "nGetCaretEdges", "(J[Z)[F", (void *)getCaretEdges },
};

jint register_com_mta_tehreer_sfnt_ShapingResult(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/sfnt/ShapingResult", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
