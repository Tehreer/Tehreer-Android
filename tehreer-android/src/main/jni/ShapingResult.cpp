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

#include <hb.h>
#include <jni.h>

#include "JavaBridge.h"
#include "ShapingResult.h"

using namespace std;
using namespace Tehreer;

ShapingResult::ShapingResult()
    : m_hbBuffer(hb_buffer_create())
    , m_glyphInfos(nullptr)
    , m_glyphPositions(nullptr)
    , m_glyphCount(0)
    , m_clusterMap()
    , m_sizeByEm(0.0)
    , m_isBackward(false)
    , m_isRTL(false)
    , m_charStart(0)
    , m_charEnd(0)
{
}

ShapingResult::~ShapingResult()
{
    hb_buffer_destroy(m_hbBuffer);
}

void ShapingResult::setup(jfloat sizeByEm, bool isBackward, bool isRTL, jint charStart, jint charEnd)
{
    m_glyphInfos = hb_buffer_get_glyph_infos(m_hbBuffer, &m_glyphCount);
    m_glyphPositions = hb_buffer_get_glyph_positions(m_hbBuffer, nullptr);

    m_sizeByEm = sizeByEm;
    m_isBackward = isBackward;
    m_isRTL = isRTL;
    m_charStart = charStart;
    m_charEnd = charEnd;

    m_clusterMap = buildClusterMap();
}

vector<jint> ShapingResult::buildClusterMap() const {
    jint codeUnitCount = m_charEnd - m_charStart;
    jint association = 0;

    vector<jint> array(codeUnitCount, -1);

    /* Traverse in reverse order so that first glyph takes priority in case of multiple
     * substitution. */
    for (jint i = m_glyphCount - 1; i >= 0; i--) {
        association = glyphClusterAt(i);
        array[association] = i;
    }

    if (isBackward()) {
        /* Assign the same glyph index to preceding codeunits. */
        for (jint i = codeUnitCount - 1; i >= 0; i--) {
            if (array[i] == -1) {
                array[i] = association;
            }

            association = array[i];
        }
    } else {
        /* Assign the same glyph index to subsequent codeunits. */
        for (jint i = 0; i < codeUnitCount; i++) {
            if (array[i] == -1) {
                array[i] = association;
            }

            association = array[i];
        }
    }

    return array;
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

static jfloat getSizeByEm(JNIEnv *env, jobject obj, jlong resultHandle)
{
    auto shapingResult = reinterpret_cast<ShapingResult *>(resultHandle);
    jfloat sizeByEm = shapingResult->sizeByEm();

    return sizeByEm;
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
    hb_codepoint_t glyphId = shapingResult->glyphIdAt(index);

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

static JNINativeMethod JNI_METHODS[] = {
    { "nCreate", "()J", (void *)create },
    { "nDispose", "(J)V", (void *)dispose },
    { "nIsBackward", "(J)Z", (void *)isBackward },
    { "nIsRTL", "(J)Z", (void *)isRTL },
    { "nGetSizeByEm", "(J)F", (void *)getSizeByEm },
    { "nGetCharStart", "(J)I", (void *)getCharStart },
    { "nGetCharEnd", "(J)I", (void *)getCharEnd },
    { "nGetCharCount", "(J)I", (void *)getCharCount },
    { "nGetGlyphCount", "(J)I", (void *)getGlyphCount },
    { "nGetGlyphId", "(JI)I", (void *)getGlyphId },
    { "nGetGlyphXOffset", "(JI)F", (void *)getGlyphXOffset },
    { "nGetGlyphYOffset", "(JI)F", (void *)getGlyphYOffset },
    { "nGetGlyphAdvance", "(JI)F", (void *)getGlyphAdvance },
    { "nGetClusterMapPtr", "(J)J", (void *)getClusterMapPtr },
};

jint register_com_mta_tehreer_sfnt_ShapingResult(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/sfnt/ShapingResult", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
